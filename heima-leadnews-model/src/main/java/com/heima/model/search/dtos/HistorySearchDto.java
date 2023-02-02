package com.heima.model.search.dtos;

import com.heima.model.common.annotation.IdEncrypt;
import lombok.Data;

@Data
public class HistorySearchDto {
    /**
     * 接收搜索历史记录id
     */
    @IdEncrypt
    private Long id;
}