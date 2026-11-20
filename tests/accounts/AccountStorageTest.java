import org.telegram.messenger.AccountSlots;
import org.telegram.messenger.AccountStore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class AccountStorageTest {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) throws Exception {
        AccountStore<String> store = new AccountStore<>();
        for (int slot = 0; slot < 10000; slot++) store.set(slot, "account-" + slot);
        for (int slot = 0; slot < 10000; slot++) {
            check(("account-" + slot).equals(store.get(slot)), "Account data crossed slots");
        }
        store.set(7, null);
        check(store.get(7) == null && "account-8".equals(store.get(8)), "Logout cleared another slot");
        check(store.snapshot().size() == 9999, "Snapshot should contain only allocated state");
        AccountStore<Boolean> flags = new AccountStore<>(() -> false);
        check(!flags.get(50000), "New slots need primitive defaults");
        flags.set(50000, true);
        check(flags.get(50000) && !flags.get(50001), "Flags must be account-specific");

        AtomicInteger factoryCalls = new AtomicInteger();
        AccountStore<Object> locks = new AccountStore<>(() -> {
            factoryCalls.incrementAndGet();
            return new Object();
        });
        CountDownLatch start = new CountDownLatch(1);
        List<Object> observed = java.util.Collections.synchronizedList(new ArrayList<>());
        List<Thread> workers = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            Thread thread = new Thread(() -> {
                try { start.await(); } catch (InterruptedException e) { throw new AssertionError(e); }
                observed.add(locks.get(8000));
            });
            workers.add(thread);
            thread.start();
        }
        start.countDown();
        for (Thread worker : workers) worker.join();
        check(factoryCalls.get() == 1, "A slot must have exactly one synchronization lock");
        for (Object object : observed) check(object == observed.get(0), "Racing controller lock creation");

        AtomicInteger savedCount = new AtomicInteger(4);
        AccountSlots slots = new AccountSlots(savedCount.get(), savedCount::set);
        check(slots.findAvailable(account -> account != 2) == 2, "Must reuse an empty slot");
        check(savedCount.get() == 4, "Reusing a slot must not renumber existing accounts");
        for (int expected = 4; expected < 2000; expected++) {
            check(slots.findAvailable(account -> true) == expected, "Dynamic allocation stopped at a fixed limit");
        }
        AccountSlots reopened = new AccountSlots(savedCount.get(), savedCount::set);
        check(reopened.count() == 2000, "Slots must survive process restart");
        check(reopened.findAvailable(account -> account != 17) == 17, "Must reuse a logged-out slot after restart");
        reopened.ensureCount(5001);
        check(savedCount.get() == 5001, "Migration must preserve higher account IDs");
        System.out.println("PASS: dynamic allocation, persistence, isolation, slot reuse and concurrent locks");
    }
}
