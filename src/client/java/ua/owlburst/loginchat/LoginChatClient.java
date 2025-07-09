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
import ua.owlburst.loginchat.config.LoginChatConfigManager;
import ua.owlburst.loginchat.config.LoginChatConfig;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginChatClient implements ClientModInitializer {
	public static int delayedMessagesCount = 0;
	public static final String MOD_ID = "loginchat";
	public static final Logger LOGGER = LoggerFactory.getLogger("loginchat");
	private static boolean isLoggedIn = false;

	private static void send(MinecraftClient client, @NotNull ArrayList<String> commandsList) {
		ExecutorService commandsExecutor = Executors.newSingleThreadExecutor(r -> new Thread(r, "Login Chat"));
		commandsList.forEach(el -> commandsExecutor.submit(new SendCommandTask(client, el)));
	}

	@Override
	public void onInitializeClient() {
		LoginChatConfigManager.init();
		ClientPlayConnectionEvents.JOIN.register((LoginChatClient::onPlayReady));
		ClientPlayConnectionEvents.DISCONNECT.register(LoginChatClient::onDisconnect);
	}

	private static void onPlayReady(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client) {
		if (LoginChatConfig.HANDLER.getConfig().respectPaperMultiworlds && isLoggedIn) {return;} // the messages have already been sent
		isLoggedIn = true;
		LoginChatClient.delayedMessagesCount = 0;
		ArrayList<String> serversList = new ArrayList<>(LoginChatConfig.HANDLER.getConfig().serversList);
		ArrayList<String> commandsList = new ArrayList<>(LoginChatConfig.HANDLER.getConfig().commandsList);
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
				if (LoginChatConfig.HANDLER.getConfig().isListPerServer)
					// load messages from the configuration file corresponding to server
					send(client, LoginChatConfigManager.load(ip));
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
					if (LoginChatConfig.HANDLER.getConfig().isListPerServer) {
						client.player.sendMessage(Text.literal("[Login Chat] ").append(Text.translatable("loginchat.chat.listPerServerEnabled")).append(Text.of(" ")), false);
					}
				}
				LOGGER.info("Connecting to the server: {}", ip);
			}
		} else {
			if (LoginChatConfig.HANDLER.getConfig().isEnabledInSingleplayer) {
				LOGGER.info("Joining the singleplayer world");
				if (LoginChatConfig.HANDLER.getConfig().isListPerServer)
					// if per server, send from configuration file for localhost
					send(client, LoginChatConfigManager.load("localhost"));
				else send(client, commandsList);
			}
		}
	}

	private static void onDisconnect(ClientPlayNetworkHandler handler, MinecraftClient client) {
		isLoggedIn = false;
	}
}