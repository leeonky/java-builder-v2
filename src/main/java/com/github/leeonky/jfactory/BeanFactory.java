package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.PropertyWriter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

class BeanFactory<T> implements Factory<T> {
    private final BeanClass<T> type;
    private final Map<String, BiConsumer<Argument, Spec>> mixIns = new HashMap<>();
    private Function<Argument, T> constructor = this::newInstance;
    private BiConsumer<Argument, Spec> definition = (arg, spec) -> {
    };

    public BeanFactory(BeanClass<T> type) {
        this.type = type;
    }

    public static <T> BeanFactory<T> create(Class<T> type) {
        return ValueFactories.of(type).orElseGet(() -> new BeanFactory<>(BeanClass.create(type)));
    }

    protected T newInstance(Argument argument) {
        return getType().newInstance();
    }

    public T create(Argument argument) {
        return constructor.apply(argument);
    }

    public BeanClass<T> getType() {
        return type;
    }

    public Map<String, PropertyWriter<T>> getPropertyWriters() {
        return type.getPropertyWriters();
    }

    @Override
    public Factory<T> construct(Function<Argument, T> constructor) {
        this.constructor = Objects.requireNonNull(constructor);
        return this;
    }

    @Override
    public Factory<T> define(BiConsumer<Argument, Spec> definition) {
        this.definition = Objects.requireNonNull(definition);
        return this;
    }

    @Override
    public Factory<T> canMixIn(String name, BiConsumer<Argument, Spec> mixIn) {
        mixIns.put(name, Objects.requireNonNull(mixIn));
        return this;
    }

    public void collectSpec(Argument argument, BeanSpec beanSpec) {
        definition.accept(argument, beanSpec);
    }

    public void collectMixInSpecs(Argument argument, List<String> mixIns, BeanSpec beanSpec) {
        mixIns.stream()
                .map(this::getMixIn)
                .forEach(spec -> spec.accept(argument, beanSpec));
    }

    private BiConsumer<Argument, Spec> getMixIn(String name) {
        BiConsumer<Argument, Spec> mixIn = mixIns.get(name);
        if (mixIn == null)
            throw new IllegalArgumentException("Mix-in `" + name + "` not exist");
        return mixIn;
    }
}
