/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */
package com.ymatou.doorgod.decisionengine.repository;

import com.ymatou.doorgod.decisionengine.model.mongo.MongoSamplePo;
import com.ymatou.doorgod.decisionengine.model.mongo.RejectReqPo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * 
 * @author qianmin 2016年9月13日 下午2:48:03
 * 
 */
@Repository
public interface RejectReqRepository extends MongoRepository<RejectReqPo, String> {

}
