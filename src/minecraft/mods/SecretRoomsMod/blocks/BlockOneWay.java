package mods.SecretRoomsMod.blocks;

import java.util.ArrayList;
import java.util.Random;

import mods.SecretRoomsMod.SecretRooms;
import mods.SecretRoomsMod.common.BlockHolder;
import mods.SecretRoomsMod.common.fake.FakeWorld;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author AbrarSyed
 */
public class BlockOneWay extends BlockContainer
{
	public BlockOneWay(int i)
	{
		super(i, Material.wood);
		setHardness(1.0F);
		setStepSound(Block.soundWoodFootstep);
		setLightOpacity(0);
		setCreativeTab(SecretRooms.tab);
	}

	@Override
	public void addCreativeItems(ArrayList itemList)
	{
		itemList.add(new ItemStack(this));
	}

	@Override
	public int quantityDropped(Random random)
	{
		return 1;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public int getLightOpacity(World world, int x, int y, int z)
	{
		return world.getBlockMetadata(x, y, z) == 1 ? 0 : 255;
	}

	@Override
	public int getRenderType()
	{
		return SecretRooms.camoRenderId;
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return true;
	}

	@SideOnly(value = Side.CLIENT)
	@Override
	public Icon getBlockTexture(IBlockAccess world, int x, int y, int z, int side)
	{
		if (!SecretRooms.displayCamo)
			return getBlockTextureFromSide(side);
		
		int metadata = world.getBlockMetadata(x, y, z);
		
		if (side != metadata)
			return Block.glass.getBlockTextureFromSide(side);

		try
		{
			TileEntityCamoFull entity = (TileEntityCamoFull) world.getBlockTileEntity(x, y, z);
			int id = entity.getCopyID();

			FakeWorld fake = SecretRooms.proxy.getFakeWorld(entity.worldObj);

			return Block.blocksList[id].getBlockTexture(fake, x, y, z, side);
		}
		catch (Throwable t)
		{
			return blockIcon;
		}
	}

	@SideOnly(value = Side.CLIENT)
	@Override
	public boolean shouldSideBeRendered(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5)
	{
		int var6 = par1IBlockAccess.getBlockId(par2, par3, par4);
		return var6 == blockID ? false : super.shouldSideBeRendered(par1IBlockAccess, par2, par3, par4, par5);
	}

	@Override
	public Icon getIcon(int i, int meta)
	{
		if (i == 5)
			return blockIcon;
		return Block.glass.getBlockTextureFromSide(i);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister par1IconRegister)
	{
		blockIcon = par1IconRegister.registerIcon(SecretRooms.TEXTURE_BLOCK_BASE);
	}

	@Override
	public void onBlockPlacedBy(World world, int i, int j, int k, EntityLiving entityliving, ItemStack stack)
	{
		int metadata = 1;
		if (entityliving instanceof EntityPlayer)
		{
			metadata = BlockOneWay.determineOrientation(world, i, j, k, (EntityPlayer) entityliving);
		}

		world.setBlockMetadataWithNotify(i, j, k, metadata, 2);
		
		// CAMO STUFF
		BlockHolder holder = getIdCamoStyle(world, i, j, k);

		TileEntityCamoFull entity = (TileEntityCamoFull) world.getBlockTileEntity(i, j, k);

		if (holder == null)
		{
			holder = new BlockHolder(1, 0, null);
		}

		entity.setBlockHolder(holder);
		PacketDispatcher.sendPacketToAllInDimension(entity.getDescriptionPacket(), world.provider.dimensionId);
	}
	
	@Override
	public void onBlockAdded(World world, int i, int j, int k)
	{
		super.onBlockAdded(world, i, j, k);
	}
	
	/**
	 * annalyses surrounding blocks and decides on a BlockID for the Camo Block to copy.
	 * @param world
	 * @param x coord
	 * @param y coord
	 * @param z coord
	 * @return the ID of the block to be copied
	 */
	private BlockHolder getIdCamoStyle(World world, int x, int y, int z)
	{
		int meta = world.getBlockMetadata(x, y, z);
		BlockHolder[] holders = this.getHoldersFromFacing(world, x, y, z, meta);

		// if there is only 1 in the PLUS SIGN checked.
		if (BlockCamoFull.isOneLeft(holders))
		{
			holders = BlockCamoFull.truncateArray(holders);
			return holders[0];
		}

		BlockHolder end = null;
		
		// first line.
		if (holders[0] != null && holders[0].equals(holders[1]))
		{
			end = holders[0];
		}
		else if (holders[2] != null && holders[2].equals(holders[3]))
		{
			end = holders[0];
		}

		if (end != null)
			return end;

		// GET MODE
		holders = BlockCamoFull.truncateArray(holders);
		return BlockCamoFull.tallyMode(holders);
	}

	@SideOnly(value = Side.CLIENT)
	@Override
	public int colorMultiplier(IBlockAccess world, int x, int y, int z)
	{
		if (!SecretRooms.displayCamo)
			return super.colorMultiplier(world, x, y, z);

		TileEntityCamoFull entity = (TileEntityCamoFull) world.getBlockTileEntity(x, y, z);

		if (entity == null)
			return super.colorMultiplier(world, x, y, z);

		FakeWorld fake = SecretRooms.proxy.getFakeWorld(entity.worldObj);
		int id = entity.getCopyID();

		if (id == 0)
			return super.colorMultiplier(world, x, y, z);

		Block fakeBlock = Block.blocksList[id];

		return fakeBlock.colorMultiplier(fake, x, y, z);
	}

	public static int determineOrientation(World world, int i, int j, int k, EntityPlayer entityplayer)
	{
		int direction = BlockPistonBase.determineOrientation(world, i, j, k, entityplayer);

		ForgeDirection dir = ForgeDirection.getOrientation(direction);

		if (!SecretRooms.proxy.getFaceAway(entityplayer.username))
		{
			dir = dir.getOpposite();
		}

		return dir.ordinal();
	}

	public BlockHolder[] getHoldersFromFacing(World world, int i, int j, int k, int direction)
	{
		BlockHolder[] holders = new BlockHolder[4];
		
		switch (direction)
			{
				case 0:
					holders[0] = BlockCamoFull.getInfo(world, i + 1, j, k);
					holders[1] = BlockCamoFull.getInfo(world, i - 1, j, k);
					holders[2] = BlockCamoFull.getInfo(world, i, j, k + 1);
					holders[3] = BlockCamoFull.getInfo(world, i, j, k - 1);
					break;

				case 1:
					holders[0] = BlockCamoFull.getInfo(world, i + 1, j, k);
					holders[1] = BlockCamoFull.getInfo(world, i - 1, j, k);
					holders[2] = BlockCamoFull.getInfo(world, i, j, k + 1);
					holders[3] = BlockCamoFull.getInfo(world, i, j, k - 1);
					break;

				case 2:
					holders[0] = BlockCamoFull.getInfo(world, i, j + 1, k);
					holders[1] = BlockCamoFull.getInfo(world, i, j - 1, k);
					holders[2] = BlockCamoFull.getInfo(world, i + 1, j, k);
					holders[3] = BlockCamoFull.getInfo(world, i - 1, j, k);
					break;

				case 3:
					holders[0] = BlockCamoFull.getInfo(world, i, j + 1, k);
					holders[1] = BlockCamoFull.getInfo(world, i, j - 1, k);
					holders[2] = BlockCamoFull.getInfo(world, i + 1, j, k);
					holders[3] = BlockCamoFull.getInfo(world, i - 1, j, k);
					break;

				case 4:
					holders[0] = BlockCamoFull.getInfo(world, i, j + 1, k);
					holders[1] = BlockCamoFull.getInfo(world, i, j - 1, k);
					holders[2] = BlockCamoFull.getInfo(world, i, j, k + 1);
					holders[3] = BlockCamoFull.getInfo(world, i, j, k - 1);
					break;

				case 5:
					holders[0] = BlockCamoFull.getInfo(world, i, j + 1, k);
					holders[1] = BlockCamoFull.getInfo(world, i, j - 1, k);
					holders[2] = BlockCamoFull.getInfo(world, i, j, k + 1);
					holders[3] = BlockCamoFull.getInfo(world, i, j, k - 1);
					break;
			}

		// return texture only
		return holders;
	}

	@Override
	public TileEntity createNewTileEntity(World var1)
	{
		return new TileEntityCamoFull();
	}
	
	@Override
	public int getFlammability(IBlockAccess world, int x, int y, int z, int metadata, ForgeDirection face)
	{
		return 0;
	}
}