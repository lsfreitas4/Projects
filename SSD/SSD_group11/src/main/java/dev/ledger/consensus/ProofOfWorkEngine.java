package dev.ledger.consensus;

import dev.ledger.core.Block;

public class ProofOfWorkEngine implements IConsensusEngine {
    private final int difficulty;

    public ProofOfWorkEngine(int difficulty) {
        this.difficulty = difficulty;
    }

    @Override
    public void sealBlock(Block block) {
        block.mineBlock(difficulty);
    }

    @Override
    public boolean validateConsensus(Block block) {
        String target = new String(new char[difficulty]).replace('\0', '0');
        return block.getHash().substring(0, difficulty).equals(target) 
                && block.getHash().equals(block.calculateHash());
    }
}