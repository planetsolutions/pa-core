package ru.doccloud.cmis;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)

@Suite.SuiteClasses(
        {
                CmisGetObjectByPathTest.class,
                CmisGetObjectTest.class
        }
)
public class CmisTestSuite {
}
