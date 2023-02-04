package com.heima.wemedia.controller.v1;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.heima.wemedia.service.WmNewsMaterialService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>
 * 自媒体图文引用素材信息表 前端控制器
 * </p>
 *
 * @author itheima
 */
@Slf4j
@RestController
@RequestMapping("wmNewsMaterial")
public class WmNewsMaterialController {

    @Autowired
    private WmNewsMaterialService  wmNewsMaterialService;
}
