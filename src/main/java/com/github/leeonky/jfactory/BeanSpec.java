package com.github.leeonky.jfactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Collections.singletonList;

class BeanSpec implements Spec {
    private final Builder<?>.BeanFactoryProducer beanFactoryProducer;
    private final FactorySet factorySet;
    private final Argument argument;

    public BeanSpec(Builder<?>.BeanFactoryProducer beanFactoryProducer, FactorySet factorySet, Argument argument) {
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
            List<Object> beanIndexes = beanFactoryProducer.getIndexes();

            List<Object> propertyIndexChain = new ArrayList<>(beanIndexes);
            propertyIndexChain.add(this.property);

            List<Object> dependencyIndexChain = new ArrayList<>(beanIndexes);
            dependencyIndexChain.add(property);

            beanFactoryProducer.getRoot()
                    .addDependency(propertyIndexChain, singletonList(dependencyIndexChain), (deps) -> dependency.apply(deps.get(0)));
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
            ((CollectionProducer<?>) beanFactoryProducer.getOrAdd(property, () ->
                    new CollectionProducer<>(beanFactoryProducer.getType().getPropertyWriter(property).getPropertyTypeWrapper())))
                    .setElementProducer(index, producer);
        }

    }
}
