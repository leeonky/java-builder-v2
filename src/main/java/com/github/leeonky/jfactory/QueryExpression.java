package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.PropertyReader;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

class QueryExpression<T> {
    private final BeanClass<T> beanClass;
    private final String property;
    private final Object baseValue;
    private final Map<String, Object> conditionValues = new LinkedHashMap<>();
    private String[] mixIns = new String[0];
    private String definition;

    public QueryExpression(BeanClass<T> beanClass, String chain, Object value) {
        this.beanClass = beanClass;
        Matcher matcher;
        if ((matcher = Pattern.compile("([^.]+)\\((.+)[, |,| ](.+)\\)\\.(.+)").matcher(chain)).matches()) {
            property = matcher.group(1);
            mixIns = matcher.group(2).split(", |,| ");
            definition = matcher.group(3);
            conditionValues.put(matcher.group(4), value);
            baseValue = null;
        } else if ((matcher = Pattern.compile("([^.]+)\\((.+)\\)\\.(.+)").matcher(chain)).matches()) {
            property = matcher.group(1);
            definition = matcher.group(2);
            conditionValues.put(matcher.group(3), value);
            baseValue = null;
        } else if ((matcher = Pattern.compile("([^.]+)\\.(.+)").matcher(chain)).matches()) {
            property = matcher.group(1);
            conditionValues.put(matcher.group(2), value);
            baseValue = null;
        } else if ((matcher = Pattern.compile("([^.]+)").matcher(chain)).matches()) {
            property = matcher.group(1);
            baseValue = value;
        } else
            throw new IllegalStateException("Invalid query expression `" + chain + "`");
    }

    public static <T> List<QueryExpression<T>> createQueryExpressions(BeanClass<T> beanClass, Map<String, Object> criteria) {
        return criteria.entrySet().stream()
                .map(e -> new QueryExpression<>(beanClass, e.getKey(), e.getValue()))
                .collect(Collectors.groupingBy(expression -> expression.property)).values().stream()
                .map(QueryExpression::mergeToSingle)
                .collect(Collectors.toList());
    }

    private static <T> QueryExpression<T> mergeToSingle(List<QueryExpression<T>> expressions) {
        for (int i = 1; i < expressions.size(); i++)
            expressions.get(0).merge(expressions.get(i));
        return expressions.get(0);
    }

    @SuppressWarnings("unchecked")
    public boolean matches(Object object) {
        if (object == null)
            return false;
        PropertyReader propertyReader = beanClass.getPropertyReader(property);
        Object propertyValue = propertyReader.getValue(object);
        if (conditionValues.isEmpty())
            return Objects.equals(propertyValue, propertyReader.tryConvert(baseValue));
        return conditionValues.entrySet().stream()
                .map(conditionValue -> new QueryExpression(propertyReader.getPropertyTypeWrapper(), conditionValue.getKey(), conditionValue.getValue()))
                .allMatch(queryExpression -> queryExpression.matches(propertyValue));
    }

    public void queryOrCreateNested(FactorySet factorySet, PropertiesProducer propertiesProducer) {
        if (conditionValues.isEmpty())
            propertiesProducer.add(new ValueProducer<>(property, baseValue));
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
                .mixIn(mixIns).properties(conditionValues);
    }

    private void merge(QueryExpression<T> another) {
        mergeDefinition(another);
        mergeMixIn(another);
        conditionValues.putAll(another.conditionValues);
    }

    private void mergeMixIn(QueryExpression<T> another) {
        if (mixIns.length != 0 && another.mixIns.length != 0
                && !new HashSet<>(asList(mixIns)).equals(new HashSet<>(asList(another.mixIns))))
            throw new IllegalArgumentException(String.format("Cannot merge different mix-in %s and %s for %s.%s",
                    Arrays.toString(mixIns), Arrays.toString(another.mixIns), beanClass.getName(), property));
        if (mixIns.length == 0)
            mixIns = another.mixIns;
    }

    private void mergeDefinition(QueryExpression<T> another) {
        if (definition != null && another.definition != null
                && !Objects.equals(definition, another.definition))
            throw new IllegalArgumentException(String.format("Cannot merge different definition `%s` and `%s` for %s.%s",
                    definition, another.definition, beanClass.getName(), property));
        if (definition == null)
            definition = another.definition;
    }
}
