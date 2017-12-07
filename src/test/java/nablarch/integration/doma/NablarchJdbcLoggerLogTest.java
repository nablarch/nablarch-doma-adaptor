package nablarch.integration.doma;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.junit.After;
import org.junit.Assume;
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
    public static List<Param> parameters() {
        return Arrays.asList(
                new Param(LogLevel.FATAL, null, LogLevel.ERROR),
                new Param(LogLevel.ERROR, LogLevel.FATAL, LogLevel.WARN),
                new Param(LogLevel.WARN, LogLevel.ERROR, LogLevel.INFO),
                new Param(LogLevel.INFO, LogLevel.WARN, LogLevel.DEBUG),
                new Param(LogLevel.DEBUG, LogLevel.INFO, LogLevel.TRACE),
                new Param(LogLevel.TRACE, LogLevel.DEBUG, null));
    }

    private NablarchJdbcLogger sut;
    private LoggerFactory factory;
    private final Param param;

    public NablarchJdbcLoggerLogTest(Param param) {
        this.param = param;
    }

    /**
     * 指定されたログレベルでログが出力されていること
     */
    @Test
    public void log() {
        sut.log(param.level, null, null, null, () -> "test");

        List<String> messages = OnMemoryLogWriter.getMessages("writer.mem");
        assertThat(messages.size(), is(1));
        assertThat(messages.get(0),
                allOf(containsString(param.level.name()), containsString("test")));
    }

    /**
     * ロガーに設定されたログレベルよりも低いレベルのログは出力されること
     */
    @Test
    public void logUnderLevel() {
        Assume.assumeThat("ロガーに設定されているのが最も低いログレベルなのでテストは不要", param.underLevel, notNullValue());
        sut.log(param.underLevel, null, null, null, () -> "test");

        List<String> messages = OnMemoryLogWriter.getMessages("writer.mem");
        assertThat(messages.size(), is(1));
        assertThat(messages.get(0),
                allOf(containsString(param.underLevel.name()), containsString("test")));
    }

    /**
     * ロガーに設定されたログレベルよりも高いレベルのログは出力されないこと
     */
    @Test
    public void logOverLevel() {
        Assume.assumeThat("ロガーに設定されているのが最も高いログレベルなのでテストは不要", param.overLevel, notNullValue());
        sut.log(param.overLevel, null, null, null, () -> "test");

        List<String> messages = OnMemoryLogWriter.getMessages("writer.mem");
        assertThat(messages.isEmpty(), is(true));
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
    private class MockLogSettings extends LogSettings {

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
            props.put("loggers.log.level", param.level.name());
            props.put("loggers.log.writerNames", "mem");
            return props;
        }
    }

    private static class Param {

        /** ロガーに設定されるログレベル */
        private final LogLevel level;
        /** {@link #level}よりも低いログレベル */
        private final LogLevel underLevel;
        /** {@link #level}よりも高いログレベル */
        private final LogLevel overLevel;

        public Param(LogLevel level, LogLevel underLevel, LogLevel overLevel) {
            this.level = level;
            this.underLevel = underLevel;
            this.overLevel = overLevel;
        }
    }
}
