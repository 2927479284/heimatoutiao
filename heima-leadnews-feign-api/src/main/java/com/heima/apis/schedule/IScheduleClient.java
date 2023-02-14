package com.heima.apis.schedule;


import com.heima.model.schedule.dtos.Task;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("leadnews-schedule")
public interface IScheduleClient {

    /**
     * 添加任务到DB和CACHE中
     * @param task
     * @return
     */
    @PostMapping("/api/v1/schedule/addTask")
    public long add(@RequestBody Task task);


    /**
     * 从REDIS的当前任务队列拉取任务
     * @param type
     * @param priority
     * @return
     */
    @PostMapping("/api/v1/schedule/pollTask/{type}/{priority}")
    public Task pollTask(@PathVariable("type") int type, @PathVariable("priority") int priority);

}
