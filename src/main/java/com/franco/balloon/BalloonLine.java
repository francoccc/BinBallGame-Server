package com.franco.balloon;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import com.alibaba.fastjson.JSONObject;
import com.franco.activity.bean.PlayerActivity;
import com.franco.balloon.message.BalloonMessage;
import com.franco.common.Tuple;

/**
 * BalloonLine
 *
 * @author franco
 */
public class BalloonLine extends AbstractQueue<BalloonLine.Balloon> {

    private Balloon[] elem;
    private int front;
    private int rear;
    private int size;
    private static final AtomicInteger gen = new AtomicInteger();
    private long startTime;
    // 修正推送的时间
    private long offset;
    public MoveData data;
    public ReentrantLock lock = new ReentrantLock();
    public int playerId;

    public BalloonLine(int capacity, long offset, MoveData data, int playerId) {
        this.elem = new Balloon[capacity];
        this.offset = offset;      // 修正偏移时间
        this.data = data;
        this.playerId = playerId;
    }

    public void make() {
        size = 0;
        fillUpToCapacity();
    }

    public Balloon tail() {
        return elem[rear];
    }

    private void fillUpToCapacity() {
        filterOldBalloon();
        generateNewBalloon();
    }

    private void filterOldBalloon() {
        long now = System.currentTimeMillis();
        if (startTime == 0) {
            startTime = now;
        }
        float l = data.l - (data.v * (now - startTime)) / 1000;
        while(peek() != null
                && peek().measurePos(l, null) < 0) {
            assert peek() != null;
            this.startTime += peek().interval;
            remove(peek());
        }
    }

    private void generateNewBalloon() {
        if (size == 0) {
            this.startTime = System.currentTimeMillis();
        }
        float t = 0;
        Balloon prev = elem[rear];
        while(size < elem.length) {
            Balloon b = new Balloon(this, prev);
            prev = b;
            offer(b);
        }
    }

    @Override
    public Iterator<Balloon> iterator() {
        return new Iterator<>() {

            private int i = dec(front);

            @Override
            public boolean hasNext() {
                return inc(i) != rear;
            }

            @Override
            public Balloon next() {
                i = inc(i);
                return elem[i];
            }

            @Override
            public void remove() {
                while(peek() != null
                        && peek().id != elem[i].id) {
                    BalloonLine.this.remove();
                }
                BalloonLine.this.remove();
                fillUpToCapacity();
            }
        };
    }

    private int inc(int i) {
        return ++i % elem.length;
    }

    private int dec(int i) {
        return --i % elem.length;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean offer(Balloon balloon) {
        if (size < elem.length) {
            elem[rear] = balloon;
            rear = inc(rear);
            size++;
            return true;
        }
        return false;    }

    @Override
    public Balloon poll() {
        if (size > 0) {
            Balloon balloon = elem[front];
            front = inc(front);
            size--;
            return balloon;
        }
        return null;
    }

    @Override
    public Balloon peek() {
        if(size > 0) {
            return elem[front];
        }
        return null;
    }

    private static int generateId() {
        return gen.incrementAndGet() % 1024;
    }

    public void listAll(JSONObject jsonObject) {
        float l = data.l - (data.v * (System.currentTimeMillis() - startTime)) / 1000;
        List<BalloonMessage> msg = new ArrayList<>();
        for(Balloon balloon : elem) {
           balloon.measurePos(l, msg);
        }
    }

    public Tuple<Long, Balloon> pushTime(int bid) {
        float l = data.l - (data.v * (System.currentTimeMillis() - startTime)) / 1000;
        boolean find = false;
        for(Balloon balloon : elem) {
            long time = balloon.measureHitTime(l);
            if (time > 0 && find) {
                return new Tuple<>(time, balloon);
            }
            if (bid == balloon.id) {
                find = true;
            }
        }
        return new Tuple<>(elem[rear].measureHitTime(l), tail());
    }

    public boolean hitBy(PlayerActivity pa, int bid) {
        for(Balloon balloon : elem) {
            if (balloon.id == bid) {
                return balloon.hitBy(pa);
            }
        }
        return false;
    }

    public static class Balloon {

        enum Type {
            SMALL,
            BIG
        }

        private Type type;
        private boolean hit;
        public float interval;
        public int id;
        private BalloonLine line;

        public Balloon(BalloonLine line, Balloon prev) {
            this.type = randomType();
            float time = randomInterval();
            float intervalDis = time * line.data.v + getSize() / 2;
            if(prev != null) {
                intervalDis += prev.getSize() / 2;
            }
            this.interval = intervalDis / line.data.v + (prev != null ? prev.interval : 0);
            this.id = BalloonLine.generateId();
            this.line = line;
        }

        public boolean hitBy(PlayerActivity pa) {
            // if 条件不通过 return
            if (hit) {
                return false;
            }
            onHit();
            return true;
        }

        private float measurePos(float l, List<BalloonMessage> msg) {
            float dis = l + interval * line.data.v;
            System.out.println(String.format("%s pos: %f", this, dis / line.data.l));
            if (msg != null) {
                BalloonMessage m = new BalloonMessage();
                m.setPos(dis / line.data.l);
                m.setId(id);
            }
            return dis / line.data.l;
        }

        private long measureHitTime(float l) {
            float dis = l + interval * line.data.v;
            if (type.equals(Type.SMALL) && dis > line.data.lr) {
                System.out.println((dis - line.data.lr) / line.data.v);
                return  (long) ((dis - line.data.lr) / line.data.v * 1000f);
            }
            return 0;
        }

        private void onHit() {
            System.out.println(String.format("%s has been hit", this));
            hit = true;
            line.remove(this);
        }

        private static Type randomType() {
            Random random = new Random();
            if (random.nextDouble() < 0.5d) {
                return Type.BIG;
            }
            return Type.SMALL;
        }

        private static float randomInterval() {
            Random random = new Random();
            List<Float> list = Arrays.asList(0f, 0.5f, 0.7f, 0.8f);
            return list.get(random.nextInt(list.size()));
        }

        private float getSize() {
            switch (type) {
                case SMALL:
                    return line.data.s1;
                case BIG:
                    return line.data.s2;
            }
            return 0f;
        }

        @Override
        public String toString() {
            return String.format("balloon:[%d, %d, %.2f, %d]", id, hit ? 1 : 0, interval, type.ordinal());
        }
    }

    public static class MoveData {

        public float v;
        public float s1;
        public float s2;
        public float l;
        public float bt;

        public float lr;

        public void calcPushPos() {
            lr = l / 2 + bt * v - s1 / 2;
        }
    }
}
