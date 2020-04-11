package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.GenericType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ValueShapes {
    private static final Map<Class<?>, Shape<?>> shapes = new HashMap<Class<?>, Shape<?>>() {{
        put(String.class, new StringShape());
        put(Integer.class, new IntegerShape());
        put(int.class, new IntegerShape());
    }};

    @SuppressWarnings("unchecked")
    public static <T> Optional<Shape<T>> shapeOf(Class<T> type) {
        return Optional.ofNullable((Shape<T>) shapes.get(type));
    }

    static class ValueShape<T> extends Shape<T> {
        ValueShape() {
            super(null);
        }

        @Override
        @SuppressWarnings("unchecked")
        public BeanClass<T> getType() {
            return BeanClass.create((Class<T>) GenericType.createGenericType(getClass().getGenericSuperclass()).getGenericTypeParameter(0)
                    .orElseThrow(() -> new IllegalStateException(String.format("Invalid ValueShape declaration '%s' should specify generic type or override getType() method", getClass().getName())))
                    .getRawType());
        }
    }

    static class StringShape extends ValueShape<String> {
        @Override
        public String newInstance(int sequence) {
            return "string" + sequence;
        }
    }

    static class IntegerShape extends ValueShape<Integer> {
        @Override
        public Integer newInstance(int sequence) {
            return sequence;
        }
    }
}
