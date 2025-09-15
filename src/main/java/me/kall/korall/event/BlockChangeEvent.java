package me.kall.korall.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class BlockChangeEvent extends BlockEvent {
    private final BlockState oldState;
    private final ChangeStatus changeStatus;
    private final boolean isSameThread;

    public BlockChangeEvent(BlockPos pos, ServerLevel level, @NotNull BlockState oldState, BlockState newState, boolean isSameThread) {
        super(level, pos, newState);
        this.oldState = oldState;
        this.isSameThread = isSameThread;
        this.changeStatus = oldState.isAir() ? ChangeStatus.ADD : (newState.isAir() ? ChangeStatus.REMOVE : ChangeStatus.REPLACE);

    }

    public ChangeStatus getChangeStatus() {
        return changeStatus;
    }

    public BlockState getNewState() {
        return this.getState();
    }

    public BlockState getOldState() {
        return oldState;
    }

    public boolean isSameThread() {
        return isSameThread;
    }

    @Override
    public @NotNull ServerLevel getLevel() {
        return (ServerLevel) super.getLevel();
    }

    public enum ChangeStatus {
        REMOVE, ADD, REPLACE
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static final class PoiChange extends BlockChangeEvent {
        private final Optional<Holder<PoiType>> oldPoi, newPoi;

        public PoiChange(BlockPos pos, ServerLevel level, BlockState oldState, BlockState newState, boolean isSameThread, Optional<Holder<PoiType>> oldPoi, Optional<Holder<PoiType>> newPoi) {
            super(pos, level, oldState, newState, isSameThread);
            this.oldPoi = oldPoi;
            this.newPoi = newPoi;
        }

        public Optional<Holder<PoiType>> getOldPoi() {
            return oldPoi;
        }

        public Optional<Holder<PoiType>> getNewPoi() {
            return newPoi;
        }
    }
}
