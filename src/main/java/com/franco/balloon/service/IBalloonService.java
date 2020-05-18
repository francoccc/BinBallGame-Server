package com.franco.balloon.service;

public interface IBalloonService {

    /**
     * 获取所有气球信息
     *
     * @param playerId
     * @return
     */
    byte[] getBalloonInfo(int playerId);

    /**
     * 加次数
     */
    byte[] addNum(int playerId, int num);

    /**
     * 击中气球
     *
     * @param playerId
     * @param bid
     * @return
     */
    byte[] hitBalloon(int playerId, int bid);

    /**
     * 设置是否是自动
     * @param playerId
     * @param bid
     * @return
     */
    byte[] setAuto(int playerId, int bid);
}
