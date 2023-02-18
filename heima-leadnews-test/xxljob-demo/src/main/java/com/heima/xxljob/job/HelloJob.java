package com.heima.xxljob.job;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class HelloJob {

    @Value("${server.port}")
    private String port;


    @XxlJob("demoJobHandler")
    public void helloJob(){
        System.out.println("简单任务执行了。。。。"+port);

    }

    @XxlJob("shardingJobHandler")
    public void shardingJobHandler(){
        //分片的参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();

        //业务逻辑
        List<String> list = getList();
        for (int i = 0; i < list.size(); i++) {
            if(i % shardTotal == shardIndex){
                System.out.println(list.get(i));
            }
        }
    }

    public List<String> getList(){
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            list.add("龙王："+i+"号");
        }
        return list;
    }
}