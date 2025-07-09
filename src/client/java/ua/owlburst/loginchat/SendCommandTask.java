package ua.owlburst.loginchat;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.minecraft.client.MinecraftClient;
import ua.owlburst.loginchat.config.LoginChatConfig;

import static java.lang.Thread.sleep;

class SendCommandTask implements Runnable {
    MinecraftClient client;
    String input;

    public SendCommandTask(MinecraftClient client, String input) {
        this.client = client;
        this.input = input;
    }

    public void run() {
        int messageStartDelay = LoginChatConfig.HANDLER.getConfig().chatMessagesDelay;
        int delayBetweenMessages = LoginChatConfig.HANDLER.getConfig().delayBetweenMessages;
        if (messageStartDelay > 0 && LoginChatClient.delayedMessagesCount <= 0) {
            LoginChatClient.LOGGER.info("Delaying the chat messages by {} " +
                    "milliseconds", messageStartDelay);
            try {
                sleep(messageStartDelay);
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
                    delayMessage(delayBetweenMessages);
                    break;
                } else {
                    LoginChatClient.LOGGER.error("Unable to execute the command: {}...", input);
                    delayMessage(delayBetweenMessages);
                }
            }
        } else {
            if (client.player != null) {
                LoginChatClient.LOGGER.info("Sending the chat message: {}", this.input);
                client.player.networkHandler.sendChatMessage(input);
                delayMessage(delayBetweenMessages);
            } else {
                LoginChatClient.LOGGER.warn("Can't send the chat message, can't get the player data");
            }
        }
    }

    private void delayMessage(int chatMessagesDelay) {
        try {
            LoginChatClient.LOGGER.info("Taking a break for {}ms...", chatMessagesDelay);
            sleep(chatMessagesDelay);
        } catch (InterruptedException ignored) {
        }
    }
}
