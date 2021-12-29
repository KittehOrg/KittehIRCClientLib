package org.kitteh.irc.client.library.feature.twitch.messagetag;

import org.kitteh.irc.client.library.Client;
import org.mockito.Mockito;
import org.junit.Assert;
import org.junit.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EmoteSetsTest{
	private static final String NAME = "emote-sets";
	
	/**
	 * Verifies integer format is accepted.
	 */
	@Test
	public void verifyInteger(){
		Client client = Mockito.mock(Client.class);
		EmoteSets tested = EmoteSets.FUNCTION.apply(client, NAME, "123,456,789");
		
		List<String> expected = Arrays.asList("123", "456", "789");
		Assert.assertEquals(expected, tested.getEmoteSets());
	}
	
	/**
	 * Verifies UUID format is accepted.
	 */
	@Test
	public void verifyUUID(){
		Client client = Mockito.mock(Client.class);
		EmoteSets tested = EmoteSets.FUNCTION.apply(client, NAME, "fb70df85-0e31-41ea-a13f-c3201bac7013,1a313266-b8e1-49c2-9409-68526a85a350");
		
		List<String> expected = Arrays.asList("fb70df85-0e31-41ea-a13f-c3201bac7013", "1a313266-b8e1-49c2-9409-68526a85a350");
		Assert.assertEquals(expected, tested.getEmoteSets());
	}
	
	/**
	 * Verifies null values returns an empty list.
	 */
	@Test
	public void verifyNull(){
		Client client = Mockito.mock(Client.class);
		EmoteSets tested = EmoteSets.FUNCTION.apply(client, NAME, null);
		
		Assert.assertEquals(Collections.emptyList(), tested.getEmoteSets());
	}
}