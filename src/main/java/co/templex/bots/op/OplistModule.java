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

import co.templex.bots.ban.BanListener;
import co.templex.bots.ban.PardonListener;
import co.templex.bots.lib.discord.ListenerFactory;
import co.templex.bots.lib.discord.Module;

import java.util.Arrays;
import java.util.List;

public class OplistModule extends Module {

    public OplistModule() {
        super("op");
    }

    @Override
    public List<ListenerFactory> setup() {
        OpListener.Factory opListenerFactory = new OpListener.Factory(
                getBot().getProperty("op-reports", null)
        );
        DeopListener.Factory deopListenerFactory = new DeopListener.Factory(
                getBot().getProperty("op-reports", null)
        );
        OplistModificationCommand.Factory oplistModificationCommandFactory = new OplistModificationCommand.Factory(
                Arrays.asList(getBot().getProperty("op-channels", null).split(",")),
                getWriter()
        );
        return Arrays.asList(opListenerFactory, deopListenerFactory, oplistModificationCommandFactory);
    }
}
