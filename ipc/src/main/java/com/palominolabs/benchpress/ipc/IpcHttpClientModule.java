package com.palominolabs.benchpress.ipc;

import com.google.inject.AbstractModule;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;

public final class IpcHttpClientModule extends AbstractModule {
    @Override
    protected void configure() {
        AsyncHttpClientConfig.Builder builder = new AsyncHttpClientConfig.Builder();
        bind(AsyncHttpClient.class).annotatedWith(Ipc.class).toInstance(new AsyncHttpClient(builder.build()));
    }
}
