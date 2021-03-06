package com.iamshift.mineaddons.potions;

import com.iamshift.mineaddons.core.Refs;
import com.iamshift.mineaddons.init.ModPotions;

import net.minecraft.client.Minecraft;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public class PotionWitherProof extends Potion
{
	public PotionWitherProof(String name)
	{
		super(false, 6579455);
		setPotionName("potion." + name);
		setRegistryName(Refs.ID, name);
		setIconIndex(1, 0);
		
		setBeneficial();
		
		ModPotions.potions.add(this);
	}
	
	@Override
	public boolean isReady(int duration, int amplifier) 
	{
		return true;
	}
	
	@Override
	public boolean shouldRender(PotionEffect effect) 
	{
		return false;
	}
	
	@Override
	public boolean shouldRenderInvText(PotionEffect effect) 
	{
		return false;
	}
	
	@Override
	public int getStatusIconIndex() 
	{
		Minecraft.getMinecraft().renderEngine.bindTexture(ModPotions.icon);
		return super.getStatusIconIndex();
	}
}
