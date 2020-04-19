package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.PropertyReader;

import java.util.Collection;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class QueryExpression<T> {
    private final BeanClass<T> beanClass;
    private final String baseName;
    private final String[] mixIns;
    private final String definition, condition;
    private final Object value;

    public QueryExpression(BeanClass<T> beanClass, String chain, Object value) {
        this.beanClass = beanClass;
        this.value = value;
        Matcher matcher;
        if ((matcher = Pattern.compile("([^.]+)\\((.+)[, |,| ](.+)\\)\\.(.+)").matcher(chain)).matches()) {
            baseName = matcher.group(1);
            mixIns = matcher.group(2).split(", |,| ");
            definition = matcher.group(3);
            condition = matcher.group(4);
        } else if ((matcher = Pattern.compile("([^.]+)\\((.+)\\)\\.(.+)").matcher(chain)).matches()) {
            baseName = matcher.group(1);
            mixIns = new String[0];
            definition = matcher.group(2);
            condition = matcher.group(3);
        } else if ((matcher = Pattern.compile("([^.]+)\\.(.+)").matcher(chain)).matches()) {
            baseName = matcher.group(1);
            mixIns = new String[0];
            definition = null;
            condition = matcher.group(2);
        } else if ((matcher = Pattern.compile("([^.]+)").matcher(chain)).matches()) {
            baseName = matcher.group(1);
            mixIns = new String[0];
            definition = null;
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

    public void queryOrCreateNested(FactorySet factorySet, PropertiesProducer propertiesProducer) {
        if (condition == null)
            propertiesProducer.add(new ValueProducer<>(baseName, value));
        else {
            Collection<?> collection = toBuilder(factorySet, beanClass.getPropertyReader(baseName).getPropertyType()).queryAll();
            if (collection.isEmpty())
                propertiesProducer.add(toBuilder(factorySet, beanClass.getPropertyWriter(baseName).getPropertyType()).producer(baseName));
            else
                propertiesProducer.add(new ValueProducer<>(baseName, collection.iterator().next()));
        }
    }

    private Builder<?> toBuilder(FactorySet factorySet, Class<?> propertyType) {
        return (definition != null ? factorySet.toBuild(definition) : factorySet.type(propertyType))
                .mixIn(mixIns).property(condition, value);
    }
}
