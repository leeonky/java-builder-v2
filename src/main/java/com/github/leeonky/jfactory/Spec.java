package com.github.leeonky.jfactory;

import com.github.leeonky.jfactory.util.BeanSpec;

public interface Spec<T> {
    BeanSpec.PropertySpec property(String stringValue);
}
