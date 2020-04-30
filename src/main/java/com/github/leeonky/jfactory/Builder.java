package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

public class Builder<T> {
    private final Map<String, Object> properties = new LinkedHashMap<>();
    private final Map<String, BiConsumer<Argument, BeanSpec.PropertySpec>> propertySpecs = new LinkedHashMap<>();
    private final FactorySet factorySet;
    private final BeanFactory<T> beanFactory;
    private final Map<String, Object> params = new HashMap<>();
    private final List<String> mixIns = new ArrayList<>();
    private BiConsumer<Argument, Spec> typeMixIn = (arg, spec) -> {
    };

    Builder(FactorySet factorySet, BeanFactory<T> beanFactory) {
        this.factorySet = factorySet;
        this.beanFactory = beanFactory;
    }

    public T create() {
        T object = producer(null).processSpec().produce();
        factorySet.getDataRepository().save(object);
        return object;
    }

    BeanFactoryProducer producer(String property) {
        return new BeanFactoryProducer(new Argument(property, factorySet.getSequence(beanFactory.getType()), params));
    }

    public T query() {
        Collection<T> collection = queryAll();
        return collection.isEmpty() ? null : collection.iterator().next();
    }

    public Collection<T> queryAll() {
        return factorySet.getDataRepository().query(beanFactory.getType(), properties);
    }

    public Builder<T> property(String property, Object value) {
        Builder<T> newBuilder = copy();
        newBuilder.properties.put(property, value);
        return newBuilder;
    }

    private Builder<T> copy() {
        Builder<T> newBuilder = new Builder<>(factorySet, beanFactory);
        newBuilder.properties.putAll(properties);
        newBuilder.params.putAll(params);
        newBuilder.mixIns.addAll(mixIns);
        newBuilder.typeMixIn = typeMixIn;
        newBuilder.propertySpecs.putAll(propertySpecs);
        return newBuilder;
    }

    public Builder<T> properties(Map<String, ?> properties) {
        Builder<T> newBuilder = copy();
        newBuilder.properties.putAll(properties);
        return newBuilder;
    }

    public Builder<T> param(String name, Object value) {
        Builder<T> newBuilder = copy();
        newBuilder.params.put(name, value);
        return newBuilder;
    }

    public Builder<T> params(Map<String, ?> params) {
        Builder<T> newBuilder = copy();
        newBuilder.params.putAll(params);
        return newBuilder;
    }

    public Builder<T> mixIn(String... names) {
        Builder<T> newBuilder = copy();
        newBuilder.mixIns.addAll(asList(names));
        return newBuilder;
    }

    Builder<T> mixIn(BiConsumer<Argument, Spec> mixIn) {
        Builder<T> newBuilder = copy();
        newBuilder.typeMixIn = mixIn;
        return newBuilder;
    }

    public Builder<T> spec(String property, BiConsumer<Argument, BeanSpec.PropertySpec> propertySpec) {
        Builder<T> newBuilder = copy();
        newBuilder.propertySpecs.put(property, propertySpec);
        return newBuilder;
    }

    public Builder<T> baseOn(Builder<T> base) {
        Builder<T> newBuilder = base.copy();
        newBuilder.propertySpecs.putAll(propertySpecs);
        newBuilder.properties.putAll(properties);
        return newBuilder;
    }

    class BeanFactoryProducer extends Producer<T> {
        private final Map<String, ProducerRef<?>> propertyProducerRefs = new LinkedHashMap<>();
        private final BeanClass type;
        private final Argument argument;
        private Map<List<Object>, PropertyDependency<?>> dependencies = new LinkedHashMap<>();

        public BeanFactoryProducer(Argument argument) {
            this.argument = argument;
            type = beanFactory.getType();
            beanFactory.getPropertyWriters()
                    .forEach((name, propertyWriter) ->
                            factorySet.getValueFactories().of(propertyWriter.getPropertyType()).ifPresent(fieldFactory ->
                                    add(name, new ValueFactoryProducer<>(fieldFactory, argument.forNested(name)))));
            BeanSpec beanSpec = new BeanSpec(this, factorySet, argument);
            beanFactory.collectSpec(argument, beanSpec);
            typeMixIn.accept(argument, beanSpec);
            beanFactory.collectMixInSpecs(argument, mixIns, beanSpec);
            propertySpecs.forEach((property, spec) -> spec.accept(argument, beanSpec.property(property)));
            QueryExpression.createQueryExpressions(beanFactory.getType(), properties)
                    .forEach(exp -> exp.queryOrCreateNested(factorySet, this));
        }

