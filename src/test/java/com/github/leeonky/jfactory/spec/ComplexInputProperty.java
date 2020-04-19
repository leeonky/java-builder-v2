package com.github.leeonky.jfactory.spec;

import com.github.leeonky.jfactory.FactorySet;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ComplexInputProperty {
    private FactorySet factorySet = new FactorySet();

    @Test
    void support_specify_multi_properties_in_nested_property_query() {
        factorySet.type(BeansWrapper.class)
                .property("beans.bean.content", "hello").create();

        BeansWrapper wrapper = factorySet.type(BeansWrapper.class)
                .property("beans", factorySet.type(Beans.class)
                        .property("bean", factorySet.type(Bean.class)
                                .property("content", "hello")
                                .property("intValue", 100)
                                .create())
                        .create())
                .create();

        assertThat(factorySet.type(BeansWrapper.class)
                .property("beans.bean.content", "hello")
                .property("beans.bean.intValue", 100).queryAll())
                .hasSize(1)
                .containsOnly(wrapper);
    }

    @Test
    void support_specify_multi_properties_in_nested_property_create() {
        Bean bean = factorySet.type(Beans.class)
                .property("bean.content", "hello")
                .property("bean.intValue", 100)
                .create().getBean();

        assertThat(bean)
                .hasFieldOrPropertyWithValue("content", "hello")
                .hasFieldOrPropertyWithValue("intValue", 100)
        ;
    }

    @Getter
    @Setter
    public static class Bean {
        private String content;
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
}

