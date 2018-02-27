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
import co.templex.bots.lib.discord.Command;
import co.templex.bots.lib.discord.ListenerFactory;
import co.templex.bots.lib.minecraft.ScreenWriter;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.listener.Listener;
import lombok.NonNull;
import lombok.Value;

import java.util.List;

public class BanlistModificationCommand extends Command {

    private final ScreenWriter writer;

    private BanlistModificationCommand(List<String> channelIDs, ScreenWriter writer) {
        super(channelIDs, "\\.(ban|pardon) .+");
        this.writer = writer;
    }

    @Override
    public void onMatch(Message message) {
        message.delete();
        writer.println(message.getContent().substring(1));
    }

    @Value
    public static class Factory implements ListenerFactory {
        @NonNull
        List<String> channelIDs;
        @NonNull
        ScreenWriter writer;

        @Override
        public Listener generateListener(Bot bot, ChannelFactory factory) {
            return new BanlistModificationCommand(channelIDs, writer);
        }
    }
}
