package com.dua3.meja.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.dua3.meja.util.OptionSet.Value;

public class Options {

    private static final Options EMPTY_OPTIONS = new Options(Collections.emptyMap());

    public static Options empty() {
        return EMPTY_OPTIONS;
    }

    private Map<Option<?>, Value<?>> options = new HashMap<>();

    public Options() {
        this(new HashMap<>());
    }

    private Options(Map<Option<?>, Value<?>> options) {
        this.options = options;
    }

    public Options(Options other) {
        this.options = new HashMap<>(other.options);
    }

    public Value<?> get(Option<?> op) {
        return options.getOrDefault(op, op.getDefault());
    }

    public boolean hasOption(Option<?> option) {
        return options.containsKey(option);
    }

    public void put(Option<?> option, Value<?> value) {
        Class<?> klassO = option.getOptionClass();
        Class<?> klassV = value.getValue().getClass();
        if (!(klassO.isAssignableFrom(klassV))) {
            throw new IllegalArgumentException("Incompatible value for option '" + option.getName() + "' - expected: "
                    + klassO + ", is: " + klassV);
        }
        options.put(option, value);
    }

    @Override
    public String toString() {
        return options.toString();
    }
}
