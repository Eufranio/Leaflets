package io.github.eufranio.leaflets;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import io.github.eufranio.leaflets.config.BookConfig;
import io.github.eufranio.leaflets.config.Config;
import io.github.eufranio.leaflets.config.MainConfig;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.recipe.crafting.CraftingRecipe;
import org.spongepowered.api.item.recipe.crafting.Ingredient;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.File;
import java.util.List;

@Plugin(
        id = "leaflets",
        name = "Leaflets",
        authors = {
                "Eufranio"
        }
)
public class Leaflets {

    @Inject
    private Logger logger;

    @Inject
    @ConfigDir(sharedRoot = false)
    private File configDir;

    private List<BookConfig> books = Lists.newArrayList();

    public Config<MainConfig> config;

    @Listener
    public void onPreInit(GamePreInitializationEvent e) {
        Sponge.getRegistry().getCraftingRecipeRegistry().register(new BookUpgradeRecipe());
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        this.config = new Config<>(MainConfig.class, "Leaflets.conf", configDir);
        this.loadBooks();

        CommandSpec givePage = CommandSpec.builder()
                .permission("leaflets.givepage")
                .arguments(
                        GenericArguments.string(Text.of("book name")),
                        GenericArguments.integer(Text.of("index")),
                        GenericArguments.player(Text.of("player"))
                )
                .executor((sender, context) -> {
                    String bookName = context.<String>getOne("book name").get();
                    int index = context.<Integer>getOne("index").get();
                    Player player = context.<Player>getOne("player").get();

                    ItemStack page = this.config.get().getPage(index, bookName);
                    player.getInventory().offer(page);

                    sender.sendMessage(Text.of(
                            TextColors.GREEN, "Successfully given page!"
                    ));
                    return CommandResult.success();
                })
                .build();

        CommandSpec giveBook = CommandSpec.builder()
                .permission("leaflets.givebook")
                .arguments(
                        GenericArguments.string(Text.of("book name")),
                        GenericArguments.player(Text.of("player"))
                )
                .executor((sender, context) -> {
                    String name = context.<String>getOne("book name").get();
                    Player player = context.<Player>getOne("player").get();

                    ItemStack book = this.books.stream()
                            .filter(b -> b.name.equalsIgnoreCase(name))
                            .findFirst().orElseThrow(() -> new CommandException(Text.of("Unknown book!")))
                            .getBook();
                    player.getInventory().offer(book);

                    sender.sendMessage(Text.of(
                            TextColors.GREEN, "Successfully given book!"
                    ));
                    return CommandResult.success();
                })
                .build();

        CommandSpec main = CommandSpec.builder()
                .permission("leaflets.main")
                .child(giveBook, "giveBook")
                .child(givePage, "givePage")
                .build();

        Sponge.getCommandManager().register(this, main, "leaflets");
    }

    @Listener
    public void onReload(GameReloadEvent e) {
        this.config.reload();
        this.loadBooks();
    }

    private void loadBooks() {
        this.books.clear();

        File booksDir = new File(configDir, "books");
        if (!booksDir.exists()) {
            booksDir.mkdirs();
        }

        new Config<>(BookConfig.class, "default.conf", booksDir);

        for (File file : booksDir.listFiles()) {
            this.books.add(new Config<>(BookConfig.class, file.getName(), booksDir).get());
        }
    }

    public BookConfig getBook(String name) {
        return this.books
                .stream()
                .filter(b -> b.name.equalsIgnoreCase(name))
                .findFirst().orElse(null);
    }

    public static Text toText(String s) {
        return TextSerializers.FORMATTING_CODE.deserialize(s);
    }

    public static Leaflets getInstance() {
        return (Leaflets) Sponge.getPluginManager().getPlugin("leaflets")
                .get()
                .getInstance()
                .get();
    }


}
