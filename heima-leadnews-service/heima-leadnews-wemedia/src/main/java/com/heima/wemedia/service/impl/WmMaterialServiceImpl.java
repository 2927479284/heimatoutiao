package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.file.service.FileStorageService;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmMaterialDto;
import com.heima.model.wemedia.pojos.WmMaterial;
import com.heima.utils.common.ThreadLocalUtil;
import com.heima.wemedia.mapper.WmMaterialMapper;
import com.heima.wemedia.service.WmMaterialService;
import com.heima.wemedia.service.WmNewsAuditService;
import org.checkerframework.checker.guieffect.qual.UIPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

@Service
public class WmMaterialServiceImpl extends ServiceImpl<WmMaterialMapper, WmMaterial> implements WmMaterialService {


    @Autowired
    private FileStorageService fileStorageService;
    @Override
    public ResponseResult uploadPicture(MultipartFile multipartFile) {
        //保证业务严谨性，是开发经验的重要体现
        //判断业务严谨性的参考标准：值是否为空、值的格式是否合法、数据是否存在（不存在）、业务状态是否合适

        //1.判断素材的数据的值格式的合法性（不能为空、必须为图片类型）
        if (multipartFile.isEmpty() || multipartFile.getSize() == 0 ){
            return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR,"文件内容不能为空");
        }
        //2.为素材生成唯一文件名
        //2.1 文件名前缀，保证唯一，UUID值
        String fileNamePrefix = UUID.randomUUID().toString().replace("-", "");

        //2.2 获取原始文件名后缀
        String originalFilename = multipartFile.getOriginalFilename();// 获取的文件的原始文件名
        int i = originalFilename.lastIndexOf(".");
        String fileNamePostfix = originalFilename.substring(i);// 文件后缀

        //2.3 拼接完整文件名
        String fileNameFull = fileNamePrefix + fileNamePostfix;
        //3.将文件上传到MinIO，得到URL
        try {
            String url = fileStorageService.uploadImgFile("", fileNameFull, multipartFile.getInputStream());
            //4.构建素材数据，并保存
            WmMaterial wmMaterial = new WmMaterial();
            Integer userId = ThreadLocalUtil.getUserId();
            wmMaterial.setUserId(userId);//用户ID
            wmMaterial.setUrl(url);//素材在MinIO中的地址
            wmMaterial.setType(0);//素材类型-图片
            wmMaterial.setIsCollection(0);//是否被收藏-未收藏
            wmMaterial.setCreatedTime(new Date());//上传时间
            save(wmMaterial);
            //5.响应素材数据（目的是回显）
            return ResponseResult.okResult(wmMaterial);
        }catch (Exception e){
            e.printStackTrace();
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "文件上传失败");
        }
    }


    @Override
    public ResponseResult list(WmMaterialDto dto) {
        //1.设置分页参数默认值
        dto.checkParam();

        //2.拼接查询条件
        LambdaQueryWrapper<WmMaterial> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(WmMaterial::getUserId, ThreadLocalUtil.getUserId());   //拼接固定查询条件-根据当前用户ID查询
        if(dto.getIsCollection()>0){
            lambdaQueryWrapper.eq(WmMaterial::getIsCollection, dto.getIsCollection()); //拼接动态查询条件-根据是否已收藏查询
        }
        //设置根据时间排序
        lambdaQueryWrapper.orderByDesc(WmMaterial::getCreatedTime);

        //3.执行分页查询
        IPage<WmMaterial> page = new Page<>(dto.getPage(),dto.getSize());
        this.page(page, lambdaQueryWrapper);

        //4.构建分页响应结果
        ResponseResult responseResult = new PageResponseResult(dto.getPage(),dto.getSize(), page.getTotal());
        responseResult.setData(page.getRecords());//设置每一页查询到的列表数据

        return responseResult;
    }

    @Override
    public ResponseResult collect(Integer id) {
        WmMaterial wmMaterial = getOne(Wrappers.<WmMaterial>lambdaQuery().eq(WmMaterial::getId,id));
        if (wmMaterial.getIsCollection() == 0){
            wmMaterial.setIsCollection(1);
            updateById(wmMaterial);
        }
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS.getCode());
    }

    @Override
    public ResponseResult cancelCollect(Integer id) {
        WmMaterial wmMaterial = getOne(Wrappers.<WmMaterial>lambdaQuery().eq(WmMaterial::getId,id));
        if (wmMaterial.getIsCollection() == 1){
            wmMaterial.setIsCollection(0);
            updateById(wmMaterial);
        }
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS.getCode());
    }


    @Override
    public ResponseResult delPicture(Integer id) {
        WmMaterial one = getOne(Wrappers.<WmMaterial>lambdaQuery().eq(WmMaterial::getId, id));
        if (one != null){
            removeById(one);
        }
        return  ResponseResult.okResult(AppHttpCodeEnum.SUCCESS.getCode());
    }
}
