package nablarch.integration.doma.batch.ee.listener.integration;

import nablarch.integration.doma.batch.ee.listener.DomaTransactionStepListener;
import nablarch.test.support.SystemRepositoryResource;
import nablarch.test.support.db.helper.VariousDbTestHelper;
import nablarch.test.support.log.app.OnMemoryLogWriter;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.*;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * {@link DomaTransactionStepListener}のテスト
 */
@RunWith(Arquillian.class)
@Ignore
public class DomaTransactionStepListenerIntegrationTest extends DomaTestSupport {

    @Deployment
    public static WebArchive createDeployment() {
        final WebArchive archive = ShrinkWrap.create(WebArchive.class, "doma-adaptor.war")
                .addPackages(true, object -> true, "nablarch");
        return archive;
    }

    @Rule
    public SystemRepositoryResource repositoryResource = new SystemRepositoryResource("integration-test/jbatch.xml");

    @BeforeClass
    public static void beforeClass() throws IOException {
        DomaTestSupport.beforeClass();
        VariousDbTestHelper.createTable(TestEntity.class);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        DomaTestSupport.afterClass();
    }

    @Before
    public void setUp() throws Exception {
        OnMemoryLogWriter.clear();
        VariousDbTestHelper.delete(TestEntity.class);
    }

    /**
     * {@link nablarch.integration.doma.batch.ee.listener.integration.app.DbTestBatchlet}が正常に実行され、コミットされていること。
     */
    @Test
    public void testBatchletSuccess() {
        executeJob("dbTest-batchlet-test");
        TestEntity expected = VariousDbTestHelper.findById(TestEntity.class, "AAA");
        assertThat(expected, notNullValue());
    }

    /**
     * {@link nablarch.integration.doma.batch.ee.listener.integration.app.DbTestBatchlet}が失敗し、ロールバックされていること。
     */
    @Test
    public void testBatchletError() {
        executeJob("dbTest-batchlet-test-error");
        OnMemoryLogWriter.assertLogContains("writer.memory", "java.lang.RuntimeException: Test Exception Message.");
        TestEntity expected = VariousDbTestHelper.findById(TestEntity.class, "AAA");
        assertThat(expected, nullValue());
    }
}
