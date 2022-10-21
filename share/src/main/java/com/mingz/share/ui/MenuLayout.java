package com.mingz.share.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import com.mingz.share.R;

import java.util.ArrayList;

/**
 * 弹出菜单布局.<br/>
 *
 * 子View中的第一个不为GONE的View将作为主菜单，其余的将作为菜单项.当点击主菜单时，将弹出或收起菜单条目.<br/>
 *
 * 所有子View垂直排列，主菜单位于最下方，菜单项从上到下依序排列，与主菜单中线对齐.
 */
public class MenuLayout extends ViewGroup {
    // 动画过程为：菜单项中心点 从 主菜单中心点位置 到 菜单展开后菜单项中心点位置 变化，菜单项透明度则是从0到1
    // 动画开始值
    private static final float ANIMATOR_START = 0.0f;
    // 动画结束值
    private static final float ANIMATOR_End = 1.0f;

    private int mainIndex = -1; // 主菜单在子View中的索引
    private final Rect mainRect = new Rect(); // 主菜单所在矩阵
    private int leftLengthOfIncrease = 0; // 为保持中线对齐而在左边追加的尺寸
    // 宽度为MATCH_PARENT的菜单项
    private final ArrayList<View> matchItems = new ArrayList<>();
    private final ValueAnimator animator; // 动画
    private float animatorValue = ANIMATOR_START; // 当前动画值
    private boolean reverse = false; // 动画是否应该反向播放
    private final OnClickListener mainClickListener;

    public MenuLayout(Context context) {
        this(context, null);
    }

