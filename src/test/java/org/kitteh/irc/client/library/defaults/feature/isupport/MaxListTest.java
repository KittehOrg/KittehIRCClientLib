package org.kitteh.irc.client.library.defaults.feature.isupport;

import org.junit.Assert;
import org.junit.Test;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.defaults.element.isupport.DefaultISupportMaxList;
import org.kitteh.irc.client.library.element.ISupportParameter;
import org.mockito.Mockito;

import java.util.Optional;

public class MaxListTest {
    @Test
    public void testMaxList() {
        ISupportParameter.MaxList maxList = new DefaultISupportMaxList(Mockito.mock(Client.class), "MAXLIST", "b:60,e:60,I:60");
        Assert.assertEquals("b should be 60", 60, maxList.getLimit('b'));
        Assert.assertEquals("q should be -1", -1, maxList.getLimit('q'));
        Assert.assertEquals("e should be 60", 60, maxList.getLimit('e'));
        Assert.assertEquals("I should be 60", 60, maxList.getLimit('I'));
        Assert.assertEquals("Should be 3 limit data", 3, maxList.getAllLimitData().size());
        Optional<ISupportParameter.MaxList.LimitData> bDataOpt = maxList.getLimitData('b');
        Assert.assertTrue("b data should be present", bDataOpt.isPresent());
        Assert.assertFalse("q data should not be present", maxList.getLimitData('q').isPresent());
        ISupportParameter.MaxList.LimitData bData = bDataOpt.get();
        Assert.assertEquals("b via data should be 60", 60, bData.getLimit());
        Assert.assertEquals("Should be one char in b data", 1, bData.getModes().size());
        Assert.assertTrue("b should be in b data", bData.getModes().contains('b'));
    }

    @Test
    public void testMaxListMerged() {
        ISupportParameter.MaxList maxList = new DefaultISupportMaxList(Mockito.mock(Client.class), "MAXLIST", "bqeI:100");
        Assert.assertEquals("b should be 100", 100, maxList.getLimit('b'));
        Assert.assertEquals("q should be 100", 100, maxList.getLimit('q'));
        Assert.assertEquals("e should be 100", 100, maxList.getLimit('e'));
        Assert.assertEquals("I should be 100", 100, maxList.getLimit('I'));
        Assert.assertEquals("Should be 1 limit data", 1, maxList.getAllLimitData().size());
        Optional<ISupportParameter.MaxList.LimitData> bDataOpt = maxList.getLimitData('b');
        Assert.assertTrue("bData should be present", bDataOpt.isPresent());
        ISupportParameter.MaxList.LimitData bData = bDataOpt.get();
        Assert.assertEquals("b via data should be 60", 100, bData.getLimit());
        Assert.assertEquals("Should be 4 chars in b data", 4, bData.getModes().size());
        Assert.assertTrue("b should be in b data", bData.getModes().contains('b'));
    }
}
