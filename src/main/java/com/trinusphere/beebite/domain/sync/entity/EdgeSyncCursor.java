package com.trinusphere.beebite.domain.sync.entity;

import com.trinusphere.beebite.domain.shared.StoreGuard;

import java.util.UUID;

public class EdgeSyncCursor implements StoreGuard.StoreScoped {
    private final UUID storeId;
    private long lastContiguousSequence;

    private EdgeSyncCursor(UUID storeId, long lastContiguousSequence) {
        if (storeId == null) {
            throw new IllegalArgumentException("storeId é obrigatório.");
        }
        this.storeId = storeId;
        this.lastContiguousSequence = Math.max(0L, lastContiguousSequence);
    }

    public static EdgeSyncCursor initialize(UUID storeId, long baseline) {
        return new EdgeSyncCursor(storeId, baseline);
    }

    public static EdgeSyncCursor reconstitute(UUID storeId, long lastContiguousSequence) {
        return new EdgeSyncCursor(storeId, lastContiguousSequence);
    }

    public boolean isDuplicate(long sequence) {
        return sequence <= lastContiguousSequence;
    }

    public boolean isNext(long sequence) {
        return sequence == lastContiguousSequence + 1L;
    }

    public void advanceTo(long sequence) {
        if (!isNext(sequence)) {
            throw new IllegalStateException(
                    "Sequência fora de ordem: esperado " + (lastContiguousSequence + 1L) + ", recebido " + sequence);
        }
        this.lastContiguousSequence = sequence;
    }

    public boolean hasUnrecoverableGap(long firstAvailableSequence) {
        return lastContiguousSequence < firstAvailableSequence - 1L;
    }

    public void rebase(long newBaseline) {
        this.lastContiguousSequence = Math.max(0L, newBaseline);
    }

    @Override
    public UUID getStoreId() {
        return storeId;
    }

    public long getLastContiguousSequence() {
        return lastContiguousSequence;
    }
}

