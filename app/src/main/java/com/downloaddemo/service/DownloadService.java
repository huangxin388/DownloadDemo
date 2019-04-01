package com.downloaddemo.service;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.downloaddemo.DownloadTask;
import com.downloaddemo.bean.FileBean;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedHashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadService extends Service {

    public static final String DOWNLOAD_START = "START";
    public static final String DOWNLOAD_STTOP = "STOP";
    public static final String DOWNLOAD_UPDATE = "UPDATE";
    public static final String DOWNLOAD_FINISHED = "FINISHED";
    private static final String TAG = "DownloadService";

    private static final int MSG_INIT = 0x110;
    public static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()
            + "/wechat";
    private Map<Integer,DownloadTask> mTasks = new LinkedHashMap<>();

    public DownloadService() {
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_INIT:
                    FileBean bean = (FileBean) msg.obj;
                    Log.d(TAG, "handleMessage: 长度为" + bean.getLength());
                    //启动下载任务
                    DownloadTask mTask = new DownloadTask(DownloadService.this,bean,3);
                    mTask.download();
                    mTasks.put(bean.getId(),mTask);
                    break;
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(DOWNLOAD_START.equals(intent.getAction())) {
            FileBean bean = (FileBean) intent.getSerializableExtra("fileInfo");
            InitThread initThread = new InitThread(bean);
            DownloadTask.sExecutorService.execute(initThread);
//            new InitThread(bean).start();
        } else if(DOWNLOAD_STTOP.equals(intent.getAction())) {
            FileBean bean = (FileBean) intent.getSerializableExtra("fileInfo");
            DownloadTask task = mTasks.get(bean.getId());
            if(task != null) {
                task.isPause = true;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    class InitThread extends Thread {
        FileBean fileBean = null;

        public InitThread(FileBean fileBean) {
            this.fileBean = fileBean;
        }

        @Override
        public void run() {
            super.run();
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(fileBean.getUrl())
                    .build();
            RandomAccessFile raf = null;
            try {
                Response response = client.newCall(request).execute();
                int length = 0;
                if(response != null && response.isSuccessful()) {
                    length = (int) response.body().contentLength();
                }
                if(length <= 0) {
                    return;
                }
                File dir = new File(DOWNLOAD_PATH);
                if(!dir.exists()) {
                    dir.mkdir();
                }
                File file = new File(dir,fileBean.getFileName());
                raf = new RandomAccessFile(file,"rwd");
                //设置文件长度
                raf.setLength(length);
                fileBean.setLength(length);
                mHandler.obtainMessage(MSG_INIT,fileBean).sendToTarget();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
