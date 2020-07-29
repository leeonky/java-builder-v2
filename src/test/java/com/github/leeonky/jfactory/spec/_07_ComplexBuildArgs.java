package com.github.leeonky.jfactory.spec;

import com.github.leeonky.jfactory.Builder;
import com.github.leeonky.jfactory.Definition;
import com.github.leeonky.jfactory.FactorySet;
import com.github.leeonky.jfactory.MixIn;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class _07_ComplexBuildArgs {
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

    @Getter
    @Setter
    public static class BeansPair {
        private Beans beans1, beans2;
    }

    @Getter
    @Setter
    public static class BeanCollection {
        private Bean[] array;
        private List<Bean> list;
        private Set<Bean> set;
    }

    @Getter
    @Setter
    public static class BeansWrapper {
        private Beans beans;
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Strings {
        public String[] strings;
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

    public static class AnotherBean extends Definition<Bean> {

        @Override
        public void define() {
            spec().property("content").value("this is another bean");
        }

        @MixIn
        public void int200() {
            spec().property("intValue").value(200);
        }

    }

    public static class ABeans extends Definition<Beans> {
        @Override
        public void define() {
            spec().property("bean").fromMixIn(ABean.class, ABean::int100);
        }
    }

    @Nested
    class MergeProperty {

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
            BeansWrapper beansWrapper = factorySet.type(BeansWrapper.class)
                    .property("beans.bean.content", "hello")
                    .property("beans.bean.intValue", 100)
                    .create();

            assertThat(beansWrapper.getBeans().getBean())
                    .hasFieldOrPropertyWithValue("content", "hello")
                    .hasFieldOrPropertyWithValue("intValue", 100)
            ;
        }

        @Test
        void should_raise_error_when_property_has_different_definition() {
            factorySet.define(ABean.class).define(AnotherBean.class);

            assertThrows(IllegalArgumentException.class, () -> factorySet.type(BeansWrapper.class)
                    .property("beans.bean(ABean).content", "hello")
                    .property("beans.bean(AnotherBean).intValue", 100)
                    .create());
        }

        @Test
        void support_merge_with_definition_and_null_definition() {
            factorySet.define(ABean.class).define(AnotherBean.class);

            assertThat(factorySet.type(BeansWrapper.class)
                    .property("beans.bean(ABean).stringValue", "hello")
                    .property("beans.bean.intValue", 100)
                    .create().getBeans().getBean())
                    .hasFieldOrPropertyWithValue("content", "this is a bean")
                    .hasFieldOrPropertyWithValue("stringValue", "hello")
                    .hasFieldOrPropertyWithValue("intValue", 100)
            ;

            assertThat(factorySet.type(BeansWrapper.class)
                    .property("beans.bean.stringValue", "hello")
                    .property("beans.bean(ABean).intValue", 100)
                    .create().getBeans().getBean())
                    .hasFieldOrPropertyWithValue("content", "this is a bean")
                    .hasFieldOrPropertyWithValue("stringValue", "hello")
                    .hasFieldOrPropertyWithValue("intValue", 100)
            ;
        }

        @Test
        void should_raise_error_when_property_has_different_mix_in() {
            factorySet.define(ABean.class);

            assertThrows(IllegalArgumentException.class, () -> factorySet.type(BeansWrapper.class)
                    .property("beans.bean(hello ABean).content", "xxx")
                    .property("beans.bean(int100 ABean).intValue", 10)
                    .create());
        }

        @Test
        void support_merge_with_mix_in_and_empty_mix_in() {
            factorySet.define(ABean.class);

            assertThat(factorySet.type(BeansWrapper.class)
                    .property("beans.bean(hello ABean).content", "any")
                    .property("beans.bean(ABean).intValue", 10)
                    .create().getBeans().getBean())
                    .hasFieldOrPropertyWithValue("stringValue", "hello")
                    .hasFieldOrPropertyWithValue("intValue", 10)
            ;

            assertThat(factorySet.type(BeansWrapper.class)
                    .property("beans.bean(ABean).intValue", 10)
                    .property("beans.bean(hello ABean).content", "any")
                    .create().getBeans().getBean())
                    .hasFieldOrPropertyWithValue("stringValue", "hello")
                    .hasFieldOrPropertyWithValue("intValue", 10)
            ;
        }

        @Test
        void should_not_merge_when_not_specfiy_properties() {
            factorySet.factory(BeansPair.class).define((argument, beanSpec) -> {
                beanSpec.property("beans1").type(Beans.class);
                beanSpec.property("beans2").type(Beans.class);
            });

            BeansPair beansPair = factorySet.create(BeansPair.class);

            assertThat(beansPair.beans1).isNotEqualTo(beansPair.beans2);
        }
    }

    @Nested
    class CollectionProperty {

        @Test
        void default_element_value_should_generated_from_value_factory() {
            assertThat(factorySet.type(Strings.class).property("strings[1]", "").create().getStrings()[0])
                    .isInstanceOf(String.class);
        }

        @Test
        void support_element_in_build() {
            BeanCollection beanCollection = factorySet.type(BeanCollection.class)
                    .property("list[0].stringValue", "hello")
                    .property("list[0].intValue", 100)
                    .property("list[1].stringValue", "world")
                    .property("list[1].intValue", 200)
                    .create();

            assertThat(beanCollection.getList().size()).isEqualTo(2);

            assertThat(beanCollection.getList().get(0))
                    .hasFieldOrPropertyWithValue("stringValue", "hello")
                    .hasFieldOrPropertyWithValue("intValue", 100)
            ;
            assertThat(beanCollection.getList().get(1))
                    .hasFieldOrPropertyWithValue("stringValue", "world")
                    .hasFieldOrPropertyWithValue("intValue", 200)
            ;
        }

        @Test
        void support_element_in_query() {
            Builder<BeanCollection> builder1 = factorySet.type(BeanCollection.class)
                    .property("list[0].stringValue", "hello")
                    .property("list[0].intValue", 100)
                    .property("list[1].stringValue", "world")
                    .property("list[1].intValue", 200);

            Builder<BeanCollection> builder2 = factorySet.type(BeanCollection.class)
                    .property("list[0].stringValue", "goodbye")
                    .property("list[0].intValue", 300)
                    .property("list[1].stringValue", "world")
                    .property("list[1].intValue", 400);

            BeanCollection beanCollection1 = builder1.create();
            BeanCollection beanCollection2 = builder2.create();

            assertThat(builder1.queryAll()).containsOnly(beanCollection1);
            assertThat(builder2.queryAll()).containsOnly(beanCollection2);
        }

        @Test
        void also_support_definition_and_mix_in_in_element() {
            factorySet.define(ABean.class);

            BeanCollection beanCollection = factorySet.type(BeanCollection.class)
                    .property("list[0](int100 ABean).stringValue", "hello")
                    .create();

            assertThat(beanCollection.getList().get(0))
                    .hasFieldOrPropertyWithValue("content", "this is a bean")
                    .hasFieldOrPropertyWithValue("stringValue", "hello")
                    .hasFieldOrPropertyWithValue("intValue", 100)
            ;

            assertThat(factorySet.type(BeanCollection.class)
                    .property("list[0](int100 ABean).stringValue", "hello").queryAll())
                    .containsOnly(beanCollection);
        }

        @Test
        void support_different_type_in_each_element() {
            Bean bean = new Bean();
            Builder<BeanCollection> builder = factorySet.type(BeanCollection.class)
                    .property("list[0].stringValue", "hello")
                    .property("list[1]", bean);

            BeanCollection beanCollection = builder.create();

            assertThat(beanCollection.getList().get(0))
                    .hasFieldOrPropertyWithValue("stringValue", "hello")
            ;

            assertThat(beanCollection.getList().get(1))
                    .isEqualTo(bean)
            ;

            assertThat(builder.queryAll()).containsOnly(beanCollection);
        }
    }

    @Nested
    class IntentlyCreate {

        @Test
        void create_query_intently() {
            factorySet.type(Bean.class)
                    .property("stringValue", "hello")
                    .create();

            assertThat(factorySet.type(Bean.class)
                    .property("stringValue!", "hello")
                    .queryAll()).isEmpty();
        }

        @Test
        void create_nested_object_intently() {
            Bean bean = factorySet.type(Bean.class)
                    .property("stringValue", "hello")
                    .property("intValue", 100)
                    .create();

            assertThat(factorySet.type(Beans.class)
                    .property("bean!.stringValue", "hello")
                    .create().getBean()).isNotEqualTo(bean);

            assertThat(factorySet.type(BeanCollection.class)
                    .property("list[0]!.stringValue", "hello")
                    .property("list[0].intValue", 100).create().getList().get(0)).isNotEqualTo(bean);

            assertThat(factorySet.type(BeanCollection.class)
                    .property("list[0].stringValue", "hello")
                    .property("list[0]!.intValue", 100).create().getList().get(0)).isNotEqualTo(bean);
        }

        @Test
        void support_create_object_only_with_definition_and_mix_in_and_ignore_value() {
            factorySet.define(ABean.class);

            assertThat(factorySet.type(Beans.class)
                    .property("bean(int100 ABean)!", "")
                    .create().getBean())
                    .hasFieldOrPropertyWithValue("content", "this is a bean")
                    .hasFieldOrPropertyWithValue("intValue", 100)
            ;

            assertThat(factorySet.type(Beans.class)
                    .property("bean(ABean)!", "")
                    .create().getBean())
                    .hasFieldOrPropertyWithValue("content", "this is a bean")
            ;
        }
    }

    @Nested
    class UniqCreation {

        @Test
        void uniq_build_in_nested_duplicated_object_creation() {
            BeansPair beansPair = factorySet.type(BeansPair.class)
                    .property("beans1.bean.stringValue", "hello")
                    .property("beans2.bean.stringValue", "hello")
                    .create();

            assertThat(factorySet.type(Bean.class).queryAll()).hasSize(1);
            assertThat(beansPair.beans1).isEqualTo(beansPair.beans2);
        }
    }

    @Nested
    class OverrideDefinition {

        @Test
        void should_use_nested_definition_in_definition_when_property_not_specify_definition() {
            assertThat(factorySet.toBuild(ABeans.class).property("bean.stringValue", "hello").create().getBean())
                    .hasFieldOrPropertyWithValue("stringValue", "hello")
                    .hasFieldOrPropertyWithValue("content", "this is a bean")
                    .hasFieldOrPropertyWithValue("intValue", 100);
        }

        @Test
        void should_use_specified_definition_when_property_specify_definition() {
            factorySet.define(AnotherBean.class);

            assertThat(factorySet.toBuild(ABeans.class).property("bean(int200 AnotherBean).stringValue", "hello").create().getBean())
                    .hasFieldOrPropertyWithValue("stringValue", "hello")
                    .hasFieldOrPropertyWithValue("content", "this is another bean")
                    .hasFieldOrPropertyWithValue("intValue", 200);
        }
    }
}
