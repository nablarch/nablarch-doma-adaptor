package nablarch.integration.doma;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import nablarch.core.log.LogSettings;
import nablarch.core.log.Logger;
import nablarch.core.log.LoggerFactory;
import nablarch.core.log.basic.BasicLoggerFactory;
import nablarch.core.log.basic.LogLevel;
import nablarch.test.support.log.app.OnMemoryLogWriter;

/**
 * {@link NablarchJdbcLogger#log(LogLevel, String, String, Throwable, Supplier)}}のテストクラス。
 */
@RunWith(Parameterized.class)
public class NablarchJdbcLoggerLogTest {

    @Parameters(name = "{0}")
    public static List<LogLevel> parameters() {
        return Arrays.asList(
                LogLevel.FATAL,
                LogLevel.ERROR,
                LogLevel.WARN,
                LogLevel.INFO,
                LogLevel.DEBUG,
                LogLevel.TRACE);
    }

    private NablarchJdbcLogger sut;
    private LoggerFactory factory;
    private final LogLevel logLevel;

    public NablarchJdbcLoggerLogTest(LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    /**
     * 指定されたログレベルでログが出力されていること
     */
    @Test
    public void log() {
        sut.log(logLevel, null, null, null, () -> "test");

        List<String> messages = OnMemoryLogWriter.getMessages("writer.mem");
        assertThat(messages.size(), is(1));
        assertThat(messages.get(0), allOf(containsString(logLevel.name()), containsString("test")));
    }

    @Before
    public void setUp() {
        OnMemoryLogWriter.clear();
        factory = new BasicLoggerFactory();
        factory.initialize(new MockLogSettings());
        Logger logger = factory.get("test");
        //コンストラクタで設定しているログレベルはAbstractJdbcLoggerがlogメソッドへ渡すために使われる。
        //このテストではlogメソッドを直接呼び出しているのでコンストラクタで設定するログレベルは何でも良い。
        sut = new NablarchJdbcLogger(LogLevel.TRACE, logger);
    }

    @After
    public void tearDown() {
        factory.terminate();
        OnMemoryLogWriter.clear();
    }

    /**
     * OnMemoryLogWriterを使うための設定。
     */
    private static class MockLogSettings extends LogSettings {

        public MockLogSettings() {
            super(null);
        }

        @Override
        protected Map<String, String> loadSettings(String filePath) {
            Map<String, String> props = new HashMap<>();
            props.put("writerNames", "mem");
            props.put("writer.mem.className", OnMemoryLogWriter.class.getName());
            props.put("availableLoggersNamesOrder", "log");
            props.put("loggers.log.nameRegex", ".+");
            props.put("loggers.log.level", "TRACE");
            props.put("loggers.log.writerNames", "mem");
            return props;
        }
    }
}
