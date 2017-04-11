package nablarch.integration.doma.listener.app;

import nablarch.integration.doma.listener.TestDomaEntity;

import javax.batch.api.chunk.AbstractItemReader;
import javax.enterprise.context.Dependent;
import javax.inject.Named;

/**
 * データベースに挿入する情報を取得する{@link javax.batch.api.chunk.ItemReader}実装クラス。
 */
@Dependent
@Named
public class DbTestItemReader extends AbstractItemReader {

    /** カウンター */
    private int count = 1;

    @Override
    public Object readItem() throws Exception {
        if (count >= 5) {
            return null;
        }
        return new TestDomaEntity(String.format("%d", count++));
    }
}
