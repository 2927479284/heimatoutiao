package com.heima.wemedia.service.impl;

import com.heima.model.wemedia.pojos.WmNewsMaterial;
import com.heima.wemedia.mapper.WmNewsMaterialMapper;
import com.heima.wemedia.service.WmNewsMaterialService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>
 * 自媒体图文引用素材信息表 服务实现类
 * </p>
 *
 * @author itheima
 */
@Slf4j
@Service
public class WmNewsMaterialServiceImpl extends ServiceImpl<WmNewsMaterialMapper, WmNewsMaterial> implements WmNewsMaterialService {

}
