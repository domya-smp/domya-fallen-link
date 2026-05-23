package ru.nyansus.mc.fallenlink;

import org.junit.Assert;
import org.junit.Test;

public final class JsonWriterTest {

    @Test
    public void quoteEscapesJsonControlCharacters() {
        Assert.assertEquals("\"a\\\"b\\\\c\\nd\"", JsonWriter.quote("a\"b\\c\nd"));
    }

    @Test
    public void quoteConvertsNullToEmptyString() {
        Assert.assertEquals("\"\"", JsonWriter.quote(null));
    }
}
