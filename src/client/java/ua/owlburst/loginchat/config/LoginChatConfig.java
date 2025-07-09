package ua.owlburst.loginchat.config;

import com.google.gson.GsonBuilder;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import dev.isxander.yacl3.config.ConfigEntry;
import dev.isxander.yacl3.config.ConfigInstance;
import dev.isxander.yacl3.config.GsonConfigInstance;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import ua.owlburst.loginchat.LoginChatClient;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class LoginChatConfig{
    private static final File MINECRAFT_CONFIG_FOLDER = new File(MinecraftClient.getInstance().runDirectory.getPath(), "config");
    public static final File MOD_CONFIG_FOLDER = new File(MINECRAFT_CONFIG_FOLDER, LoginChatClient.MOD_ID);
    public static final ConfigInstance<LoginChatConfig> HANDLER = GsonConfigInstance.createBuilder(LoginChatConfig.class)
            .setPath(Path.of(MOD_CONFIG_FOLDER.getParentFile().getPath(), LoginChatClient.MOD_ID + ".json"))
            .overrideGsonBuilder(new GsonBuilder().setPrettyPrinting())
            .build();

    @ConfigEntry
    public boolean isEnabledInSingleplayer = false;
    @ConfigEntry
    public List<String> serversList = new ArrayList<>();
    @ConfigEntry
    public List<String> commandsList = new ArrayList<>();

    @ConfigEntry
    public boolean isListPerServer = false;

    @ConfigEntry
    public int chatMessagesDelay = 0;

    @ConfigEntry
    public int delayBetweenMessages = 1000;

    @ConfigEntry
    public boolean respectPaperMultiworlds =  false;

    public static Screen getModConfigScreenFactory(Screen parentScreen) {
        return YetAnotherConfigLib.create(LoginChatConfig.HANDLER, (defaults, config, builder) -> builder
                        .title(Text.of("Login Chat Config"))
                        .category(ConfigCategory.createBuilder()
                                .name(Text.of("Login Chat"))
                                .group(ListOption.<String>createBuilder()
                                        .name(Text.translatable("loginchat.config.serverslist"))
                                        .description(OptionDescription.of(Text.translatable("loginchat.config.serverslist.desc")))
                                        .controller(StringControllerBuilder::create)
                                        .binding(
                                                defaults.serversList,
                                                () -> config.serversList,
                                                (value) -> config.serversList = value
                                        )
                                        .initial("")
                                        .build())
                                .group(ListOption.<String>createBuilder()
                                        .name(Text.translatable("loginchat.config.messageslist"))
                                        .description(OptionDescription.of(Text.translatable("loginchat.config.messageslist.desc")))
                                        .controller(StringControllerBuilder::create)
                                        .binding(
                                                defaults.commandsList,
                                                () -> config.commandsList,
                                                (value) -> config.commandsList = value
                                        )
                                        .initial("")
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.translatable("loginchat.config.singleplayer"))
                                        .description(OptionDescription.of(Text.translatable("loginchat.config.singleplayer.desc")))
                                        .controller(TickBoxControllerBuilder::create)
                                        .binding(defaults.isEnabledInSingleplayer,
                                                () -> config.isEnabledInSingleplayer,
                                                (value) -> config.isEnabledInSingleplayer = value)
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(Text.translatable("loginchat.config.delay"))
                                        .description(OptionDescription.of(Text.translatable("loginchat.config.delay" +
                                                ".desc")))
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(0, 10000)
                                                .step(100)
                                        )
                                        .binding(Binding.generic(defaults.chatMessagesDelay,
                                                () -> config.chatMessagesDelay,
                                                (value) -> config.chatMessagesDelay = value))
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(Text.translatable("loginchat.config.delaybetween"))
                                        .description(OptionDescription.of(Text.translatable("loginchat.config.delaybetween" +
                                                ".desc")))
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(0, 4000)
                                                .step(50)
                                        )
                                        .binding(Binding.generic(defaults.delayBetweenMessages,
                                                () -> config.delayBetweenMessages,
                                                (value) -> config.delayBetweenMessages = value))
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.translatable("loginchat.config.messageslist.mode"))
                                        .description(OptionDescription.of(Text.translatable("loginchat.config.messageslist.mode.desc")))
                                        .controller(booleanOption -> BooleanControllerBuilder.create(booleanOption)
                                                .valueFormatter(value -> value ? Text.literal("PER_SERVER") : Text.literal("SHARED"))
                                                .coloured(true))
                                        .binding(Binding.generic(defaults.isListPerServer,
                                                () -> config.isListPerServer,
                                                (val) -> config.isListPerServer = val))
                                        .listener((booleanOption, aBoolean) -> {
                                            if (config.isListPerServer && !MOD_CONFIG_FOLDER.exists()) {
                                                MOD_CONFIG_FOLDER.mkdirs();
                                                LoginChatClient.LOGGER.info("Creating the Login Chat config folder...");
                                            };
                                        })
                                        .build())
                                .option(ButtonOption.createBuilder()
                                        .name(Text.translatable("loginchat.config.openfolder"))
                                        .description(OptionDescription.of(Text.translatable("loginchat.config.openfolder.desc")))
                                        .action((yaclScreen, buttonOption) -> Util.getOperatingSystem().open(MOD_CONFIG_FOLDER))
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.translatable("loginchat.config.respectpapermultiworlds"))
                                        .description(OptionDescription.of(Text.translatable("loginchat.config.respectpapermultiworlds.desc")))
                                        .controller(TickBoxControllerBuilder::create)
                                        .binding(Binding.generic(defaults.respectPaperMultiworlds,
                                                () -> config.respectPaperMultiworlds,
                                                (val) -> config.respectPaperMultiworlds = val))
                                        .build())
                                .build())
                )
                .generateScreen(parentScreen);

    }
}
