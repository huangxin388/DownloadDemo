package com.downloaddemo.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.downloaddemo.MainActivity;
import com.downloaddemo.R;
import com.downloaddemo.bean.FileBean;
import com.downloaddemo.service.DownloadService;

import java.util.List;

public class MyAdapter extends BaseAdapter {

    private Context mContext;
    private List<FileBean> mData;
    private static final String TAG = "MyAdapter";

    public MyAdapter(Context mContext, List<FileBean> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final FileBean bean = mData.get(position);
        ViewHolder holder = null;
        if(convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item,parent,false);
            holder.btnStart = convertView.findViewById(R.id.btn_start);
            holder.btnStop = convertView.findViewById(R.id.btn_stop);
            holder.tvFileName = convertView.findViewById(R.id.tv_file_name);
            holder.progressBar = convertView.findViewById(R.id.progress_bar);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Log.d(TAG, "getView: 最大长度" + bean.getLength());
        holder.progressBar.setMax(bean.getLength());
        holder.tvFileName.setText(bean.getFileName());
        holder.btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext,DownloadService.class);
                intent.setAction(DownloadService.DOWNLOAD_START);
                intent.putExtra("fileInfo",bean);
                mContext.startService(intent);
            }
        });
        holder.btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext,DownloadService.class);
                intent.setAction(DownloadService.DOWNLOAD_STTOP);
                intent.putExtra("fileInfo",bean);
                mContext.startService(intent);
            }
        });
        Log.d(TAG, "getView: 进度为" + bean.getLoaded());
        holder.progressBar.setProgress(bean.getLoaded());

        return convertView;
    }

    public void updateProgress(int id,int loaded) {
        Log.d(TAG, "updateProgress: 下载进度" + loaded);
        mData.get(id).setLoaded(loaded);
        notifyDataSetChanged();
    }

    static class ViewHolder {
        Button btnStart,btnStop;
        TextView tvFileName;
        ProgressBar progressBar;
    }
}
