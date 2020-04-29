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
    private Function<Argument, T> constructor = argument -> getType().newInstance();
    private BiConsumer<Argument, Spec> definition = (arg, spec) -> {
    };

    public BeanFactory(BeanClass<T> type) {
        this.type = type;
    }

    public final T create(Argument argument) {
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
        mixIns.stream().peek(name -> {
            if (!this.mixIns.containsKey(name))
                throw new IllegalArgumentException("Mix-in `" + name + "` not exist");
        }).map(this.mixIns::get).forEach(spec -> spec.accept(argument, beanSpec));
    }
}
