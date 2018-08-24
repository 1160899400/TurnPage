package com.royole.demo.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

import com.royole.demo.R;
import com.royole.demo.bean.MyPoint;
import com.royole.demo.constants.TurnPageMode;
import com.royole.demo.utils.MyPointUtils;


/**
 * @author HZLI02
 * @date 2018/8/10
 */

public class RightPageView extends View {
    private Context context;
    private float viewWidth;
    private float viewHeight;

    /**
     * 翻页模式：不翻页，从右上右下右中，左上左下左中翻页
     */
    private int turnPageMode = TurnPageMode.MODE_NO_ACTION;

    public int sendMode = TurnPageMode.MODE_NO_ACTION;

    public float postAWidth;
    public float postAHeight;

    /**
     * 翻页成功结果
     */
    private boolean turnPage = false;

    /**
     * 从左往右翻页的高度
     */
    private float startY;


    //-1为左上，1为左下
    private int calPointFactor;

    /**
     * 翻页状态(正在翻页)
     */
    private boolean isTurningPage = false;
    private Scroller mScroller1;
    private Scroller mScroller2;
    /**
     * pathA为当前页路径，pathB为下一页，pathC为背页，pathD为上一页
     */
    private Paint pathAPaint;
    private Paint pathBPaint;
    private Paint pathCPaint;
    private Paint pathDPaint;
    //绘制文字画笔
    private Paint textPaint;
    private Bitmap bmpCurrentPage;
    private Bitmap bmpNextPage;
    private Bitmap bmpBackPage;
    private Bitmap bmpLastPage;
    private float sin0;
    private float cos0;
    private Matrix mMatrix;


    private MyPoint a, f, g, e, h, c, j, b, k, d, i, m, n, o, p, q, r;


    public RightPageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    /**
     * 对View初始化
     */
    private void init(Context context, AttributeSet attrs) {
        a = new MyPoint();
        a.setXY(-1, -1);
        b = new MyPoint();
        c = new MyPoint();
        d = new MyPoint();
        e = new MyPoint();
        f = new MyPoint();
        g = new MyPoint();
        h = new MyPoint();
        i = new MyPoint();
        j = new MyPoint();
        k = new MyPoint();
        m = new MyPoint();
        n = new MyPoint();
        o = new MyPoint();
        p = new MyPoint();
        q = new MyPoint();
        r = new MyPoint();
        this.context = context;
        pathAPaint = new Paint();
        pathAPaint.setColor(ContextCompat.getColor(context, R.color.blue_light));
        pathAPaint.setAntiAlias(true);

        pathBPaint = new Paint();
        pathBPaint.setAntiAlias(true);
        pathBPaint.setColor(ContextCompat.getColor(context, R.color.green_light));

        pathCPaint = new Paint();
        pathCPaint.setAntiAlias(true);
        pathCPaint.setColor(ContextCompat.getColor(context, R.color.red_light));

        pathDPaint = new Paint();
        pathDPaint.setAntiAlias(true);
        pathDPaint.setColor(ContextCompat.getColor(context, R.color.yellow_light));

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextAlign(Paint.Align.CENTER);
        //设置自像素。如果该项为true，将有助于文本在LCD屏幕上的显示效果。
        textPaint.setSubpixelText(true);
        textPaint.setTextSize(30);
        mScroller1 = new Scroller(context, new LinearInterpolator());
        mScroller2 = new Scroller(context, new AccelerateDecelerateInterpolator());
        mMatrix = new Matrix();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = measureSize(getMeasuredHeight(), heightMeasureSpec);
        int width = measureSize(getMeasuredWidth(), widthMeasureSpec);
        setMeasuredDimension(width, height);
        viewWidth = width;
        viewHeight = height;
        //在这里初始化A B C页面要展示的内容,A为当前页，B为下一页，C为当前页的背页
        setBmpCurrentPage(Bitmap.createBitmap((int) viewWidth, (int) viewHeight, Bitmap.Config.RGB_565));
        setBmpNextPage(Bitmap.createBitmap((int) viewWidth, (int) viewHeight, Bitmap.Config.RGB_565));
        setBmpBackPage(Bitmap.createBitmap((int) viewWidth, (int) viewHeight, Bitmap.Config.RGB_565));
        setBmpLastPage(Bitmap.createBitmap((int) viewWidth, (int) viewHeight, Bitmap.Config.RGB_565));
        a.setXY(-1, -1);
    }

