package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.heima.wemedia.service.WmChannelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>
 * 频道信息表 前端控制器
 * </p>
 *
 * @author itheima
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/channel")
public class WmChannelController {

    @Autowired
    private WmChannelService  wmChannelService;

    @GetMapping("/channels")
    public ResponseResult findAll(){
        return ResponseResult.okResult(wmChannelService.list());
    }
}
