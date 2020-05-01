package com.github.leeonky.jfactory;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class BeanSpec<T> implements Spec<T> {
    private final Builder<T>.BeanFactoryProducer beanFactoryProducer;
    private final FactorySet factorySet;
    private final Argument argument;

    public BeanSpec(Builder<T>.BeanFactoryProducer beanFactoryProducer, FactorySet factorySet, Argument argument) {
        this.beanFactoryProducer = beanFactoryProducer;
        this.factorySet = factorySet;
        this.argument = argument;
    }

    @Override
    public PropertySpec property(String property) {
        String[] propertyAndIndex = property.split("[\\[\\]]");
        if (propertyAndIndex.length > 1)
            return new CollectionElementSpec(propertyAndIndex[0], Integer.valueOf(propertyAndIndex[1]));
        return new PropertySpec(property);
    }

    public class PropertySpec {
        protected final String property;

        PropertySpec(String property) {
            this.property = property;
        }


        public void value(Object value) {
            addProducer(new ValueProducer<>(() -> value));
        }

        protected void addProducer(Producer<?> producer) {
            beanFactoryProducer.add(property, producer);
        }

        public <T> void from(Class<? extends Definition<T>> definition) {
            addProducer(factorySet.toBuild(definition).params(argument.allParams()).producer(property));
        }

        public <T> void from(Class<? extends Definition<T>> definition, Function<Builder<T>, Builder<T>> builder) {
            addProducer(builder.apply(factorySet.toBuild(definition).params(argument.allParams())).producer(property));
        }

        public <T, D extends Definition<T>> void fromMixIn(Class<D> definition, Consumer<D> mixIn) {
            addProducer(factorySet.toBuild(definition, mixIn).params(argument.allParams()).producer(property));
        }

        public void type(Class<?> type) {
            addProducer(factorySet.type(type).params(argument.allParams()).producer(property));
        }

        public <T> void type(Class<T> type, Function<Builder<T>, Builder<T>> builder) {
            addProducer(builder.apply(factorySet.type(type).params(argument.allParams())).producer(property));
        }

        public void supplier(Supplier<?> supplier) {
            addProducer(new ValueProducer<>(supplier));
        }

        public void dependsOn(String property, Function<Object, Object> dependency) {
            dependsOn(new String[]{property}, objects -> dependency.apply(objects[0]));
        }

        public void dependsOn(String[] properties, Function<Object[], Object> dependency) {
            beanFactoryProducer.addDependency(property, properties, dependency);
        }
    }

    class CollectionElementSpec extends PropertySpec {
        private final int index;

        public CollectionElementSpec(String property, int index) {
            super(property);
            this.index = index;
        }

        @Override
        protected void addProducer(Producer<?> producer) {
            beanFactoryProducer.getOrAddCollectionProducer(property).setElementProducer(index, producer);
        }
    }
}
