package configuration;

import org.junit.Assert;
import org.junit.Test;

public class GuildConfigurationHandlerTest {

    @Test
    public void checkValue_boolean_true() {
        GuildConfigurationHandler gch = new GuildConfigurationHandler();
        Assert.assertTrue(gch.checkValue("logging_enabled", "true"));
    }

    @Test
    public void checkValue_boolean_false() {
        GuildConfigurationHandler gch = new GuildConfigurationHandler();
        Assert.assertTrue(gch.checkValue("logging_enabled", "false"));
    }

    @Test
    public void checkValue_boolean_string() {
        GuildConfigurationHandler gch = new GuildConfigurationHandler();
        Assert.assertFalse(gch.checkValue("logging_enabled", "heltruelo"));
    }

    @Test
    public void checkValue_text() {
        GuildConfigurationHandler gch = new GuildConfigurationHandler();
        Assert.assertTrue(gch.checkValue("fix_role_add", "0123456789012345678901234567890123456789" +
                                                         "0123456789012345678901234567890123456789" +
                                                         "01234567890123456789"));
    }

    @Test
    public void checkValue_text_too_long() {
        GuildConfigurationHandler gch = new GuildConfigurationHandler();
        Assert.assertFalse(gch.checkValue("fix_role_add", "0123456789012345678901234567890123456789" +
                                                          "0123456789012345678901234567890123456789" +
                                                          "012345678901234567891"));
    }

    @Test
    public void checkValue_number() {
        GuildConfigurationHandler gch = new GuildConfigurationHandler();
        Assert.assertTrue(gch.checkValue("temporary_channel_max", "5"));
    }

    @Test
    public void checkValue_number_negative() {
        GuildConfigurationHandler gch = new GuildConfigurationHandler();
        Assert.assertFalse(gch.checkValue("temporary_channel_max", "-1"));
    }

    @Test
    public void checkValue_number_too_long() {
        GuildConfigurationHandler gch = new GuildConfigurationHandler();
        Assert.assertFalse(gch.checkValue("temporary_channel_max", "1000000000000000000000000000000"));
    }

    @Test
    public void checkValue_number_string() {
        GuildConfigurationHandler gch = new GuildConfigurationHandler();
        Assert.assertFalse(gch.checkValue("temporary_channel_max", "a"));
    }

}
