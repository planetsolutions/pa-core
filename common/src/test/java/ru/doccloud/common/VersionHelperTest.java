package ru.doccloud.common;

import org.junit.Test;
import ru.doccloud.common.util.VersionHelper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class VersionHelperTest {

    private static final String START_MINOR_VERSION_VALUE = "0.1";
    private static final double MINOR_VERSION_LAG = 0.1;

    @Test
    public void generateMinorDocVersionTestWithBlankOLdVersion_thenStartMinorVersion()  {

        final String newMinorVersion = VersionHelper.generateMinorDocVersion("");

        assertNotNull(newMinorVersion);
        assertEquals(START_MINOR_VERSION_VALUE, newMinorVersion);
    }

    @Test
    public void generateMinorDocVersionTestWith02_then03()  {

        final String newMinorVersion = VersionHelper.generateMinorDocVersion("0.2");

        assertNotNull(newMinorVersion);
        assertEquals("0.3", newMinorVersion);
    }

    @Test
    public void generateMinorDocVersionTestWith09_then10()  {

        final String newMinorVersion = VersionHelper.generateMinorDocVersion("0.9");

        assertNotNull(newMinorVersion);
        assertEquals("1.0", newMinorVersion);
    }



}
