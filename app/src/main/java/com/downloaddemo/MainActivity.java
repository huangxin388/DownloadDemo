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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.downloaddemo.bean.FileBean;
import com.downloaddemo.bean.ThreadBean;
import com.downloaddemo.db.DBHelper;
import com.downloaddemo.db.ThreadDAO;
import com.downloaddemo.db.ThreadDAOImpl;
import com.downloaddemo.service.DownloadService;

public class MainActivity extends AppCompatActivity {

    private TextView tvFileName;
    private ProgressBar progressBar;
    private Button btnStop, btnStart;
    public static final String FILE_URL = "http://down.360safe.com/yunpan/360wangpan_setup_6.5.6.1288.exe";
    private BroadcastReceiver receiver;
    private static final String TAG = "MainActivity";
    private ThreadDAO threadDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermission();
        initView();
        initEvent();
    }

    private void requestPermission() {
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},0x111);
        }
    }

    private void initView() {
        tvFileName = findViewById(R.id.tv_file_name);
        progressBar = findViewById(R.id.progress_bar);
        btnStart = findViewById(R.id.btn_start);
        btnStop = findViewById(R.id.btn_stop);
        progressBar.setMax(100);
    }
    private void initEvent() {
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,DownloadService.class);
                intent.setAction(DownloadService.DOWNLOAD_START);
                FileBean bean = new FileBean(12,FILE_URL,"360云盘",2048,0);
                intent.putExtra("fileInfo",bean);
                startService(intent);
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,DownloadService.class);
                intent.setAction(DownloadService.DOWNLOAD_STTOP);
                startService(intent);
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadService.DOWNLOAD_UPDATE);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(DownloadService.DOWNLOAD_UPDATE.equals(intent.getAction())) {
                    int finish = intent.getIntExtra("loaded",0);
                    Log.d(TAG, "onReceive: progress = " + finish);
                    progressBar.setProgress(finish);
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
