package com.pau101.fairylights.item;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;

import com.pau101.fairylights.FairyLights;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemLight extends Item {
	private List<String> theBetweenlandsInfo;

	private static final int THE_BETWEENLANDS_INFO_LINE_COUNT = 3;

	@SideOnly(Side.CLIENT)
	private IIcon[] icons;

	public ItemLight() {
		setHasSubtypes(true);
		setMaxDamage(0);
		setCreativeTab(FairyLights.fairyLightsTab);
	}

	@Override
	public String getItemStackDisplayName(ItemStack itemStack) {
		String localizedLightName = StatCollector.translateToLocal(super.getUnlocalizedName(itemStack) + '.' + LightVariant.getLightVariant(itemStack.getItemDamage()).getName() + ".name");
		if (itemStack.hasTagCompound()) {
			NBTTagCompound tagCompound = itemStack.getTagCompound();
			String localizedColor = StatCollector.translateToLocal("item.fireworksCharge." + ItemDye.field_150923_a[tagCompound.getInteger("color")]);
			return StatCollector.translateToLocalFormatted("item.fairy_lights.colored_light", localizedColor, localizedLightName);
		}
		return localizedLightName;
	}

	@Override
	public int getColorFromItemStack(ItemStack itemStack, int renderPass) {
		if (renderPass == 0 || !itemStack.hasTagCompound()) {
			return super.getColorFromItemStack(itemStack, renderPass);
		}
		return ItemDye.field_150922_c[itemStack.getTagCompound().getInteger("color")];
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List tooltip, boolean advanced) {
		if (LightVariant.getLightVariant(stack.getItemDamage()) == LightVariant.WEEDWOOD_LANTERN) {
			if (theBetweenlandsInfo == null) {
				theBetweenlandsInfo = new ArrayList<String>(THE_BETWEENLANDS_INFO_LINE_COUNT);
				String formatting = EnumChatFormatting.GRAY + "" + EnumChatFormatting.ITALIC;
				String lineKey = "item.light." + LightVariant.WEEDWOOD_LANTERN.getName() + ".line.";
				for (int line = 0; line < THE_BETWEENLANDS_INFO_LINE_COUNT; line++) {
					theBetweenlandsInfo.add(formatting + StatCollector.translateToLocal(lineKey + line));
				}
			}
			tooltip.addAll(theBetweenlandsInfo);
		}
	}

	@Override
	public void getSubItems(Item baseItem, CreativeTabs tab, List subItems) {
		LightVariant[] variants = LightVariant.values();
		for (int meta = 0; meta < variants.length; meta++) {
			LightVariant variant = variants[meta];
			if (variant != LightVariant.LUXO_BALL) {
				ItemStack baseVariantItemStack = new ItemStack(baseItem, 1, meta);
				for (int color = 0; color < 16; color++) {
					ItemStack variantItemStack = baseVariantItemStack.copy();
					variantItemStack.setTagInfo("color", new NBTTagInt(color));
					subItems.add(variantItemStack);
				}
			}
		}
	}

	@Override
	public boolean requiresMultipleRenderPasses() {
		return true;
	}

	@Override
	public IIcon getIcon(ItemStack stack, int pass) {
		return icons[stack.getItemDamage() * 2 + pass];
	}

	@Override
	public void registerIcons(IIconRegister iconRegister) {
		LightVariant[] variants = LightVariant.values();
		icons = new IIcon[variants.length * 2];
		for (int i = 0; i < variants.length; i++) {
			String name = FairyLights.MODID + ":" + variants[i].getName();
			icons[i * 2] = iconRegister.registerIcon(name + "_0");
			icons[i * 2 + 1] = iconRegister.registerIcon(name + "_1");
		}
	}
}
