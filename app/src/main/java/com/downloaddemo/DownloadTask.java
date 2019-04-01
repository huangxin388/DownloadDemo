package com.downloaddemo;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.downloaddemo.bean.FileBean;
import com.downloaddemo.bean.ThreadBean;
import com.downloaddemo.db.ThreadDAO;
import com.downloaddemo.db.ThreadDAOImpl;
import com.downloaddemo.service.DownloadService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadTask {
    private Context mContext;
    private FileBean fileBean;
    private ThreadDAO dao;//数据库封装增删改查方法的接口
    private int mFinished = 0;//记录下载进度
    public boolean isPause = false;//用来暂停线程
    private int mThreadCount;//设置的线程数量
    private static final String TAG = "DownloadTask";
    private List<DownloadThread> mThreadList;//开启的线程列表
    public static ExecutorService sExecutorService = Executors.newCachedThreadPool();//带缓存的线程池

    public DownloadTask(Context mContext,FileBean fileBean,int threadCount) {
        this.mContext = mContext;
        this.fileBean = fileBean;
        this.mThreadCount = threadCount;
        dao = new ThreadDAOImpl(mContext);
    }

    public void download() {
        List<ThreadBean> threadInfos = dao.getThreads(fileBean.getUrl());
        if(threadInfos.size() == 0) {
            //第一次下载，之前没有记录
            int length = fileBean.getLength() / mThreadCount;
            for(int i = 0;i < mThreadCount;i++) {
                ThreadBean threadBean = new ThreadBean(i,fileBean.getUrl(),i*length,(i+1)*length-1,0);
                if(i == mThreadCount - 1) {
                    threadBean.setEnd(fileBean.getLength());
                }
                threadInfos.add(threadBean);
                //向数据库中插入信息
                dao.insertThread(threadBean);
            }
        }
        mThreadList = new ArrayList<>();
        //启动多个线程进行下载
        for(ThreadBean threadBean:threadInfos) {
            DownloadThread thread = new DownloadThread(threadBean);
//            thread.start();
            //使用线程池管理线程
            DownloadTask.sExecutorService.execute(thread);
            mThreadList.add(thread);
        }
    }


    class DownloadThread extends Thread {
        ThreadBean threadBean;
        public boolean isFinished = false;//判断单个线程是否运行完毕

        public DownloadThread(ThreadBean threadBean) {
            this.threadBean = threadBean;
        }

        @Override
        public void run() {
            super.run();
            //设置下载位置
            long start = threadBean.getStart() + threadBean.getLoaded();
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .addHeader("RANGE","bytes=" + start + "-" + threadBean.getEnd())
                    .url(threadBean.getUrl())
                    .build();
            InputStream in = null;
            RandomAccessFile raf = null;
            try {
                Intent intent = new Intent(DownloadService.DOWNLOAD_UPDATE);
                Response response = client.newCall(request).execute();
                File file = new File(DownloadService.DOWNLOAD_PATH,fileBean.getFileName());
                raf = new RandomAccessFile(file,"rwd");
                raf.seek(start);
                mFinished += threadBean.getLoaded();
                long time = System.currentTimeMillis();
                //开始下载
                if(response != null && response.isSuccessful()) {
                    in = response.body().byteStream();
                    int len = -1;
                    byte[] bytes = new byte[1024*4];
                    while((len = in.read(bytes)) != -1) {
                        raf.write(bytes,0,len);
                        mFinished += len;
                        threadBean.setLoaded(threadBean.getLoaded() + len);
                        if(System.currentTimeMillis() - time > 1000) {
                            time = System.currentTimeMillis();
                            //发送广播，更新进度
                            intent.putExtra("loaded",mFinished);
                            intent.putExtra("id",fileBean.getId());
                            intent.putExtra("length",fileBean.getLength());
                            mContext.sendBroadcast(intent);
                        }
                        if(isPause) {
                            dao.updateThread(threadBean.getUrl(),threadBean.getId(),threadBean.getLoaded());
                            return;
                        }
                    }
                    isFinished = true;
                    intent.putExtra("loaded",mFinished);
                    mContext.sendBroadcast(intent);
                    checkAllThreadsFinished();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    in.close();
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 判断是否所有线程都执行完毕
     */
    public synchronized void checkAllThreadsFinished() {
        boolean allFinished = true;
        for(DownloadThread downloadThread:mThreadList) {
            if(!downloadThread.isFinished) {
                allFinished = false;
                break;
            }
        }
        if(allFinished) {
            //删除线程信息
            dao.deleteThread(fileBean.getUrl());
            //发送广播，提示下载完毕
            Intent intent = new Intent(DownloadService.DOWNLOAD_FINISHED);
            intent.putExtra("fileInfo",fileBean);
            mContext.sendBroadcast(intent);
        }
    }
}
