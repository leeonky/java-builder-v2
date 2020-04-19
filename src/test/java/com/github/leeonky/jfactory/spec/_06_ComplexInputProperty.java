package com.github.leeonky.jfactory.spec;

import com.github.leeonky.jfactory.*;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class _06_ComplexInputProperty {
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

    public static class AnotherBean extends Definition<Bean> {
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

//            assertThat(factorySet.type(BeansWrapper.class)
//                    .property("beans.bean.stringValue", "hello")
//                    .property("beans.bean(ABean).intValue", 100)
//                    .create().getBeans().getBean())
//                    .hasFieldOrPropertyWithValue("content", "this is a bean")
//                    .hasFieldOrPropertyWithValue("stringValue", "hello")
//                    .hasFieldOrPropertyWithValue("intValue", 100)
//            ;
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
    }

    @Nested
    class CollectionProperty {

        @Nested
        class ListProperty {

            @Test
            void support_element_in_build() {
                BeanCollection beanCollection = factorySet.type(BeanCollection.class)
                        .property("list[0].stringValue", "hello")
//                        .property("list[0].intValue", 100)
//                        .property("list[1].stringValue", "world")
//                        .property("list[1].intValue", 200)
                        .create();

//                assertThat(beanCollection.getList().size()).isEqualTo(2);

                assertThat(beanCollection.getList().get(0))
                        .hasFieldOrPropertyWithValue("stringValue", "hello")
//                        .hasFieldOrPropertyWithValue("intValue", 100)
                ;
//                assertThat(beanCollection.getList().get(1))
//                        .hasFieldOrPropertyWithValue("stringValue", "world")
//                        .hasFieldOrPropertyWithValue("intValue", 200)
//                ;
            }
        }
    }
}
