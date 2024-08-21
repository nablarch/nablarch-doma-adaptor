package nablarch.integration.doma;

import org.seasar.doma.Dao;

/**
 * Doma 2.44.0以降、config属性が非推奨になったことによる後方互換確認のためのインターフェース。
 * {@link Dao#config()}に{@link DomaTransactionNotSupportedConfig}を指定でき、（Pluggable Annotation Processing API）でエラーにならないことも確認する。
 */
@Dao(config = DomaTransactionNotSupportedConfig.class)
public interface TestLegacyDao2 {
}
