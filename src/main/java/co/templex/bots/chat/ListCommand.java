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
import co.templex.bots.lib.discord.Command;
import co.templex.bots.lib.discord.ListenerFactory;
import co.templex.bots.lib.minecraft.ScreenWriter;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.listener.Listener;
import lombok.NonNull;
import lombok.Value;

import java.util.List;

public class ListCommand extends Command {
    private final ScreenWriter writer;

    private ListCommand(List<String> channelIDs, ScreenWriter writer) {
        super(channelIDs, "\\.list");
        this.writer = writer;
    }

    @Override
    public void onMatch(Message message) {
        message.delete();
        writer.println("list");
    }

    @Value
    public static class Factory implements ListenerFactory {
        @NonNull
        List<String> channelIDs;
        @NonNull
        ScreenWriter writer;

        @Override
        public Listener generateListener(Bot bot, ChannelFactory factory) {
            return new ListCommand(channelIDs, writer);
        }
    }
}
