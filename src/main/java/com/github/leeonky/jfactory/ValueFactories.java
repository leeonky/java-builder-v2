package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.GenericType;
import com.github.leeonky.util.PropertyWriter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class ValueFactories {
    private static final Map<Class<?>, Factory<?>> buildIns = new HashMap<Class<?>, Factory<?>>() {{
        put(String.class, new StringFactory());
        put(Integer.class, new IntegerFactory());
        put(int.class, new IntegerFactory());
    }};

    @SuppressWarnings("unchecked")
    static <T> Optional<Factory<T>> componentOf(Class<T> type) {
        return Optional.ofNullable((Factory<T>) buildIns.get(type));
    }

    static class ValueFactory<T> extends Factory<T> {
        ValueFactory() {
            super(null);
        }

        @Override
        @SuppressWarnings("unchecked")
        public BeanClass<T> getType() {
            return BeanClass.create((Class<T>) GenericType.createGenericType(getClass().getGenericSuperclass()).getGenericTypeParameter(0)
                    .orElseThrow(() -> new IllegalStateException(String.format("Invalid ValueFactory declaration '%s' should specify generic type or override getType() method", getClass().getName())))
                    .getRawType());
        }

        @Override
        public Map<String, PropertyWriter<T>> getPropertyWriters() {
            return new HashMap<>();
        }
    }

    static class StringFactory extends ValueFactory<String> {
        @Override
        public String newInstance(String property, int sequence) {
            return (property == null ? "string" : property) + sequence;
        }
    }

    static class IntegerFactory extends ValueFactory<Integer> {
        @Override
        public Integer newInstance(String property, int sequence) {
            return sequence;
        }
    }
}
