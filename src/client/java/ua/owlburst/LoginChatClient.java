package ua.owlburst;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.text.Text;

public class LoginChatClient implements ClientModInitializer {
	private static void onPlayReady(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client) {
		if (client.player != null) {
//			client.player.sendCommand("say Hello, BronyTales!");
			client.player.sendChatMessage("Hello everypony!", null);
			if (client.getCurrentServerEntry() != null) {
				LoginChat.LOGGER.info(String.valueOf(client.isInSingleplayer()));
				LoginChat.LOGGER.info(client.getCurrentServerEntry().address);
			}
		}
	}
	private static void onPlayDisconnect(ClientPlayNetworkHandler handler, MinecraftClient client) {
		if (client.player != null) {
			client.player.sendCommand("/mcability");
			client.player.sendChatMessage("Goodbye everypony!", null);
			if (client.getCurrentServerEntry() != null) {
				LoginChat.LOGGER.info(String.valueOf(client.isInSingleplayer()));
				LoginChat.LOGGER.info(client.getCurrentServerEntry().address);
			}
		}
	}

	@Override
	public void onInitializeClient() {
		ClientPlayConnectionEvents.JOIN.register((LoginChatClient::onPlayReady));
		ClientPlayConnectionEvents.DISCONNECT.register(LoginChatClient::onPlayDisconnect);
	}
}