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

public class BanListener extends ChannelWriter implements LineListener {

    private static final Logger logger = LoggerFactory.getLogger(BanListener.class);

    private BanListener(Channel reportChannel) {
        super(reportChannel);
    }

    @Override
    public void onLine(String line) {
        if (line.length() > 33) { // ex. "[03:05:13] [Server thread/INFO]: "
            line = line.substring(33);
            String[] splitLine = line.split(": ");
            if (line.startsWith("Banned")) {
                StringBuilder reason = new StringBuilder(splitLine[1]);
                for (int i = 2; i < splitLine.length; i++) {
                    reason.append(": ");
                    reason.append(splitLine[i]);
                }
                reportBan(splitLine[0].substring(7), "Server", reason.toString());
            } else if (line.matches("\\[.*: Banned .*:.*]")) {
                StringBuilder reason = new StringBuilder(splitLine[2]);
                for (int i = 3; i < splitLine.length; i++) {
                    reason.append(": ");
                    reason.append(splitLine[i]);
                }
                reportBan(splitLine[1].substring(7), splitLine[0].substring(1), reason.substring(0, reason.length() - 1));
            }
        }
    }

    /**
     * Reports a ban to the target server.
     *
     * @param banned The banned user.
     * @param banner The banning user.
     * @param reason The reason for the ban.
     */
    private void reportBan(String banned, String banner, String reason) {
        getReportChannel().sendMessage("", generateEmbedBuilder(
                "Ban Report",
                String.format(
                        "User %s was banned on %s with reason \"%s\".",
                        banned,
                        ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT),
                        reason
                ),
                String.format(
                        "Ban issued by %s",
                        banner
                ),
                null,
                null,
                Color.RED
        ));
        logger.info(String.format("Reported ban of user %s", banned));
    }

    @Value
    public static class Factory implements ListenerFactory {
        @NonNull
        String channelID;

        @Override
        public Listener generateListener(Bot bot, ChannelFactory factory) {
            return new BanListener(factory.generateChannel(channelID));
        }
    }

}
