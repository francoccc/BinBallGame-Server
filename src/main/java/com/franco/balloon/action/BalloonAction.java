package com.franco.balloon.action;

import com.franco.ByteResult;
import com.franco.Constants;
import com.franco.action.BaseAction;
import com.franco.balloon.service.IBalloonService;
import com.franco.spring.annotation.Command;
import com.franco.spring.annotation.RequestParam;
import com.franco.spring.core.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 气球动作
 *
 * @author franco
 */
@Component("balloonAction")
public class BalloonAction extends BaseAction {

    @Autowired
    private IBalloonService balloonService;


    @Command("ball@getInfo")
    public ByteResult getBallInfo(@RequestParam("playerId") int playerId, Request request) {
        Constants.sessions.put(playerId, request.getSession());
        return getResult(balloonService.getBalloonInfo(playerId), request);
    }

    @Command("ball@hitBall")
    public ByteResult hitBall(@RequestParam("playerId") int playerId, @RequestParam("bid") int bid, Request request) {
        Constants.sessions.put(playerId, request.getSession());
        return getResult(balloonService.hitBalloon(playerId, bid), request);
    }

    @Command("ball@setAuto")
    public ByteResult setAuto(@RequestParam("playerId") int playerId, @RequestParam("bid") int bid, Request request) {
        Constants.sessions.put(playerId, request.getSession());
        return getResult(balloonService.hitBalloon(playerId, bid), request);
    }
}
