import com.heima.model.common.enums.TaskTypeEnum;
import com.heima.model.schedule.dtos.Task;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.schedule.ScheduleApplication;
import com.heima.schedule.service.TaskService;
import com.heima.utils.common.ProtostuffUtil;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@SpringBootTest(classes = ScheduleApplication.class)
@RunWith(SpringRunner.class)
public class TaskTest {
    @Autowired
    private TaskService taskService;

    @Test
    public void testAddTask(){
        for (int i = 0; i < 10; i++) {
            //待发布的文章
            WmNews wmNews = new WmNews();
            wmNews.setId(10000+i); //文章ID
            wmNews.setTitle("测试标题"+i); //文本标题
            wmNews.setContent("测试内容"+i); //文章内容
            wmNews.setPublishTime(new Date(DateTime.now().plusSeconds(i).getMillis())); //文章发布时间

            Task task = new Task();
            if(i%2==0){
                task.setTaskType(TaskTypeEnum.WM_NEWS.getTaskType()); //任务类型：自媒体文章  1001
                task.setPriority(TaskTypeEnum.WM_NEWS.getPriority()); //任务优先级：1
            } else {
                task.setTaskType(TaskTypeEnum.FETCH_NEWS.getTaskType()); //任务类型：爬虫  1001
                task.setPriority(TaskTypeEnum.FETCH_NEWS.getPriority()); //任务优先级：2
            }

            task.setParameters(ProtostuffUtil.serialize(wmNews));
            task.setExecuteTime(wmNews.getPublishTime().getTime());

            taskService.addTask(task);
        }
    }


    @Test
    public void getMilos(){
        System.out.println(DateTime.now().getMillis());// 返回时间戳 1676264707006
    }


    @Test
    public void TaskProp(){
        Task poll = taskService.poll(1, 1001);
        System.out.println("获取到的任务："+poll);
    }
}
