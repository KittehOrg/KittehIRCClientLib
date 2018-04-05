/*
 * * Copyright (C) 2013-2018 Matt Baxter https://kitteh.org
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
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.defaults.element.DefaultWhoisData;
import org.kitteh.irc.client.library.element.WhoisData;
import org.kitteh.irc.client.library.event.client.ClientReceiveNumericEvent;
import org.kitteh.irc.client.library.event.user.WhoisEvent;
import org.kitteh.irc.client.library.feature.filter.NumericFilter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Default WHOIS listener, producing events using default classes.
 */
public class DefaultWhoisListener extends AbstractDefaultListenerBase {
    @Nullable
    private DefaultWhoisData.Builder whoisBuilder;

    /**
     * Constructs the listener.
     *
     * @param client client
     */
    public DefaultWhoisListener(@Nonnull Client.WithManagement client) {
        super(client);
    }

    private DefaultWhoisData.Builder getWhoisBuilder(@Nonnull ClientReceiveNumericEvent event) {
        String nick = event.getParameters().get(1);
        if ((this.whoisBuilder == null) ||
                !this.getClient().getServerInfo().getCaseMapping().areEqualIgnoringCase(this.whoisBuilder.getNick(), nick)) { // If suddenly a nick change, discard old
            this.whoisBuilder = new DefaultWhoisData.Builder(this.getClient(), nick);
        }
        return this.whoisBuilder;
    }

    @NumericFilter(301) // WHOISAWAY
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void whoisAway(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < 3) {
            this.trackException(event, "WHOIS AWAY response too short");
            return;
        }
        this.getWhoisBuilder(event).setAway(event.getParameters().get((event.getParameters().size() == 3) ? 2 : 3));
    }

    @NumericFilter(311) // WHOISUSER
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void whoisUser(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < 2) {
            this.trackException(event, "WHOIS USER response too short");
            return;
        }
        DefaultWhoisData.Builder whoisBuilder = this.getWhoisBuilder(event);
        switch (event.getParameters().size()) {
            case 6: // If long enough for real name, grab real name
                whoisBuilder.setRealName(event.getParameters().get(5));
            case 4: // If long enough for host, grab host
                whoisBuilder.setHost(event.getParameters().get(3));
            case 3: // If long enough for user string, grab user string. Though this one is kinda expected.
                whoisBuilder.setUserString(event.getParameters().get(2));
        }
    }

    @NumericFilter(312) // WHOISSERVER
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void whoisServer(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < 3) {
            this.trackException(event, "WHOIS SERVER response too short");
            return;
        }
        DefaultWhoisData.Builder whoisBuilder = this.getWhoisBuilder(event);
        whoisBuilder.setServer(event.getParameters().get(2));
        if (event.getParameters().size() > 3) {
            whoisBuilder.setServerDescription(event.getParameters().get(3));
        }
    }

    @NumericFilter(313) // WHOISOPERATOR
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void whoisOperator(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < 3) {
            this.trackException(event, "WHOIS OPERATOR response too short");
            return;
        }
        this.getWhoisBuilder(event).setOperatorInformation(event.getParameters().get(2));
    }

    @NumericFilter(317) // WHOISIDLE
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void whoisIdle(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < 4) {
            this.trackException(event, "WHOIS IDLE response too short");
            return;
        }
        DefaultWhoisData.Builder whoisBuilder = this.getWhoisBuilder(event);
        long idleTime;
        try {
            idleTime = Long.parseLong(event.getParameters().get(2));
        } catch (NumberFormatException e) {
            this.trackException(event, "WHOIS IDLE idle time not a number");
            return;
        }
        whoisBuilder.setIdleTime(idleTime);
        if (event.getParameters().size() > 4) {
            long signOnTime;
            try {
                signOnTime = Long.parseLong(event.getParameters().get(3));
            } catch (NumberFormatException e) {
                this.trackException(event, "WHOIS IDLE sign on time not a number");
                return;
            }
            whoisBuilder.setSignOnTime(signOnTime);
        }
    }

    @NumericFilter(330) // WHOISACCOUNT
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void whoisAccount(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < 3) {
            this.trackException(event, "WHOIS ACCOUNT response too short");
            return;
        }
        this.getWhoisBuilder(event).setAccount(event.getParameters().get(2));
    }

    @NumericFilter(319) // WHOISCHANNELS
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void whoisChannels(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < 3) {
            this.trackException(event, "WHOIS CHANNELS response too short");
            return;
        }
        this.getWhoisBuilder(event).addChannels(event.getParameters().get(2));
    }

    @NumericFilter(671) // WHOISSECURE
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void whoisSecure(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < 2) {
            this.trackException(event, "WHOIS SECURE response too short");
            return;
        }
        this.getWhoisBuilder(event).setSecure();
    }

    @NumericFilter(318) // ENDOFWHOIS
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void whoisEnd(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < 2) {
            this.trackException(event, "WHOIS END response too short");
            return;
        }
        WhoisData whois = this.getWhoisBuilder(event).build();
        if (this.getClient().getServerInfo().getCaseMapping().areEqualIgnoringCase(whois.getNick(), this.getClient().getNick()) &&
                (!this.getTracker().getTrackedUser(whois.getNick()).isPresent())) {
            this.getTracker().trackUser(whois);
        }
        this.fire(new WhoisEvent(this.getClient(), whois));
        this.whoisBuilder = null;
    }
}
