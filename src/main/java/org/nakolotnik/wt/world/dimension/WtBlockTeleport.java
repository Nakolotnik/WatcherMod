package org.nakolotnik.wt.world.dimension;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.nakolotnik.wt.world.dimension.events.TimePathEvent;

public class WtBlockTeleport extends Block {

    public WtBlockTeleport() {
        super(BlockBehaviour.Properties.of().noCollission().strength(-1.0F, 3600000.0F).noLootTable());
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && entity instanceof ServerPlayer player) {
            TimePathEvent.teleportPlayerToRandomWorld(player);
        }
    }
}
