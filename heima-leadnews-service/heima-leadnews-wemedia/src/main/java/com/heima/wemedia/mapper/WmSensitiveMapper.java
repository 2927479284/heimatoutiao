package com.heima.wemedia.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.heima.model.wemedia.pojos.WmSensitive;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 敏感词信息表 Mapper 接口
 * </p>
 *
 * @author itheima
 */
@Mapper
public interface WmSensitiveMapper extends BaseMapper<WmSensitive> {

}
