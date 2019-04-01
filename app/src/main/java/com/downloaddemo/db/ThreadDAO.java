package com.downloaddemo.db;

import com.downloaddemo.bean.ThreadBean;

import java.util.List;

public interface ThreadDAO {
    /**
     * 插入一个线程
     * @param threadBean
     */
    public void insertThread(ThreadBean threadBean);

    /**
     * 删除线程信息
     * @param url
     */
    public void deleteThread(String url);

    /**
     * 更新下载进度
     * @param url
     * @param thread_id
     */
    public void updateThread(String url,int thread_id,int loaded);

    /**
     * 查询线程信息
     * @param url
     * @return
     */
    public List<ThreadBean> getThreads(String url);

    /**
     * 判断是否已经下载
     * @param url
     * @return
     */
    public boolean isExists(String url);
}
