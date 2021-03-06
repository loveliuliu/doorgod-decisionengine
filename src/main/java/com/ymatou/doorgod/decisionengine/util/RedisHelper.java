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
    private static final String SET_NAME_TEMPLATE = "doorgod:%s:%s:%s";

    private static final String UNION_SET_NAME_TEMPLATE = "doorgod:%s:%s:%s:%s";

    private static final String BLACK_LIST_MAP_NAME_TEMPLATE = "doorgod:%s:%s";

    private static final String EMPTY_SET_NAME_TEMPLATE = "doorgod:%s";

    public static String getNormalSetName(String ruleName, String time) {
        return String.format(SET_NAME_TEMPLATE, ruleName, "set", time);
    }

    public static String getUnionSetName(String ruleName, String time, String flag) {
        return String.format(UNION_SET_NAME_TEMPLATE, ruleName, "set", time, flag);
    }

    public static String getOffendersMapName(String ruleName) {
        return String.format(BLACK_LIST_MAP_NAME_TEMPLATE, ruleName, "offenders");
    }

    public static String getEmptySetName(String name) {
        return String.format(EMPTY_SET_NAME_TEMPLATE, name);
    }
}
