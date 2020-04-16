package com.github.leeonky.jfactory.spec;

import com.github.leeonky.jfactory.*;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomizedBuildInDefinition {
    private FactorySet factorySet = new FactorySet();

    @Test
    void support_define_customized_factory_with_spec_and_mix_in() {
        assertThat(factorySet.toBuild(一个Bean.class).mixIn("int100", "hello").create())
                .hasFieldOrPropertyWithValue("content", "this is a bean")
                .hasFieldOrPropertyWithValue("stringValue", "hello")
                .hasFieldOrPropertyWithValue("intValue", 100);
    }

    @Test
    void should_call_type_base_construct_and_definition() {
        factorySet.factory(Bean.class).construct(arg -> new BeanSub()).define((arg, spec) -> {
            spec.property("intValue").value(50);
        });

        assertThat(factorySet.toBuild(一个Bean.class).create())
                .isInstanceOf(BeanSub.class)
                .hasFieldOrPropertyWithValue("intValue", 50);
    }

    @Test
    void support_define_customized_factory_and_build_through_name() {
        factorySet.define(一个Bean.class);

        assertThat(factorySet.toBuild("一个Bean").create())
                .hasFieldOrPropertyWithValue("content", "this is a bean");
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
    public static class BeanSub extends Bean {
    }

    public static class 一个Bean extends Definition<Bean> {

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
