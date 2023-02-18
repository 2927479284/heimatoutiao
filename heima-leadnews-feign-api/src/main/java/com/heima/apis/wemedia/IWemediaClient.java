package com.heima.apis.wemedia;


import com.heima.model.wemedia.pojos.WmChannel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient("leadnews-wemedia")
public interface IWemediaClient {

    /**
     * 查询全部频道
     * @return
     */
    @GetMapping("/api/v1/channel/listAll")
    public List<WmChannel> listAll();

}
