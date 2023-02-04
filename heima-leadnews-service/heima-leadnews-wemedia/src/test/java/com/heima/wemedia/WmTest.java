package com.heima.wemedia;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

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
}
