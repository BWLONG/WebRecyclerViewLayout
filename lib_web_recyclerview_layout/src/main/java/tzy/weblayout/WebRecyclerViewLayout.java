package tzy.weblayout;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.NestedScrollingParent2;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ScrollingView;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.WebView;
import android.widget.OverScroller;


public class WebRecyclerViewLayout extends ViewGroup implements NestedScrollingParent2, ScrollingView {
    private final NestedScrollingParentHelper mParentHelper;

    public WebRecyclerViewLayout(Context context) {
        this(context, null);
    }

    public WebRecyclerViewLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WebRecyclerViewLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initScrollView();
        mParentHelper = new NestedScrollingParentHelper(this);

    }

    private void initScrollView() {
        mScroller = new OverScroller(getContext());
        setFocusable(true);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        setWillNotDraw(false);
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
    }

    private WebView mWebView;
    private View mBottomView;

    private void ensureChild() {
        if (getChildCount() != 2)
            throw new IllegalStateException("头放置WebView，屁股放置RecyclerView，请按照规则来");
        mWebView = (WebView) getChildAt(0);
        mBottomView = getChildAt(1);
        if (mWebView == null || mBottomView == null) {
            throw new IllegalStateException("头放置WebView，屁股放置RecyclerView，请按照规则来");
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        ensureChild();
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);

//            measureChildWithMarginsUnspecified(child, parentWidthMeasureSpec, widthUsed, parentHeightMeasureSpec, heightUsed);
        measureChild(mWebView, widthMeasureSpec, MeasureSpec.makeMeasureSpec(parentHeight, MeasureSpec.AT_MOST));
        measureChild(mBottomView, widthMeasureSpec, MeasureSpec.makeMeasureSpec(parentHeight, MeasureSpec.AT_MOST));


        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int webViewLeft = getPaddingLeft();
        final int webViewTop = getPaddingTop();
        mWebView.layout(webViewLeft, webViewTop, webViewLeft + mWebView.getMeasuredWidth(), webViewTop + mWebView.getMeasuredHeight());

        final int recyclerViewLeft = getPaddingLeft();
        final int recyclerViewTop = mWebView.getBottom();
        mBottomView.layout(recyclerViewLeft, recyclerViewTop, recyclerViewLeft + mBottomView.getMeasuredWidth(), recyclerViewTop + mBottomView.getMeasuredHeight());

    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return false;
    }

    RecyclerView mNestedRecyclerView;

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        if (consumed) {
            mNestedRecyclerView = (RecyclerView) target;
//            Log.i("@@", "@@@@@@@:onNestedFling");
        }
        return false;
    }

    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @Nullable int[] consumed, int type) {
//        dispatchNestedPreScroll(dx, dy, consumed, null, type);

//        consumed[1] = dy;
//        Log.i("##", "@@@@@@@:onNestedPreScroll:" + dy + ",consumed:" + consumed[1]);


        int unconsumedY = dy;
        if (unconsumedY > 0) {

            if (mWebView.canScrollVertically(1)) {
                mWebView.scrollBy(0, unconsumedY);

                consumed[1] = unconsumedY;

            }

            unconsumedY -= consumed[1];
            if (unconsumedY > 0) {
                final int oldScrollY = getScrollY();
                scrollBy(0, unconsumedY);
                consumed[1] += getScrollY() - oldScrollY;
            }


        }
    }

    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        int unconsumedY = dyUnconsumed;
        if (unconsumedY < 0) {
            final int oldScrollY = getScrollY();
            if (oldScrollY > 0) {
                scrollBy(0, unconsumedY);
            }
            unconsumedY -= getScrollY() - oldScrollY;
            if (unconsumedY < 0 && mWebView.canScrollVertically(-1)) {
                mWebView.scrollBy(0, dyUnconsumed);

            }


        }


