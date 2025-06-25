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

import java.text.MessageFormat;
import java.util.ArrayList;
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
		LOGGER.info(MessageFormat.format("Server in the list: {0}", serversList.toArray()));
		boolean isSinglePlayer;
		try {
			isSinglePlayer = client.getServer().isSingleplayer();
		} catch (NullPointerException e) {
			isSinglePlayer = false;
		}
		LOGGER.info("Is singleplayer? - " + isSinglePlayer);
		if(!isSinglePlayer) {
			String ip = handler.getConnection().getAddress().toString();
			ip = ip.split("/")[0].replaceAll("\\.$", "");
			if (serversList.contains(ip)) {
				send(client, commandsList);
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
							);
				}
				LOGGER.info("Connecting to the server: {}", ip);
			}
		} else {
			if (LoginChatConfig.HANDLER.instance().isEnabledInSingleplayer) {
				LOGGER.info("Joining the singleplayer world");
				send(client, commandsList);
			}
		}
	}

	private static void send(MinecraftClient client, @NotNull ArrayList<String> commandsList) {
		ExecutorService commandsExecutor = Executors.newSingleThreadExecutor(r -> new Thread(r, "Login Chat"));
		commandsList.forEach(el -> commandsExecutor.submit(new SendCommandTask(client, el)));
	}

	@Override
	public void onInitializeClient() {
		LoginChatConfig.HANDLER.load();
		ClientPlayConnectionEvents.JOIN.register((LoginChatClient::onPlayReady));

	}
}
