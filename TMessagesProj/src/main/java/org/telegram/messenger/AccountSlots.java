package org.telegram.messenger;

import java.util.function.IntConsumer;
import java.util.function.IntPredicate;

/** Persistent high-water mark; slot IDs never change when an account logs out. */
public final class AccountSlots {
    private int count;
    private final IntConsumer persist;

    public AccountSlots(int count, IntConsumer persist) {
        this.count = Math.max(1, count);
        this.persist = persist;
    }

    public synchronized int count() {
        return count;
    }

    public synchronized void ensureCount(int required) {
        if (required > count) {
            persist.accept(required);
            count = required;
        }
    }

    public int findAvailable(IntPredicate active) {
        while (true) {
            int slots = count();
            // Do not hold the registry lock while calling account controllers.
            for (int account = 0; account < slots; account++) {
                if (!active.test(account)) {
                    return account;
                }
            }
            synchronized (this) {
                if (count == slots) {
                    ensureCount(Math.addExact(count, 1));
                    return slots;
                }
            }
        }
    }
}