//        super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type);
    }

    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int axes, int type) {
        return (axes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes, int type) {
        mParentHelper.onNestedScrollAccepted(child, target, axes, type);
    }

    @Override
    public void onStopNestedScroll(@NonNull View target, int type) {
        mParentHelper.onStopNestedScroll(target, type);
        if (type == ViewCompat.TYPE_NON_TOUCH) {
            Log.i("@@", "@@@@@@@:onStopNestedScroll");

            mNestedRecyclerView = null;
        }
    }

    @Override
    public int getNestedScrollAxes() {
        return mParentHelper.getNestedScrollAxes();
    }


    int getScrollRange() {
        int scrollRange = 0;
        if (getChildCount() > 0) {
            final int height = mWebView.getHeight() + mBottomView.getHeight();
            scrollRange = Math.max(0,
                    height - (getHeight() - getPaddingBottom() - getPaddingTop()));
        }
        return scrollRange;
    }


    private static final int INVALID_POINTER = -1;

    private boolean mIsBeingDragged = false;
    private VelocityTracker mVelocityTracker;
    private int mTouchSlop;
    private int mMinimumVelocity;
    private int mMaximumVelocity;
    private int mActivePointerId = INVALID_POINTER;
    private OverScroller mScroller;
    private int mLastMotionY;
    private int mLastScrollerY;


    private boolean inWebViewChild(int x, int y) {
        if (getChildCount() > 0) {
            final int scrollY = getScrollY();
            final View child = mWebView;
            return !(y < child.getTop() - scrollY
                    || y >= child.getBottom() - scrollY
                    || x < child.getLeft()
                    || x >= child.getRight());
        }
        return false;
    }

    private void initOrResetVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        } else {
            mVelocityTracker.clear();
        }
    }

    private void initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }


    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        if (disallowIntercept) {
            recycleVelocityTracker();
        }
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    private boolean stopNestedChildScroll() {
        if (mNestedRecyclerView != null) {
            mNestedRecyclerView.stopScroll();
            mNestedRecyclerView = null;
            return true;
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        /*
         * This method JUST determines whether we want to intercept the motion.
         * If we return true, onMotionEvent will be called and we do the actual
         * scrolling there.
         */

        /*
         * Shortcut the most recurring case: the user is in the dragging
         * state and he is moving his finger.  We want to intercept this
         * motion.
         */
        final int action = ev.getAction();
        if ((action == MotionEvent.ACTION_MOVE) && (mIsBeingDragged)) {
            return true;
        }

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_MOVE: {
                /*
                 * mIsBeingDragged == false, otherwise the shortcut would have caught it. Check
                 * whether the user has moved far enough from his original down touch.
                 */

                /*
                 * Locally do absolute value. mLastMotionY is set to the y value
                 * of the down event.
                 */
                final int activePointerId = mActivePointerId;
                if (activePointerId == INVALID_POINTER) {
                    // If we don't have a valid id, the touch down wasn't on content.
                    break;
                }

                final int pointerIndex = ev.findPointerIndex(activePointerId);
                if (pointerIndex == -1) {

                    break;
                }

                final int y = (int) ev.getY(pointerIndex);
                final int yDiff = Math.abs(y - mLastMotionY);
                if (yDiff > mTouchSlop
                        && (getNestedScrollAxes() & ViewCompat.SCROLL_AXIS_VERTICAL) == 0) {
                    mIsBeingDragged = true;
                    mLastMotionY = y;
                    initVelocityTrackerIfNotExists();
                    mVelocityTracker.addMovement(ev);
                    final ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                }
                break;
            }

            case MotionEvent.ACTION_DOWN: {
                final int y = (int) ev.getY();
//                if (!inWebViewChild((int) ev.getX(), y)) {
//                    mIsBeingDragged = false;
//                    recycleVelocityTracker();
//                    break;
//                }
                stopNestedChildScroll();

                /*
                 * Remember location of down touch.
                 * ACTION_DOWN always refers to pointer index 0.
                 */
                mLastMotionY = y;
                mActivePointerId = ev.getPointerId(0);

                initOrResetVelocityTracker();
                mVelocityTracker.addMovement(ev);
                /*
                 * If being flinged and user touches the screen, initiate drag;
                 * otherwise don't. mScroller.isFinished should be false when
                 * being flinged. We need to call computeScrollOffset() first so that
                 * isFinished() is correct.
                 */
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }

//                mScroller.computeScrollOffset();
                mIsBeingDragged = false;
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                /* Release the drag */
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                recycleVelocityTracker();
                if (mScroller.springBack(getScrollX(), getScrollY(), 0, 0, 0, getScrollRange())) {
                    ViewCompat.postInvalidateOnAnimation(this);
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
        }

        /*
         * The only time we want to intercept motion events is if we are in the
         * drag mode.
         */
        return mIsBeingDragged;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        initVelocityTrackerIfNotExists();

        MotionEvent vtev = MotionEvent.obtain(ev);

        final int actionMasked = ev.getActionMasked();


        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN: {
                if (getChildCount() == 0) {
                    return false;
                }
                if ((mIsBeingDragged = !mScroller.isFinished())) {
                    final ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                }
                stopNestedChildScroll();

                /*
                 * If being flinged and user touches, stop the fling. isFinished
                 * will be false if being flinged.
                 */
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }

                // Remember where the motion event started
                mLastMotionY = (int) ev.getY();
                mActivePointerId = ev.getPointerId(0);
                break;
            }
            case MotionEvent.ACTION_MOVE:
                final int activePointerIndex = ev.findPointerIndex(mActivePointerId);
                if (activePointerIndex == -1) {
                    break;
                }

                final int y = (int) ev.getY(activePointerIndex);
                int deltaY = mLastMotionY - y;

                if (!mIsBeingDragged && Math.abs(deltaY) > mTouchSlop) {
                    final ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                    mIsBeingDragged = true;
                    if (deltaY > 0) {
                        deltaY -= mTouchSlop;
                    } else {
                        deltaY += mTouchSlop;
                    }
                }
                if (mIsBeingDragged) {
                    // Scroll to follow the motion event
                    mLastMotionY = y;

                    final int oldY = getScrollY();
                    final int range = getScrollRange();
                    final int overscrollMode = getOverScrollMode();
                    boolean canOverscroll = overscrollMode == View.OVER_SCROLL_ALWAYS
                            || (overscrollMode == View.OVER_SCROLL_IF_CONTENT_SCROLLS && range > 0);

                    // Calling overScrollByCompat will call onOverScrolled, which
                    // calls onScrollChanged if applicable.
                    if (overScrollWithWebView(0, deltaY, 0, getScrollY(), 0, range, 0,
                            0, true)) {
                        // Break our velocity if we hit a scroll barrier.
                        mVelocityTracker.clear();
                    }


                }
                break;
            case MotionEvent.ACTION_UP:
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                int initialVelocity = (int) velocityTracker.getYVelocity(mActivePointerId);
                if ((Math.abs(initialVelocity) > mMinimumVelocity)) {
                    fling(-initialVelocity);
                } else if (mScroller.springBack(getScrollX(), getScrollY(), 0, 0, 0,
                        getScrollRange())) {
                    ViewCompat.postInvalidateOnAnimation(this);
                }
                mActivePointerId = INVALID_POINTER;
                endDrag();
                break;
            case MotionEvent.ACTION_CANCEL:
                if (mIsBeingDragged && getChildCount() > 0) {
                    if (mScroller.springBack(getScrollX(), getScrollY(), 0, 0, 0,
                            getScrollRange())) {
                        ViewCompat.postInvalidateOnAnimation(this);
                    }
                }
                mActivePointerId = INVALID_POINTER;
                endDrag();
                break;
            case MotionEvent.ACTION_POINTER_DOWN: {
                final int index = ev.getActionIndex();
                mLastMotionY = (int) ev.getY(index);
                mActivePointerId = ev.getPointerId(index);
                break;
            }
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                mLastMotionY = (int) ev.getY(ev.findPointerIndex(mActivePointerId));
                break;
        }

        if (mVelocityTracker != null) {
            mVelocityTracker.addMovement(vtev);
        }
        vtev.recycle();
        return true;
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = ev.getActionIndex();
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            // TODO: Make this decision more intelligent.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mLastMotionY = (int) ev.getY(newPointerIndex);
            mActivePointerId = ev.getPointerId(newPointerIndex);
            if (mVelocityTracker != null) {
                mVelocityTracker.clear();
            }
        }
    }

    public void fling(int velocityY) {
        if (getChildCount() > 0) {
            mScroller.fling(getScrollX(), getScrollY(), // start
                    0, velocityY, // velocities
                    0, 0, // x
                    Integer.MIN_VALUE, Integer.MAX_VALUE, // y
                    0, 0); // overscroll
            mLastScrollerY = getScrollY();
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private void endDrag() {
        mIsBeingDragged = false;

        recycleVelocityTracker();

    }

    private static int clamp(int n, int my, int child) {
        if (my >= child || n < 0) {
            /* my >= child is this case:
             *                    |--------------- me ---------------|
             *     |------ child ------|
             * or
             *     |--------------- me ---------------|
             *            |------ child ------|
             * or
             *     |--------------- me ---------------|
             *                                  |------ child ------|
             *
             * n < 0 is this case:
             *     |------ me ------|
             *                    |-------- child --------|
             *     |-- mScrollX --|
             */
            return 0;
        }
        if ((my + n) > child) {
            /* this case:
             *                    |------ me ------|
             *     |------ child ------|
             *     |-- mScrollX --|
             */
            return child - my;
        }
        return n;
    }

    @Override
    public void scrollTo(int x, int y) {
        // we rely on the fact the View.scrollBy calls scrollTo.
        if (getChildCount() > 0) {
            View child = getChildAt(0);
            final int childHeight = mWebView.getHeight() + mBottomView.getHeight();
            x = clamp(x, getWidth() - getPaddingRight() - getPaddingLeft(), child.getWidth());
            y = clamp(y, getHeight() - getPaddingBottom() - getPaddingTop(), childHeight);
            if (x != getScrollX() || y != getScrollY()) {
                super.scrollTo(x, y);
            }
        }
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            final int x = mScroller.getCurrX();
            final int y = mScroller.getCurrY();

            int dy = y - mLastScrollerY;


            if (dy != 0) {
                final int range = getScrollRange();
                final int oldScrollY = getScrollY();

                overScrollWithWebView(0, dy, getScrollX(), oldScrollY, 0, range, 0, 0, false);

                final int scrolledDeltaY = getScrollY() - oldScrollY;
                final int unconsumedY = dy - scrolledDeltaY;

            }

            // Finally update the scroll positions and post an invalidation
            mLastScrollerY = y;
            ViewCompat.postInvalidateOnAnimation(this);
        } else {

            mLastScrollerY = 0;
        }
    }

    boolean overScrollWithWebView(int deltaX, int deltaY,
                                  int scrollX, int scrollY,
                                  int scrollRangeX, int scrollRangeY,
                                  int maxOverScrollX, int maxOverScrollY,
                                  boolean isTouchEvent) {
        if (deltaY > 0) {
            if (mWebView.canScrollVertically(1)) {
                mWebView.scrollBy(0, deltaY);
            } else {
                if (overScrollByCompat(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent)) {
                    mScroller.springBack(getScrollX(), getScrollY(), 0, 0, 0, getScrollRange());
                    return true;
                }
            }

        } else if (deltaY < 0) {
            if (overScrollByCompat(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent)) {
                if (mWebView.canScrollVertically(-1)) {
                    mWebView.scrollBy(0, deltaY);
                }
                if (!mWebView.canScrollVertically(-1)) {
                    mScroller.springBack(getScrollX(), getScrollY(), 0, 0, 0, getScrollRange());
                    return true;
                }

            }


        }
        return false;
    }

    boolean overScrollByCompat(int deltaX, int deltaY,
                               int scrollX, int scrollY,
                               int scrollRangeX, int scrollRangeY,
                               int maxOverScrollX, int maxOverScrollY,
                               boolean isTouchEvent) {
        final int overScrollMode = getOverScrollMode();
        final boolean canScrollHorizontal =
                computeHorizontalScrollRange() > computeHorizontalScrollExtent();
        final boolean canScrollVertical =
                computeVerticalScrollRange() > computeVerticalScrollExtent();
        final boolean overScrollHorizontal = overScrollMode == View.OVER_SCROLL_ALWAYS
                || (overScrollMode == View.OVER_SCROLL_IF_CONTENT_SCROLLS && canScrollHorizontal);
        final boolean overScrollVertical = overScrollMode == View.OVER_SCROLL_ALWAYS
                || (overScrollMode == View.OVER_SCROLL_IF_CONTENT_SCROLLS && canScrollVertical);

        int newScrollX = scrollX + deltaX;
        if (!overScrollHorizontal) {
            maxOverScrollX = 0;
        }

        int newScrollY = scrollY + deltaY;
        if (!overScrollVertical) {
            maxOverScrollY = 0;
        }

        // Clamp values if at the limits and record
        final int left = -maxOverScrollX;
        final int right = maxOverScrollX + scrollRangeX;
        final int top = -maxOverScrollY;
        final int bottom = maxOverScrollY + scrollRangeY;

        boolean clampedX = false;
        if (newScrollX > right) {
            newScrollX = right;
            clampedX = true;
        } else if (newScrollX < left) {
            newScrollX = left;
            clampedX = true;
        }

        boolean clampedY = false;
        if (newScrollY > bottom) {
            newScrollY = bottom;
            clampedY = true;
        } else if (newScrollY < top) {
            newScrollY = top;
            clampedY = true;
        }

//        if (clampedY) {
//            mScroller.springBack(newScrollX, newScrollY, 0, 0, 0, getScrollRange());
//        }

        onOverScrolled(newScrollX, newScrollY, clampedX, clampedY);

        return clampedX || clampedY;
    }

    @Override
    protected void onOverScrolled(int scrollX, int scrollY,
                                  boolean clampedX, boolean clampedY) {
        super.scrollTo(scrollX, scrollY);
    }

    @Override
    public int computeVerticalScrollRange() {
        final int count = getChildCount();
        final int contentHeight = getHeight() - getPaddingBottom() - getPaddingTop();
        if (count == 0) {
            return contentHeight;
        }

        int scrollRange = mBottomView.getBottom();
        final int scrollY = getScrollY();
        final int overscrollBottom = Math.max(0, scrollRange - contentHeight);
        if (scrollY < 0) {
            scrollRange -= scrollY;
        } else if (scrollY > overscrollBottom) {
            scrollRange += scrollY - overscrollBottom;
        }

        return scrollRange;
    }

    @Override
    public int computeVerticalScrollOffset() {
        return Math.max(0, super.computeVerticalScrollOffset());
    }

    @Override
    public int computeVerticalScrollExtent() {
        return super.computeVerticalScrollExtent();
    }

    @Override
    public int computeHorizontalScrollRange() {
        return super.computeHorizontalScrollRange();
    }

    @Override
    public int computeHorizontalScrollOffset() {
        return super.computeHorizontalScrollOffset();
    }

    @Override
    public int computeHorizontalScrollExtent() {
        return super.computeHorizontalScrollExtent();
    }

}
