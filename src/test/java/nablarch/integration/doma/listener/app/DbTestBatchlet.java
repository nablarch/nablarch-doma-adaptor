package nablarch.integration.doma.listener.app;

import nablarch.core.log.Logger;
import nablarch.core.log.LoggerManager;
import nablarch.integration.doma.DomaDaoRepository;
import nablarch.integration.doma.listener.TestDomaEntity;
import nablarch.integration.doma.listener.TestDomaEntityDao;

import javax.batch.api.AbstractBatchlet;
import javax.batch.api.BatchProperty;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * テーブルにレコードを挿入する{@link javax.batch.api.Batchlet}実装クラス。
 */
@Named
@Dependent
public class DbTestBatchlet extends AbstractBatchlet {

    /** ロガー */
    private static final Logger LOGGER = LoggerManager.get(DbTestItemWriter.class);

    /** テスト用プロパティ */
    @Inject
    @BatchProperty
    String isError = "false";

    @Override
    public String process() throws Exception {

        DomaDaoRepository.get(TestDomaEntityDao.class).insert(new TestDomaEntity("AAA"));
        if (isError.equalsIgnoreCase("true")) {
            RuntimeException e = new RuntimeException("Test Exception Message.");
            LOGGER.logError(e.getMessage(), e);
            throw e;
        }
        return "success";
    }
}