    /**
     * 设置当前页面的bitmap内容
     *
     * @param bitmap
     */
    public void setBmpCurrentPage(Bitmap bitmap) {
        bmpCurrentPage = bitmap;
        drawPathAContentBitmap(bitmap, pathAPaint);
    }

    /**
     * 设置下一页面的bitmap内容
     *
     * @param bitmap
     */
    public void setBmpNextPage(Bitmap bitmap) {
        bmpNextPage = bitmap;
        drawPathBContentBitmap(bmpNextPage, pathBPaint);
    }

    /**
     * 设置当前页面的背页的bitmap内容
     *
     * @param bitmap
     */
    public void setBmpBackPage(Bitmap bitmap) {
        bmpBackPage = bitmap;
        drawPathAContentBitmap(bitmap, pathCPaint);
    }

    /**
     * 设置上一页面的bitmap内容
     *
     * @param bitmap
     */
    public void setBmpLastPage(Bitmap bitmap) {
        bmpLastPage = bitmap;
        drawPathDContentBitmap(bmpLastPage, pathDPaint);
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
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (TurnPageMode.MODE_NO_ACTION == turnPageMode) {
            drawCurPage(canvas, getPathDefault());
        } else if (TurnPageMode.MODE_RIGHT_TOP == turnPageMode) {
            drawCurPage(canvas, getPathAFromTopRight());
            drawBackPage(canvas, getPathC(getPathAFromTopRight()));
            drawNextPage(canvas, getPathB(getPathAFromTopRight()));
        } else if (TurnPageMode.MODE_RIGHT_MIDDLE == turnPageMode || TurnPageMode.MODE_RIGHT_BOTTOM == turnPageMode) {
            drawCurPage(canvas, getPathAFromBottomRight());
            drawBackPage(canvas, getPathC(getPathAFromBottomRight()));
            drawNextPage(canvas, getPathB(getPathAFromBottomRight()));
        } else if (TurnPageMode.MODE_LEFT_TOP == turnPageMode || TurnPageMode.MODE_LEFT_MIDDLE == turnPageMode || TurnPageMode.MODE_LEFT_BOTTOM == turnPageMode) {
            drawCurPage(canvas, getPathAFromLeft());
            drawLastPage(canvas, getPathD());
            canvas.drawText("a", a.x, a.y, textPaint);
            canvas.drawText("m", m.x, m.y, textPaint);
            canvas.drawText("n", n.x, n.y, textPaint);
        }
    }

    @Override
    public void computeScroll() {
        if (mScroller1.computeScrollOffset()) {
            float x = mScroller1.getCurrX();
            float y = mScroller1.getCurrY();
            a.setXY(x, y);
            initPointTurnLeft();
            postInvalidate();
            //翻页完成时触发，恢复初始状态
            if (mScroller1.getFinalX() == x && mScroller1.getFinalY() == y) {
                setDefaultPath();
            }
            if (turnPage && x < 50 && x >= 0) {
                postAHeight = y;
                postAWidth = x;
            }
        }
        if (mScroller2.computeScrollOffset()) {
            float x = mScroller2.getCurrX();
            float y = mScroller2.getCurrY();
            a.setXY(x, y);
            initPointTurnRight();
            postInvalidate();
            //翻页完成时触发，恢复初始状态
            if (mScroller2.getFinalX() == x && mScroller2.getFinalY() == y) {
                setDefaultPath();
            }
            if (turnPage && x < 50 && x >= 0) {
                postAHeight = y;
            }
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        if (isTurningPage) {
            return true;
        } else {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    initStartArea(event.getX(), event.getY());
                    break;
                case MotionEvent.ACTION_MOVE:
                    touchPoint(event.getX(), event.getY());
                    break;
                case MotionEvent.ACTION_UP:
                    //判断翻页是否成功
                    if (turnPageMode != TurnPageMode.MODE_NO_ACTION) {
                        if (turnPage) {
                            startTurnLeftAnim();
                        } else {
                            cancelTurnLeftAnim();
                        }
                    }
                    break;
                default:
                    break;
            }
            return true;
        }
    }


    /**
     * 判断起始翻页动作在哪一区域
     *
     * @param x
     * @param y
     * @return
     */
    private void initStartArea(float x, float y) {
        //点在右侧
        if (0.85 * viewWidth < x) {
            if (0.2 * viewHeight > y) {
                turnPageMode = TurnPageMode.MODE_RIGHT_TOP;
            } else if (0.8 * viewHeight < y) {
                turnPageMode = TurnPageMode.MODE_RIGHT_BOTTOM;
            } else {
                turnPageMode = TurnPageMode.MODE_RIGHT_MIDDLE;
            }
        } else {
            turnPageMode = TurnPageMode.MODE_NO_ACTION;
        }
    }

