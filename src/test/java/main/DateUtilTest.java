package main;

import org.junit.Assert;
import org.junit.Test;

public class DateUtilTest {

    @Test(timeout = 200)
    public void testSimpleDates() {
        Assert.assertEquals("yyyyMMdd", DateUtil.determineDateFormat("20201010"));

        Assert.assertEquals("dd.MM.yyyy", DateUtil.determineDateFormat("1.2.1900"));
        Assert.assertEquals("dd.MM.yyyy", DateUtil.determineDateFormat("01.02.1900"));

        Assert.assertEquals("dd/MM/yyyy", DateUtil.determineDateFormat("1/2/1900"));
        Assert.assertEquals("dd/MM/yyyy", DateUtil.determineDateFormat("01/02/1900"));

        Assert.assertEquals("dd-MM-yyyy", DateUtil.determineDateFormat("01-02-1900"));
        Assert.assertEquals("dd-MM-yyyy", DateUtil.determineDateFormat("1-2-1900"));

        Assert.assertEquals("dd MM yyyy", DateUtil.determineDateFormat("1 2 1900"));
        Assert.assertEquals("dd MM yyyy", DateUtil.determineDateFormat("01 02 1900"));

        Assert.assertEquals("yyyyMMddHHmm", DateUtil.determineDateFormat("202010101020"));
        Assert.assertEquals("yyyyMMdd HHmm", DateUtil.determineDateFormat("20201010 1020"));

        Assert.assertEquals("dd.MM.yyyy HH:mm", DateUtil.determineDateFormat("1.2.1900"));
        Assert.assertEquals("dd.MM.yyyy HH:mm", DateUtil.determineDateFormat("01.02.1900"));

        Assert.assertEquals("dd/MM/yyyy HH", DateUtil.determineDateFormat("1/2/1900"));
        Assert.assertEquals("dd/MM/yyyy HH", DateUtil.determineDateFormat("01/02/1900"));

        Assert.assertEquals("dd-MM-yyyy", DateUtil.determineDateFormat("01-02-1900"));
        Assert.assertEquals("dd-MM-yyyy", DateUtil.determineDateFormat("1-2-1900"));

        Assert.assertEquals("dd MM yyyy", DateUtil.determineDateFormat("1 2 1900"));
        Assert.assertEquals("dd MM yyyy", DateUtil.determineDateFormat("01 02 1900"));

    }

}
