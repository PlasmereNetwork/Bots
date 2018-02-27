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

package co.templex.bots.lib.discord;

import co.templex.bots.lib.reader.LineListener;
import co.templex.bots.lib.reader.LogReader;
import com.google.common.util.concurrent.FutureCallback;
import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.Javacord;
import de.btobastian.javacord.listener.Listener;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public final class Bot {

    private static final Logger logger = LoggerFactory.getLogger(Bot.class);

    private final Properties properties;
    private final DiscordAPI discordAPI;
    private final LogReader reader;

    public Bot(@NonNull Properties properties, @NonNull CountDownLatch shutdownLatch) {
        this.properties = properties;
        this.discordAPI = Javacord.getApi(Objects.requireNonNull(properties.getProperty("token")), true);
        this.reader = new LogReader(shutdownLatch);
    }

    public String getProperty(@NonNull String key, String defaultValue) {
        logger.info("Attempted to fetch property " + key);
        String value = properties.getProperty(key, defaultValue);
        logger.info("Found value " + value + " for property " + key);
        return value;
    }

    public void start(@NonNull List<ListenerFactory> registerOnConnect) {
        logger.info("Attempting connection.");
        discordAPI.connect(new FutureCallback<DiscordAPI>() {
            @Override
            public void onSuccess(DiscordAPI result) {
                Runtime.getRuntime().addShutdownHook(new Thread(Bot.this::shutdown));
                for (ListenerFactory factory : registerOnConnect) {
                    registerListener(factory);
                }
                reader.start();
                discordAPI.setGame("with the fates of users.");
                logger.info("Successfully connected.");
            }

            @Override
            public void onFailure(Throwable t) {
                logger.error("Unable to initialize Discord API.", t);
            }
        });
    }

    public void shutdown() {
        logger.info("Shutting down...");
        discordAPI.disconnect();
        reader.stop();
        logger.info("Successfully shut down.");
    }

    private void registerListener(ListenerFactory factory) {
        Listener listener = factory.generateListener(this, new ChannelFactory(discordAPI));
        if (listener != null) {
            if (listener instanceof LineListener) {
                reader.register(listener);
            } else {
                discordAPI.registerListener(listener);
            }
            logger.info("Registered a " + listener.getClass().getSimpleName());
        }
    }

}
