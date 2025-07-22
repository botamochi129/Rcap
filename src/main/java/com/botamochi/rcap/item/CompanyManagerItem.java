package com.botamochi.rcap.item;

import com.botamochi.rcap.network.RcapServerPackets;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class CompanyManagerItem extends Item {

    public CompanyManagerItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient) {
            RcapServerPackets.sendOpenCompanyGui((ServerPlayerEntity) user);
        }
        return TypedActionResult.success(user.getStackInHand(hand));
    }
}
