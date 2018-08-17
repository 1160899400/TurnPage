package com.royole.demo.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.os.Trace;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

import com.royole.demo.R;
import com.royole.demo.bean.MyPoint;
import com.royole.demo.constants.TurnPageMode;


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
    public int turnPageMode = TurnPageMode.MODE_NO_ACTION;
    public int turnPageMode2 = TurnPageMode.MODE_NO_ACTION;

    /**
     * 翻页结果
     */
    private boolean turnPage = false;
    /**
     * 翻页状态(正在翻页)
     */
    private boolean isTurningPage = false;
    private Scroller mScroller;
    private Paint pathAPaint;
    private Paint pathBPaint;
    private Paint pathCPaint;
    private Path pathA, pathB, pathC;

    private Paint textPaint;//绘制文字画笔

    public float postAHeight;

    /**
     * a\b\c区域的内容
     */
    private Bitmap bitmapA;
    private Bitmap bitmapB;
    public Bitmap bitmapC;


    private float[] mMatrixArray = {0, 0, 0, 0, 0, 0, 0, 0, 1.0f};
    private Matrix mMatrix;


    /**
     * a为触摸点，f为边角点，c\j为掀起的两边的点
     */
    private MyPoint a, f, g, e, h, c, j, b, k, d, i;


    public RightPageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
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
        bitmapA = Bitmap.createBitmap((int) viewWidth, (int) viewHeight, Bitmap.Config.RGB_565);
        drawPathAContentBitmap(bitmapA, pathAPaint);
        bitmapB = Bitmap.createBitmap((int) viewWidth, (int) viewHeight, Bitmap.Config.RGB_565);
        drawPathBContentBitmap(bitmapB, pathBPaint);
        bitmapC = Bitmap.createBitmap((int) viewWidth, (int) viewHeight, Bitmap.Config.RGB_565);
        drawPathAContentBitmap(bitmapC, pathCPaint);
        a.setXY(-1, -1);
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
        //颜色应与c区域颜色一致
        canvas.drawColor(ContextCompat.getColor(context, R.color.red_light));
        if (a.x == -1 && a.y == -1) {
            drawPathAContent(canvas, getPathDefault());
        } else {
            if (f.x == viewWidth && f.y == 0) {
                beginTrace("drawPathA");
                drawPathAContent(canvas, getPathAFromTopRight());
                endTrace();
                beginTrace("drawPathC");
                drawPathCContent(canvas, getPathAFromTopRight());
                endTrace();
                beginTrace("drawPathB");
                drawPathBContent(canvas, getPathAFromTopRight());
                endTrace();
            } else if (f.x == viewWidth && f.y == viewHeight) {
                beginTrace("drawPathA");
                drawPathAContent(canvas, getPathAFromBottomRight());
                endTrace();
                beginTrace("drawPathC");
                drawPathCContent(canvas, getPathAFromBottomRight());
                endTrace();
                beginTrace("drawPathB");
                drawPathBContent(canvas, getPathAFromBottomRight());
                endTrace();
            }
        }
    }

    @TargetApi(18)
    private void beginTrace(String tag) {
        Trace.beginSection(tag);
    }

    @TargetApi(18)
    private void endTrace() {
        Trace.endSection();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            float x = mScroller.getCurrX();
            float y = mScroller.getCurrY();
            a.setXY(x, y);
            initPointXY();
            postInvalidate();
            //翻页完成时触发，恢复初始状态
            if (mScroller.getFinalX() == x && mScroller.getFinalY() == y) {
                setDefaultPath();
            }
            if (turnPage && x - 50 < 0 && x >= 0) {
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
                            successTurnPage();
                            turnPageMode2 = turnPageMode;

                        } else {
                            cancelTurnPage();
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
        } else if (0.15 * viewWidth > x) {
            turnPageMode = TurnPageMode.MODE_NO_ACTION;
        } else {
            turnPageMode = TurnPageMode.MODE_NO_ACTION;
        }

    }

    private void touchPoint(float pointX, float pointY) {
        switch (turnPageMode) {
            case TurnPageMode.MODE_RIGHT_TOP:
                a.setXY(pointX, pointY);
                f.setXY(viewWidth, 0);
                initPointXY();
                if (c.x < 0) {
                    calcPointAByTouchPoint();
                    initPointXY();
                }
                if (a.x < 0.15 * viewWidth) {
                    turnPage = true;
                } else {
                    turnPage = false;
                }
                postInvalidate();
                break;
            case TurnPageMode.MODE_RIGHT_MIDDLE:
                a.setXY(pointX, pointY);
                a.y = viewHeight - 1;
                f.setXY(viewWidth, viewHeight);
                initPointXY();
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
                initPointXY();
                if (c.x < 0) {
                    calcPointAByTouchPoint();
                    initPointXY();
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

    private void drawPathAContent(Canvas canvas, Path path) {
        canvas.save();
        //对绘制内容进行裁剪，取和A区域的交集
        canvas.clipPath(path, Region.Op.INTERSECT);
        canvas.drawBitmap(bitmapA, 0, 0, null);
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
        mCanvas.drawText("这是在A区域的内容...AAAA", viewWidth - 260, viewHeight - 100, textPaint);
    }


    /**
     * 绘制下一页内容
     */
    private void drawPathBContent(Canvas canvas, Path path) {
        canvas.save();
        //裁剪出A区域
        canvas.clipPath(path);
        //裁剪出A和C区域的全集
        canvas.clipPath(getPathC(), Region.Op.UNION);
        //裁剪出B区域中不同于与AC区域的部分
        canvas.clipPath(getPathB(), Region.Op.REVERSE_DIFFERENCE);
        canvas.drawBitmap(bitmapB, 0, 0, null);
        canvas.restore();

    }

    private void drawPathBContentBitmap(Bitmap bitmap, Paint pathPaint) {
        Canvas mCanvas = new Canvas(bitmap);
        //下面开始绘制区域内的内容...
        mCanvas.drawPath(getPathDefault(), pathPaint);

        //结束绘制区域内的内容...
    }

    /**
     * 绘制当前页的背面内容
     */
    private void drawPathCContent(Canvas canvas, Path path) {
        canvas.save();
        canvas.clipPath(path);
        //裁剪出C区域不同于A区域的部分
        canvas.clipPath(getPathC(), Region.Op.REVERSE_DIFFERENCE);

        float eh = (float) Math.hypot(f.x - e.x, h.y - f.y);
        float sin0 = (f.x - e.x) / eh;
        float cos0 = (h.y - f.y) / eh;
        //设置翻转和旋转矩阵
        mMatrixArray[0] = -(1 - 2 * sin0 * sin0);
        mMatrixArray[1] = 2 * sin0 * cos0;
        mMatrixArray[3] = 2 * sin0 * cos0;
        mMatrixArray[4] = 1 - 2 * sin0 * sin0;
        mMatrix.reset();
        //翻转和旋转
        mMatrix.setValues(mMatrixArray);
        //沿当前XY轴负方向位移得到 矩形A₃B₃C₃D₃
        mMatrix.preTranslate(-e.x, -e.y);
        //沿原XY轴方向位移得到 矩形A4 B4 C4 D4
        mMatrix.postTranslate(e.x, e.y);
        canvas.drawBitmap(bitmapC, mMatrix, null);

        canvas.restore();
    }


    /**
     * 回到默认状态
     */
    private void setDefaultPath() {
        a.setXY(-1, -1);
        turnPageMode = TurnPageMode.MODE_NO_ACTION;
        isTurningPage = false;
        postInvalidate();
    }


    private Path getPathDefault() {
        pathA.reset();
        pathA.lineTo(0, viewHeight);
        pathA.lineTo(viewWidth, viewHeight);
        pathA.lineTo(viewWidth, 0);
        pathA.close();
        return pathA;
    }


    /**
     * 当翻起区域在右上角时的path绘制
     *
     * @return
     */
    private Path getPathAFromTopRight() {
        pathA.reset();
        pathA.lineTo(c.x, c.y);
        pathA.quadTo(e.x, e.y, b.x, b.y);
        pathA.lineTo(a.x, a.y);
        pathA.lineTo(k.x, k.y);
        pathA.quadTo(h.x, h.y, j.x, j.y);
        pathA.lineTo(viewWidth, viewHeight);
        pathA.lineTo(0, viewHeight);
        pathA.close();
        return pathA;
    }

    /**
     * 当翻起区域在右下角时的path绘制
     *
     * @return
     */
    private Path getPathAFromBottomRight() {
        pathA.reset();
        pathA.lineTo(0, viewHeight);
        pathA.lineTo(c.x, c.y);
        pathA.quadTo(e.x, e.y, b.x, b.y);
        pathA.lineTo(a.x, a.y);
        pathA.lineTo(k.x, k.y);
        pathA.quadTo(h.x, h.y, j.x, j.y);
        pathA.lineTo(viewWidth, 0);
        pathA.close();//闭合区域
        return pathA;
    }

    /**
     * 绘制区域B
     *
     * @return
     */
    private Path getPathB() {
        pathB.reset();
        pathB.lineTo(0, viewHeight);
        pathB.lineTo(viewWidth, viewHeight);
        pathB.lineTo(viewWidth, 0);
        pathB.close();
        return pathB;
    }

    private Path getPathC() {
        pathC.reset();
        pathC.moveTo(i.x, i.y);
        pathC.lineTo(d.x, d.y);
        pathC.lineTo(b.x, b.y);
        pathC.lineTo(a.x, a.y);
        pathC.lineTo(k.x, k.y);
        pathC.close();//闭合区域
        return pathC;
    }


    /**
     * 成功翻页
     */
    private void successTurnPage() {
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
        mScroller = new Scroller(context, new AccelerateDecelerateInterpolator());
        isTurningPage = true;
        mScroller.startScroll((int) a.x, (int) a.y, dx, dy, 1000);

    }

    /**
     * 取消翻页
     */
    private void cancelTurnPage() {
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
        mScroller = new Scroller(context, new LinearInterpolator());
        isTurningPage = true;

        mScroller.startScroll((int) a.x, (int) a.y, dx, dy, 400);

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

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextAlign(Paint.Align.CENTER);
        //设置自像素。如果该项为true，将有助于文本在LCD屏幕上的显示效果。
        textPaint.setSubpixelText(true);
        textPaint.setTextSize(30);
        mScroller = new Scroller(context, new LinearInterpolator());

        pathA = new Path();
        pathB = new Path();
        pathC = new Path();

        mMatrix = new Matrix();
    }


    /**
     * 初始化折起区域的关键点坐标
     */
    private void initPointXY() {
        //g为a,f中点
        g.setXY((a.x + f.x) / 2, (a.y + f.y) / 2);

        //eh与af垂直，e为与f水平的点，h为与f垂直的点
        e.setXY(g.x - (f.y - g.y) * (f.y - g.y) / (f.x - g.x), f.y);
        h.setXY(f.x, g.y - (f.x - g.x) * (f.x - g.x) / (f.y - g.y));
        c.setXY(e.x - (f.x - e.x) / 2, f.y);
        j.setXY(f.x, h.y - (f.y - h.y) / 2);
        b = getIntersectionPoint(a, e, c, j);
        k = getIntersectionPoint(a, h, c, j);
        d.setXY((c.x + 2 * e.x + b.x) / 4, (2 * e.y + c.y + b.y) / 4);
        i.setXY((j.x + 2 * h.x + k.x) / 4, (2 * h.y + j.y + k.y) / 4);


    }

    /**
     * 如果c点x坐标小于0,根据触摸点重新测量a点坐标
     */
    private void calcPointAByTouchPoint() {
        float w0 = viewWidth - c.x;

        float w1 = Math.abs(f.x - a.x);
        float w2 = viewWidth * w1 / w0;
        a.x = Math.abs(f.x - w2);

        float h1 = Math.abs(f.y - a.y);
        float h2 = w2 * h1 / w1;
        a.y = Math.abs(f.y - h2);
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
