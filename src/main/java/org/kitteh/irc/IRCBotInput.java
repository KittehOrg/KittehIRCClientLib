/*
 * * Copyright (C) 2013-2014 Matt Baxter http://kitteh.org
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.kitteh.irc;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;

final class IRCBotInput extends Thread {
    private final IRCBot bot;
    private final Socket socket;
    private final BufferedReader bufferedReader;
    private boolean running = true;
    private long lastInputTime;

    IRCBotInput(Socket socket, BufferedReader bufferedReader, IRCBot bot) {
        this.bot = bot;
        this.socket = socket;
        this.bufferedReader = bufferedReader;
        this.setName("Kitteh IRCBot Input (" + bot.getName() + ")");
    }

    @Override
    public void run() {
        while (this.running) {
            try {
                String line;
                while (this.running && ((line = this.bufferedReader.readLine()) != null)) {
                    this.lastInputTime = System.currentTimeMillis();
                    this.bot.processLine(line);
                }
            } catch (final IOException e) {
                if (this.running) {
                    this.bot.sendRawLine("PING " + (System.currentTimeMillis() / 1000), true);
                }
            }
        }
        try {
            this.socket.close();
        } catch (final Exception ignored) {
        }
    }

    void shutdown() {
        this.running = false;
        try {
            this.bufferedReader.close();
            this.socket.close();
        } catch (final IOException ignored) {
        }
    }

    long timeSinceInput() {
        return System.currentTimeMillis() - this.lastInputTime;
    }
}