package com.purplecloud.core;

import com.purplecloud.constant.Constant;

import java.util.concurrent.atomic.LongAdder;

/**
 * ClassName: DownloadInfoThread
 * Package: com.purplecloud.core
 * Description:
 *
 * @Author HuangXuSen
 * @Create 2023/10/19-16:23
 */
public class DownloadInfoThread implements Runnable{

    private long httpFileContentLength;

    public static LongAdder finishedSize = new LongAdder();

    public static volatile LongAdder downSize = new LongAdder();

    public double prevSize;

    public DownloadInfoThread(long httpFileContentLength) {
        this.httpFileContentLength = httpFileContentLength;
    }

    @Override
    public void run() {
        String httpFileSize = String.format("%.2f",httpFileContentLength / Constant.MB);

        int speed = (int)((downSize.doubleValue() - prevSize) / 1024d);
        prevSize = downSize.doubleValue();

        double remainSize = httpFileContentLength - finishedSize.doubleValue() - downSize.doubleValue();

        String remainTime = String.format("%.1f", remainSize / 1024 / speed);

        if ("Infinity".equalsIgnoreCase(remainTime))
            remainTime = "-";

        String currentFileSize = String.format("%.2f", (downSize.doubleValue() - finishedSize.doubleValue()) / Constant.MB);

        String downInfo = String.format("已下载 %smb/%smb,速度 %skb/s,剩余时间 %ss",
                currentFileSize, httpFileSize, speed, remainTime);

        System.out.print("\r");
        System.out.print(downInfo);
    }
}
