package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmMaterialDto;
import com.heima.model.wemedia.pojos.WmMaterial;
import org.springframework.web.multipart.MultipartFile;

public interface WmMaterialService extends IService<WmMaterial> {
    /**
     * 图片上传
     * @param multipartFile
     * @return
     */
    public ResponseResult uploadPicture(MultipartFile multipartFile);


    /**
     * 分页查询素材列表
     * @param dto
     * @return
     */
    public ResponseResult list(WmMaterialDto dto);

    /**
     * 素材收藏
     * @param id 素材主键ID
     * @return
     */
    ResponseResult collect(Integer id);

    /**
     * 取消收藏素材
     * @param id
     * @return
     */
    ResponseResult cancelCollect(Integer id);

    /**
     * 删除素材
     * @param id
     * @return
     */
    ResponseResult delPicture(Integer id);
}
