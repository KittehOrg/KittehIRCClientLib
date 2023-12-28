/*
 * * Copyright (C) 2013-2023 Matt Baxter https://kitteh.org
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
package org.kitteh.irc.client.library.event.user;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.Actor;
import org.kitteh.irc.client.library.element.ServerMessage;
import org.kitteh.irc.client.library.element.mode.ModeStatusList;
import org.kitteh.irc.client.library.element.mode.UserMode;
import org.kitteh.irc.client.library.event.abstractbase.ActorEventBase;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

/**
 * User a la mode.
 */
public class UserModeEvent extends ActorEventBase<Actor> {
    private final ModeStatusList<UserMode> statusList;
    private final String target;

    /**
     * Creates the event.
     *
     * @param client client for which this is occurring
     * @param sourceMessage source message
     * @param actor the mode setter
     * @param target the target for whom the change is occurring
     * @param statusList list of statuses
     */
    public UserModeEvent(@NonNull Client client, @NonNull ServerMessage sourceMessage, @NonNull Actor actor, @NonNull String target, @NonNull ModeStatusList<UserMode> statusList) {
        super(client, sourceMessage, actor);
        this.statusList = Sanity.nullCheck(statusList, "Status list");
        this.target = Sanity.nullCheck(target, "Target");
    }

    /**
     * Gets the list of statuses.
     *
     * @return status list
     */
    public @NonNull ModeStatusList<UserMode> getStatusList() {
        return this.statusList;
    }

    @Override
    public @NonNull String toString() {
        return new ToStringer(this).add("client", this.getClient()).add("actor", this.getActor()).add("statusList", this.statusList).add("target", this.target).toString();
    }

    @Override
    protected @NonNull ToStringer toStringer() {
        return super.toStringer().add("target", this.target).add("statusList", this.statusList);
    }
}
