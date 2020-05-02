package com.github.leeonky.jfactory;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

public class Builder<T> {
    private final Map<String, Object> properties = new LinkedHashMap<>();
    private final Map<String, BiConsumer<Argument, BeanSpec<T>.PropertySpec>> propertySpecs = new LinkedHashMap<>();
    private final FactorySet factorySet;
    private final BeanFactory<T> beanFactory;
    private final Map<String, Object> params = new HashMap<>();
    private final List<String> mixIns = new ArrayList<>();
    private BiConsumer<Argument, Spec<T>> typeMixIn = (arg, spec) -> {
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

    Builder<T> mixIn(BiConsumer<Argument, Spec<T>> mixIn) {
        Builder<T> newBuilder = copy();
        newBuilder.typeMixIn = mixIn;
        return newBuilder;
    }

    public Builder<T> spec(String property, BiConsumer<Argument, BeanSpec<T>.PropertySpec> propertySpec) {
        Builder<T> newBuilder = copy();
        newBuilder.propertySpecs.put(property, propertySpec);
        return newBuilder;
    }

    class BeanFactoryProducer extends Producer<T> {
        private final Map<String, Handler<?>> propertyProducerRefs = new LinkedHashMap<>();
        private final Argument argument;
        private Map<List<Object>, PropertyDependency<?>> dependencies = new LinkedHashMap<>();

        public BeanFactoryProducer(Argument argument) {
            this.argument = argument;
            collectPropertyDefaultProducer(argument);
            collectSpecs(argument, new BeanSpec<>(this, factorySet, argument));
            QueryExpression.createQueryExpressions(beanFactory.getType(), properties)
                    .forEach(exp -> exp.queryOrCreateNested(factorySet, this));
        }

        private void collectSpecs(Argument argument, BeanSpec<T> beanSpec) {
            beanFactory.collectSpec(argument, beanSpec);
            typeMixIn.accept(argument, beanSpec);
            beanFactory.collectMixInSpecs(argument, mixIns, beanSpec);
            propertySpecs.forEach((property, spec) -> spec.accept(argument, beanSpec.property(property)));
        }

        private void collectPropertyDefaultProducer(Argument argument) {
            beanFactory.getProperties().forEach((name, propertyWriter) ->
                    factorySet.getValueFactories().of(propertyWriter.getPropertyType()).ifPresent(fieldFactory ->
                            addProperty(name, new ValueFactoryProducer<>(fieldFactory, argument.forNested(name)))));
        }

        @SuppressWarnings("unchecked")
        public <P extends Producer<?>> P addProperty(String property, P producer) {
            propertyProducerRefs.computeIfAbsent(property, k -> new Handler<>(new ValueProducer<>(() -> null)))
                    .changeProducer((Producer) producer);
            return (P) producer.setParent(this);
        }

        @Override
        public T produce() {
            T value = beanFactory.create(argument);
            propertyProducerRefs.forEach((k, v) -> beanFactory.getType().setPropertyValue(value, k, v.produce()));
            factorySet.getDataRepository().save(value);
            return value;
        }

        @Override
        protected Collection<Handler<?>> getChildren() {
            return collectChildren(propertyProducerRefs.values());
        }

        @Override
        public Object indexOf(Producer<?> sub) {
            for (Map.Entry<String, Handler<?>> e : propertyProducerRefs.entrySet())
                if (Objects.equals(e.getValue().get(), sub))
                    return e.getKey();
            throw new IllegalStateException();
        }

        @Override
        public int hashCode() {
            return String.format("BeanFactory:%d;MixIn:%d;Properties:%d:TypeMixIn:%d:PropertySpec:%d",
                    beanFactory.hashCode(), mixIns.hashCode(), properties.hashCode(), typeMixIn.hashCode(), propertySpecs.hashCode()).hashCode();
        }

        private boolean equalsWithBuilder(Builder<?> builder) {
            return Objects.equals(beanFactory, builder.beanFactory)
                    && Objects.equals(mixIns, builder.mixIns)
                    && Objects.equals(typeMixIn, builder.typeMixIn)
                    && Objects.equals(properties, builder.properties)
                    && Objects.equals(propertySpecs, builder.propertySpecs);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Builder.BeanFactoryProducer ?
                    ((Builder<?>.BeanFactoryProducer) obj).equalsWithBuilder(Builder.this) : super.equals(obj);
        }

        @Override
        public Handler<?> getByIndex(List<Object> index) {
            LinkedList<Object> leftProperty = new LinkedList<>(index);
            Handler<?> handler = propertyProducerRefs.get(leftProperty.removeFirst());
            if (leftProperty.isEmpty())
                return handler;
            else
                return handler.get().getByIndex(leftProperty);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void changeByIndex(List<Object> index, Producer<?> producer) {
            LinkedList<Object> leftProperty = new LinkedList<>(index);
            String p = (String) leftProperty.removeFirst();
            Handler handler = propertyProducerRefs.get(p);
            if (leftProperty.isEmpty()) {
                if (handler == null)
                    addProperty(p, producer);
                else
                    handler.changeProducer(producer);
            } else
                handler.get().changeByIndex(index, producer);
        }


        @Override
        public Producer<T> changeTo(Producer<T> producer) {
            return producer.changeFrom(this);
        }

        private BeanFactoryProducer useSpecInDefinition(Builder<T> builderInProperty, Argument argument) {
            Builder<T> newBuilder = copy();
            newBuilder.propertySpecs.putAll(builderInProperty.propertySpecs);
            newBuilder.properties.putAll(builderInProperty.properties);
            return newBuilder.new BeanFactoryProducer(argument);
        }

        @Override
        protected Producer<T> changeFrom(BeanFactoryProducer beanFactoryProducer) {
            if (beanFactory instanceof CustomizedFactory)
                return this;
            return beanFactoryProducer.useSpecInDefinition(Builder.this, argument);
        }

        @SuppressWarnings("unchecked")
        public Producer<T> processSpec() {
            dependencies.values().forEach(propertyDependency -> propertyDependency.processDependency(this));

            getChildren().stream()
                    .filter(producerRef -> producerRef.get() instanceof Builder.BeanFactoryProducer)
                    .collect(Collectors.groupingBy(Handler::get))
                    .forEach((_ignore, refs) -> refs.stream().reduce((r1, r2) -> r1.link((Handler) r2)));
            return this;
        }

        public void addDependency(String property, String[] properties, Function<Object[], Object> dependency) {
            List<Object> beanIndexes = getIndex();

            List<Object> propertyIndexChain = new ArrayList<>(beanIndexes);
            propertyIndexChain.add(property);

            List<List<Object>> dependencyIndexChains = Arrays.stream(properties).map(p -> {
                List<Object> dependencyIndexChain = new ArrayList<>(beanIndexes);
                dependencyIndexChain.add(p);
                return dependencyIndexChain;
            }).collect(Collectors.toList());

            ((Builder<?>.BeanFactoryProducer) getRoot()).dependencies.put(propertyIndexChain,
                    new PropertyDependency<>(propertyIndexChain, dependencyIndexChains, deps -> dependency.apply(deps.toArray())));
        }

        public CollectionProducer<?> getOrAddCollectionProducer(String property) {
            Handler<?> handler = propertyProducerRefs.get(property);
            return handler == null ? addProperty(property, new CollectionProducer<>(beanFactory.getType().getPropertyWriter(property).getPropertyTypeWrapper()))
                    : (CollectionProducer<?>) handler.get();
        }
    }
}
