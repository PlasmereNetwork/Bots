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

package co.templex.bots.initialize;

import co.templex.bots.Main;
import co.templex.bots.lib.discord.Bot;
import co.templex.bots.lib.discord.ChannelFactory;
import co.templex.bots.lib.discord.ListenerFactory;
import de.btobastian.javacord.entities.Channel;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.listener.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.lang.management.ManagementFactory;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static co.templex.bots.lib.discord.Util.generateEmbedBuilder;

public class InitializationListener implements Listener {

    private static final Logger logger = LoggerFactory.getLogger(InitializationListener.class);

    private final ExecutorService timeMeasurer = Executors.newSingleThreadExecutor();
    private final Bot bot;
    private final Channel channel;

    private InitializationListener(Bot bot, Channel channel) {
        this.bot = bot;
        this.channel = channel;
        timeMeasurer.submit(this::initiateTimer);
    }

    private void initiateTimer() {
        try {
            Random random;
            try {
                random = SecureRandom.getInstance("SHA1PRNG");
            } catch (NoSuchAlgorithmException e) {
                logger.warn("Failed to initialize secure random instance.", e);
                random = new Random();
            }
            char[] chars = new char[random.nextInt(100) + 50];
            Arrays.fill(chars, '1');
            String messageContent = new String(chars);
            long moment = System.nanoTime();
            Message message = channel.sendMessage(messageContent).get();
            moment -= System.nanoTime();
            message.delete();
            message.getChannelReceiver().sendMessage("", generateEmbedBuilder(
                    "Templex Discord Bot",
                    "Initialization time: " + ManagementFactory.getRuntimeMXBean().getUptime() + " ms\n" +
                            "Latency: " + (-moment / 2000000) + " ms\n" +
                            "Enabled Modules: " + Main.getEnabledModules() + "\n" +
                            "Version: " + bot.getClass().getPackage().getImplementationVersion(),
                    null,
                    null,
                    null,
                    Color.GREEN
            ));
            timeMeasurer.shutdown();
        } catch (Throwable t) {
            logger.error("Couldn't report initialization.", t);
        }
    }

    public static class Factory implements ListenerFactory {
        @Override
        public Listener generateListener(Bot bot, ChannelFactory factory) {
            return new InitializationListener(bot, factory.generateChannel(bot.getProperty("init-channel", null)));
        }
    }

}
