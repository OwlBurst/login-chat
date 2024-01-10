package ua.owlburst.loginchat;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginChatClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("loginchat");
    private static void onPlayReady(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client) {
        if (client.player != null) {
            String input = "say Hello";
            client.player.sendChatMessage("Hello everypony!", null);
            client.player.sendCommand(input, null);
            if (client.getCurrentServerEntry() != null) {
                LOGGER.info(String.valueOf(client.isInSingleplayer()));
                LOGGER.info(client.getCurrentServerEntry().address);
            }
        }
    }

    @Override
    public void onInitializeClient() {
        ClientPlayConnectionEvents.JOIN.register((LoginChatClient::onPlayReady));
    }
}