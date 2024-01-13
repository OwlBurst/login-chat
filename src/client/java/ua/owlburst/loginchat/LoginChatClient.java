package ua.owlburst.loginchat;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginChatClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("loginchat");
    private static void onPlayReady(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client) {
        ArrayList<String> serversList = new ArrayList<>(LoginChatConfig.INSTANCE.getConfig().serversList);
        ArrayList<String> commandsList = new ArrayList<>(LoginChatConfig.INSTANCE.getConfig().commandsList);
        serversList.forEach(el -> LOGGER.info(MessageFormat.format("Server in the list: {0}", el)));
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
            LOGGER.info(MessageFormat.format("Joining the server: {0}", ip));
            if (serversList.contains(ip)) {
                ExecutorService executor = Executors.newSingleThreadExecutor();
                commandsList.forEach(el -> executor.submit(new SendCommandTask(client, el)));
            }
        } else {
            LOGGER.info("Joining the singleplayer world");
            if (LoginChatConfig.INSTANCE.getConfig().isEnabledInSingleplayer) {
                ExecutorService executor = Executors.newSingleThreadExecutor();
                commandsList.forEach(el -> executor.submit(new SendCommandTask(client, el)));
            }
        }
    }

    @Override
    public void onInitializeClient() {
        LoginChatConfig.INSTANCE.load();
        ClientPlayConnectionEvents.JOIN.register((LoginChatClient::onPlayReady));
    }
}

class SendCommandTask implements Runnable {
    MinecraftClient client;
    String input;
    public SendCommandTask(MinecraftClient client, String input) {
        this.client = client;
        this.input = input;
    }
    public void run() {

        if (input.startsWith("/")) {
            try {
                LoginChatClient.LOGGER.info(MessageFormat.format("Delaying the command execution: {0}...", input));
                Thread.sleep(3000);
            } catch (InterruptedException ignored) {
            }
            input = input.substring(1);
            LoginChatClient.LOGGER.info(MessageFormat.format("Command to execute: {0}", this.input));
            while (true) {
                //noinspection UnstableApiUsage
                if (net.fabricmc.fabric.impl.command.client.ClientCommandInternals.getActiveDispatcher() != null && client.player != null) {
                    client.player.sendCommand(input, null);
                    break;
                } else {
                    LoginChatClient.LOGGER.error(MessageFormat.format("Unable send the command: {0}...", input));
                }
            }
        } else {
            if(client.player != null) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {

                }
                client.player.sendChatMessage(input, null);
            } else {
                LoginChatClient.LOGGER.warn("Can't send the chat message, can't get the player data");
            }
        }
    }
}