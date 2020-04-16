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

    @Getter
    @Setter
    public static class Bean {
        private String content;
        private String stringValue;
        private int intValue;
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
