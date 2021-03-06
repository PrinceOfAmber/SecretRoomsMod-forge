package com.wynprice.secretroomsmod.items;

import com.wynprice.secretroomsmod.SecretRooms5;
import com.wynprice.secretroomsmod.base.interfaces.ISecretBlock;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;

public class ProgrammableSwitchProbe extends Item
{
	public ProgrammableSwitchProbe() {
		setRegistryName("programmable_switch_probe");
		setUnlocalizedName("programmable_switch_probe");
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) 
	{
		if(worldIn.isRemote)
			SecretRooms5.proxy.displayGui(0, playerIn.getHeldItem(handIn));
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
	}
	
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
			EnumFacing facing, float hitX, float hitY, float hitZ) 
	{
		IBlockState state = worldIn.getBlockState(pos);
		if(state.getBlock() == Blocks.AIR)
			return EnumActionResult.PASS;
		ItemStackHandler handler = new ItemStackHandler(1);
		handler.setStackInSlot(0, state.getBlock().getPickBlock(state, worldIn.rayTraceBlocks(player.getPositionVector(), new Vec3d(pos)), worldIn, pos, player));
		if(state.getBlock() instanceof ISecretBlock && player.getHeldItem(hand).hasTagCompound() && player.getHeldItem(hand).getTagCompound().hasKey("hit_block", 8))
		{
			Block block = Block.REGISTRY.getObject(new ResourceLocation(player.getHeldItem(hand).getTagCompound().getString("hit_block")));
			if(!(block instanceof ISecretBlock) && block != Blocks.AIR)
			{
				((ISecretBlock)state.getBlock()).forceBlockState(worldIn, pos, BlockPos.ORIGIN, block.getStateFromMeta(player.getHeldItem(hand).getTagCompound().getInteger("hit_meta")));
				worldIn.markBlockRangeForRenderUpdate(pos.add(-1, -1, -1), pos.add(1, 1, 1));
				return EnumActionResult.SUCCESS;
			}
		}
		return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
	}
}
