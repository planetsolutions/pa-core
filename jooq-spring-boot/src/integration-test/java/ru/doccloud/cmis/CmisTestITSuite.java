package ru.doccloud.cmis;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)

@Suite.SuiteClasses(
        {
                CmisGetObjectByPathTestIT.class,
                CmisGetObjectTestIT.class
        }
)
public class CmisTestITSuite {
}
