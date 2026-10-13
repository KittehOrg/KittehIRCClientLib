package org.kitteh.irc.client.library.defaults.feature;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.feature.CaseMapping;
import org.kitteh.irc.client.library.feature.ServerInfo;
import org.mockito.Mockito;

import java.util.Set;

/**
 * Tests the DefaultActorTracker.
 */
public class DefaultActorTrackerTest {
    private DefaultActorTracker tracker;
    private CaseMapping caseMapping = CaseMapping.ASCII;

    @BeforeEach
    public void before() {
        Client.WithManagement client = Mockito.mock(Client.WithManagement.class);
        ServerInfo.WithManagement serverInfo = Mockito.mock(ServerInfo.WithManagement.class);
        Mockito.when(serverInfo.getCaseMapping()).thenAnswer(invocation -> this.caseMapping);
        Mockito.when(client.getServerInfo()).thenReturn(serverInfo);
        this.tracker = new DefaultActorTracker(client);
    }

    private Channel join(String channel, String... users) {
        this.tracker.trackChannel(channel);
        for (String user : users) {
            this.tracker.trackChannelUser(channel, (User) this.tracker.getActor(user), Set.of());
        }
        return this.tracker.getTrackedChannel(channel).orElseThrow();
    }

    /**
     * Tests that channel user lookups follow the casemapping.
     */
    @Test
    public void channelGetUserIgnoresCase() {
        Channel channel = this.join("#kitteh", "Kitteh!kitteh@kitteh.test", "Kat[]!kat@kitteh.test");
        Assertions.assertEquals("Kitteh", channel.getUser("KITTEH").orElseThrow().getNick());
        Assertions.assertEquals("Kat[]", channel.getUser("kat[]").orElseThrow().getNick());
        Assertions.assertTrue(channel.getUser("kat{}").isEmpty());
        Assertions.assertTrue(channel.getUserModes("KITTEH").isPresent());

        this.caseMapping = CaseMapping.RFC1459;
        Assertions.assertEquals("Kat[]", channel.getUser("KAT{}").orElseThrow().getNick());
    }

    /**
     * Tests channel user lookups with a Unicode casemapping.
     */
    @Test
    public void channelGetUserRfc8265() {
        this.caseMapping = CaseMapping.RFC8265;
        Channel channel = this.join("#ΣΟΦΊΑ", "Σοφία!sofia@kitteh.test");
        Assertions.assertEquals("Σοφία", channel.getUser("ΣΟΦΊΑ").orElseThrow().getNick());
        Assertions.assertEquals("Σοφία", channel.getUser("σοφία").orElseThrow().getNick());
        Assertions.assertEquals("#ΣΟΦΊΑ", this.tracker.getTrackedChannel("#σοφία").orElseThrow().getName());
    }
}
