package org.kitteh.irc.client.library.defaults.feature.isupport;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.defaults.element.isupport.DefaultISupportMaxList;
import org.kitteh.irc.client.library.element.ISupportParameter;
import org.mockito.Mockito;

import java.util.Optional;

public class MaxListTest {
    @Test
    public void testMaxList() {
        ISupportParameter.MaxList maxList = new DefaultISupportMaxList(Mockito.mock(Client.class), "MAXLIST", "b:60,e:60,I:60");
        Assertions.assertEquals(60, maxList.getLimit('b'), "b should be 60");
        Assertions.assertEquals(-1, maxList.getLimit('q'), "q should be -1");
        Assertions.assertEquals(60, maxList.getLimit('e'), "e should be 60");
        Assertions.assertEquals(60, maxList.getLimit('I'), "I should be 60");
        Assertions.assertEquals(3, maxList.getAllLimitData().size(), "Should be 3 limit data");
        Optional<ISupportParameter.MaxList.LimitData> bDataOpt = maxList.getLimitData('b');
        Assertions.assertTrue(bDataOpt.isPresent(), "b data should be present");
        Assertions.assertFalse(maxList.getLimitData('q').isPresent(), "q data should not be present");
        ISupportParameter.MaxList.LimitData bData = bDataOpt.get();
        Assertions.assertEquals(60, bData.getLimit(), "b via data should be 60");
        Assertions.assertEquals(1, bData.getModes().size(), "Should be one char in b data");
        Assertions.assertTrue(bData.getModes().contains('b'), "b should be in b data");
    }

    @Test
    public void testMaxListMerged() {
        ISupportParameter.MaxList maxList = new DefaultISupportMaxList(Mockito.mock(Client.class), "MAXLIST", "bqeI:100");
        Assertions.assertEquals(100, maxList.getLimit('b'), "b should be 100");
        Assertions.assertEquals(100, maxList.getLimit('q'), "q should be 100");
        Assertions.assertEquals(100, maxList.getLimit('e'), "e should be 100");
        Assertions.assertEquals(100, maxList.getLimit('I'), "I should be 100");
        Assertions.assertEquals(1, maxList.getAllLimitData().size(), "Should be 1 limit data");
        Optional<ISupportParameter.MaxList.LimitData> bDataOpt = maxList.getLimitData('b');
        Assertions.assertTrue(bDataOpt.isPresent(), "bData should be present");
        ISupportParameter.MaxList.LimitData bData = bDataOpt.get();
        Assertions.assertEquals(100, bData.getLimit(), "b via data should be 60");
        Assertions.assertEquals(4, bData.getModes().size(), "Should be 4 chars in b data");
        Assertions.assertTrue(bData.getModes().contains('b'), "b should be in b data");
    }
}