    private void touchPoint(float pointX, float pointY) {
        switch (turnPageMode) {
            case TurnPageMode.MODE_RIGHT_TOP:
                a.setXY(pointX, pointY);
                f.setXY(viewWidth, 0);
                Log.i("###","a.x: " + a.x + "  a.y:  " +a.y);
                Log.i("###","c.x: " + c.x + "  c.y:  " +c.y);
                initPointTurnLeft();
                if (c.x < 0) {
                    calcPointAByTouchPoint();
                    initPointTurnLeft();
                }
                if (a.x < 0.15 * viewWidth) {
                    turnPage = true;
                } else {
                    turnPage = false;
                }
                postInvalidate();
                break;
            case TurnPageMode.MODE_RIGHT_MIDDLE:
                a.setXY(pointX, viewHeight - 1);
                f.setXY(viewWidth, viewHeight);
                initPointTurnLeft();
                if (a.x < viewWidth * 0.1) {
                    turnPage = true;
                } else {
                    turnPage = false;
                }
                postInvalidate();
                break;
            case TurnPageMode.MODE_RIGHT_BOTTOM:
                a.setXY(pointX, pointY);
                f.setXY(viewWidth, viewHeight);
                initPointTurnLeft();
                if (c.x < 0) {
                    calcPointAByTouchPoint();
                    initPointTurnLeft();
                }
                if (a.x < viewWidth * 0.15) {
                    turnPage = true;
                } else {
                    turnPage = false;
                }
                postInvalidate();
                break;
            case TurnPageMode.MODE_NO_ACTION:
                break;
            default:
                break;
        }
    }

    /**
     * 绘制当前页内容(将bitmap绘入A区域)
     */
    private void drawCurPage(Canvas canvas, Path path) {
        canvas.save();
        //对绘制内容进行裁剪，取和A区域的交集
        canvas.clipPath(path);
        canvas.drawBitmap(bmpCurrentPage, 0, 0, null);
        canvas.restore();
    }

    /**
     * 绘制A区域bitmap的内容
     *
     * @param bitmap
     * @param pathPaint
     */
    private void drawPathAContentBitmap(Bitmap bitmap, Paint pathPaint) {
        Canvas mCanvas = new Canvas(bitmap);
        //下面开始绘制区域内的内容...
        mCanvas.drawPath(getPathDefault(), pathPaint);
        //结束绘制区域内的内容...
        mCanvas.drawText("这是在A区域的内容...AAAA", 260, viewHeight - 100, textPaint);
    }

    /**
     * 绘制下一页内容
     */
    private void drawNextPage(Canvas canvas, Path path) {
        canvas.save();
        canvas.clipPath(path);
        canvas.drawBitmap(bmpNextPage, 0, 0, null);
        canvas.restore();
    }

    private void drawPathBContentBitmap(Bitmap bitmap, Paint pathPaint) {
        Canvas mCanvas = new Canvas(bitmap);
        mCanvas.drawPath(getPathDefault(), pathPaint);
    }

    /**
     * 绘制当前页的背面内容
     */
    private void drawBackPage(Canvas canvas, Path path) {
        canvas.save();
        canvas.clipPath(path);
        float eh = (float) Math.hypot(f.x - e.x, h.y - f.y);
        float cos0 = (h.y - f.y) / eh;
        float angel = new Double(Math.toDegrees(Math.acos(cos0))).floatValue();
        //设置翻转和旋转矩阵
        mMatrix.reset();
        mMatrix.setRotate(-2 * angel, 0, f.y);
        mMatrix.postTranslate(a.x - 0, a.y - f.y);

        canvas.drawBitmap(bmpBackPage, mMatrix, null);
        canvas.restore();
    }


    private void drawLastPage(Canvas canvas, Path path) {
        canvas.save();
        canvas.clipPath(path);
        mMatrix.reset();
        float angel = new Double(Math.toDegrees(Math.asin(sin0))).floatValue();
        mMatrix.setRotate(angel);
        if (calPointFactor == 1) {
            mMatrix.postTranslate(n.x, n.y);
        } else {
            mMatrix.postTranslate(o.x, o.y);
        }
        canvas.drawBitmap(bmpLastPage, mMatrix, null);
        canvas.restore();
    }

