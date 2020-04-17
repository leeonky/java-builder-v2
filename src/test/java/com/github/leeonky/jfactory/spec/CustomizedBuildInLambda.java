package com.github.leeonky.jfactory.spec;

import com.github.leeonky.jfactory.FactorySet;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CustomizedBuildInLambda {
    private FactorySet factorySet = new FactorySet();

    @Test
    void support_define_build() {
        factorySet.factory(Bean.class).define((arg, spec) -> {
            spec.property("stringValue").value("hello");
        });

        assertThat(factorySet.type(Bean.class).create())
                .hasFieldOrPropertyWithValue("stringValue", "hello");
    }

    @Test
    void support_define_build_with_arg() {
        factorySet.factory(Bean.class).define((arg, spec) ->
                spec.property("stringValue").value(arg.getSequence() + (int) arg.param("i")));

        assertThat(factorySet.type(Bean.class).param("i", 2).create())
                .hasFieldOrPropertyWithValue("stringValue", "3");
    }

    @Test
    void support_define_mix_in() {
        factorySet.factory(Bean.class).canMixIn("100", (arg, spec) -> {
            spec.property("intValue").value(100);
        });

        assertThat(factorySet.type(Bean.class).mixIn("100").create())
                .hasFieldOrPropertyWithValue("intValue", 100);
    }

    @Test
    void raise_error_when_mixin_not_exist() {
        assertThrows(IllegalArgumentException.class, () -> factorySet.type(Bean.class).mixIn("not exist").create());
    }

    @Getter
    @Setter
    public static class Bean {
        private String stringValue;
        private int intValue;
    }
}

