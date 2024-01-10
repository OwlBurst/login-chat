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
            LoginChatThread thread = new LoginChatThread(client, input);
            thread.start();

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

class LoginChatThread extends Thread {
    MinecraftClient client;
    String input;
    public LoginChatThread(MinecraftClient client, String input) {
        this.client = client;
        this.input = input;
    }
    public void run() {
        while (true) {
            if (net.fabricmc.fabric.impl.command.client.ClientCommandInternals.getActiveDispatcher() != null && client.player != null) {
                client.player.sendCommand(input, null);
                break;
            } else {
                try {
                    LoginChatClient.LOGGER.info("Delaying the command...");
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {}
            }
        }
    }
}