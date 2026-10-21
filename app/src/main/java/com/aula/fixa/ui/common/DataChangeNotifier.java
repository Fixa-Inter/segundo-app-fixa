package com.aula.fixa.ui.common;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.concurrent.atomic.AtomicLong;

/** App-wide signal for screens that need to refresh after a successful data mutation. */
public final class DataChangeNotifier {
    private static final MutableLiveData<Long> CHANGES = new MutableLiveData<>();
    private static final AtomicLong REVISION = new AtomicLong();

    private DataChangeNotifier() { }

    public static void notifyChanged() {
        CHANGES.postValue(REVISION.incrementAndGet());
    }

    public static void observe(LifecycleOwner owner, Observer<Long> observer) {
        CHANGES.observe(owner, observer);
    }
}
