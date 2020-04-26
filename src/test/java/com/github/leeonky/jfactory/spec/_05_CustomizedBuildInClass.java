package com.github.leeonky.jfactory.spec;

import com.github.leeonky.jfactory.Definition;
import com.github.leeonky.jfactory.FactorySet;
import com.github.leeonky.jfactory.MixIn;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class _05_CustomizedBuildInClass {
    private FactorySet factorySet = new FactorySet();

    @Test
    void support_define_customized_factory_with_spec_and_mix_in() {
        assertThat(factorySet.toBuild(ABean.class).mixIn("int100", "hello").create())
                .hasFieldOrPropertyWithValue("content", "this is a bean")
                .hasFieldOrPropertyWithValue("stringValue", "hello")
                .hasFieldOrPropertyWithValue("intValue", 100);
    }

    @Test
    void support_use_mix_in_in_java_code() {
        assertThat(factorySet.toBuild(ABean.class, mixIn -> mixIn.int100().strHello()).create())
                .hasFieldOrPropertyWithValue("content", "this is a bean")
                .hasFieldOrPropertyWithValue("stringValue", "hello")
                .hasFieldOrPropertyWithValue("intValue", 100);
    }

    @Test
    void should_call_type_base_construct_and_definition() {
        factorySet.factory(Bean.class).construct(arg -> new BeanSub()).define((arg, spec) -> {
            spec.property("intValue").value(50);
        });

        assertThat(factorySet.createFrom(ABean.class))
                .isInstanceOf(BeanSub.class)
                .hasFieldOrPropertyWithValue("intValue", 50);
    }

    @Test
    void support_define_customized_factory_and_build_through_name() {
        factorySet.define(ABean.class);

        assertThat((Bean) factorySet.createFrom("ABean"))
                .hasFieldOrPropertyWithValue("content", "this is a bean");
    }

    @Test
    void should_raise_error_when_definition_or_mix_in_not_exist() {
        assertThrows(IllegalArgumentException.class, () -> factorySet.toBuild("ABean"));
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

    public static class ABean extends Definition<Bean> {

        @Override
        public void define() {
            spec().property("content").value("this is a bean");
        }

        @MixIn
        public ABean int100() {
            spec().property("intValue").value(100);
            return this;
        }

        @MixIn("hello")
        public ABean strHello() {
            spec().property("stringValue").value("hello");
            return this;
        }
    }
}
