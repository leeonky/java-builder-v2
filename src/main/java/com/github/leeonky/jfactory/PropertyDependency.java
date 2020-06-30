package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.NullPointerInChainException;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

class PropertyDependency<T> {
    private final List<Object> property;
    private final List<List<Object>> dependencies;
    private final Function<List<Object>, T> rule;

    public PropertyDependency(List<Object> property, List<List<Object>> dependencies, Function<List<Object>, T> rule) {
        this.property = property;
        this.dependencies = dependencies;
        this.rule = rule;
    }

    // TODO producer maybe changed by another dependency
    @SuppressWarnings("unchecked")
    public void processDependency(Producer<?> producer, Argument argument) {
        producer.changeByIndex(property, new DependencyProducer(
                dependencies.stream().map(index -> (Supplier<?>) () -> {
                    Producer.Handler<?> handler = producer.getByIndex(index);
                    return handler != null ? handler.produce() : getProperty(argument.willGetCurrent().get(), index);
                }).collect(Collectors.toList()),
                rule));
    }

    @SuppressWarnings("unchecked")
    private Object getProperty(Object object, List<Object> properties) {
        BeanClass beanClass = BeanClass.create(object.getClass());
        try {
            return beanClass.getPropertyChainValue(object, properties);
        } catch (NullPointerInChainException e) {
            return beanClass.getPropertyChainReader(properties).getPropertyTypeWrapper().createDefault();
        }
    }
}
