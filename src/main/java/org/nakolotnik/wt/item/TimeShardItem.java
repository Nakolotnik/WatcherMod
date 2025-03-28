package org.nakolotnik.wt.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import org.nakolotnik.wt.procedures.TimeProcedure;
import org.nakolotnik.wt.world.dimension.events.TimePathEvent;


public class TimeShardItem extends Item {
    public TimeShardItem() {
        super(new Item.Properties()
                .stacksTo(16)
                .fireResistant()
                .rarity(Rarity.EPIC));
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!world.isClientSide) {
            if (player instanceof ServerPlayer serverPlayer) {
                String dimName = world.dimension().location().toString();

                if (dimName.contains("timeless_wasteland")) {
                    TimePathEvent.generatePathForPlayer(serverPlayer);
                    player.sendSystemMessage(Component.translatable("item.wt.time_shard.timeless_message"));
                } else if (dimName.equals("minecraft:overworld")) {
                    TimeProcedure.execute(world, serverPlayer);
                    player.sendSystemMessage(Component.translatable("item.wt.time_shard.overworld_message"));
                } else {
                    player.sendSystemMessage(Component.translatable("item.wt.time_shard.invalid_dimension_message"));
                }

            }
        }
        if (!player.getAbilities().instabuild) {
            itemStack.shrink(1);
        }
        return InteractionResultHolder.success(itemStack);
    }


    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return false;
    }


}
