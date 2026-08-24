package com.auction.ledger.network.kademlia;

import com.auction.ledger.network.Message;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KademliaService {
    private static final Logger logger = LoggerFactory.getLogger(KademliaService.class);
    private final ObjectMapper mapper = new ObjectMapper();

    private final NodeIdentity localIdentity;
    private final RoutingTable routingTable;
    private final int networkDifficulty;
    
    // NOVO: Dicionário de armazenamento (Data Store)
    // Mapeia uma Chave Kademlia (em formato String Hexadecimal) para o valor (qualquer String/JSON)
    private final Map<String, String> dataStore = new ConcurrentHashMap<>();

    public KademliaService(NodeIdentity localIdentity, RoutingTable routingTable, int networkDifficulty) {
        this.localIdentity = localIdentity;
        this.routingTable = routingTable;
        this.networkDifficulty = networkDifficulty;
    }

    /**
     * Processa mensagens Kademlia recebidas e devolve a resposta adequada.
     */
    public Message handleMessage(Message incomingMessage, String senderIp, int senderPort) {
        try {
            switch (incomingMessage.getType()) {
                case PING_REQ:
                    logger.info("Recebido PING de {}:{}", senderIp, senderPort);
                    return createPingResponse();

                case PING_RES:
                    logger.info("Recebido PONG de {}:{}", senderIp, senderPort);
                    return null;

                case JOIN_REQ:
                    logger.info("Recebido JOIN_REQ de {}:{}", senderIp, senderPort);
                    return processJoinRequest(incomingMessage.getPayload(), senderIp, senderPort);

                case FIND_NODE_REQ:
                    logger.info("Recebido FIND_NODE de {}:{}", senderIp, senderPort);
                    return processFindNodeRequest(incomingMessage.getPayload());

                // NOVO: Processar pedidos de STORE e FIND_VALUE
                case STORE_REQ:
                    logger.info("Recebido STORE_REQ de {}:{}", senderIp, senderPort);
                    return processStoreRequest(incomingMessage.getPayload());

                case FIND_VALUE_REQ:
                    logger.info("Recebido FIND_VALUE_REQ de {}:{}", senderIp, senderPort);
                    return processFindValueRequest(incomingMessage.getPayload());

                default:
                    return null;
            }
        } catch (Exception e) {
            logger.error("Erro ao processar mensagem Kademlia", e);
            return null;
        }
    }

    // --- MÉTODOS EXISTENTES (JOIN E PING) ---

    private Message processJoinRequest(String payload, String senderIp, int senderPort) throws Exception {
        Map<String, Object> data = mapper.readValue(payload, new TypeReference<Map<String, Object>>() {});
        String nodeIdHex = (String) data.get("nodeId");
        long nonce = ((Number) data.get("nonce")).longValue();
        String pubKeyHex = (String) data.get("publicKey");

        boolean isValidPoW = verifyJoinPoW(nodeIdHex, nonce, pubKeyHex, networkDifficulty);
        Map<String, Object> responseMap = new HashMap<>();

        if (!isValidPoW) {
            logger.warn("Ataque Sybil detetado de {}:{}!", senderIp, senderPort);
            responseMap.put("accepted", false);
            return new Message(Message.MessageType.JOIN_RES, mapper.writeValueAsString(responseMap));
        }

        byte[] idBytes = Hex.decode(nodeIdHex);
        KademliaId remoteNodeId = new KademliaId(idBytes);
        NodeContact newFriend = new NodeContact(remoteNodeId, senderIp, senderPort);
        
        routingTable.addContact(newFriend);

        List<NodeContact> closestNodes = routingTable.findClosestNodes(remoteNodeId, 20);

        // --- ADIÇÃO: Garantir que o nó local está na lista de conhecidos ---
        NodeContact localContact = new NodeContact(localIdentity.getNodeId(), "127.0.0.1", 4004); // Usa a tua porta de serviço
        if (!closestNodes.stream().anyMatch(n -> n.getId().toHexString().equals(localContact.getId().toHexString()))) {
            closestNodes.add(localContact);
        }

        responseMap.put("accepted", true);
        List<Map<String, Object>> nodesList = new ArrayList<>();
        for (NodeContact contact : closestNodes) {
            Map<String, Object> nodeInfo = new HashMap<>();
            nodeInfo.put("id", contact.getId().toHexString());
            nodeInfo.put("ip", contact.getIp());
            nodeInfo.put("port", contact.getPort());
            nodesList.add(nodeInfo);
        }
        responseMap.put("closestNodes", nodesList);

        return new Message(Message.MessageType.JOIN_RES, mapper.writeValueAsString(responseMap));
    }

    private boolean verifyJoinPoW(String nodeIdHex, long nonce, String pubKeyHex, int difficultyBits) {
        try {
            byte[] pubKeyBytes = Hex.decode(pubKeyHex);
            MessageDigest digest = MessageDigest.getInstance("SHA-256", "BC");
            
            digest.update(pubKeyBytes);
            ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
            buffer.putLong(nonce);
            digest.update(buffer.array());
            
            byte[] computedHash = digest.digest();

            int bytesToCheck = difficultyBits / 8;
            int remainingBits = difficultyBits % 8;

            for (int i = 0; i < bytesToCheck; i++) {
                if (computedHash[i] != 0) return false;
            }

            if (remainingBits > 0) {
                byte mask = (byte) (0xFF << (8 - remainingBits));
                if ((computedHash[bytesToCheck] & mask) != 0) return false;
            }

            byte[] expectedIdBytes = new byte[KademliaId.ID_LENGTH];
            System.arraycopy(computedHash, 0, expectedIdBytes, 0, KademliaId.ID_LENGTH);
            String expectedIdHex = Hex.toHexString(expectedIdBytes);

            return expectedIdHex.equalsIgnoreCase(nodeIdHex);

        } catch (Exception e) {
            return false;
        }
    }

    private Message createPingResponse() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("alive", true);
        payload.put("nodeId", localIdentity.getNodeId().toHexString());
        return new Message(Message.MessageType.PING_RES, mapper.writeValueAsString(payload));
    }

    // --- NOVOS MÉTODOS DE ROTEAMENTO E ARMAZENAMENTO ---

    /**
     * Processa um pedido FIND_NODE devolvendo os K nós mais próximos do target.
     */
    private Message processFindNodeRequest(String payload) throws Exception {
        Map<String, Object> request = mapper.readValue(payload, new TypeReference<Map<String, Object>>() {});
        String targetIdHex = (String) request.get("targetId");
        
        KademliaId targetId = new KademliaId(Hex.decode(targetIdHex));
        List<NodeContact> closestNodes = routingTable.findClosestNodes(targetId, 20);
        
        Map<String, Object> response = new HashMap<>();
        response.put("closestNodes", formatNodesList(closestNodes));
        
        return new Message(Message.MessageType.FIND_NODE_RES, mapper.writeValueAsString(response));
    }

    /**
     * Processa um pedido STORE guardando a chave e o valor em memória.
     */
    private Message processStoreRequest(String payload) throws Exception {
        Map<String, Object> request = mapper.readValue(payload, new TypeReference<Map<String, Object>>() {});
        String key = (String) request.get("key");
        String value = (String) request.get("value"); // O valor deve vir como String JSON
        
        dataStore.put(key, value);
        logger.info("Dado armazenado na DHT. Chave: {}", key);
        
        // Retornar um STORE_RES simples a confirmar sucesso
        return new Message(Message.MessageType.STORE_RES, "{\"success\": true}");
    }

    /**
     * Processa um pedido FIND_VALUE.
     * Regra do Kademlia: Se temos o valor, devolvemos o valor. 
     * Se não temos, devolvemos os nós mais próximos da chave.
     */
    private Message processFindValueRequest(String payload) throws Exception {
        Map<String, Object> request = mapper.readValue(payload, new TypeReference<Map<String, Object>>() {});
        String keyHex = (String) request.get("key");
        
        Map<String, Object> response = new HashMap<>();
        
        if (dataStore.containsKey(keyHex)) {
            // Temos o valor guardado!
            logger.info("FIND_VALUE sucesso local para a chave: {}", keyHex);
            response.put("valueFound", true);
            response.put("value", dataStore.get(keyHex));
        } else {
            // Não temos. Devolver os vizinhos mais próximos.
            logger.info("FIND_VALUE falhou localmente. A devolver vizinhos para a chave: {}", keyHex);
            response.put("valueFound", false);
            
            KademliaId targetId = new KademliaId(Hex.decode(keyHex));
            List<NodeContact> closestNodes = routingTable.findClosestNodes(targetId, 20);
            response.put("closestNodes", formatNodesList(closestNodes));
        }
        
        return new Message(Message.MessageType.FIND_VALUE_RES, mapper.writeValueAsString(response));
    }

    /**
     * Helper para formatar listas de nós para JSON.
     */
    private List<Map<String, Object>> formatNodesList(List<NodeContact> nodes) {
        List<Map<String, Object>> nodesList = new ArrayList<>();
        for (NodeContact contact : nodes) {
            Map<String, Object> nodeInfo = new HashMap<>();
            nodeInfo.put("id", contact.getId().toHexString());
            nodeInfo.put("ip", contact.getIp());
            nodeInfo.put("port", contact.getPort());
            nodesList.add(nodeInfo);
        }
        return nodesList;
    }
}