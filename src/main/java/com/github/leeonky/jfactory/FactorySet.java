package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FactorySet {
    private final Map<Class<?>, Integer> sequences = new HashMap<>();
    private final Map<Class<?>, BeanFactory<?>> beanFactories = new HashMap<>();
    private final Map<Class<?>, CustomizedFactory<?>> customizedFactoryInType = new HashMap<>();
    private final Map<String, CustomizedFactory<?>> customizedFactoryInName = new HashMap<>();
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
        return (BeanFactory<T>) beanFactories.computeIfAbsent(type, BeanFactory::create);
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

    public <T> FactorySet define(Class<? extends Definition<T>> definition) {
        Definition<T> definitionInstance = BeanClass.newInstance(definition);
        CustomizedFactory<T> customizedFactory = new CustomizedFactory<>(queryObjectFactory(definitionInstance.getType()), definitionInstance);
        customizedFactoryInType.put(definition, customizedFactory);
        customizedFactoryInName.put(definitionInstance.getName(), customizedFactory);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> Builder<T> toBuild(Class<? extends Definition<T>> definition) {
        define(definition);
        return new Builder<>(this, (BeanFactory<T>) customizedFactoryInType.get(definition));
    }

    @SuppressWarnings("unchecked")
    public <T> Builder<T> toBuild(String definition) {
        return new Builder<>(this, (BeanFactory<T>) customizedFactoryInName.get(definition));
    }
}
