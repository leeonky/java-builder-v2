package com.github.leeonky.jfactory.spec;

import com.github.leeonky.jfactory.Definition;
import com.github.leeonky.jfactory.FactorySet;
import com.github.leeonky.jfactory.MixIn;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

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

    @Getter
    @Setter
    public static class Father {
        private Son son;
    }

    @Getter
    @Setter
    public static class Son {
        private Father father;
        private String name;
    }

    @Getter
    @Setter
    public static class Table {
        private List<Row> rows;
    }

    @Getter
    @Setter
    public static class Row {
        private Table table;
        private int value;
    }

    public static class ATable extends Definition<Table> {

        @Override
        public void define() {
            spec().property("rows[0]").type(Row.class, builder ->
                    builder.propertySpec("table", (arg, pSpec) ->
                            pSpec.supplier(argument().willGetCurrent())).property("value", 100)
            );
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
                    spec.property("content").supplier(() -> "hello"));

            assertThat(factorySet.create(Bean.class))
                    .hasFieldOrPropertyWithValue("content", "hello")
            ;
        }

        @Test
        void support_specify_current_object_in_nested_property() {
            factorySet.factory(Father.class).define((argument, spec) ->
                    spec.property("son").type(Son.class, sonBuilder ->
                            sonBuilder.propertySpec("father", ((argument1, propertySpec) -> propertySpec.supplier(argument.willGetCurrent())))
                    ));

            Father father = factorySet.create(Father.class);

            assertThat(father.getSon())
                    .hasFieldOrPropertyWithValue("father", father);
        }
    }

    @Nested
    class SpecifyDefinition {

        @Test
        void support_specify_definition() {
            factorySet.factory(Beans.class).define((arg, spec) ->
                    spec.property("bean").from(ABean.class));

            assertThat(factorySet.create(Beans.class).getBean())
                    .hasFieldOrPropertyWithValue("content", "this is a bean")
            ;
        }

        @Test
        void support_specify_definition_with_mix_in() {
            factorySet.factory(Beans.class).define((arg, spec) ->
                    spec.property("bean").fromMixIn(ABean.class, ABean::int100));

            assertThat(factorySet.create(Beans.class).getBean())
                    .hasFieldOrPropertyWithValue("content", "this is a bean")
                    .hasFieldOrPropertyWithValue("intValue", 100);
        }

        @Test
        void support_specify_customized_builder_args() {
            factorySet.factory(Beans.class).define((arg, spec) ->
                    spec.property("bean").from(ABean.class, builder -> builder.mixIn("int100")));

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

    @Nested
    class CollectionProperty {

        @Test
        void support_define_collection_element_spec() {
            Table table = factorySet.createFrom(ATable.class);

            assertThat(table.getRows())
                    .hasSize(1);

            assertThat(table.getRows().get(0))
                    .hasFieldOrPropertyWithValue("table", table)
                    .hasFieldOrPropertyWithValue("value", 100);
        }
    }
}
