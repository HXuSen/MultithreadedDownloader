package com.purplecloud.core;

import com.purplecloud.constant.Constant;
import com.purplecloud.util.FileUtils;
import com.purplecloud.util.HttpUtils;
import com.purplecloud.util.LogUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.concurrent.*;

/**
 * ClassName: Downloader
 * Package: com.purplecloud.core
 * Description:
 *
 * @Author HuangXuSen
 * @Create 2023/10/19-15:31
 */
public class Downloader {

    public ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    public ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(Constant.THREAD_NUM,Constant.THREAD_NUM,0,TimeUnit.SECONDS,new ArrayBlockingQueue<>(Constant.THREAD_NUM));
    private CountDownLatch countDownLatch = new CountDownLatch(Constant.THREAD_NUM);

    public void download(String url){
        String fileName = HttpUtils.getHttpFileName(url);
        fileName = Constant.PATH + fileName;

        long fileContentLength = FileUtils.getFileContentLength(fileName);

        HttpURLConnection connection = null;
        DownloadInfoThread downloadInfoThread = null;
        try {
            connection = HttpUtils.getHttpURLConnection(url);

            int contentLength = connection.getContentLength();

            if (fileContentLength >= contentLength){
                LogUtils.info("{}已下载完毕，无需重新下载",fileName);
                return;
            }

            downloadInfoThread = new DownloadInfoThread(contentLength);
            scheduledExecutorService.scheduleAtFixedRate(downloadInfoThread,1,1, TimeUnit.SECONDS);

            ArrayList<Future> list = new ArrayList<>();
            split(url,list);

            countDownLatch.await();

            if (merge(fileName))
                clearTemp(fileName);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.print("\r");
            System.out.print("下载完成");
            if (connection != null)
                connection.disconnect();
            scheduledExecutorService.shutdownNow();
            poolExecutor.shutdown();
        }

        //try (
        //        InputStream input = connection.getInputStream();
        //        BufferedInputStream bis = new BufferedInputStream(input);
        //        FileOutputStream fos = new FileOutputStream(fileName);
        //        BufferedOutputStream bos = new BufferedOutputStream(fos);
        //        ){
        //    int len = -1;
        //    byte[] buf = new byte[Constant.BYTE_SIZE];
        //    while ((len = bis.read(buf)) != -1) {
        //        downloadInfoThread.downSize += len;
        //        bos.write(buf, 0, len);
        //    }
        //}catch (FileNotFoundException e){
        //    LogUtils.error("下载文件不存在{}",url);
        //}catch (Exception e){
        //    LogUtils.error("下载失败");
        //}finally {
        //    System.out.print("\r");
        //    System.out.print("下载完成");
        //    if (connection != null)
        //        connection.disconnect();
        //    scheduledExecutorService.shutdownNow();
        //}
    }

    public void split(String url, ArrayList<Future> futureList) throws IOException {
        try {
            long contentLength = HttpUtils.getHttpFileContentLength(url);
            long size = contentLength / Constant.THREAD_NUM;
            for (int i = 0; i < Constant.THREAD_NUM; i++) {
                long startPos = i * size;
                long endPos;
                if (i == Constant.THREAD_NUM-1){
                    endPos = 0;
                }else{
                    endPos = startPos + size;
                }

                if (startPos != 0)
                    startPos++;
                DownloaderTask downloaderTask = new DownloaderTask(url, startPos, endPos, i,countDownLatch);
                Future<Boolean> future = poolExecutor.submit(downloaderTask);
                futureList.add(future);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean merge(String fileName){
        LogUtils.info("开始合并文件{}",fileName);
        byte[] buf = new byte[Constant.BYTE_SIZE];
        int len = -1;
        try (RandomAccessFile accessFile = new RandomAccessFile(fileName,"rw")){
            for (int i = 0; i < Constant.THREAD_NUM; i++) {
                try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileName + ".temp" + i))){
                    while ((len = bis.read(buf)) != -1)
                        accessFile.write(buf,0,len);
                }
            }
            LogUtils.info("文件:{}合并完毕",fileName);
        }catch (Exception e){
            LogUtils.error("合并文件:{}出现错误",fileName);
            return false;
        }
        return true;
    }

    public boolean clearTemp(String fileName){
        for (int i = 0; i < Constant.THREAD_NUM; i++) {
            File file = new File(fileName + ".temp" + i);
            file.delete();
        }
        return true;
    }
}
