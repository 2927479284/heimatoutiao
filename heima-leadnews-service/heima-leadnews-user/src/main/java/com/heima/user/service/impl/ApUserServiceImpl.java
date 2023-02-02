package com.heima.user.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.dtos.LoginDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.user.mapper.ApUserMapper;
import com.heima.user.service.ApUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.utils.common.AppJwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * APP用户信息表 服务实现类
 * </p>
 *
 * @author itheima
 */
@Slf4j
@Service
public class ApUserServiceImpl extends ServiceImpl<ApUserMapper, ApUser> implements ApUserService {

    @Override
    public ResponseResult login(LoginDto dto) {

        //1.处理普通用户登录
        if(StringUtils.isNotBlank(dto.getPhone()) && StringUtils.isNotBlank(dto.getPassword())){
            //1.1 根据用户名查询用户判断是否存在
            ApUser apUser = getOne(Wrappers.<ApUser>lambdaQuery().eq(ApUser::getPhone, dto.getPhone()));
            if(apUser==null){
                return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST,"用户不存在");
            }

            //1.2 比对登录密码与表中的密文密码
            String password = apUser.getPassword(); //用户在表中的密文密码
            String loginPwd = dto.getPassword(); //用户登录的明文密码
            boolean result = BCrypt.checkpw(loginPwd, password);
            //1.3 比较失败，响应密码错误
            if(!result){
                return ResponseResult.errorResult(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR);
            }

            //1.4 比较成功，生成TOKEN
            String token = AppJwtUtil.getToken(apUser.getId().longValue());
            apUser.setPassword("");

            Map resultMap = new HashMap();
            resultMap.put("token", token);
            resultMap.put("user", apUser);

            return ResponseResult.okResult(resultMap);

        } else { //2.处理游客模式登录
            //直接为用户按照0生成TOKEN
            String token = AppJwtUtil.getToken(0L);
            Map resultMap = new HashMap();
            resultMap.put("token", token);

            return ResponseResult.okResult(resultMap);
        }
    }
}
