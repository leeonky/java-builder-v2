package com.github.leeonky.jfactory;

import com.github.leeonky.jfactory.factory.ObjectFactory;
import com.github.leeonky.jfactory.repo.HashMapDataRepository;
import com.github.leeonky.util.BeanClass;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FactorySet {
    private final Map<Class<?>, Integer> sequences = new HashMap<>();
    private final Map<Class<?>, ObjectFactory<?>> objectFactories = new HashMap<>();
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
    private <T> ObjectFactory<T> queryObjectFactory(Class<T> type) {
        return (ObjectFactory<T>) objectFactories.computeIfAbsent(type, ObjectFactory::create);
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
