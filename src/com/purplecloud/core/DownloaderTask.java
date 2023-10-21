package com.purplecloud.core;

import com.purplecloud.constant.Constant;
import com.purplecloud.util.HttpUtils;
import com.purplecloud.util.LogUtils;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

/**
 * ClassName: DownloaderTask
 * Package: com.purplecloud.core
 * Description:
 *
 * @Author HuangXuSen
 * @Create 2023/10/19-18:53
 */
public class DownloaderTask implements Callable<Boolean> {

    private String url;

    private long startPos;

    private long endPos;

    private int part;
    private CountDownLatch countDownLatch;

    public DownloaderTask(String url, long startPos, long endPos, int part,CountDownLatch countDownLatch) {
        this.url = url;
        this.startPos = startPos;
        this.endPos = endPos;
        this.part = part;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public Boolean call() throws Exception {
        String httpFileName = HttpUtils.getHttpFileName(url);
        httpFileName = httpFileName + ".temp" + part;
        httpFileName = Constant.PATH + httpFileName;

        HttpURLConnection httpURLConnection = HttpUtils.getHttpURLConnection(url, startPos, endPos);

        try(
                InputStream is = httpURLConnection.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is);
                RandomAccessFile accessFile = new RandomAccessFile(httpFileName,"rw");
                ){
            byte[] buf = new byte[Constant.BYTE_SIZE];
            int len = -1;
            while ((len = bis.read(buf)) != -1){
                DownloadInfoThread.downSize.add(len);
                accessFile.write(buf,0,len);
            }
        }catch (FileNotFoundException e){
            LogUtils.error("下载文件不存在{}",url);
            return false;
        }catch (Exception e){
            LogUtils.error("下载异常");
            return false;
        }finally {
            httpURLConnection.disconnect();
            countDownLatch.countDown();
        }

        return true;
    }
}
