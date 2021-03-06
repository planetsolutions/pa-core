package ru.doccloud.document.repository;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import ru.doccloud.common.exception.DocumentNotFoundException;
import ru.doccloud.common.exception.TypeNotFoundException;
import ru.doccloud.document.IntegrationTestConstants;
import ru.doccloud.document.PersistenceContext;
import ru.doccloud.document.model.SystemDocument;
import ru.doccloud.repository.SystemRepository;

import java.util.List;
import java.util.UUID;

import static com.googlecode.catchexception.CatchException.catchException;
import static com.googlecode.catchexception.CatchException.caughtException;
import static org.assertj.core.api.Assertions.assertThat;
import static ru.doccloud.document.PageAssert.assertThatPage;
import static ru.doccloud.document.model.SystemAssert.assertThatSystemDocument;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {PersistenceContext.class})
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class })

public class ITSystemRepositoryTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ITSystemRepositoryTest.class);

    private static final int FIRST_PAGE = 0;
    private static final int SECOND_PAGE = 1;
    private static final int THIRD_PAGE = 2;
    private static final int PAGE_SIZE = 1;
    private static final int ONE_ELEMENT_ON_PAGE = 1;
    private static final long TWO_ELEMENTS = 2;
    private static final int TWO_PAGES = 2;
    private static final long ZERO_ELEMENTS = 0L;
    private static final int ZERO_ELEMENTS_ON_PAGE = 0;
    private static final int ZERO_PAGES = 0;

    @Autowired
    private SystemRepository<SystemDocument> repository;

    @Test
    @DatabaseSetup("/ru/doccloud/system/empty-system-data.xml")
    @ExpectedDatabase(value= "/ru/doccloud/system/system-data-add.xml", assertionMode = DatabaseAssertionMode.NON_STRICT)
    @DatabaseTearDown(value={"/ru/doccloud/system/system-data-add.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void add_ShouldAddNewSystemEntry() {
        SystemDocument  newDocumentEntry = SystemDocument.getBuilder(IntegrationTestConstants.NEW_TITLE)
                .description(IntegrationTestConstants.NEW_DESCRIPTION)
                .author(IntegrationTestConstants.NEW_AUTHOR)
                .build();

        SystemDocument  persistedDocumentEntry = repository.add(newDocumentEntry.getType(), newDocumentEntry);

        assertThatSystemDocument(persistedDocumentEntry)
                .hasId()
                .hasDescription(IntegrationTestConstants.NEW_DESCRIPTION)
                .hasTitle(IntegrationTestConstants.NEW_TITLE)
                .hasAuthor(IntegrationTestConstants.NEW_AUTHOR);

    }

    /**
     * see http://blog.codeleak.pl/2014/04/yet-another-way-to-handle-exceptions-in.html
     */
    @Test
    @DatabaseSetup("/ru/doccloud/system/system-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value = "/ru/doccloud/system/system-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/system/system-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void delete_SystemEntryNotFound_ShouldDeleteSystemDoc() {
        LOGGER.info("delete_DocumentEntryNotFound_ShouldDeleteDocument(): trying to throw exception");
        catchException(repository, DocumentNotFoundException.class).delete(null, UUID.fromString(IntegrationTestConstants.THIRD_UUID));
        assertThat( (DocumentNotFoundException)caughtException()).isExactlyInstanceOf(DocumentNotFoundException.class);
    }
//
    @Test
    @DatabaseSetup("/ru/doccloud/system/system-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value = "/ru/doccloud/system/system-data-deleted.xml")
    @DatabaseTearDown(value={"/ru/doccloud/system/system-data-deleted.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void delete_SystemEntryFound_ShouldDeleteSystemDoc() {
        SystemDocument  deletedSystemEntry = repository.delete(null, UUID.fromString(IntegrationTestConstants.FIRST_UUID));
        assertThatSystemDocument(deletedSystemEntry)
                .hasId(UUID.fromString(IntegrationTestConstants.FIRST_UUID))
                .hasDescription(IntegrationTestConstants.CURRENT_DESCRIPTION)
                .hasTitle(IntegrationTestConstants.CURRENT_TITLE_FIRST_DOCUMENT)
                .hasAuthor(IntegrationTestConstants.CURRENT_AUTHOR_DOCUMENT);
    }
//
    @Test
    @DatabaseSetup("/ru/doccloud/system/empty-system-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value = "/ru/doccloud/system/empty-system-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/system/empty-system-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findAll_NoSystemEntriesFound_ShouldReturnEmptyList() {
        List<SystemDocument> entries = repository.findAll();
        assertThat(entries).isEmpty();
    }
//
    @Test
    @DatabaseSetup("/ru/doccloud/system/system-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value = "/ru/doccloud/system/system-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/system/system-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findAll_TwoSystemEntriesFound_ShouldReturnTwoSystemEntries() {
        List<SystemDocument> entries = repository.findAll();

        assertThat(entries).hasSize(2);

        SystemDocument  firstSystemEntry = entries.get(0);
        assertThatSystemDocument(firstSystemEntry)
                .hasId(UUID.fromString(IntegrationTestConstants.FIRST_UUID))
                .hasDescription(IntegrationTestConstants.CURRENT_DESCRIPTION)
                .hasTitle(IntegrationTestConstants.CURRENT_TITLE_FIRST_DOCUMENT);

        SystemDocument  secondSystemEntry = entries.get(1);
        assertThatSystemDocument(secondSystemEntry)
                .hasId(UUID.fromString(IntegrationTestConstants.SECOND_UUID))
                .hasDescription(IntegrationTestConstants.CURRENT_DESCRIPTION)
                .hasTitle(IntegrationTestConstants.CURRENT_TITLE_SECOND_DOCUMENT);
    }

    @Test
    @DatabaseSetup("/ru/doccloud/system/system-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value = "/ru/doccloud/system/system-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/system/system-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findById_SystemEntryFound_ShouldReturnSystem() {
        SystemDocument  foundSystemEntry = repository.findById(null, UUID.fromString(IntegrationTestConstants.FIRST_UUID));

        assertThatSystemDocument(foundSystemEntry)
                .hasId(UUID.fromString(IntegrationTestConstants.FIRST_UUID))
                .hasDescription(IntegrationTestConstants.CURRENT_DESCRIPTION)
                .hasTitle(IntegrationTestConstants.CURRENT_TITLE_FIRST_DOCUMENT);
    }

    @Test
    @DatabaseSetup("/ru/doccloud/system/empty-system-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value = "/ru/doccloud/system/empty-system-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/system/empty-system-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findById_SystemEntryNotFound_ShouldThrowException() {
        catchException(repository, DocumentNotFoundException.class).findById(null, UUID.fromString(IntegrationTestConstants.FIRST_UUID));
        assertThat((DocumentNotFoundException) caughtException()).isExactlyInstanceOf(DocumentNotFoundException.class);
    }

    @Test
    @DatabaseSetup("/ru/doccloud/system/system-settings-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value = "/ru/doccloud/system/system-settings-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/system/system-settings-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findBySymbolicName_SystemEntryFound_ShouldReturnSystem() {
        SystemDocument  foundSystemEntry = repository.findBySymbolicName(IntegrationTestConstants.SYMBOLIC_NAME);

        assertThatSystemDocument(foundSystemEntry)
                .hasSymbolicName(IntegrationTestConstants.SYMBOLIC_NAME);
    }

    @Test
    @DatabaseSetup("/ru/doccloud/system/system-settings-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value = "/ru/doccloud/system/system-settings-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/system/system-settings-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findBySymbolicName_SystemEntryNotFound_ShouldThrowException() {
        catchException(repository, TypeNotFoundException.class).findBySymbolicName(IntegrationTestConstants.SYMBOLIC_NAME_NOT_EXIST);
        assertThat((TypeNotFoundException) caughtException()).isExactlyInstanceOf(TypeNotFoundException.class);
    }

    @Test
    @DatabaseSetup("/ru/doccloud/system/empty-system-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value = "/ru/doccloud/system/empty-system-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/system/empty-system-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findSettings_EmptySymbolicName_ShouldThrowException() {
        catchException(repository, DocumentNotFoundException.class).findBySymbolicName(null);
        assertThat((DocumentNotFoundException) caughtException()).isExactlyInstanceOf(DocumentNotFoundException.class);
    }

    @Test
    @DatabaseSetup("/ru/doccloud/system/system-settings-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value = "/ru/doccloud/system/system-settings-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/system/system-settings-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findSettings_SystemEntryFound_ShouldReturnSystem() {
        SystemDocument  foundSystemEntry = repository.findSettings(IntegrationTestConstants.SETTINGS_KEY_EXIST);

        assertThatSystemDocument(foundSystemEntry)
                .hasType(IntegrationTestConstants.SETTINGS_KEY_EXIST);
    }

    @Test
    @DatabaseSetup("/ru/doccloud/system/system-settings-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value = "/ru/doccloud/system/system-settings-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/system/system-settings-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findSettings_SystemEntryNotFound_ShouldThrowException() {
        catchException(repository, DocumentNotFoundException.class).findSettings(IntegrationTestConstants.SETTINGS_KEY_NOT_EXIST);
        assertThat((DocumentNotFoundException) caughtException()).isExactlyInstanceOf(DocumentNotFoundException.class);
    }

    @Test
    @DatabaseSetup("/ru/doccloud/system/empty-system-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value = "/ru/doccloud/system/empty-system-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/system/empty-system-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findSettings_EmptySettingsKey_ShouldThrowException() {
        catchException(repository, DocumentNotFoundException.class).findSettings(null);
        assertThat((DocumentNotFoundException) caughtException()).isExactlyInstanceOf(DocumentNotFoundException.class);
    }

    @Test
    @DatabaseSetup("/ru/doccloud/system/system-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value = "/ru/doccloud/system/system-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/system/system-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findByUUID_DocumentEntryFound_ShouldReturnDocument() {
        SystemDocument foundDocumentEntry = repository.findByUUID(IntegrationTestConstants.FIRST_UUID);

        assertThatSystemDocument(foundDocumentEntry)
                .hasId(UUID.fromString(IntegrationTestConstants.FIRST_UUID))
                .hasDescription(IntegrationTestConstants.CURRENT_DESCRIPTION)
                .hasTitle(IntegrationTestConstants.CURRENT_TITLE_FIRST_DOCUMENT);
    }

    @Test
    @DatabaseSetup("/ru/doccloud/system/system-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value = "/ru/doccloud/system/system-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/system/system-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findByUUID_DocumentEntryNotFound_ShouldThrowException() {
        catchException(repository, DocumentNotFoundException.class).findByUUID(IntegrationTestConstants.SECOND_UUID);
        assertThat((DocumentNotFoundException) caughtException()).isExactlyInstanceOf(DocumentNotFoundException.class);
    }

    @Test
    @DatabaseSetup("/ru/doccloud/system/system-parent-type-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value = "/ru/doccloud/system/system-parent-type-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/system/system-parent-type-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findAllByParentAndType_EmptyParentId_ShouldThrowException() {
        catchException(repository, DocumentNotFoundException.class).findAllByParentAndType(null, IntegrationTestConstants.TYPE, new PageRequest(FIRST_PAGE, PAGE_SIZE));
        assertThat((DocumentNotFoundException) caughtException()).isExactlyInstanceOf(DocumentNotFoundException.class);
    }

    @Test
    @DatabaseSetup("/ru/doccloud/system/system-parent-type-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value = "/ru/doccloud/system/system-parent-type-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/system/system-parent-type-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findAllByParentAndType_EmptyType_ShouldThrowException() {
        catchException(repository, DocumentNotFoundException.class).findAllByParentAndType(UUID.fromString(IntegrationTestConstants.PARENT_ID), null, new PageRequest(FIRST_PAGE, PAGE_SIZE));
        assertThat((DocumentNotFoundException) caughtException()).isExactlyInstanceOf(DocumentNotFoundException.class);
    }

    @Test
    @DatabaseSetup("/ru/doccloud/system/system-parent-type-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value = "/ru/doccloud/system/system-parent-type-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/system/system-parent-type-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findAllByParentAndType_FirstPageWithPageSizeOne_TwoDocumentEntriesExistAndSortedByTitleAsc_ShouldReturnFirstPageWithSecondDocumentEntry() {
        Sort sortSpecification = new Sort(new Sort.Order(Sort.Direction.ASC, IntegrationTestConstants.SORT_FIELD_TITLE));
        PageRequest pageSpecification = new PageRequest(FIRST_PAGE, PAGE_SIZE, sortSpecification);

        Page<SystemDocument> firstPage = repository.findAllByParentAndType(UUID.fromString(IntegrationTestConstants.PARENT_ID), IntegrationTestConstants.TYPE, pageSpecification);

        assertThatPage(firstPage)
                .hasPageNumber(FIRST_PAGE)
                .hasPageSize(PAGE_SIZE)
                .hasNumberOfElements(ONE_ELEMENT_ON_PAGE)
                .isFirstPage()
                .isNotLastPage()
                .hasTotalNumberOfElements(TWO_ELEMENTS)
                .hasTotalNumberOfPages(TWO_PAGES);

        SystemDocument foundDocumentEntry = firstPage.getContent().get(0);

        LOGGER.info("findBySearchTerm_FirstPageWithPageSizeOne: FirstPage SystemDocument {}", foundDocumentEntry);
        assertThatSystemDocument(foundDocumentEntry)
                .hasId(UUID.fromString(IntegrationTestConstants.SECOND_UUID))
                .hasTitle(IntegrationTestConstants.CURRENT_TITLE_SECOND_DOCUMENT);
    }

    @Test
    @DatabaseSetup("/ru/doccloud/system/empty-system-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value = "/ru/doccloud/system/empty-system-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/system/empty-system-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findBySearchTerm_DocumentEntriesNotFound_ShouldReturnPageWithoutElements() {
        Page<SystemDocument> firstPage = repository.findBySearchTerm(IntegrationTestConstants.SEARCH_TERM,
                new PageRequest(FIRST_PAGE, PAGE_SIZE)
        );

        assertThatPage(firstPage)
                .hasPageNumber(FIRST_PAGE)
                .hasPageSize(PAGE_SIZE)
                .isEmpty()
                .isFirstPage()
                .isLastPage()
                .hasTotalNumberOfElements(ZERO_ELEMENTS)
                .hasTotalNumberOfPages(ZERO_PAGES);
    }


    @Test
    @DatabaseSetup("/ru/doccloud/system/system-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value = "/ru/doccloud/system/system-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/system/system-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findBySearchTerm_FirstPageWithPageSizeOne_TwoDocumentEntriesExist_ShouldReturnFirstPageWithFirstDocumentEntry() {
        Page<SystemDocument> firstPage = repository.findBySearchTerm(IntegrationTestConstants.SEARCH_TERM,
                new PageRequest(FIRST_PAGE, PAGE_SIZE)
        );

        assertThatPage(firstPage)
                .hasPageNumber(FIRST_PAGE)
                .hasPageSize(PAGE_SIZE)
                .hasNumberOfElements(ONE_ELEMENT_ON_PAGE)
                .isFirstPage()
                .isNotLastPage()
                .hasTotalNumberOfElements(TWO_ELEMENTS)
                .hasTotalNumberOfPages(TWO_PAGES);

        SystemDocument foundDocumentEntry = firstPage.getContent().get(0);
        assertThatSystemDocument(foundDocumentEntry)
                .hasId(UUID.fromString(IntegrationTestConstants.FIRST_UUID))
                .hasDescription(IntegrationTestConstants.CURRENT_DESCRIPTION)
                .hasTitle(IntegrationTestConstants.CURRENT_TITLE_FIRST_DOCUMENT);
    }

    @Test
    @DatabaseSetup("/ru/doccloud/system/system-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value = "/ru/doccloud/system/system-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/system/system-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findBySearchTerm_FirstPageWithPageSizeOne_TwoDocumentEntriesExistAndSortedByTitleAsc_ShouldReturnFirstPageWithSecondDocumentEntry() {
        Sort sortSpecification = new Sort(new Sort.Order(Sort.Direction.ASC, IntegrationTestConstants.SORT_FIELD_TITLE));
        PageRequest pageSpecification = new PageRequest(FIRST_PAGE, PAGE_SIZE, sortSpecification);

        Page<SystemDocument> firstPage = repository.findBySearchTerm(IntegrationTestConstants.SEARCH_TERM, pageSpecification);

        assertThatPage(firstPage)
                .hasPageNumber(FIRST_PAGE)
                .hasPageSize(PAGE_SIZE)
                .hasNumberOfElements(ONE_ELEMENT_ON_PAGE)
                .isFirstPage()
                .isNotLastPage()
                .hasTotalNumberOfElements(TWO_ELEMENTS)
                .hasTotalNumberOfPages(TWO_PAGES);

        SystemDocument foundDocumentEntry = firstPage.getContent().get(0);

        assertThatSystemDocument(foundDocumentEntry)
                .hasId(UUID.fromString(IntegrationTestConstants.SECOND_UUID))
                .hasDescription(IntegrationTestConstants.CURRENT_DESCRIPTION)
                .hasTitle(IntegrationTestConstants.CURRENT_TITLE_SECOND_DOCUMENT);
    }

    @Test
    @DatabaseSetup("/ru/doccloud/system/system-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value = "/ru/doccloud/system/system-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/system/system-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findBySearchTerm_FirstPageWithPageSizeOne_TwoDocumentEntriesExistAndSortedByTitleDesc_ShouldReturnFirstPageWithFirstDocumentEntry() {
        Sort sortSpecification = new Sort(new Sort.Order(Sort.Direction.DESC, IntegrationTestConstants.SORT_FIELD_TITLE));
        PageRequest pageSpecification = new PageRequest(FIRST_PAGE, PAGE_SIZE, sortSpecification);

        Page<SystemDocument> firstPage = repository.findBySearchTerm(IntegrationTestConstants.SEARCH_TERM, pageSpecification);

        assertThatPage(firstPage)
                .hasPageNumber(FIRST_PAGE)
                .hasPageSize(PAGE_SIZE)
                .hasNumberOfElements(ONE_ELEMENT_ON_PAGE)
                .isFirstPage()
                .isNotLastPage()
                .hasTotalNumberOfElements(TWO_ELEMENTS)
                .hasTotalNumberOfPages(TWO_PAGES);

        SystemDocument foundDocumentEntry = firstPage.getContent().get(0);
        assertThatSystemDocument(foundDocumentEntry)
                .hasId(UUID.fromString(IntegrationTestConstants.FIRST_UUID))
                .hasDescription(IntegrationTestConstants.CURRENT_DESCRIPTION)
                .hasTitle(IntegrationTestConstants.CURRENT_TITLE_FIRST_DOCUMENT);
    }


    @Test
    @DatabaseSetup("/ru/doccloud/system/system-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value = "/ru/doccloud/system/system-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/system/system-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findBySearchTerm_SecondPageWithPageSizeOne_TwoDocumentEntriesExist_ShouldReturnSecondPageWithSecondDocumentEntry() {
        Page<SystemDocument> secondPage = repository.findBySearchTerm(IntegrationTestConstants.SEARCH_TERM,
                new PageRequest(SECOND_PAGE, PAGE_SIZE)
        );

        assertThatPage(secondPage)
                .hasPageNumber(SECOND_PAGE)
                .hasPageSize(PAGE_SIZE)
                .hasNumberOfElements(ONE_ELEMENT_ON_PAGE)
                .isNotFirstPage()
                .isLastPage()
                .hasTotalNumberOfElements(TWO_ELEMENTS)
                .hasTotalNumberOfPages(TWO_PAGES);

        SystemDocument foundDocumentEntry = secondPage.getContent().get(0);
        assertThatSystemDocument(foundDocumentEntry)
                .hasId(UUID.fromString(IntegrationTestConstants.SECOND_UUID))
                .hasDescription(IntegrationTestConstants.CURRENT_DESCRIPTION)
                .hasTitle(IntegrationTestConstants.CURRENT_TITLE_SECOND_DOCUMENT);
    }


    @Test
    @DatabaseSetup("/ru/doccloud/system/system-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value = "/ru/doccloud/system/system-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/system/system-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findBySearchTerm_SecondPageWithPageSizeOne_TwoDocumentEntriesExistAndSortedByTitleAsc_ShouldReturnSecondPageWithFirstDocumentEntry() {
        Sort sortSpecification = new Sort(new Sort.Order(Sort.Direction.ASC, IntegrationTestConstants.SORT_FIELD_TITLE));
        PageRequest pageSpecification = new PageRequest(SECOND_PAGE, PAGE_SIZE, sortSpecification);

        Page<SystemDocument> secondPage = repository.findBySearchTerm(IntegrationTestConstants.SEARCH_TERM, pageSpecification);

        assertThatPage(secondPage)
                .hasPageNumber(SECOND_PAGE)
                .hasPageSize(PAGE_SIZE)
                .hasNumberOfElements(ONE_ELEMENT_ON_PAGE)
                .isNotFirstPage()
                .isLastPage()
                .hasTotalNumberOfElements(TWO_ELEMENTS)
                .hasTotalNumberOfPages(TWO_PAGES);

        SystemDocument foundDocumentEntry = secondPage.getContent().get(0);

        LOGGER.info("findBySearchTerm_SecondPageWithPageSizeOne: secondPage SystemDocument {}", foundDocumentEntry);

        assertThatSystemDocument(foundDocumentEntry)
                .hasId(UUID.fromString(IntegrationTestConstants.FIRST_UUID))
                .hasDescription(IntegrationTestConstants.CURRENT_DESCRIPTION)
                .hasTitle(IntegrationTestConstants.CURRENT_TITLE_FIRST_DOCUMENT);
    }


    @Test
    @DatabaseSetup("/ru/doccloud/system/system-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value = "/ru/doccloud/system/system-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/system/system-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findAll_FirstPageWithPageSizeOne_TwoDocumentEntriesExistAndSortedByTitleDesc_ShouldReturnFirstPageWithFirstDocumentEntry() {
        Sort sortSpecification = new Sort(new Sort.Order(Sort.Direction.DESC, IntegrationTestConstants.SORT_FIELD_TITLE));
        PageRequest pageSpecification = new PageRequest(FIRST_PAGE, PAGE_SIZE, sortSpecification);

        Page<SystemDocument> firstPage = repository.findAll(pageSpecification, IntegrationTestConstants.JSON_QUERY);

        assertThatPage(firstPage)
                .hasPageNumber(FIRST_PAGE)
                .hasPageSize(PAGE_SIZE)
                .hasNumberOfElements(ONE_ELEMENT_ON_PAGE)
                .isFirstPage()
                .isNotLastPage()
                .hasTotalNumberOfElements(TWO_ELEMENTS)
                .hasTotalNumberOfPages(TWO_PAGES);

        SystemDocument foundDocumentEntry = firstPage.getContent().get(0);
        assertThatSystemDocument(foundDocumentEntry)
                .hasId(UUID.fromString(IntegrationTestConstants.FIRST_UUID))
                .hasAuthor(IntegrationTestConstants.CURRENT_AUTHOR_DOCUMENT);
    }

    @Test
    @DatabaseSetup("/ru/doccloud/system/system-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value = "/ru/doccloud/system/system-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/system/system-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findAll_SecondPageWithPageSizeOne_TwoDocumentEntriesExistAndSortedByTitleAsc_ShouldReturnSecondPageWithFirstDocumentEntry() {
        Sort sortSpecification = new Sort(new Sort.Order(Sort.Direction.ASC, IntegrationTestConstants.SORT_FIELD_TITLE));
        PageRequest pageSpecification = new PageRequest(SECOND_PAGE, PAGE_SIZE, sortSpecification);

        Page<SystemDocument> secondPage = repository.findAll(pageSpecification, IntegrationTestConstants.JSON_QUERY);

        assertThatPage(secondPage)
                .hasPageNumber(SECOND_PAGE)
                .hasPageSize(PAGE_SIZE)
                .hasNumberOfElements(ONE_ELEMENT_ON_PAGE)
                .isNotFirstPage()
                .isLastPage()
                .hasTotalNumberOfElements(TWO_ELEMENTS)
                .hasTotalNumberOfPages(TWO_PAGES);

        SystemDocument foundDocumentEntry = secondPage.getContent().get(0);

        LOGGER.info("findBySearchTerm_SecondPageWithPageSizeOne: secondPage SystemDocument {}", foundDocumentEntry);

        assertThatSystemDocument(foundDocumentEntry)
                .hasId(UUID.fromString(IntegrationTestConstants.FIRST_UUID))
                .hasAuthor(IntegrationTestConstants.CURRENT_AUTHOR_DOCUMENT);
    }

    @Test
    @DatabaseSetup("/ru/doccloud/system/system-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value = "/ru/doccloud/system/system-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/system/system-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findAllByType_FirstPageWithPageSizeOne_EmptyFields_TwoDocumentEntriesExistAndSortedByTitleDesc_ShouldReturnFirstPageWithFirstDocumentEntry() {
        Sort sortSpecification = new Sort(new Sort.Order(Sort.Direction.DESC, IntegrationTestConstants.SORT_FIELD_TYPE));
        PageRequest pageSpecification = new PageRequest(FIRST_PAGE, PAGE_SIZE, sortSpecification);

        Page<SystemDocument> firstPage = repository.findAllByType(
                IntegrationTestConstants.TYPE, null, pageSpecification,
                IntegrationTestConstants.JSON_QUERY, null);

        assertThatPage(firstPage)
                .hasPageNumber(FIRST_PAGE)
                .hasPageSize(PAGE_SIZE)
                .hasNumberOfElements(ONE_ELEMENT_ON_PAGE)
                .isFirstPage()
                .isNotLastPage()
                .hasTotalNumberOfElements(TWO_ELEMENTS)
                .hasTotalNumberOfPages(TWO_PAGES);

        SystemDocument foundDocumentEntry = firstPage.getContent().get(0);
        assertThatSystemDocument(foundDocumentEntry)
                .hasId(UUID.fromString(IntegrationTestConstants.FIRST_UUID))
                .hasAuthor(IntegrationTestConstants.CURRENT_AUTHOR_DOCUMENT)
                .hasType(IntegrationTestConstants.TYPE);
    }


    /**
     * there is an exception java.lang.NullPointerException: null
     at ru.doccloud.document.dbfactory.datatypes.JsonType.typeCast(JsonType.java:24)
     [main] org.springframework.test.context.TestContextManager: Caught exception while allowing TestExecutionListener [com.github.springtestdbunit.DbUnitTestExecutionListener@160396db] to process 'after'
     */


//    @Test
//    @DatabaseSetup("/ru/doccloud/system/system-data-json.xml")
//    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value = "/ru/doccloud/system/system-data-json.xml")
//    @DatabaseTearDown(value={"/ru/doccloud/system/system-data-json.xml"}, type= DatabaseOperation.DELETE_ALL)
//    public void findAllByType_FirstPageWithPageSizeOne_ArrFields_TwoDocumentEntriesExistAndSortedByTitleDesc_ShouldReturnFirstPageWithFirstDocumentEntry() {
//        Sort sortSpecification = new Sort(new Sort.Order(Sort.Direction.DESC, IntegrationTestConstants.SORT_FIELD_TYPE));
//        PageRequest pageSpecification = new PageRequest(FIRST_PAGE, PAGE_SIZE, sortSpecification);
//
//        Page<SystemDocument> firstPage = repository.findAllByType(
//                IntegrationTestConstants.TYPE, IntegrationTestConstants.FIELDS_ARR_TEST, pageSpecification,
//                IntegrationTestConstants.JSON_QUERY_TEST);
//
//        LOGGER.info("fisrstPage content {}", firstPage.getContent());
//
//        assertThatPage(firstPage)
//                .hasPageNumber(FIRST_PAGE)
//                .hasPageSize(PAGE_SIZE)
//                .hasNumberOfElements(ONE_ELEMENT_ON_PAGE)
//                .isFirstPage()
//                .isNotLastPage()
//                .hasTotalNumberOfElements(TWO_ELEMENTS)
//                .hasTotalNumberOfPages(TWO_PAGES);
//
//        SystemDocument foundDocumentEntry = firstPage.getContent().get(0);
//        assertThatSystemDocument(foundDocumentEntry)
//                .hasId(UUID.fromString(IntegrationTestConstants.FIRST_UUID))
//                .hasType(IntegrationTestConstants.TYPE);
//    }

    @Test
    @DatabaseSetup("/ru/doccloud/system/system-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value = "/ru/doccloud/system/system-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/system/system-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findAllByType_SecondPageWithPageSizeOne_EmptyFields_TwoDocumentEntriesExistAndSortedByTitleAsc_ShouldReturnSecondPageWithFirstDocumentEntry() {
        Sort sortSpecification = new Sort(new Sort.Order(Sort.Direction.ASC, IntegrationTestConstants.SORT_FIELD_TITLE));
        PageRequest pageSpecification = new PageRequest(SECOND_PAGE, PAGE_SIZE, sortSpecification);

        Page<SystemDocument> secondPage = repository.findAllByType(
                IntegrationTestConstants.TYPE, null, pageSpecification,
                IntegrationTestConstants.JSON_QUERY, null);

        assertThatPage(secondPage)
                .hasPageNumber(SECOND_PAGE)
                .hasPageSize(PAGE_SIZE)
                .hasNumberOfElements(ONE_ELEMENT_ON_PAGE)
                .isNotFirstPage()
                .isLastPage()
                .hasTotalNumberOfElements(TWO_ELEMENTS)
                .hasTotalNumberOfPages(TWO_PAGES);

        SystemDocument foundDocumentEntry = secondPage.getContent().get(0);

        LOGGER.info("findBySearchTerm_SecondPageWithPageSizeOne: secondPage SystemDocument {}", foundDocumentEntry);

        assertThatSystemDocument(foundDocumentEntry)
                .hasId(UUID.fromString(IntegrationTestConstants.FIRST_UUID))
                .hasTitle(IntegrationTestConstants.CURRENT_TITLE_FIRST_DOCUMENT);
    }

    /**
     * there is an exception java.lang.NullPointerException: null
     at ru.doccloud.document.dbfactory.datatypes.JsonType.typeCast(JsonType.java:24)
        [main] org.springframework.test.context.TestContextManager: Caught exception while allowing TestExecutionListener [com.github.springtestdbunit.DbUnitTestExecutionListener@160396db] to process 'after'
     */

//    @Test
//    @DatabaseSetup("/ru/doccloud/system/system-data-json.xml")
//    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value = "/ru/doccloud/system/system-data-json.xml")
//    @DatabaseTearDown(value={"/ru/doccloud/system/system-data-json.xml"}, type= DatabaseOperation.DELETE_ALL)
//    public void findAllByType_SecondPageWithPageSizeOne_ArrFields_TwoDocumentEntriesExistAndSortedByTitleAsc_ShouldReturnSecondPageWithFirstDocumentEntry() {
//        Sort sortSpecification = new Sort(new Sort.Order(Sort.Direction.ASC, IntegrationTestConstants.SORT_FIELD_TITLE));
//        PageRequest pageSpecification = new PageRequest(SECOND_PAGE, PAGE_SIZE, sortSpecification);
//
//        Page<SystemDocument> secondPage = repository.findAllByType(
//                IntegrationTestConstants.TYPE, IntegrationTestConstants.FIELDS_ARR_TEST, pageSpecification,
//                IntegrationTestConstants.JSON_QUERY_TEST);
//
//        LOGGER.info("secondPage content {}", secondPage.getContent());
//
//        assertThatPage(secondPage)
//                .hasPageNumber(SECOND_PAGE)
//                .hasPageSize(PAGE_SIZE)
//                .hasNumberOfElements(ONE_ELEMENT_ON_PAGE)
//                .isNotFirstPage()
//                .isLastPage()
//                .hasTotalNumberOfElements(TWO_ELEMENTS)
//                .hasTotalNumberOfPages(TWO_PAGES);
//
//        SystemDocument foundDocumentEntry = secondPage.getContent().get(0);
//
//        LOGGER.info("findBySearchTerm_SecondPageWithPageSizeOne: secondPage SystemDocument {}", foundDocumentEntry);
//
//        assertThatSystemDocument(foundDocumentEntry)
//                .hasId(UUID.fromString(IntegrationTestConstants.FIRST_UUID));
//    }


    @Test
    @DatabaseSetup("/ru/doccloud/system/system-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value = "/ru/doccloud/system/system-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/system/system-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findBySearchTerm_SecondPageWithPageSizeOne_TwoDocumentEntriesExistAndSortedByTitleDesc_ShouldReturnSecondPageWithSecondDocumentEntry() {
        Sort sortSpecification = new Sort(new Sort.Order(Sort.Direction.DESC, IntegrationTestConstants.SORT_FIELD_TITLE));
        PageRequest pageSpecification = new PageRequest(1, 1, sortSpecification);

        Page<SystemDocument> secondPage = repository.findBySearchTerm(IntegrationTestConstants.SEARCH_TERM, pageSpecification);

        assertThatPage(secondPage)
                .hasPageNumber(SECOND_PAGE)
                .hasPageSize(PAGE_SIZE)
                .hasNumberOfElements(ONE_ELEMENT_ON_PAGE)
                .isNotFirstPage()
                .isLastPage()
                .hasTotalNumberOfElements(TWO_ELEMENTS)
                .hasTotalNumberOfPages(TWO_PAGES);

        SystemDocument foundDocumentEntry = secondPage.getContent().get(0);
        assertThatSystemDocument(foundDocumentEntry)
                .hasId(UUID.fromString(IntegrationTestConstants.SECOND_UUID))
                .hasTitle(IntegrationTestConstants.CURRENT_TITLE_SECOND_DOCUMENT);
    }

    @Test
    @DatabaseSetup("/ru/doccloud/system/system-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value = "/ru/doccloud/system/system-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/system/system-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findBySearchTerm_ThirdPageWithPageSizeOne_TwoDocumentEntriesExist_ShouldReturnPageWithEmptyList() {
        Page<SystemDocument> thirdPage = repository.findBySearchTerm(IntegrationTestConstants.SEARCH_TERM, new PageRequest(2, 1));

        assertThatPage(thirdPage)
                .hasPageNumber(THIRD_PAGE)
                .hasPageSize(PAGE_SIZE)
                .hasNumberOfElements(ZERO_ELEMENTS_ON_PAGE)
                .isNotFirstPage()
                .isLastPage()
                .hasTotalNumberOfElements(TWO_ELEMENTS)
                .hasTotalNumberOfPages(TWO_PAGES);
    }

    @Test
    @DatabaseSetup("/ru/doccloud/system/system-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value = "/ru/doccloud/system/system-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/system/system-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findBySearchTerm_ThirdPageWithPageSizeOne_TwoDocumentEntriesExistAndSortedByTitleAsc_ShouldReturnPageWithEmptyList() {
        Sort sortSpecification = new Sort(new Sort.Order(Sort.Direction.ASC, IntegrationTestConstants.SORT_FIELD_TITLE));
        PageRequest pageSpecification = new PageRequest(2, 1, sortSpecification);

        Page<SystemDocument> thirdPage = repository.findBySearchTerm(IntegrationTestConstants.SEARCH_TERM, pageSpecification);

        assertThatPage(thirdPage)
                .hasPageNumber(THIRD_PAGE)
                .hasPageSize(PAGE_SIZE)
                .hasNumberOfElements(ZERO_ELEMENTS_ON_PAGE)
                .isNotFirstPage()
                .isLastPage()
                .hasTotalNumberOfElements(TWO_ELEMENTS)
                .hasTotalNumberOfPages(TWO_PAGES);
    }

    @Test
    @DatabaseSetup("/ru/doccloud/system/system-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value = "/ru/doccloud/system/system-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/system/system-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findBySearchTerm_ThirdPageWithPageSizeOne_TwoDocumentEntriesExistAndSortedByTitleDesc_ShouldReturnPageWithEmptyList() {
        Sort sortSpecification = new Sort(new Sort.Order(Sort.Direction.DESC, IntegrationTestConstants.SORT_FIELD_TITLE));
        PageRequest pageSpecification = new PageRequest(2, 1, sortSpecification);

        Page<SystemDocument> thirdPage = repository.findBySearchTerm(IntegrationTestConstants.SEARCH_TERM, pageSpecification);

        assertThatPage(thirdPage)
                .hasPageNumber(THIRD_PAGE)
                .hasPageSize(PAGE_SIZE)
                .hasNumberOfElements(ZERO_ELEMENTS_ON_PAGE)
                .isNotFirstPage()
                .isLastPage()
                .hasTotalNumberOfElements(TWO_ELEMENTS)
                .hasTotalNumberOfPages(TWO_PAGES);
    }

    @Test
    @DatabaseSetup("/ru/doccloud/system/empty-system-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value = "/ru/doccloud/system/empty-system-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/system/empty-system-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void update_DocumentEntryNotFound_ShouldThrowException() {
        SystemDocument updatedDocumentEntry = SystemDocument.getBuilder("title")
                .description("description")
                .id(UUID.fromString(IntegrationTestConstants.SECOND_UUID))
                .build();

        catchException(repository, DocumentNotFoundException.class).update(null, updatedDocumentEntry);
        assertThat((DocumentNotFoundException) caughtException()).isExactlyInstanceOf(DocumentNotFoundException.class);
    }

    @Test
    @DatabaseSetup("/ru/doccloud/system/system-data.xml")
    @ExpectedDatabase(value= "/ru/doccloud/system/system-data-updated.xml", assertionMode = DatabaseAssertionMode.NON_STRICT)
    @DatabaseTearDown(value={"/ru/doccloud/system/system-data-updated.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void update_DocumentEntryFound_ShouldUpdateDocument() {
        SystemDocument updatedDocumentEntry = SystemDocument.getBuilder(IntegrationTestConstants.NEW_TITLE)
                .description(IntegrationTestConstants.NEW_DESCRIPTION)
                .id(UUID.fromString(IntegrationTestConstants.SECOND_UUID))
                .build();

        SystemDocument returnedDocumentEntry = repository.update(null, updatedDocumentEntry);

        assertThatSystemDocument(returnedDocumentEntry)
                .hasId(UUID.fromString(IntegrationTestConstants.SECOND_UUID))
                .hasDescription(IntegrationTestConstants.NEW_DESCRIPTION)
                .hasTitle(IntegrationTestConstants.NEW_TITLE);
    }

//    @Test
//    @DatabaseSetup("/ru/doccloud/system/system-parent-data.xml")
//    @ExpectedDatabase(value= "/ru/doccloud/system/system-parent-data-updated.xml", assertionMode = DatabaseAssertionMode.NON_STRICT)
//    public void setParent_DocumentEntryFound_ShouldSetParentDocument() {
//        SystemDocument parentdDocumentEntry = SystemDocument.getBuilder(IntegrationTestConstants.NEW_TITLE)
//                .description(IntegrationTestConstants.NEW_DESCRIPTION)
//                .id(UUID.fromString(IntegrationTestConstants.THIRD_UUID))
//                .parent(String.valueOf(UUID.fromString(IntegrationTestConstants.SECOND_UUID)))
//                .build();
//
//        SystemDocument returnedDocumentEntry = repository.setParent(parentdDocumentEntry);
//
//        assertThatSystemDocument(returnedDocumentEntry)
//                .hasId(UUID.fromString(IntegrationTestConstants.THIRD_UUID))
//                .hasParent(String.valueOf(IntegrationTestConstants.ID_SECOND_DOCUMENT));
//    }

    @Test
    @DatabaseSetup("/ru/doccloud/system/system-fileinfo-data.xml")
    @ExpectedDatabase(value= "/ru/doccloud/system/system-fileinfo-data-updated.xml", assertionMode = DatabaseAssertionMode.NON_STRICT)
    @DatabaseTearDown(value={"/ru/doccloud/system/system-fileinfo-data-updated.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void updateFileInfo_DocumentEntryFound_ShouldUpdateDocument() {
        SystemDocument updatedDocumentEntry = SystemDocument.getBuilder(IntegrationTestConstants.CURRENT_TITLE_FIRST_DOCUMENT)
                .id(UUID.fromString(IntegrationTestConstants.FIRST_UUID))
                .modifier(IntegrationTestConstants.MODIFIER_NEW)
                .fileLength(IntegrationTestConstants.FILE_LENGHT_NEW)
                .fileMimeType(IntegrationTestConstants.MIME_TYPE_NEW)
                .fileName(IntegrationTestConstants.FILE_NAME_NEW)
                .filePath(IntegrationTestConstants.FILEPATH_NEW)
                .build();

        SystemDocument returnedDocumentEntry = repository.updateFileInfo(null, updatedDocumentEntry);

        assertThatSystemDocument(returnedDocumentEntry)
                .hasId(UUID.fromString(IntegrationTestConstants.FIRST_UUID))
                .hasFileLength(IntegrationTestConstants.FILE_LENGHT_NEW)
                .hasFileName(IntegrationTestConstants.FILE_NAME_NEW)
                .hasFilePath(IntegrationTestConstants.FILEPATH_NEW)
                .hasMimeType(IntegrationTestConstants.MIME_TYPE_NEW)
                .hasModifier(IntegrationTestConstants.MODIFIER_NEW);
    }
}
