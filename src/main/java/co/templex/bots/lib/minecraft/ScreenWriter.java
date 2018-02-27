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

package co.templex.bots.lib.minecraft;

import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class ScreenWriter {

    private static final Logger logger = LoggerFactory.getLogger(ScreenWriter.class);

    private final Base64.Encoder encoder = Base64.getEncoder();

    private final AtomicReference<CountDownLatch> latch = new AtomicReference<>(new CountDownLatch(0));

    public void println(@NonNull String line) {
        try {
            CountDownLatch lock = lockChat();
            try {
                String executedCommand = String.format("./write_to_server %s", encoder.encodeToString((line + "\n").getBytes()));
                logger.info(String.format("Executing raw command \"%s\"", executedCommand));
                Runtime.getRuntime().exec(executedCommand).waitFor();
            } catch (IOException e) {
                logger.error("Failed to execute screen write.", e);
            } finally {
                lock.countDown();
            }
        } catch (InterruptedException e) {
            logger.error("Interrupt during listener registration.", e);
        }
    }

    private CountDownLatch lockChat() throws InterruptedException {
        CountDownLatch lock;
        latch.getAndSet(lock = new CountDownLatch(1)).await();
        return lock;
    }

}
