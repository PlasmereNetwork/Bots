package co.templex.bots.initialize;

import co.templex.bots.lib.discord.ListenerFactory;
import co.templex.bots.lib.discord.Module;

import java.util.Collections;
import java.util.List;

public class InitializationModule extends Module {
    public InitializationModule() {
        super("init");
    }

    @Override
    public List<ListenerFactory> setup() {
        return Collections.singletonList(new InitializationListener.Factory());
    }
}
