package com.github.leeonky.jfactory.spec;

import com.github.leeonky.jfactory.*;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CustomizedBuildInClass {
    private FactorySet factorySet = new FactorySet();

    @Test
    void support_define_customized_factory_with_spec_and_mix_in() {
        assertThat(factorySet.toBuild(ABean.class).mixIn("int100", "hello").create())
                .hasFieldOrPropertyWithValue("content", "this is a bean")
                .hasFieldOrPropertyWithValue("stringValue", "hello")
                .hasFieldOrPropertyWithValue("intValue", 100);
    }

    @Test
    void should_call_type_base_construct_and_definition() {
        factorySet.factory(Bean.class).construct(arg -> new BeanSub()).define((arg, spec) -> {
            spec.property("intValue").value(50);
        });

        assertThat(factorySet.toBuild(ABean.class).create())
                .isInstanceOf(BeanSub.class)
                .hasFieldOrPropertyWithValue("intValue", 50);
    }

    @Test
    void support_define_customized_factory_and_build_through_name() {
        factorySet.define(ABean.class);

        assertThat(factorySet.toBuild("ABean").create())
                .hasFieldOrPropertyWithValue("content", "this is a bean");
    }

    @Test
    void should_raise_error_when_definition_or_mix_in_not_exist() {
        assertThrows(IllegalArgumentException.class, () -> factorySet.toBuild("ABean"));
    }

    @Test
    void support_create_nested_object_with_definition_name() {
        factorySet.define(ABean.class);

        assertThat(factorySet.type(Beans.class).property("bean(ABean).stringValue", "hello").create().getBean())
                .hasFieldOrPropertyWithValue("stringValue", "hello");
    }

    @Test
    void support_create_nested_object_with_definition_and_mix_id() {
        factorySet.define(ABean.class);

        assertThat(factorySet.type(BeansWrapper.class).property("beans.bean(int100 ABean).stringValue", "hello").create().getBeans().getBean())
                .hasFieldOrPropertyWithValue("stringValue", "hello")
                .hasFieldOrPropertyWithValue("content", "this is a bean")
                .hasFieldOrPropertyWithValue("intValue", 100);
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

    @Getter
    @Setter
    public static class BeanSub extends Bean {
    }

    public static class ABean extends Definition<Bean> {

        @Override
        public void define(Argument arg, Spec<Bean> spec) {
            spec.property("content").value("this is a bean");
        }

        @MixIn
        public void int100(Argument arg, Spec<Bean> spec) {
            spec.property("intValue").value(100);
        }

        @MixIn("hello")
        public void strHello(Argument arg, Spec<Bean> spec) {
            spec.property("stringValue").value("hello");
        }
    }
}
