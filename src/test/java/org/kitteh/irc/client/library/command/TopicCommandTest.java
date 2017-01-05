package org.kitteh.irc.client.library.command;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.feature.ServerInfo;
import org.mockito.Mockito;

/**
 * Tests TopicCommand.
 */
public class TopicCommandTest {
    private static final String CHANNEL = "#targetchannel";
    private static final String TOPIC = "Meow meow meow!";

    private Client client;

    /**
     * And then Kitteh said, let there be test!
     */
    @Before
    public void before() {
        this.client = Mockito.mock(Client.class);
        ServerInfo serverInfo = Mockito.mock(ServerInfo.class);
        Mockito.when(this.client.getServerInfo()).thenReturn(serverInfo);
        Mockito.when(serverInfo.isValidChannel(Mockito.any())).thenReturn(true);
    }

    /**
     * Tests a simple topic check.
     */
    @Test
    public void testNoNew() {
        TopicCommand topicCommand = new TopicCommand(client, CHANNEL);
        topicCommand.execute();

        Mockito.verify(client, Mockito.times(1)).sendRawLine("TOPIC " + CHANNEL);
    }

    /**
     * Tests a simple topic check after a topic's been removed.
     */
    @Test
    public void testNoNewAnymore() {
        TopicCommand topicCommand = new TopicCommand(client, CHANNEL);
        topicCommand.topic(TOPIC);
        topicCommand.query();
        topicCommand.execute();

        Mockito.verify(client, Mockito.times(1)).sendRawLine("TOPIC " + CHANNEL);
    }

    /**
     * Tests a simple topic check after a topic's been removed.
     */
    @Test
    public void testNoNewAnymoreNull() {
        TopicCommand topicCommand = new TopicCommand(client, CHANNEL);
        topicCommand.topic(TOPIC);
        topicCommand.topic(null);
        topicCommand.execute();

        Mockito.verify(client, Mockito.times(1)).sendRawLine("TOPIC " + CHANNEL);
    }

    /**
     * Tests a simple topic set's toString.
     */
    @Test
    public void testToString() {
        TopicCommand topicCommand = new TopicCommand(client, CHANNEL);
        topicCommand.topic(TOPIC);
        topicCommand.execute();

        Assert.assertTrue(topicCommand.toString().contains(TOPIC));
    }

    /**
     * Tests setting the topic.
     */
    @Test
    public void testSet() {
        TopicCommand topicCommand = new TopicCommand(client, CHANNEL);
        topicCommand.topic(TOPIC);
        topicCommand.execute();

        Mockito.verify(client, Mockito.times(1)).sendRawLine("TOPIC " + CHANNEL + " :" + TOPIC);
    }
}
