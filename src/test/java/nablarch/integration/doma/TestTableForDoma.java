package nablarch.integration.doma;

/**
 *
 */
@org.seasar.doma.Entity
@org.seasar.doma.Table(name = "TEST_TABLE")
public class TestTableForDoma {

    public TestTableForDoma() {
    }

    public TestTableForDoma(String name) {
        this.name = name;
    }

    @org.seasar.doma.Id
    @org.seasar.doma.Column(name = "NAME")
    public String name;
}
