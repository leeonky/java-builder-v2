package com.github.leeonky.jfactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class Argument {
    private final String property;
    private final int sequence;
    private final Map<String, Object> params = new HashMap<>();
    private Object current;

    Argument(String property, int sequence, Map<String, Object> params) {
        this.property = property;
        this.sequence = sequence;
        this.params.putAll(params);
    }

    Argument setCurrent(Object current) {
        this.current = current;
        return this;
    }

    public int getSequence() {
        return sequence;
    }

    public String getProperty() {
        return property;
    }

    @SuppressWarnings("unchecked")
    public <T> T param(String p) {
        return (T) params.get(p);
    }

    public Map<String, Object> getParams() {
        return params;
    }

    Argument newProperty(String property) {
        return new Argument(property, sequence, params);
    }

    public Supplier<Object> current() {
        return () -> current;
    }
}
