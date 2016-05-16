package info.jallaix.common.language.dao;

import com.github.tlrx.elasticsearch.test.EsSetup;
import com.github.tlrx.elasticsearch.test.EsSetupRuntimeException;
import com.github.tlrx.elasticsearch.test.provider.JSONProvider;
import com.github.tlrx.elasticsearch.test.request.CreateIndex;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.elasticsearch.client.Client;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.test.context.ContextConfiguration;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static com.github.tlrx.elasticsearch.test.EsSetup.*;
import static org.junit.Assert.*;

/**
 * Test class for the Spring Data Elastic module.<br/>
 * It supports data initialization thanks to the <a href="https://github.com/tlrx/elasticsearch-test">elasticsearch-test framework</a>.<br/>
 * It also performs generic CRUD tests on the tested repository.<br/><br/>
 * The repository must verify the following tests related to <b>indexing</b> or <b>saving</b> that have the same behavior :
 * <ul>
 *     <li>Indexing a null document throws an IllegalArgumentException.</li>
 *     <li>Saving a null document throws an IllegalArgumentException.</li>
 *     <li>Saving a list of documents with one null throws an IllegalArgumentException and no document is indexed.</li>
 *     <li>Indexing a new document inserts the document in the index.</li>
 *     <li>Saving a new document inserts the document in the index.</li>
 *     <li>Saving a list of new documents inserts the documents in the index.</li>
 *     <li>Indexing an existing document replaces the document in the index.</li>
 *     <li>Saving an existing document replaces the document in the index.</li>
 *     <li>Saving a list of existing documents replaces the documents in the index.</li>
 * </ul>
 */
@ContextConfiguration(classes = SpringDataEsTestConfiguration.class)
public abstract class InitializedSpringDataEsTestCase<T, ID extends Serializable, R extends ElasticsearchRepository<T, ID>> {

    /**
     * Document repository
     */
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private R repository;

    /**
     * Elastic index of the document to test
     */
    private final String index;
    /**
     * Elastic type of the document to test
     */
    private final String type;

    /**
     * Number of types documents in the index at initialization
     */
    private long initialDocumentCount = 0;

    /**
     * File extension for document mapping
     */
    protected static final String DOCUMENT_MAPPING_EXTENSION = ".mapping.json";
    /**
     * File extension for document data
     */
    protected static final String DOCUMENT_DATA_EXTENSION = ".data.bulk";

    /**
     * Elastic client
     */
    @Autowired
    private Client elasticsearchClient;
    /**
     * Elastic setup for index/type initialization
     */
    private EsSetup esSetup;

    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(InitializedSpringDataEsTestCase.class);


    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                      Initialization of Elastic index                                           */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Empty constructor that gets the Elastic index and type from the associated entity class.
     */
    public InitializedSpringDataEsTestCase() {

        // Find document class
        ParameterizedType superClass = (ParameterizedType) getClass().getGenericSuperclass();
        Type [] types = superClass.getActualTypeArguments();
        @SuppressWarnings("unchecked")
        Class<T> documentClass = (Class<T>) types[0];

        // Get index and type from document class annotations
        index = documentClass.getDeclaredAnnotation(Document.class).indexName();
        type = documentClass.getDeclaredAnnotation(Document.class).type();
    }

    /**
     * Create an Elastic index and type and load custom data in it.
     */
    @Before
    public void initElasticIndex() {

        CreateIndex createIndex = createIndex(getIndex());

        // Add mapping to the Elastic type
        JSONProvider mappingClassPath = fromClassPath(getClass().getName().replace(".", "/") + DOCUMENT_MAPPING_EXTENSION);
        try {
            createIndex.withMapping(getType(), mappingClassPath);
        }
        catch (EsSetupRuntimeException e) {
            //noinspection ThrowableResultOfMethodCallIgnored
            logger.warn(ExceptionUtils.getRootCause(e).getMessage());
        }

        // Add data to the Elastic type
        JSONProvider dataClassPath = fromClassPath(getClass().getName().replace(".", "/") + DOCUMENT_DATA_EXTENSION);
        createIndex.withData(dataClassPath);

        // Setup Elastic index/type initialization
        esSetup = new EsSetup(elasticsearchClient, false);
        try {
            esSetup.execute(deleteAll(), createIndex);
            initialDocumentCount = countDocuments();                   // Initial number of documents
        }
        catch (EsSetupRuntimeException e) {
            //noinspection ThrowableResultOfMethodCallIgnored
            logger.warn(ExceptionUtils.getRootCause(e).getMessage());
            esSetup = null;
        }
    }

