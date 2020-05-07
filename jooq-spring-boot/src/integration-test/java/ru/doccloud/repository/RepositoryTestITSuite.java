package ru.doccloud.repository;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import ru.doccloud.cmis.CmisGetObjectByPathTestIT;
import ru.doccloud.cmis.CmisGetObjectTestIT;

@RunWith(Suite.class)

@Suite.SuiteClasses(
        {
                DocumentRepositoryTestIT.class
        }
)
public class RepositoryTestITSuite {
}
