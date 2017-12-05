package nablarch.integration.doma;

import java.util.function.Supplier;

import org.seasar.doma.jdbc.AbstractJdbcLogger;
import org.seasar.doma.jdbc.JdbcLogger;

import nablarch.core.log.Logger;
import nablarch.core.log.LoggerManager;
import nablarch.core.log.basic.LogLevel;

/**
 * Nablarchの{@link Logger}へログを出力する{@link JdbcLogger}の実装クラス。
 * 
 * @author Taichi Uragami
 */
public class NablarchJdbcLogger extends AbstractJdbcLogger<LogLevel> {

    private final Logger logger;

    public NablarchJdbcLogger(final LogLevel level) {
        this(level, LoggerManager.get(NablarchJdbcLogger.class));
    }

    //テストで使用するコンストラクタ
    NablarchJdbcLogger(final LogLevel level, final Logger logger) {
        super(level);
        this.logger = logger;
    }

    @Override
    protected void log(final LogLevel level, final String callerClassName,
            final String callerMethodName, final Throwable throwable,
            final Supplier<String> messageSupplier) {
        switch (level) {
        case FATAL:
            if (logger.isFatalEnabled()) {
                logger.logFatal(messageSupplier.get(), throwable);
            }
            break;
        case ERROR:
            if (logger.isErrorEnabled()) {
                logger.logError(messageSupplier.get(), throwable);
            }
            break;
        case WARN:
            if (logger.isWarnEnabled()) {
                logger.logWarn(messageSupplier.get(), throwable);
            }
            break;
        case INFO:
            if (logger.isInfoEnabled()) {
                logger.logInfo(messageSupplier.get(), throwable);
            }
            break;
        case DEBUG:
            if (logger.isDebugEnabled()) {
                logger.logDebug(messageSupplier.get(), throwable);
            }
            break;
        case TRACE:
            if (logger.isTraceEnabled()) {
                logger.logTrace(messageSupplier.get(), throwable);
            }
            break;
        }
    }
}
