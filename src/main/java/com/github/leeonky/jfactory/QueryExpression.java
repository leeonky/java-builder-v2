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
        String[] propertyAndCondition = chain.split("\\.", 2);
        boolean intently = propertyAndCondition[0].endsWith("!");
        propertyAndCondition[0] = propertyAndCondition[0].replace("!", "");
        String[] propertyAndDefinitionMixIns = propertyAndCondition[0].split("[()]");
        property = propertyAndDefinitionMixIns[0];

        String[] mixIn;
        String definition;
        if (propertyAndDefinitionMixIns.length > 1) {
            String[] mixInAndDefinition = propertyAndDefinitionMixIns[1].split(", |,| ");
            mixIn = Arrays.copyOf(mixInAndDefinition, mixInAndDefinition.length - 1);
            definition = mixInAndDefinition[mixInAndDefinition.length - 1];
        } else {
            mixIn = new String[0];
            definition = null;
        }

        conditionValue = propertyAndCondition.length > 1 ?
                new ConditionValueSet(propertyAndCondition[1], value, definition, mixIn)
                : new SingleValue(value, mixIn, definition);

        conditionValue.setIntently(intently);

        Matcher matcher;
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
        return conditionValue.matches(propertyReader.getElementOrPropertyType(), propertyReader.getValue(object));
    }

    public void queryOrCreateNested(FactorySet factorySet, BeanProducers beanProducers) {
        beanProducers.add(property, conditionValue.buildProducer(factorySet));
    }

    private abstract class ConditionValue {
        private boolean intently = false;

        public abstract boolean matches(Class<?> type, Object propertyValue);

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

        public boolean isIntently() {
            return intently;
        }

        public void setIntently(boolean intently) {
            this.intently = intently;
        }
    }

    private class SingleValue extends ConditionValue {
        private final Object value;
        private final String[] mixIns;
        private final String definition;

        public SingleValue(Object value, String[] mixIns, String definition) {
            this.value = value;
            this.mixIns = mixIns;
            this.definition = definition;
        }

        @Override
        public boolean matches(Class<?> type, Object propertyValue) {
            return !isIntently() && Objects.equals(propertyValue, beanClass.getConverter().tryConvert(type, value));
        }

        @Override
        public Producer<?> buildProducer(FactorySet factorySet) {
            if (isIntently())
                return toBuilder(factorySet, beanClass.getPropertyWriter(property).getElementOrPropertyType()).producer(property);
            return new ValueProducer<>(() -> value);
        }

        @Override
        public ConditionValue merge(ConditionValue conditionValue) {
            return conditionValue.mergeTo(this);
        }

        @Override
        protected ConditionValue mergeTo(SingleValue singleValue) {
            return this;
        }

        private Builder<?> toBuilder(FactorySet factorySet, Class<?> propertyType) {
            return (definition != null ? factorySet.toBuild(definition) : factorySet.type(propertyType))
                    .mixIn(mixIns);
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
        public boolean matches(Class<?> type, Object propertyValue) {
            return conditionValues.entrySet().stream()
                    .map(conditionValue -> new QueryExpression(BeanClass.create(type), conditionValue.getKey(), conditionValue.getValue()))
                    .allMatch(queryExpression -> queryExpression.matches(propertyValue));
        }

        @Override
        public Producer<?> buildProducer(FactorySet factorySet) {
            if (isIntently())
                return toBuilder(factorySet, beanClass.getPropertyWriter(property).getElementOrPropertyType()).producer(property);
            Collection<?> collection = toBuilder(factorySet, beanClass.getPropertyReader(property).getElementOrPropertyType()).queryAll();
            if (collection.isEmpty())
                return toBuilder(factorySet, beanClass.getPropertyWriter(property).getElementOrPropertyType()).producer(property);
            else
                return new ValueProducer<>(() -> collection.iterator().next());
        }

        private Builder<?> toBuilder(FactorySet factorySet, Class<?> propertyType) {
            return (definition != null ? factorySet.toBuild(definition) : factorySet.type(propertyType))
                    .mixIn(mixIns).properties(conditionValues);
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
            setIntently(isIntently() || conditionValueSet.isIntently());
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
        public boolean matches(Class<?> type, Object propertyValue) {
            List<Object> elements = BeanClass.getElements(propertyValue).orElseThrow(IllegalStateException::new)
                    .collect(Collectors.toList());
            return conditionValueIndexMap.entrySet().stream()
                    .allMatch(e -> e.getValue().matches(type, elements.get(e.getKey())));
        }

        @Override
        public Producer<?> buildProducer(FactorySet factorySet) {
            CollectionProducer<Object> producer = new CollectionProducer<>(beanClass.getPropertyWriter(property).getPropertyTypeWrapper());
            conditionValueIndexMap.forEach((k, v) -> producer.setElementProducer(k, v.buildProducer(factorySet)));
            return producer;
        }

        @Override
        public ConditionValue merge(ConditionValue conditionValue) {
            return conditionValue.mergeTo(this);
        }

        @Override
        protected ConditionValue mergeTo(CollectionConditionValue collectionConditionValue) {
            collectionConditionValue.conditionValueIndexMap.forEach((k, v) ->
                    conditionValueIndexMap.put(k, conditionValueIndexMap.containsKey(k) ? conditionValueIndexMap.get(k).merge(v) : v));
            return this;
        }
    }
}
