package com.ideabytes.scioty;

/**
 * Created by ideabytes on 2/26/16.
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.Shape;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Scroller;

import com.ideabytes.scioty.utility.UtilityMethods;

public class HorizontalPagerWithPageControl  extends ViewGroup {
    private static final int ANIMATION_SCREEN_SET_DURATION_MILLIS = 500;
    private static final int FRACTION_OF_SCREEN_WIDTH_FOR_SWIPE = 4;
    private static final int INVALID_SCREEN = -1;

    private static final int SNAP_VELOCITY_DIP_PER_SECOND = 10;
    private static final int VELOCITY_UNIT_PIXELS_PER_SECOND = 100;

    private static final int TOUCH_STATE_REST = 0;
    private static final int TOUCH_STATE_HORIZONTAL_SCROLLING = 1;
    private static final int TOUCH_STATE_VERTICAL_SCROLLING = -1;
    private static final String ACTIVE_OVAL_COLOR = "#1E90FF";
    private static final String INACTIVE_OVAL_COLOR = "#E2E2E2";

    private int mCurrentScreen;
    private int mDensityAdjustedSnapVelocity;
    private boolean mFirstLayout = true;
    private float mLastMotionX;
    private float mLastMotionY;
    private OnScreenSwitchListener mOnScreenSwitchListener;
    private int mMaximumVelocity;
    private int mNextScreen = INVALID_SCREEN;
    private Scroller mScroller;
    private int mTouchSlop;
    private int mTouchState = TOUCH_STATE_REST;
    private VelocityTracker mVelocityTracker;
    private int mLastSeenLayoutWidth = -1;
    // Active and inactive draw
    private Drawable activeDrawable;
    private Drawable inactiveDrawable;
    // The size for the drawables
    private float mIndicatorSize;

    public HorizontalPagerWithPageControl(final Context context) {
        super(context);
        init();
    }

    /*
     *  Constructor that is called when inflating a view from XML.
     *         This is called when a view is being constructed from an XML file,
     *         supplying attributes that were specified in the XML file. This
     *         version uses a default style of 0, so the only attribute values
     *         applied are those in the Context's Theme and the given
     *         AttributeSet. The method onFinishInflate() will be called after
     *         all children have been added.
     *
     * @param context
     *            The Context the view is running in, through which it can
     *            access the current theme, resources, etc.
     * @param attrs
     *            The attributes of the XML tag that is inflating the view.
     * @see #View(Context, AttributeSet, int)
     */
    public HorizontalPagerWithPageControl(final Context context,
                                          final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Sets up the scroller and touch/fling sensitivity parameters for the
     * pager.
     */
    private void init() {

        mScroller = new Scroller(getContext());

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getMetrics(displayMetrics);
        mDensityAdjustedSnapVelocity = (int) (displayMetrics.density * SNAP_VELOCITY_DIP_PER_SECOND);
        final int height = displayMetrics.heightPixels;
        mIndicatorSize = new UtilityMethods().getSizeOfSwipeCircle(height);

        final ViewConfiguration configuration = ViewConfiguration
                .get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();

        // draw the shapes
        makeShapes();
    }

    /**
     * method overridden from the super class
     */
    @Override
    protected void onMeasure(final int widthMeasureSpec,
                             final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException(
                    "ViewSwitcher can only be used in EXACTLY mode.");
        }

        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException(
                    "ViewSwitcher can only be used in EXACTLY mode.");
        }

        // The children are given the same width and height as the workspace
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
        }

        if (mFirstLayout) {
            scrollTo(mCurrentScreen * width, 0);
            mFirstLayout = false;
        }

        else if (width != mLastSeenLayoutWidth) { // Width has changed
            // Calculate the density-dependent snap velocity in pixels

            final Display display = ((WindowManager) getContext().getSystemService(
                    Context.WINDOW_SERVICE)).getDefaultDisplay();
            @SuppressWarnings("deprecation")
            int displayWidth = display.getWidth();

            mNextScreen = Math.max(0,
                    Math.min(getCurrentScreen(), getChildCount() - 1));
            final int currentScreeValueX = mNextScreen * displayWidth;
            final int currentScreen = currentScreeValueX - getScrollX();

            mScroller.startScroll(getScrollX(), 0, currentScreen, 0, 0);
        }
        mLastSeenLayoutWidth = width;
    }

    /**
     * method overridden from the super class
     */
    @Override
    protected void onLayout(final boolean changed, final int l, final int t,
                            final int r, final int b) {
        int childLeft = 0;
        final int count = getChildCount();

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != View.GONE) {
                final int childWidth = child.getMeasuredWidth();
                child.layout(childLeft, 0, childLeft + childWidth,
                        child.getMeasuredHeight());
                childLeft += childWidth;
            }
        }
    }

    /**
     * method overridden from the super class
     */
    @Override
    public boolean onInterceptTouchEvent(final MotionEvent motionEvent) {

        final int action = motionEvent.getAction();
        boolean intercept = false;

        switch (action) {
            case MotionEvent.ACTION_MOVE:

                if (mTouchState == TOUCH_STATE_HORIZONTAL_SCROLLING) {

                    intercept = true;
                } else if (mTouchState == TOUCH_STATE_VERTICAL_SCROLLING) {

                    intercept = false;
                } else {

                    final float motionValueX = motionEvent.getX();
                    final int motionXDiffrence = (int) Math.abs(motionValueX - mLastMotionX);
                    boolean xMotionValue = motionXDiffrence > mTouchSlop;

                    if (xMotionValue) {
                        // Scroll if the user moved far enough along the X axis
                        mTouchState = TOUCH_STATE_HORIZONTAL_SCROLLING;
                        mLastMotionX = motionValueX;
                    }

                    final float motionValueY = motionEvent.getY();
                    final int motionYDiffrenceValue = (int) Math.abs(motionValueY - mLastMotionY);
                    boolean yMotionValue = motionYDiffrenceValue > mTouchSlop;

                    if (yMotionValue) {
                        mTouchState = TOUCH_STATE_VERTICAL_SCROLLING;
                    }
                }

                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mTouchState = TOUCH_STATE_REST;
                break;
            case MotionEvent.ACTION_DOWN:

                mLastMotionY = motionEvent.getY();
                mLastMotionX = motionEvent.getX();
                break;
            default:
                break;
        }

        return intercept;
    }

    /**
     * method overridden from the super class
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(final MotionEvent ev) {

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);

        final int action = ev.getAction();
        final float motionValueX = ev.getX();

        switch (action) {
            case MotionEvent.ACTION_DOWN:

                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }

                mLastMotionX = motionValueX;

                if (mScroller.isFinished()) {
                    mTouchState = TOUCH_STATE_REST;
                } else {
                    mTouchState = TOUCH_STATE_HORIZONTAL_SCROLLING;
                }

                break;
            case MotionEvent.ACTION_MOVE:
                final int xDiff = (int) Math.abs(motionValueX - mLastMotionX);
                boolean xMoved = xDiff > mTouchSlop;

                if (xMoved) {
                    // Scroll if the user moved far enough along the X axis
                    mTouchState = TOUCH_STATE_HORIZONTAL_SCROLLING;
                }

                if (mTouchState == TOUCH_STATE_HORIZONTAL_SCROLLING) {
                    // Scroll to follow the motion event
                    final int deltaX = (int) (mLastMotionX - motionValueX);
                    mLastMotionX = motionValueX;
                    final int scrollX = getScrollX();

                    if (deltaX < 0) {
                        if (scrollX > 0) {
                            scrollBy(Math.max(-scrollX, deltaX), 0);
                        }
                    } else if (deltaX > 0) {
                        final int availableToScroll = getChildAt(
                                getChildCount() - 1).getRight()
                                - scrollX - getWidth();

                        if (availableToScroll > 0) {
                            scrollBy(Math.min(availableToScroll, deltaX), 0);
                        }
                    }
                }

                break;

            case MotionEvent.ACTION_UP:
                if (mTouchState == TOUCH_STATE_HORIZONTAL_SCROLLING) {
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(
                            VELOCITY_UNIT_PIXELS_PER_SECOND, mMaximumVelocity);
                    int velocityX = (int) velocityTracker.getXVelocity();

                    if (velocityX > mDensityAdjustedSnapVelocity
                            && mCurrentScreen > 0) {
                        // Fling hard enough to move left
                        snapToScreen(mCurrentScreen - 1);
                    } else if (velocityX < -mDensityAdjustedSnapVelocity
                            && mCurrentScreen < getChildCount() - 1) {
                        // Fling hard enough to move right
                        snapToScreen(mCurrentScreen + 1);
                    } else {
                        snapToDestination();
                    }

                    if (mVelocityTracker != null) {
                        mVelocityTracker.recycle();
                        mVelocityTracker = null;
                    }
                }

                mTouchState = TOUCH_STATE_REST;

                break;
            case MotionEvent.ACTION_CANCEL:
                mTouchState = TOUCH_STATE_REST;
                break;
            default:
                break;
        }

        return true;
    }

    /**
     * method overridden from the super class
     */
    @Override
    public void computeScroll() {

        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        } else if (mNextScreen != INVALID_SCREEN) {
            mCurrentScreen = Math.max(0,
                    Math.min(mNextScreen, getChildCount() - 1));

            // Notify observer about screen change
            if (mOnScreenSwitchListener != null) {
                mOnScreenSwitchListener.onScreenSwitched(mCurrentScreen);
            }

            mNextScreen = INVALID_SCREEN;
        }
    }

    /**
     * Returns the index of the currently displayed screen.
     *
     * @return int
     */
    public int getCurrentScreen() {
        return mCurrentScreen;
    }

    /**
     *
     * @param currentScreen
     *            The new screen.
     * @param animate
     *            True to smoothly scroll to the screen, false to snap instantly
     */
    public void setCurrentScreen(final int currentScreen, final boolean animate) {
        mCurrentScreen = Math.max(0,
                Math.min(currentScreen, getChildCount() - 1));
        if (animate) {
            snapToScreen(currentScreen, ANIMATION_SCREEN_SET_DURATION_MILLIS);
        } else {
            scrollTo(mCurrentScreen * getWidth(), 0);
        }
        invalidate();
    }

    /**
     * @param onScreenSwitchListener
     *            The listener for switch events.
     */
    public void setOnScreenSwitchListener(
            final OnScreenSwitchListener onScreenSwitchListener) {
        mOnScreenSwitchListener = onScreenSwitchListener;
    }

    /**
     * Snaps to the screen we think the user wants (the current screen for very
     * small movements; the next/prev screen for bigger movements).
     */
    private void snapToDestination() {

        final int screenWidth = getWidth();
        final int scrollX = getScrollX();
        int currentScreen = mCurrentScreen;
        final int deltaX = scrollX - (screenWidth * mCurrentScreen);

        if ((deltaX < 0)
                && mCurrentScreen != 0
                && ((screenWidth / FRACTION_OF_SCREEN_WIDTH_FOR_SWIPE) < -deltaX)) {
            currentScreen --;
        } else if ((deltaX > 0)
                && (mCurrentScreen + 1 != getChildCount())
                && ((screenWidth / FRACTION_OF_SCREEN_WIDTH_FOR_SWIPE) < deltaX)) {
            currentScreen ++;
        }

        snapToScreen(currentScreen);
    }

    /**
     * Snap to a specific screen, animating automatically for a
     *         duration proportional to the distance left to scroll.
     *
     * @param currentScreen
     *            Screen to snap to
     */
    private void snapToScreen(final int currentScreen) {
        snapToScreen(currentScreen, -1);
    }

    /**
     * Snaps to a specific screen, animating for a specific amount
     *         of time to get there.
     *
     * @param currentScreen
     *            Screen to snap to
     * @param duration
     *            -1 to automatically time it based on scroll distance; a
     *            positive number to make the scroll take an exact duration.
     */
    private void snapToScreen(final int currentScreen, final int duration) {

        mNextScreen = Math.max(0, Math.min(currentScreen, getChildCount() - 1));
        final int nextScreenWidth = mNextScreen * getWidth();
        final int currentScreenWidth = nextScreenWidth - getScrollX();

        if (duration < 0) {
            mScroller
                    .startScroll(
                            getScrollX(),
                            0,
                            currentScreenWidth,
                            0,
                            (int) (Math.abs(currentScreenWidth) / (float) getWidth() * ANIMATION_SCREEN_SET_DURATION_MILLIS));
        } else {
            mScroller.startScroll(getScrollX(), 0, currentScreenWidth, 0, duration);
        }

        // sets the drawables when the user swipes
        setActiveInactiveDrawables(currentScreen);
        // redraw screen
        invalidate();
    }

    /**
     * @author ajay Allways called when the user swipes to another view. Gets
     *         the current view an sets the active drawable / shape to the
     *         curretn view. (in the page control)
     */
    @SuppressWarnings("deprecation")
    public void setActiveInactiveDrawables(final int currentScreen) {

        // Getting the Linear Layout where the page control drawables are inside
        LinearLayout linLayout = (LinearLayout) ((ViewGroup) this.getParent())
                .getChildAt(((LinearLayout) this.getParent()).getChildCount() - 1);
        // get every imageview and set the one of the current screen to active
        for (int i = 0; i < this.getChildCount(); i++) {
            ImageView imgView = (ImageView) linLayout.getChildAt(i);
            if (i == currentScreen) {
                imgView.setBackgroundDrawable(activeDrawable);
            } else {
                imgView.setBackgroundDrawable(inactiveDrawable);
            }
        }
    }

    /**
     * Listener for the event that the HorizontalPager switches to
     *         a new view.
     */
    public static interface OnScreenSwitchListener {
        /**
         * Notifies listeners about the new screen. Runs after the animation
         * completed.
         *
         * @param screen
         *
         */
        void onScreenSwitched(int screen);
    }

    /**
     * Builds the active and inactive shapes / drawables for the
     *         page control
     */
    private void makeShapes() {

        activeDrawable = new ShapeDrawable();
        inactiveDrawable = new ShapeDrawable();
        activeDrawable.setBounds(1, 1, (int) mIndicatorSize,
                (int) mIndicatorSize);
        inactiveDrawable.setBounds(1, 1, (int) mIndicatorSize,
                (int) mIndicatorSize);

        int bubbleColors[] = new int[2];
        bubbleColors[0] = android.R.attr.textColorSecondary;
        bubbleColors[1] = android.R.attr.textColorSecondaryInverse;

        Shape ovelShape = new OvalShape();
        ovelShape.resize(mIndicatorSize, mIndicatorSize);

        Shape selectedOvalShape = new OvalShape();
        selectedOvalShape.resize(mIndicatorSize, mIndicatorSize);

        ((ShapeDrawable) activeDrawable).getPaint().setColor(
                Color.parseColor(ACTIVE_OVAL_COLOR));
        ((ShapeDrawable) inactiveDrawable).getPaint().setColor(
                Color.parseColor(INACTIVE_OVAL_COLOR));

        ((ShapeDrawable) activeDrawable).setShape(ovelShape);
        ((ShapeDrawable) inactiveDrawable).setShape(selectedOvalShape);
    }

    /**
     * Called by the Activity when all Views are added to the
     *         horizontal pager to
     * @param layout
     */
    public void addPagerControl(LinearLayout layout) {
        initPageControl(layout);
    }

    private void initPageControl(LinearLayout layout) {
        setPageCount(this.getChildCount(), layout);
    }

    /**
     * Initializes the page control layout at be bottom of the news
     *         view. Draws all page control shapes an set the active shape to
     *         the first view
     * @param pageCount
     *            the cout of the pages the user can swipe to
     * @param linearLayout
     *            ,pagecount
     * @return linearlayout
     */
    private LinearLayout setPageCount(int pageCount, LinearLayout linearLayout) {

        for (int i = 0; i < pageCount; i++) {

            final ImageView imageView = new ImageView(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    (int) mIndicatorSize, (int) mIndicatorSize);
            params.rightMargin = 10;
            imageView.setLayoutParams(params);
            imageView.setScaleType(ImageView.ScaleType.CENTER);
            imageView.setBackground(inactiveDrawable);

            if (i == 0) {
                imageView.setBackground(activeDrawable);
            }
            linearLayout.addView(imageView);
        }
        return linearLayout;
    }


}
