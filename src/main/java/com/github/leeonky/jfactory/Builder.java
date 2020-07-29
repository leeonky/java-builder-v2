package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

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
        return producer(null).processSpec().produce();
    }

    BeanFactoryProducer producer(String property) {
        return new BeanFactoryProducer(new Argument(property, factorySet.getSequence(beanFactory.getType()), params));
    }

    public T query() {
        Collection<T> collection = queryAll();
        return collection.isEmpty() ?
                null
                : collection.iterator().next();
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
        private List<List<List<Object>>> links = new ArrayList<>();

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
            beanFactory.getProperties().forEach((name, propertyWriter) -> {
                Producer<?> producer = factorySet.getValueFactories().getOfDefault(propertyWriter.getPropertyType())
                        .map(fieldFactory -> (Producer<?>) new ValueFactoryProducer<>(fieldFactory, argument.forNested(name)))
                        .orElseGet(() -> new CollectionProducer<>(BeanClass.create(propertyWriter.getPropertyType()), argument.forNested(name)));
                addProducer(name, producer);
            });
        }

        @SuppressWarnings("unchecked")
        public <P extends Producer<?>> P addProducer(String property, P producer) {
            return (P) propertyProducerRefs.computeIfAbsent(property, k -> new Handler<>(new SuggestedValueProducer<>(() -> null), this))
                    .changeProducer((Producer) producer);
        }

        @Override
        public T produce() {
            T value = beanFactory.create(argument);
            propertyProducerRefs.entrySet().stream().filter(e -> produceFirst(e.getValue().get()))
                    .forEach(e -> beanFactory.getType().setPropertyValue(value, e.getKey(), e.getValue().produce()));
            propertyProducerRefs.entrySet().stream().filter(e -> !produceFirst(e.getValue().get()))
                    .forEach(e -> beanFactory.getType().setPropertyValue(value, e.getKey(), e.getValue().produce()));
            factorySet.getDataRepository().save(value);
            return value;
        }

        private boolean produceFirst(Producer<?> value) {
            return value instanceof DestinedValueProducer || value instanceof SuggestedValueProducer;
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
        public Handler<?> getBy(Object key) {
            return propertyProducerRefs.get(key);
        }

        @Override
        protected void changeBy(Object key, Producer<T> producer) {
            addProducer((String) key, producer);
        }

        @Override
        protected Producer<T> changeTo(Producer<T> producer) {
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

        public Producer<T> processSpec() {
            uniqSameSubBuild();
            processDependencies();
            processLinks();
            return this;
        }

        @Override
        protected void processLinks() {
            propertyProducerRefs.forEach((k, v) -> v.get().processLinks());
            links.forEach(properties -> {
                List<List<Object>> noProducerLinks = new ArrayList<>();
                link(mapToHandlersAndCollectNoProducerLinks(properties, noProducerLinks)).ifPresent(handler ->
                        noProducerLinks.forEach(index -> createProducerForNoProducerLink(handler, index)));
            });
        }

        @SuppressWarnings("unchecked")
        private void createProducerForNoProducerLink(Handler<?> handler, List<Object> index) {
            LinkedList<Object> leftIndex = new LinkedList<>(index);
            tryGetChildBy(leftIndex).forLink(leftIndex).ifPresent(p -> {
                Producer<?> producer = handler.get();
                if (producer instanceof LinkProducer)
                    ((LinkProducer) producer).absorb(p);
                else
                    handler.changeProducer(LinkProducer.create(producer).absorb((Producer) p));
            });
        }

        private List<Handler<?>> mapToHandlersAndCollectNoProducerLinks(List<List<Object>> properties, List<List<Object>> noProducerLinks) {
            return properties.stream().map(i -> {
                Handler<?> handler = getChildBy(i);
                if (handler == null)
                    noProducerLinks.add(i);
                return handler;
            }).filter(Objects::nonNull).collect(toList());
        }

        private void uniqSameSubBuild() {
            getChildren().stream()
                    .filter(handler -> handler.get() instanceof Builder.BeanFactoryProducer)
                    .collect(Collectors.groupingBy(Handler::get))
                    .forEach((_ignore, refs) -> link(refs));
        }

        @SuppressWarnings("unchecked")
        private Optional<Handler<?>> link(List<Handler<?>> handlers) {
            return handlers.stream().reduce((r1, r2) -> r1.link((Handler) r2));
        }

        @Override
        protected void processDependencies() {
            propertyProducerRefs.forEach((k, v) -> v.get().processDependencies());
            dependencies.values().forEach(propertyDependency -> propertyDependency.processDependency(this, argument));
        }

        public void addDependency(String property, String[] properties, Function<Object[], Object> dependency) {
            List<Object> propertyIndexChain = toIndex(property);
            List<List<Object>> dependencyIndexChains = Arrays.stream(properties).map(this::toIndex).collect(toList());
            ((Builder<?>.BeanFactoryProducer) getRoot()).dependencies.put(propertyIndexChain,
                    new PropertyDependency<>(propertyIndexChain, dependencyIndexChains, deps -> dependency.apply(deps.toArray())));
        }

        private List<Object> toIndex(String property) {
            List<Object> propertyIndexChain = new ArrayList<>(getIndex());
            propertyIndexChain.addAll(Arrays.stream(property.split("[\\[\\].]")).filter(s -> !s.isEmpty()).map(s -> {
                try {
                    return Integer.valueOf(s);
                } catch (Exception ignore) {
                    return s;
                }
            }).collect(toList()));
            return propertyIndexChain;
        }

        public CollectionProducer<?> getOrAddCollectionProducer(String property) {
            Handler<?> handler = propertyProducerRefs.get(property);
            return handler == null ?
                    addProducer(property, new CollectionProducer<>(beanFactory.getType().getPropertyWriter(property).getPropertyTypeWrapper(), argument.forNested(property)))
                    : (CollectionProducer<?>) handler.get();
        }

        @Override
        public int hashCode() {
            return String.format("BeanFactory:%d;MixIn:%d;Properties:%d:TypeMixIn:%d:PropertySpec:%d",
                    beanFactory.hashCode(), mixIns.hashCode(), properties.hashCode(), typeMixIn.hashCode(), propertySpecs.hashCode()).hashCode();
        }

        private boolean equalsWithBuilder(Builder<?> builder) {
            return Objects.equals(beanFactory, builder.beanFactory)
                    && Objects.equals(mixIns, builder.mixIns)
                    && !properties.isEmpty()
                    && Objects.equals(properties, builder.properties);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Builder.BeanFactoryProducer ?
                    ((Builder<?>.BeanFactoryProducer) obj).equalsWithBuilder(Builder.this)
                    : super.equals(obj);
        }

        public void addLink(List<String> properties) {
            links.add(properties.stream().map(this::toIndex).collect(toList()));
        }
    }
}
