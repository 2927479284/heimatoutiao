package com.heima.kafka.listener;


import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.stereotype.Component;

@Component
public class ConsumerListener {

    @KafkaListener(topics = "testTopic")
    public void msg(ConsumerRecord<String,String> consumerRecord){
        System.out.println("Key："+consumerRecord.key()+"，Value："+consumerRecord.value());
    }


    @KafkaListener(topics = "Topic01")
    public void msgNew01(ConsumerRecord<String,String> consumerRecord){
        System.out.println("value："+consumerRecord.value()+"偏移量："+consumerRecord.offset());
    }


    /**
     * 读取主题Topic02分区为0的消息
     * @param consumerRecord
     */
    @KafkaListener(topicPartitions = {@TopicPartition(topic = "Topic02",partitions = {"0"})})
    public void msgNew02(ConsumerRecord<String,String> consumerRecord){
        System.out.println("key："+consumerRecord.key()+" value："+consumerRecord.value()+" 偏移量："+consumerRecord.offset());
    }
}
