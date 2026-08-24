package com.auction.ledger.network;

import com.auction.auction.TransactionPool;
import dev.ledger.core.Block;
import dev.ledger.core.BlockchainContext;
import dev.ledger.core.Transaction;
import com.auction.ledger.network.kademlia.KademliaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class P2PServer {
    private static final Logger logger = LoggerFactory.getLogger(P2PServer.class);
    private final int port;
    private final BlockchainContext context;
    private final KademliaService kademliaService;
    private final TransactionPool txPool;

    // Lista global e thread-safe de todos os canais TCP ativos ligados a este servidor
    private static final List<Channel> connectedPeers = new CopyOnWriteArrayList<>();

    // Constante do protocolo Gossip: número de nós aleatórios para quem reencaminhamos a mensagem (Fanout)
    private static final int GOSSIP_FANOUT = 3;

    public P2PServer(int port, BlockchainContext context, KademliaService kademliaService, TransactionPool txPool) {
        this.port = port;
        this.context = context;
        this.kademliaService = kademliaService;
        this.txPool = txPool;
    }

    public void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .childHandler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 public void initChannel(SocketChannel ch) {
                     ChannelPipeline p = ch.pipeline();
                     p.addLast(new LineBasedFrameDecoder(10485760));
                     p.addLast(new StringDecoder(CharsetUtil.UTF_8));
                     p.addLast(new StringEncoder(CharsetUtil.UTF_8));
                     p.addLast(new ServerHandler(context, kademliaService, txPool));
                 }
             });

            ChannelFuture f = b.bind(port).sync();
            logger.info("P2P Server listening for peers on port {}", port);
            
        } catch (InterruptedException e) {
            logger.error("P2P Server interrupted", e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Envia uma mensagem para uma sub-seleção aleatória de canais TCP ativos (Epidemic Gossip),
     * excluindo o canal de onde a mensagem veio originalmente.
     */
    public static void relayGossip(Message message, Channel originChannel) {
        if (connectedPeers.isEmpty()) return;

        List<Channel> targets = new ArrayList<>(connectedPeers);
        // Evitar mandar de volta para quem nos enviou a mensagem
        if (originChannel != null) {
            targets.remove(originChannel);
        }

        // Se houver mais peers do que o nosso Fanout, baralhamos e escolhemos apenas GOSSIP_FANOUT nós
        if (targets.size() > GOSSIP_FANOUT) {
            Collections.shuffle(targets);
            targets = targets.subList(0, GOSSIP_FANOUT);
        }

        String rawPayload = message.toJsonLine();
        logger.info("Gossip Relay: A replicar mensagem [{}] para {} peers aleatórios.", message.getType(), targets.size());
        
        for (Channel ch : targets) {
            if (ch.isActive()) {
                ch.writeAndFlush(rawPayload);
            }
        }
    }

    // Inner handler for incoming messages
    private static class ServerHandler extends SimpleChannelInboundHandler<String> {
        private final BlockchainContext context;
        private final KademliaService kademliaService;
        private final TransactionPool txPool; // Nova dependência da Pool
        private final ObjectMapper mapper = new ObjectMapper(); 

        // Cache de Deduplicação global para evitar tempestades de mensagens (Gossip Storms)
        private static final Set<String> seenMessagesCache = Collections.newSetFromMap(new ConcurrentHashMap<>());

        public ServerHandler(BlockchainContext context, KademliaService kademliaService, TransactionPool txPool) {
            this.context = context;
            this.kademliaService = kademliaService;
            this.txPool = txPool;
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            logger.info("New peer connected from: {}", ctx.channel().remoteAddress());
            connectedPeers.add(ctx.channel());
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            logger.info("Peer disconnected: {}", ctx.channel().remoteAddress());
            connectedPeers.remove(ctx.channel());
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) {
            try {
                Message message = Message.fromJson(msg);

                java.net.InetSocketAddress remoteAddress = (java.net.InetSocketAddress) ctx.channel().remoteAddress();
                String senderIp = remoteAddress.getAddress().getHostAddress();
                int senderPort = remoteAddress.getPort();

                // 1. Processamento da camada P2P Kademlia
                Message kademliaResponse = kademliaService.handleMessage(message, senderIp, senderPort);
                if (kademliaResponse != null) {
                    ctx.writeAndFlush(kademliaResponse.toJsonLine());
                    return; 
                }

                // 2. Processamento da Camada de Distribuição do Ledger (Protocolo Gossip Seguro)
                if (message.getType() == Message.MessageType.NEW_TRANSACTION) {
                    Transaction tx = mapper.readValue(message.getPayload(), Transaction.class);
                    String txId = tx.getTransactionId();

                    if (seenMessagesCache.contains(txId)) return;
                    seenMessagesCache.add(txId);

                    logger.info("Received new transaction over network: {}", txId);
                    
                    // Integração com a Pool
                    if (txPool.addTransaction(tx)) {
                        logger.info("Transação aceite na pool com sucesso. A propagar...");
                        P2PServer.relayGossip(message, ctx.channel());
                    } else {
                        logger.warn("Transação rejeitada pela pool (Assinatura inválida ou Nonce antigo/Replay).");
                    }
                } 
                
                else if (message.getType() == Message.MessageType.NEW_BLOCK) {
                    Block block = mapper.readValue(message.getPayload(), Block.class);
                    String blockHash = block.getHash();

                    if (seenMessagesCache.contains(blockHash)) return;
                    seenMessagesCache.add(blockHash);

                    logger.info("Received new block over network: {}", blockHash);
                    
                    if (context.addBlock(block)) {
                        logger.info("Block successfully validated and appended!");
                        P2PServer.relayGossip(message, ctx.channel());
                    } else {
                        logger.warn("Received block was invalid. Reencaminhamento abortado.");
                    }
                } 
                
                else if (message.getType().name().contains("_REQ") || message.getType().name().contains("_RES")) {
                    logger.debug("Kademlia message processed silently: {}", message.getType());
                }
                else {
                    logger.warn("Unknown message type: {}", message.getType());
                }

            } catch (Exception e) {
                logger.warn("Received malformed P2P message", e);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            logger.error("Connection error", cause);
            ctx.close();
        }
    }
}