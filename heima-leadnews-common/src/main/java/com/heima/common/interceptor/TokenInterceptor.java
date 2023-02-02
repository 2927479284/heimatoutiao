package com.heima.common.interceptor;

import com.heima.common.exception.CustomException;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.utils.common.ThreadLocalUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TokenInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String userId = request.getHeader("userId");
        if(StringUtils.isNotBlank(userId)){
            //存入到当前线程中
            ThreadLocalUtil.setUserId(Integer.valueOf(userId));
            return true;
        }
        throw new CustomException(AppHttpCodeEnum.NEED_LOGIN);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        ThreadLocalUtil.clear();
    }
}
