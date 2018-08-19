package com.royole.demo.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Scroller;

import com.royole.demo.R;
import com.royole.demo.bean.MyPoint;
import com.royole.demo.constants.TurnPageMode;

/**
 * @author HZLI02
 * @date 2018/8/15
 */

public class LeftPageView extends View {
    private MyPoint a, c, e, f, g, m, n, p;
    private Path pathLast;
    private Path pathCurrent;
    private Context context;
    private Bitmap bitmapLast;
    private Paint paintLast;
    private Bitmap bitmapCurrent;
    private Paint paintCurrent;
    private Paint textPaint;
    private Scroller mScroller;
    private float viewWidth;
    private float viewHeight;
    private float startY;
    private boolean isTurningPage = false;
    private int turnPageMode = TurnPageMode.MODE_NO_ACTION;
    private Matrix mMatrix = new Matrix();
    private float[] mMatrixArray = {0, 0, 0, 0, 0, 0, 0, 0, 1.0f};


    public LeftPageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        this.context = context;
        a = new MyPoint();
        a.setXY(100, 1500);
        c = new MyPoint();
        e = new MyPoint();
        f = new MyPoint();
        g = new MyPoint();
        m = new MyPoint();
        n = new MyPoint();
        p = new MyPoint();
        initPointXY();
        postInvalidate();
        pathLast = new Path();
        pathCurrent = new Path();

        paintLast = new Paint();
        paintLast.setColor(ContextCompat.getColor(context, R.color.blue_light));
        paintLast.setAntiAlias(true);

