package io.github.eufranio.leaflets.config;

import com.google.common.collect.Lists;
import io.github.eufranio.leaflets.Leaflets;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Frani on 15/11/2018.
 */
@ConfigSerializable
public class BookConfig {

    @Setting
    public String name = "default";

    @Setting
    public String displayName = "&6Magic Book";

    @Setting
    private String author = "Notch";

    @Setting
    private List<String> lore = Lists.newArrayList("Have fun with this book!");

    @Setting
    private String nextPage = "Next page: %next%";

    @Setting
    private String completed = "&aCompleted!";

    @Setting
    public BookPage initialPage = new BookPage();

    @Setting
    public List<BookPage> pages = Lists.newArrayList(new BookPage());

    public List<Text> getPages(int amount) {
        List<Text> pages = Lists.newArrayList(initialPage.getText());
        pages.addAll(this.pages.subList(0, amount).stream().map(BookPage::getText).collect(Collectors.toList()));
        return pages;
    }

    @ConfigSerializable
    public static class BookPage {

        @Setting
        private String book = "default";

        @Setting
        private int index = 1;

        @Setting
        private List<String> lines = Lists.newArrayList(
                "This is a",
                "book page",
                "page index: %index%"
        );

        public Text getText() {
            return Text.joinWith(Text.NEW_LINE, lines.stream().map(s ->
                    s.replace("%index%", "" + this.index)
                    .replace("%book%", this.book)
            )
                    .map(Leaflets::toText)
                    .collect(Collectors.toList()));
        }

    }

    private List<Text> getLore(boolean completed, int next) {
        List<Text> lore = this.lore.stream()
                .map(Leaflets::toText)
                .collect(Collectors.toList());
        if (completed) {
            lore.add(Leaflets.toText(this.completed));
        } else {
            lore.add(Leaflets.toText(this.nextPage.replace("%next%", ""+next)));
        }
        return lore;
    }

    public ItemStack getBook() {
        return ItemStack.builder().fromContainer(
                ItemStack.builder()
                        .itemType(ItemTypes.WRITTEN_BOOK)
                        .add(Keys.BOOK_AUTHOR, Leaflets.toText(this.author))
                        .add(Keys.BOOK_PAGES, Lists.newArrayList(this.initialPage.getText()))
                        .add(Keys.DISPLAY_NAME, Leaflets.toText(this.displayName))
                        .add(Keys.ITEM_LORE, this.getLore(false, 1))
                        .build()
                        .toContainer()
                        .set(DataQuery.of("UnsafeData", "nextPage"), 1)
                        .set(DataQuery.of("UnsafeData", "bookName"), this.name)
        ).build();
    }

    public ItemStack updateBook(ItemStack book) {
        int next = book.toContainer().get(DataQuery.of("UnsafeData", "nextPage"))
                .map(Object::toString)
                .map(Integer::parseInt)
                .get() + 1;

        boolean completed = book.get(Keys.BOOK_PAGES).get().size() == this.pages.size();
        book.offer(Keys.ITEM_LORE, this.getLore(completed, next));
        book.offer(Keys.BOOK_PAGES, this.getPages(next - 1));

        book = ItemStack.builder()
                .fromContainer(
                        book.toContainer()
                        .set(DataQuery.of("UnsafeData", "nextPage"), completed ? next - 1 : next)
                ).build();
        return book;
    }

    public boolean isCompleted(ItemStack book) {
        return book.get(Keys.BOOK_PAGES).get().size() == (this.pages.size() + 1);
    }

}
