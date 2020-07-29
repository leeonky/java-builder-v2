package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class FactorySet {
    private final Map<Class<?>, Integer> sequences = new HashMap<>();
    private final Map<Class<?>, BeanFactory<?>> beanFactories = new HashMap<>();
    private final Map<Class<?>, CustomizedFactory<?>> customizedFactoryInType = new HashMap<>();
    private final Map<String, CustomizedFactory<?>> customizedFactoryInName = new HashMap<>();
    private final DataRepository dataRepository;
    private final ValueFactories valueFactories = new ValueFactories();

    public FactorySet(DataRepository dataRepository) {
        this.dataRepository = Objects.requireNonNull(dataRepository);
    }

    public FactorySet() {
        dataRepository = new HashMapDataRepository();
    }

    public ValueFactories getValueFactories() {
        return valueFactories;
    }

    private <T> BeanFactory<T> createFactory(Class<T> type) {
        return valueFactories.get(type).orElseGet(() -> new BeanFactory<>(BeanClass.create(type)));
    }

    public <T> Builder<T> type(Class<T> type) {
        return new Builder<>(this, queryObjectFactory(type));
    }

    @SuppressWarnings("unchecked")
    private <T> BeanFactory<T> queryObjectFactory(Class<T> type) {
        return (BeanFactory<T>) beanFactories.computeIfAbsent(type, this::createFactory);
    }

    <T> int getSequence(BeanClass<T> type) {
        int sequence = sequences.getOrDefault(type.getType(), 0) + 1;
        sequences.put(type.getType(), sequence);
        return sequence;
    }

    public <T> T create(Class<T> type) {
        return type(type).create();
    }

    public <T, D extends Definition<T>> T createFrom(Class<D> definition) {
        return toBuild(definition).create();
    }

    @SuppressWarnings("unchecked")
    public <T> T createFrom(String definition) {
        return (T) toBuild(definition).create();
    }

    public <T> Factory<T> factory(Class<T> type) {
        return queryObjectFactory(type);
    }

    DataRepository getDataRepository() {
        return dataRepository;
    }

    public <T> FactorySet define(Class<? extends Definition<T>> definition) {
        Definition<T> definitionInstance = BeanClass.newInstance(definition);
        CustomizedFactory<T> customizedFactory = new CustomizedFactory<>(queryObjectFactory(definitionInstance.getType()), definition);
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
        BeanFactory<T> beanFactory = (BeanFactory<T>) customizedFactoryInName.get(definition);
        if (beanFactory == null)
            throw new IllegalArgumentException("Definition `" + definition + "` not exist");
        return new Builder<>(this, beanFactory);
    }

    @SuppressWarnings("unchecked")
    public <T, D extends Definition<T>> Builder<T> toBuild(Class<D> definition, Consumer<D> mixIn) {
        define(definition);
        return new Builder<>(this, (BeanFactory<T>) customizedFactoryInType.get(definition))
                .mixIn((arg, spec) -> mixIn.accept((D) BeanClass.newInstance(definition).setContext(arg, spec)));
    }

    public void clearRepo() {
        getDataRepository().clear();
    }

}
