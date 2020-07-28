package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.Property;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class ValueFactories {
    private static final LocalDate LOCAL_DATE_START = LocalDate.parse("1996-01-23");
    private static final LocalDateTime LOCAL_DATE_TIME_START = LocalDateTime.parse("1996-01-23T00:00:00");
    private static final LocalTime LOCAL_TIME_START = LocalTime.parse("00:00:00");
    private static final Instant INSTANT_START = Instant.parse("1996-01-23T00:00:00Z");
    private final Map<Class<?>, Factory<?>> buildIns = new HashMap<Class<?>, Factory<?>>() {{
        put(Byte.class, new ValueFactory<>(Byte.class).construct(argument -> (byte) argument.getSequence()));
        put(Short.class, new ValueFactory<>(Short.class).construct(argument -> (short) argument.getSequence()));
        put(Integer.class, new ValueFactory<>(Integer.class).construct(Argument::getSequence));
        put(Long.class, new ValueFactory<>(Long.class).construct(argument -> (long) argument.getSequence()));
        put(Float.class, new ValueFactory<>(Float.class).construct(argument -> (float) argument.getSequence()));
        put(Double.class, new ValueFactory<>(Double.class).construct(argument -> (double) argument.getSequence()));
        put(Boolean.class, new ValueFactory<>(Boolean.class).construct(argument -> argument.getSequence() % 2 == 1));
        put(byte.class, get(Byte.class));
        put(short.class, get(Short.class));
        put(int.class, get(Integer.class));
        put(long.class, get(Long.class));
        put(float.class, get(Float.class));
        put(double.class, get(Double.class));
        put(boolean.class, get(Boolean.class));

        put(String.class, new ValueFactory<>(String.class).construct(argument ->
                (argument.getProperty() == null ?
                        "string"
                        : argument.getProperty()) + "#" + argument.getSequence()));

        put(BigInteger.class, new ValueFactory<>(BigInteger.class).construct(argument ->
                BigInteger.valueOf(argument.getSequence())));
        put(BigDecimal.class, new ValueFactory<>(BigDecimal.class).construct(argument ->
                BigDecimal.valueOf(argument.getSequence())));

        put(UUID.class, new ValueFactory<>(UUID.class).construct(argument ->
                UUID.fromString(String.format("00000000-0000-0000-0000-%012d", argument.getSequence()))));

        put(Instant.class, new ValueFactory<>(Instant.class).construct(argument ->
                INSTANT_START.plusSeconds(argument.getSequence())));
        put(Date.class, new ValueFactory<>(Date.class).construct(argument ->
                Date.from(INSTANT_START.plus(argument.getSequence(), ChronoUnit.DAYS))));
        put(LocalTime.class, new ValueFactory<>(LocalTime.class).construct(argument ->
                LOCAL_TIME_START.plusSeconds(argument.getSequence())));
        put(LocalDate.class, new ValueFactory<>(LocalDate.class).construct(argument ->
                LOCAL_DATE_START.plusDays(argument.getSequence())));
        put(LocalDateTime.class, new ValueFactory<>(LocalDateTime.class).construct(argument ->
                LOCAL_DATE_TIME_START.plusSeconds(argument.getSequence())));
        put(OffsetDateTime.class, new ValueFactory<>(OffsetDateTime.class).construct(argument ->
                INSTANT_START.plusSeconds(argument.getSequence()).atZone(ZoneId.systemDefault()).toOffsetDateTime()));
        put(ZonedDateTime.class, new ValueFactory<>(ZonedDateTime.class).construct(argument ->
                INSTANT_START.plusSeconds(argument.getSequence()).atZone(ZoneId.systemDefault())));
    }};

    @SuppressWarnings("unchecked")
    public <T> Optional<BeanFactory<T>> of(Class<T> type) {
        return Optional.ofNullable((BeanFactory<T>) buildIns.get(type));
    }

    public static class ValueFactory<T> extends BeanFactory<T> {

        public ValueFactory(Class<T> type) {
            super(BeanClass.create(type));
        }

        @Override
        public Map<String, ? extends Property<T>> getProperties() {
            return Collections.emptyMap();
        }
    }
}
