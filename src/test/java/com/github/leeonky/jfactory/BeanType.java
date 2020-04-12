package com.github.leeonky.jfactory;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BeanType {
    private Factory factory = new Factory();

    @Test
    void create_default_bean() {
        assertThat(factory.create(Bean.class))
                .hasFieldOrPropertyWithValue("stringValue", "stringValue1")
                .hasFieldOrPropertyWithValue("intValue", 1)
        ;

        assertThat(factory.create(Bean.class))
                .hasFieldOrPropertyWithValue("stringValue", "stringValue2")
                .hasFieldOrPropertyWithValue("intValue", 2)
        ;
    }

    @Getter
    @Setter
    public static class Bean {
        private String stringValue;
        private int intValue;
    }
}
