package com.downloaddemo;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.downloaddemo.adapter.MyAdapter;
import com.downloaddemo.bean.FileBean;
import com.downloaddemo.bean.ThreadBean;
import com.downloaddemo.db.DBHelper;
import com.downloaddemo.db.ThreadDAO;
import com.downloaddemo.db.ThreadDAOImpl;
import com.downloaddemo.service.DownloadService;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String FILE_URL = "http://down.360safe.com/yunpan/360wangpan_setup_6.5.6.1288.exe";
    private BroadcastReceiver receiver;
    private static final String TAG = "MainActivity";

    private ListView listView;
    private List<FileBean> mData;
    private MyAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermission();
        initData();
        initView();
        initEvent();
    }

    private void initData() {
        mData = new ArrayList<>();
        FileBean bean = new FileBean(0,FILE_URL,"imooc.apk",100,0);
        FileBean bean1 = new FileBean(1,FILE_URL,"activator.exe",100,0);
        FileBean bean2 = new FileBean(2,FILE_URL,"iTunes64Setup",100,0);
        FileBean bean3 = new FileBean(3,FILE_URL,"BaiduPlayerNetSetup_100.exe",100,0);
        mData.add(bean);
        mData.add(bean1);
        mData.add(bean2);
        mData.add(bean3);
    }

    private void requestPermission() {
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},0x111);
        }
    }

    private void initView() {
        listView = findViewById(R.id.list_view);
        mAdapter = new MyAdapter(MainActivity.this,mData);
        listView.setAdapter(mAdapter);
    }
    private void initEvent() {


        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadService.DOWNLOAD_UPDATE);
        filter.addAction(DownloadService.DOWNLOAD_FINISHED);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(DownloadService.DOWNLOAD_UPDATE.equals(intent.getAction())) {
                    int finish = intent.getIntExtra("loaded",0);
                    int id = intent.getIntExtra("id",0);
                    int length = intent.getIntExtra("length",0);
                    mData.get(id).setLength(length);
                    mAdapter.updateProgress(id,finish);
                } else if(DownloadService.DOWNLOAD_FINISHED.equals(intent.getAction())) {
                    FileBean fileBean = (FileBean) intent.getSerializableExtra("fileInfo");
                    mAdapter.updateProgress(fileBean.getId(),0);
                    Toast.makeText(MainActivity.this,fileBean.getFileName() + "下载完成",Toast.LENGTH_SHORT).show();
                }
            }
        };

        registerReceiver(receiver,filter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 0x111) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                Toast.makeText(MainActivity.this,"You denied the permission",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        Intent intent = new Intent(MainActivity.this,DownloadService.class);
        stopService(intent);
    }
}
