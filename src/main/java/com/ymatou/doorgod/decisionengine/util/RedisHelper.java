/*
 *
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.util;

/**
 * @author luoshiqian 2016/9/12 14:20
 */
public class RedisHelper {

    // redis set名称 rulename:typename:time
    private static final String SET_NAME_TEMPLATE = "%s:%s:%s";

    public static String getNormalSetName(String ruleName, String time) {
        return SET_NAME_TEMPLATE.format(ruleName, "set", time);
    }


}