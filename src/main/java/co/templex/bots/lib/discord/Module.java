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

import co.templex.bots.lib.minecraft.ScreenWriter;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.List;

public abstract class Module {

    @Getter
    private final String name;
    @Getter(AccessLevel.PROTECTED)
    private Bot bot;
    @Getter(AccessLevel.PROTECTED)
    private ScreenWriter writer;

    public Module(String name) {
        this.name = name;
    }

    public void initialize(Bot bot, ScreenWriter writer) {
        this.bot = bot;
        this.writer = writer;
    }

    public abstract List<ListenerFactory> setup();


}
