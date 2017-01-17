package com.pau101.fairylights.util.crafting.ingredient;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.pau101.fairylights.util.crafting.GenericRecipe.MatchResult;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public interface Ingredient<I extends Ingredient, M extends MatchResult<I, M>> {
	/**
	 * Provides an immutable list of stacks that will match this ingredient.
	 *
	 * @return Immutable list of potential inputs for this ingredient
	 */
	ImmutableList<ItemStack> getInputs();

	/**
	 * Provides an immutable list of stacks which are required to craft the given output stack.
	 *
	 * Only auxiliary ingredients should provide multiple.
	 *
	 * Must be overriden by implementors which modify the output stack to provide accurate recipes for JEI.
	 *
	 * @return Immutable copy of stacks required to produce output
	 */
	default ImmutableList<ImmutableList<ItemStack>> getInput(ItemStack output) {
		return ImmutableList.of(getInputs());
	}

	M matches(ItemStack input, @Nullable ItemStack output);

	default boolean dictatesOutputType() {
		return false;
	}

	default void present(ItemStack output) {}

	default void absent(ItemStack output) {}

	default ImmutableList<ItemStack> getMatchingSubtypes(ItemStack stack) {
		Objects.requireNonNull(stack, "stack");
		List<ItemStack> subtypes = new ArrayList<>();
		Item item = stack.getItem();
		for (CreativeTabs tab : item.getCreativeTabs()) {
			item.getSubItems(item, tab, subtypes);
		}
		Iterator<ItemStack> iter = subtypes.iterator();
		while (iter.hasNext()) {
			if (!matches(iter.next(), null).doesMatch()) {
				iter.remove();
			}
		}
		return ImmutableList.copyOf(subtypes);
	}

	default void addTooltip(List<String> tooltip) {}
}
