package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.PropertyReader;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class QueryExpression<T> {
    private final BeanClass<T> beanClass;
    private final String property;
    private final String[] mixIns;
    private final String definition;
    @Deprecated
    private final String condition;
    @Deprecated
    private final Object value;
    private final Object baseValue;
    private final Map<String, Object> conditionValues = new LinkedHashMap<>();

    public QueryExpression(BeanClass<T> beanClass, String chain, Object value) {
        this.beanClass = beanClass;
        this.value = value;
        Matcher matcher;
        if ((matcher = Pattern.compile("([^.]+)\\((.+)[, |,| ](.+)\\)\\.(.+)").matcher(chain)).matches()) {
            property = matcher.group(1);
            mixIns = matcher.group(2).split(", |,| ");
            definition = matcher.group(3);
            condition = matcher.group(4);
            conditionValues.put(condition, value);
            baseValue = null;
        } else if ((matcher = Pattern.compile("([^.]+)\\((.+)\\)\\.(.+)").matcher(chain)).matches()) {
            property = matcher.group(1);
            mixIns = new String[0];
            definition = matcher.group(2);
            condition = matcher.group(3);
            conditionValues.put(condition, value);
            baseValue = null;
        } else if ((matcher = Pattern.compile("([^.]+)\\.(.+)").matcher(chain)).matches()) {
            property = matcher.group(1);
            mixIns = new String[0];
            definition = null;
            condition = matcher.group(2);
            conditionValues.put(condition, value);
            baseValue = null;
        } else if ((matcher = Pattern.compile("([^.]+)").matcher(chain)).matches()) {
            property = matcher.group(1);
            mixIns = new String[0];
            definition = null;
            condition = null;
            baseValue = value;
        } else
            throw new IllegalStateException("Invalid query expression `" + chain + "`");
    }

    public String getProperty() {
        return property;
    }

    @SuppressWarnings("unchecked")
    public boolean matches(Object object) {
        if (object == null)
            return false;
        PropertyReader propertyReader = beanClass.getPropertyReader(property);
        Object propertyValue = propertyReader.getValue(object);
        if (conditionValues.isEmpty())
            return Objects.equals(propertyValue, propertyReader.tryConvert(baseValue));
        return conditionValues.entrySet().stream().allMatch(e -> new QueryExpression(propertyReader.getPropertyTypeWrapper(), e.getKey(), e.getValue()).matches(propertyValue));
    }

    public void queryOrCreateNested(FactorySet factorySet, PropertiesProducer propertiesProducer) {
        if (condition == null)
            propertiesProducer.add(new ValueProducer<>(property, value));
        else {
            Collection<?> collection = toBuilder(factorySet, beanClass.getPropertyReader(property).getPropertyType()).queryAll();
            if (collection.isEmpty())
                propertiesProducer.add(toBuilder(factorySet, beanClass.getPropertyWriter(property).getPropertyType()).producer(property));
            else
                propertiesProducer.add(new ValueProducer<>(property, collection.iterator().next()));
        }
    }

    private Builder<?> toBuilder(FactorySet factorySet, Class<?> propertyType) {
        return (definition != null ? factorySet.toBuild(definition) : factorySet.type(propertyType))
                .mixIn(mixIns).property(condition, value);
    }

    public QueryExpression<T> merge(QueryExpression<T> another) {
        conditionValues.putAll(another.conditionValues);
        return this;
    }
}
