package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.GenericType;
import com.github.leeonky.util.PropertyWriter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ValueComponents {
    private static final Map<Class<?>, Component<?>> buildIns = new HashMap<Class<?>, Component<?>>() {{
        put(String.class, new StringComponent());
        put(Integer.class, new IntegerComponent());
        put(int.class, new IntegerComponent());
    }};

    @SuppressWarnings("unchecked")
    public static <T> Optional<Component<T>> componentOf(Class<T> type) {
        return Optional.ofNullable((Component<T>) buildIns.get(type));
    }

    static class ValueComponent<T> extends Component<T> {
        ValueComponent() {
            super(null);
        }

        @Override
        @SuppressWarnings("unchecked")
        public BeanClass<T> getType() {
            return BeanClass.create((Class<T>) GenericType.createGenericType(getClass().getGenericSuperclass()).getGenericTypeParameter(0)
                    .orElseThrow(() -> new IllegalStateException(String.format("Invalid ValueComponent declaration '%s' should specify generic type or override getType() method", getClass().getName())))
                    .getRawType());
        }

        @Override
        public Map<String, PropertyWriter<T>> getPropertyWriters() {
            return new HashMap<>();
        }
    }

    static class StringComponent extends ValueComponent<String> {
        @Override
        public String newInstance(String property, int sequence) {
            return (property == null ? "string" : property) + sequence;
        }
    }

    static class IntegerComponent extends ValueComponent<Integer> {
        @Override
        public Integer newInstance(String property, int sequence) {
            return sequence;
        }
    }
}
