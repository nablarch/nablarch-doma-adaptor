package nablarch.integration.doma;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import java.lang.reflect.Field;

import nablarch.test.support.SystemRepositoryResource;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.seasar.doma.Dao;
import org.seasar.doma.internal.RuntimeConfig;
import org.seasar.doma.jdbc.Config;

/**
 * {@link DomaDaoRepository}のテストクラス。
 */
public class DomaDaoRepositoryTest {
    
    @ClassRule
    public static SystemRepositoryResource systemRepositoryResource = new SystemRepositoryResource("config.xml");

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    /**
     * {@link Dao}のconfigを指定していないDaoの実装クラスのインスタンスが取得できること、Daoには{@link DomaConfig}が設定されていること
     * @throws Exception
     */
    @Test
    public void get() {
        TestDao dao = DomaDaoRepository.get(TestDao.class);
        assertThat(dao, instanceOf(TestDaoImpl.class));

        // デフォルトではDomaConfigが設定される
        assertThat(unwrapConfig(((TestDaoImpl) dao).getConfig()), instanceOf(DomaConfig.class));
    }

    /**
     * 実装クラスがキャッシュされているため、同一のインスタンスが返却されること
     * @throws Exception
     */
    @Test
    public void get_cache() {
        TestDao dao1 = DomaDaoRepository.get(TestDao.class);
        TestDao dao2 = DomaDaoRepository.get(TestDao.class);
        assertThat(dao1, sameInstance(dao2));
    }

    /**
     * {@link Dao}のconfigを指定しているDaoの実装クラスのインスタンスが取得できること、Daoには指定された{@link org.seasar.doma.jdbc.Config}が設定されていること
     * @throws Exception
     */
    @Test
    public void get_legacy() {
        TestLegacyDao1 dao1 = DomaDaoRepository.get(TestLegacyDao1.class);
        assertThat(dao1, instanceOf(TestLegacyDao1Impl.class));

        // Dao#configで指定したConfigが設定される
        assertThat(unwrapConfig(((TestLegacyDao1Impl) dao1).getConfig()), instanceOf(DomaConfig.class));

        TestLegacyDao2 dao2 = DomaDaoRepository.get(TestLegacyDao2.class);
        assertThat(dao2, instanceOf(TestLegacyDao2Impl.class));

        // Dao#configで指定したConfigが設定される
        assertThat(unwrapConfig(((TestLegacyDao2Impl) dao2).getConfig()), instanceOf(DomaTransactionNotSupportedConfig.class));
    }

    /**
     * 実装クラスがキャッシュされているため、同一のインスタンスが返却されること
     * @throws Exception
     */
    @Test
    public void get_legacy_cache() {
        TestLegacyDao1 dao1 = DomaDaoRepository.get(TestLegacyDao1.class);
        TestLegacyDao1 dao2 = DomaDaoRepository.get(TestLegacyDao1.class);
        assertThat(dao1, sameInstance(dao2));

        TestLegacyDao2 dao3 = DomaDaoRepository.get(TestLegacyDao2.class);
        TestLegacyDao2 dao4 = DomaDaoRepository.get(TestLegacyDao2.class);
        assertThat(dao3, sameInstance(dao4));
    }

    /**
     * {@link Dao}のconfigを指定していないDaoの場合、{@link DomaDaoRepository#get(Class, Config)}でDaoの実装クラスが使用する{@link Config}を指定できること
     * @throws Exception
     */
    @Test
    public void get_with_config() {
        TestDao dao1 = DomaDaoRepository.get(TestDao.class, DomaConfig.singleton());
        TestDao dao2 = DomaDaoRepository.get(TestDao.class, DomaConfig.singleton());
        // 同じインスタンス
        assertThat(dao1, sameInstance(dao2));
        assertThat(unwrapConfig(((TestDaoImpl) dao1).getConfig()), instanceOf(DomaConfig.class));

        TestDao dao3 = DomaDaoRepository.get(TestDao.class, DomaTransactionNotSupportedConfig.singleton());
        TestDao dao4 = DomaDaoRepository.get(TestDao.class, DomaTransactionNotSupportedConfig.singleton());
        // 同じインスタンス
        assertThat(dao3, sameInstance(dao4));
        // DomaDaoRepository.get(daoClass, Config)で指定したConfigが設定される
        assertThat(unwrapConfig(((TestDaoImpl) dao3).getConfig()), instanceOf(DomaTransactionNotSupportedConfig.class));
    }

    /**
     * {@link Dao}のconfigを指定していないDaoの場合、{@link DomaDaoRepository#get(Class, Config)}でDaoの実装クラスが使用する{@link Config}を指定できること
     * @throws Exception
     */
    @Test
    public void get_legacy_with_config1() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("implementation class is invalid. Do not specify config attribute for Dao annotation. class name = [" + TestLegacyDao1.class.getName() + ']');

        DomaDaoRepository.get(TestLegacyDao1.class, DomaTransactionNotSupportedConfig.singleton());
    }

    /**
     * {@link Dao}のconfigを指定していないDaoの場合、{@link DomaDaoRepository#get(Class, Config)}でDaoの実装クラスが使用する{@link Config}を指定できること
     * @throws Exception
     */
    @Test
    public void get_legacy_with_config2() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("implementation class is invalid. Do not specify config attribute for Dao annotation. class name = [" + TestLegacyDao2.class.getName() + ']');

        DomaDaoRepository.get(TestLegacyDao2.class, DomaConfig.singleton());
    }

    @Test
    public void get_undefined() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("implementation class is undefined. class name = [" + Undefined.class.getName() + ']');

        DomaDaoRepository.get(Undefined.class);

    }

    private Config unwrapConfig(Config config) {
        if (config instanceof RuntimeConfig runtimeConfig) {
            try {
                Field configField = RuntimeConfig.class.getDeclaredField("config");
                configField.setAccessible(true);
                return (Config) configField.get(runtimeConfig);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }

        return config;
    }

    public interface Undefined {}
}