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

package co.templex.bots.chat;

import co.templex.bots.lib.discord.Bot;
import co.templex.bots.lib.discord.ChannelFactory;
import co.templex.bots.lib.discord.ChannelWriter;
import co.templex.bots.lib.discord.ListenerFactory;
import co.templex.bots.lib.reader.LineListener;
import de.btobastian.javacord.entities.Channel;
import de.btobastian.javacord.listener.Listener;
import lombok.NonNull;
import lombok.Value;

import java.awt.*;

import static co.templex.bots.lib.discord.Util.generateEmbedBuilder;

public class ListListener extends ChannelWriter implements LineListener {
    private ListListener(Channel reportChannel) {
        super(reportChannel);
    }

    @Override
    public void onLine(String line) {
        if (line.length() > 33) { // ex. "[03:05:13] [Server thread/INFO]: "
            line = line.substring(33);
            if (line.matches("There are [0-9]+ of a max [0-9]+ players online: (\\w+, )*\\w*")) {
                String[] splitLine = line.split(": ");
                getReportChannel().sendMessage("", generateEmbedBuilder(
                        "Online Players",
                        splitLine[1].replaceAll(", ", "\n"),
                        splitLine[0],
                        null,
                        null,
                        Color.GREEN
                ));
            }
        }
    }

    @Value
    public static class Factory implements ListenerFactory {
        @NonNull
        String channelID;

        @Override
        public Listener generateListener(Bot bot, ChannelFactory factory) {
            return new ListListener(factory.generateChannel(channelID));
        }
    }
}
