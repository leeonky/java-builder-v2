package com.github.leeonky.jfactory.spec;

import com.github.leeonky.jfactory.FactorySet;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JavaLangType {
    private FactorySet factorySet = new FactorySet();

    @Test
    void create_default_string() {
        assertThat(factorySet.create(String.class)).isEqualTo("string1");
        assertThat(factorySet.create(String.class)).isEqualTo("string2");
    }

    @Test
    void create_default_int() {
        assertThat(factorySet.create(int.class)).isEqualTo(1);
        assertThat(factorySet.create(Integer.class)).isEqualTo(2);
    }
}