        paintCurrent = new Paint();
        paintCurrent.setAntiAlias(true);
        paintCurrent.setColor(ContextCompat.getColor(context, R.color.green_light));

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(ContextCompat.getColor(context, R.color.red_light));
        textPaint.setSubpixelText(true);
        textPaint.setTextSize(30);
        mScroller = new Scroller(context);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = measureSize(getMeasuredHeight(), heightMeasureSpec);
        int width = measureSize(getMeasuredWidth(), widthMeasureSpec);
        setMeasuredDimension(width, height);
        viewWidth = width;
        viewHeight = height;
        //在这里初始化当前页和即将翻过来的页面
        bitmapLast = Bitmap.createBitmap((int) viewWidth, (int) viewHeight, Bitmap.Config.RGB_565);
        drawLastPageBitmap(bitmapLast, paintLast);
        bitmapCurrent = Bitmap.createBitmap((int) viewWidth, (int) viewHeight, Bitmap.Config.RGB_565);
        drawCurrentPageBitmap(bitmapCurrent, paintCurrent);

    }

    private int measureSize(int defaultSize, int measureSpec) {
        int result = defaultSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            result = Math.min(result, specSize);
        }
        return result;
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            float x = mScroller.getCurrX();
            switch (turnPageMode) {
                case TurnPageMode.MODE_RIGHT_TOP:
                    a.y = x * x * startY * (-1) / viewWidth  + 2 * startY * x;
                    break;
                case TurnPageMode.MODE_RIGHT_MIDDLE:
                case TurnPageMode.MODE_RIGHT_BOTTOM:
                    a.y = x * x * startY / viewWidth - 2 * startY * x + viewHeight;
                    break;
                default:
                    break;
            }
            a.x = x;
            Log.i("###turn left: ", "a.x:   " + a.x + "     a.y:  " + a.y);
            initPointXY();
            postInvalidate();
            //翻页完成时触发，恢复初始状态
            if (mScroller.getFinalX() == x) {
                setDefaultPath();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isTurningPage = true;
                a.setXY(200, 1300);
                f.setXY(2 * viewWidth, viewHeight);
                break;
            case MotionEvent.ACTION_MOVE:
                touchPoint(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_UP:
                //判断翻页是否成功
                setDefaultPath();
                break;
            default:
                break;
        }
        return true;
    }

    private void touchPoint(float x, float y) {
        a.setXY(x, y);
        initPointXY();
        postInvalidate();
    }

    private void drawLastPageBitmap(Bitmap bitmap, Paint paint) {
        Canvas mCanvas = new Canvas(bitmap);
        //下面开始绘制区域内的内容...
        mCanvas.drawPath(getPathDefault(), paint);
        //结束绘制区域内的内容...
        mCanvas.drawText("这是在A区域的内容...AAAA", viewWidth - 260, viewHeight - 100, textPaint);

    }

    private void drawCurrentPageBitmap(Bitmap bitmap, Paint paint) {
        Canvas mCanvas = new Canvas(bitmap);
        //下面开始绘制区域内的内容...
        mCanvas.drawPath(getPathDefault(), paint);
        //结束绘制区域内的内容...
        mCanvas.drawText("这是在b区域的内容...bbbb", viewWidth - 260, viewHeight - 100, textPaint);
    }


    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawText("a", a.x, a.y, textPaint);
        canvas.drawText("f", f.x, f.y, textPaint);
        canvas.drawText("g", g.x, g.y, textPaint);

        canvas.drawText("e", e.x, e.y, textPaint);
        canvas.drawText("m", m.x, m.y, textPaint);

        canvas.drawText("c", c.x, c.y, textPaint);
        canvas.drawText("n", n.x, n.y, textPaint);

        canvas.drawText("p", p.x, p.y, textPaint);


        canvas.drawColor(ContextCompat.getColor(context, R.color.red_light));
        if (!isTurningPage) {
            drawLastPage(canvas, getPathDefault());
        } else {
            drawLastPage(canvas, getPathLast());
            drawCurrentPage(canvas, getPathCurrent());
        }
    }


    private void drawLastPage(Canvas canvas, Path path) {
        canvas.save();
        canvas.clipPath(getPathDefault());
//        canvas.clipPath(path, Region.Op.INTERSECT);
        canvas.drawPath(path, paintLast);
//        canvas.drawBitmap(bitmapLast, 0, 0, null);
        canvas.restore();
    }

    private void drawCurrentPage(Canvas canvas, Path path) {
        canvas.save();
        canvas.clipPath(path, Region.Op.INTERSECT);
        canvas.drawPath(path, paintCurrent);

        float eh = (float) Math.hypot(e.x - a.x, e.y - a.y);

        float sin0 = (e.y - a.y) / eh;
        float cos0 = (e.x - a.x) / eh;
        //设置翻转和旋转矩阵
        mMatrixArray[0] = -(cos0);
        mMatrixArray[1] = sin0;
        mMatrixArray[3] = sin0;
        mMatrixArray[4] = cos0;
        mMatrix.reset();
        //翻转和旋转
        mMatrix.setValues(mMatrixArray);
        //平移
        switch (turnPageMode) {
            case TurnPageMode.MODE_RIGHT_TOP:
                mMatrix.postTranslate(n.x, n.y);
                break;
            case TurnPageMode.MODE_RIGHT_MIDDLE:
            case TurnPageMode.MODE_LEFT_BOTTOM:
                mMatrix.postTranslate(e.x, e.y);
                break;
            default:
                break;
        }
        canvas.drawBitmap(bitmapCurrent, mMatrix, null);
        canvas.restore();
    }

    private void setDefaultPath() {
        a.setXY(-1, -1);
        isTurningPage = false;
        turnPageMode = TurnPageMode.MODE_NO_ACTION;
        postInvalidate();
    }


    /**
     * 当右页向左翻动时
     */
    private void initPointXY() {
        //g为a,f中点
        g.setXY((a.x + f.x) / 2, (a.y + f.y) / 2);
        //eh与af垂直，e为与f水平的点，h为与f垂直的点,am垂直ae
        e.setXY(g.x - (f.y - g.y) * (f.y - g.y) / (f.x - g.x), f.y);
        c.setXY(e.x - a.x / 10, f.y);
        p.setXY(2 * e.x / 3 + 1 * a.x / 3, 2 * e.y / 3 + 1 * a.y / 3);
        m.setXY(a.x + (a.y - e.y) * (viewHeight - f.y - a.y) / (e.x - a.x), viewHeight - f.y);
        n.setXY(m.x - a.x + e.x, m.y - a.y + e.y);
    }

    private Path getPathDefault() {
        Path path = new Path();
        path.lineTo(0, viewHeight);
        path.lineTo(viewWidth, viewHeight);
        path.lineTo(viewWidth, 0);
        path.close();
        return path;
    }


    private Path getPathLast() {
        pathLast.reset();
        pathLast.lineTo(m.x, m.y);
        pathLast.lineTo(a.x, a.y);
        pathLast.lineTo(p.x, p.y);
        pathLast.quadTo(e.x, e.y, c.x, c.y);
        pathLast.lineTo(0, viewHeight);
        pathLast.close();
        return pathLast;
    }

    private Path getPathCurrent() {
        pathCurrent.reset();
        pathCurrent.moveTo(m.x, m.y);
        pathCurrent.lineTo(a.x, a.y);
        pathCurrent.lineTo(p.x, p.y);
        pathCurrent.quadTo(e.x, e.y, c.x, c.y);
        pathCurrent.lineTo(e.x, e.y);
        pathCurrent.lineTo(n.x, n.y);
        pathCurrent.lineTo(viewWidth,viewHeight-f.y);
        pathCurrent.close();
        return pathCurrent;
    }

    /**
     * 向左翻页触发
     */
    public void turnLeft(float height, int MODE, Bitmap bitmap) {
        a.setXY(viewWidth, height);
        startY = height;
//        bitmapLast = bitmapCurrent;
//        drawCurrentPageBitmap(bitmapCurrent, paintCurrent);
        turnPageMode = MODE;
        startTurnPageAnim();
    }

    private void startTurnPageAnim() {
        isTurningPage = true;
        int dx = 0, dy = 0;
        switch (turnPageMode) {
            case TurnPageMode.MODE_RIGHT_TOP:
                dx = 0 - (int) a.x;
                f.setXY(2 * viewWidth, 0f);
                break;
            case TurnPageMode.MODE_RIGHT_MIDDLE:
            case TurnPageMode.MODE_RIGHT_BOTTOM:
                dx = 0 - (int) a.x;
                f.setXY(2 * viewWidth, viewHeight);
                break;
            default:
                break;
        }
        initPointXY();
        postInvalidate();
        mScroller = new Scroller(context, new AccelerateDecelerateInterpolator());
        mScroller.startScroll((int) a.x, 0, dx, dy, 1200);
    }

    /**
     * 获取p1,p2的连线与p3,p4的连线的相交点
     */
    public MyPoint getIntersectionPoint(MyPoint p1, MyPoint p2, MyPoint p3, MyPoint p4) {
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
