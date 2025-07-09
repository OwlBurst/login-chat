package ua.owlburst.loginchat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import ua.owlburst.loginchat.config.LoginChatConfig;

public class ModMenuImpl implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return LoginChatConfig::getModConfigScreenFactory;
    }
}
