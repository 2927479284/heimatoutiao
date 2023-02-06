package com.heima.wemedia;


import com.heima.audit.aliyun.GreenTextScan;
import com.heima.audit.aliyun.SampleUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;
import java.util.UUID;

@SpringBootTest
@RunWith(SpringRunner.class)
public class WmTest {

    @Test
    public void test1(){
        String s = UUID.randomUUID().toString();
        String replace = UUID.randomUUID().toString().replace("-", "");
        System.out.println(replace);
    }

    @Autowired
    private GreenTextScan greenTextScan;
    @Autowired
    private SampleUtils sample;
    /**
     * 测试文本内容审核
     */
    @Test
    public void testScanText() {
        Map<String,String> map = sample.checkText("傻逼");
        if(map.get("suggestion").equals("block")){
            System.out.println("文本中出现违规内容......");
        } else if(map.get("suggestion").equals("review")){
            System.out.println("文本中出现不确定因素，需要人工审核");
        } else {
            System.out.println("文本内容正常");
        }
    }

    @Test
    public void test2(){
        System.out.println("aaaaa");
    }


}
