package io.github.eufranio.leaflets.config;

import com.google.common.collect.Lists;
import io.github.eufranio.leaflets.Leaflets;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Frani on 15/11/2018.
 */
@ConfigSerializable
public class MainConfig {

    @Setting
    private ItemType pageItem = ItemTypes.PAPER;

    @Setting
    private String pageName = "Magic Page - Index %index%";

    @Setting
    private List<String> pageLore = Lists.newArrayList("Craft this with a %book% book! Index: %index%");

    public ItemStack getPage(int index, String book) {
        BookConfig cfg = Leaflets.getInstance().getBook(book);
        return ItemStack.builder().fromContainer(
                ItemStack.builder()
                        .itemType(pageItem)
                        .add(Keys.DISPLAY_NAME, Leaflets.toText(pageName.replace("%index%", index+"")))
                        .add(Keys.ITEM_LORE, pageLore.stream()
                                .map(s -> s.replace("%index%", index+""))
                                .map(s -> s.replace("%book%", cfg.displayName))
                                .map(Leaflets::toText)
                                .collect(Collectors.toList()))
                .build()
                .toContainer()
                .set(DataQuery.of("UnsafeData", "leafletsPage"), index)
                .set(DataQuery.of("UnsafeData", "leafletsBook"), book)
        ).build();
    }

}
