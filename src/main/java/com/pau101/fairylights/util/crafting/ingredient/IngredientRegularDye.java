package com.pau101.fairylights.util.crafting.ingredient;

import java.util.Collections;

import com.google.common.collect.ImmutableList;
import com.pau101.fairylights.util.DyeOreDictUtils;
import com.pau101.fairylights.util.crafting.GenericRecipe.MatchResultRegular;

import net.minecraft.item.ItemStack;

public class IngredientRegularDye implements IngredientRegular {
	@Override
	public MatchResultRegular matches(ItemStack input, ItemStack output) {
		return new MatchResultRegular(this, input, DyeOreDictUtils.isDye(input), Collections.EMPTY_LIST);
	}

	@Override
	public ImmutableList<ItemStack> getInputs() {
		return DyeOreDictUtils.getAllDyes();
	}
}
