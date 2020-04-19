package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface DataRepository {
    void save(Object object);

    <T> Collection<T> queryAll(Class<T> type);

    default <T> Collection<T> query(BeanClass<T> beanClass, Map<String, Object> criteria) {
        List<QueryExpression<T>> expressions = criteria.entrySet().stream()
                .map(e -> new QueryExpression<>(beanClass, e.getKey(), e.getValue()))
                .collect(Collectors.groupingBy(QueryExpression::getProperty)).values().stream()
                .map(exps -> {
                    for (int i = 1; i < exps.size(); i++)
                        exps.get(0).merge(exps.get(i));
                    return exps.get(0);
                }).collect(Collectors.toList());

//                .collect(Collectors.toList());


        return queryAll(beanClass.getType()).stream()
                .filter(o -> expressions.stream().allMatch(e -> e.matches(o)))
                .collect(Collectors.toList());
    }

    void clear();
}
