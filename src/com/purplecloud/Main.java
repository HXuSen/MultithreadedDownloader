package com.purplecloud;

import com.purplecloud.core.Downloader;
import com.purplecloud.util.LogUtils;

import java.util.Scanner;

/**
 * ClassName: Main
 * Package: com.purplecloud
 * Description:
 *
 * @Author HuangXuSen
 * @Create 2023/10/19-15:22
 */
public class Main {
    public static void main(String[] args) {
        String url = null;
        if (args == null || args.length == 0){
            for (;;){
                LogUtils.info("请输入下载地址:");
                Scanner scanner = new Scanner(System.in);
                url = scanner.next();
                if (url != null) {
                    break;
                }
            }
        }else{
            url = args[0];
        }
        Downloader downloader = new Downloader();
        downloader.download(url);
    }
}
