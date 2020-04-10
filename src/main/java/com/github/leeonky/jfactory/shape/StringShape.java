package com.github.leeonky.jfactory.shape;

import com.github.leeonky.jfactory.Shape;
import com.github.leeonky.util.BeanClass;

public class StringShape extends Shape<String> {
    public StringShape() {
        super(BeanClass.create(String.class));
    }

    @Override
    public String newInstance(int sequence) {
        return "string" + sequence;
    }
}
