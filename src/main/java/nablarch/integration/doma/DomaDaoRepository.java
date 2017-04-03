package nablarch.integration.doma;

import java.util.Map;
import java.util.WeakHashMap;

import nablarch.core.util.annotation.Published;

/**
 * Domaで使用するDaoの実装クラスを生成・保持するクラス。
 *
 * @author Naoki Yamamoto
 */
@Published
public final class DomaDaoRepository {

    /** Dao実装クラスのインスタンスを保持するMap */
    private static final Map<Class<?>, Object> DAO_IMPL_MAP = new WeakHashMap<>();

    /** 隠蔽コンストラクタ */
    private DomaDaoRepository() {}

    /**
     * 指定されたDaoインタフェースの実装クラスを取得する。
     *
     * @param daoClass Daoインタフェースの{@link Class}
     * @param <T> Daoインタフェース
     * @return Dao実装クラス
     */
    @SuppressWarnings("unchecked")
    public static synchronized  <T> T get(final Class<T> daoClass) {
        return (T) DAO_IMPL_MAP.computeIfAbsent(daoClass, DomaDaoRepository::createInstance);
    }

    /**
     * 指定されたDaoインタフェースの実装クラスを生成する。
     *
     * @param daoClass Daoインタフェースの{@link Class}
     * @param <T> Daoインタフェース
     * @return Dao実装クラス
     */
    @SuppressWarnings("unchecked")
    private static <T> T createInstance(final Class<T> daoClass) {
        final String implClassName = daoClass.getName() + "Impl";
        try {
            final Class<T> implClass = (Class<T>) Thread.currentThread().getContextClassLoader().loadClass(implClassName);
            return implClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("implementation class is undefined. class name = [" + daoClass.getName() + ']', e);
        }

    }
}
