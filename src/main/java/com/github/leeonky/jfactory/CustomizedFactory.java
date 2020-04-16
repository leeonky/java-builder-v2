package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.stream.Stream;

class CustomizedFactory<T> extends BeanFactory<T> {
    public CustomizedFactory(Definition<T> definition) {
        super(BeanClass.create(definition.getType()));
        define(definition::define);
        Stream.of(definition.getClass().getMethods())
                .filter(method -> method.getAnnotation(MixIn.class) != null)
                .forEach(method -> canMixIn(getMixInName(method), (argument, spec) -> {
                    try {
                        method.invoke(definition, argument, spec);
                    } catch (IllegalAccessException e) {
                        throw new IllegalStateException(e);
                    } catch (InvocationTargetException e) {
                        if (e.getTargetException() instanceof RuntimeException)
                            throw (RuntimeException) e.getTargetException();
                        throw new IllegalStateException(e);
                    }
                }));
    }

    private String getMixInName(Method method) {
        MixIn annotation = method.getAnnotation(MixIn.class);
        return annotation.value().isEmpty() ? method.getName() : annotation.value();
    }
}
