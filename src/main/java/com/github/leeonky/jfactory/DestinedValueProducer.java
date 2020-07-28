package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.NullPointerInChainException;

import java.util.LinkedList;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;

public class DestinedValueProducer<T> extends Producer<T> {
    private final T value;

    DestinedValueProducer(T value) {
        this.value = value;
    }

    @Override
    public T produce() {
        return value;
    }

    @Override
    protected Producer<T> changeTo(Producer<T> producer) {
        return producer instanceof LinkProducer ? producer : this;
    }

    @Override
    protected Optional<Producer<?>> forLink(LinkedList<Object> leftIndex) {
        BeanClass beanClass = BeanClass.create(value.getClass());
        try {
            return of(new DestinedValueProducer<>(beanClass.getPropertyChainValue(value, leftIndex)));
        } catch (NullPointerInChainException e) {
            return empty();
        }
    }
}
