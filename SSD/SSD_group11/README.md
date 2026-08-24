# Secure Decentralized Public Auction Ledger

A secure-by-design, containerized, three-tier distributed prototype implementing a decentralized, permissionless public ledger that records single-attribute English auction activity.

## Architecture Overview

This distributed system is composed of three interconnected architectural tiers:
1. **Secure P2P Overlay Substrate (Part A):** A fault-tolerant Kademlia Distributed Hash Table (DHT) handling peer discovery, node bootstrapping, metadata placement, and rumor-mongering gossip routing over an adverse environment prone to churn.
2. **Secure Distributed Ledger Layer (Part B):** An append-only, structurally validated blockchain that groups signed transactions into blocks, ordering transactions transparently through a Proof-of-Work (PoW) consensus design engine.
3. **Auction Application Domain (Part C):** A Publisher/Subscriber interactions system where sellers host auction structures, buyers broadcast cryptographic bids, and all state mutations settle directly into the underlying blocks.

---

## Security Engineering Invariants & Threat Model

The prototype enforces strict threat mitigations to defend the system against common distributed attack vectors:

### Identity & Authenticity
* **Identity Spoofing Prevention:** Every ledger participant is bound to an asymmetric BouncyCastle ECDSA key pair. Node identities (`nodeId`) are derived from their cryptographic keys.
* **Transaction Validity:** Every ledger transaction requires a valid DER-encoded signature checked via `verifySignature()`. Unsigned or weakly signed transactions are rejected.

### Ledger Integrity & Anti-Replay
* **Immutability:** Blocks track transaction sets via pairwise cryptographic binary Merkle tree headers (`MerkleEngine`). Tampering with block states invalidates parent hash commitments.
* **Replay Attack Mitigation:** To prevent duplicate bids or double-spending exploits, every transaction is assigned a monotonically incremented account sequence `nonce`. Blocks packing out-of-order nonces fail context evaluation.

### Consensus Hardening
* **Forking & Double Spend Controls:** Validator nodes enforce the longest chain rule. If concurrent block splits emerge, miners converge onto the heaviest structural PoW trajectory.

### System Execution Flow
1. User submits auction action (CREATE / START / BID / END, etc)
2. AuctionCLI converts it into AuctionCommand
3. AuctionCommand is wrapped into a Transaction
4. Transaction is broadcast via P2P (gossip network)
5. Nodes validate and store it in TransactionPool
6. Miner selects transactions and creates a Block
7. Proof-of-Work seals the block
8. BlockchainContext validates:
   - Previous hash
   - Merkle root
   - Signatures
   - Nonce ordering
9. Block is appended and propagated to peers
10. AuctionManager rebuilds auction state from full chain

---

## Project Structure

```text
secure-auction-blockchain/
│
├── docker-compose.yml
├── .env
├── Dockerfile
├── pom.xml
├── run.bat
├── target/
│
└── src/
    └── main/
        └── java/
            └── com/
                └── auction/
                    │
                    ├── auction/                   # Lógica de domínio do sistema de leilões
                    │   ├── AuctionCommand         # Representa comandos de leilão (CREATE, BID, START, END)
                    │   ├── AuctionManager         # Reconstrói e gere estado dos leilões a partir da blockchain
                    │   ├── TransactionPool        # Memória temporária de transações pendentes antes de mineração
                        ├── AuctionCLI              # CLI para criar leilões, dar bids e interagir com o ledger
                    │   └── Auction                 # Camada de domínio de leilões (regras de negócio + estado dos leilões)
                    │
                    ├── ledger/
                        ├── LedgerNode              # Main entrypoint do nó: inicializa blockchain, P2P, Kademlia, mining e CLI
                        │
                    │   ├── config/
                    │   │   └── LedgerConfig      # Configuração do nó (rede, mineração, storage, API)
                    │   │
                    │   └── network/              # Camada de comunicação P2P e Kademlia DHT
                    │       ├── Message            # Estrutura genérica de mensagens da rede
                    │       ├── P2PClient         # Cliente P2P para ligação a peers e bootstrap
                    │       ├── P2PServer         # Servidor P2P com gossip e processamento de mensagens
                    │       ├── LedgerNode        # Ponto de entrada principal do nó blockchain
                    │       │
                    │       └── kademlia/         # Implementação da rede Kademlia DHT
                    │           ├── KademliaId     # Identificador XOR usado na DHT
                    │           ├── KademliaService# Lógica RPC (JOIN, STORE, FIND_NODE, FIND_VALUE)
                    │           ├── KBucket        # Estrutura de armazenamento de vizinhos
                    │           ├── NodeContact    # Representação de um nó na rede
                    │           ├── NodeIdentity   # Identidade criptográfica do nó + PoW
                    │           └── RoutingTable   # Tabela de roteamento baseada em XOR distance
                    │
                    └── dev/
                        └── ledger/
                            │
                            ├── consensus/
                            │   ├── IConsensusEngine  # Interface do algoritmo de consenso
                            │   └── ProofOfWorkEngine # Implementação de Proof-of-Work
                            │
                            └── core/
                                ├── Block             # Estrutura de bloco da blockchain
                                ├── BlockchainContext # Estado global e validação da cadeia
                                ├── MerkleEngine     # Construção da Merkle Tree
                                └── Transaction      # Transação assinada do ledger