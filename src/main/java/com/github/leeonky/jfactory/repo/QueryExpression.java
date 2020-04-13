package com.github.leeonky.jfactory.repo;

import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.PropertyReader;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryExpression<T> {
    private final BeanClass<T> beanClass;
    private final Object value;
    private final String baseName;
    private final String[] combinations;
    private final String factory, condition;

    public QueryExpression(BeanClass<T> beanClass, String chain, Object value) {
        this.beanClass = beanClass;
        this.value = value;
        Matcher matcher;
        if ((matcher = Pattern.compile("([^.]+)\\((.+)[, |,| ](.+)\\)\\.(.+)").matcher(chain)).matches()) {
            baseName = matcher.group(1);
            combinations = matcher.group(2).split(", |,| ");
            factory = matcher.group(3);
            condition = matcher.group(4);
        } else if ((matcher = Pattern.compile("([^.]+)\\((.+)\\)\\.(.+)").matcher(chain)).matches()) {
            baseName = matcher.group(1);
            combinations = new String[0];
            factory = matcher.group(2);
            condition = matcher.group(3);
        } else if ((matcher = Pattern.compile("([^.]+)\\.(.+)").matcher(chain)).matches()) {
            baseName = matcher.group(1);
            combinations = new String[0];
            factory = null;
            condition = matcher.group(2);
        } else if ((matcher = Pattern.compile("([^.]+)").matcher(chain)).matches()) {
            baseName = matcher.group(1);
            combinations = new String[0];
            factory = null;
            condition = null;
        } else
            throw new IllegalStateException("Invalid query expression `" + chain + "`");
    }

    @SuppressWarnings("unchecked")
    public boolean matches(Object object) {
        if (object == null)
            return false;
        PropertyReader propertyReader = beanClass.getPropertyReader(baseName);
        Object propertyValue = propertyReader.getValue(object);
        if (condition == null)
            return Objects.equals(propertyValue, propertyReader.tryConvert(value));
        return new QueryExpression(propertyReader.getPropertyTypeWrapper(), condition, value).matches(propertyValue);
    }
}
