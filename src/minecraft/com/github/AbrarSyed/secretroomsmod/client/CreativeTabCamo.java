package com.github.AbrarSyed.secretroomsmod.client;

import com.github.AbrarSyed.secretroomsmod.common.SecretRooms;

import net.minecraft.creativetab.CreativeTabs;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class CreativeTabCamo extends CreativeTabs
{
	public CreativeTabCamo()
	{
		super("Secret Items");
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getTabIconItemIndex()
	{
		return SecretRooms.camoPaste.itemID;
	}

}
