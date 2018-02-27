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

public class OpListener extends ChannelWriter implements LineListener {

    private static final Logger logger = LoggerFactory.getLogger(OpListener.class);

    private OpListener(Channel reportChannel) {
        super(reportChannel);
    }

    @Override
    public void onLine(String line) {
        if (line.length() > 33) { // ex. "[03:05:13] [Server thread/INFO]: "
            line = line.substring(33);
            String[] splitLine = line.split(": ");
            if (line.matches("Made .+ a server operator")) {
                logger.debug("Detected /op from console.");
                reportOp(splitLine[0].substring(5, splitLine[0].length() - 18), "Server");
            } else if (line.matches("\\[.*: Made .+ a server operator]")) {
                logger.debug("Detected /op from server chat.");
                reportOp(splitLine[1].substring(5, splitLine[1].length() - 19), splitLine[0].substring(1));
            }
        }
    }

    /**
     * Reports an op call to the target server.
     *
     * @param opped The opped user.
     * @param opper The opping user.
     */
    private void reportOp(String opped, String opper) {
        getReportChannel().sendMessage("", generateEmbedBuilder(
                "Op Report",
                String.format(
                        "User %s was opped on %s.",
                        opped,
                        ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT)
                ),
                String.format(
                        "Op issued by %s",
                        opper
                ),
                null,
                null,
                Color.GREEN
        ));
        logger.info(String.format("Reported op of user %s", opped));
    }

    @Value
    public static class Factory implements ListenerFactory {
        @NonNull
        String channelID;

        @Override
        public Listener generateListener(Bot bot, ChannelFactory factory) {
            return new OpListener(factory.generateChannel(channelID));
        }
    }

}
