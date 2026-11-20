package org.telegram.messenger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/** Lazy account-indexed state, without a fixed array capacity. */
public final class AccountStore<T> {
    private final ConcurrentHashMap<Integer, T> values = new ConcurrentHashMap<>();
    private final Supplier<T> factory;

    public AccountStore() {
        this.factory = null;
    }

    public AccountStore(Supplier<T> factory) {
        this.factory = factory;
    }

    public T get(int account) {
        if (account < 0) {
            throw new IllegalArgumentException("Negative account slot: " + account);
        }
        return factory == null ? values.get(account) : values.computeIfAbsent(account, key -> factory.get());
    }

    public T set(int account, T value) {
        if (account < 0) {
            throw new IllegalArgumentException("Negative account slot: " + account);
        }
        if (value == null) {
            values.remove(account);
        } else {
            values.put(account, value);
        }
        return value;
    }

    public Collection<T> snapshot() {
        return new ArrayList<>(values.values());
    }
}
