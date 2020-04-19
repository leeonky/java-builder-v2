package com.github.leeonky.jfactory.spec;

import com.github.leeonky.jfactory.Builder;
import com.github.leeonky.jfactory.DataRepository;
import com.github.leeonky.jfactory.FactorySet;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class _03_CreateWithRepo {
    private FactorySet factorySet = new FactorySet();

    @Test
    void save_in_repo_after_create() {
        Builder<Bean> builder = factorySet.type(Bean.class).property("stringValue", "hello").property("intValue", 100);

        Bean created = builder.create();

        assertThat(builder.query()).isEqualTo(created);

        factorySet.clearRepo();

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
        Beans notMatched = factorySet.type(Beans.class).create();

        Beans beans = factorySet.type(BeansWrapper.class).property("beans.bean.stringValue", "hello").create().getBeans();
        assertThat(beans).isNotEqualTo(notMatched);
        assertThat(beans.getBean())
                .hasFieldOrPropertyWithValue("stringValue", "hello");
    }

    @Test
    void support_use_customized_repo() {
        MyDataRepository dataRepository = new MyDataRepository();
        factorySet = new FactorySet(dataRepository);

        assertThat(factorySet.type(Bean.class).create()).isEqualTo(dataRepository.object);
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

    @Getter
    @Setter
    public static class BeansWrapper {
        private Beans beans;
    }

    private static class MyDataRepository implements DataRepository {
        public Object object;

        @Override
        public void save(Object object) {
            this.object = object;
        }

        @Override
        public <T> Collection<T> queryAll(Class<T> type) {
            return null;
        }

        @Override
        public void clear() {
        }
    }
}
