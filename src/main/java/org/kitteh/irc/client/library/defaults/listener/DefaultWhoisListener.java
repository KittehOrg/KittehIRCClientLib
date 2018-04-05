package org.kitteh.irc.client.library.defaults.listener;

import net.engio.mbassy.listener.Handler;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.defaults.element.DefaultWhoisData;
import org.kitteh.irc.client.library.element.WhoisData;
import org.kitteh.irc.client.library.event.client.ClientReceiveNumericEvent;
import org.kitteh.irc.client.library.event.user.WhoisEvent;
import org.kitteh.irc.client.library.feature.filter.NumericFilter;

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
    public DefaultWhoisListener(Client.WithManagement client) {
        super(client);
    }

    private DefaultWhoisData.Builder getWhoisBuilder(String nick) {
        if ((this.whoisBuilder == null) || !this.getClient().getServerInfo().getCaseMapping().areEqualIgnoringCase(this.whoisBuilder.getNick(), nick)) {
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
        this.getWhoisBuilder(event.getParameters().get(1)).setAway(event.getParameters().get((event.getParameters().size() == 3) ? 2 : 3));
    }

    @NumericFilter(311) // WHOISUSER
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void whoisUser(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < 2) {
            this.trackException(event, "WHOIS USER response too short");
            return;
        }
        DefaultWhoisData.Builder whoisBuilder = this.getWhoisBuilder(event.getParameters().get(1));
        switch (event.getParameters().size()) {
            case 6:
                whoisBuilder.setRealName(event.getParameters().get(5));
            case 4:
                whoisBuilder.setHost(event.getParameters().get(3));
            case 3:
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
        DefaultWhoisData.Builder whoisBuilder = this.getWhoisBuilder(event.getParameters().get(1));
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
        this.getWhoisBuilder(event.getParameters().get(1)).setOperatorInformation(event.getParameters().get(2));
    }

    @NumericFilter(317) // WHOISIDLE
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void whoisIdle(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < 4) {
            this.trackException(event, "WHOIS IDLE response too short");
            return;
        }
        DefaultWhoisData.Builder whoisBuilder = this.getWhoisBuilder(event.getParameters().get(1));
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
        this.getWhoisBuilder(event.getParameters().get(1)).setAccount(event.getParameters().get(2));
    }

    @NumericFilter(319) // WHOISCHANNELS
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void whoisChannels(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < 3) {
            this.trackException(event, "WHOIS CHANNELS response too short");
            return;
        }
        this.getWhoisBuilder(event.getParameters().get(1)).addChannels(event.getParameters().get(2));
    }

    @NumericFilter(671) // WHOISSECURE
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void whoisSecure(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < 2) {
            this.trackException(event, "WHOIS SECURE response too short");
            return;
        }
        this.getWhoisBuilder(event.getParameters().get(1)).setSecure();
    }

    @NumericFilter(318) // ENDOFWHOIS
    @Handler(priority = Integer.MAX_VALUE - 1)
    public void whoisEnd(ClientReceiveNumericEvent event) {
        if (event.getParameters().size() < 2) {
            this.trackException(event, "WHOIS END response too short");
            return;
        }
        WhoisData whois = this.getWhoisBuilder(event.getParameters().get(1)).build();
        if (this.getClient().getServerInfo().getCaseMapping().areEqualIgnoringCase(whois.getNick(), this.getClient().getNick()) && (!this.getTracker().getTrackedUser(whois.getNick()).isPresent())) {
            this.getTracker().trackUser(whois);
        }
        this.fire(new WhoisEvent(this.getClient(), whois));
        this.whoisBuilder = null;
    }
}
