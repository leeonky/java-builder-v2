package com.github.leeonky.jfactory.factory;

import com.github.leeonky.jfactory.Argument;
import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.GenericType;
import com.github.leeonky.util.PropertyWriter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Factories {
    private static final Map<Class<?>, ObjectFactory<?>> buildIns = new HashMap<Class<?>, ObjectFactory<?>>() {{
        put(String.class, new StringFactory());
        put(Integer.class, new IntegerFactory());
        put(int.class, new IntegerFactory());
    }};

    @SuppressWarnings("unchecked")
    public static <T> Optional<ObjectFactory<T>> of(Class<T> type) {
        return Optional.ofNullable((ObjectFactory<T>) buildIns.get(type));
    }

    static class ValueFactory<T> extends ObjectFactory<T> {
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
        protected String newInstance(Argument argument) {
            return (argument.getProperty() == null ? "string" : argument.getProperty()) + argument.getSequence();
        }
    }

    static class IntegerFactory extends ValueFactory<Integer> {
        @Override
        public Integer create(Argument argument) {
            return argument.getSequence();
        }
    }
}
