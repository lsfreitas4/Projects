package com.auction.ledger.network;

import dev.ledger.core.Block;
import dev.ledger.core.BlockchainContext;
import dev.ledger.core.Transaction;
import com.auction.auction.TransactionPool;
import com.auction.ledger.network.kademlia.KademliaId;
import com.auction.ledger.network.kademlia.NodeContact;
import com.auction.ledger.network.kademlia.NodeIdentity;
import com.auction.ledger.network.kademlia.RoutingTable;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class P2PClient {
    private static final Logger logger = LoggerFactory.getLogger(P2PClient.class);
    private final EventLoopGroup group = new NioEventLoopGroup();
    private final List<Channel> activePeers = new ArrayList<>();
    private final ObjectMapper mapper = new ObjectMapper();
    
    private final RoutingTable routingTable;
    private final BlockchainContext context;
    private final TransactionPool txPool;

    public P2PClient(RoutingTable routingTable, BlockchainContext context, TransactionPool txPool) {
        this.routingTable = routingTable;
        this.context = context;
        this.txPool = txPool;
    }

    public void connectToPeers(String peers, NodeIdentity myIdentity) {
        if (peers == null || peers.trim().isEmpty()) {
            logger.info("Nenhum peer inicial configurado. A aguardar conexões inbound.");
            return;
        }

        String[] peerList = peers.split(",");
        for (String peer : peerList) {
            String[] parts = peer.split(":");
            if (parts.length == 2) {
                connect(parts[0], Integer.parseInt(parts[1]), myIdentity);
            }
        }
    }

    private void connect(String host, int port, NodeIdentity myIdentity) {
        Bootstrap b = new Bootstrap();
        b.group(group)
         .channel(NioSocketChannel.class)
         .handler(new ChannelInitializer<SocketChannel>() {
             @Override
             public void initChannel(SocketChannel ch) {
                 ChannelPipeline p = ch.pipeline();
                 p.addLast(new LineBasedFrameDecoder(10485760));
                 p.addLast(new StringDecoder(CharsetUtil.UTF_8));
                 p.addLast(new StringEncoder(CharsetUtil.UTF_8));
                 p.addLast(new ClientHandler());
             }
         });

        b.connect(host, port).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                logger.info("Conectado com sucesso ao nó bootstrap {}:{}", host, port);
                activePeers.add(future.channel());
                
                try {
                    // O Envio do Nonce para evitar o NullPointerException
                    Map<String, Object> joinData = new HashMap<>();
                    joinData.put("id", myIdentity.getNodeId().toHexString());
                    joinData.put("ip", host);
                    joinData.put("port", port);
                    joinData.put("nonce", myIdentity.getNonce());

                    Message msg = new Message(Message.MessageType.JOIN_REQ, mapper.writeValueAsString(joinData));
                    future.channel().writeAndFlush(msg.toJsonLine());
                    logger.info("JOIN_REQ enviado para o nó bootstrap.");
                } catch (Exception e) { 
                    logger.error("Erro ao construir JOIN_REQ", e); 
                }
            } else {
                // Auto-Reconnect se o Nó 1 ainda estiver a iniciar
                logger.warn("O Nó Principal ({}:{}) ainda não está pronto. A tentar novamente em 5 segundos...", host, port);
                group.schedule(() -> connect(host, port, myIdentity), 5, java.util.concurrent.TimeUnit.SECONDS);
            }
        });
    }

    public void broadcast(Message message) {
        String rawPayload = message.toJsonLine();
        for (Channel ch : activePeers) {
            if (ch.isActive()) ch.writeAndFlush(rawPayload);
        }
    }

    private class ClientHandler extends SimpleChannelInboundHandler<String> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) {
            try {
                Message message = mapper.readValue(msg, Message.class);
                
                if (message.getType() == Message.MessageType.JOIN_RES) {
                    Map<String, Object> data = mapper.readValue(message.getPayload(), new TypeReference<Map<String, Object>>() {});
                    if ((Boolean) data.get("accepted")) {
                        logger.info("Rede Kademlia Aceite! RoutingTable atualizada.");
                    }
                } 
                else if (message.getType() == Message.MessageType.NEW_BLOCK) {
                    Block block = mapper.readValue(message.getPayload(), Block.class);
                    if (context.addBlock(block)) {
                        logger.info("Bloco recebido do Nó Principal e validado com sucesso! A Blockchain sincronizou.");
                    }
                } 
                else if (message.getType() == Message.MessageType.NEW_TRANSACTION) {
                    Transaction tx = mapper.readValue(message.getPayload(), Transaction.class);
                    if (txPool.addTransaction(tx)) {
                        logger.info("Transação recebida da rede e adicionada à Pool.");
                    }
                }
            } catch (Exception e) {
                logger.debug("Erro ao processar mensagem no cliente: {}", e.getMessage());
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            logger.info("Ligação perdida com o peer: {}", ctx.channel().remoteAddress());
            activePeers.remove(ctx.channel());
        }
    }
}