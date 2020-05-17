package com.franco.balloon.service;

import com.alibaba.fastjson.JSONObject;
import com.franco.Constants;
import com.franco.activity.bean.PlayerActivity;
import com.franco.balloon.BalloonLine;
import com.franco.common.Tuple;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TimerTask;

@Component("balloonService")
public class BalloonService implements IBalloonService {

    private HashMap<Integer, BalloonLine> lines = new HashMap<>();
    private HashMap<Integer, PlayerActivity> pas = new HashMap<>();
    private HashSet<Integer> isAutos = new HashSet<>();
    private static final int SIZE = 10;
    private static final BalloonLine.MoveData moveData = new BalloonLine.MoveData();

    static {
        moveData.v = 3;
        moveData.l = 13;
        moveData.bt = 0.2f;
        moveData.s1 = 1;
        moveData.s2 = 2;
        moveData.calcPushPos();
    }

    /**
     * 获取所有气球信息
     *
     * @param playerId
     * @return
     */
    @Override
    public byte[] getBalloonInfo(int playerId) {
        PlayerActivity pa = createIfNotExistPa(playerId);
        BalloonLine line = lines.get(playerId);
        if (line == null) {
            // 创建一条线
            line = new BalloonLine(SIZE, 0, moveData, playerId);
            lines.put(playerId, line);
        }
        line.make();

        JSONObject json = new JSONObject();
        json.put("count", pa.getCount());
        line.listAll(json);
        return json.toJSONString().getBytes();
    }

    /**
     * 击中气球
     *
     * @param playerId
     * @return
     */
    @Override
    public byte[] hitBalloon(int playerId, int bid) {
        PlayerActivity pa = createIfNotExistPa(playerId);
        BalloonLine line = lines.get(playerId);
        if (line == null || pa.getCount() <= 0) {
            return "{msg:\"参数错误\"}".getBytes();
        }
        JSONObject json = new JSONObject();
        if (line.hitBy(pa, bid)) {
            pa.setCount(pa.getCount() - 1);
            json.put("hit", 1);
        }
        return json.toJSONString().getBytes();
    }

    /**
     * 设置是否是自动
     *
     * @param playerId
     * @param bid
     * @return
     */
    @Override
    public byte[] setAuto(int playerId, int bid) {
        if (isAutos.contains(playerId) || !lines.containsKey(playerId)) {
            return "{msg:\"参数错误\"}".getBytes();
        }
        PlayerActivity pa = createIfNotExistPa(playerId);
        if (pa.getCount() <= 0) {
            return "{msg:\"参数错误\"}".getBytes();
        }
        pa.setAuto(1);
        isAutos.add(playerId);
        JSONObject json = new JSONObject();
        json.put("isAuto", 1);
        addAutoPushTask(lines.get(playerId), bid);
        return json.toJSONString().getBytes();
    }

    private void addAutoPushTask(BalloonLine line, int bid) {
        PlayerActivity pa = createIfNotExistPa(line.playerId);
        if (pa.getCount() <= 0) {
            isAutos.remove(line.playerId);
            pa.setAuto(0);
            return;
        }
        // 得到下一次的时间
        Tuple<Long, BalloonLine.Balloon> tuple = line.pushTime(bid);
        JSONObject json = new JSONObject();
        if (tuple.right.equals(line.tail())) {
            line.make();
            line.pushTime(bid);
            line.listAll(json);
            Constants.push(line.playerId, json);
        }
        Constants.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                doPush(line, tuple.right);
            }
        }, new Date(tuple.left));
    }

    private void doPush(BalloonLine line, BalloonLine.Balloon balloon) {
        if (balloon.hitBy(createIfNotExistPa(line.playerId))) {
            JSONObject json = new JSONObject();
            json.put("hit", 1);
            json.put("id", balloon.id);
            Constants.push(line.playerId, json);
        }
        addAutoPushTask(line, balloon.id);
    }


    private PlayerActivity createIfNotExistPa(int playerId) {
        PlayerActivity pa = pas.get(playerId);
        if (pa == null) {
            pa = new PlayerActivity();
            pa.setUid(playerId);
            pa.setCount(100);
        }
        return pa;
    }
}
