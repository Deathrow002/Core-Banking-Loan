package com.loan.config.logs;

import org.reactivestreams.Subscription;
import org.slf4j.MDC;
import reactor.core.CoreSubscriber;
import reactor.util.context.Context;
import java.util.Map;
import java.util.stream.Collectors;

public class MdcContextLifterSubscriber<T> implements CoreSubscriber<T> {

    private final CoreSubscriber<T> delegate;

    public MdcContextLifterSubscriber(CoreSubscriber<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public Context currentContext() {
        return delegate.currentContext();
    }

    @Override
    public void onSubscribe(Subscription s) {
        delegate.onSubscribe(s);
    }

    @Override
    public void onNext(T t) {
        copyToMdc(delegate.currentContext());
        delegate.onNext(t);
        MDC.clear();
    }

    @Override
    public void onError(Throwable t) {
        copyToMdc(delegate.currentContext());
        delegate.onError(t);
        MDC.clear();
    }

    @Override
    public void onComplete() {
        copyToMdc(delegate.currentContext());
        delegate.onComplete();
        MDC.clear();
    }

    private void copyToMdc(Context context) {
        Map<String, String> mdcMap = context.stream()
            .filter(e -> e.getKey() instanceof String && e.getValue() instanceof String)
            .collect(Collectors.toMap(
                e -> (String) e.getKey(),
                e -> (String) e.getValue()
            ));
        MDC.setContextMap(mdcMap);
    }
}
