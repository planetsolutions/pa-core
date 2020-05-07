package ru.doccloud.service;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import ru.doccloud.repository.DocumentRepositoryTestIT;

@RunWith(Suite.class)

@Suite.SuiteClasses(
        {
                DocumentServiceTestIT.class
        }
)
public class ServiceTestITSuite {
}
