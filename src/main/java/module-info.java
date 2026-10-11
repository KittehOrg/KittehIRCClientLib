/*
 * * Copyright (C) 2013-2026 Matt Baxter https://kitteh.org
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
module org.kitteh.irc.client.library {
    requires transitive mbassador;
    requires transitive org.jspecify;
    requires io.netty.buffer;
    requires io.netty.codec;
    requires io.netty.common;
    requires io.netty.handler;
    requires io.netty.handler.proxy;
    requires transitive io.netty.transport;

    exports org.kitteh.irc.client.library;
    exports org.kitteh.irc.client.library.command;
    exports org.kitteh.irc.client.library.defaults;
    exports org.kitteh.irc.client.library.defaults.element;
    exports org.kitteh.irc.client.library.defaults.element.isupport;
    exports org.kitteh.irc.client.library.defaults.element.messagetag;
    exports org.kitteh.irc.client.library.defaults.element.mode;
    exports org.kitteh.irc.client.library.defaults.feature;
    exports org.kitteh.irc.client.library.defaults.feature.network;
    exports org.kitteh.irc.client.library.defaults.listener;
    exports org.kitteh.irc.client.library.element;
    exports org.kitteh.irc.client.library.element.mode;
    exports org.kitteh.irc.client.library.event.abstractbase;
    exports org.kitteh.irc.client.library.event.batch;
    exports org.kitteh.irc.client.library.event.capabilities;
    exports org.kitteh.irc.client.library.event.channel;
    exports org.kitteh.irc.client.library.event.client;
    exports org.kitteh.irc.client.library.event.connection;
    exports org.kitteh.irc.client.library.event.helper;
    exports org.kitteh.irc.client.library.event.user;
    exports org.kitteh.irc.client.library.exception;
    exports org.kitteh.irc.client.library.feature;
    exports org.kitteh.irc.client.library.feature.auth;
    exports org.kitteh.irc.client.library.feature.auth.element;
    exports org.kitteh.irc.client.library.feature.defaultmessage;
    exports org.kitteh.irc.client.library.feature.filter;
    exports org.kitteh.irc.client.library.feature.network;
    exports org.kitteh.irc.client.library.feature.sending;
    exports org.kitteh.irc.client.library.feature.sts;
    exports org.kitteh.irc.client.library.feature.twitch;
    exports org.kitteh.irc.client.library.feature.twitch.event;
    exports org.kitteh.irc.client.library.feature.twitch.messagetag;
    exports org.kitteh.irc.client.library.util;
}
