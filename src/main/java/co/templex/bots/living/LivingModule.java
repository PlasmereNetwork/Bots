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

package co.templex.bots.living;

import co.templex.bots.lib.discord.ListenerFactory;
import co.templex.bots.lib.discord.Module;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

public class LivingModule extends Module {
    public LivingModule() {
        super("living");
    }

    @Override
    public List<ListenerFactory> setup() throws Exception {
        return Collections.singletonList(new LivingListener.Factory(
                getBot().getProperty("living-channel", null),
                getBot().getProperty("living-hosts", null).split(","),
                ((Callable<int[]>) () -> {
                    String[] portStrings = getBot().getProperty("living-ports", null).split(",");
                    int[] returned = new int[portStrings.length];
                    for (int i = 0; i < portStrings.length; i++) {
                        returned[i] = Integer.parseInt(portStrings[i]);
                    }
                    return returned;
                }).call(),
                Integer.parseInt(getBot().getProperty("living-timeout", "30")),
                Integer.parseInt(getBot().getProperty("living-interval", "60")
                )));
    }
}
