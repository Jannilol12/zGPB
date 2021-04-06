package main;

import org.junit.Assert;
import org.junit.Test;

public class UtilTest {

    @Test
    public void isValidDiscordID_message() {
        Assert.assertTrue(Util.isValidDiscordID("821102479319367790"));
    }

    @Test
    public void isValidDiscordID_channel() {
        Assert.assertTrue(Util.isValidDiscordID("765695967131205637"));
    }

    @Test
    public void isValidDiscordID_guild() {
        Assert.assertTrue(Util.isValidDiscordID("765695966450942014"));
    }

    @Test
    public void isValidDiscordID_string() {
        Assert.assertFalse(Util.isValidDiscordID("a"));
    }

    @Test
    public void isValidDiscordID_null() {
        Assert.assertFalse(Util.isValidDiscordID(null));
    }

    @Test
    public void isValidDiscordID_empty() {
        Assert.assertFalse(Util.isValidDiscordID(""));
    }

}
