package com.heima.schedule.service;

import java.util.Set;

public interface TaskRefreshService {


    /**
     * 刷新缓存中任务
     */
    void refreshCache(Set<String> zsetKeySet);
}
