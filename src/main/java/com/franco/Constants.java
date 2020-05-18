package com.franco;

import com.alibaba.fastjson.JSONObject;
import com.franco.spring.core.Session;

import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 应用常量
 *
 * @author franco
 */
public class Constants {

    /**
     * Timer
     */
    public final static Timer timer = new Timer();

    public final static ConcurrentHashMap<Integer, Session> sessions = new ConcurrentHashMap<>();

    public static void push(int playerId, JSONObject json) {
        if (sessions.get(playerId) != null) {
            sessions.get(playerId).push("push@update", json.toJSONString().getBytes());
        }
    }
}
