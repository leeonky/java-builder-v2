package com.github.leeonky.jfactory.spec;

import com.github.leeonky.jfactory.FactorySet;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class _08_DependencyAndLink {
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
        private Bean bean1, bean2;
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
        void depends_properties_in_same_level() {
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
    }
}
