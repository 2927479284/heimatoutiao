package com.heima.schedule.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.common.constants.ScheduleConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.schedule.dtos.Task;
import com.heima.model.schedule.pojos.Taskinfo;
import com.heima.model.schedule.pojos.TaskinfoLogs;
import com.heima.schedule.mapper.TaskinfoLogsMapper;
import com.heima.schedule.mapper.TaskinfoMapper;
import com.heima.schedule.service.TaskRefreshService;
import com.heima.schedule.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
@Transactional
@Slf4j
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskinfoMapper taskinfoMapper;

    @Autowired
    private TaskinfoLogsMapper taskinfoLogsMapper;

    @Autowired
    private CacheService cacheService;

    @Override
    public long addTask(Task task) {
        //1.添加任务数据到DB中
        boolean flag = addTaskToDB(task);

        //2.添加任务数据到Redis中
        if(flag){
            addTaskToCache(task);
        }
        return task.getTaskId();
    }

    /**
     * 添加任务至redis中
     * @param task
     */
    private void addTaskToCache(Task task) {
        //1.获取当前系统时间、预设时间、任务执行时间
        long currentTime = DateTime.now().getMillis(); //系统当前时间
        long executeTime = task.getExecuteTime();//任务执行时间

        //2.如果任务执行时间<=系统当前时间，则将任务添加到Redis的当前任务队列中（list）
        if(executeTime <= currentTime){
            String redisKeyPostFix = task.getTaskType() + ":" + task.getPriority();
            String redisKey = ScheduleConstants.TOPIC + redisKeyPostFix; //最终rediskey的组成结构：  topic:1:100
            cacheService.lLeftPush(redisKey, JSON.toJSONString(task));

            log.info("任务添加到当前任务队列成功，任务ID：{}", task.getTaskId());
        } else {
            //3. 系统当前时间<如果任务执行时间，则将任务添加到Redis的未来任务队列中（zset）
            String redisKeyPostFix = task.getTaskType() + ":" + task.getPriority();
            String redisKey = ScheduleConstants.FUTURE + redisKeyPostFix; //最终rediskey的组成结构：  future:1:100
            cacheService.zAdd(redisKey,JSON.toJSONString(task), executeTime);
            log.info("任务添加到未来任务队列成功，任务ID：{}", task.getTaskId());
        }
    }

    /**
     * 添加任务至数据库中
     * @param task
     * @return
     */
    private boolean addTaskToDB(Task task) {

        try {
            //1.copy 实体
            Taskinfo taskinfo = new Taskinfo();
            BeanUtils.copyProperties(task,taskinfo);
            taskinfo.setExecuteTime(new Date(task.getExecuteTime()));
            //2.保存task_info数据
            taskinfoMapper.insert(taskinfo);
            task.setTaskId(taskinfo.getTaskId());
            //3.复制taskInfo给taskInfoLogs
            TaskinfoLogs taskinfoLogs = new TaskinfoLogs();
            BeanUtils.copyProperties(taskinfo, taskinfoLogs);
            taskinfoLogs.setVersion(1);
            taskinfoLogs.setStatus(ScheduleConstants.SCHEDULED);
            //4.保存task_info_logs数据
            taskinfoLogsMapper.insert(taskinfoLogs);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

    }

    @Autowired
    private TaskRefreshService taskRefreshService;


    @Override
    @Scheduled(cron = "*/10 * * * * ?")
    public void refreshCache() {
        String setNxLockName = "lock:refesh:cache";
        String lock = cacheService.tryLock(setNxLockName, 10 * 1000);
        //1.查询所有的key 未来队列
        if (StringUtils.isNotEmpty(lock)){
            Set<String> scans = cacheService.scan(ScheduleConstants.FUTURE + "*");
            if (!scans.isEmpty()){
                taskRefreshService.refreshCache(scans);
            }
        }

    }
}
