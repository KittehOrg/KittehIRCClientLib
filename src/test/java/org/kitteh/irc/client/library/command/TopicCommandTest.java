package org.kitteh.irc.client.library.command;

import org.junit.Assert;
import org.junit.Test;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.Channel;
import org.mockito.Mockito;

import java.util.Optional;

/**
 * Tests TopicCommand.
 */
public class TopicCommandTest {
    private static final String CHANNEL = "#targetchannel";
    private static final String TOPIC = "Meow meow meow!";

    /**
     * Tests a simple topic check.
     */
    @Test
    public void testNoNew() {
        Client client = Mockito.mock(Client.class);
        Mockito.when(client.getChannel(CHANNEL)).thenReturn(Optional.of(Mockito.mock(Channel.class)));

        TopicCommand topicCommand = new TopicCommand(client, CHANNEL);
        topicCommand.execute();

        Mockito.verify(client, Mockito.times(1)).sendRawLine("TOPIC " + CHANNEL);
    }

    /**
     * Tests a simple topic check after a topic's been removed.
     */
    @Test
    public void testNoNewAnymore() {
        Client client = Mockito.mock(Client.class);
        Mockito.when(client.getChannel(CHANNEL)).thenReturn(Optional.of(Mockito.mock(Channel.class)));

        TopicCommand topicCommand = new TopicCommand(client, CHANNEL);
        topicCommand.topic(TOPIC);
        topicCommand.query();
        topicCommand.execute();

        Mockito.verify(client, Mockito.times(1)).sendRawLine("TOPIC " + CHANNEL);
    }

    /**
     * Tests a simple topic set's toString.
     */
    @Test
    public void testToString() {
        Client client = Mockito.mock(Client.class);
        Mockito.when(client.getChannel(CHANNEL)).thenReturn(Optional.of(Mockito.mock(Channel.class)));

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
        Client client = Mockito.mock(Client.class);
        Mockito.when(client.getChannel(CHANNEL)).thenReturn(Optional.of(Mockito.mock(Channel.class)));

        TopicCommand topicCommand = new TopicCommand(client, CHANNEL);
        topicCommand.topic(TOPIC);
        topicCommand.execute();

        Mockito.verify(client, Mockito.times(1)).sendRawLine("TOPIC " + CHANNEL + " :" + TOPIC);
    }
}
