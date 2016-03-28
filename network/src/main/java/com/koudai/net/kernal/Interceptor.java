
package com.koudai.net.kernal;

import java.io.IOException;

/**
 * Observes, modifies, and potentially short-circuits requests going out and the corresponding
 * responses coming back in. Typically interceptors add, remove, or transform headers on the request
 * or response.
 */
public interface Interceptor {
    Response intercept(Chain chain) throws IOException;

    interface Chain {
        Request request();

        Response proceed(Request request) throws IOException;

        Connection connection();
    }
}

