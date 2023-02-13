package com.heima.schedule.service.impl;

import com.heima.common.constants.ScheduleConstants;
import com.heima.common.redis.CacheService;
import com.heima.schedule.service.TaskRefreshService;
import lombok.extern.log4j.Log4j2;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@Log4j2
public class TaskRefreshServiceImpl implements TaskRefreshService {
    @Autowired
    private CacheService cacheService;

    @Async("taskExecutor")
    public void refreshCache(Set<String> zsetKeySet) {
        //2.遍历KEY集合，根据每个KEY从未来队列ZSET拉取执行时间已到的任务
        for (String futureZsetKey : zsetKeySet) {

            //根据当前系统时间获取执行时间已到的任务集合
            Set<String> taskSets = cacheService.zRangeByScore(futureZsetKey, 0, DateTime.now().getMillis());

            //基于pipeline进行更新操作
            List<Object> objectList = cacheService.getstringRedisTemplate().executePipelined(new RedisCallback<Object>() {
                @Override
                public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {

                    if (!taskSets.isEmpty()) {
                        //3.如果任务拉取到不为空，说明任务时间已到，则将任务从未来队列ZSET删除且添加到当前队列LIST

                        //将任务集合从未来队列删除
                        cacheService.zRemove(futureZsetKey, taskSets);

                        //通过未来队列的redisKey获取当前队列的redisKey（他们区别在于前缀不同，后缀相同）

                        //futureZsetKey未来队列的KEY格式：   topic:101:1
                        String[] arr = futureZsetKey.split(ScheduleConstants.FUTURE);
                        String redisPostFix = arr[1]; //获取redisKey共同的后缀

                        //topicListKey当前队列的KEY格式：   topic:101:1
                        String topicListKey = ScheduleConstants.TOPIC + redisPostFix;

                        //将任务集合添加到未来队列
                        cacheService.lLeftPushAll(topicListKey, taskSets);

                    }
                    cacheService.delete("cache:task:refresh:lock");
                    return null;
                }
            });
            log.info("[刷新任务]执行完毕..........");
        }
    }
}
