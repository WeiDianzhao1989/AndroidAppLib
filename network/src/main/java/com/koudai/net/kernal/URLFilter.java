package com.koudai.net.kernal;

import java.io.IOException;
import java.net.URL;

/**
 * Request filter based on the request's URL.
 *
 */
public interface URLFilter {
    /**
     * Check whether request to the provided URL is permitted to be issued.
     *
     * @throws IOException if the request to the provided URL is not permitted.
     */
    void checkURLPermitted(URL url) throws IOException;
}
