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
package org.kitteh.irc.client.library.defaults.feature;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.defaults.element.DefaultISupportParameter;
import org.kitteh.irc.client.library.defaults.element.isupport.DefaultISupportCaseMapping;
import org.kitteh.irc.client.library.defaults.element.isupport.DefaultISupportChanLimit;
import org.kitteh.irc.client.library.defaults.element.isupport.DefaultISupportChanModes;
import org.kitteh.irc.client.library.defaults.element.isupport.DefaultISupportChanTypes;
import org.kitteh.irc.client.library.defaults.element.isupport.DefaultISupportChannelLen;
import org.kitteh.irc.client.library.defaults.element.isupport.DefaultISupportModes;
import org.kitteh.irc.client.library.defaults.element.isupport.DefaultISupportNetwork;
import org.kitteh.irc.client.library.defaults.element.isupport.DefaultISupportNickLen;
import org.kitteh.irc.client.library.defaults.element.isupport.DefaultISupportPrefix;
import org.kitteh.irc.client.library.defaults.element.isupport.DefaultISupportTopicLen;
import org.kitteh.irc.client.library.defaults.element.isupport.DefaultISupportWhoX;
import org.kitteh.irc.client.library.element.ISupportParameter;
import org.kitteh.irc.client.library.exception.KittehServerISupportException;
import org.kitteh.irc.client.library.feature.ISupportManager;
import org.kitteh.irc.client.library.util.AbstractNameValueProcessor;
import org.kitteh.irc.client.library.util.TriFunction;

import java.util.Optional;

/**
 * Default implementation of {@link ISupportManager}.
 */
public class DefaultISupportManager extends AbstractNameValueProcessor<ISupportParameter> implements ISupportManager {
    /**
     * Constructs the manager.
     *
     * @param client client for which this exists
     */
    public DefaultISupportManager(Client.WithManagement client) {
        super(client);
        this.registerParameter(ISupportParameter.CaseMapping.NAME, DefaultISupportCaseMapping::new);
        this.registerParameter(ISupportParameter.ChannelLen.NAME, DefaultISupportChannelLen::new);
        this.registerParameter(ISupportParameter.ChanLimit.NAME, DefaultISupportChanLimit::new);
        this.registerParameter(ISupportParameter.ChanModes.NAME, DefaultISupportChanModes::new);
        this.registerParameter(ISupportParameter.ChanTypes.NAME, DefaultISupportChanTypes::new);
        this.registerParameter(ISupportParameter.Modes.NAME, DefaultISupportModes::new);
        this.registerParameter(ISupportParameter.Network.NAME, DefaultISupportNetwork::new);
        this.registerParameter(ISupportParameter.NickLen.NAME, DefaultISupportNickLen::new);
        this.registerParameter(ISupportParameter.Prefix.NAME, DefaultISupportPrefix::new);
        this.registerParameter(ISupportParameter.TopicLen.NAME, DefaultISupportTopicLen::new);
        this.registerParameter(ISupportParameter.WhoX.NAME, DefaultISupportWhoX::new);
    }

    @Override
    public @NonNull Optional<TriFunction<Client, String, String, ? extends ISupportParameter>> getCreator(@NonNull String tagName) {
        return this.getCreatorByName(tagName);
    }

    @Override
    public @NonNull Optional<TriFunction<Client, String, String, ? extends ISupportParameter>> registerParameter(@NonNull String tagName, @NonNull TriFunction<Client, String, String, ? extends ISupportParameter> function) {
        return this.registerCreator(tagName, new Creator<>(function));
    }

    @Override
    public @NonNull Optional<TriFunction<Client, String, String, ? extends ISupportParameter>> unregisterParameter(@NonNull String tagName) {
        return this.unregisterCreator(tagName);
    }

    @Override
    public @NonNull ISupportParameter createParameter(@NonNull String tag) {
        int index;
        Creator<ISupportParameter> creator;
        String tagName;
        @Nullable String value;
        // Split out value if present
        if (((index = tag.indexOf('=')) > -1) && (index < (tag.length() - 1))) {
            tagName = tag.substring(0, index);
            value = tag.substring(index + 1);
        } else {
            tagName = tag;
            value = null;
        }
        ISupportParameter iSupportParameter = null;
        // Attempt creating from registered creator, fall back on default
        if ((creator = this.getRegistrations().get(tagName)) != null) {
            try {
                iSupportParameter = creator.getFunction().apply(this.getClient(), tagName, value);
            } catch (KittehServerISupportException thrown) {
                this.getClient().getExceptionListener().queue(new KittehServerISupportException(tag, "Creator failed: " + thrown.getMessage()));
            } catch (Exception thrown) {
                this.getClient().getExceptionListener().queue(new KittehServerISupportException(tag, "Creator failed", thrown));
            }
        }
        if (iSupportParameter == null) {
            iSupportParameter = new DefaultISupportParameter(this.getClient(), tagName, value);
        }
        return iSupportParameter;
    }
}
