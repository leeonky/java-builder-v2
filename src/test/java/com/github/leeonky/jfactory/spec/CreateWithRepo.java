package com.github.leeonky.jfactory.spec;

import com.github.leeonky.jfactory.Builder;
import com.github.leeonky.jfactory.FactorySet;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CreateWithRepo {
    private FactorySet factorySet = new FactorySet();

    @Test
    void save_in_repo_after_create() {
        Builder<Bean> builder = factorySet.type(Bean.class).property("stringValue", "hello");

        Bean created = builder.create();

        assertThat(builder.query()).isEqualTo(created);

        factorySet.getDataRepository().clear();

        assertThat(builder.query()).isNull();
    }

    @Test
    void support_queried_object_with_nested_specified_property() {
        Bean helloBean = factorySet.type(Bean.class).property("stringValue", "hello").create();

        assertThat(factorySet.type(Beans.class).property("bean.stringValue", "hello").create())
                .hasFieldOrPropertyWithValue("bean", helloBean);
    }

    @Test
    void should_create_nested_with_property_and_value_when_query_return_empty() {
        Beans beans = factorySet.type(Beans.class).property("bean.stringValue", "hello").create();

        assertThat(beans.getBean())
                .hasFieldOrPropertyWithValue("stringValue", "hello");
    }

    @Getter
    @Setter
    public static class Bean {
        private String stringValue;
        private int intValue;
    }

    @Getter
    @Setter
    public static class Beans {
        private Bean bean;
    }
}
