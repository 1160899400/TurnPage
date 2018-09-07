package com.royole.demo.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.graphics.drawable.GradientDrawable;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

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

    private final int MAX_PAGE = 8;
    private int deepColor = 0x55333333;
    private int lightColor = 0x01333333;

    /**
     * 翻页模式：不翻页，从右上右下右中，左上左下左中翻页
     */
    private int turnPageMode = TurnPageMode.MODE_NO_ACTION;
    public int sendMode = TurnPageMode.MODE_NO_ACTION;

    public int pageIndex = 2;

    public float postAWidth;
    public float postAHeight;

    //拉伸图像时细分网格以及网格点坐标
    private static final int SUB_WIDTH = 19, SUB_HEIGHT = 19;
    private float[] mDistortPoint = new float[(SUB_WIDTH + 1) * (SUB_HEIGHT + 1) * 2];

    /**
     * 翻页成功结果
     */
    private boolean turnPage = false;


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
    private Bitmap bmpCurrentPage;
    private Bitmap bmpNextPage;
    private Bitmap bmpBackPage;
    private Bitmap bmpLastPage;
    private float sin0;
    private float cos0;
    private Matrix mMatrix;

    private Paint textPaint;

    float lPathAShadowDis = 10.0f;
    float rPathAShadowDis = 10.0f;
    private GradientDrawable shadow1;
    private GradientDrawable shadow2;


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
        mScroller1 = new Scroller(context, new LinearInterpolator());
        mScroller2 = new Scroller(context, new AccelerateDecelerateInterpolator());
        mMatrix = new Matrix();
        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setSubpixelText(true);
        textPaint.setTextSize(30);
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
        setAllPage();
        a.setXY(-1, -1);
    }


    public void setAllPage() {
        bmpCurrentPage = getBitmap(pageIndex);
        if (pageIndex + 2 <= MAX_PAGE) {
            bmpNextPage = getBitmap(pageIndex + 2);
        }
        if (pageIndex + 1 <= MAX_PAGE) {
            bmpBackPage = getBitmap(pageIndex + 1);
        }
        if (pageIndex + 1 <= MAX_PAGE) {
            bmpBackPage = getBitmap(pageIndex + 1);
        }
        if (pageIndex - 2 >= 1) {
            bmpLastPage = getBitmap(pageIndex - 2);
        }
    }

    public Bitmap getBitmap(int index) {
        String filePath = Environment.getExternalStorageDirectory().getPath() + "/ImageTest/" + index + ".png";
        BitmapFactory.Options config = new BitmapFactory.Options();
        config.inScaled = true;
        config.inDensity = 480;
        config.inTargetDensity = 480;
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, config).copy(Bitmap.Config.ARGB_8888, true);
        return bitmap;
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
        } else if (TurnPageMode.MODE_RIGHT_MIDDLE == turnPageMode || TurnPageMode.MODE_RIGHT_BOTTOM == turnPageMode) {
            drawCurPage(canvas, getPathAFromBottomRight());
            drawBackPage(canvas, getPathC(getPathAFromBottomRight()));
            drawShadowHorizontal(canvas, getPathAFromBottomRight());
            drawNextPage(canvas, getPathB(getPathAFromBottomRight()));
            drawPathBShadow(canvas, getPathAFromBottomRight());
        } else if (TurnPageMode.MODE_LEFT_TOP == turnPageMode || TurnPageMode.MODE_LEFT_MIDDLE == turnPageMode || TurnPageMode.MODE_LEFT_BOTTOM == turnPageMode) {
            drawCurPage(canvas, getPathAFromLeft());
            drawLastPage(canvas, getPathD());
            drawShadowRightTurn(canvas);
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
            //翻页完成时触发
            if (mScroller1.isFinished()) {
                if (turnPage) {
                    addPage();
                } else {
                    setDefaultPath();
                }
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
            //翻页完成时触发
            if (mScroller2.isFinished()) {
                decPage();
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
            if (0.7 * viewHeight < y) {
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
     * 绘制下一页内容
     */
    private void drawNextPage(Canvas canvas, Path path) {
        canvas.save();
        canvas.clipPath(path);
        canvas.drawBitmap(bmpNextPage, 0, 0, null);
        canvas.restore();
    }


    /**
     * 绘制当前页的背面内容
     */
    private void drawBackPage(Canvas canvas, Path path) {

        canvas.save();

        Canvas canvasCache = new Canvas();
        Bitmap bitmapCache = Bitmap.createBitmap((int) viewWidth + 200, (int) viewHeight + 200, Bitmap.Config.ARGB_8888);
        canvasCache.setBitmap(bitmapCache);
        // 计算底部扭曲的起始细分下标
        float mSubMinWidth = viewWidth / SUB_WIDTH;
        float mSubMinHeight = viewHeight / SUB_HEIGHT;
        int mSubWidthStart = (int) Math.round(MyPointUtils.getLength(b, a) / mSubMinWidth) - 1;
        int mSubWidthEnd = (int) Math.round((MyPointUtils.getLength(e, a) + MyPointUtils.getLength(c, e)) / mSubMinWidth) + 1;
        // 计算右侧扭曲的起始细分下标
        int mSubHeightEnd = (int) Math.round((viewHeight - MyPointUtils.getLength(k, a)) / mSubMinHeight) + 1;
        int mSubHeightStart = (int) Math.round((viewHeight - MyPointUtils.getLength(a, h)) / mSubMinHeight) - 1;

        // 长边偏移
        float offsetLong = (i.x - k.x) / 2;
        // 长边偏移倍增
        float mulOffsetLong = 1.0F;
        // 短边偏移
        float offsetShort = (e.x - c.x) / 2;
        // 短边偏移倍增
        float mulOffsetShort = 1.0F;
        /*
         * 生成折叠区域的扭曲坐标
         */

        Log.i("###sub", "start width:  " + mSubWidthStart + "   end width:  " + mSubWidthEnd);
        Log.i("###sub", "start height:  " + mSubHeightStart + "   end height:  " + mSubHeightEnd);

        int index = 0;
        for (int y = 0; y <= SUB_HEIGHT; y++) {
            float fy = viewHeight * y / SUB_HEIGHT;
            for (int x = 0; x <= SUB_WIDTH; x++) {
                float fx = viewWidth * x / SUB_WIDTH;
                /*
                 * 左侧扭曲
                 */
                if (x >= 0 && x <= 3) {
                    if (y >= mSubHeightStart && y <= mSubHeightEnd) {
                        fx = fx - offsetLong * mulOffsetLong;
                        mulOffsetLong = mulOffsetLong / 1.5F;
                    }
                }

                /*
                 * 底部扭曲
                 */
                if (y == SUB_HEIGHT) {
                    if (x >= mSubWidthStart && x <= mSubWidthEnd) {
                        fy = fy + offsetShort * mulOffsetShort;
                        mulOffsetShort = mulOffsetShort / 1.5F;
                    }
                }
                mDistortPoint[index * 2] = fx;
                mDistortPoint[index * 2 + 1] = fy;
                index += 1;
            }
        }
        canvasCache.drawBitmapMesh(bmpBackPage, SUB_WIDTH, SUB_HEIGHT, mDistortPoint, 0, null, 0, null);


        canvas.clipPath(path);
        float eh = (float) Math.hypot(f.x - e.x, h.y - f.y);
        float cos0 = (h.y - f.y) / eh;
        float angel = new Double(Math.toDegrees(Math.acos(cos0))).floatValue();
        //设置翻转和旋转矩阵
        mMatrix.reset();
        mMatrix.setRotate(-2 * angel, 0, f.y);
        mMatrix.postTranslate(a.x - 0, a.y - f.y);

//        Paint vPaint = new Paint();
//        vPaint.setStyle(Paint.Style.STROKE);
//        vPaint.setAlpha(60);
//        canvas.drawColor(getResources().getColor(R.color.yellow_light));
        canvas.drawBitmap(bitmapCache, mMatrix, null);
//        canvas.drawBitmap(bmpBackPage, mMatrix, null);
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

    private void addPage() {
        pageIndex += 2;
        setAllPage();
        setDefaultPath();
    }

    private void decPage() {
        pageIndex -= 2;
        setAllPage();
        setDefaultPath();
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
        Path pathCache = new Path();
        pathCache.moveTo(a.x, a.y);
        pathCache.lineTo(b.x, b.y);
        pathCache.lineTo(d.x, d.y);
        pathCache.lineTo(i.x, i.y);
        pathCache.lineTo(k.x, k.y);
        pathCache.close();
        path.op(pathCache, Path.Op.DIFFERENCE);
        return path;
    }

    /**
     * 背面区域
     *
     * @return
     */
    private Path getPathC(Path pathA) {
//        Path path = new Path();
//        path.moveTo(i.x, i.y);
//        path.lineTo(d.x, d.y);
//        path.lineTo(b.x, b.y);
//        path.lineTo(a.x, a.y);
//        path.lineTo(k.x, k.y);
//        path.close();
//        path.op(pathA, Path.Op.DIFFERENCE);
        Path path = getPathDefault();
        path.op(pathA, Path.Op.DIFFERENCE);
        path.op(getPathB(pathA), Path.Op.DIFFERENCE);
        return path;
    }

    /**
     * 上一页区域
     */
    private Path getPathD() {
        Path path = new Path();
//        path.moveTo(a.x, a.y);
//        path.quadTo(m.x, m.y, q.x, q.y);
//        path.lineTo(n.x, n.y);
//        path.lineTo(p.x, p.y);
//        path.quadTo(o.x, o.y, a.x, a.y);

        path.moveTo(m.x, m.y);
        path.lineTo(a.x, a.y);
//        path.lineTo(p.x, p.y);
//        path.quadTo(r.x, r.y, 0, f.y);
//        path.lineTo(r.x, r.y);
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
//        if(){
//
//        }
        h.setXY(f.x, g.y - (f.x - g.x) * (f.x - g.x) / (f.y - g.y));

        c.setXY(e.x - (f.x - e.x) / 2, f.y);
        j.setXY(f.x, h.y - (f.y - h.y) / 2);
        b = MyPointUtils.getIntersectionPoint(a, e, c, j);
        k = MyPointUtils.getIntersectionPoint(a, h, c, j);
        d.setXY((c.x + 2 * e.x + b.x) / 4, (2 * e.y + c.y + b.y) / 4);
        i.setXY((j.x + 2 * h.x + k.x) / 4, (2 * h.y + j.y + k.y) / 4);
        //计算d点到ae的距离
        float lA = a.y - e.y;
        float lB = e.x - a.x;
        float lC = a.x * e.y - e.x * a.y;
        lPathAShadowDis = Math.abs((lA * d.x + lB * d.y + lC) / (float) Math.hypot(lA, lB));
        //计算i点到ah的距离
        float rA = a.y - h.y;
        float rB = h.x - a.x;
        float rC = a.x * h.y - h.x * a.y;
        rPathAShadowDis = Math.abs((rA * i.x + rB * i.y + rC) / (float) Math.hypot(rA, rB));
    }

    private void initPointTurnRight() {
        //g为a,f中点
        g.setXY((a.x + f.x) / 2, (a.y + f.y) / 2);
        r.setXY(g.x - (f.y - g.y) * (f.y - g.y) / (f.x - g.x), f.y);
//        q.setXY(r.x + a.x / 10, f.y);
//        p.setXY(2 * r.x / 3 + 1 * a.x / 3, 2 * r.y / 3 + 1 * a.y / 3);
        float eh = (float) Math.hypot(a.x - r.x, a.y - r.y);
        sin0 = (a.y - r.y) / eh;
        cos0 = (a.x - r.x) / eh;
        m.setXY(a.x + calPointFactor * viewHeight * sin0, a.y - calPointFactor * viewHeight * cos0);
        o.setXY(a.x - viewWidth * cos0, a.y - viewWidth * sin0);
        n.setXY(m.x - a.x + o.x, m.y - a.y + o.y);

//        Log.i("###", "o.x:  " + o.x + "  o.y:  " + o.y);
//        p.setXY(o.x - calPointFactor * (viewWidth - a.x) / 10 * sin0, o.y + calPointFactor * (viewWidth - a.x) / 10 * cos0);
//        q.setXY(m.x + (viewWidth - a.x) / 6 * cos0, m.y + (viewWidth - a.x) / 6 * sin0);
//        Log.i("###", "p.x:  " + p.x + "  p.y:  " + p.y);
//        Log.i("###", "q.x:  " + q.x + "  q.y:  " + q.y);
    }


    /**
     * 如果c点x坐标小于0,根据触摸点重新测量a点坐标
     */
    private void calcPointAByTouchPoint() {
        float w0 = viewWidth - c.x;
        float w1 = Math.abs(f.x - a.x);
        float w2 = viewWidth * w1 / w0 - viewWidth / 100;
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
        invalidate();
    }

    private void startTurnLeftAnim() {
        isTurningPage = true;
        int dx, dy;
        dx = (int) (-1 - viewWidth - a.x);
        dy = (int) (viewHeight - 1 - a.y);
        mScroller1 = new Scroller(context, new AccelerateDecelerateInterpolator());
        sendMode = turnPageMode;
        mScroller1.startScroll((int) a.x, (int) a.y, dx, dy, 2500);
        invalidate();
    }

    private void cancelTurnLeftAnim() {
        isTurningPage = true;
        int dx, dy;
        //让a滑动到f点所在位置，留出1像素是为了防止当a和f重叠时出现View闪烁的情况
        dx = (int) (viewWidth - 1 - a.x);
        dy = (int) (viewHeight - 1 - a.y);
        mScroller1.startScroll((int) a.x, (int) a.y, dx, dy, 400);
    }

    private void drawShadow(Canvas canvas, Path pathA) {
        float viewDiagonalLength = (float) Math.hypot(viewWidth, viewHeight);
        Path mPath = new Path();
        //渐变颜色数组
        int[] gradientColor1 = {lightColor, deepColor};
        int[] gradientColor2 = {deepColor, lightColor, lightColor};
        if (turnPageMode == TurnPageMode.MODE_RIGHT_TOP) {
            shadow1 = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, gradientColor1);
            shadow1.setGradientType(GradientDrawable.LINEAR_GRADIENT);
            shadow1.setBounds((int) (e.x - lPathAShadowDis / 2), (int) e.y, (int) (e.x), (int) (e.y + viewHeight));
            shadow2 = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, gradientColor2);
            shadow2.setGradientType(GradientDrawable.LINEAR_GRADIENT);
            shadow2.setBounds((int) h.x, (int) (h.y - rPathAShadowDis / 2), (int) (h.x + viewDiagonalLength * 10), (int) h.y);
        } else {
            shadow1 = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, gradientColor1);
            shadow1.setGradientType(GradientDrawable.LINEAR_GRADIENT);
            shadow1.setBounds((int) (e.x), (int) e.y, (int) (e.x + lPathAShadowDis / 2), (int) (e.y + viewHeight));
            shadow2 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, gradientColor2);
            shadow2.setGradientType(GradientDrawable.LINEAR_GRADIENT);
            shadow2.setBounds((int) h.x, (int) h.y, (int) (h.x + viewDiagonalLength * 10), (int) (h.y + rPathAShadowDis / 2));
        }
        canvas.save();
        mPath.moveTo(a.x - Math.max(rPathAShadowDis, lPathAShadowDis) / 2, a.y);
        mPath.lineTo(d.x, d.y);
        mPath.lineTo(e.x, e.y);
        mPath.lineTo(a.x, a.y);
        mPath.close();
        canvas.clipPath(pathA);
        canvas.clipPath(mPath, Region.Op.INTERSECT);
        canvas.rotate((float) Math.toDegrees(Math.atan2(e.x - a.x, a.y - e.y)), e.x, e.y);
        shadow1.draw(canvas);
        canvas.restore();
        canvas.save();
        mPath.reset();
        mPath.moveTo(a.x - Math.max(rPathAShadowDis, lPathAShadowDis) / 2, a.y);
        mPath.lineTo(h.x, h.y);
        mPath.lineTo(a.x, a.y);
        mPath.close();
        canvas.clipPath(pathA);
        canvas.clipPath(mPath, Region.Op.INTERSECT);
        canvas.rotate((float) Math.toDegrees(Math.atan2(a.y - h.y, a.x - h.x)), h.x, h.y);
        shadow2.draw(canvas);
        canvas.restore();
    }

    private void drawShadowHorizontal(Canvas canvas, Path pathA) {
        canvas.save();
        int[] gradientColors = {lightColor, deepColor};
        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, gradientColors);
        gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        //阴影矩形最大的宽度
        int maxShadowWidth = 30;
        gradientDrawable.setBounds((int) (a.x - Math.min(maxShadowWidth, (rPathAShadowDis / 2))), 0, (int) (a.x), (int) viewHeight);
        canvas.clipPath(pathA, Region.Op.INTERSECT);
        float mDegrees = (float) Math.toDegrees(Math.atan2(f.x - a.x, f.y - h.y));
        canvas.rotate(mDegrees, a.x, a.y);
        gradientDrawable.draw(canvas);
        canvas.restore();
    }

    private void drawShadowRightTurn(Canvas canvas) {
        canvas.save();
        int[] gradientColor = {0x30333333, deepColor, lightColor};
        GradientDrawable shadow1, shadow2;
        Path path1, path2;
        float mDegree1, mDegree2;
        float shadowLength = rPathAShadowDis / 8;
        shadow1 = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, gradientColor);
        shadow1.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        shadow2 = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, gradientColor);
        shadow2.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        if (turnPageMode == TurnPageMode.MODE_LEFT_TOP) {
            shadow1.setBounds((int) (a.x), (int) (a.y), (int) (a.x + shadowLength), (int) (a.y + viewHeight));
            shadow2.setBounds((int) a.x, (int) (a.y - viewWidth), (int) (a.x + shadowLength), (int) (a.y));
            mDegree1 = (float) Math.toDegrees(Math.acos(cos0));
            mDegree2 = mDegree1 - 90;
            path1 = new Path();
            path1.moveTo(a.x, a.y);
            path1.lineTo(a.x + 7, a.y - 7);
            path1.quadTo(m.x, m.y, m.x + rPathAShadowDis, m.y);
            path1.lineTo(m.x, m.y);
            path1.close();
            path2 = new Path();
            path2.moveTo(a.x, a.y);
            path2.lineTo(a.x + 7, a.y - 7);
            path2.quadTo(o.x, o.y, o.x + rPathAShadowDis, o.y);
            path2.lineTo(o.x, o.y);
            path2.close();
        } else {
            shadow1.setBounds((int) (a.x), (int) (a.y - viewHeight), (int) (a.x + shadowLength), (int) (a.y));
            shadow2.setBounds((int) a.x, (int) (a.y), (int) (a.x + shadowLength), (int) (a.y + viewWidth));
            mDegree1 = -(float) Math.toDegrees(Math.acos(cos0));
            mDegree2 = mDegree1 + 90;
            path1 = new Path();
            path1.moveTo(a.x, a.y);
            path1.lineTo(a.x + 7, a.y + 7);
            path1.quadTo(m.x, m.y, m.x + rPathAShadowDis, m.y);
            path1.lineTo(m.x, m.y);
            path1.close();
            path2 = new Path();
            path2.moveTo(a.x, a.y);
            path2.lineTo(a.x + 7, a.y + 7);
            path2.quadTo(o.x, o.y, o.x + rPathAShadowDis, o.y);
            path2.lineTo(o.x, o.y);
            path2.close();
        }
        canvas.clipPath(path1);
        canvas.rotate(mDegree1, a.x, a.y);
        shadow1.draw(canvas);
        canvas.restore();
        canvas.save();
        canvas.clipPath(path2);
        canvas.rotate(mDegree2, a.x, a.y);
        shadow2.draw(canvas);
        canvas.restore();
    }

    private void drawPathBShadow(Canvas canvas, Path pathA) {
        canvas.save();
        canvas.clipPath(pathA);//裁剪出A区域
        canvas.clipPath(getPathC(pathA), Region.Op.UNION);//裁剪出A和C区域的全集
        canvas.clipPath(getPathB(pathA), Region.Op.REVERSE_DIFFERENCE);//裁剪出B区域中不同于与AC区域的部分
        int[] gradientColors = new int[]{deepColor, lightColor};
        int deepOffset = 0;
        int lightOffset = 0;
        float aTof = (float) Math.hypot((a.x - f.x), (a.y - f.y));
        float viewDiagonalLength = (float) Math.hypot(viewWidth, viewHeight);
        int left;
        int right;
        int top = (int) c.y;
        int bottom = (int) (viewDiagonalLength + c.y);
        GradientDrawable gradientDrawable;
        if (turnPageMode == TurnPageMode.MODE_RIGHT_TOP) {
            gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, gradientColors);
            gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
            left = (int) (c.x - deepOffset);//c点位于左上角
            right = (int) (c.x + aTof / 4 + lightOffset);
        } else {
            //从右向左线性渐变
            gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, gradientColors);
            gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
            left = (int) (c.x - aTof / 4 - lightOffset);
            right = (int) (c.x + deepOffset);
        }
        gradientDrawable.setBounds(left, top, right, bottom);
        float rotateDegrees = (float) Math.toDegrees(Math.atan2(e.x - f.x, h.y - f.y));
        canvas.rotate(rotateDegrees, c.x, c.y);
        gradientDrawable.draw(canvas);
        canvas.restore();
    }
}
