package com.koudai.net.toolbox;

import java.io.File;

/**
 * Created by zhaoyu on 15/11/26.
 */
public final class UploadPart {

    private final String key;
    private final File file;
    private final String mediaType;

    public UploadPart(String key, File file, String mediaType) {
        this.key = key;
        this.file = file;
        this.mediaType = mediaType;
    }

    public File getFile() {
        return file;
    }

    public String getMediaType() {
        return mediaType;
    }

    public String getKey() {
        return key;
    }
}
