package com.saraswati.institute.network;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Retrofit callback that splits the three outcomes a call really has: a successful body, an
 * HTTP error carrying the service's {@link ErrorResponse}, and a transport failure.
 *
 * <p>Both callbacks run on the main thread, as Retrofit's own do.
 *
 * @param <T> the response body type
 */
public abstract class ApiCallback<T> implements Callback<T> {

    /** 2xx with a body. {@code body} is {@code null} only for an endpoint that returns none. */
    public abstract void onSuccess(@Nullable T body);

    /** Any failure: {@link ApiError#isTransport()} distinguishes network from HTTP errors. */
    public abstract void onError(@NonNull ApiError error);

    /** Runs after {@link #onSuccess} or {@link #onError}, for clearing a loading state. */
    public void onComplete() {
    }

    @Override
    public final void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
        try {
            if (response.isSuccessful()) {
                onSuccess(response.body());
            } else {
                onError(ApiError.of(response));
            }
        } finally {
            onComplete();
        }
    }

    @Override
    public final void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
        try {
            if (call.isCanceled()) {
                return;
            }
            onError(ApiError.of(t));
        } finally {
            onComplete();
        }
    }
}
