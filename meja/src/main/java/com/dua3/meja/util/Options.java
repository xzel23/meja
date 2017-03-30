package com.dua3.meja.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class Options {

  private final Set<Option<?>> options = new LinkedHashSet<>();

  public Options() {
  }

  @SuppressWarnings("unchecked")
  public <T> void addOption(String name, Class<T> klass, T defaultValue, T... values) {
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

  public Object getOptionValue(String name, Map<Option<?>, Object> overrides) {
    Optional<Option<?>> option = getOption(name);

    // the requested option does not exist
    if (!option.isPresent()) {
      return null;
    }

    // retrieve the override
    Object value = overrides.get(option.get());

    if (value!=null) {
      // value present in overrides -> check its type
      if (!option.get().getOptionClass().isInstance(value)) {
        throw new IllegalStateException("Incompatible types for option.");
      }
    } else {
      // fetch the default value
      Optional<Object> v = option.map(op -> op.getDefaultChoice());
      if (v.isPresent()) {
        value = v.get();
      }
    }

    return value;
  }

  public List<Option<?>> asList() {
    return new ArrayList<>(options);
  }
}
