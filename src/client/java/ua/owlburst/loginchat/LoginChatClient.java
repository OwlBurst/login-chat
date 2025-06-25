package ua.owlburst.loginchat;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.owlburst.loginchat.config.ConfigManager;
import ua.owlburst.loginchat.config.LoginChatConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginChatClient implements ClientModInitializer {
	public static int delayedMessagesCount = 0;
	public static final String MOD_ID = "loginchat";
	public static final Logger LOGGER = LoggerFactory.getLogger("loginchat");

	private static void onPlayReady(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client) {
		LoginChatClient.delayedMessagesCount = 0;
		ArrayList<String> serversList = new ArrayList<>(LoginChatConfig.HANDLER.instance().serversList);
		ArrayList<String> commandsList = new ArrayList<>(LoginChatConfig.HANDLER.instance().commandsList);
		LOGGER.info("Server in the list: {}", serversList.toArray());
		boolean isSinglePlayer;
		try {
			isSinglePlayer = client.getServer().isSingleplayer();
		} catch (NullPointerException e) {
			isSinglePlayer = false;
		}
        LOGGER.info("Is singleplayer? - {}", isSinglePlayer);
		if(!isSinglePlayer) {
			String ip = handler.getConnection().getAddress().toString();
			ip = ip.split("/")[0].replaceAll("\\.$", "");
			if (serversList.contains(ip)) {
				if (LoginChatConfig.HANDLER.instance().isListPerServer) send(client, ip);
                else send(client, commandsList);
			} else {
				if (client.player != null) {
					client.player
							.sendMessage(Text.literal("[Login Chat] ").append(Text.translatable("loginchat.chat.ip")).append(Text.of(" "))
							.append(Text.literal(ip)
									.setStyle(Style.EMPTY
											.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, ip))
											.withFormatting(Formatting.YELLOW)
											.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("loginchat.chat.clipboard")))
									)
							)
							, false);
					if (LoginChatConfig.HANDLER.instance().isListPerServer) {
						client.player.sendMessage(Text.literal("[Login Chat] ").append(Text.translatable("loginchat.chat.listPerServerEnabled")).append(Text.of(" ")), false);
					}
				}
				LOGGER.info("Connecting to the server: {}", ip);
			}
		} else {
			if (LoginChatConfig.HANDLER.instance().isEnabledInSingleplayer) {
				LOGGER.info("Joining the singleplayer world");
				if (LoginChatConfig.HANDLER.instance().isListPerServer) send(client, "localhost");
				else send(client, commandsList);
			}
		}
	}

	private static void send(MinecraftClient client, @NotNull ArrayList<String> commandsList) {
		ExecutorService commandsExecutor = Executors.newSingleThreadExecutor(r -> new Thread(r, "Login Chat"));
		commandsList.forEach(el -> commandsExecutor.submit(new SendCommandTask(client, el)));
	}

	private static void send(MinecraftClient client, @NotNull String ip) {
		ArrayList<String> commandsList = new ArrayList<>();
		File file = new File(LoginChatConfig.MOD_CONFIG_FOLDER, ip + ".txt");
		if (file.exists()) {
			try (Scanner sc = new Scanner(file)) {
				while (sc.hasNextLine()) {
					String line = sc.nextLine();
					if (line.startsWith("#") || line.trim().isBlank()) continue;
					LOGGER.info("Message: {}", line);
					commandsList.add(line);
				}
			} catch (FileNotFoundException ignored) {

			}
		}
		send(client, commandsList);
	}

	@Override
	public void onInitializeClient() {
		ConfigManager.init();
		ClientPlayConnectionEvents.JOIN.register((LoginChatClient::onPlayReady));

	}
}