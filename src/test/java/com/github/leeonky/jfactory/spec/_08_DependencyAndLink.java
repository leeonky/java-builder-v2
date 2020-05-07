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

    @Nested
    class PropertyDependency {

        @Test
        void dependency_in_same_level() {
            factorySet.factory(Beans.class).define((argument, spec) -> {
                spec.property("bean1").dependsOn("bean2", obj -> obj);
            });
            Bean bean2 = new Bean();

            assertThat(factorySet.type(Beans.class).property("bean2", bean2).create())
                    .hasFieldOrPropertyWithValue("bean1", bean2);
        }

        @Test
        void list_dependency_in_same_level() {
            factorySet.factory(Beans.class).define((argument, spec) -> {
                spec.property("bean1").dependsOn(new String[]{"bean2"}, objs -> objs[0]);
            });
            Bean bean2 = new Bean();

            assertThat(factorySet.type(Beans.class).property("bean2", bean2).create())
                    .hasFieldOrPropertyWithValue("bean1", bean2);
        }

        @Test
        void override_with_dependency() {
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
    }
}
