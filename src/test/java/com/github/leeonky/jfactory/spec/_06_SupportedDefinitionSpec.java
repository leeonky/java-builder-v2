package com.github.leeonky.jfactory.spec;

import com.github.leeonky.jfactory.Definition;
import com.github.leeonky.jfactory.FactorySet;
import com.github.leeonky.jfactory.MixIn;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class _06_SupportedDefinitionSpec {
    private FactorySet factorySet = new FactorySet();

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

    public static class ABean extends Definition<Bean> {

        @Override
        public void define() {
            spec().property("content").value("this is a bean");
        }

        @MixIn
        public void int100() {
            spec().property("intValue").value(100);
        }

        @MixIn("hello")
        public void strHello() {
            spec().property("stringValue").value("hello");
        }
    }

    @Nested
    class SpecifyValue {

        @Test
        void support_specify_value_in_definition() {
            factorySet.factory(Bean.class).define((arg, spec) ->
                    spec.property("content").value("hello"));

            assertThat(factorySet.create(Bean.class))
                    .hasFieldOrPropertyWithValue("content", "hello")
            ;
        }

        @Test
        void support_specify_value_supplier_in_definition() {
            factorySet.factory(Bean.class).define((arg, spec) ->
                    spec.property("content").valueSupplier(() -> "hello"));

            assertThat(factorySet.create(Bean.class))
                    .hasFieldOrPropertyWithValue("content", "hello")
            ;
        }
    }

    @Nested
    class SpecifyDefinition {

        @Test
        void support_specify_definition() {
            factorySet.factory(Beans.class).define((arg, spec) ->
                    spec.property("bean").supposeFrom(ABean.class));

            assertThat(factorySet.create(Beans.class).getBean())
                    .hasFieldOrPropertyWithValue("content", "this is a bean")
            ;
        }

        @Test
        void support_specify_definition_with_mix_in() {
            factorySet.factory(Beans.class).define((arg, spec) ->
                    spec.property("bean").supposeFromMixIn(ABean.class, ABean::int100));

            assertThat(factorySet.create(Beans.class).getBean())
                    .hasFieldOrPropertyWithValue("content", "this is a bean")
                    .hasFieldOrPropertyWithValue("intValue", 100);
        }

        @Test
        void support_specify_customized_builder_args() {
            factorySet.factory(Beans.class).define((arg, spec) ->
                    spec.property("bean").supposeFrom(ABean.class, builder -> builder.mixIn("int100")));

            assertThat(factorySet.create(Beans.class).getBean())
                    .hasFieldOrPropertyWithValue("content", "this is a bean")
                    .hasFieldOrPropertyWithValue("intValue", 100);
        }
    }

    @Nested
    class SpecifyType {

        @Test
        void support_specify_type() {
            factorySet.factory(Beans.class).define((arg, spec) ->
                    spec.property("bean").type(Bean.class));

            assertThat(factorySet.create(Beans.class).getBean())
                    .isInstanceOf(Bean.class)
            ;
        }

        @Test
        void support_specify_customized_builder_args() {
            factorySet.factory(Beans.class).define((arg, spec) ->
                    spec.property("bean").type(Bean.class, builder -> builder.property("intValue", 100)));

            assertThat(factorySet.create(Beans.class).getBean())
                    .hasFieldOrPropertyWithValue("intValue", 100);
        }
    }
}
