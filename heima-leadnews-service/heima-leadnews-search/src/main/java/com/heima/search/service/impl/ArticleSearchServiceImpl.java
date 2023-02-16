package com.heima.search.service.impl;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.search.dtos.UserSearchDto;
import com.heima.search.service.ArticleSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class ArticleSearchServiceImpl implements ArticleSearchService {


    @Override
    public ResponseResult search(UserSearchDto dto) throws IOException {
        return null;
    }
}
