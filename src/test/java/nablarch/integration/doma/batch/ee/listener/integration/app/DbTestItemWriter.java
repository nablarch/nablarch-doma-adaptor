package nablarch.integration.doma.batch.ee.listener.integration.app;

import nablarch.core.log.Logger;
import nablarch.core.log.LoggerManager;
import nablarch.integration.doma.DomaDaoRepository;
import nablarch.integration.doma.batch.ee.listener.integration.TestDomaEntity;
import nablarch.integration.doma.batch.ee.listener.integration.TestDomaEntityDao;

import javax.batch.api.BatchProperty;
import javax.batch.api.chunk.AbstractItemWriter;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * データベースにレコードを挿入する{@link javax.batch.api.chunk.ItemWriter}実装クラス。
 */
@Dependent
@Named
public class DbTestItemWriter extends AbstractItemWriter {

    /** ロガー */
    private static final Logger  LOGGER = LoggerManager.get(DbTestItemWriter.class);

    /** テスト用プロパティ */
    @Inject
    @BatchProperty
    String isError = "false";

    @Override
    public void writeItems(final List<Object> list) throws Exception {
        for (Object obj : list) {
            TestDomaEntity entity = (TestDomaEntity) obj;
            DomaDaoRepository.get(TestDomaEntityDao.class).insert(entity);
        }
        if (isError.equalsIgnoreCase("true")) {
            RuntimeException e = new RuntimeException("Test Exception Message.");
            LOGGER.logError(e.getMessage(), e);
            throw e;
        }
    }
}
