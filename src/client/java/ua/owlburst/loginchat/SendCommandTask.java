package ua.owlburst.loginchat;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.minecraft.client.MinecraftClient;

import static java.lang.Thread.sleep;

class SendCommandTask implements Runnable {
    MinecraftClient client;
    String input;

    public SendCommandTask(MinecraftClient client, String input) {
        this.client = client;
        this.input = input;
    }

    public void run() {
        int chatMessagesDelay = LoginChatConfig.HANDLER.instance().chatMessagesDelay;
        if (chatMessagesDelay > 0 && LoginChatClient.delayedMessagesCount <= 0) {
            LoginChatClient.LOGGER.info("Delaying the chat messages by {} " +
                    "milliseconds", chatMessagesDelay);
            try {
                sleep(chatMessagesDelay);
                LoginChatClient.delayedMessagesCount++;
            } catch (InterruptedException ignored) {
            }
        }
        if (input.startsWith("/")) {
            input = input.substring(1);
            LoginChatClient.LOGGER.info("Command to execute: {}", this.input);
            for (int i = 0; i < 5; i++) {
                if (ClientCommandManager.getActiveDispatcher() != null && client.player != null) {
                    client.player.networkHandler.sendChatCommand(input);
                    break;
                } else {
                    LoginChatClient.LOGGER.error("Unable to execute the command: {}...", input);
                    try {
                        sleep(1000);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        } else {
            if (client.player != null) {
                LoginChatClient.LOGGER.info("Sending the chat message: {}", this.input);
                client.player.networkHandler.sendChatMessage(input);
            } else {
                LoginChatClient.LOGGER.warn("Can't send the chat message, can't get the player data");
            }
        }
    }
}