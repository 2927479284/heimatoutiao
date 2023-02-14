package com.heima.schedule.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.common.constants.ScheduleConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.schedule.dtos.Task;
import com.heima.model.schedule.pojos.Taskinfo;
import com.heima.model.schedule.pojos.TaskinfoLogs;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.schedule.mapper.TaskinfoLogsMapper;
import com.heima.schedule.mapper.TaskinfoMapper;
import com.heima.schedule.service.TaskRefreshService;
import com.heima.schedule.service.TaskService;
import com.heima.utils.common.ProtostuffUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
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
        log.info("定时任务执行中");
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



    /**
     * 系统重新启动后，立即同步表中的待执行任务到Redis中
     */
    @PostConstruct
    public void reloadTask(){
        //1.清空redis 所有的数据 未来队列&执行队列
        //未来队列
        Set<String> setKeySet = cacheService.scan(ScheduleConstants.FUTURE + "*");
        if (!setKeySet.isEmpty()){
            cacheService.delete(setKeySet);
        }
        //执行队列
        Set<String> listKeySet = cacheService.scan(ScheduleConstants.TOPIC + "*");
        if (!listKeySet.isEmpty()){
            cacheService.delete(listKeySet);
        }
        //2.查询数据库中状态为初始化状态的任务
        List<TaskinfoLogs> taskInfoLogs = taskinfoLogsMapper.selectList(Wrappers.<TaskinfoLogs>lambdaQuery()
                .eq(TaskinfoLogs::getStatus, ScheduleConstants.SCHEDULED));
        //3.进行任务同步写进数据库
        for (TaskinfoLogs taskInfoLog : taskInfoLogs) {
            Task task = new Task();
            BeanUtils.copyProperties(taskInfoLog,task);
            addTaskToCache(task);
        }
    }


    @Override
    public Task poll(int type, int priority) {
        //1.根据type和priority组装redisKey
        String redisKey = ScheduleConstants.TOPIC + type + ":" + priority;

        //2.根据redisKey从当前任务队列（LIST）中拉取任务（右侧POP任务）
        String taskJSON = cacheService.lRightPop(redisKey);

        Task task = null;
        if(StringUtils.isNotEmpty(taskJSON)){
            task = JSON.parseObject(taskJSON, Task.class);

            byte[] parameters = task.getParameters();
            //转换成对应的类[测试用]
            WmNews wmNews = ProtostuffUtil.deserialize(parameters, WmNews.class);
            System.out.println(wmNews);

            //3.删除task_info表的记录
            taskinfoMapper.deleteById(task.getTaskId());

            //4.更新task_info_logs表记录状态为已执行（乐观锁会自动将version当做条件并更新version+1）
            TaskinfoLogs taskinfoLogs = new TaskinfoLogs();
            /**
             * 最终执行的SQL，类似于：
             * ==>  Preparing: UPDATE taskinfo_logs SET version=?, status=? WHERE task_id=? AND version=?
             * ==> Parameters: 2(Integer), 1(Integer), 1478026992393125890(Long), 1(Integer)
             * <==    Updates: 1
             */
            taskinfoLogs.setTaskId(task.getTaskId()); //更新条件1
            taskinfoLogs.setVersion(1); //更新条件2
            taskinfoLogs.setStatus(ScheduleConstants.EXECUTED); //要更新的值
            taskinfoLogsMapper.updateById(taskinfoLogs);
        }
        return task;
    }
}
