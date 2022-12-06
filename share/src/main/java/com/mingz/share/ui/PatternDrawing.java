package com.mingz.share.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.mingz.share.R;

public class PatternDrawing extends ViewGroup {
    private final Paint paint = new Paint();
    private final Path path = new Path();
    // 是否允许绘制图案
    private boolean allowDraw = true;
    // 所有图案绘制结点
    private final Rect[] pointGroup;
    // 记录各图案绘制结点是否被选中
    private final boolean[] isSelected;
    // 依序选择的图案绘制结点
    private int[] pattern = new int[0];
    // 不在绘制结点中心点区域的最后一个连接线上的点
    private Point lastPoint = null;
    // 回调
    @Nullable
    private Callback callback = null;

    @ColorInt
    private final int lineColor; // 连接线颜色
    // 布局、绘制相关参数
    private final int rowAndColCount; // 行、列绘制结点的个数
    private int sideLen = 0; // 图案区域边长
    private int pointSideLen = 0; // 绘制结点的边长
    private int pointMargin = 0; // 绘制结点之间的间距

    public PatternDrawing(Context context) {
        this(context, null);
    }

    public PatternDrawing(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PatternDrawing(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public PatternDrawing(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        final int commonColor;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PatternDrawing,
                defStyleAttr, defStyleRes);
        try {
            commonColor = typedArray.getColor(R.styleable.PatternDrawing_commonColor, Color.CYAN);
            lineColor = typedArray.getColor(R.styleable.PatternDrawing_lightColor, Color.BLUE);
        } finally {
            typedArray.recycle();
        }
        // 添加绘制结点
        rowAndColCount = 3;
        final int count = rowAndColCount * rowAndColCount;
        pointGroup = new Rect[count];
        isSelected = new boolean[count];
        for (int i = 0; i < count; i++) {
            addView(new PointView(context, commonColor, lineColor));
            pointGroup[i] = new Rect();
            isSelected[i] = false;
        }
        // 允许绘制（绘制连接线）
        setWillNotDraw(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 确定组件尺寸及图案区域边长
        int wSize = MeasureSpec.getSize(widthMeasureSpec);
        int wMode = MeasureSpec.getMode(widthMeasureSpec);
        int hSize = MeasureSpec.getSize(heightMeasureSpec);
        int hMode = MeasureSpec.getMode(heightMeasureSpec);
        boolean useWSize = true; // 若是，则使用宽度作为边长，否则使用高度作为边长
        switch (wMode) {
            case MeasureSpec.EXACTLY:
                switch (hMode) {
                    case MeasureSpec.EXACTLY:
                        // 若较高，取宽度作为边长，否则取高度作为边长
                        useWSize = hSize > wSize;
                        break;
                    case MeasureSpec.AT_MOST:
                        if (wSize > hSize) { // 高度不能达到宽度的大小，取高度最大值作为边长
                            heightMeasureSpec = MeasureSpec.makeMeasureSpec(hSize, MeasureSpec.EXACTLY);
                            useWSize = false;
                        } else { // 高度可以达到宽度的大小，取宽度作为边长
                            heightMeasureSpec = MeasureSpec.makeMeasureSpec(wSize, MeasureSpec.EXACTLY);
                        }
                        break;
                    case MeasureSpec.UNSPECIFIED: // 高度取宽度的大小，宽度作为边长
                        heightMeasureSpec = MeasureSpec.makeMeasureSpec(wSize, MeasureSpec.EXACTLY);
                }
                break;
            case MeasureSpec.AT_MOST:
                switch (hMode) {
                    case MeasureSpec.EXACTLY:
                        if (hSize > wSize) { // 宽度不能达到高度的大小，取宽度最大值作为边长
                            widthMeasureSpec = MeasureSpec.makeMeasureSpec(wSize, MeasureSpec.EXACTLY);
                        } else { // 宽度可以达到高度的大小，取高度作为边长
                            widthMeasureSpec = MeasureSpec.makeMeasureSpec(hSize, MeasureSpec.EXACTLY);
                            useWSize = false;
                        }
                        break;
                    case MeasureSpec.AT_MOST:
                        if (hSize > wSize) { // 高度的最大值更大，取宽度的最大值作为边长
                            widthMeasureSpec = MeasureSpec.makeMeasureSpec(wSize, MeasureSpec.EXACTLY);
                            heightMeasureSpec = MeasureSpec.makeMeasureSpec(wSize, MeasureSpec.EXACTLY);
                        } else { // 宽度的最大值更大，取高度的最大值作为边长
                            widthMeasureSpec = MeasureSpec.makeMeasureSpec(hSize, MeasureSpec.EXACTLY);
                            heightMeasureSpec = MeasureSpec.makeMeasureSpec(hSize, MeasureSpec.EXACTLY);
                            useWSize = false;
                        }
                        break;
                    case MeasureSpec.UNSPECIFIED: // 高度取宽度的最大值，宽度最大值作为边长
                        widthMeasureSpec = MeasureSpec.makeMeasureSpec(wSize, MeasureSpec.EXACTLY);
                        heightMeasureSpec = MeasureSpec.makeMeasureSpec(wSize, MeasureSpec.EXACTLY);
                        break;
                }
                break;
            case MeasureSpec.UNSPECIFIED:
                switch (hMode) {
                    case MeasureSpec.EXACTLY: // 宽度取高度的大小，高度作为边长
                        widthMeasureSpec = MeasureSpec.makeMeasureSpec(hSize, MeasureSpec.EXACTLY);
                        useWSize = false;
                        break;
                    case MeasureSpec.AT_MOST: // 宽度取高度的最大值，高度最大值作为边长
                        widthMeasureSpec = MeasureSpec.makeMeasureSpec(hSize, MeasureSpec.EXACTLY);
                        heightMeasureSpec = MeasureSpec.makeMeasureSpec(hSize, MeasureSpec.EXACTLY);
                        useWSize = false;
                        break;
                    case MeasureSpec.UNSPECIFIED: // 宽高取0，边长也取0
                        widthMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY);
                        heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY);
                        wSize = 0;
                        break;
                }
                break;
        }
        if (useWSize) {
            sideLen = wSize - getPaddingLeft() - getPaddingRight();
        } else {
            sideLen = hSize - getPaddingTop() - getPaddingBottom();
        }
        if (sideLen < 0) {
            sideLen = 0;
        }
        // 计算每个绘制结点的边长（间距取绘制结点边长的一半）
        // 设每行/列有N个结点，区域边长宽度为W，结点间距m，有：W = 2N * m + (N-1) * m = (3N-1) * m
        pointMargin = sideLen / (3 * rowAndColCount - 1);
        pointSideLen = 2 * pointMargin;
        // 测量（给出）绘制结点边长
        final int pointSideLenMeasureSpec = MeasureSpec.makeMeasureSpec(pointSideLen, MeasureSpec.EXACTLY);
        final int count = rowAndColCount * rowAndColCount;
        for (int i = 0; i < count; i++) {
            getChildAt(i).measure(pointSideLenMeasureSpec, pointSideLenMeasureSpec);
        }
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // 计算绘制结点中心点所在的区域
        final int pointSideLenHalf = pointSideLen / 2; // 绘制结点边长的一半
        final int centerSideLenHalf = (int) (pointSideLen * PointView.centerRadiusScale); // 绘制结点中心点长度的一半
        // 中心点的left、top相对于绘制结点的left、top的增长值
        final int leftOrTop = pointSideLenHalf - centerSideLenHalf;
        // 中心点的right、bottom相对于绘制结点的right、bottom的增长值
        final int rightOrBottom = pointSideLenHalf + centerSideLenHalf;
        // 图案绘制区域居中放置
        final int left = (getWidth() - sideLen) / 2;
        int pointLeft = left;
        int pointTop = (getHeight() - sideLen) / 2;
        // 放置绘制结点
        int count = rowAndColCount * rowAndColCount;
        for (int col = 0, i = 0; i < count; i++) {
            getChildAt(i).layout(pointLeft, pointTop, pointLeft + pointSideLen, pointTop + pointSideLen);
            Rect area = pointGroup[i];
            area.set(pointLeft + leftOrTop, pointTop + leftOrTop,
                    pointLeft + rightOrBottom, pointTop + rightOrBottom);
            if (++col < rowAndColCount) { // 当前行未排列完
                pointLeft += (pointSideLen + pointMargin);
            } else { // 排列完一行
                pointLeft = left;
                pointTop += (pointSideLen + pointMargin);
                col = 0; // 置为第一列
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 组合连接线
        path.reset();
        // 添加已选择的结点的中心点
        boolean isStart = true; // 标记起始点
        for (int number : pattern) {
            Rect area = pointGroup[number];
            int x = area.centerX();
            int y = area.centerY();
            if (isStart) {
                isStart = false;
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        // 添加最后一个连接点
        if (lastPoint != null) {
            path.lineTo(lastPoint.x, lastPoint.y);
        }
        // 连接线的宽度取结点中心点宽度的 1/5，则线宽的一半即其中心点一半宽度的 1/5
        final float lineWidthHalf = pointSideLen * PointView.centerRadiusScale / 5.0f;
        // 绘制连接线
        paint.reset();
        paint.setColor(lineColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(lineWidthHalf);
        canvas.drawPath(path, paint);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // 事件不分发至子View，直接由该View处理
        return allowDraw && onTouchEvent(ev);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                // 若按下时在检测区域类，则继续接收后续事件，否则忽略后续事件
                return appendPoint(x, y);
            case MotionEvent.ACTION_MOVE:
                if (appendPoint(x, y)) {
                    lastPoint = null;
                } else {
                    if (lastPoint == null) { // 添加最后一个连接点
                        lastPoint = new Point(x, y);
                    } else { // 更新最后一个连接点
                        lastPoint.x = x;
                        lastPoint.y = y;
                    }
                }
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
            default:
                // 绘制结束
                lastPoint = null; // 移除最后一个连接点
                invalidate();
                // 不再允许绘制
                allowDraw = false;
                // 回调
                if (callback != null) {
                    if (pattern.length >= 4) {
                        // 将选中结点编号依序组合作为密码
                        final StringBuilder password = new StringBuilder();
                        for (int number : pattern) {
                            password.append(number);
                        }
                        callback.onResult(password.toString());
                    } else {
                        callback.onLess();
                    }
                }
                // 忽略后续事件
                return false;
        }
    }

    // 查找并返回触摸点是否处于未记录的绘制结点的中心点区域.
    // 若是，将添加该结点
    private boolean appendPoint(int x, int y) {
        for (int i = 0; i < pointGroup.length; i++) {
            if (isSelected[i]) continue;
            Rect area = pointGroup[i];
            if (area.contains(x, y)) {
                // 因选中而添加该结点
                int[] temp = new int[pattern.length + 1];
                System.arraycopy(pattern, 0, temp, 0, pattern.length);
                temp[pattern.length] = i;
                pattern = temp;
                // 设置绘制结点被选择
                ((PointView) getChildAt(i)).setHighlight(true);
                isSelected[i] = true;
                return true;
            }
        }
        return false;
    }

    // 不添加其它子View
    @Override
    public void addView(View child, int index, LayoutParams params) {
        if (child instanceof PointView) {
            super.addView(child, index, params);
        }
    }

    /**
     * 设置事件回调.
     */
    public void setCallback(@Nullable Callback callback) {
        this.callback = callback;
    }

    /**
     * 清除当前绘制，并允许重新绘制.
     */
    public void reDraw() {
        // 允许绘制
        allowDraw = true;
        // 重置选择的结点
        pattern = new int[0];
        // 重置绘制结点选中项
        for (int i = 0; i < isSelected.length; i++) {
            if (isSelected[i]) {
                ((PointView) getChildAt(i)).setHighlight(false);
                isSelected[i] = false;
            }
        }
        invalidate();
    }

    public interface Callback {
        /**
         * 连接的点太少（少于4个）.
         */
        void onLess();

        /**
         * 所绘制图案转化为数值后的结果.<br>
         *
         * 若连接的点太少将不会调用该方法.
         * @param password 转化后的结果
         */
        void onResult(@NonNull String password);
    }

    // 图案绘制结点
    private static class PointView extends View {
        private final Paint paint = new Paint();

        @ColorInt
        private final int commonColor; // 绘制结点未高亮时的颜色
        @ColorInt
        private final int lightColor; // 绘制结点高亮时的颜色
        // 绘制参数
        @ColorInt
        private int color;
        private final float strokeWidthHalf; // 圆形边框宽度的一半
        private static final float centerRadiusScale = 0.25f; // 中心点的半径占宽度的比例

        public PointView(Context context, @ColorInt int commonColor, @ColorInt int lightColor) {
            super(context);
            this.commonColor = commonColor;
            this.lightColor = lightColor;
            strokeWidthHalf = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.0f,
                    context.getResources().getDisplayMetrics()); // 1dp
            color = commonColor;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            int width = getWidth();
            float widthHalf = width / 2.0f;
            paint.reset();
            paint.setAntiAlias(true);
            paint.setColor(color);
            // 绘制圆形边框
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(strokeWidthHalf);
            canvas.drawCircle(widthHalf, widthHalf, widthHalf - strokeWidthHalf, paint);
            // 绘制中心点
            paint.setStyle(Paint.Style.FILL);
            float centerRadius = width * centerRadiusScale;
            canvas.drawCircle(widthHalf, widthHalf, centerRadius, paint);
        }

        /**
         * 设置是否高亮.
         */
        private void setHighlight(boolean highlight) {
            if (highlight) {
                color = lightColor;
            } else {
                color = commonColor;
            }
            invalidate();
        }
    }
}
