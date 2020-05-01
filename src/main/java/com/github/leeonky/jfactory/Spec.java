package com.github.leeonky.jfactory;

public interface Spec<T> {
    BeanSpec<T>.PropertySpec property(String stringValue);
}
