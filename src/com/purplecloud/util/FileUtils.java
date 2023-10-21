package com.purplecloud.util;

import java.io.File;

/**
 * ClassName: FileUtils
 * Package: com.purplecloud.util
 * Description:
 *
 * @Author HuangXuSen
 * @Create 2023/10/19-16:36
 */
public class FileUtils {
    public static long getFileContentLength(String path){
        File file = new File(path);
        return file.exists() && file.isFile() ? file.length() : 0;
    }
}
