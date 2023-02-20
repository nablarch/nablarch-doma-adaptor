package nablarch.integration.doma;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import nablarch.common.dao.UniversalDao;
import nablarch.common.mail.FreeTextMailContext;
import nablarch.common.mail.MailRequester;
import nablarch.common.mail.MailUtil;
import nablarch.core.date.BusinessDateUtil;
import nablarch.core.db.connection.DbConnectionContext;
import nablarch.core.transaction.TransactionContext;
import nablarch.fw.ExecutionContext;
import nablarch.fw.Handler;
import nablarch.test.support.SystemRepositoryResource;
import nablarch.test.support.db.helper.DatabaseTestRunner;
import nablarch.test.support.db.helper.VariousDbTestHelper;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * {@link Transactional}とNablarchのデータベースアクセスを併用する場合のテスト。
 */
@RunWith(DatabaseTestRunner.class)
public class TransactionalWithNablarch {

    @Rule
    public SystemRepositoryResource systemRepositoryResource = new SystemRepositoryResource("DomaConnectionConfig.xml");

    @BeforeClass
    public static void setUpClass() throws Exception {
        VariousDbTestHelper.createTable(TestTable.class);
        VariousDbTestHelper.createTable(BusinessDate.class);
        VariousDbTestHelper.createTable(MailRequest.class);
        VariousDbTestHelper.createTable(MailRecipient.class);
        VariousDbTestHelper.createTable(MailFile.class);
        final Statement statement = VariousDbTestHelper.getNativeConnection()
                                                       .createStatement();
        statement.execute("create sequence MAIL_ID");
        statement.close();
    }

    @Before
    public void setUp() throws Exception {
        VariousDbTestHelper.delete(TestTable.class);
        VariousDbTestHelper.setUpTable(new BusinessDate("default", "20170728"));
    }

    @Test
    public void transactional以降でNablarchのデータベースアクセスが行えること() throws Exception {
        final ExecutionContext context = new ExecutionContext();
        context.addHandler(new Handler<Object, Object>() {
            @Override
            @Transactional
            public Object handle(final Object o, final ExecutionContext c) {

                // universal dao
                UniversalDao.insert(new TestTable("dao"));

                // doma
                final TestTableDao dao = DomaDaoRepository.get(TestTableDao.class);
                dao.insert(new TestTableForDoma("doma"));

                // business date
                assertThat(BusinessDateUtil.getDate())
                        .isEqualTo("20170728");

                // mail
                final MailRequester requester = MailUtil.getMailRequester();
                final FreeTextMailContext mailContext = new FreeTextMailContext();
                mailContext.setSubject("title");
                mailContext.setMailBody("body");
                mailContext.addTo("to@to.com");
                requester.requestToSend(mailContext);
                return "ok";
            }
        });

        final Object result = context.handleNext(null);
        assertThat(result).isEqualTo("ok");

        assertThat(DbConnectionContext.containConnection(TransactionContext.DEFAULT_TRANSACTION_CONTEXT_KEY))
                .as("contextには接続が残っていないこと")
                .isFalse();

        final List<TestTable> testTAbles = VariousDbTestHelper.findAll(TestTable.class);
        assertThat(testTAbles)
                .hasSize(2)
                .extracting(input -> input.name)
                .containsExactlyInAnyOrder("dao", "doma");

        final List<MailRequest> mailRequests = VariousDbTestHelper.findAll(MailRequest.class);
        assertThat(mailRequests)
                .hasSize(1)
                .extracting(r -> r.subject)
                .containsExactlyInAnyOrder("title");
    }

    @Test
    public void 例外が発生しDomaでロールバックが発生した場合Nablarchで処理したものもロールバックされること() throws Exception {

        final ExecutionContext context = new ExecutionContext();
        context.addHandler(new Handler<Object, Object>() {
            @Override
            @Transactional
            public Object handle(final Object o, final ExecutionContext c) {

                // universal dao
                UniversalDao.insert(new TestTable("dao"));

                // doma
                final TestTableDao dao = DomaDaoRepository.get(TestTableDao.class);
                dao.insert(new TestTableForDoma("doma"));

                // business date
                assertThat(BusinessDateUtil.getDate())
                        .isEqualTo("20170728");

                // mail
                final MailRequester requester = MailUtil.getMailRequester();
                final FreeTextMailContext mailContext = new FreeTextMailContext();
                mailContext.setSubject("title");
                mailContext.setMailBody("body");
                mailContext.addTo("to@to.com");
                requester.requestToSend(mailContext);

                throw new RuntimeException("ロールバックさせる例外");
            }
        });

        assertThatThrownBy(() -> context.handleNext(null))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("ロールバックさせる例外");
        
        assertThat(DbConnectionContext.containConnection(TransactionContext.DEFAULT_TRANSACTION_CONTEXT_KEY))
                .as("contextには接続が残っていないこと")
                .isFalse();

        final List<TestTable> testTAbles = VariousDbTestHelper.findAll(TestTable.class);
        assertThat(testTAbles)
                .isEmpty();

        final List<MailRequest> mailRequests = VariousDbTestHelper.findAll(MailRequest.class);
        assertThat(mailRequests)
                .isEmpty();
    }

    @Entity
    @Table(name = "b_date")
    public static class BusinessDate {

        @Id
        @Column(name = "segment")
        public String segment;

        @Column(name = "dt")
        public String dt;

        public BusinessDate() {
        }

        public BusinessDate(final String segment, final String dt) {
            this.segment = segment;
            this.dt = dt;
        }
    }

    @Entity
    @Table(name = "mail_request")
    public static class MailRequest {

        @Id
        @Column(name = "id")
        public String id;

        @Column(name = "subject")
        public String subject;

        @Column(name = "from_address")
        public String fromAddress;

        @Column(name = "reply")
        public String reply;

        @Column(name = "return")
        public String returnPath;

        @Column(name = "body")
        public String body;

        @Column(name = "charset")
        public String charset;

        @Column(name = "status")
        public String status;

        @Column(name = "request_dt")
        public Timestamp requestDt;

        @Column(name = "send_dt")
        public Timestamp sendDt;
    }

    @Entity
    @Table(name = "mail_recipient")
    public static class MailRecipient {

        @Id
        @Column(name = "id")
        public String id;

        @Id
        @Column(name = "no")
        public Long no;

        @Column(name = "mail_address")
        public String mailAddress;

        @Column(name = "type")
        public String type;
    }

    @Entity
    @Table(name = "mail_file")
    public static class MailFile {

        @Id
        @Column(name = "id")
        public String id;

        @Id
        @Column(name = "no")
        public Long no;

        @Column(name = "content_type")
        public String contentType;

        @Column(name = "name")
        public String name;

        @Column(name = "file")
        public byte[] file;
    }
}
