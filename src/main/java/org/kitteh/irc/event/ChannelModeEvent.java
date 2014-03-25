package org.kitteh.irc.event;

import org.kitteh.irc.elements.Actor;
import org.kitteh.irc.elements.Channel;

/**
 * Channel a la mode.
 */
public class ChannelModeEvent {
    private Actor actor;
    private Channel channel;
    private boolean setting;
    private char mode;
    private String arg;

    public ChannelModeEvent(Actor actor, Channel channel, boolean setting, char mode, String arg) {
        this.actor = actor;
        this.channel = channel;
        this.setting = setting;
        this.mode = mode;
        this.arg = arg;
    }

    public Actor getActor() {
        return this.actor;
    }

    public String getArgument() {
        return this.arg;
    }

    public Channel getChannel() {
        return this.channel;
    }

    public char getMode() {
        return this.mode;
    }

    public boolean isSetting() {
        return this.setting;
    }
}