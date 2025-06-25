package ua.owlburst.loginchat.config;

import net.minecraft.text.Text;
import ua.owlburst.loginchat.LoginChatClient;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

public class ConfigManager {
    public static void init() {
        LoginChatConfig.HANDLER.load();
        LoginChatConfig.MOD_CONFIG_FOLDER.mkdirs();
        if (LoginChatConfig.MOD_CONFIG_FOLDER.isDirectory() && Objects.requireNonNull(LoginChatConfig.MOD_CONFIG_FOLDER.listFiles()).length <= 0) {
            File file = new File(LoginChatConfig.MOD_CONFIG_FOLDER, "mc.example.com.txt");
            try {
                if (file.createNewFile()) {
                    try (FileWriter fw = new FileWriter(file)){
                        fw.append(Text.literal(
                                """
                                # This is an example of a messages list definition file
                                # The mod will read the messages from this file and send them to the server
                                # if the Messages List Mode is set to PER_SERVER (isListPerServer config option is true)
                                # when trying to connect to mc.example.com, as denoted by the filename.
                                # Below is an example definition of a chat message and a command
                                
                                # To set up the per server config, create a text file in this directory
                                # with a server IP as it's filename and a txt extension
                                # Then put the messages you would like to send to that server inside of it (one message per line)
                                # Then add the server IP to the Servers List. If the server isn't added to the List,
                                # the mod won't send anything even if the corresponding file is present in this directory
                                
                                # The format also supports comments. All the lines that begin with a hash, like this one, will be ignored.
                                # Note that only the full-line comments are supported, if you'll put a hash in the middle of the line,
                                # it won't comment out the rest of the line after itself.
                                
                                # The singleplayer world is represented by `localhost.txt`, to choose the lines that'll be sent in singleplayer,
                                # create the file `localhost.txt` in this directory, put the messages there and tick the 'Enabled in singleplayer'
                                # box in the mod configuration.
                                
                                Hello! I'm an example message that will be sent when connecting to mc.example.com, as per my file name.
                                /say And I am a command that will be executed upon joining the said server after the message above will be sent.
                                # Just like the shared Messages List in the GUI and JSON config, just customised per server!
                                """
                        ).getLiteralString());
                    }
                }
            } catch (IOException e) {
                LoginChatClient.LOGGER.error("Exception thrown when trying to create the mod config folder", e);
            }
        }
    }
}
