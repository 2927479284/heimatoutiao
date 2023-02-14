package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.apis.schedule.IScheduleClient;
import com.heima.common.redis.CacheService;
import com.heima.model.common.enums.TaskTypeEnum;
import com.heima.model.schedule.dtos.Task;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.utils.common.ProtostuffUtil;
import com.heima.wemedia.service.WmNewsAuditService;
import com.heima.wemedia.service.WmNewsService;
import com.heima.wemedia.service.WmNewsTaskService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class WmNewsTaskServiceImpl implements WmNewsTaskService {
    @Autowired
    private IScheduleClient scheduleClient;

    @Autowired
    @Lazy
    private WmNewsAuditService wmNewsAuditService;


    @Autowired
    private CacheService cacheService;

    @Autowired
    private WmNewsService wmNewsService;


    @Override
    public void addTask(WmNews wmNews) {
        Task task = new Task();
        task.setTaskType(TaskTypeEnum.WM_NEWS.getTaskType());//任务类型
        task.setPriority(TaskTypeEnum.WM_NEWS.getPriority());//任务优先级
        task.setExecuteTime(wmNews.getPublishTime().getTime());//任务执行时间（文章的发布时间）
        task.setParameters(ProtostuffUtil.serialize(wmNews)); //任务参数

        scheduleClient.add(task);
    }



    /**
     * 从REDIS的当前任务队列拉取任务
     */
    @Scheduled(cron = "*/1 * * * * ?")
    @Override
    public void listenerPublishWmNews() {
        log.error("从REDIS的当前任务队列拉取任务定时任务执行");
        String lockName = "lock:poll:publish:news";
        String lock = cacheService.tryLock(lockName, 3 * 1000);

        if(StringUtils.isNotBlank(lock)){
            log.info("[listenerPublishWmNews]获取到分布式锁，开始执行拉取任务");

            //1.根据类型和优先级从Redis当前任务队列拉取任务
            Task task = scheduleClient.pollTask(TaskTypeEnum.WM_NEWS.getTaskType(), TaskTypeEnum.WM_NEWS.getPriority());

            if(task!=null){
                //2.从任务中获取wmNews
                byte[] wmNewsBytes = task.getParameters();
                WmNews wmNews = ProtostuffUtil.deserialize(wmNewsBytes, WmNews.class);

                int count = wmNewsService.count(Wrappers.<WmNews>lambdaQuery()
                        .eq(WmNews::getId, wmNews.getId())
                        .in(WmNews::getStatus, WmNews.Status.ADMIN_SUCCESS.getCode(), WmNews.Status.SUCCESS.getCode())
                );
                if(count>0){

                    //3.创建远程APP文章数据
                    wmNewsAuditService.saveOrUpdateApArticle(wmNews);


                    //4.修改自媒体文章状态为已发布
                    WmNews wmNewsDB = new WmNews();
                    wmNewsDB.setId(wmNews.getId());
                    wmNewsDB.setStatus(WmNews.Status.PUBLISHED.getCode());
                    wmNewsDB.setReason("已发布");
                    wmNewsService.updateById(wmNewsDB);
                }
            }
        } else {
            log.error("[listenerPublishWmNews]未获取到分布式锁"  );
        }


    }
}
