package nablarch.integration.doma.batch.ee.listener.integration;

import mockit.Expectations;
import mockit.Mocked;
import nablarch.fw.batch.ee.listener.NablarchListenerContext;
import nablarch.integration.doma.batch.ee.listener.DomaTransactionItemWriteListener;
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
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * {@link DomaTransactionStepListener}のテスト
 */
@RunWith(Arquillian.class)
@Ignore
public class DomaTransactionItemWriteListenerIntegrationTest extends DomaTestSupport {

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
        VariousDbTestHelper.delete(TestEntity.class);
    }

    /**
     * {@link nablarch.integration.doma.batch.ee.listener.integration.app.DbTestItemReader}と{@link nablarch.integration.doma.batch.ee.listener.integration.app.DbTestItemWriter}が
     * 正常に実行され、コミットされていること。
     */
    @Test
    public void testChunkSuccess() {
        executeJob("dbTest-chunk-test");
        List<TestEntity> list = VariousDbTestHelper.findAll(TestEntity.class, "name");
        assertThat(list.size(), is(4));

        int count = 1;
        for (TestEntity testEntity : list) {
            assertThat(testEntity.name, is(String.format("%d", count++)));
        }
    }

    /**
     * {@link nablarch.integration.doma.batch.ee.listener.integration.app.DbTestItemReader}と{@link nablarch.integration.doma.batch.ee.listener.integration.app.DbTestItemWriter}が
     * 正常に実行されるが、{@link DomaTransactionItemWriteListener}の処理結果が<code>false</code>のため、ロールバックされていること。
     */
    @Test
    public void testChunkSuccessButNotCommit(@Mocked final NablarchListenerContext context) {
        new Expectations(NablarchListenerContext.class) {{
            context.isProcessSucceeded();
            result = false;
        }};

        executeJob("dbTest-chunk-test");
        List<TestEntity> expected = VariousDbTestHelper.findAll(TestEntity.class);
        assertThat(expected.size(), is(0));
    }

    /**
     * {@link nablarch.integration.doma.batch.ee.listener.integration.app.DbTestItemReader}と{@link nablarch.integration.doma.batch.ee.listener.integration.app.DbTestItemWriter}が失敗し、
     * ロールバックされていること。
     */
    @Test
    public void testChunkError() {
        executeJob("dbTest-chunk-test-error");
        OnMemoryLogWriter.assertLogContains("writer.memory", "java.lang.RuntimeException: Test Exception Message.");
        List<TestEntity> expected = VariousDbTestHelper.findAll(TestEntity.class);
        assertThat(expected.size(), is(0));
    }
}
