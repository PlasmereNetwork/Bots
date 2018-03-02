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

import co.templex.bots.lib.discord.*;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import de.btobastian.javacord.entities.Channel;
import de.btobastian.javacord.listener.Listener;
import lombok.NonNull;
import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static co.templex.bots.lib.discord.Util.generateEmbedBuilder;

public class LivingListener extends ChannelWriter implements CustomListener {

    private static final Logger logger = LoggerFactory.getLogger(LivingListener.class);

    private final ScheduledExecutorService service;
    private final InetSocketAddress[] address;
    private final int timeout;

    private boolean previous[];

    private LivingListener(Channel reportChannel, String[] host, int[] port, int timeout, int interval) {
        super(reportChannel);
        this.service = Executors.newScheduledThreadPool(host.length);
        this.address = new InetSocketAddress[host.length];
        this.timeout = timeout;
        this.previous = new boolean[host.length];
        Arrays.fill(previous, true);
        for (final int i : ContiguousSet.create(Range.closed(0, host.length - 1), DiscreteDomain.integers()).asList().toArray(new Integer[host.length])) {
            this.address[i] = new InetSocketAddress(host[i], port[0]);
            this.service.scheduleAtFixedRate(() -> query(i), interval, interval, TimeUnit.SECONDS);
        }
    }

    private void query(final int index) {
        logger.info(String.format("Attempting connection to host %s on port %d", address[index].getHostName(), address[index].getPort()));
        Throwable e = null;
        try (Socket socket = new Socket()) {
            socket.connect(address[index], timeout);
            logger.info("Connection successful");
        } catch (Throwable internal) {
            e = internal;
        }
        report(index, e);
    }

    private void report(final int index, Throwable e) {
        String message;
        if (e == null) {
            message = String.format("Successfully connected to host %s on port %d", address[index].getHostName(), address[index].getPort());
            logger.info(message);
        } else {
            if (e instanceof UnknownHostException) {
                message = String.format("Failed to resolve host %s", address[index].getHostName());
            } else if (e instanceof SocketTimeoutException) {
                message = String.format("Connection timed out while attempting to connect to host %s on port %d", address[index].getHostName(), address[index].getPort());
            } else if (e.getMessage().equals("Connection refused")) {
                message = String.format("Connection refused to host %s on port %d", address[index].getHostName(), address[index].getPort());
            } else if (e.getMessage().equals("Resource temporarily unavailable")) {
                message = String.format("Resource temporarily unavailable (likely a mismatch in DNS entry) while connecting to host %s on port %d", address[index].getHostName(), address[index].getPort());
            } else {
                message = String.format("Unhandled Exception while connecting to host %s on port %d.\n\nSee Living Listener log for details.", address[index].getHostName(), address[index].getPort());
            }
            logger.warn(message, e);
        }
        if (previous[index] != (previous[index] = e == null)) {
            if (e == null) {
                getReportChannel().sendMessage("", generateEmbedBuilder(
                        "Living Listener: All Clear",
                        message,
                        null,
                        null,
                        null,
                        Color.GREEN
                ));
            } else {
                getReportChannel().sendMessage("", generateEmbedBuilder(
                        "Living Listener: Alert!",
                        String.format("Help, I've gone down and I can't get up!\n\n%s", message),
                        String.format("%s while attempting connection.", e.getClass().getName()),
                        null,
                        null,
                        Color.RED
                ));
            }
        }
    }

    @Override
    public void shutdown() {
        this.service.shutdown();
    }

    @Value
    public static class Factory implements ListenerFactory {

        @NonNull
        String channelID;
        @NonNull
        String[] host;
        int[] port;
        int timeout, interval;

        @Override
        public Listener generateListener(Bot bot, ChannelFactory factory) {
            return new LivingListener(
                    factory.generateChannel(channelID),
                    host,
                    port,
                    timeout,
                    interval
            );
        }
    }
}
