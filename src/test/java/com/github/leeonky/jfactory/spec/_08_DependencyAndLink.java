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
    }
}
