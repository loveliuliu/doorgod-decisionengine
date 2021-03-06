/*
 *
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */
package com.ymatou.doorgod.decisionengine.service.job.persistence;

import static com.ymatou.doorgod.decisionengine.constants.Constants.*;
import static com.ymatou.doorgod.decisionengine.constants.Constants.PerformanceServiceEnum.MONGO_SAVE_1000SAMPLE_RULE;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;
import com.ymatou.doorgod.decisionengine.model.Sample;
import com.ymatou.performancemonitorclient.PerformanceStatisticContainer;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Component;

import com.ymatou.doorgod.decisionengine.config.props.BizProps;
import com.ymatou.doorgod.decisionengine.constants.Constants;
import com.ymatou.doorgod.decisionengine.holder.RuleHolder;
import com.ymatou.doorgod.decisionengine.model.LimitTimesRule;
import com.ymatou.doorgod.decisionengine.model.mongo.MongoSamplePo;
import com.ymatou.doorgod.decisionengine.repository.MongoSampleRepository;
import com.ymatou.doorgod.decisionengine.util.MongoTemplate;
import com.ymatou.doorgod.decisionengine.util.RedisHelper;
import com.ymatou.doorgod.decisionengine.util.Utils;

/**
 * FIXME:check biz rule
 * @author qianmin 2016年9月12日 上午11:05:19
 * 
 */
@Component
public class LimitTimesRuleSampleMongoPersistenceExecutor implements Job {

    private static final Logger logger = LoggerFactory.getLogger(LimitTimesRuleSampleMongoPersistenceExecutor.class);

    @Autowired
    private BizProps bizProps;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private MongoSampleRepository sampleUnionRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    public void execute(JobExecutionContext context) throws JobExecutionException {
        LocalDateTime now = LocalDateTime.now();

        logger.info("exec job persist");
        for (LimitTimesRule rule : RuleHolder.limitTimesRules.values()) {
            persistUnionSample(rule, now);
        }
        logger.info("presist redis data to mongodb. {}", now.format(FORMATTER_YMDHMS));
    }

    /**
     * 每一分钟持久化一次访问记录(各个rule的sample)
     * 
     * @param rule
     * @param now
     */
    public void persistUnionSample(LimitTimesRule rule, LocalDateTime now) {

        // 合并Redis以备持久化
        String currentBucket = RedisHelper.getUnionSetName(rule.getName(), now.format(FORMATTER_YMDHMS), MONGO_UNION);
        List<String> timeBuckets = Utils.getAllTimeBucket(rule.getName(), now, 60);
        redisTemplate.opsForZSet().unionAndStore(RedisHelper.getEmptySetName(EMPTY_SET), timeBuckets,
                currentBucket);
        redisTemplate.opsForSet().getOperations().expire(currentBucket, UNION_FOR_MONGO_PERSISTENCE_EXPIRE_TIME,
                TimeUnit.SECONDS);

        Set<TypedTuple<String>> sampleUnion =
                redisTemplate.opsForZSet().rangeWithScores(currentBucket, bizProps.getPresistToMongoTopN() * -1, -1);
        if (sampleUnion != null && sampleUnion.size() > 0) {

            if (!mongoTemplate.collectionExists("sample")) {
                mongoTemplate.createCollection("sample", Constants.COLLECTION_OPTIONS);
                mongoTemplate.indexOps("sample").ensureIndex(new Index("time", Sort.Direction.ASC));
                mongoTemplate.indexOps("sample").ensureIndex(new Index("ruleName", Sort.Direction.ASC));
            }
            List<MongoSamplePo> mongoSamples = new ArrayList<>();
            for (TypedTuple<String> sample : sampleUnion) {
                MongoSamplePo msp = new MongoSamplePo();
                msp.setRuleName(rule.getName());
                msp.setSample(Sample.fromJsonStr(sample.getValue()));
                msp.setCount(sample.getScore());
                msp.setTime(now.format(FORMATTER_YMDHMS));
                mongoSamples.add(msp);
            }
            PerformanceStatisticContainer.add(() -> mongoTemplate.insert(mongoSamples,MongoSamplePo.class),MONGO_SAVE_1000SAMPLE_RULE.name());
            logger.info("persist union sample size: {}, rule: {}", mongoSamples.size(), rule.getName());
        }
    }

}
