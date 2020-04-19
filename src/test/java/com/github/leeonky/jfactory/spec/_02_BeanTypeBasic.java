package com.github.leeonky.jfactory.spec;

import com.github.leeonky.jfactory.FactorySet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

class _02_BeanTypeBasic {
    private FactorySet factorySet = new FactorySet();

    @Test
    void default_creation() {
        assertThat(factorySet.create(Bean.class))
                .hasFieldOrPropertyWithValue("stringValue", "stringValue1")
                .hasFieldOrPropertyWithValue("intValue", 1)
        ;

        assertThat(factorySet.create(Bean.class))
                .hasFieldOrPropertyWithValue("stringValue", "stringValue2")
                .hasFieldOrPropertyWithValue("intValue", 2)
        ;
    }

    @Test
    void support_specify_properties() {
        assertThat(factorySet.type(Bean.class).property("stringValue", "hello").create())
                .hasFieldOrPropertyWithValue("stringValue", "hello");

        assertThat(factorySet.type(Bean.class).properties(new HashMap<String, Object>() {{
            put("stringValue", "hello");
            put("intValue", 100);
        }}).create())
                .hasFieldOrPropertyWithValue("stringValue", "hello")
                .hasFieldOrPropertyWithValue("intValue", 100);
    }

    @Test
    void support_customized_constructor() {
        factorySet.factory(BeanWithNoDefaultConstructor.class).construct(arg -> new BeanWithNoDefaultConstructor("hello", 100));

        assertThat(factorySet.type(BeanWithNoDefaultConstructor.class).create())
                .hasFieldOrPropertyWithValue("stringValue", "hello")
                .hasFieldOrPropertyWithValue("intValue", 100);
    }

    @Test
    void support_specify_params() {
        factorySet.factory(BeanWithNoDefaultConstructor.class).construct(arg -> new BeanWithNoDefaultConstructor(arg.param("p"), arg.getSequence()));

        assertThat(factorySet.type(BeanWithNoDefaultConstructor.class).param("p", "hello").create())
                .hasFieldOrPropertyWithValue("stringValue", "hello")
                .hasFieldOrPropertyWithValue("intValue", 1);
    }

    @Getter
    @Setter
    public static class Bean {
        private String stringValue;
        private int intValue;
    }

    @Getter
    @AllArgsConstructor
    public static class BeanWithNoDefaultConstructor {
        private final String stringValue;
        private int intValue;
    }
}
