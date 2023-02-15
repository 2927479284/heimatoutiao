package com.heima.app.gateway.filter;


import com.heima.utils.common.AppJwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class AuthorizeFilter implements Ordered, GlobalFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //1.获取request和response对象
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        String path = request.getURI().getPath();
        //2.判断是否是登录接口，是直接放行
        if (request.getURI().getPath().equals("/user/api/v1/login/login_auth/")){
            return chain.filter(exchange);
        }
        //3.非登录接口，获取token
        String token = request.getHeaders().getFirst("token");

        //4.判断token是否存在，不存在返回401(无权访问)
        if (StringUtils.isBlank(token)){
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        //5.判断token是否有效，无效返回401(无权访问)
        Claims claimsBody = AppJwtUtil.getClaimsBody(token);
        int result = AppJwtUtil.verifyToken(claimsBody);
        if (result == 0){
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        //获取TOKEN载荷里的用户ID
        String userId = String.valueOf(claimsBody.get("id"));
        //将用户ID设置到请求头
        request.mutate().header("userId",userId);
        //6.放行
        return chain.filter(exchange);
    }

    /**
     * 优先级设置  值越小  优先级越高
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
