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

package co.templex.bots.op;

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

public class DeopListener extends ChannelWriter implements LineListener {

    private static final Logger logger = LoggerFactory.getLogger(DeopListener.class);

    private DeopListener(Channel reportChannel) {
        super(reportChannel);
    }

    @Override
    public void onLine(String line) {
        if (line.length() > 33) { // ex. "[03:05:13] [Server thread/INFO]: "
            line = line.substring(33);
            String[] splitLine = line.split(": ");
            if (line.matches("Made .+ no longer a server operator")) {
                reportDeop(splitLine[0].substring(5, splitLine[0].length() - 28), "Server");
            } else if (line.matches("\\[.*: Made .+ no longer a server operator]")) {
                reportDeop(splitLine[1].substring(5, splitLine[1].length() - 29), splitLine[0].substring(1));
            }
        }
    }

    /**
     * Reports an deop call to the target server.
     *
     * @param deopped The deopped user.
     * @param deopper The deopping user.
     */
    private void reportDeop(String deopped, String deopper) {
        getReportChannel().sendMessage("", generateEmbedBuilder(
                "Deop Report",
                String.format(
                        "User %s was deopped on %s.",
                        deopped,
                        ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT)
                ),
                String.format(
                        "Deop issued by %s",
                        deopper
                ),
                null,
                null,
                Color.ORANGE
        ));
        logger.info(String.format("Reported deop of user %s", deopped));
    }

    @Value
    public static class Factory implements ListenerFactory {
        @NonNull
        String channelID;

        @Override
        public Listener generateListener(Bot bot, ChannelFactory factory) {
            return new DeopListener(factory.generateChannel(channelID));
        }
    }

}
