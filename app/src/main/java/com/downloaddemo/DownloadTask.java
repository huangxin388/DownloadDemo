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
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadTask {
    private Context mContext;
    private FileBean fileBean;
    private ThreadDAO dao;
    private int mFinished = 0;
    public boolean isPause = false;
    private static final String TAG = "DownloadTask";

    public DownloadTask(Context mContext,FileBean fileBean) {
        this.mContext = mContext;
        this.fileBean = fileBean;
        dao = new ThreadDAOImpl(mContext);
    }

    public void download() {
        List<ThreadBean> threadInfos = dao.getThreads(fileBean.getUrl());
        ThreadBean bean = null;
        if(threadInfos.size() == 0) {
            bean = new ThreadBean(fileBean.getId(),fileBean.getUrl(),0, fileBean.getLength(),0);
        } else {
            bean = threadInfos.get(0);
        }
        new DownloadThread(bean).start();
    }


    class DownloadThread extends Thread {
        ThreadBean threadBean;

        public DownloadThread(ThreadBean threadBean) {
            this.threadBean = threadBean;
        }

        @Override
        public void run() {
            super.run();
            //向数据库中插入信息
            if(!dao.isExists(threadBean.getUrl())) {
                dao.insertThread(threadBean);
            }
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
                        if(System.currentTimeMillis() - time > 200) {
                            time = System.currentTimeMillis();
                            intent.putExtra("loaded",mFinished*100/fileBean.getLength());
                            mContext.sendBroadcast(intent);
                        }
                        if(isPause) {
                            dao.updateThread(threadBean.getUrl(),threadBean.getId(),mFinished);
                            return;
                        }
                    }
                    intent.putExtra("loaded",mFinished*100/fileBean.getLength());
                    mContext.sendBroadcast(intent);
                    dao.deleteThread(threadBean.getUrl(),threadBean.getId());
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
}
