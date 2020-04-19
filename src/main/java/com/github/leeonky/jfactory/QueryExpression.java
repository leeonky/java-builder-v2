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
    private final List<ConditionValue> conditionValueList = new ArrayList<>();
    private String[] mixIns = new String[0];
    private String definition;
    private ConditionValue conditionValue;

    public QueryExpression(BeanClass<T> beanClass, String chain, Object value) {
        this.beanClass = beanClass;
        Matcher matcher;
        String property;
        if ((matcher = Pattern.compile("([^.]+)\\((.+)[, |,| ](.+)\\)\\.(.+)").matcher(chain)).matches()) {
            property = matcher.group(1);
            mixIns = matcher.group(2).split(", |,| ");
            definition = matcher.group(3);
            conditionValue = new ConditionValueSet(matcher.group(4), value);
        } else if ((matcher = Pattern.compile("([^.]+)\\((.+)\\)\\.(.+)").matcher(chain)).matches()) {
            property = matcher.group(1);
            definition = matcher.group(2);
            conditionValue = new ConditionValueSet(matcher.group(3), value);
        } else if ((matcher = Pattern.compile("([^.]+)\\.(.+)").matcher(chain)).matches()) {
            property = matcher.group(1);
            conditionValue = new ConditionValueSet(matcher.group(2), value);
        } else if ((matcher = Pattern.compile("([^.]+)").matcher(chain)).matches()) {
            property = matcher.group(1);
            conditionValue = new SingleValue(value);
        } else
            throw new IllegalStateException("Invalid query expression `" + chain + "`");

        if ((matcher = Pattern.compile("(.*)\\[(.*)]").matcher(property)).matches()) {
            this.property = matcher.group(1);
            int index = Integer.valueOf(matcher.group(2));
            for (int i = 0; i <= index; i++)
                conditionValueList.add(null);
            conditionValueList.set(index, conditionValue);
        } else
            this.property = property;
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
        return conditionValue.matches(propertyReader, propertyReader.getValue(object));
    }

    public void queryOrCreateNested(FactorySet factorySet, PropertiesProducer propertiesProducer) {
        conditionValue.queryOrCreateNested(factorySet, propertiesProducer, this);
    }

    private Builder<?> toBuilder(FactorySet factorySet, Class<?> propertyType, Map<String, Object> conditionValues) {
        return (definition != null ? factorySet.toBuild(definition) : factorySet.type(propertyType)).mixIn(mixIns).properties(conditionValues);
    }

    private void merge(QueryExpression<T> another) {
        mergeDefinition(another);
        mergeMixIn(another);
        conditionValue = conditionValue.merge(another.conditionValue);
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

    public ValueProducer<Object> propertyValueProducer(Object value) {
        return new ValueProducer<>(property, value);
    }

    public Builder<?> queryBuilder(FactorySet factorySet, Map<String, Object> conditionValues) {
        return toBuilder(factorySet, beanClass.getPropertyReader(property).getPropertyType(), conditionValues);
    }

    public FactoryProducer<?> propertyFactoryProducer(FactorySet factorySet, Map<String, Object> conditionValues) {
        return toBuilder(factorySet, beanClass.getPropertyWriter(property).getPropertyType(), conditionValues).producer(property);
    }

    private static abstract class ConditionValue {
        public abstract boolean matches(PropertyReader propertyReader, Object propertyValue);

        public abstract void queryOrCreateNested(FactorySet factorySet, PropertiesProducer propertiesProducer, QueryExpression<?> queryExpression);

        public ConditionValue merge(ConditionValue conditionValue) {
            return conditionValue;
        }

        protected ConditionValue mergeTo(ConditionValueSet conditionValueSet) {
            return this;
        }
    }

    private static class SingleValue extends ConditionValue {
        private final Object value;

        public SingleValue(Object value) {
            this.value = value;
        }

        @Override
        public boolean matches(PropertyReader propertyReader, Object propertyValue) {
            return Objects.equals(propertyValue, propertyReader.tryConvert(value));
        }

        @Override
        public void queryOrCreateNested(FactorySet factorySet, PropertiesProducer propertiesProducer, QueryExpression<?> queryExpression) {
            propertiesProducer.add(queryExpression.propertyValueProducer(value));
        }

    }

    private static class ConditionValueSet extends ConditionValue {
        private final Map<String, Object> conditionValues = new LinkedHashMap<>();

        public ConditionValueSet(String condition, Object value) {
            conditionValues.put(condition, value);
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean matches(PropertyReader propertyReader, Object propertyValue) {
            return conditionValues.entrySet().stream()
                    .map(conditionValue -> new QueryExpression(propertyReader.getPropertyTypeWrapper(), conditionValue.getKey(), conditionValue.getValue()))
                    .allMatch(queryExpression -> queryExpression.matches(propertyValue));
        }

        @Override
        public void queryOrCreateNested(FactorySet factorySet, PropertiesProducer propertiesProducer, QueryExpression<?> queryExpression) {
            Collection<?> collection = queryExpression.queryBuilder(factorySet, conditionValues).queryAll();
            if (collection.isEmpty())
                propertiesProducer.add(queryExpression.propertyFactoryProducer(factorySet, conditionValues));
            else
                propertiesProducer.add(queryExpression.propertyValueProducer(collection.iterator().next()));
        }

        @Override
        public ConditionValue merge(ConditionValue conditionValue) {
            return conditionValue.mergeTo(this);
        }

        @Override
        protected ConditionValue mergeTo(ConditionValueSet conditionValueSet) {
            // to keep property original sequence
            conditionValueSet.conditionValues.putAll(conditionValues);
            conditionValues.clear();
            conditionValues.putAll(conditionValueSet.conditionValues);
            return this;
        }
    }
}
