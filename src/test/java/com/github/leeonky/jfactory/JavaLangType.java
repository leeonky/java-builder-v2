package com.github.leeonky.jfactory;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JavaLangType {
    private Factory factory = new Factory();

    @Test
    void create_default_string() {
        assertThat(factory.create(String.class)).isEqualTo("string1");
        assertThat(factory.create(String.class)).isEqualTo("string2");
    }

    @Test
    void create_default_int() {
        assertThat(factory.create(int.class)).isEqualTo(1);
        assertThat(factory.create(Integer.class)).isEqualTo(2);
    }
}
