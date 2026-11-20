package app.opengram.accounttests;

import android.app.Activity;
import android.app.Instrumentation;
import android.os.Bundle;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;

/** Exercises the installed release's real JNI connections without logging in new users. */
public final class AccountInstrumentation extends Instrumentation {
    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        start();
    }

    @Override
    public void onStart() {
        AtomicReference<Throwable> failure = new AtomicReference<>();
        runOnMainSync(() -> {
            try {
                Class<?> type = getTargetContext().getClassLoader()
                    .loadClass("org.telegram.tgnet.ConnectionsManager");
                Method getInstance = type.getMethod("getInstance", int.class);
                Method getDatacenter = type.getMethod("getCurrentDatacenterId");
                Method getTime = type.getMethod("getCurrentTime");
                Object account64 = getInstance.invoke(null, 64);
                Object account65 = getInstance.invoke(null, 65);
                if (account64 == account65 || account64 != getInstance.invoke(null, 64)) {
                    throw new AssertionError("Account instances are not isolated and stable");
                }
                if ((Integer) getDatacenter.invoke(account64) <= 0 ||
                    (Integer) getDatacenter.invoke(account65) <= 0 ||
                    (Integer) getTime.invoke(account64) <= 0) {
                    throw new AssertionError("High-slot JNI connection was not initialized");
                }
            } catch (Throwable error) {
                failure.set(error);
            }
        });
        Bundle result = new Bundle();
        if (failure.get() == null) {
            result.putString("result", "PASS: installed release initializes independent JNI account slots 64 and 65");
            finish(Activity.RESULT_OK, result);
        } else {
            result.putString("result", "FAIL: " + failure.get());
            finish(Activity.RESULT_CANCELED, result);
        }
    }
}
