package com.heima.schedule.feign;

import com.heima.apis.schedule.IScheduleClient;
import com.heima.model.schedule.dtos.Task;
import com.heima.schedule.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class ScheduleClient implements IScheduleClient {

    @Autowired
    private TaskService taskService;

    @Override
    @PostMapping("/api/v1/schedule/addTask")
    public long add(Task task) {
        return taskService.addTask(task);
    }

    @Override
    @PostMapping("/api/v1/schedule/pollTask/{type}/{priority}")
    public Task pollTask(@PathVariable("type") int type, @PathVariable("priority") int priority) {
        return taskService.poll(type,priority);
    }
}
