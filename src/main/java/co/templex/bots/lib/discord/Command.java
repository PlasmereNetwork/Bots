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

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.listener.message.MessageCreateListener;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.intellij.lang.annotations.Language;

import java.util.Collections;
import java.util.List;

@Value
@NonFinal
public abstract class Command implements MessageCreateListener {

    List<String> channelIDs;
    String regex;

    protected Command(List<String> channelIDs, @Language("RegExp") String regex) {
        this.channelIDs = Collections.unmodifiableList(channelIDs);
        this.regex = regex;
    }

    @Override
    public void onMessageCreate(DiscordAPI discordAPI, Message message) {
        if (channelIDs.contains(message.getChannelReceiver().getId()) && message.getContent().matches(regex)) {
            onMatch(message);
        }
    }

    protected abstract void onMatch(Message message);

}