    /**
     * Free resources used by Elastic.
     */
    @After
    public void terminateElasticIndex() {

        esSetup.terminate();
    }


    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                             Sub-classes methods                                                */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Get the Elastic index.
     * @return The Elastic index
     */
    protected String getIndex() {
        return index;
    }

    /**
     * Get the Elastic type.
     * @return The Elastic type
     */
    protected String getType() {
        return type;
    }

    /**
     * Give access to the tested repository.
     * @return The tested repository
     */
    @SuppressWarnings("unused")
    protected R getRepository() { return repository; }

    /**
     * Count the number of typed documents in the index.
     * @return The number of typed documents found
     */
    protected long countDocuments() {

        return elasticsearchClient.prepareCount(getIndex()).setTypes(getType()).get().getCount();
    }

    /**
     * Return a new document for insertion.
     * @return A document that will be inserted
     */
    protected abstract T newDocumentToInsert();

    /**
     * Return a new document for update.
     * @return A document that will update an existing one
     */
    protected abstract T newDocumentToUpdate();


    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                    Tests related to language indexing                                          */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Indexing a null document throws an IllegalArgumentException.
     */
    @Test(expected=IllegalArgumentException.class)
    public void indexNullDocument() {

        repository.index(null);
    }

    /**
     * Saving a null document throws an IllegalArgumentException.
     */
    @Test(expected=IllegalArgumentException.class)
    public void saveNullDocument() {

        repository.save((T)null);
    }

    /**
     * Indexing a list of documents with one null throws an IllegalArgumentException and no document is indexed.
     */
    @Test
    public void saveNullDocuments() {

        List<T> documents = new ArrayList<>(1);
        documents.add(newDocumentToInsert());
        documents.add(null);

        try {
            repository.save(documents);
            fail("IllegalArgumentException must be thrown");
        }
        catch (IllegalArgumentException e) {
            assertEquals(initialDocumentCount, countDocuments());
        }
    }

    /**
     * Indexing a new document inserts the document in the index.
     */
    @Test
    public void indexNewDocument() {

        T toInsert = newDocumentToInsert();
        T inserted = repository.index(toInsert);

        assertEquals(initialDocumentCount + 1, countDocuments());
        assertEquals(toInsert, inserted);
    }

    /**
     * Saving a new document inserts the document in the index.
     */
    @Test
    public void saveNewDocument() {

        T toInsert = newDocumentToInsert();
        T inserted = repository.save(toInsert);

        assertEquals(initialDocumentCount + 1, countDocuments());
        assertEquals(toInsert, inserted);
    }

    /**
     * Saving a list of new documents inserts the documents in the index.
     */
    @Test
    public void saveNewDocuments() {

        List<T> toInsert = new ArrayList<>(1);
        toInsert.add(newDocumentToInsert());
        List<T> inserted = new ArrayList<>(1);
        repository.save(toInsert).forEach(inserted::add);

        assertEquals(initialDocumentCount + 1, countDocuments());
        assertArrayEquals(toInsert.toArray(), inserted.toArray());
    }

    /**
     * Indexing an existing document replaces the document in the index.
     */
    @Test
    public void indexExistingDocument() {

        T toUpdate = newDocumentToUpdate();
        T updated = repository.index(toUpdate);

        assertEquals(initialDocumentCount, countDocuments());
        assertEquals(toUpdate, updated);
    }

    /**
     * Saving an existing document replaces the document in the index.
     */
    @Test
    public void saveExistingDocument() {

        T toUpdate = newDocumentToUpdate();
        T updated = repository.save(toUpdate);

        assertEquals(initialDocumentCount, countDocuments());
        assertEquals(toUpdate, updated);
    }

    /**
     * Saving a list of existing documents replaces the documents in the index.
     */
    @Test
    public void saveExistingDocuments() {

        List<T> toUpdate = new ArrayList<>(1);
        toUpdate.add(newDocumentToUpdate());
        List<T> updated = new ArrayList<>(1);
        repository.save(toUpdate).forEach(updated::add);

        assertEquals(initialDocumentCount, countDocuments());
        assertArrayEquals(toUpdate.toArray(), updated.toArray());
    }
}
