package com.dua3.meja.util;

import java.util.Objects;
import java.util.function.Function;

public class NamedFunction<T, R>
        implements Function<T, R> {

    public static <T, R> NamedFunction<T, R> create(String name, Function<T, R> f) {
        return new NamedFunction<>(name, f);
    }

    private final String name;

    private final Function<T, R> f;

    public NamedFunction(String name, Function<T, R> f) {
        this.name = Objects.requireNonNull(name);
        this.f = Objects.requireNonNull(f);
    }

    @Override
    public R apply(T t) {
        return f.apply(t);
    }

    @Override
    public String toString() {
        return name;
    }
}
