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

package co.templex.bots.ban;

import co.templex.bots.lib.discord.Bot;
import co.templex.bots.lib.discord.ChannelFactory;
import co.templex.bots.lib.discord.ChannelWriter;
import co.templex.bots.lib.discord.ListenerFactory;
import co.templex.bots.lib.reader.LineListener;
import de.btobastian.javacord.entities.Channel;
import de.btobastian.javacord.listener.Listener;
import lombok.NonNull;
import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static co.templex.bots.lib.discord.Util.generateEmbedBuilder;

public class PardonListener extends ChannelWriter implements LineListener {

    private static final Logger logger = LoggerFactory.getLogger(PardonListener.class);

    private PardonListener(Channel reportChannel) {
        super(reportChannel);
    }

    @Override
    public void onLine(String line) {
        if (line.length() > 33) { // ex. "[03:05:13] [Server thread/INFO]: "
            line = line.substring(33);
            String[] splitLine = line.split(": ");
            if (line.startsWith("Unbanned")) {
                reportPardon(splitLine[0].substring(9), "Server");
            } else if (line.matches("\\[.*: Unbanned .*]")) {
                reportPardon(splitLine[1].substring(9, splitLine[1].length() - 1), splitLine[0].substring(1));
            }
        }
    }

    /**
     * Reports a pardon to the target server.
     *
     * @param pardoned The pardoned user.
     * @param pardoner The pardoning user.
     */
    private void reportPardon(String pardoned, String pardoner) {
        getReportChannel().sendMessage("", generateEmbedBuilder(
                "Pardon Report",
                String.format(
                        "User %s was pardoned on %s.",
                        pardoned,
                        ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT)
                ),
                String.format(
                        "Pardon issued by %s",
                        pardoner
                ),
                null,
                null,
                Color.YELLOW
        ));
        logger.info(String.format("Reported pardon of user %s", pardoned));
    }

    @Value
    public static class Factory implements ListenerFactory {
        @NonNull
        String channelID;

        @Override
        public Listener generateListener(Bot bot, ChannelFactory factory) {
            return new PardonListener(factory.generateChannel(channelID));
        }
    }

}
