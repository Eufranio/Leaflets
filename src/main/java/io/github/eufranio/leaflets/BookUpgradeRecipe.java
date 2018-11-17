package io.github.eufranio.leaflets;

import com.google.common.collect.Lists;
import io.github.eufranio.leaflets.config.BookConfig;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.crafting.CraftingGridInventory;
import org.spongepowered.api.item.recipe.crafting.CraftingRecipe;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Optional;

/**
 * Created by Frani on 15/11/2018.
 */
public class BookUpgradeRecipe implements CraftingRecipe {

    @Override
    public boolean isValid(CraftingGridInventory grid, World world) {
        boolean hasBook = false;
        String bookName = null;
        int nextPage = -1;
        boolean hasPage = false;

        // first checks if a custom written book exists, and
        // stores its name if it exists
        for (Inventory slot : grid.slots()) {
            ItemStack stack = slot.peek().orElse(null);
            if (stack == null) continue;

            if (stack.getQuantity() != 1) return false;

            if (stack.getType() == ItemTypes.WRITTEN_BOOK) {
                Optional<Object> value = stack.toContainer().get(DataQuery.of("UnsafeData", "nextPage"));
                if (value.isPresent()) {
                    if (hasBook) return false;
                    hasBook = true;
                    nextPage = Integer.parseInt(value.get().toString());
                    bookName = stack.toContainer().get(DataQuery.of("UnsafeData", "bookName")).get().toString();

                    BookConfig config = Leaflets.getInstance().getBook(bookName);
                    if (config.isCompleted(stack)) {
                        return false;
                    }
                }
            }
        }

        // checks if a custom page exists, and if it does,
        // checks if it refers to the right book
        for (Inventory slot : grid.slots()) {
            ItemStack stack = slot.peek().orElse(null);
            if (stack == null || stack.getQuantity() != 1) continue;
            if (stack.getType() == ItemTypes.PAPER) {
                Optional<Object> value = stack.toContainer().get(DataQuery.of("UnsafeData", "leafletsPage"));
                if (value.isPresent()) {
                    if (hasPage) return false;
                    String book = stack.toContainer().get(DataQuery.of("UnsafeData", "leafletsBook")).get().toString();
                    if (!book.equalsIgnoreCase(bookName)) return false;

                    // only accepts if the next needed page is the one we have
                    hasPage = Integer.parseInt(value.get().toString()) == nextPage;
                }
            }
        }

        return hasBook && hasPage;
    }

    @Override
    public ItemStackSnapshot getResult(CraftingGridInventory grid) {
        for (Inventory slot : grid.slots()) {
            ItemStack stack = slot.peek().orElse(null);
            if (stack == null) continue;
            if (stack.getType() == ItemTypes.WRITTEN_BOOK) {
                BookConfig book = stack.toContainer().get(DataQuery.of("UnsafeData", "bookName"))
                        .map(Object::toString)
                        .map(s -> Leaflets.getInstance().getBook(s))
                        .get();
                return book.updateBook(stack).createSnapshot();
            }
        }
        return null;
    }

    @Override
    public List<ItemStackSnapshot> getRemainingItems(CraftingGridInventory grid) {
        List<ItemStackSnapshot> items = Lists.newArrayList();
        for (int i = 0; i < 9; i++) {
            items.add(ItemTypes.NONE.getTemplate());
        }
        return items;
    }

    @Override
    public Optional<String> getGroup() {
        return Optional.empty();
    }

    @Override
    public String getId() {
        return "leaflets:bookrecipe";
    }

    @Override
    public String getName() {
        return "BookUpgradeRecipe";
    }

    @Override
    public ItemStackSnapshot getExemplaryResult() {
        return ItemTypes.WRITTEN_BOOK.getTemplate();
    }
}
