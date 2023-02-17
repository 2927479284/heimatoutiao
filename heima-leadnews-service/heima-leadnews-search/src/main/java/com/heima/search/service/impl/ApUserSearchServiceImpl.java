package com.heima.search.service.impl;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.search.dtos.HistorySearchDto;
import com.heima.model.search.pojos.ApUserSearch;
import com.heima.search.service.ApUserSearchService;
import com.heima.utils.common.SnowflakeIdWorker;
import com.heima.utils.common.ThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class ApUserSearchServiceImpl implements ApUserSearchService {


    @Autowired
    private MongoTemplate mongoTemplate;
    /**
     * 保存用户搜索历史记录
     * @param keyword
     */
    @Async("taskExecutor")
    @Override
    public void insert(String keyword, Integer userId) {

        //1.查询搜索记录（查询条件：用户ID和关键词）
        ApUserSearch apUserSearch = mongoTemplate.findOne(
                Query.query(Criteria.where("userId").is(userId).and("keyword").is(keyword)), ApUserSearch.class);

        //2.如果搜索记录为空，则保存搜索记录
        if(apUserSearch==null){
            apUserSearch = new ApUserSearch();
            SnowflakeIdWorker snowflakeIdWorker = new SnowflakeIdWorker(0,0);
            apUserSearch.setId(snowflakeIdWorker.nextId());//设置雪花算法ID
            apUserSearch.setUserId(userId); //用户ID
            apUserSearch.setKeyword(keyword);//搜素记录关键词
            apUserSearch.setIsDeleted(0); //未删除
            apUserSearch.setCreatedTime(new Date()); //创建时间
            apUserSearch.setUpdatedTime(new Date());//更新时间
            mongoTemplate.save(apUserSearch);
            return;
        }

        //3.如果搜索记录不为空
        //3.1 搜索记录未删除，那么就更新搜索记录时间
        if(apUserSearch.getIsDeleted()==0){
            apUserSearch.setUpdatedTime(new Date());//更新时间
            mongoTemplate.save(apUserSearch);
        } else {
            //3.2 搜索记录已删除，那么就更新搜索记录状态为未删除同时更新时间
            apUserSearch.setIsDeleted(0);//未删除状态
            apUserSearch.setUpdatedTime(new Date());//更新时间
            mongoTemplate.save(apUserSearch);
        }
    }

    @Override
    public ResponseResult findUserSearch() {
        //查询条件：条件1-用户ID、 条件2-状态为0
        Query query = Query.query(Criteria.where("userId").is(ThreadLocalUtil.getUserId()).and("isDeleted").is(0));
        query.with(Sort.by(Sort.Direction.DESC,"updatedTime")); //根据更新时间倒序
        query.limit(10); //限制查询条数
        List<ApUserSearch> userSearchList = mongoTemplate.find(query, ApUserSearch.class);
        return ResponseResult.okResult(userSearchList);
    }

    @Override
    public ResponseResult delUserSearch(HistorySearchDto dto) {
        //1.查询搜索记录判断是否为空
        ApUserSearch apUserSearch = mongoTemplate.findById(dto.getId(), ApUserSearch.class);
        if(apUserSearch==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "数据不存在无法删除");
        }

        //2.判断是否已删除过
        if(apUserSearch.getIsDeleted()==1){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_EXIST, "无需重复删除");
        }

        //3.设置删除标识
        apUserSearch.setIsDeleted(1);
        apUserSearch.setUpdatedTime(new Date());
        mongoTemplate.save(apUserSearch);

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
