package info.jallaix.common.language.dao;

import com.carrotsearch.randomizedtesting.RandomizedContext;
import com.carrotsearch.randomizedtesting.RandomizedRunner;
import com.carrotsearch.randomizedtesting.generators.RandomStrings;
import com.github.tlrx.elasticsearch.test.EsSetup;
import com.github.tlrx.elasticsearch.test.EsSetupRuntimeException;
import com.github.tlrx.elasticsearch.test.provider.JSONProvider;
import com.github.tlrx.elasticsearch.test.request.CreateIndex;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.elasticsearch.client.Client;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.ReflectionUtils;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Random;

import static com.github.tlrx.elasticsearch.test.EsSetup.*;
import static org.junit.Assert.assertEquals;

/**
 * Test class for the Spring Data Elastic module.<br/>
 * It supports data initialization thanks to the <a href="https://github.com/tlrx/elasticsearch-test">elasticsearch-test framework</a>.<br/>
 * It also performs generic CRUD tests on the tested repository.
 */
//@RunWith(RandomizedRunner.class)
@ContextConfiguration(classes = SpringDataEsTestConfiguration.class)
public class InitializedSpringDataEsTestCase<T, ID extends Serializable, R extends ElasticsearchRepository<T, ID>> {

    private final Class<T> typeClass;
    private final Class<ID> idClass;

    /**
     * Elastic index of the document to test
     */
    private String index;
    /**
     * Elastic type of the document to test
     */
    private String type;

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

    @Autowired
    private R repository;

    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(InitializedSpringDataEsTestCase.class);

    /*----------------------------------------------------------------------------------------------------------------*/
    /*                                      Initialization of Elastic index                                           */
    /*----------------------------------------------------------------------------------------------------------------*/

    /**
     * Empty constructor that gets the Elastic index and type from the associated Java entity.
     */
    public InitializedSpringDataEsTestCase() {

        // Find type, id and repository classes
        ParameterizedType superClass = (ParameterizedType) getClass().getGenericSuperclass();
        Type [] types = superClass.getActualTypeArguments();
        typeClass = (Class<T>) types[0];
        idClass = (Class<ID>) types[1];

        index = typeClass.getDeclaredAnnotation(Document.class).indexName();
        type = typeClass.getDeclaredAnnotation(Document.class).type();
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
            logger.warn(ExceptionUtils.getRootCause(e).getMessage());
        }

        // Add data to the Elastic type
        JSONProvider dataClassPath = fromClassPath(getClass().getName().replace(".", "/") + DOCUMENT_DATA_EXTENSION);
        createIndex.withData(dataClassPath);

        // Setup Elastic index/type initialization
        esSetup = new EsSetup(elasticsearchClient, false);
        try {
            esSetup.execute(deleteAll(), createIndex);
        }
        catch (EsSetupRuntimeException e) {
            logger.warn(ExceptionUtils.getRootCause(e).getMessage());
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
     * Get the Elastic index
     * @return The Elastic index
     */
    protected String getIndex() {
        return index;
    }

    /**
     * Get the Elastic type
     * @return The Elastic type
     */
    protected String getType() {
        return type;
    }

    /**
     * Count the number of typed documents in the index.
     * @return The number of typed documents found
     */
    protected long countDocuments() {

        return elasticsearchClient.prepareCount(getIndex()).setTypes(getType()).get().getCount();
    }

    /**
     * Instantiate a document and set its properties with random values
     * Currently only support basic types and their object versions
     * @return The new document
     * @throws ReflectiveOperationException If the document couldn't be instantiated
     */
    protected T newRandomDocument() throws ReflectiveOperationException {

        T document = typeClass.newInstance();

        Random random = RandomizedContext.current().getRandom();
        ReflectionUtils.doWithFields(typeClass, field -> {
            if (field.getType() == String.class)
                field.set(document, RandomStrings.randomUnicodeOfLength(random, 50));
            else if (field.getType() == int.class || field.getType() == Integer.class)
                field.setInt(document, random.nextInt());
            else if (field.getType() == long.class || field.getType() == Long.class)
                field.setLong(document, random.nextLong());
            else if (field.getType() == boolean.class || field.getType() == Boolean.class)
                field.setBoolean(document, random.nextBoolean());
            else if (field.getType() == double.class || field.getType() == Double.class)
                field.setDouble(document, random.nextDouble());
            else if (field.getType() == float.class || field.getType() == Float.class)
                field.setFloat(document, random.nextFloat());
            else if (field.getType() == byte.class || field.getType() == Byte.class)
                field.setByte(document, (byte)random.nextInt());
            else if (field.getType() == char.class || field.getType() == Character.class)
                field.setChar(document, RandomStrings.randomAsciiOfLength(random, 1).charAt(0));
            else if (field.getType() == short.class || field.getType() == Short.class)
                field.setShort(document, (short)random.nextInt());
            else
                logger.warn("Unsupported object property : " + field.getName());
        });

        return document;
    }

    /**
     * Give access to the tested repository
     * @return The tested repository
     */
    protected R getRepository() { return repository; }


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
     * Indexing a non existing document inserts the document in the index.
     * @throws ReflectiveOperationException
     */
    @Test
    public void indexNewDocument() throws ReflectiveOperationException {

        assertEquals(2, countDocuments());

        T toInsert = newRandomDocument();
        T inserted = repository.index(toInsert);

        assertEquals(3, countDocuments());
        assertEquals(toInsert, inserted);
    }

    /**
     * Indexing an existing document replaces the document in the index.
     * @throws ReflectiveOperationException
     */
    @Test
    public void indexExistingDocument() throws ReflectiveOperationException {

        assertEquals(2, countDocuments());
        T toUpdate = newRandomDocument();
        T updated = repository.index(toUpdate);

        assertEquals(2, countDocuments());
        assertEquals(toUpdate, updated);
    }
}
