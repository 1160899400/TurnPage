package com.royole.demo.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

import com.royole.demo.bean.MyPoint;

/**
 * @author HZLI02
 * @date 2018/8/10
 */

public class MyPageView extends View {
    private boolean turnPage = false;
    private float viewWidth;
    private float viewHeight;

    private Scroller mScroller;
    private Paint pageAPaint;
    private Paint pageBPaint;
    private Paint pageCPaint;
    private Path pathA, pathB, pathC;
    private Canvas bitmapCanvas;

    /**
     * a\b\c区域的内容
     */
    private Bitmap bitmapA;
    private Bitmap bitmapB;
    private Bitmap bitmapC;

    /**
     * A区域阴影矩形短边长度参考值
     */
    private float lPathAShadowDis = 0;
    private float rPathAShadowDis = 0;

    private GradientDrawable drawableLeftTopRight;
    private GradientDrawable drawableLeftLowerRight;

    private GradientDrawable drawableRightTopRight;
    private GradientDrawable drawableRightLowerRight;
    private GradientDrawable drawableHorizontalLowerRight;

    private GradientDrawable drawableBTopRight;
    private GradientDrawable drawableBLowerRight;

    private GradientDrawable drawableCTopRight;
    private GradientDrawable drawableCLowerRight;


    /**
     * a为触摸点，f为边角点，c\j为掀起的两边的点
     */
    private MyPoint a, f, g, e, h, c, j, b, k, d, i;

    /**
     * 翻页时最初的触摸区域(A为右上角，B为右中间，C为右下角，D为左上角，E为左中间，F为左下角)
     */
    private int startArea = NO_START;
    private int endArea = 0;
    private static final int NO_START = 0;
    private static final int AREA_A = 1;
    private static final int AREA_B = 2;
    private static final int AREA_C = 3;
    private static final int AREA_D = 4;
    private static final int AREA_E = 5;
    private static final int AREA_F = 6;


    public MyPageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        viewWidth = getMeasuredWidth();
        viewHeight = getMeasuredHeight();
        //在这里初始化A B C页面要展示的内容
        bitmapA = Bitmap.createBitmap((int)viewWidth, (int)viewHeight, Bitmap.Config.RGB_565);
        drawPathAContentBitmap(bitmapA, pageAPaint);

        bitmapB = Bitmap.createBitmap((int)viewWidth, (int)viewHeight, Bitmap.Config.RGB_565);
        drawPathBContentBitmap(bitmapB, pageBPaint);

        bitmapC = Bitmap.createBitmap((int)viewWidth, (int)viewHeight, Bitmap.Config.RGB_565);
        drawPathAContentBitmap(bitmapC, pageCPaint);
        a.setXY(-1, -1);
        initPointXY();
        Log.i("###draw view", "view height width" + viewWidth + "  " + viewHeight);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (a.x == -1 && a.y == -1) {
            drawPageAContent(canvas, getPathADefault());
        } else {
            if (f.x == viewWidth && f.y == 0) {
                drawPageAContent(canvas, getPathAFromTopRight());
                drawPageCContent(canvas, getPathAFromTopRight());
                drawPageBContent(canvas, getPathAFromTopRight());
            } else if (f.x == viewWidth && f.y == viewHeight) {
                drawPageAContent(canvas, getPathAFromLowerRight());
                drawPageCContent(canvas, getPathAFromLowerRight());
                drawPageBContent(canvas, getPathAFromLowerRight());
            }
        }
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            float x = mScroller.getCurrX();
            float y = mScroller.getCurrY();
            if (startArea == AREA_A) {
//                setTouchPoint(x, y, STYLE_TOP_RIGHT);
            } else {
//                setTouchPoint(x, y, STYLE_LOWER_RIGHT);
            }
            if (mScroller.getFinalX() == x && mScroller.getFinalY() == y) {
                setDefaultPath();
            }
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initStartArea(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                if (turnPage && isTurnPage(event.getX(), event.getY())) {
                    initPointXY();
                }
                postInvalidate();
                break;
            case MotionEvent.ACTION_UP:
                cancelTurnPage();
                postInvalidate();
                break;
            default:
                break;
        }

