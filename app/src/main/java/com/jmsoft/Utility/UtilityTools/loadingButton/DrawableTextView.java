

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.jmsoft.Utility.UtilityTools.loadingButton;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewOutlineProvider;

import androidx.annotation.Dimension;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatTextView;

import com.jmsoft.R.styleable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;

public class DrawableTextView extends AppCompatTextView {
    private final Drawable[] mDrawables = new Drawable[]{null, null, null, null};
    private final Rect[] mDrawablesBounds = new Rect[4];
    private float mTextWidth;
    private float mTextHeight;
    private boolean firstLayout;
    private int canvasTransX = 0;
    private int canvasTransY = 0;
    private boolean isCenterHorizontal;
    private boolean isCenterVertical;
    private boolean enableCenterDrawables;
    private boolean enableTextInCenter;
    private int radius;

    public DrawableTextView(Context context) {
        super(context);
        this.init(context, (AttributeSet) null);
    }

    public DrawableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context, attrs);
    }

    public DrawableTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        Drawable[] drawables = this.getCompoundDrawablesRelative();
        TypedArray array = context.obtainStyledAttributes(attrs, styleable.DrawableTextView);
        this.enableCenterDrawables = array.getBoolean(styleable.DrawableTextView_enableCenterDrawables, false);
        this.enableTextInCenter = array.getBoolean(styleable.DrawableTextView_enableTextInCenter, false);
        this.radius = array.getDimensionPixelSize(styleable.DrawableTextView_radius, 0);
        if (this.radius > 0) {
            this.setClipToOutline(true);
            this.setOutlineProvider(new RadiusViewOutlineProvider());
        }

        Rect bottomBounds;
        if (drawables[0] != null) {
            bottomBounds = drawables[0].getBounds();
            bottomBounds.right = array.getDimensionPixelSize(styleable.DrawableTextView_drawableStartWidth, drawables[0].getIntrinsicWidth());
            bottomBounds.bottom = array.getDimensionPixelSize(styleable.DrawableTextView_drawableStartHeight, drawables[0].getIntrinsicHeight());
        }

        if (drawables[1] != null) {
            bottomBounds = drawables[1].getBounds();
            bottomBounds.right = array.getDimensionPixelSize(styleable.DrawableTextView_drawableTopWidth, drawables[1].getIntrinsicWidth());
            bottomBounds.bottom = array.getDimensionPixelSize(styleable.DrawableTextView_drawableTopHeight, drawables[1].getIntrinsicHeight());
        }

        if (drawables[2] != null) {
            bottomBounds = drawables[2].getBounds();
            bottomBounds.right = array.getDimensionPixelSize(styleable.DrawableTextView_drawableEndWidth, drawables[2].getIntrinsicWidth());
            bottomBounds.bottom = array.getDimensionPixelSize(styleable.DrawableTextView_drawableEndHeight, drawables[2].getIntrinsicHeight());
        }

        if (drawables[3] != null) {
            bottomBounds = drawables[3].getBounds();
            bottomBounds.right = array.getDimensionPixelSize(styleable.DrawableTextView_drawableBottomWidth, drawables[3].getIntrinsicWidth());
            bottomBounds.bottom = array.getDimensionPixelSize(styleable.DrawableTextView_drawableBottomHeight, drawables[3].getIntrinsicHeight());
        }

        array.recycle();
        this.setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3]);
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (this.enableCenterDrawables) {
            int absoluteGravity = Gravity.getAbsoluteGravity(this.getGravity(), this.getLayoutDirection());
            this.isCenterHorizontal = (absoluteGravity & 7) == 1;
            this.isCenterVertical = (absoluteGravity & 112) == 16;
        }

        if (!this.firstLayout) {
            this.onFirstLayout(left, top, right, bottom);
            this.firstLayout = true;
        }

    }

    protected void onFirstLayout(int left, int top, int right, int bottom) {
        this.measureTextWidth();
        this.measureTextHeight();
    }

    protected void onDraw(Canvas canvas) {
        if (this.enableCenterDrawables && this.isCenterHorizontal | this.isCenterVertical) {
            boolean textNoEmpty = !TextUtils.isEmpty(this.getText());
            int transX = 0;
            int transY = 0;
            Rect bounds;
            int offset;
            if (this.mDrawables[0] != null && this.isCenterHorizontal) {
                bounds = this.mDrawablesBounds[0];
                offset = (int) this.calcOffset(0);
                this.mDrawables[0].setBounds(bounds.left + offset, bounds.top, bounds.right + offset, bounds.bottom);
                if (this.enableTextInCenter && textNoEmpty) {
                    transX -= this.mDrawablesBounds[0].width() + this.getCompoundDrawablePadding() >> 1 + bounds.width()/2;
                }
            }

            if (this.mDrawables[1] != null && this.isCenterVertical) {
                bounds = this.mDrawablesBounds[1];
                offset = (int) this.calcOffset(1);
                this.mDrawables[1].setBounds(bounds.left, bounds.top + offset, bounds.right, bounds.bottom + offset);
                if (this.enableTextInCenter && textNoEmpty) {
                    transY -= this.mDrawablesBounds[1].height() + this.getCompoundDrawablePadding() >> 1;
                }
            }

            if (this.mDrawables[2] != null && this.isCenterHorizontal) {
                bounds = this.mDrawablesBounds[2];
                offset = -((int) this.calcOffset(2));
                this.mDrawables[2].setBounds(bounds.left + offset, bounds.top, bounds.right + offset, bounds.bottom);
                if (this.enableTextInCenter && textNoEmpty) {
                    transX += this.mDrawablesBounds[2].width() + this.getCompoundDrawablePadding() >> 1;
                }
            }

            if (this.mDrawables[3] != null && this.isCenterVertical) {
                bounds = this.mDrawablesBounds[3];
                offset = -((int) this.calcOffset(3));
                this.mDrawables[3].setBounds(bounds.left, bounds.top + offset, bounds.right, bounds.bottom + offset);
                if (this.enableTextInCenter && textNoEmpty) {
                    transY += this.mDrawablesBounds[3].height() + this.getCompoundDrawablePadding() >> 1;
                }
            }

            if (this.enableTextInCenter && textNoEmpty) {
                canvas.translate((float) transX, (float) transY);
                this.canvasTransX = transX;
                this.canvasTransY = transY;
            }
        }

        super.onDraw(canvas);
    }

    public void onDrawForeground(Canvas canvas) {
        canvas.translate((float) (-this.canvasTransX), (float) (-this.canvasTransY));
        super.onDrawForeground(canvas);
    }

    private float calcOffset(int position) {
        switch (position) {
            case 0:
            case 2:
                return ((float) this.getWidth() - ((float) (this.getCompoundPaddingStart() + this.getCompoundPaddingEnd()) + this.getTextWidth())) / 2.0F;
            case 1:
            case 3:
                return ((float) this.getHeight() - ((float) (this.getCompoundPaddingTop() + this.getCompoundPaddingBottom()) + this.getTextHeight())) / 2.0F;
            default:
                return 0.0F;
        }
    }

    protected int getCanvasTransX() {
        return this.canvasTransX;
    }

    protected int getCanvasTransY() {
        return this.canvasTransY;
    }

    protected void measureTextWidth() {
        Rect textBounds = new Rect();
        this.getLineBounds(0, textBounds);
        String text = "";
        if (this.getText() != null && this.getText().length() > 0) {
            text = this.getText().toString();
        } else if (this.getHint() != null && this.getHint().length() > 0) {
            text = this.getHint().toString();
        }

        float width = this.getPaint().measureText(text);
        float maxWidth = (float) textBounds.width();
        this.mTextWidth = !(width <= maxWidth) && maxWidth != 0.0F ? maxWidth : width;
    }

    protected void measureTextHeight() {
        if ((this.getText() == null || this.getText().length() <= 0) && (this.getHint() == null || this.getHint().length() <= 0)) {
            this.mTextHeight = 0.0F;
        } else {
            this.mTextHeight = (float) (this.getLineHeight() * this.getLineCount());
        }

    }

    protected float getTextWidth() {
        return this.mTextWidth;
    }

    public float getTextHeight() {
        return this.mTextHeight;
    }

    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
        this.measureTextWidth();
        this.measureTextHeight();
    }

    public void setDrawable(int position, @Nullable Drawable drawable, @Px int width, @Px int height) {
        this.mDrawables[position] = drawable;
        if (drawable != null) {
            Rect bounds = new Rect();
            if (width == -1 && height == -1) {
                if (drawable.getBounds().width() > 0 && drawable.getBounds().height() > 0) {
                    Rect origin = drawable.getBounds();
                    bounds.set(origin.left, origin.top, origin.right, origin.bottom);
                } else {
                    bounds.set(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                }
            } else {
                bounds.right = width;
                bounds.bottom = height;
            }

            this.mDrawables[position].setBounds(bounds.left, bounds.top, bounds.right, bounds.bottom);
            this.mDrawablesBounds[position] = bounds;
        }

        super.setCompoundDrawables(this.mDrawables[0], this.mDrawables[1], this.mDrawables[2], this.mDrawables[3]);
    }
    public void setDrawable(int position, @Nullable Drawable drawable, @Px int width, @Px int height,int padding) {
        this.mDrawables[position] = drawable;
        if (drawable != null) {
            Rect bounds = new Rect();
            if (width == -1 && height == -1) {
                if (drawable.getBounds().width() > 0 && drawable.getBounds().height() > 0) {
                    Rect origin = drawable.getBounds();
                    bounds.set(origin.left, origin.top, origin.right, origin.bottom);
                } else {
                    bounds.set(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                }
            } else {
                bounds.right = width;
                bounds.bottom = height;
            }

            this.mDrawables[position].setBounds(bounds.left + padding, bounds.top, bounds.right, bounds.bottom);
            this.mDrawablesBounds[position] = bounds;
        }

        super.setCompoundDrawables(this.mDrawables[0], this.mDrawables[1], this.mDrawables[2], this.mDrawables[3]);
    }

    public void setCompoundDrawables(@Nullable Drawable left, @Nullable Drawable top, @Nullable Drawable right, @Nullable Drawable bottom) {
        super.setCompoundDrawables(left, top, right, bottom);
        this.storeDrawables(left, top, right, bottom);
    }

    public void setCompoundDrawablesRelative(@Nullable Drawable start, @Nullable Drawable top, @Nullable Drawable end, @Nullable Drawable bottom) {
        super.setCompoundDrawablesRelative(start, top, end, bottom);
        this.storeDrawables(start, top, end, bottom);
    }

    private void storeDrawables(@Nullable Drawable start, @Nullable Drawable top, @Nullable Drawable end, @Nullable Drawable bottom) {
        if (this.mDrawables != null) {
            if (start != null && start != this.mDrawables[0]) {
                this.mDrawablesBounds[0] = start.copyBounds();
            }

            this.mDrawables[0] = start;
            if (top != null && top != this.mDrawables[1]) {
                this.mDrawablesBounds[1] = top.copyBounds();
            }

            this.mDrawables[1] = top;
            if (end != null && end != this.mDrawables[2]) {
                this.mDrawablesBounds[2] = end.copyBounds();
            }

            this.mDrawables[2] = end;
            if (bottom != null && bottom != this.mDrawables[3]) {
                this.mDrawablesBounds[3] = bottom.copyBounds();
            }

            this.mDrawables[3] = bottom;
        }

    }

    protected Drawable[] copyDrawables(boolean clearOffset) {
        Drawable[] drawables = (Drawable[]) Arrays.copyOf(this.getDrawables(), 4);
        if (clearOffset) {
            this.clearOffset(drawables);
        }

        return drawables;
    }

    private void clearOffset(Drawable... drawables) {
        Drawable[] var2 = drawables;
        int var3 = drawables.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            Drawable drawable = var2[var4];
            if (drawable != null) {
                Rect bounds = drawable.getBounds();
                bounds.offset(-bounds.left, -bounds.top);
            }
        }

    }

    protected int dp2px(float dpValue) {
        float scale = this.getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5F);
    }

    public DrawableTextView setDrawableStart(Drawable drawableStart, @Dimension(unit = 0) int width, @Dimension(unit = 0) int height) {
        this.setDrawable(0, drawableStart, this.dp2px((float) width), this.dp2px((float) height));
        return this;
    }

    public DrawableTextView setDrawableStart(Drawable drawableStart) {
        this.setDrawableStart(drawableStart, -1, -1);
        return this;
    }

    public DrawableTextView setDrawableTop(Drawable drawableTop, @Dimension(unit = 0) int width, @Dimension(unit = 0) int height) {
        this.setDrawable(1, drawableTop, this.dp2px((float) width), this.dp2px((float) height));
        return this;
    }

    public DrawableTextView setDrawableTop(Drawable drawableTop) {
        this.setDrawableTop(drawableTop, -1, -1);
        return this;
    }

    public DrawableTextView setDrawableEnd(Drawable drawableEnd, @Dimension(unit = 0) int width, @Dimension(unit = 0) int height) {
        this.setDrawable(2, drawableEnd, this.dp2px((float) width), this.dp2px((float) height));
        return this;
    }

    public DrawableTextView setDrawableEnd(Drawable drawableEnd) {
        this.setDrawableEnd(drawableEnd, -1, -1);
        return this;
    }

    public DrawableTextView setDrawableBottom(Drawable drawableBottom, @Dimension(unit = 0) int width, @Dimension(unit = 0) int height) {
        this.setDrawable(3, drawableBottom, this.dp2px((float) width), this.dp2px((float) height));
        return this;
    }

    public DrawableTextView setDrawableBottom(Drawable drawableBottom) {
        this.setDrawableBottom(drawableBottom, -1, -1);
        return this;
    }

    public boolean isEnableTextInCenter() {
        return this.enableTextInCenter;
    }

    public DrawableTextView setEnableTextInCenter(boolean enable) {
        this.enableTextInCenter = enable;
        return this;
    }

    public boolean isEnableCenterDrawables() {
        return this.enableCenterDrawables;
    }

    public DrawableTextView setEnableCenterDrawables(boolean enable) {
        if (this.enableCenterDrawables) {
            this.clearOffset(this.mDrawables);
        }

        this.enableCenterDrawables = enable;
        return this;
    }

    public Drawable[] getDrawables() {
        return this.mDrawables;
    }

    @RequiresApi(
            api = 21
    )
    public DrawableTextView setRadiusDP(@Dimension(unit = 0) int dp) {
        return this.setRadius(this.dp2px((float) dp));
    }

    public int getRadius() {
        return this.radius;
    }

    @RequiresApi(
            api = 21
    )
    public DrawableTextView setRadius(@Px int px) {
        this.radius = px;
        if (!(this.getOutlineProvider() instanceof RadiusViewOutlineProvider)) {
            this.setOutlineProvider(new RadiusViewOutlineProvider());
            this.setClipToOutline(true);
        } else {
            this.invalidateOutline();
        }

        return this;
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface POSITION {
        int START = 0;
        int TOP = 1;
        int END = 2;
        int BOTTOM = 3;
    }

    @RequiresApi(
            api = 21
    )
    public class RadiusViewOutlineProvider extends ViewOutlineProvider {
        public RadiusViewOutlineProvider() {
        }

        public void getOutline(View view, Outline outline) {
            outline.setRoundRect(0, 0, DrawableTextView.this.getWidth(), DrawableTextView.this.getHeight(), (float) DrawableTextView.this.radius);
        }
    }
}
