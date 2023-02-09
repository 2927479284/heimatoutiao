package com.heima.kafka.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProducerController {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    /**
     * 生产消息的方法
     */
    @GetMapping("/send/{key}/{value}")
    public String send(@PathVariable("key") String key, @PathVariable("value") String value){
        kafkaTemplate.send("testTopic",key, value);
        return "ok";
    }


    /**
     * 测试新的发送消息，消费端打印当前消息偏移量
     * @param value
     * @return
     */
    @GetMapping("/sendNew/{value}")
    public String sendNew(@PathVariable("value") String value){
        kafkaTemplate.send("Topic02",0,"key",value);
        return "ok";
    }
}
