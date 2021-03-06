package com.dua3.meja.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.dua3.meja.util.OptionSet.Value;

/**
 * An Option that changes the behavior of other classes. This class is intended
 * to be used to create settings dialogs at runtime.
 *
 * @param <T> the type of this option's values
 */
public class Option<T> {

    private final String name;
    private final Class<T> klass;
    private final List<Value<T>> choices;
    private final Value<T> defaultChoice;

    @SafeVarargs
    public Option(String name, Class<T> klass, Value<T> defaultChoice, Value<T>... choices) {
        this.name = Objects.requireNonNull(name);
        this.klass = Objects.requireNonNull(klass);
        this.defaultChoice = defaultChoice;

        // make sure this.choices does not contain a duplicate for defaultChoice
        List<Value<T>> choices_ = Arrays.asList(choices);
        if (choices_.contains(defaultChoice)) {
            this.choices = Arrays.asList(choices);
        } else {
            List<Value<T>> allChoices = new ArrayList<>(choices.length + 1);
            allChoices.add(defaultChoice);
            allChoices.addAll(choices_);
            this.choices = allChoices;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Option<?> other = (Option<?>) obj;
        return name.equals(other.name) && klass.equals(other.klass);
    }

    public List<Value<T>> getChoices() {
        return Collections.unmodifiableList(choices);
    }

    public Value<T> getDefault() {
        return defaultChoice;
    }

    public String getName() {
        return name;
    }

    public Class<T> getOptionClass() {
        return klass;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, klass);
    }

    @Override
    public String toString() {
        return name + "[" + klass + ",default=" + getDefault() + "]";
    }

}