    public MenuLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MenuLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MenuLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MenuLayout,
                defStyleAttr, defStyleRes);
        try {
            if (typedArray.getBoolean(R.styleable.MenuLayout_preview, false)) {
                animatorValue = ANIMATOR_End;
            }
        } finally {
            typedArray.recycle();
        }
        animator = ValueAnimator.ofFloat(ANIMATOR_START, ANIMATOR_End);
        animator.setDuration(300);
        animator.addUpdateListener(animation -> {
            animatorValue = (float) animation.getAnimatedValue();
            invalidate();
            requestLayout();
        });
        mainClickListener = v -> {
            if (reverse) {
                animator.reverse();
            } else {
                animator.start();
            }
            reverse = !reverse;
        };
        setChildrenDrawingOrderEnabled(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 该布局需要的最小宽高
        int minWidth = 0;
        int minHeight = 0;
        // 向上传递状态（可能有子View给出MEASURED_STATE_TOO_SMALL状态，向上传递以可能获得更大的尺寸）
        int state = 0;
        boolean findMain = false; // 是否已定位到主菜单
        int mainLeftLength = 0; // 主菜单左半部分长度（含左间距）
        int mainRightLength = 0; // 主菜单右半部分长度（含右间距）
        leftLengthOfIncrease = 0;
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            if (findMain && lp.width == LayoutParams.MATCH_PARENT) { // 宽度为MATCH_PARENT的菜单项
                // 延后测量
                matchItems.add(child);
                // 先将其宽度视为0，以辅助确定中线的位置
                if (lp.leftMargin > mainLeftLength) {
                    int length = lp.leftMargin - mainLeftLength;
                    leftLengthOfIncrease += length;
                    minWidth += length;
                }
                if (lp.rightMargin > mainRightLength) {
                    minWidth += (lp.rightMargin - mainRightLength);
                }
                continue;
            }
            // 当前为主菜单，或是宽度不为MATCH_PARENT的菜单项
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
            int widthHalf = child.getMeasuredWidth() / 2;
            int leftLength = widthHalf + lp.leftMargin; // 当前子View左半部分长度
            int rightLength = widthHalf + lp.rightMargin; // 当前子View右半部分长度
            if (findMain) { // 当前为菜单项
                if (leftLength > mainLeftLength) { // 在与主菜单中线对齐后，左边需要更大一点，因此加上它
                    int length = leftLength - mainLeftLength;
                    leftLengthOfIncrease += length;
                    minWidth += length;
                }
                if (rightLength > mainRightLength) { // 同理，右边需要更大一点
                    minWidth += (rightLength - mainRightLength);
                }
            } else { // 当前为主菜单
                findMain = true;
                mainLeftLength = leftLength;
                mainRightLength = rightLength;
                minWidth = leftLength + rightLength;
                mainIndex = i;
                child.setOnClickListener(mainClickListener);
            }
            minHeight += (child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin); // 累加需要的高度
            state = combineMeasuredStates(state, child.getMeasuredState());
        }
        if (!findMain) { // 没有合适的View作为主菜单
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        // 测量宽度为MATCH_PARENT的菜单项
        count = matchItems.size();
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                View child = matchItems.get(i);
                MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
                // 中线位置
                int midLine = leftLengthOfIncrease + mainLeftLength;
                int parentWidthMeasureSpec = MeasureSpec.makeMeasureSpec(Math.max(
                        // 菜单项需要与主菜单中线对齐，因此MATCH_PARENT解释为全部可见、能够保持中线对齐下的最大宽度
                        0, Math.min(midLine, minWidth - midLine)
                ) * 2 + getPaddingLeft() + getPaddingRight(), MeasureSpec.EXACTLY);
                measureChildWithMargins(child, parentWidthMeasureSpec, 0, heightMeasureSpec, 0);
                minHeight += (child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin); // 累加需要的高度
                state = combineMeasuredStates(state, child.getMeasuredState());
            }
            matchItems.clear();
        }
        // 添加内边距
        minWidth += (getPaddingLeft() + getPaddingRight());
        minHeight += (getPaddingTop() + getPaddingBottom());
        // 检查最小宽高
        minWidth = Math.max(minWidth, getSuggestedMinimumWidth());
        minHeight = Math.max(minHeight, getSuggestedMinimumHeight());
        // 设置该布局尺寸及状态
        setMeasuredDimension(resolveSizeAndState(minWidth, widthMeasureSpec, state), resolveSizeAndState(minHeight,
                heightMeasureSpec, state << MEASURED_HEIGHT_STATE_SHIFT));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();
        int currentBottom = b - getPaddingBottom();
        int mainVerticalMid = 0; // 主菜单垂直中心线位置
        int mainHorizontalMid = 0; // 主菜单水平中心线位置
        int mainIndex = 0; // 主菜单在子View中的索引
        while (mainIndex < count) {
            View main = getChildAt(mainIndex);
            if (main.getVisibility() == GONE) {
                mainIndex++;
                continue;
            }
            int widthHalf = main.getMeasuredWidth() / 2;
            int heightHalf = main.getMeasuredHeight() / 2;
            MarginLayoutParams lp = (MarginLayoutParams) main.getLayoutParams();
            int mainLeft = l + getPaddingLeft() + leftLengthOfIncrease + lp.leftMargin;
            mainVerticalMid = mainLeft + widthHalf;
            int mainRight = mainVerticalMid + widthHalf;
            int mainBottom = currentBottom - lp.bottomMargin;
            mainHorizontalMid = mainBottom - heightHalf;
            int mainTop = mainHorizontalMid - heightHalf;
            // 放置主菜单
            main.layout(mainLeft, mainTop, mainRight, mainBottom);
            mainRect.set(mainLeft, mainTop, mainRight, mainBottom);
            currentBottom = mainTop - lp.topMargin;
            break;
        }
        if (mainIndex < count/*已定位到主菜单*/) {
            for (int i = count - 1; i > mainIndex; i--) { // 逆序遍历菜单项，将菜单项从上到下摆放
                View child = getChildAt(i);
                if (child.getVisibility() == GONE) {
                    continue;
                }
                int widthHalf = child.getMeasuredWidth() / 2;
                int heightHalf = child.getMeasuredHeight() / 2;
                MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
                // 当动画结束时，菜单项水平中心线位置
                int childHorizontalMidWhenEnd = currentBottom - lp.bottomMargin - heightHalf;
                currentBottom = childHorizontalMidWhenEnd - heightHalf - lp.topMargin;
                // 当前动画值下，菜单项水平中心线位置
                int childHorizontalMid = mainHorizontalMid -
                        // 当前动画值下，菜单项水平中心线位置相对主菜单水平中心线位置的偏移值
                        (int) ((mainHorizontalMid - childHorizontalMidWhenEnd) * animatorValue);
                int childLeft = mainVerticalMid - widthHalf;
                int childRight = mainVerticalMid + widthHalf;
                int childBottom = childHorizontalMid + heightHalf;
                int childTop = childHorizontalMid - heightHalf;
                child.layout(childLeft, childTop, childRight, childBottom);
                child.setAlpha(animatorValue);
            }
        }
    }

    // 先绘制菜单项，最后绘制主菜单
    @Override
    protected int getChildDrawingOrder(int childCount, int drawingPosition) {
        if (mainIndex >= 0) {
            if (drawingPosition >= mainIndex && drawingPosition < childCount - 1) {
                return drawingPosition + 1;
            } else {
                return mainIndex;
            }
        }
        return super.getChildDrawingOrder(childCount, drawingPosition);
    }

    @Override
    protected boolean checkLayoutParams(LayoutParams p) {
        return p instanceof MarginLayoutParams;
    }

    // 用于解析margin属性
    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    // 当未通过checkLayoutParams测试时生成
    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    // 用于addView时未设置布局参数的View
    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // 当菜单项未展开时，忽略不在主菜单矩阵范围内的触摸事件
        if (animatorValue == ANIMATOR_START) {
            if (!mainRect.contains((int) ev.getX(), (int) ev.getY())) {
                return true;
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}
