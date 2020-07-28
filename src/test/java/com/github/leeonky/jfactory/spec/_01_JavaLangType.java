package com.github.leeonky.jfactory.spec;

import com.github.leeonky.jfactory.FactorySet;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class _01_JavaLangType {
    private FactorySet factorySet = new FactorySet();

    @Test
    void create_default_string() {
        assertCreate(String.class, "string#1");
        assertCreate(String.class, "string#2");
    }

    private void assertCreate(Class<?> type, Object expect) {
        assertThat(factorySet.create(type)).isEqualTo(expect);
    }

    @Test
    void create_default_int() {
        assertCreate(int.class, 1);
        assertCreate(Integer.class, 2);
    }

    @Test
    void create_default_short() {
        assertCreate(short.class, (short) 1);
        assertCreate(Short.class, (short) 2);
    }

    @Test
    void create_default_byte() {
        assertCreate(byte.class, (byte) 1);
        assertCreate(Byte.class, (byte) 2);
    }

    @Test
    void create_default_long() {
        assertCreate(long.class, 1L);
        assertCreate(Long.class, 2L);
    }

    @Test
    void create_default_float() {
        assertCreate(float.class, 1.0f);
        assertCreate(Float.class, 2.0f);
    }

    @Test
    void create_default_double() {
        assertCreate(double.class, 1.0);
        assertCreate(Double.class, 2.0);
    }

    @Test
    void create_default_boolean() {
        assertCreate(boolean.class, true);
        assertCreate(Boolean.class, false);
        assertCreate(boolean.class, true);
    }

    @Test
    void create_default_big_int() {
        assertCreate(BigInteger.class, BigInteger.valueOf(1));
        assertCreate(BigInteger.class, BigInteger.valueOf(2));
    }

    @Test
    void create_default_big_decimal() {
        assertCreate(BigDecimal.class, BigDecimal.valueOf(1));
        assertCreate(BigDecimal.class, BigDecimal.valueOf(2));
    }

    @Test
    void create_default_uuid() {
        assertCreate(UUID.class, UUID.fromString("00000000-0000-0000-0000-000000000001"));
        assertCreate(UUID.class, UUID.fromString("00000000-0000-0000-0000-000000000002"));
    }

    @Test
    void create_default_instant() {
        assertCreate(Instant.class, Instant.parse("1996-01-23T00:00:01Z"));
        assertCreate(Instant.class, Instant.parse("1996-01-23T00:00:02Z"));
    }

    @Test
    void create_default_date() {
        assertCreate(Date.class, Date.from(Instant.parse("1996-01-24T00:00:00Z")));
        assertCreate(Date.class, Date.from(Instant.parse("1996-01-25T00:00:00Z")));
    }

    @Test
    void create_default_local_time() {
        assertCreate(LocalTime.class, LocalTime.parse("00:00:01"));
        assertCreate(LocalTime.class, LocalTime.parse("00:00:02"));
    }

    @Test
    void create_default_local_date() {
        assertCreate(LocalDate.class, LocalDate.parse("1996-01-24"));
        assertCreate(LocalDate.class, LocalDate.parse("1996-01-25"));
    }

    @Test
    void create_default_local_date_time() {
        assertCreate(LocalDateTime.class, LocalDateTime.parse("1996-01-23T00:00:01"));
        assertCreate(LocalDateTime.class, LocalDateTime.parse("1996-01-23T00:00:02"));
    }

    @Test
    void create_default_offset_date_time() {
        assertCreate(OffsetDateTime.class, Instant.parse("1996-01-23T00:00:01Z").atZone(ZoneId.systemDefault()).toOffsetDateTime());
        assertCreate(OffsetDateTime.class, Instant.parse("1996-01-23T00:00:02Z").atZone(ZoneId.systemDefault()).toOffsetDateTime());
    }

    @Test
    void create_default_zoned_date_time() {
        assertCreate(ZonedDateTime.class, Instant.parse("1996-01-23T00:00:01Z").atZone(ZoneId.systemDefault()));
        assertCreate(ZonedDateTime.class, Instant.parse("1996-01-23T00:00:02Z").atZone(ZoneId.systemDefault()));
    }
}
