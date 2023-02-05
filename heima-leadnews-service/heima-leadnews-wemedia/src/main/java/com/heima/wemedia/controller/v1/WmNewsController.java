package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.heima.wemedia.service.WmNewsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>
 * 自媒体图文内容信息表 前端控制器
 * </p>
 *
 * @author itheima
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/news")
public class WmNewsController {

    @Autowired
    private WmNewsService  wmNewsService;

    @PostMapping("/list")
    public ResponseResult list(@RequestBody WmNewsPageReqDto dto){
        return wmNewsService.list(dto);
    }

    @PostMapping("/submit")
    public ResponseResult sumbmit(@RequestBody WmNewsDto dto){
        return wmNewsService.submit(dto,dto.getStatus());
    }
}
