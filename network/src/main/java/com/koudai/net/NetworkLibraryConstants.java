package com.koudai.net;

/**
 * Created by zhaoyu on 15/11/3.
 * 网络库常量类
 */
public final class NetworkLibraryConstants {

    public static final String KOUDAI_REQUEST_ENCODING = "geili-zip";

    public static final String ENCRYPT_TYPE = "encryType";

    public static final String ENCRYPT_TYPE_NO = "0";

    public static final String ENCRYPT_TYPE_YES = "1";

    public static final String GZIP_TYPE = "gzipType";

    public static final String GZIP_TYPE_NO = "0";

    public static final String GZIP_TYPE_YES = "1";

    public static final String ENCRYPT_STATUS = "encryStatus";

    public static final String ENCRYPT_PARAMS_KEY = "edata";

    public static final String GET = "GET";

    public static final String POST = "POST";

    public static final String CUSTOM_KEY = "custom_key";

    public static final String TMP_FILE_SUFFIX = ".tmp";

    public static final short DOWNLOAD_BREAK_POINT_CHECK_SIZE = 500;


    public static final int DEFAULT_READ_TIMEOUT = 15 * 1000;
    public static final int DEFAULT_WRITE_TIMEOUT = 25 * 1000;
    public static final int DEFAULT_CONNECT_TIMEOUT = 15 * 1000;

    public static final int POOR_READ_TIMEOUT = 35 * 1000;
    public static final int POOR_WRITE_TIMEOUT = 40 * 1000;
    public static final int POOR_CONNECT_TIMEOUT = 35 * 1000;


    public static final int MODERATE_READ_TIMEOUT = 25 * 1000;
    public static final int MODERATE_WRITE_TIMEOUT = 30 * 1000;
    public static final int MODERATE_CONNECT_TIMEOUT = 25 * 1000;

    public static final int GOOD_READ_TIMEOUT = 15 * 1000;
    public static final int GOOD_WRITE_TIMEOUT = 20 * 1000;
    public static final int GOOD_CONNECT_TIMEOUT = 15 * 1000;


    public static final int EXCELLENT_READ_TIMEOUT = 8 * 1000;
    public static final int EXCELLENT_WRITE_TIMEOUT = 12 * 1000;
    public static final int EXCELLENT_CONNECT_TIMEOUT = 8 * 1000;


    public static final int DEFAULT_MAX_REQUEST = 32;
    public static final int DEFAULT_MAX_REQUEST_PER_HOST = 4;

    public static final int POOR_MAX_REQUEST = 8;
    public static final int POOR_MAX_REQUEST_PER_HOST = 2;

    public static final int MODERATE_MAX_REQUEST = 16;
    public static final int MODERATE_MAX_REQUEST_PER_HOST = 3;

    public static final int GOOD_MAX_REQUEST = 32;
    public static final int GOOD_MAX_REQUEST_PER_HOST = 4;

    public static final int EXCELLENT_MAX_REQUEST = 64;
    public static final int EXCELLENT_MAX_REQUEST_PER_HOST = 5;


    public static final int INTERNAL_ERROR = 1000;//通用内部错误
    public static final int DOWNLOAD_FILE_ERROR_CODE = 1001;//文件下载失败的内部错误
    public static final int RESPONSE_PARSE_ERROR = 1002;//响应的json解析的内部错误
    public static final int CONNECT_FAILED_ERROR = 1002;//手机没有开网络

}
