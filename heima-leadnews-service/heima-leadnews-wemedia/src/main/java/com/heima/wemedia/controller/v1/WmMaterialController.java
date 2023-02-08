package com.heima.wemedia.controller.v1;


import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmMaterialDto;
import com.heima.wemedia.service.WmMaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 素材控制器
 */
@RestController
@RequestMapping("/api/v1/material")
public class WmMaterialController {

    @Autowired
    private WmMaterialService wmMaterialService;

    @PostMapping("/upload_picture")
    public ResponseResult uploadPicture(MultipartFile multipartFile){
        return wmMaterialService.uploadPicture(multipartFile);
    }


    @PostMapping("/list")
    public ResponseResult list(@RequestBody WmMaterialDto dto){
        return wmMaterialService.list(dto);
    }


    /**
     * 收藏素材
     * @param id
     * @return
     */
    @GetMapping("/collect/{id}")
    public ResponseResult collect(@PathVariable("id") Integer id){
        return wmMaterialService.collect(id);
    }


    /**
     * 取消收藏素材
     * @param id
     * @return
     */
    @GetMapping("/cancel_collect/{id}")
    public ResponseResult cancelCollect(@PathVariable("id") Integer id){
        return wmMaterialService.cancelCollect(id);
    }

    /**
     * 删除素材
     * @param id
     * @return
     */
    @GetMapping("/del_picture/{id}")
    public ResponseResult delPicture(@PathVariable("id") Integer id){
        return wmMaterialService.delPicture(id);
    }

}
