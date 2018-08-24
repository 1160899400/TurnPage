package com.royole.demo.utils;

import com.royole.demo.bean.MyPoint;

/**
 * @author Hongzhi.Liu 2014302580200@whu.edu.cn
 * @date 2018/8/22
 */
public class MyPointUtils {
    /**
     * 获取p1,p2的连线与p3,p4的连线的相交点
     */
    public static MyPoint getIntersectionPoint(MyPoint p1, MyPoint p2, MyPoint p3, MyPoint p4) {
        float x1, y1, x2, y2, x3, y3, x4, y4;
        x1 = p1.x;
        y1 = p1.y;
        x2 = p2.x;
        y2 = p2.y;
        x3 = p3.x;
        y3 = p3.y;
        x4 = p4.x;
        y4 = p4.y;
        float pointX = ((x1 - x2) * (x3 * y4 - x4 * y3) - (x3 - x4) * (x1 * y2 - x2 * y1))
                / ((x3 - x4) * (y1 - y2) - (x1 - x2) * (y3 - y4));
        float pointY = ((y1 - y2) * (x3 * y4 - x4 * y3) - (x1 * y2 - x2 * y1) * (y3 - y4))
                / ((y1 - y2) * (x3 - x4) - (x1 - x2) * (y3 - y4));

        return new MyPoint(pointX, pointY);
    }
}
