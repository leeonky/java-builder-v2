package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.Property;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class CollectionProducer<T> extends Producer<T> {
    private final BeanClass<?> collectionType;
    private final Class<?> elementType;
    private final Argument argument;
    private final ValueFactories valueFactories;
    private List<Handler<?>> elementHandlers = new ArrayList<>();

    public CollectionProducer(Property<?> property, Argument argument, ValueFactories valueFactories) {
        collectionType = property.getPropertyTypeWrapper();
        elementType = property.getElementType();
        this.argument = argument;
        this.valueFactories = valueFactories;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T produce() {
        List<Object> elements = new ArrayList<>();
        // Should not use java stream here, elementHandlers.size may be changed in produce
        for (int i = 0; i < elementHandlers.size(); i++)
            elements.add(elementHandlers.get(i).produce());
        return (T) collectionType.createCollection(elements);
    }

    @SuppressWarnings("unchecked")
    public void setElementProducer(int index, Producer<?> producer) {
        fillCollectionWithDefaultValue(index);
        elementHandlers.get(index).changeProducer((Producer) producer);
    }

    private void fillCollectionWithDefaultValue(int index) {
        for (int i = elementHandlers.size(); i <= index; i++)
            valueFactories.defaultProducer(argument, argument.getProperty(), elementType)
                    .ifPresent(producer -> elementHandlers.add(new Handler<>(producer)));
    }

    @Override
    protected Collection<Handler<?>> getChildren() {
        return collectChildren(elementHandlers);
    }

    @Override
    public Handler<?> getBy(Object key) {
        fillCollectionWithDefaultValue((Integer) key);
        return elementHandlers.get((int) key);
    }

    @Override
    protected void changeBy(Object key, Producer<T> producer) {
        elementHandlers.set((Integer) key, new Handler<>(producer));
    }
}
