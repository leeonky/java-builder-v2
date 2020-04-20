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
    private String property;
    private ConditionValue conditionValue;

    public QueryExpression(BeanClass<T> beanClass, String chain, Object value) {
        this.beanClass = beanClass;
        Matcher matcher;
        if ((matcher = Pattern.compile("([^.]+)\\((.+)[, |,| ](.+)\\)\\.(.+)").matcher(chain)).matches()) {
            property = matcher.group(1);
            conditionValue = new ConditionValueSet(matcher.group(4), value, matcher.group(3), matcher.group(2).split(", |,| "));
        } else if ((matcher = Pattern.compile("([^.]+)\\((.+)\\)\\.(.+)").matcher(chain)).matches()) {
            property = matcher.group(1);
            conditionValue = new ConditionValueSet(matcher.group(3), value, matcher.group(2), new String[0]);
        } else if ((matcher = Pattern.compile("([^.]+)\\.(.+)").matcher(chain)).matches()) {
            property = matcher.group(1);
            conditionValue = new ConditionValueSet(matcher.group(2), value, null, new String[0]);
        } else if ((matcher = Pattern.compile("([^.]+)").matcher(chain)).matches()) {
            property = matcher.group(1);
            conditionValue = new SingleValue(value);
        } else
            throw new IllegalStateException("Invalid query expression `" + chain + "`");

        if ((matcher = Pattern.compile("(.*)\\[(.*)]").matcher(property)).matches()) {
            property = matcher.group(1);
            conditionValue = new CollectionConditionValue(Integer.valueOf(matcher.group(2)), conditionValue);
        }
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

    private void merge(QueryExpression<T> another) {
        conditionValue = conditionValue.merge(another.conditionValue);
    }

    @SuppressWarnings("unchecked")
    public boolean matches(Object object) {
        if (object == null)
            return false;
        PropertyReader propertyReader = beanClass.getPropertyReader(property);
        return conditionValue.matches(propertyReader, propertyReader.getValue(object));
    }

    public void queryOrCreateNested(FactorySet factorySet, BeanProducers beanProducers) {
        beanProducers.add(conditionValue.buildProducer(factorySet));
    }

    private abstract class ConditionValue {
        public abstract boolean matches(PropertyReader propertyReader, Object propertyValue);

        public abstract Producer<?> buildProducer(FactorySet factorySet);

        public abstract ConditionValue merge(ConditionValue conditionValue);

        protected ConditionValue mergeTo(SingleValue singleValue) {
            throw new IllegalArgumentException(String.format("Cannot merge different structure %s.%s", beanClass.getName(), property));
        }

        protected ConditionValue mergeTo(ConditionValueSet conditionValueSet) {
            throw new IllegalArgumentException(String.format("Cannot merge different structure %s.%s", beanClass.getName(), property));
        }

        protected ConditionValue mergeTo(CollectionConditionValue collectionConditionValue) {
            throw new IllegalArgumentException(String.format("Cannot merge different structure %s.%s", beanClass.getName(), property));
        }
    }

    private class SingleValue extends ConditionValue {
        private final Object value;

        public SingleValue(Object value) {
            this.value = value;
        }

        @Override
        public boolean matches(PropertyReader propertyReader, Object propertyValue) {
            return Objects.equals(propertyValue, propertyReader.tryConvert(value));
        }

        @Override
        public Producer<?> buildProducer(FactorySet factorySet) {
            return new ValueProducer<>(property, value);
        }

        @Override
        public ConditionValue merge(ConditionValue conditionValue) {
            return conditionValue.mergeTo(this);
        }

        @Override
        protected ConditionValue mergeTo(SingleValue singleValue) {
            return this;
        }
    }

    private class ConditionValueSet extends ConditionValue {
        private final Map<String, Object> conditionValues = new LinkedHashMap<>();
        private String[] mixIns;
        private String definition;

        public ConditionValueSet(String condition, Object value, String definition, String[] mixIns) {
            this.mixIns = mixIns;
            this.definition = definition;
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
        public Producer<?> buildProducer(FactorySet factorySet) {
            Collection<?> collection = toBuilder(factorySet, beanClass.getPropertyReader(property).getElementOrPropertyType()).queryAll();
            if (collection.isEmpty())
                return toBuilder(factorySet, beanClass.getPropertyWriter(property).getElementOrPropertyType()).producer(property);
            else
                return new ValueProducer<Object>(property, collection.iterator().next());
        }

        private Builder<?> toBuilder(FactorySet factorySet, Class<?> propertyType) {
            return (definition != null ? factorySet.toBuild(definition) : factorySet.type(propertyType)).mixIn(mixIns).properties(conditionValues);
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
            mergeMixIn(conditionValueSet);
            mergeDefinition(conditionValueSet);
            return this;
        }

        private void mergeMixIn(ConditionValueSet another) {
            if (mixIns.length != 0 && another.mixIns.length != 0
                    && !new HashSet<>(asList(mixIns)).equals(new HashSet<>(asList(another.mixIns))))
                throw new IllegalArgumentException(String.format("Cannot merge different mix-in %s and %s for %s.%s",
                        Arrays.toString(mixIns), Arrays.toString(another.mixIns), beanClass.getName(), property));
            if (mixIns.length == 0)
                mixIns = another.mixIns;
        }

        private void mergeDefinition(ConditionValueSet another) {
            if (definition != null && another.definition != null
                    && !Objects.equals(definition, another.definition))
                throw new IllegalArgumentException(String.format("Cannot merge different definition `%s` and `%s` for %s.%s",
                        definition, another.definition, beanClass.getName(), property));
            if (definition == null)
                definition = another.definition;
        }
    }

    private class CollectionConditionValue extends ConditionValue {
        private final Map<Integer, ConditionValue> conditionValueIndexMap = new LinkedHashMap<>();

        public CollectionConditionValue(int index, ConditionValue conditionValue) {
            conditionValueIndexMap.put(index, conditionValue);
        }

        @Override
        public boolean matches(PropertyReader propertyReader, Object propertyValue) {
            return false;
        }

        @Override
        public Producer<?> buildProducer(FactorySet factorySet) {
            CollectionProducer<Object> producer = new CollectionProducer<>(beanClass.getPropertyWriter(property),
                    conditionValueIndexMap.keySet().stream().max(Integer::compareTo).orElse(0) + 1);
            conditionValueIndexMap.forEach((k, v) -> producer.setElementProducer(k, v.buildProducer(factorySet)));
            return producer;
        }

        @Override
        public ConditionValue merge(ConditionValue conditionValue) {
            return conditionValue.mergeTo(this);
        }

        @Override
        protected ConditionValue mergeTo(CollectionConditionValue collectionConditionValue) {
            collectionConditionValue.conditionValueIndexMap.forEach((k, v) -> {
                ConditionValue conditionValue = conditionValueIndexMap.get(k);
                if (conditionValue != null)
                    conditionValueIndexMap.put(k, conditionValue.merge(v));
                else
                    conditionValueIndexMap.put(k, v);
            });
            return this;
        }
    }
}
