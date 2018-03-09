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
import co.templex.oauth.OAuthResponse;
import co.templex.oauth.Querier;
import co.templex.oauth.SuccessfulOAuthResponse;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.listener.Listener;
import lombok.NonNull;
import lombok.Value;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class ChatCommand extends Command {

    private final ScreenWriter writer;
    private final Querier querier;
    private final RegisteredUserMap registeredUsers;
    private final File registeredUsersCache;
    private final Gson gson;
    private final AtomicReference<CountDownLatch> latch;

    private ChatCommand(List<String> channelIDs, ScreenWriter writer) {
        super(channelIDs, "([^.].+|\\.register [^ ]+)");
        this.writer = writer;
        this.querier = new Querier();
        this.registeredUsers = new RegisteredUserMap();
        this.registeredUsersCache = new File("registered-users.json");
        this.gson = new Gson();
        this.latch = new AtomicReference<>(new CountDownLatch(0));
        if (registeredUsersCache.exists()) {
            try (FileReader reader = new FileReader(registeredUsersCache)) {
                RegisteredUserMap map = gson.fromJson(reader, RegisteredUserMap.class);
                if (map != null) {
                    registeredUsers.putAll(map);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onMatch(Message message) {
        if (!message.getAuthor().isYourself()) {
            SuccessfulOAuthResponse user = registeredUsers.get(message.getAuthor().getId());
            if (user == null) {
                if (message.getContent().charAt(0) == '.') {
                    OAuthResponse response;
                    try {
                        response = querier.query(message.getContent().split(" ")[1]);
                    } catch (IOException e) {
                        message.reply(String.format("Error: Couldn't query mc-oauth with exception: %s", e.getMessage()));
                        return;
                    }
                    if (response instanceof SuccessfulOAuthResponse) {
                        registeredUsers.put(message.getAuthor().getId(), (SuccessfulOAuthResponse) response);
                        saveRegisteredUsers();
                    } else {
                        message.reply(String.format("Error: Registration failed with reason: %s", response.getMessage()));
                    }
                } else {
                    message.reply("You need to register yourself before using MC chat!\nLogin to srv.mc-oauth.net and use the token in the register command like so:\n\n.register <token>");
                }
            } else {
                writer.println(
                        String.format(
                                "tellraw @a [\"\",{\"text\":\"<\"},{\"text\":\"%s\",\"color\":\"dark_purple\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://discord.gg/4JBG3h2\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Said through the discord. Click to join!\"}]}}},{\"text\":\"> %s\",\"color\":\"none\"}]",
                                user.getUsername(),
                                message.getContent()
                        )
                );
            }
        }
    }

    private void saveRegisteredUsers() {
        CountDownLatch latch = null;
        try {
            this.latch.getAndSet(latch = new CountDownLatch(1)).await();
            Files.write(gson.toJson(registeredUsers), registeredUsersCache, Charsets.UTF_8);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (latch != null) {
                latch.countDown();
            }
        }
    }

    @Value
    public static class Factory implements ListenerFactory {
        @NonNull
        List<String> channelIDs;
        @NonNull
        ScreenWriter writer;

        @Override
        public Listener generateListener(Bot bot, ChannelFactory factory) {
            return new ChatCommand(channelIDs, writer);
        }
    }
}
