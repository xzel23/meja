package com.dua3.meja.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class Options {

  public interface Value<T> {
    T getValue();
  }

  private static class StaticValue<T> implements Value<T> {

    private final String name;
    private final T value;

    @Override
    public String toString() {
      return name;
    }

    @Override
    public T getValue() {
      return value;
    }

    public StaticValue(String name, T value) {
      this.name = name;
      this.value = value;
    }
  }

  public static <T> Value<T> value(String name, T value) {
    return new StaticValue<>(name, value);
  }

  public static <T> Value<T> value(T value) {
    return new StaticValue<>(String.valueOf(value), value);
  }

  public static <T> Value<T>[] wrap(T[] choices) {
    @SuppressWarnings("unchecked")
    Value<T>[] values = new Value[choices.length];
    for (int i=0; i<choices.length; i++) {
      values[i] = value(choices[i]);
    }
    return values;
  }

  private final Set<Option<?>> options = new LinkedHashSet<>();

  public Options() {
  }

  @SafeVarargs
  public final <T> void addOption(String name, Class<T> klass, Value<T> defaultValue, Value<T>... values) {
    options.add(new Option<>(name, klass, defaultValue, values));
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public <T> void addOption(String name, Class<T> klass, T defaultChoice, T... choices) {
    Value<T> defaultValue = value(String.valueOf(defaultChoice), defaultChoice);
    Value[] values = wrap(choices);
    options.add(new Option<>(name, klass, defaultValue, values));
  }

  public Optional<Option<?>> getOption(String name) {
    for (Option<?> o: options) {
      if (o.getName().equals(name)) {
        return Optional.of(o);
      }
    }
    return Optional.empty();
  }

  public Object getOptionValue(String name, Map<Option<?>, Value<?>> overrides) {
    Optional<Option<?>> option = getOption(name);

    // the requested option does not exist
    if (!option.isPresent()) {
      return null;
    }

    Option<?> op = option.get();
    Value<?> value = overrides.getOrDefault(op, op.getDefault());
    return value.getValue();
  }

  public List<Option<?>> asList() {
    return new ArrayList<>(options);
  }

}
