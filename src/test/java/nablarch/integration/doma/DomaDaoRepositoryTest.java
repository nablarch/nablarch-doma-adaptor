package nablarch.integration.doma;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * {@link DomaDaoRepository}のテストクラス。
 */
public class DomaDaoRepositoryTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    /**
     * 実装クラスが取得できること
     * @throws Exception
     */
    @Test
    public void get() throws Exception {
        TestDao dao = DomaDaoRepository.get(TestDao.class);
        assertThat(dao, instanceOf(TestDaoImpl.class));
    }

    /**
     * 実装クラスがキャッシュされているため、同一のインスタンスが返却されること
     * @throws Exception
     */
    @Test
    public void get_cache() throws Exception {
        TestDao dao1 = DomaDaoRepository.get(TestDao.class);
        TestDao dao2 = DomaDaoRepository.get(TestDao.class);
        assertThat(dao1, sameInstance(dao2));
    }

    @Test
    public void get_undefined() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("implementation class is undefined. class name = [" + Undefined.class.getName() + ']');

        DomaDaoRepository.get(Undefined.class);

    }

    public interface TestDao {}

    public static class TestDaoImpl implements TestDao {}

    public interface Undefined {}
}