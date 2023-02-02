package com.heima.model.common.dtos;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.io.Serializable;

@Data
public class PageResponseResult extends ResponseResult implements Serializable {
    private long currentPage;
    private long size;
    private long total;

    public PageResponseResult(long currentPage, long size, long total) {
        this.currentPage = currentPage;
        this.size = size;
        this.total = total;
    }

    public PageResponseResult() {

    }


    public PageResponseResult(Page page) {
        this.currentPage = page.getCurrent();
        this.size = page.getSize();
        this.total = page.getTotal();
        setData(page.getRecords());
    }

}
