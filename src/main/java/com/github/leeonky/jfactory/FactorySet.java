package com.github.leeonky.jfactory;

import com.github.leeonky.jfactory.util.BeanFactory;
import com.github.leeonky.jfactory.util.HashMapDataRepository;
import com.github.leeonky.util.BeanClass;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FactorySet {
    private final Map<Class<?>, Integer> sequences = new HashMap<>();
    private final Map<Class<?>, BeanFactory<?>> objectFactories = new HashMap<>();
    private final DataRepository dataRepository;

    public FactorySet(DataRepository dataRepository) {
        this.dataRepository = Objects.requireNonNull(dataRepository);
    }

    public FactorySet() {
        dataRepository = new HashMapDataRepository();
    }

    public <T> Builder<T> type(Class<T> type) {
        return new Builder<>(this, queryObjectFactory(type));
    }

    @SuppressWarnings("unchecked")
    private <T> BeanFactory<T> queryObjectFactory(Class<T> type) {
        return (BeanFactory<T>) objectFactories.computeIfAbsent(type, BeanFactory::create);
    }

    <T> int getSequence(BeanClass<T> type) {
        int sequence = sequences.getOrDefault(type.getType(), 0) + 1;
        sequences.put(type.getType(), sequence);
        return sequence;
    }

    public <T> T create(Class<T> type) {
        return type(type).create();
    }

    public <T> Factory<T> factory(Class<T> type) {
        return queryObjectFactory(type);
    }

    public DataRepository getDataRepository() {
        return dataRepository;
    }
}
