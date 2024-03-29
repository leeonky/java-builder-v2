package com.github.leeonky.jfactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
            return new CollectionElementSpec(propertyAndIndex[0], Integer.valueOf(propertyAndIndex[1]),
                    propertyAndIndex.length > 2 ? propertyAndIndex[2] : "");
        return new PropertySpec(property);
    }

    @Override
    public void link(String property, String... others) {
        List<String> properties = new ArrayList<>();
        properties.add(property);
        properties.addAll(Arrays.asList(others));
        beanFactoryProducer.addLink(properties);
    }

    public class PropertySpec {
        protected final String property;

        PropertySpec(String property) {
            this.property = property;
        }


        public void value(Object value) {
            addProducer(new SuggestedValueProducer<>(() -> value));
        }

        protected void addProducer(Producer<?> producer) {
            if (property.contains(".")) {
                StackTraceElement traceElement = new Exception().getStackTrace()[2];
                throw new IllegalArgumentException(String.format("Not support property chain '%s' in current operation\n\tat %s:%d\n", property, traceElement.getFileName(), traceElement.getLineNumber()));
            }
            beanFactoryProducer.addProducer(property, producer);
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
            addProducer(new SuggestedValueProducer<>(supplier));
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
        private final String leftChain;

        public CollectionElementSpec(String property, int index, String leftChain) {
            super(property);
            this.index = index;
            this.leftChain = leftChain;
        }

        @Override
        protected void addProducer(Producer<?> producer) {
            beanFactoryProducer.getOrAddCollectionProducer(property).setElementProducer(index, producer);
        }

        @Override
        public void dependsOn(String[] properties, Function<Object[], Object> dependency) {
            beanFactoryProducer.addDependency(String.format("%s[%d]%s", property, index, leftChain), properties, dependency);
        }
    }
}