    private void drawPathDContentBitmap(Bitmap bitmap, Paint paint) {
        Canvas mCanvas = new Canvas(bitmap);
        mCanvas.drawColor(getResources().getColor(R.color.gray));
        mCanvas.drawText("这是在D区域的内容...DDDD", viewWidth - 260, viewHeight - 100, textPaint);
    }

    /**
     * 回到默认状态
     */
    private void setDefaultPath() {
        a.setXY(-1, -1);
        turnPageMode = TurnPageMode.MODE_NO_ACTION;
        isTurningPage = false;
        turnPage = false;
        postInvalidate();
    }

    /**
     * 默认的整页区域
     *
     * @return
     */
    private Path getPathDefault() {
        Path path = new Path();
        path.reset();
        path.lineTo(0, viewHeight);
        path.lineTo(viewWidth, viewHeight);
        path.lineTo(viewWidth, 0);
        path.close();
        return path;
    }

    /**
     * 当翻起区域在右上角时的path绘制
     *
     * @return
     */
    private Path getPathAFromTopRight() {
        Path path = new Path();
        path.lineTo(c.x, c.y);
        path.quadTo(e.x, e.y, b.x, b.y);
        path.lineTo(a.x, a.y);
        path.lineTo(k.x, k.y);
        path.quadTo(h.x, h.y, j.x, j.y);
        path.lineTo(viewWidth, viewHeight);
        path.lineTo(0, viewHeight);
        path.close();
        return path;
    }

    /**
     * 当翻起区域在右下角时的path绘制
     *
     * @return
     */
    private Path getPathAFromBottomRight() {
        Path path = new Path();
        path.lineTo(0, viewHeight);
        path.lineTo(c.x, c.y);
        path.quadTo(e.x, e.y, b.x, b.y);
        path.lineTo(a.x, a.y);
        path.lineTo(k.x, k.y);
        path.quadTo(h.x, h.y, j.x, j.y);
        path.lineTo(viewWidth, 0);
        path.close();//闭合区域
        return path;
    }

    private Path getPathAFromLeft() {
        Path path = getPathDefault();
        path.op(getPathD(), Path.Op.DIFFERENCE);
        return path;
    }

    /**
     * 下一页区域
     *
     * @return
     */
    private Path getPathB(Path pathA) {
        Path path = getPathDefault();
        path.op(pathA, Path.Op.DIFFERENCE);
        path.op(getPathC(pathA), Path.Op.DIFFERENCE);
        return path;
    }

    /**
     * 背面区域
     *
     * @return
     */
    private Path getPathC(Path pathA) {
        Path path = new Path();
        path.moveTo(i.x, i.y);
        path.lineTo(d.x, d.y);
        path.lineTo(b.x, b.y);
        path.lineTo(a.x, a.y);
        path.lineTo(k.x, k.y);
        path.close();
        path.op(pathA, Path.Op.DIFFERENCE);
        return path;
    }

    /**
     * 上一页区域
     */
    private Path getPathD() {
        Path path = new Path();
        path.moveTo(m.x, m.y);
        path.lineTo(a.x, a.y);
        path.lineTo(p.x, p.y);
        path.quadTo(r.x, r.y, 0, f.y);
        path.lineTo(r.x, r.y);
        path.lineTo(o.x, o.y);
        path.lineTo(n.x, n.y);
        path.close();
        return path;
    }


    /**
     * 初始化折起区域的关键点坐标
     */
    private void initPointTurnLeft() {
        //g为a,f中点
        g.setXY((a.x + f.x) / 2, (a.y + f.y) / 2);
        //eh与af垂直，e为与f水平的点，h为与f垂直的点
        e.setXY(g.x - (f.y - g.y) * (f.y - g.y) / (f.x - g.x), f.y);
        h.setXY(f.x, g.y - (f.x - g.x) * (f.x - g.x) / (f.y - g.y));
        c.setXY(e.x - (f.x - e.x) / 2, f.y);
        j.setXY(f.x, h.y - (f.y - h.y) / 2);
        b = MyPointUtils.getIntersectionPoint(a, e, c, j);
        k = MyPointUtils.getIntersectionPoint(a, h, c, j);
        d.setXY((c.x + 2 * e.x + b.x) / 4, (2 * e.y + c.y + b.y) / 4);
        i.setXY((j.x + 2 * h.x + k.x) / 4, (2 * h.y + j.y + k.y) / 4);
    }

