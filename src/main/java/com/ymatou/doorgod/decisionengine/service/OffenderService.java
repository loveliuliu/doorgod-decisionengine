/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.service;

import com.ymatou.doorgod.decisionengine.model.LimitTimesRule;

/**
 * @author luoshiqian 2016/9/22 15:37
 */
public interface OffenderService {

    Boolean saveOffender(LimitTimesRule rule, String sample, String releaseDate, String addTime);

}
