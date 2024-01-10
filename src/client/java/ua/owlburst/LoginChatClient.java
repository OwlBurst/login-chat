package ua.owlburst;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginChatClient implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("loginchat");
	private static void onPlayReady(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client) {
		if (client.player != null) {
            String input = "/mcability";
            try {
                handler.getCommandDispatcher().execute(input, handler.getCommandSource());
            } catch (CommandSyntaxException e) {
                LOGGER.error(String.format("The command you specified in the config file has incorrect syntax: %s", input));
				client.player.sendMessage(Text.of(String.format("The command you specified in the config file has incorrect syntax: %s", input)).copy().setStyle(Style.EMPTY.withColor(Formatting.RED)));
            }
            client.player.sendChatMessage("Hello everypony!", null);
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