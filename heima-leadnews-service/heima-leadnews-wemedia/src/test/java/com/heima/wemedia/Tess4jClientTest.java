package com.heima.wemedia;

import com.heima.audit.tess4j.Tess4jClient;
import com.heima.file.service.FileStorageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;

@SpringBootTest(classes = WemediaApplication.class)
@RunWith(SpringRunner.class)
public class Tess4jClientTest {

    @Autowired
    Tess4jClient tess4jClient;

    @Autowired
    FileStorageService fileStorageService;

    @Test
    public void upload() throws Exception {
        String uploadImgFile = fileStorageService.uploadImgFile("", "test.png",
                new FileInputStream("D:\\BaiduNetdiskDownload\\黑马最新\\黑马头条资料\\day04资料-【wemedia端】自媒体文章审核\\测试图片\\143.png"));
        System.out.println(uploadImgFile);
    }

    /**
     * 测试文本内容审核
     */
    @Test
    public void testScanText() throws Exception {

        byte[] bytes = fileStorageService.downLoadFile("http://192.168.200.130:9000/leadnews/2023/02/07/test.png");

        //图片识别文字审核---begin-----

        //从byte[]转换为butteredImage
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        BufferedImage imageFile = ImageIO.read(in);
        //识别图片的文字
        String result = tess4jClient.doOCR(imageFile);
        System.out.println("图片文字识别结果 "+result);
    }
}
