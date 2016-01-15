package com.koudai.net.toolbox.processor;

import java.io.File;

/**
 * Created by zhaoyu on 15/12/3.
 */
public interface FileDownloadProcessor {

    File postProcessor(File originalFile);
}
