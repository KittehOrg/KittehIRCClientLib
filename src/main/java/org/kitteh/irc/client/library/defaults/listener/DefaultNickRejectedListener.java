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
package org.kitteh.irc.client.library.defaults.listener;

import net.engio.mbassy.listener.Handler;
import org.jspecify.annotations.NonNull;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.event.client.ClientReceiveNumericEvent;
import org.kitteh.irc.client.library.event.client.NickRejectedEvent;
import org.kitteh.irc.client.library.feature.ServerInfo;
import org.kitteh.irc.client.library.feature.filter.NumericFilter;

import java.util.List;

/**
 * Default nick rejected listener, producing events using default classes.
 * <p>
 * Before registration completes, a rejected nick is retried automatically,
 * first as the intended nick with a ` appended and then with an increasing
 * number appended. A nick longer than 9 characters (the RFC 1459 limit)
 * rejected as erroneous is retried at a shorter length first, as the server's
 * nick length limit is not known until after registration. After
 * 10 rejections, the same is tried with the intended nick
 * reduced to the characters allowed by RFC 2812, if that differs. After
 * registration, or once the attempts are exhausted, a new nick is only sent
 * if set via {@link NickRejectedEvent#setNewNick(String)}.
 */
public class DefaultNickRejectedListener extends AbstractDefaultListenerBase {
    private static final int MAX_ATTEMPTS = 10;
    private static final int MIN_NICK_LENGTH = 9; // RFC 1459 limit, accepted by any server

    private ServerInfo stateServerInfo;
    private boolean registered;
    private boolean sanitized;
    private boolean gaveUp;
    private int attempts;
    private int step;
    private int truncationLimit;
    private int guessedLimit;

    /**
     * Constructs the listener.
     *
     * @param client client
     */
    public DefaultNickRejectedListener(Client.@NonNull WithManagement client) {
        super(client);
    }

    @NumericFilter(1)
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void welcome(ClientReceiveNumericEvent event) {
        this.updateState();
        this.registered = true;
    }

    @NumericFilter(431) // No nick given
    @NumericFilter(432) // Erroneous nickname
    @NumericFilter(433) // Nick in use
    @NumericFilter(436) // Nick collision
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void nickInUse(ClientReceiveNumericEvent event) {
        this.updateState();
        String intendedNick = this.getClient().getIntendedNick();
        String requestedNick = this.getClient().getRequestedNick();

        // Servers that silently truncate long nicks reply with the truncated nick
        List<String> parameters = event.getParameters();
        if (parameters.size() > 1) {
            String rejectedNick = parameters.get(1);
            if (!rejectedNick.isEmpty() && (rejectedNick.length() < requestedNick.length()) && requestedNick.startsWith(rejectedNick)) {
                this.truncationLimit = rejectedNick.length();
            }
        }

        boolean automatic = false;
        String defaultNewNick;
        if (this.registered) {
            defaultNewNick = this.candidate(intendedNick, 1, this.truncationLimit);
        } else {
            if (++this.attempts > MAX_ATTEMPTS) {
                if (!this.sanitized && !sanitize(intendedNick).equals(intendedNick)) {
                    this.sanitized = true;
                    this.attempts = 0;
                    this.step = 0;
                    this.guessedLimit = -1;
                    automatic = true;
                }
            } else {
                automatic = true;
                if ((event.getNumeric() == 432) && (requestedNick.length() > MIN_NICK_LENGTH)) {
                    // Possibly too long, so try the same nick shorter before moving on
                    this.guessedLimit = Math.max(MIN_NICK_LENGTH, (MIN_NICK_LENGTH + requestedNick.length()) / 2);
                } else {
                    this.step++;
                }
            }
            String base = this.sanitized ? sanitize(intendedNick) : intendedNick;
            int limit = (this.guessedLimit > 0) && ((this.truncationLimit < 0) || (this.guessedLimit < this.truncationLimit)) ? this.guessedLimit : this.truncationLimit;
            defaultNewNick = this.candidate(base, automatic ? this.step : (this.step + 1), limit);
        }
        if (defaultNewNick.equals(requestedNick)) {
            defaultNewNick = defaultNewNick + '`';
        }

        NickRejectedEvent nickRejectedEvent = new NickRejectedEvent(this.getClient(), event.getSource(), requestedNick, defaultNewNick);
        this.fire(nickRejectedEvent);
        if (automatic || !nickRejectedEvent.getNewNick().equals(defaultNewNick)) {
            this.getClient().sendNickChange(nickRejectedEvent.getNewNick());
        } else if (!this.registered && !this.gaveUp) {
            this.gaveUp = true;
            this.trackException(event, "Nick rejected too many times, no longer retrying");
        }
    }

    private void updateState() {
        ServerInfo serverInfo = this.getClient().getServerInfo();
        if (serverInfo != this.stateServerInfo) {
            this.stateServerInfo = serverInfo;
            this.registered = false;
            this.sanitized = false;
            this.gaveUp = false;
            this.attempts = 0;
            this.step = 0;
            this.truncationLimit = -1;
            this.guessedLimit = -1;
        }
    }

    /**
     * Gets the nick for a step of the sequence base, base`, base1, base2,
     * and so on, with base shortened to fit the limit.
     */
    private @NonNull String candidate(@NonNull String base, int step, int limit) {
        String suffix = switch (step) {
            case 0 -> "";
            case 1 -> "`";
            default -> String.valueOf(step - 1);
        };
        if ((limit > 0) && ((base.length() + suffix.length()) > limit)) {
            base = base.substring(0, Math.max(1, limit - suffix.length()));
        }
        return base + suffix;
    }

    /**
     * Reduces a nick to the RFC 2812 nickname characters: letters, digits,
     * []\`_^{|} and -, not starting with a digit or -.
     */
    static @NonNull String sanitize(@NonNull String nick) {
        StringBuilder builder = new StringBuilder(nick.length() + 1);
        for (char ch : nick.toCharArray()) {
            boolean allowed = ((ch >= 'A') && (ch <= '}')) || ((ch >= '0') && (ch <= '9')) || (ch == '-');
            builder.append(allowed ? ch : '_');
        }
        char first = builder.charAt(0);
        if (((first >= '0') && (first <= '9')) || (first == '-')) {
            builder.insert(0, '_');
        }
        return builder.toString();
    }
}
