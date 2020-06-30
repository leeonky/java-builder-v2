package com.github.leeonky.jfactory.spec;

import com.github.leeonky.jfactory.FactorySet;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class _08_DependencyAndLink {
    private FactorySet factorySet = new FactorySet();

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Bean {
        private String content;
        private String stringValue;
        private int intValue;
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Beans {
        private Bean bean1, bean2, bean3;
    }

    @Getter
    @Setter
    public static class BeansWrapper {
        private Bean bean;
        private Beans beans;
    }

    @Getter
    @Setter
    public static class BeanArray {
        private Bean[] beans;
        private int intValue;
    }

    @Getter
    @Setter
    public static class BeansArray {
        private Beans[] beansArray;
    }

    @Nested
    class PropertyDependency {

        @Test
        void depends_on_one_property_in_same_level() {
            factorySet.factory(Beans.class).define((argument, spec) -> {
                spec.property("bean1").dependsOn("bean2", obj -> obj);
            });
            Bean bean2 = new Bean();

            assertThat(factorySet.type(Beans.class).property("bean2", bean2).create())
                    .hasFieldOrPropertyWithValue("bean1", bean2);
        }

        @Test
        void depends_property_list_in_same_level() {
            factorySet.factory(Beans.class).define((argument, spec) -> {
                spec.property("bean1").dependsOn(new String[]{"bean2"}, objs -> objs[0]);
            });
            Bean bean2 = new Bean();

            assertThat(factorySet.type(Beans.class).property("bean2", bean2).create())
                    .hasFieldOrPropertyWithValue("bean1", bean2);
        }

        @Test
        void dependency_override_spec() {
            factorySet.factory(Beans.class).define((argument, spec) -> {
                spec.property("bean1").value(null);
                spec.property("bean1").dependsOn("bean2", obj -> obj);
            });
            Bean bean2 = new Bean();

            assertThat(factorySet.type(Beans.class).property("bean2", bean2).create())
                    .hasFieldOrPropertyWithValue("bean1", bean2);
        }

        @Test
        void dependency_in_different_level() {
            factorySet.factory(BeansWrapper.class).define((argument, spec) -> {
                spec.property("beans").type(Beans.class);
                spec.property("beans.bean1").dependsOn("bean", obj -> obj);
            });
            Bean bean = new Bean();

            assertThat(factorySet.type(BeansWrapper.class).property("bean", bean).create().getBeans())
                    .hasFieldOrPropertyWithValue("bean1", bean);

            factorySet.factory(BeansWrapper.class).define((argument, spec) -> {
                spec.property("beans").type(Beans.class, builder -> builder.property("bean1", bean));
                spec.property("bean").dependsOn("beans.bean1", obj -> obj);
            });

            BeansWrapper actual = factorySet.type(BeansWrapper.class).create();
            assertThat(actual)
                    .hasFieldOrPropertyWithValue("bean", bean);
        }

        @Test
        void dependency_in_collection() {
            factorySet.factory(BeanArray.class).define((argument, spec) -> {
                spec.property("beans[1]").dependsOn("beans[0]", obj -> obj);
            });

            Bean bean = new Bean();

            assertThat(factorySet.type(BeanArray.class).property("beans[0]", bean).create().getBeans())
                    .containsOnly(bean, bean);
        }

        @Test
        void dependency_in_deep_level_collection() {
            factorySet.factory(BeansArray.class).define((argument, spec) -> {
                spec.property("beansArray[0]").type(Beans.class);
                spec.property("beansArray[1]").type(Beans.class);
                spec.property("beansArray[0].bean1").dependsOn("beansArray[1].bean2", obj -> obj);
            });

            Bean bean = new Bean();

            BeansArray beansArray = factorySet.type(BeansArray.class).property("beansArray[1].bean2", bean).create();
            assertThat(beansArray.getBeansArray()).hasSize(2);
            assertThat(beansArray.getBeansArray()[0])
                    .hasFieldOrPropertyWithValue("bean1", bean)
                    .hasFieldOrPropertyWithValue("bean2", null);
            assertThat(beansArray.getBeansArray()[1])
                    .hasFieldOrPropertyWithValue("bean1", null)
                    .hasFieldOrPropertyWithValue("bean2", bean);
        }

        @Test
        void dependency_chain_in_array() {
            factorySet.factory(BeanArray.class).define((argument, spec) -> {
                spec.property("beans[2]").dependsOn("beans[1]", obj -> obj);
                spec.property("beans[1]").dependsOn("beans[0]", obj -> obj);
            });

            Bean bean = new Bean();

            assertThat(factorySet.type(BeanArray.class).property("beans[0]", bean).create().getBeans())
                    .containsOnly(bean, bean, bean);
        }

        @Test
        void dependency_chain_in_one_object() {
            factorySet.factory(Beans.class).define((argument, spec) -> {
                spec.property("bean1").dependsOn("bean2", obj -> obj);
                spec.property("bean2").dependsOn("bean3", obj -> obj);
            });

            Bean bean = new Bean();

            assertThat(factorySet.type(Beans.class).property("bean3", bean).create())
                    .hasFieldOrPropertyWithValue("bean1", bean)
                    .hasFieldOrPropertyWithValue("bean2", bean)
                    .hasFieldOrPropertyWithValue("bean3", bean)
            ;
        }

        @Test
        void dependency_in_two_object_spec_definitions() {
            factorySet.factory(BeansWrapper.class).define((argument, spec) -> {
                spec.property("beans").type(Beans.class);
                spec.property("bean").dependsOn("beans.bean1", obj -> obj);
            });
            factorySet.factory(Beans.class).define((argument, spec) -> {
                spec.property("bean1").dependsOn("bean2", obj -> obj);
            });

            Bean bean = new Bean();
            BeansWrapper beansWrapper = factorySet.type(BeansWrapper.class)
                    .property("beans.bean2", bean).create();

            assertThat(beansWrapper)
                    .hasFieldOrPropertyWithValue("bean", bean);
            assertThat(beansWrapper.getBeans())
                    .hasFieldOrPropertyWithValue("bean1", bean)
                    .hasFieldOrPropertyWithValue("bean2", bean);
        }
    }

    @Nested
    class OverrideByInputProperty {

        @Test
        void override_by_input_property() {
            factorySet.factory(Beans.class).define((argument, spec) -> {
                spec.property("bean1").dependsOn("bean2", obj -> obj);
            });

            Bean bean1 = new Bean();
            Bean bean2 = new Bean();
            Beans beans = factorySet.type(Beans.class)
                    .property("bean1", bean1)
                    .property("bean2", bean2).create();

            assertThat(beans)
                    .hasFieldOrPropertyWithValue("bean1", bean1)
                    .hasFieldOrPropertyWithValue("bean2", bean2)
            ;
        }

        @Test
        void override_by_input_property_in_parent_object() {
            factorySet.factory(BeansWrapper.class).define((argument, spec) -> {
                spec.property("beans").type(Beans.class);
                spec.property("beans.bean1").dependsOn("bean", obj -> obj);
            });

            Bean bean = new Bean();
            Beans beans = new Beans();
            BeansWrapper beansWrapper = factorySet.type(BeansWrapper.class)
                    .property("beans", beans)
                    .property("bean", bean).create();

            assertThat(beansWrapper.getBeans().getBean1()).isNotEqualTo(bean);
            assertThat(beansWrapper)
                    .hasFieldOrPropertyWithValue("beans", beans)
                    .hasFieldOrPropertyWithValue("bean", bean)
            ;
        }
    }

    @Nested
    class OverrideByDependency {

        @Test
        void parent_property_dependency_can_override_sub_property_spec() {
            factorySet.factory(Beans.class).define((argument, spec) -> {
                spec.property("bean1").dependsOn("bean3", obj -> obj);
                spec.property("bean1.stringValue").dependsOn("bean2", obj -> ((Bean) obj).getIntValue() + "");
            });

            Bean bean2 = new Bean().setIntValue(1000);
            Bean bean3 = new Bean().setStringValue("bean3");
            Beans beans = factorySet.type(Beans.class)
                    .property("bean2", bean2)
                    .property("bean3", bean3)
                    .create();

            assertThat(beans)
                    .hasFieldOrPropertyWithValue("bean1", bean3)
                    .hasFieldOrPropertyWithValue("bean2", bean2);

            assertThat(bean3).hasFieldOrPropertyWithValue("stringValue", "bean3");
        }
    }

    @Nested
    class TargetPropertyObjectIsNotBeanFactoryProducer {

        @Nested
        class InProperty {

            @Test
            void should_ignore_dependency() {
                factorySet.factory(Beans.class).define((argument, spec) -> {
                    spec.property("bean1.stringValue").dependsOn("bean2", obj -> ((Bean) obj).getIntValue() + "");
                });

                Bean bean = new Bean();
                Beans beans = factorySet.type(Beans.class)
                        .property("bean2", bean)
                        .create();

                assertThat(beans)
                        .hasFieldOrPropertyWithValue("bean1", null)
                        .hasFieldOrPropertyWithValue("bean2", bean);
            }

            @Test
            void should_ignore_dependency_in_collection() {
                factorySet.factory(BeanArray.class).define((argument, spec) -> {
                    spec.property("beans[1].stringValue").dependsOn("beans[0]", obj -> ((Bean) obj).getIntValue() + "");
                });

                Bean bean = new Bean();
                BeanArray beanArray = factorySet.type(BeanArray.class)
                        .property("beans[0]", bean)
                        .create();

                assertThat(beanArray.getBeans()).containsOnly(bean, null);
            }
        }
    }

    @Nested
    class DependencyIsNotProducer {

        @Test
        void read_property_value_from_object() {
            Bean bean = new Bean();
            factorySet.factory(Beans.class)
                    .construct(argument -> new Beans().setBean2(bean))
                    .define((argument, spec) ->
                            spec.property("bean1").dependsOn("bean2", obj -> obj)
                    );

            assertThat(factorySet.create(Beans.class))
                    .hasFieldOrPropertyWithValue("bean1", bean)
                    .hasFieldOrPropertyWithValue("bean2", bean)
            ;
        }

        @Test
        void read_property_value_from_sub_object() {
            Bean bean = new Bean().setIntValue(100);
            factorySet.factory(Beans.class)
                    .construct(argument -> new Beans().setBean2(bean))
                    .define((argument, spec) -> {
                        spec.property("bean1").type(Bean.class);
                        spec.property("bean1.intValue").dependsOn("bean2.intValue", obj -> obj);
                    });

            assertThat(factorySet.create(Beans.class).getBean1())
                    .hasFieldOrPropertyWithValue("intValue", 100)
                    .isNotEqualTo(bean);
        }

        @Test
        void should_use_type_default_value_when_has_null_in_property_chain() {
            factorySet.factory(Beans.class)
                    .define((argument, spec) -> {
                        spec.property("bean1").type(Bean.class);
                        spec.property("bean1.intValue").dependsOn("bean2.intValue", obj -> obj);
                    });

            assertThat(factorySet.create(Beans.class).getBean1())
                    .hasFieldOrPropertyWithValue("intValue", 0)
            ;
        }

        @Test
        void read_property_value_from_collection() {
            factorySet.factory(BeanArray.class)
                    .construct(argument -> {
                        BeanArray beanArray = new BeanArray();
                        beanArray.beans = new Bean[]{null, new Bean().setIntValue(100)};
                        return beanArray;
                    })
                    .define((argument, spec) -> {
                        spec.property("beans[0]").type(Bean.class);
                        spec.property("beans[0].intValue").dependsOn("beans[1].intValue", obj -> obj);
                    });

            BeanArray beanArray = factorySet.create(BeanArray.class);

            assertThat(beanArray.getBeans()[0])
                    .hasFieldOrPropertyWithValue("intValue", 100);
        }
    }
}
