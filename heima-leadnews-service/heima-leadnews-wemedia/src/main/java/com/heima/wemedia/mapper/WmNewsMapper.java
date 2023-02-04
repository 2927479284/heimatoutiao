package com.heima.wemedia.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.heima.model.wemedia.pojos.WmNews;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 自媒体图文内容信息表 Mapper 接口
 * </p>
 *
 * @author itheima
 */
@Mapper
public interface WmNewsMapper extends BaseMapper<WmNews> {

}
