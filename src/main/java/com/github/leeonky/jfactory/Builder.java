package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static com.github.leeonky.jfactory.Producer.collectChildren;
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

    BeanFactoryProducer<T> producer(String property) {
        return new BeanFactoryProducer<>(factorySet, beanFactory, new Argument(property, factorySet.getSequence(beanFactory.getType()), params), this);
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

    Builder<T> specs(Map<String, BiConsumer<Argument, BeanSpec.PropertySpec>> propertySpecs) {
        Builder<T> newBuilder = copy();
        newBuilder.propertySpecs.putAll(propertySpecs);
        return newBuilder;
    }

    public Builder<T> baseOn(Builder<T> base) {
        Builder<T> newBuilder = base.copy();
        newBuilder.propertySpecs.putAll(propertySpecs);
        newBuilder.properties.putAll(properties);
        return newBuilder;
    }

    class BeanProducers {
        private final Map<String, ProducerRef<?>> propertyProducerRefs = new LinkedHashMap<>();
        private final BeanClass type;
        private final BeanFactoryProducer<?> producer;

        public BeanProducers(Argument argument, BeanFactoryProducer<T> producer) {
            type = beanFactory.getType();
            this.producer = producer;
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

        public Producer<?> getProducer() {
            return producer;
        }

        @SuppressWarnings("unchecked")
        public void add(String property, Producer<?> producer) {
            propertyProducerRefs.computeIfAbsent(property, k -> new ProducerRef<>(new ValueProducer<>(() -> null)))
                    .changeProducer((Producer) producer);
            producer.setParent(this.producer);
        }

        @SuppressWarnings("unchecked")
        public void produce(Object data) {
            propertyProducerRefs.forEach((k, v) -> type.setPropertyValue(data, k, v.produce()));
        }

        public Collection<ProducerRef<?>> getProducers() {
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
            if (obj instanceof Builder.BeanProducers) {
                Builder.BeanProducers another = (Builder.BeanProducers) obj;
                return Objects.equals(beanFactory, another.builder().beanFactory)
                        && Objects.equals(mixIns, another.builder().mixIns)
                        && Objects.equals(typeMixIn, another.builder().typeMixIn)
                        && Objects.equals(properties, another.builder().properties)
                        && Objects.equals(propertySpecs, another.builder().propertySpecs)
                        ;
            }
            return super.equals(obj);
        }
    }
}
