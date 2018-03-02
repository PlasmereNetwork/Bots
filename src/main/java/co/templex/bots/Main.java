/*
 * Bots: A Discord bot that manages the Templex server.
 * Copyright (C) 2018  vtcakavsmoace
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package co.templex.bots;

import co.templex.bots.ban.BanlistModule;
import co.templex.bots.chat.ChatModule;
import co.templex.bots.initialize.InitializationModule;
import co.templex.bots.lib.discord.Bot;
import co.templex.bots.lib.discord.ListenerFactory;
import co.templex.bots.lib.discord.Module;
import co.templex.bots.lib.minecraft.ScreenWriter;
import co.templex.bots.living.LivingModule;
import co.templex.bots.op.OplistModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

/**
 * Main class for this library. This may optionally be avoided if custom uses for the HTTP Server/Discord bot are
 * necessary, but this library will likely be used solely as an application.
 */
@SuppressWarnings("WeakerAccess")
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static final List<Module> availableModules = Arrays.asList(new BanlistModule(), new ChatModule(), new OplistModule(), new InitializationModule(), new LivingModule());

    private static final List<String> enabledModules = new ArrayList<>();

    /**
     * Hidden constructor. Instantiation of this class is not permitted.
     */
    private Main() {
        throw new UnsupportedOperationException("Instantiation not permitted.");
    }

    /**
     * Main method for this application. This reads both of the properties files (should they exist) and passes the
     * appropriate properties instances to the Bot and HTTP Server instantiated within this method.
     * <p>
     * Note that this will await the shutdown of both the bot and the http server before shutting down the JVM.
     *
     * @param args The command line arguments. These will be ignored.
     * @throws IOException          If the properties files exist but are unreadable.
     * @throws InterruptedException If the latch is interrupted at any point.
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        Properties botProperties = new Properties();
        try (FileInputStream bot = new FileInputStream("bot.properties")) {
            botProperties.load(bot);
        }
        CountDownLatch shutdownLatch = new CountDownLatch(1);
        Bot bot = new Bot(botProperties, shutdownLatch);
        ScreenWriter writer = new ScreenWriter();
        List<ListenerFactory> listeners = new ArrayList<>();
        for (Module module : availableModules) {
            if (Boolean.parseBoolean(bot.getProperty(module.getName() + "-enabled", "false"))) {
                module.initialize(bot, writer);
                try {
                    listeners.addAll(module.setup());
                    enabledModules.add(module.getName());
                } catch (Exception e) {
                    logger.warn("Unable to setup module " + module.getName(), e);
                }
            }
        }
        bot.start(listeners);
        shutdownLatch.await();
    }

    public static String getEnabledModules() {
        if (enabledModules.size() == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder(enabledModules.get(0));
        for (int i = 1; i < enabledModules.size(); i++) {
            builder.append(',').append(enabledModules.get(i));
        }
        return builder.toString();
    }
}
