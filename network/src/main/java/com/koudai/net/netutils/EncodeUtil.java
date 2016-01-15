package com.koudai.net.netutils;

import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created by zhaoyu on 15/11/6.
 */
public class EncodeUtil {

    /**
     * 参数转化时别忘记了Url Encode
     *
     * @param params
     * @param paramsEncoding
     * @return
     */
    public static byte[] encodeParameters(Map<String, String> params,
                                          String paramsEncoding) {

        StringBuilder encodedParams = new StringBuilder();

        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {

                if (entry.getKey() == null) {
                    continue;
                }

                String value = entry.getValue();
                value = (TextUtils.isEmpty(value) ? "" : value);

                encodedParams.append(URLEncoder.encode(entry.getKey(),
                        paramsEncoding));
                encodedParams.append('=');
                encodedParams.append(URLEncoder.encode(value,
                        paramsEncoding));
                encodedParams.append('&');
            }

            String body = encodedParams.toString().substring(0,
                    encodedParams.length() - 1);

            return body.getBytes(paramsEncoding);
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: "
                    + paramsEncoding, uee);
        }
    }
}
