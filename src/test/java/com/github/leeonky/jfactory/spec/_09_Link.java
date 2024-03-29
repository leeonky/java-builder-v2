package com.github.leeonky.jfactory.spec;

import com.github.leeonky.jfactory.FactorySet;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class _09_Link {

    private FactorySet factorySet = new FactorySet();

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Bean {
        public String str1, str2, str3, str4;
        public String s1, s2, s3, s4;
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Beans {
        public Bean[] beans;
        public Bean bean;
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class BeanWrapper {
        public Bean bean;
        public String str;
        public Bean another;
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Strings {
        public String[] strings;
        public String value;
    }

    @Nested
    class FlattenLink {

        @Test
        void producer_link_producer() {
            factorySet.factory(Bean.class).define((argument, spec) -> {
                spec.link("str1", "str2");
            });

            Bean bean = factorySet.create(Bean.class);

            assertThat(bean.str1).isEqualTo(bean.str2);
        }

        @Test
        void should_use_input_property_in_link() {
            factorySet.factory(Bean.class).define((argument, spec) -> {
                spec.link("str1", "str2");
            });

            Bean bean = factorySet.type(Bean.class).property("str2", "string").create();

            assertThat(bean.str1).isEqualTo("string");
            assertThat(bean.str2).isEqualTo("string");
        }

        private void assertLink(String property, String value, String... properties) {
            Bean bean = factorySet.type(Bean.class).property(property, value).create();
            for (String p : properties)
                assertThat(bean).hasFieldOrPropertyWithValue(p, value);
        }

        @Test
        void support_link_link_producer() {
            factorySet.factory(Bean.class).define((argument, spec) -> {
                spec.link("str1", "str2");
                spec.link("str2", "str3");
            });

            assertLink("str1", "string", "str1", "str2", "str3");
            assertLink("str2", "string", "str1", "str2", "str3");
            assertLink("str3", "string", "str1", "str2", "str3");
        }

        @Test
        void support_producer_link_link() {
            factorySet.factory(Bean.class).define((argument, spec) -> {
                spec.link("str1", "str2");
                spec.link("str3", "str2");
            });

            assertLink("str1", "string", "str1", "str2", "str3");
            assertLink("str2", "string", "str1", "str2", "str3");
            assertLink("str3", "string", "str1", "str2", "str3");
        }

        @Test
        void support_link_link_link() {
            factorySet.factory(Bean.class).define((argument, spec) -> {
                spec.link("str1", "str2");
                spec.link("str3", "str4");
                spec.link("str2", "str3");
            });

            Bean bean = factorySet.create(Bean.class);

            assertThat(bean.str1).isEqualTo(bean.str2);
            assertThat(bean.str2).isEqualTo(bean.str3);
            assertThat(bean.str3).isEqualTo(bean.str4);
        }

        @Test
        void support_link_with_bean_object() {
            factorySet.factory(BeanWrapper.class).define((argument, spec) -> {
                spec.link("bean", "another");
            });

            Bean bean = new Bean();
            assertThat(factorySet.type(BeanWrapper.class).property("another", bean).create())
                    .hasFieldOrPropertyWithValue("bean", bean);
        }
    }

    @Nested
    class CollectionLink {

        @Test
        void link_property() {
            factorySet.factory(Beans.class).define((argument, spec) -> {
                spec.property("beans[0]").type(Bean.class);
                spec.property("beans[1]").type(Bean.class);
                spec.property("beans[2]").type(Bean.class);
                spec.link("beans[0].str1", "beans[1].str1", "beans[2].str1");
            });

            Beans beans = factorySet.create(Beans.class);

            assertThat(beans.beans[0].str1).isEqualTo(beans.beans[1].str1);
            assertThat(beans.beans[1].str1).isEqualTo(beans.beans[2].str1);
        }

        @Test
        void support_link_with_bean_object() {
            factorySet.factory(Beans.class).define((argument, spec) -> {
                spec.link("beans[0]", "beans[1]");
                spec.link("beans[1]", "bean");
            });

            Bean bean = new Bean();
            Beans beans = factorySet.type(Beans.class).property("bean", bean).create();
            assertThat(beans.getBeans())
                    .containsExactly(bean, bean);
        }

        @Test
        void support_link_with_value_type_collection_element_and_property() {
            factorySet.factory(Strings.class).define((argument, spec) -> {
                spec.property("value").value("hello");
                spec.link("strings[0]", "value");
            });

            Strings strings = factorySet.create(Strings.class);
            assertThat(strings.getStrings()[0]).isEqualTo(strings.getValue()).isEqualTo("hello");
        }
    }

    @Nested
    class NestedLink {

        @Test
        void support_nest_object_link() {
            factorySet.factory(Bean.class).define((argument, spec) -> spec.link("str1", "str2"));

            factorySet.factory(BeanWrapper.class).define((argument, spec) -> {
                spec.property("bean").type(Bean.class);
                spec.link("str", "bean.str1");
            });

            BeanWrapper beanWrapper = factorySet.create(BeanWrapper.class);

            assertThat(beanWrapper.getBean().getStr1()).isEqualTo(beanWrapper.getBean().getStr2());
            assertThat(beanWrapper.getBean().getStr1()).isEqualTo(beanWrapper.getStr());
        }
    }

    @Nested
    class ProducerNotValid {

        @Test
        void should_ignore_link_when_producer_not_valid() {
            factorySet.factory(BeanWrapper.class).define((argument, spec) -> {
                spec.link("str", "bean.str1");
            });

            BeanWrapper beanWrapper = factorySet.create(BeanWrapper.class);

            assertThat(beanWrapper.getBean()).isNull();
            assertThat(beanWrapper.getStr()).isInstanceOf(String.class);
        }
    }

    @Nested
    class ProducerPriority {

        @Test
        void use_property_value_in_link() {
            factorySet.factory(Bean.class).define((argument, spec) -> {
                spec.link("str1", "str2", "str3", "str4");
                spec.property("str2").dependsOn("s1", obj -> obj);
                spec.property("str3").value("hello");
            });

            assertThat(factorySet.type(Bean.class).property("str1", "input").create())
                    .hasFieldOrPropertyWithValue("str1", "input")
                    .hasFieldOrPropertyWithValue("str2", "input")
                    .hasFieldOrPropertyWithValue("str3", "input")
                    .hasFieldOrPropertyWithValue("str4", "input")
            ;
        }

        @Test
        void should_raise_error_when_has_ambiguous() {
            factorySet.factory(Bean.class).define((argument, spec) -> {
                spec.link("str1", "str2");
            });

            assertThrows(RuntimeException.class, () -> factorySet.type(Bean.class)
                    .property("str1", "input1")
                    .property("str2", "input2")
                    .create());
        }

        @Test
        void use_dependency_value_in_link() {
            factorySet.factory(Bean.class).define((argument, spec) -> {
                spec.link("str1", "str3", "str2", "str4");
                spec.property("str2").dependsOn("s1", obj -> obj);
                spec.property("str3").value("hello");
            });

            assertThat(factorySet.type(Bean.class).property("s1", "input").create())
                    .hasFieldOrPropertyWithValue("str1", "input")
                    .hasFieldOrPropertyWithValue("str2", "input")
                    .hasFieldOrPropertyWithValue("str3", "input")
                    .hasFieldOrPropertyWithValue("str4", "input")
            ;
        }

        @Test
        void use_suggested_value_in_link() {
            factorySet.factory(Bean.class).define((argument, spec) -> {
                spec.link("str1", "str2", "str3");
                spec.property("str3").value("hello");
            });

            assertThat(factorySet.type(Bean.class).create())
                    .hasFieldOrPropertyWithValue("str1", "hello")
                    .hasFieldOrPropertyWithValue("str2", "hello")
                    .hasFieldOrPropertyWithValue("str3", "hello")
            ;
        }
    }

    @Nested
    class LinkToNoProducerField {

        @Test
        void should_return_property_value_when_parent_object_is_property() {
            factorySet.factory(BeanWrapper.class).define((argument, spec) -> {
                spec.link("bean.str1", "str");
            });

            assertThat(factorySet.type(BeanWrapper.class).property("bean", new Bean().setStr1("hello")).create().getStr()).isEqualTo("hello");

            assertThat(factorySet.type(BeanWrapper.class).property("bean", null).create().getStr()).isInstanceOf(String.class);
        }

        @Test
        void should_use_suggested_value_when_parent_object_is_suggested_value() {
            factorySet.factory(BeanWrapper.class).define((argument, spec) -> {
                spec.property("bean").value(new Bean().setStr1("hello"));
                spec.link("bean.str1", "str");
            });

            assertThat(factorySet.type(BeanWrapper.class).create().getStr()).isEqualTo("hello");

            factorySet.factory(BeanWrapper.class).define((argument, spec) -> {
                spec.property("bean").value(null);
                spec.link("bean.str1", "str");
            });

            assertThat(factorySet.type(BeanWrapper.class).create().getStr()).isInstanceOf(String.class);
        }

        @Test
        void should_use_as_suggested_value_when_parent_object_is_link() {
            factorySet.factory(BeanWrapper.class).define((argument, spec) -> {
                spec.link("bean", "another");
                spec.link("bean.str1", "str");
            });

            assertThat(factorySet.type(BeanWrapper.class).property("another", new Bean().setStr1("hello")).create().getStr()).isEqualTo("hello");
        }
    }

    @Nested
    class Limitation {

        //        @Test
        void linked_producer_was_changed_by_other_link() {
            factorySet.factory(Bean.class).define((argument, spec) -> spec.property("str1").value("hello"));

            factorySet.factory(BeanWrapper.class).define((argument, spec) -> {
                spec.property("bean").type(Bean.class);
                spec.link("bean.str1", "str");
                spec.link("bean", "another");
            });

            assertThat(factorySet.type(BeanWrapper.class).property("another", new Bean().setStr1("world")).create())
                    .hasFieldOrPropertyWithValue("str", "world");
        }
    }
}
