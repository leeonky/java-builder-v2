package com.github.leeonky.jfactory.spec;

import com.github.leeonky.jfactory.FactorySet;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomizedBuild {
    private FactorySet factorySet = new FactorySet();

    @Test
    void support_define_build() {
        factorySet.factory(Bean.class).define((arg, spec) -> {
            spec.property("stringValue").value("hello");
        });

        assertThat(factorySet.type(Bean.class).create())
                .hasFieldOrPropertyWithValue("stringValue", "hello");
    }

    @Getter
    @Setter
    public static class Bean {
        private String stringValue;
        private int intValue;
    }
}

