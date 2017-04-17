package nablarch.integration.doma.batch.ee.listener.integration;

@org.seasar.doma.Entity
@org.seasar.doma.Table(name = "TEST_ENTITY")
public class TestDomaEntity {

    public TestDomaEntity() {
    }

    public TestDomaEntity(String name) {
        this.name = name;
    }

    @org.seasar.doma.Id
    @org.seasar.doma.Column(name = "NAME")
    public String name;
}
