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

package co.templex.bots.lib.reader;

import de.btobastian.javacord.listener.Listener;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class LogReader {

    private static final Logger logger = LoggerFactory.getLogger(LogReader.class);

    private final ArrayList<LineListener> lineListeners;
    private final AtomicReference<CountDownLatch> countDownLatch;
    private final CountDownLatch shutdownLatch;
    private final AtomicBoolean running;

    private final ExecutorService readThread = Executors.newSingleThreadExecutor();

    private final ExecutorService listenerThread = Executors.newSingleThreadExecutor();

    public LogReader(CountDownLatch shutdownLatch) {
        this.lineListeners = new ArrayList<>();
        countDownLatch = new AtomicReference<>(new CountDownLatch(0));
        this.shutdownLatch = shutdownLatch;
        this.running = new AtomicBoolean(false);
    }

    public void start() {
        logger.debug("Starting LogReader.");
        if (!running.getAndSet(true)) {
            readThread.submit(this::readLoop);
            logger.debug("Successfully started.");
        }
    }

    private void readLoop() {
        logger.debug("Read loop initiated.");
        try {
            Path path = Paths.get(System.getProperty("user.dir"), "logs");
            File latestLog = new File(path.toFile(), "latest.log");
            BufferedReader reader = null;
            try (final WatchService watchService = FileSystems.getDefault().newWatchService()) {
                if (latestLog.exists()) {
                    reader = new BufferedReader(new FileReader(latestLog));
                    discardUntilEnd(reader);
                }
                path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);
                WatchKey wk;
                do {
                    wk = watchService.take();
                    for (WatchEvent<?> event : wk.pollEvents()) {
                        if (event.kind() == StandardWatchEventKinds.OVERFLOW) {
                            continue;
                        }
                        if (((Path) event.context()).endsWith("latest.log")) {
                            if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                                if (reader != null) {
                                    reader.close();
                                    reader = null;
                                }
                                continue;
                            }
                            if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                                reader = new BufferedReader(new FileReader(latestLog));
                            }
                            if (reader != null) {
                                CountDownLatch lock = lockListenerList();
                                for (String line; (line = reader.readLine()) != null; ) {
                                    final String finalLine = line;
                                    for (final LineListener lineListener : lineListeners) {
                                        listenerThread.submit(() -> lineListener.onLine(finalLine));
                                    }
                                }
                                lock.countDown();
                            }
                        }
                    }

                    if (!wk.reset()) {
                        logger.warn("Watch key was unregistered.");
                    }
                } while (!readThread.isShutdown());
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException | InterruptedException e) {
                logger.error("Broke out of file update loop.", e);
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e1) {
                        logger.error("Failed to close buffered file reader.", e);
                    }
                }
            }
        } finally {
            shutdownLatch.countDown();
        }
    }

    /**
     * Helper method to discard all previous entries (server is mid-execution, bot is just starting execution).
     *
     * @param reader Reader to discard all previous entries from.
     * @throws IOException If there is an error in the reader.
     */
    private void discardUntilEnd(BufferedReader reader) throws IOException {
        //noinspection StatementWithEmptyBody
        while (reader.readLine() != null) ;
    }

    public void stop() {
        logger.debug("Attempting to stop LogReader.");
        if (running.get()) {
            readThread.shutdownNow();
            logger.debug("Successfully shut down LogReader.");
        }
    }

    public void register(@NonNull Listener lineListener) {
        if (lineListener instanceof LineListener) {
            try {
                CountDownLatch lock = null;
                try {
                    lock = lockListenerList();
                    lineListeners.add((LineListener) lineListener);
                    logger.info("Registered listener of type " + lineListener.getClass().getSimpleName());
                } finally {
                    if (lock != null) {
                        lock.countDown();
                    }
                }
            } catch (InterruptedException e) {
                logger.error("Interrupt during listener registration.", e);
            }
        }
    }

    private CountDownLatch lockListenerList() throws InterruptedException {
        CountDownLatch lock;
        countDownLatch.getAndSet(lock = new CountDownLatch(1)).await();
        return lock;
    }

}
