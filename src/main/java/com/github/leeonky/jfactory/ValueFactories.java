package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.GenericType;
import com.github.leeonky.util.PropertyWriter;

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
        put(Byte.class, new ValueFactory<Byte>() {
            @Override
            protected Byte newInstance(Argument argument) {
                return (byte) argument.getSequence();
            }
        });
        put(Short.class, new ValueFactory<Short>() {
            @Override
            protected Short newInstance(Argument argument) {
                return (short) argument.getSequence();
            }
        });
        put(Integer.class, new ValueFactory<Integer>() {
            @Override
            protected Integer newInstance(Argument argument) {
                return argument.getSequence();
            }
        });
        put(Long.class, new ValueFactory<Long>() {
            @Override
            protected Long newInstance(Argument argument) {
                return (long) argument.getSequence();
            }
        });
        put(Float.class, new ValueFactory<Float>() {
            @Override
            protected Float newInstance(Argument argument) {
                return (float) argument.getSequence();
            }
        });
        put(Double.class, new ValueFactory<Double>() {
            @Override
            protected Double newInstance(Argument argument) {
                return (double) argument.getSequence();
            }
        });
        put(Boolean.class, new ValueFactory<Boolean>() {
            @Override
            protected Boolean newInstance(Argument argument) {
                return argument.getSequence() % 2 == 1;
            }
        });
        put(byte.class, get(Byte.class));
        put(short.class, get(Short.class));
        put(int.class, get(Integer.class));
        put(long.class, get(Long.class));
        put(float.class, get(Float.class));
        put(double.class, get(Double.class));
        put(boolean.class, get(Boolean.class));
        put(String.class, new ValueFactory<String>() {
            @Override
            protected String newInstance(Argument argument) {
                return (argument.getProperty() == null ? "string" : argument.getProperty()) + argument.getSequence();
            }
        });
        put(BigInteger.class, new ValueFactory<BigInteger>() {
            @Override
            protected BigInteger newInstance(Argument argument) {
                return BigInteger.valueOf(argument.getSequence());
            }
        });
        put(BigDecimal.class, new ValueFactory<BigDecimal>() {
            @Override
            protected BigDecimal newInstance(Argument argument) {
                return BigDecimal.valueOf(argument.getSequence());
            }
        });
        put(UUID.class, new ValueFactory<UUID>() {
            @Override
            protected UUID newInstance(Argument argument) {
                return UUID.fromString(String.format("00000000-0000-0000-0000-%012d", argument.getSequence()));
            }
        });
        put(Instant.class, new ValueFactory<Instant>() {
            @Override
            public Instant newInstance(Argument argument) {
                return INSTANT_START.plusSeconds(argument.getSequence());
            }
        });
        put(Date.class, new ValueFactory<Date>() {
            @Override
            protected Date newInstance(Argument argument) {
                return Date.from(INSTANT_START.plus(argument.getSequence(), ChronoUnit.DAYS));
            }
        });
        put(LocalTime.class, new ValueFactory<LocalTime>() {
            @Override
            protected LocalTime newInstance(Argument argument) {
                return LOCAL_TIME_START.plusSeconds(argument.getSequence());
            }
        });
        put(LocalDate.class, new ValueFactory<LocalDate>() {
            @Override
            protected LocalDate newInstance(Argument argument) {
                return LOCAL_DATE_START.plusDays(argument.getSequence());
            }
        });
        put(LocalDateTime.class, new ValueFactory<LocalDateTime>() {
            @Override
            protected LocalDateTime newInstance(Argument argument) {
                return LOCAL_DATE_TIME_START.plusSeconds(argument.getSequence());
            }
        });
        put(OffsetDateTime.class, new ValueFactory<OffsetDateTime>() {
            @Override
            protected OffsetDateTime newInstance(Argument argument) {
                return INSTANT_START.plusSeconds(argument.getSequence()).atZone(ZoneId.systemDefault()).toOffsetDateTime();
            }
        });
        put(ZonedDateTime.class, new ValueFactory<ZonedDateTime>() {
            @Override
            protected ZonedDateTime newInstance(Argument argument) {
                return INSTANT_START.plusSeconds(argument.getSequence()).atZone(ZoneId.systemDefault());
            }
        });
    }};

    @SuppressWarnings("unchecked")
    public <T> Optional<BeanFactory<T>> of(Class<T> type) {
        return Optional.ofNullable((BeanFactory<T>) buildIns.get(type));
    }

    public static class ValueFactory<T> extends BeanFactory<T> {
        ValueFactory() {
            super(null);
        }

        @Override
        @SuppressWarnings("unchecked")
        public BeanClass<T> getType() {
            return BeanClass.create((Class<T>) GenericType.createGenericType(getClass().getGenericSuperclass()).getGenericTypeParameter(0)
                    .orElseThrow(() -> new IllegalStateException(String.format("Invalid ValueFactory declaration '%s' should specify generic type or override getType() method", getClass().getName())))
                    .getRawType());
        }

        @Override
        public Map<String, PropertyWriter<T>> getPropertyWriters() {
            return Collections.emptyMap();
        }
    }
}
