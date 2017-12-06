/*
 * * Copyright (C) 2013-2017 Matt Baxter http://kitteh.org
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
package org.kitteh.irc.client.library.feature.defaultmanager;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.ISupportParameter;
import org.kitteh.irc.client.library.element.defaults.DefaultISupportParameter;
import org.kitteh.irc.client.library.element.defaults.isupport.ISupportCaseMapping;
import org.kitteh.irc.client.library.element.defaults.isupport.ISupportChanLimit;
import org.kitteh.irc.client.library.element.defaults.isupport.ISupportChanModes;
import org.kitteh.irc.client.library.element.defaults.isupport.ISupportChanTypes;
import org.kitteh.irc.client.library.element.defaults.isupport.ISupportChannelLen;
import org.kitteh.irc.client.library.element.defaults.isupport.ISupportModes;
import org.kitteh.irc.client.library.element.defaults.isupport.ISupportNetwork;
import org.kitteh.irc.client.library.element.defaults.isupport.ISupportNickLen;
import org.kitteh.irc.client.library.element.defaults.isupport.ISupportPrefix;
import org.kitteh.irc.client.library.element.defaults.isupport.ISupportTopicLen;
import org.kitteh.irc.client.library.element.defaults.isupport.ISupportWHOX;
import org.kitteh.irc.client.library.exception.KittehServerISupportException;
import org.kitteh.irc.client.library.feature.ISupportManager;
import org.kitteh.irc.client.library.util.AbstractNameValueProcessor;
import org.kitteh.irc.client.library.util.TriFunction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
        this.registerParameter(ISupportParameter.CaseMapping.NAME, ISupportCaseMapping::new);
        this.registerParameter(ISupportParameter.ChannelLen.NAME, ISupportChannelLen::new);
        this.registerParameter(ISupportParameter.ChanLimit.NAME, ISupportChanLimit::new);
        this.registerParameter(ISupportParameter.ChanModes.NAME, ISupportChanModes::new);
        this.registerParameter(ISupportParameter.ChanTypes.NAME, ISupportChanTypes::new);
        this.registerParameter(ISupportParameter.Modes.NAME, ISupportModes::new);
        this.registerParameter(ISupportParameter.Network.NAME, ISupportNetwork::new);
        this.registerParameter(ISupportParameter.NickLen.NAME, ISupportNickLen::new);
        this.registerParameter(ISupportParameter.Prefix.NAME, ISupportPrefix::new);
        this.registerParameter(ISupportParameter.TopicLen.NAME, ISupportTopicLen::new);
        this.registerParameter(ISupportParameter.WHOX.NAME, ISupportWHOX::new);
    }

    @Nonnull
    @Override
    public Optional<TriFunction<Client, String, String, ? extends ISupportParameter>> getCreator(@Nonnull String tagName) {
        return this.getCreatorByName(tagName);
    }

    @Nonnull
    @Override
    public Optional<TriFunction<Client, String, String, ? extends ISupportParameter>> registerParameter(@Nonnull String tagName, @Nonnull TriFunction<Client, String, String, ? extends ISupportParameter> function) {
        return this.registerCreator(tagName, new Creator<>(function));
    }

    @Nonnull
    @Override
    public Optional<TriFunction<Client, String, String, ? extends ISupportParameter>> unregisterParameter(@Nonnull String tagName) {
        return this.unregisterCreator(tagName);
    }

    @Nonnull
    @Override
    public ISupportParameter createParameter(@Nonnull String tag) {
        int index;
        Creator<ISupportParameter> creator;
        String tagName;
        @Nullable
        String value;
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