    private void initPointTurnRight() {
        //g为a,f中点
        g.setXY((a.x + f.x) / 2, (a.y + f.y) / 2);
        r.setXY(g.x - (f.y - g.y) * (f.y - g.y) / (f.x - g.x), f.y);
        q.setXY(r.x + a.x / 10, f.y);
        p.setXY(2 * r.x / 3 + 1 * a.x / 3, 2 * r.y / 3 + 1 * a.y / 3);
        float eh = (float) Math.hypot(a.x - r.x, a.y - r.y);
        sin0 = (a.y - r.y) / eh;
        cos0 = (a.x - r.x) / eh;
        m.setXY(a.x + calPointFactor * viewHeight * sin0, a.y - calPointFactor * viewHeight * cos0);
        o.setXY(a.x - viewWidth * cos0, a.y - viewWidth * sin0);
        n.setXY(m.x - a.x + o.x, m.y - a.y + o.y);
    }

    /**
     * 如果c点x坐标小于0,根据触摸点重新测量a点坐标
     */
    private void calcPointAByTouchPoint() {
        Log.i("###","re calc");
        float w0 = viewWidth - c.x;
        float w1 = Math.abs(f.x - a.x);
        float w2 = viewWidth * w1 / w0 - viewWidth/100;
        a.x = Math.abs(f.x - w2);
        float h1 = Math.abs(f.y - a.y);
        float h2 = w2 * h1 / w1;
        a.y = Math.abs(f.y - h2);
    }


    /**
     * 向右翻页触发
     */
    public void turnRight(float height, int MODE, Bitmap bitmapNextPage) {
        a.setXY(postAWidth, height);
        startY = height;
//        bitmapLast = bitmapCurrent;
//        drawCurrentPageBitmap(bitmapCurrent, paintCurrent);
        turnPageMode = MODE;
        startTurnRightAnim();
    }

    private void startTurnRightAnim() {
        isTurningPage = true;
        int dx = 0, dy = 0;
        switch (turnPageMode) {
            case TurnPageMode.MODE_LEFT_TOP:
                dx = (int) viewWidth - (int) a.x;
                dy = 0 - (int) a.y;
                f.setXY(-1 * viewWidth, 0f);
                calPointFactor = -1;
                break;
            case TurnPageMode.MODE_LEFT_MIDDLE:
            case TurnPageMode.MODE_LEFT_BOTTOM:
                dx = (int) viewWidth - (int) a.x;
                dy = (int) viewHeight - (int) a.y;
                f.setXY(-1 * viewWidth, viewHeight);
                calPointFactor = 1;
                break;
            default:
                break;
        }
        initPointTurnRight();
        postInvalidate();
        mScroller2.startScroll((int) a.x, (int) a.y, dx, dy, 2500);
    }

    private void startTurnLeftAnim() {
        isTurningPage = true;
        int dx, dy;
        switch (turnPageMode) {
            case TurnPageMode.MODE_RIGHT_TOP:
                dx = (int) (-1 - viewWidth - a.x);
                dy = (int) (1 - a.y);
                break;
            case TurnPageMode.MODE_RIGHT_MIDDLE:
            case TurnPageMode.MODE_RIGHT_BOTTOM:
                dx = (int) (-1 - viewWidth - a.x);
                dy = (int) (viewHeight - 1 - a.y);
                break;
            default:
                dx = 0;
                dy = 0;
                break;
        }
        mScroller1 = new Scroller(context, new AccelerateDecelerateInterpolator());
        sendMode = turnPageMode;
        mScroller1.startScroll((int) a.x, (int) a.y, dx, dy, 2500);
    }

    private void cancelTurnLeftAnim() {
        isTurningPage = true;
        int dx, dy;
        //让a滑动到f点所在位置，留出1像素是为了防止当a和f重叠时出现View闪烁的情况
        if (turnPageMode == TurnPageMode.MODE_RIGHT_BOTTOM || turnPageMode == TurnPageMode.MODE_RIGHT_MIDDLE) {
            dx = (int) (viewWidth - 1 - a.x);
            dy = (int) (viewHeight - 1 - a.y);
        } else if (turnPageMode == TurnPageMode.MODE_RIGHT_TOP) {
            dx = (int) (viewWidth - 1 - a.x);
            dy = (int) (1 - a.y);
        } else {
            dx = 0;
            dy = 0;
        }
        mScroller1.startScroll((int) a.x, (int) a.y, dx, dy, 400);
    }
}
