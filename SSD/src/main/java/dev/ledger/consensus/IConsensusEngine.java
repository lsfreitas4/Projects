package dev.ledger.consensus;

import dev.ledger.core.Block;

public interface IConsensusEngine {
    void sealBlock(Block block);
    boolean validateConsensus(Block block);
}