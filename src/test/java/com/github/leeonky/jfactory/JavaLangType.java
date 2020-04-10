package com.github.leeonky.jfactory;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JavaLangType {
    private Factory factory = new Factory();

    @Test
    void create_default_string() {
        assertThat(factory.type(String.class).create()).isEqualTo("string1");
        assertThat(factory.type(String.class).create()).isEqualTo("string2");
    }
}
