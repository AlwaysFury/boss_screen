package com.boss.canal_client.handler;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/8/3
 */
public interface BaseHandler<T> {

    default void insert(T t) {
    }

    default void update(T before, T after) {
    }

    default void delete(T t) {
    }
}
