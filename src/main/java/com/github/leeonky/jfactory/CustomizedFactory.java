package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.stream.Stream;

class CustomizedFactory<T> extends BeanFactory<T> {
    private final BeanFactory<T> base;
    private final Class<? extends Definition<T>> definition;

    public CustomizedFactory(BeanFactory<T> base, Class<? extends Definition<T>> definition) {
        super(base.getType());
        this.base = base;
        this.definition = definition;
        registerMixIns();
    }

    private void registerMixIns() {
        Stream.of(definition.getMethods())
                .filter(method -> method.getAnnotation(MixIn.class) != null)
                .forEach(method -> canMixIn(getMixInName(method), (arg, spec) -> {
                    try {
                        method.invoke(BeanClass.newInstance(definition).setContext(arg, spec));
                    } catch (IllegalAccessException e) {
                        throw new IllegalStateException(e);
                    } catch (InvocationTargetException e) {
                        if (e.getTargetException() instanceof RuntimeException)
                            throw (RuntimeException) e.getTargetException();
                        throw new IllegalStateException(e);
                    }
                }));
    }

    @Override
    protected T newInstance(Argument argument) {
        return base.create(argument);
    }

    @Override
    public void collectSpec(Argument arg, BeanSpec<T> spec) {
        base.collectSpec(arg, spec);
        BeanClass.newInstance(definition).setContext(arg, spec).define();
        super.collectSpec(arg, spec);
    }

    private String getMixInName(Method method) {
        MixIn annotation = method.getAnnotation(MixIn.class);
        return annotation.value().isEmpty() ? method.getName() : annotation.value();
    }
}
