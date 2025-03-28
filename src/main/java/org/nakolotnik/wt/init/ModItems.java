package org.nakolotnik.wt.init;


import net.minecraft.world.item.Item;
import org.nakolotnik.wt.item.TimeShardItem;
import org.zeith.hammerlib.annotations.RegistryName;
import org.zeith.hammerlib.annotations.SimplyRegister;

@SimplyRegister
public interface ModItems {
    @RegistryName("time_shard")
    Item TIME_SHARD = new TimeShardItem();
}
