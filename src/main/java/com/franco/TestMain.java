package com.franco;

import com.alibaba.fastjson.JSONObject;
import com.franco.balloon.message.BalloonMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Test
 *
 * @author franco
 */
public class TestMain {


    public static void main(String[] args) {
        List<BalloonMessage> msg = new ArrayList<>();
        BalloonMessage m1 = new BalloonMessage();
        BalloonMessage m2 = new BalloonMessage();
        m1.setPos(1.0f);
        m2.setPos(1.2f);
        msg.add(m1);
        msg.add(m2);
        JSONObject json = new JSONObject();
        json.put("balloons", msg);
        System.out.println(json.toJSONString());
    }
}
