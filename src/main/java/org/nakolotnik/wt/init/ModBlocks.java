package org.nakolotnik.wt.init;

import org.nakolotnik.wt.world.dimension.WtBlockTeleport;
import org.zeith.hammerlib.annotations.RegistryName;
import org.zeith.hammerlib.annotations.SimplyRegister;

@SimplyRegister
public interface ModBlocks {
    @RegistryName("wt_block_teleport")
    WtBlockTeleport WT_BLOCK_TELEPORT = new WtBlockTeleport();
}