        @SuppressWarnings("unchecked")
        public void add(String property, Producer<?> producer) {
            propertyProducerRefs.computeIfAbsent(property, k -> new ProducerRef<>(new ValueProducer<>(() -> null)))
                    .changeProducer((Producer) producer);
            producer.setParent(this);
        }

        @Override
        @SuppressWarnings("unchecked")
        public T produce() {
            T value = beanFactory.create(argument);
            argument.setCurrent(value);
            propertyProducerRefs.forEach((k, v) -> type.setPropertyValue(value, k, v.produce()));
            factorySet.getDataRepository().save(value);
            return value;
        }

        @Override
        protected Collection<ProducerRef<?>> getChildren() {
            return collectChildren(propertyProducerRefs.values());
        }

        public BeanClass<?> getType() {
            return type;
        }

        private Producer<?> getProducer(String property) {
            ProducerRef<?> producerRef = propertyProducerRefs.get(property);
            if (producerRef == null)
                return null;
            else
                return producerRef.get();
        }

        public ProducerRef<?> getProducerRef(String property) {
            return propertyProducerRefs.get(property);
        }

        public Producer<?> getOrAdd(String property, Supplier<Producer<?>> supplier) {
            Producer<?> producer = getProducer(property);
            if (producer == null)
                add(property, producer = supplier.get());
            return producer;
        }

        @Override
        public Object indexOf(Producer<?> sub) {
            for (Map.Entry<String, ProducerRef<?>> e : propertyProducerRefs.entrySet())
                if (Objects.equals(e.getValue().get(), sub))
                    return e.getKey();
            throw new IllegalStateException();
        }

        @Override
        public int hashCode() {
            return String.format("BeanFactory:%d;MixIn:%d;Properties:%d:TypeMixIn:%d:PropertySpec:%d",
                    beanFactory.hashCode(), mixIns.hashCode(), properties.hashCode(), typeMixIn.hashCode(), propertySpecs.hashCode()).hashCode();
        }

        Builder<T> builder() {
            return Builder.this;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Builder.BeanFactoryProducer) {
                Builder.BeanFactoryProducer another = (Builder.BeanFactoryProducer) obj;
                return Objects.equals(beanFactory, another.builder().beanFactory)
                        && Objects.equals(mixIns, another.builder().mixIns)
                        && Objects.equals(typeMixIn, another.builder().typeMixIn)
                        && Objects.equals(properties, another.builder().properties)
                        && Objects.equals(propertySpecs, another.builder().propertySpecs)
                        ;
            }
            return super.equals(obj);
        }

        @Override
        public ProducerRef<?> getByIndexes(List<Object> property) {
            LinkedList<Object> leftProperty = new LinkedList<>(property);
            ProducerRef<?> producerRef = getProducerRef((String) leftProperty.removeFirst());
            if (leftProperty.isEmpty())
                return producerRef;
            else
                return producerRef.get().getByIndexes(leftProperty);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void changeByIndexes(List<Object> property, Producer<?> producer) {
            LinkedList<Object> leftProperty = new LinkedList<>(property);
            String p = (String) leftProperty.removeFirst();
            ProducerRef producerRef = getProducerRef(p);
            if (leftProperty.isEmpty()) {
                if (producerRef == null)
                    add(p, producer);
                else
                    producerRef.changeProducer(producer);
            } else
                producerRef.get().changeByIndexes(property, producer);
        }


        @Override
        public Producer<T> changeTo(Producer<T> producer) {
            return producer.changeFrom(this);
        }

        @Override
        protected Producer<T> changeFrom(BeanFactoryProducer beanFactoryProducer) {
            if (beanFactory instanceof CustomizedFactory)
                return this;
            return builder().baseOn(beanFactoryProducer.builder()).new BeanFactoryProducer(argument);
        }

        @Override
        public <T> void addDependency(List<Object> property, List<List<Object>> dependencies, Function<List<Object>, T> rule) {
            this.dependencies.put(property, new PropertyDependency<>(property, dependencies, rule));
        }

        @SuppressWarnings("unchecked")
        public Producer<T> processSpec() {
            dependencies.values().forEach(propertyDependency -> propertyDependency.processDependency(this));

            getChildren().stream()
                    .filter(ProducerRef::isBeanFactoryProducer)
                    .collect(Collectors.groupingBy(Function.identity()))
                    .forEach((_ignore, refs) -> refs.stream().reduce((r1, r2) -> r1.link((ProducerRef) r2)));
            return this;
        }
    }
}
