package nablarch.integration.doma;

import org.seasar.doma.jdbc.Config;

/**
 * {@link Config Domaの設定}の中で{@link java.sql.Statement Statement}に関するものをまとめたクラス。
 * <br/>
 * バッチサイズはStatementに設定する項目ではないが、Statementを実行する単位を決定するための値なので当クラスに含んでいる。
 * 
 * @author Taichi Uragami
 */
public class DomaStatementProperties {

    /** 最大行数の制限値 */
    private int maxRows;

    /** フェッチサイズ */
    private int fetchSize;

    /** クエリタイムアウト（秒） */
    private int queryTimeout;

    /** バッチサイズ */
    private int batchSize;

    /**
     * 最大行数の制限値を取得する。
     * 
     * @return 最大行数の制限値
     * @see Config#getMaxRows()
     */
    public int getMaxRows() {
        return maxRows;
    }

    /**
     * 最大行数の制限値をセットする。
     * 
     * @param maxRows 最大行数の制限値
     */
    public void setMaxRows(int maxRows) {
        this.maxRows = maxRows;
    }

    /**
     * フェッチサイズを取得する。
     * 
     * @return フェッチサイズ
     * @see Config#getFetchSize()
     */
    public int getFetchSize() {
        return fetchSize;
    }

    /**
     * フェッチサイズをセットする。
     * 
     * @param fetchSize フェッチサイズ
     */
    public void setFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
    }

    /**
     * クエリタイムアウト（秒）を取得する。
     * 
     * @return クエリタイムアウト（秒）
     * @see Config#getQueryTimeout()
     */
    public int getQueryTimeout() {
        return queryTimeout;
    }

    /**
     * クエリタイムアウト（秒）をセットする。
     * 
     * @param queryTimeout クエリタイムアウト（秒）
     */
    public void setQueryTimeout(int queryTimeout) {
        this.queryTimeout = queryTimeout;
    }

    /**
     * バッチサイズを取得する。
     * 
     * @return バッチサイズ
     * @see Config#getBatchSize()
     */
    public int getBatchSize() {
        return batchSize;
    }

    /**
     * バッチサイズをセットする。
     * 
     * @param batchSize バッチサイズ
     */
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
}