        return true;
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
        if (0.8 * viewWidth < x) {
            if (0.2 * viewHeight > y) {
                startArea = AREA_A;
                a.setXY(x, y);
                f.setXY(viewWidth, 0);
            } else if (0.8 * viewHeight < y) {
                startArea = AREA_C;
                a.setXY(x, y);
                f.setXY(viewWidth, viewHeight);
            } else {
                startArea = AREA_B;
                a.setXY(x, viewHeight - 1);
                f.setXY(viewWidth, viewHeight);
            }
            turnPage = true;
        }
    }



    /**
     * 绘制当前页内容
     */
    private void drawPageAContent(Canvas canvas, Path path) {
        canvas.save();
        canvas.clipPath(pathA);
        canvas.drawBitmap(bitmapA, 0, 0, null);
        drawPathAShadow(canvas, pathA);
        canvas.restore();
    }

    /**
     * 绘出a区域的阴影
     * 包括从右上右下翻时的a区域的左右阴影，以及a区域的水平阴影
     */
    private void drawPathAShadow(Canvas canvas, Path path) {
        canvas.restore();
        canvas.save();
        switch (startArea) {
            case AREA_A:
            case AREA_C:
                drawPathALeftShadow(canvas, pathA);
                drawPathARightShadow(canvas, pathA);
                break;
            case AREA_B:
                canvas.clipPath(path);
                //阴影矩形最大的宽度
                int maxShadowWidth = 30;
                int left = (int) (a.x - Math.min(maxShadowWidth, (rPathAShadowDis / 2)));
                GradientDrawable gradientDrawable = drawableHorizontalLowerRight;
                gradientDrawable.setBounds(left, 0, (int)a.x, (int)viewHeight);
                float mDegrees = (float) Math.toDegrees(Math.atan2(f.x - a.x, f.y - h.y));
                canvas.rotate(mDegrees, a.x, a.y);
                gradientDrawable.draw(canvas);
                break;
            default:
                break;
        }
    }


    /**
     * 绘制A区域左阴影
     *
     * @param canvas
     */
    private void drawPathALeftShadow(Canvas canvas, Path pathA) {
        canvas.restore();
        canvas.save();

        int left;
        int right;
        int top = (int) e.y;
        int bottom = (int) (e.y + viewHeight);

        GradientDrawable gradientDrawable;
        if (startArea == AREA_A) {
            gradientDrawable = drawableLeftTopRight;
            left = (int) (e.x - lPathAShadowDis / 2);
            right = (int) (e.x);
        } else {
            gradientDrawable = drawableLeftLowerRight;
            left = (int) (e.x);
            right = (int) (e.x + lPathAShadowDis / 2);
        }

        Path mPath = new Path();
        mPath.moveTo(a.x - Math.max(rPathAShadowDis, lPathAShadowDis) / 2, a.y);
        mPath.lineTo(d.x, d.y);
        mPath.lineTo(e.x, e.y);
        mPath.lineTo(a.x, a.y);
        mPath.close();
        canvas.clipPath(pathA);
        canvas.clipPath(mPath);

        float mDegrees = (float) Math.toDegrees(Math.atan2(e.x - a.x, a.y - e.y));
        canvas.rotate(mDegrees, e.x, e.y);

        gradientDrawable.setBounds(left, top, right, bottom);
        gradientDrawable.draw(canvas);
    }

    /**
     * 绘制A区域右阴影
     *
     * @param canvas
     */
    private void drawPathARightShadow(Canvas canvas, Path pathA) {
        canvas.restore();
        canvas.save();
        //view对角线长度
        float viewDiagonalLength = (float) Math.hypot(viewWidth, viewHeight);
        int left = (int) h.x;
        //需要足够长的长度
        int right = (int) (h.x + viewDiagonalLength * 10);
        int top;
        int bottom;

        GradientDrawable gradientDrawable;
        if (startArea == AREA_A) {
            gradientDrawable = drawableRightTopRight;
            top = (int) (h.y - rPathAShadowDis / 2);
            bottom = (int) h.y;
        } else {
            gradientDrawable = drawableRightLowerRight;
            top = (int) h.y;
            bottom = (int) (h.y + rPathAShadowDis / 2);
        }
        gradientDrawable.setBounds(left, top, right, bottom);

        Path mPath = new Path();
        mPath.moveTo(a.x - Math.max(rPathAShadowDis, lPathAShadowDis) / 2, a.y);
        mPath.lineTo(h.x, h.y);
        mPath.lineTo(a.x, a.y);
        mPath.close();
        canvas.clipPath(pathA);
        canvas.clipPath(mPath);

        float mDegrees = (float) Math.toDegrees(Math.atan2(a.y - h.y, a.x - h.x));
        canvas.rotate(mDegrees, h.x, h.y);
        gradientDrawable.draw(canvas);
    }

    /**
     * 绘制当下一页内容
     */
    private void drawPageBContent(Canvas canvas, Path path) {

    }

    /**
     * 绘制当前页的背面内容
     */
    private void drawPageCContent(Canvas canvas, Path path) {

    }

    /**
     * 回到默认状态
     */
    public void setDefaultPath() {
        a.x = -1;
        a.y = -1;
        turnPage = false;
        startArea = NO_START;
        postInvalidate();
    }

    /**
     * 绘制默认的全页面
     */
    private Path getPathADefault() {
        pathA.reset();
        pathA.lineTo(0, viewHeight);
        pathA.lineTo(viewWidth, viewHeight);
        pathA.lineTo(viewWidth, 0);
        pathA.close();
        return pathA;
    }

    /**
     * 当翻起区域在右下角时的path绘制
     *
     * @return
     */
    private Path getPathAFromLowerRight() {
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

    private Path getPathC() {
        pathC.reset();
        pathC.moveTo(i.x, i.y);
        pathC.lineTo(d.x, d.y);
        pathC.lineTo(b.x, b.y);
        pathC.lineTo(a.x, a.y);
        pathC.lineTo(k.x, k.y);
        pathC.close();
        return pathC;
    }




    /**
     * 触屏并滑动一段距离时触发，判断是否为翻页
     *
     * @param x
     * @param y
     * @return
     */
    private boolean isTurnPage(float x, float y) {
        return true;
    }

    /**
     * 取消翻页
     */
    private void cancelTurnPage() {
        int dx, dy;
        //让a滑动到f点所在位置，留出1像素是为了防止当a和f重叠时出现View闪烁的情况
        if (startArea == AREA_A) {
            dx = (int) (viewWidth - 1 - a.x);
            dy = (int) (1 - a.y);
        } else {
            dx = (int) (viewWidth - 1 - a.x);
            dy = (int) (viewHeight - 1 - a.y);
        }
        mScroller.startScroll((int) a.x, (int) a.y, dx, dy, 400);
        turnPage = false;
    }


    /**
     * 对View初始化
     */
    private void init(Context context, AttributeSet attrs) {
        a = new MyPoint();
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
        pageAPaint = new Paint();
        pageAPaint.setColor(Color.BLUE);
        pageAPaint.setAntiAlias(true);

        pageBPaint = new Paint();
        pageBPaint.setAntiAlias(true);
        pageBPaint.setColor(Color.GRAY);
        pageBPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP));

        pageCPaint = new Paint();
        pageCPaint.setAntiAlias(true);
        pageCPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP));

        pathA = new Path();
        pathB = new Path();
        pathC = new Path();
        mScroller = new Scroller(context, new LinearInterpolator());
        createGradientDrawable();



    }
    private void drawPathAContentBitmap(Bitmap bitmap, Paint pathPaint) {
        Canvas mCanvas = new Canvas(bitmap);
        //下面开始绘制区域内的内容...
        mCanvas.drawPath(getPathADefault(), pathPaint);

        //结束绘制区域内的内容...
    }

    private void drawPathBContentBitmap(Bitmap bitmap, Paint pathPaint) {
        Canvas mCanvas = new Canvas(bitmap);
        //下面开始绘制区域内的内容...
        mCanvas.drawPath(getPathADefault(), pathPaint);

        //结束绘制区域内的内容...
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

        //计算d点到ae的距离
        float lA = a.y - e.y;
        float lB = e.x - a.x;
        float lC = a.x * e.y - e.x * a.y;
//        lPathAShadowDis = Math.abs((lA * d.x + lB * d.y + lC) / (float) Math.hypot(lA, lB));

        //计算i点到ah的距离
        float rA = a.y - h.y;
        float rB = h.x - a.x;
        float rC = a.x * h.y - h.x * a.y;
//        rPathAShadowDis = Math.abs((rA * i.x + rB * i.y + rC) / (float) Math.hypot(rA, rB))

    }

    /**
     * 获取p1,p2的连线与p3,p4的连线的相交点
     *
     * @param p1
     * @param p2
     * @param p3
     * @param p4
     * @return
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

    /**
     * 初始化各区域阴影GradientDrawable
     */
    private void createGradientDrawable() {
        int deepColor = 0x33333333;
        int lightColor = 0x01333333;
        int[] gradientColors = new int[]{lightColor, deepColor};//渐变颜色数组
        drawableLeftTopRight = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, gradientColors);
        drawableLeftTopRight.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        drawableLeftLowerRight = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, gradientColors);
        drawableLeftLowerRight.setGradientType(GradientDrawable.LINEAR_GRADIENT);

        deepColor = 0x22333333;
        lightColor = 0x01333333;
        gradientColors = new int[]{deepColor, lightColor, lightColor};
        drawableRightTopRight = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, gradientColors);
        drawableRightTopRight.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        drawableRightLowerRight = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, gradientColors);
        drawableRightLowerRight.setGradientType(GradientDrawable.LINEAR_GRADIENT);

        deepColor = 0x44333333;
        lightColor = 0x01333333;
        gradientColors = new int[]{lightColor, deepColor};//渐变颜色数组
        drawableHorizontalLowerRight = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, gradientColors);
        ;
        drawableHorizontalLowerRight.setGradientType(GradientDrawable.LINEAR_GRADIENT);

        deepColor = 0x55111111;
        lightColor = 0x00111111;
        gradientColors = new int[]{deepColor, lightColor};//渐变颜色数组
        drawableBTopRight = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, gradientColors);
        drawableBTopRight.setGradientType(GradientDrawable.LINEAR_GRADIENT);//线性渐变
        drawableBLowerRight = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, gradientColors);
        drawableBLowerRight.setGradientType(GradientDrawable.LINEAR_GRADIENT);

        deepColor = 0x55333333;
        lightColor = 0x00333333;
        gradientColors = new int[]{lightColor, deepColor};//渐变颜色数组
        drawableCTopRight = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, gradientColors);
        drawableCTopRight.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        drawableCLowerRight = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, gradientColors);
        drawableCLowerRight.setGradientType(GradientDrawable.LINEAR_GRADIENT);
    }
}
