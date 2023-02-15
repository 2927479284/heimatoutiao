package com.heima.comment;

import com.heima.apis.user.IUserClient;
import com.heima.model.user.pojos.ApUser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class FeignTest {


    @Autowired
    private IUserClient iUserClient;
    @Test
    public void test(){
        ApUser byId = iUserClient.findById(1);
        System.out.println(byId);
    }
}
