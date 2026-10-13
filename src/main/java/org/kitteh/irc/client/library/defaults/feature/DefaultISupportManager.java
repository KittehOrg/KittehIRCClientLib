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
package org.kitteh.irc.client.library.defaults.feature;

import org.jspecify.annotations.NonNull;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.defaults.element.DefaultISupportParameter;
import org.kitteh.irc.client.library.defaults.element.isupport.*;
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
    public DefaultISupportManager(Client.@NonNull WithManagement client) {
        super(client);
        this.registerParameter(ISupportParameter.AwayLen.NAME, DefaultISupportAwayLen::new);
        this.registerParameter(ISupportParameter.Bot.NAME, DefaultISupportBot::new);
        this.registerParameter(ISupportParameter.CaseMapping.NAME, DefaultISupportCaseMapping::new);
        this.registerParameter(ISupportParameter.ChannelLen.NAME, DefaultISupportChannelLen::new);
        this.registerParameter(ISupportParameter.ChanLimit.NAME, DefaultISupportChanLimit::new);
        this.registerParameter(ISupportParameter.ChanModes.NAME, DefaultISupportChanModes::new);
        this.registerParameter(ISupportParameter.ChanTypes.NAME, DefaultISupportChanTypes::new);
        this.registerParameter(ISupportParameter.ClientTagDeny.NAME, DefaultISupportClientTagDeny::new);
        this.registerParameter(ISupportParameter.EList.NAME, DefaultISupportEList::new);
        this.registerParameter(ISupportParameter.Excepts.NAME, DefaultISupportExcepts::new);
        this.registerParameter(ISupportParameter.ExtBan.NAME, DefaultISupportExtBan::new);
        this.registerParameter(ISupportParameter.HostLen.NAME, DefaultISupportHostLen::new);
        this.registerParameter(ISupportParameter.InvEx.NAME, DefaultISupportInvEx::new);
        this.registerParameter(ISupportParameter.KickLen.NAME, DefaultISupportKickLen::new);
        this.registerParameter(ISupportParameter.MaxList.NAME, DefaultISupportMaxList::new);
        this.registerParameter(ISupportParameter.MaxTargets.NAME, DefaultISupportMaxTargets::new);
        this.registerParameter(ISupportParameter.Modes.NAME, DefaultISupportModes::new);
        this.registerParameter(ISupportParameter.Network.NAME, DefaultISupportNetwork::new);
        this.registerParameter(ISupportParameter.NickLen.NAME, DefaultISupportNickLen::new);
        this.registerParameter(ISupportParameter.Prefix.NAME, DefaultISupportPrefix::new);
        this.registerParameter(ISupportParameter.Silence.NAME, DefaultISupportSilence::new);
        this.registerParameter(ISupportParameter.StatusMsg.NAME, DefaultISupportStatusMsg::new);
        this.registerParameter(ISupportParameter.TargMax.NAME, DefaultISupportTargMax::new);
        this.registerParameter(ISupportParameter.TopicLen.NAME, DefaultISupportTopicLen::new);
        this.registerParameter(ISupportParameter.UserLen.NAME, DefaultISupportUserLen::new);
        this.registerParameter(ISupportParameter.UTF8Mapping.NAME, DefaultISupportUTF8Mapping::new);
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
        String value;
        // Split out value if present
        if ((index = tag.indexOf('=')) > -1) {
            tagName = tag.substring(0, index);
            value = (index < (tag.length() - 1)) ? unescape(tag.substring(index + 1)) : null;
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

    /**
     * Decodes \x20 (space), \x5C (backslash), and \x3D (equals).
     *
     * @param value raw value
     * @return decoded value
     */
    private static @NonNull String unescape(@NonNull String value) {
        if (value.indexOf('\\') < 0) { // Early bail!
            return value;
        }
        StringBuilder builder = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if ((ch == '\\') && ((i + 3) < value.length()) && (value.charAt(i + 1) == 'x')) {
                char decoded = switch (value.substring(i + 2, i + 4).toUpperCase()) {
                    case "20" -> ' ';
                    case "5C" -> '\\';
                    case "3D" -> '=';
                    default -> 0;
                };
                if (decoded != 0) {
                    builder.append(decoded);
                    i += 3;
                    continue;
                }
            }
            builder.append(ch);
        }
        return builder.toString();
    }
}
