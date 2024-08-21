package nablarch.integration.doma;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import nablarch.core.util.annotation.Published;
import org.seasar.doma.Dao;
import org.seasar.doma.jdbc.Config;

/**
 * Domaで使用するDaoの実装クラスを生成・保持するクラス。
 *
 * @author Naoki Yamamoto
 */
@Published
public final class DomaDaoRepository {
    /** Dao実装クラスのインスタンスを保持するMap（{@link org.seasar.doma.SingletonConfig}および{@link Dao}のconfig属性を使用している実装向け */
    private static final Map<Class<?>, Object> LEGACY_DAO_IMPL_MAP = new WeakHashMap<>();

    /** Dao実装クラスのインスタンスを保持するMap */
    private static final Map<DaoClassWithConfigKey, Object> DAO_IMPL_MAP = new WeakHashMap<>();

    private static final Map<Class<? extends Config>, Config> CONFIG_MAP = new HashMap<>(Map.of(
            DomaConfig.class, DomaConfig.singleton(),
            DomaTransactionNotSupportedConfig.class, DomaTransactionNotSupportedConfig.singleton()
    ));

    /** 隠蔽コンストラクタ */
    private DomaDaoRepository() {}

    /**
     * 指定されたDaoインタフェースの実装クラスを取得する。
     * Daoの実装クラスのインスタンス生成の際には、{@link DomaConfig}をコンストラクタ引数に指定することを試みる。
     * Daoの実装クラスに{@link Config}を引数に取るコンストラクタが存在しない場合は、デフォルトコンストラクタを利用する。
     *
     * @param daoClass Daoインタフェースの{@link Class}
     * @param <T> Daoインタフェース
     * @return Dao実装クラス
     */
    @SuppressWarnings("unchecked")
    public static synchronized  <T> T get(final Class<T> daoClass) {
        T daoImplInstance = (T) LEGACY_DAO_IMPL_MAP.get(daoClass);

        if (daoImplInstance != null) {
            return daoImplInstance;
        }

        daoImplInstance = (T) DAO_IMPL_MAP.get(new DaoClassWithConfigKey(daoClass, CONFIG_MAP.get(DomaConfig.class)));

        if (daoImplInstance != null) {
            return daoImplInstance;
        }

        if (hasNonDefaultConfigAttribute(daoClass)) {
            // @Dao#configが指定されている場合はデフォルトコンストラクタでインスタンス化
            return (T) LEGACY_DAO_IMPL_MAP.computeIfAbsent(daoClass, DomaDaoRepository::createInstance);
        }

        return (T) DAO_IMPL_MAP.computeIfAbsent(
                new DaoClassWithConfigKey(daoClass, DomaConfig.singleton()), d -> createInstance(daoClass, CONFIG_MAP.get(DomaConfig.class))
        );
    }

    /**
     * 指定されたDaoインタフェースの実装クラスのインスタンスを、指定された{@link Config}をコンストラクタ引数として指定して取得する。
     * Daoインターフェースに{@link Dao}の{@code config}属性が指定されていた場合は、求められる動作を満たせないため例外をスローする
     *
     * @param daoClass Daoインタフェースの{@link Class}
     * @param configClass {@link Config}の{@link Class}
     * @param <T> Daoインタフェース
     * @return Dao実装クラス
     */
    @SuppressWarnings("unchecked")
    public static synchronized  <T> T get(final Class<T> daoClass, Class<? extends Config> configClass) {
        Config c = CONFIG_MAP.get(configClass);

        if (c == null) {
            c = createConfigInstance(configClass);
        }

        Config config = c;

        T daoImplInstance = (T) DAO_IMPL_MAP.get(new DaoClassWithConfigKey(daoClass, config));

        if (daoImplInstance != null) {
            return daoImplInstance;
        }

        if (hasNonDefaultConfigAttribute(daoClass)) {
            throw new IllegalArgumentException(
                    "implementation class is invalid." +
                            " Do not specify config attribute for Dao annotation." +
                            " class name = [" + daoClass.getName() + ']'
            );
        }

        return (T) DAO_IMPL_MAP.computeIfAbsent(
                new DaoClassWithConfigKey(daoClass, config), d -> {
                    T dao = createInstance(daoClass, config);
                    // Daoのインスタンスに生成した場合はConfigのインスタンスをキャッシュする
                    CONFIG_MAP.putIfAbsent(configClass, config);
                    return dao;
                }
        );
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> findDaoImplClass(Class<T> daoClass) {
        final String implClassName = daoClass.getName() + "Impl";
        try {
            return (Class<T>) Thread.currentThread().getContextClassLoader().loadClass(implClassName);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("implementation class is undefined. class name = [" + daoClass.getName() + ']', e);
        }
    }

    /**
     * Daoの{@link Class}に付与された{@link Dao}アノテーションにconfig属性がデフォルト値以外に設定されている場合、{@code true}を返却する。
     *
     * @param daoClass Daoの{@link Class}クラス
     * @return Daoの{@link Class}に付与された{@link Dao}アノテーションにconfig属性がデフォルト値以外に設定されている場合は{@code true}
     */
    @SuppressWarnings("deprecation")
    private static boolean hasNonDefaultConfigAttribute(Class<?> daoClass) {
        return daoClass.getAnnotation(Dao.class) != null
                && !daoClass.getAnnotation(Dao.class).config().equals(Config.class);
    }

    /**
     * 指定されたDaoインタフェースの実装クラスのインスタンスを生成する。インスタンス生成の際には、{@link Config}をコンストラクタ引数に指定する。
     *
     * @param daoClass Daoインタフェースの{@link Class}
     * @param config {@link Config}のインスタンス
     * @param <T> Daoインタフェース
     * @return Dao実装クラス
     */
    private static <T> T createInstance(final Class<T> daoClass, final Config config) {
        final Class<T> implClass = findDaoImplClass(daoClass);

        try {
            Constructor<T> constructor = implClass.getConstructor(Config.class);
            return constructor.newInstance(config);
        } catch (Exception e) {
            throw new IllegalArgumentException("implementation class is invalid. class name = [" + daoClass.getName() + ']', e);
        }
    }

    /**
     * 指定されたDaoインタフェースの実装クラスのインスタンスを生成する。
     *
     * @param daoClass Daoインタフェースの{@link Class}
     * @param <T> Daoインタフェース
     * @return Dao実装クラス
     */
    private static <T> T createInstance(final Class<T> daoClass) {
        final Class<T> implClass = findDaoImplClass(daoClass);

        try {
            return implClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("implementation class is invalid. class name = [" + daoClass.getName() + ']', e);
        }
    }

    /**
     * {@link Config}のインスタンスを生成する。
     *
     * @param configClass {@link Config}の{@link Class}クラス
     * @return {@link Config}のインスタンス
     */
    private static Config createConfigInstance(final Class<? extends Config> configClass) {
        try {
            return configClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("default constructor not defined in Config class. class name = [" + configClass.getName() + ']', e);
        }
    }

    record DaoClassWithConfigKey(Class<?> daoClass, Config config) {
    }
}
