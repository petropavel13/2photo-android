/*
 * This file provided by Facebook is for non-commercial testing and evaluation
 * purposes only.  Facebook reserves all rights not expressly granted.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * FACEBOOK BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.facebook.samples.zoomable;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.facebook.common.internal.Preconditions;
import com.facebook.common.logging.FLog;
import com.facebook.drawee.controller.AbstractDraweeController;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.drawable.AutoRotateDrawable;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.DraweeView;
import com.github.petropavel13.twophoto.R;

import javax.annotation.Nullable;

/**
 * DraweeView that has zoomable capabilities.
 * <p>
 * Once the image loads, pinch-to-zoom and translation gestures are enabled.
 *
 */
public class ZoomableDraweeView extends DraweeView<GenericDraweeHierarchy>
        implements ZoomableController.Listener {

  private static final Class<?> TAG = ZoomableDraweeView.class;

  private static final float HUGE_IMAGE_SCALE_FACTOR_THRESHOLD = 1.1f;

  private final RectF mImageBounds = new RectF();
  private final RectF mViewBounds = new RectF();

  private float mAspectRatio = 0;

  private final ControllerListener mControllerListener = new BaseControllerListener<Object>() {
    @Override
    public void onFinalImageSet(
            String id,
            @Nullable Object imageInfo,
            @Nullable Animatable animatable) {
      ZoomableDraweeView.this.onFinalImageSet();
    }

    @Override
    public void onRelease(String id) {
      ZoomableDraweeView.this.onRelease();
    }
  };

  private DraweeController mHugeImageController;
  private ZoomableController mZoomableController = DefaultZoomableController.newInstance();

  public ZoomableDraweeView(Context context) {
    super(context);
    init();
    inflateHierarchy(context, null);
  }

  public ZoomableDraweeView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
    inflateHierarchy(context, attrs);
  }

  public ZoomableDraweeView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init();
    inflateHierarchy(context, attrs);
  }

  private void inflateHierarchy(Context context, @Nullable AttributeSet attrs) {
    Resources resources = context.getResources();

    // fading animation defaults
    int fadeDuration = GenericDraweeHierarchyBuilder.DEFAULT_FADE_DURATION;
    // images & scale types defaults
    int placeholderId = 0;
    ScalingUtils.ScaleType placeholderScaleType
            = GenericDraweeHierarchyBuilder.DEFAULT_SCALE_TYPE;
    int retryImageId = 0;
    ScalingUtils.ScaleType retryImageScaleType =
            GenericDraweeHierarchyBuilder.DEFAULT_SCALE_TYPE;
    int failureImageId = 0;
    ScalingUtils.ScaleType failureImageScaleType =
            GenericDraweeHierarchyBuilder.DEFAULT_SCALE_TYPE;
    int progressBarId = 0;
    ScalingUtils.ScaleType progressBarScaleType =
            GenericDraweeHierarchyBuilder.DEFAULT_SCALE_TYPE;
    ScalingUtils.ScaleType actualImageScaleType =
            GenericDraweeHierarchyBuilder.DEFAULT_ACTUAL_IMAGE_SCALE_TYPE;
    int backgroundId = 0;
    int overlayId = 0;
    int pressedStateOverlayId = 0;
    // rounding defaults
    boolean roundAsCircle = false;
    int roundedCornerRadius = 0;
    boolean roundTopLeft = true;
    boolean roundTopRight = true;
    boolean roundBottomRight = true;
    boolean roundBottomLeft = true;
    int roundWithOverlayColor = 0;
    int roundingBorderWidth = 0;
    int roundingBorderColor = 0;
    int progressBarAutoRotateInterval = 0;


    if (attrs != null) {
      TypedArray gdhAttrs = context.obtainStyledAttributes(
              attrs,
              R.styleable.GenericDraweeView);
      try {
        // fade duration
        fadeDuration = gdhAttrs.getInt(
                R.styleable.GenericDraweeView_fadeDuration,
                fadeDuration);

        // aspect ratio
        mAspectRatio = gdhAttrs.getFloat(
                R.styleable.GenericDraweeView_viewAspectRatio,
                mAspectRatio);

        // placeholder image
        placeholderId = gdhAttrs.getResourceId(
                R.styleable.GenericDraweeView_placeholderImage,
                placeholderId);
        // placeholder image scale type
        placeholderScaleType = getScaleTypeFromXml(
                gdhAttrs,
                R.styleable.GenericDraweeView_placeholderImageScaleType,
                placeholderScaleType);

        // retry image
        retryImageId = gdhAttrs.getResourceId(
                R.styleable.GenericDraweeView_retryImage,
                retryImageId);
        // retry image scale type
        retryImageScaleType = getScaleTypeFromXml(
                gdhAttrs,
                R.styleable.GenericDraweeView_retryImageScaleType,
                retryImageScaleType);

        // failure image
        failureImageId = gdhAttrs.getResourceId(
                R.styleable.GenericDraweeView_failureImage,
                failureImageId);
        // failure image scale type
        failureImageScaleType = getScaleTypeFromXml(
                gdhAttrs,
                R.styleable.GenericDraweeView_failureImageScaleType,
                failureImageScaleType);

        // progress bar image
        progressBarId = gdhAttrs.getResourceId(
                R.styleable.GenericDraweeView_progressBarImage,
                progressBarId);
        // progress bar image scale type
        progressBarScaleType = getScaleTypeFromXml(
                gdhAttrs,
                R.styleable.GenericDraweeView_progressBarImageScaleType,
                progressBarScaleType);
        // progress bar auto rotate interval
        progressBarAutoRotateInterval = gdhAttrs.getInteger(
                R.styleable.GenericDraweeView_progressBarAutoRotateInterval,
                0);

        // actual image scale type
        actualImageScaleType = getScaleTypeFromXml(
                gdhAttrs,
                R.styleable.GenericDraweeView_actualImageScaleType,
                actualImageScaleType);

        // background
        backgroundId = gdhAttrs.getResourceId(
                R.styleable.GenericDraweeView_backgroundImage,
                backgroundId);

        // overlay
        overlayId = gdhAttrs.getResourceId(
                R.styleable.GenericDraweeView_overlayImage,
                overlayId);

        // pressedState overlay
        pressedStateOverlayId = gdhAttrs.getResourceId(
                R.styleable.GenericDraweeView_pressedStateOverlayImage,
                pressedStateOverlayId);

        // rounding parameters
        roundAsCircle = gdhAttrs.getBoolean(
                R.styleable.GenericDraweeView_roundAsCircle,
                roundAsCircle);
        roundedCornerRadius = gdhAttrs.getDimensionPixelSize(
                R.styleable.GenericDraweeView_roundedCornerRadius,
                roundedCornerRadius);
        roundTopLeft = gdhAttrs.getBoolean(
                R.styleable.GenericDraweeView_roundTopLeft,
                roundTopLeft);
        roundTopRight = gdhAttrs.getBoolean(
                R.styleable.GenericDraweeView_roundTopRight,
                roundTopRight);
        roundBottomRight = gdhAttrs.getBoolean(
                R.styleable.GenericDraweeView_roundBottomRight,
                roundBottomRight);
        roundBottomLeft = gdhAttrs.getBoolean(
                R.styleable.GenericDraweeView_roundBottomLeft,
                roundBottomLeft);
        roundWithOverlayColor = gdhAttrs.getColor(
                R.styleable.GenericDraweeView_roundWithOverlayColor,
                roundWithOverlayColor);
        roundingBorderWidth = gdhAttrs.getDimensionPixelSize(
                R.styleable.GenericDraweeView_roundingBorderWidth,
                roundingBorderWidth);
        roundingBorderColor = gdhAttrs.getColor(
                R.styleable.GenericDraweeView_roundingBorderColor,
                roundingBorderColor);
      }
      finally {
        gdhAttrs.recycle();
      }
    }

    GenericDraweeHierarchyBuilder builder = new GenericDraweeHierarchyBuilder(resources);
    // set fade duration
    builder.setFadeDuration(fadeDuration);
    // set images & scale types
    if (placeholderId > 0) {
      builder.setPlaceholderImage(resources.getDrawable(placeholderId), placeholderScaleType);
    }
    if (retryImageId > 0) {
      builder.setRetryImage(resources.getDrawable(retryImageId), retryImageScaleType);
    }
    if (failureImageId > 0) {
      builder.setFailureImage(resources.getDrawable(failureImageId), failureImageScaleType);
    }
    if (progressBarId > 0) {
      Drawable progressBarDrawable = resources.getDrawable(progressBarId);
      if (progressBarAutoRotateInterval > 0) {
        progressBarDrawable =
                new AutoRotateDrawable(progressBarDrawable, progressBarAutoRotateInterval);
      }
      builder.setProgressBarImage(progressBarDrawable, progressBarScaleType);
    }
    if (backgroundId > 0) {
      builder.setBackground(resources.getDrawable(backgroundId));
    }
    if (overlayId > 0) {
      builder.setOverlay(resources.getDrawable(overlayId));
    }
    if (pressedStateOverlayId > 0) {
      builder.setPressedStateOverlay(getResources().getDrawable(pressedStateOverlayId));
    }

    builder.setActualImageScaleType(actualImageScaleType);
    // set rounding parameters
    if (roundAsCircle || roundedCornerRadius > 0) {
      RoundingParams roundingParams = new RoundingParams();
      roundingParams.setRoundAsCircle(roundAsCircle);
      if (roundedCornerRadius > 0) {
        roundingParams.setCornersRadii(
                roundTopLeft ? roundedCornerRadius : 0,
                roundTopRight ? roundedCornerRadius : 0,
                roundBottomRight ? roundedCornerRadius : 0,
                roundBottomLeft ? roundedCornerRadius : 0);
      }
      if (roundWithOverlayColor != 0) {
        roundingParams.setOverlayColor(roundWithOverlayColor);
      }
      if (roundingBorderColor != 0 && roundingBorderWidth > 0) {
        roundingParams.setBorder(roundingBorderColor, roundingBorderWidth);
      }
      builder.setRoundingParams(roundingParams);
    }
    setHierarchy(builder.build());
  }

  /**
   * Returns the scale type indicated in XML, or null if the special 'none' value was found.
   */
  private static ScalingUtils.ScaleType getScaleTypeFromXml(
          TypedArray attrs,
          int attrId,
          ScalingUtils.ScaleType defaultScaleType) {
    String xmlType = attrs.getString(attrId);
    return (xmlType != null) ? ScalingUtils.ScaleType.fromString(xmlType) : defaultScaleType;
  }

  private void init() {
    mZoomableController.setListener(this);
  }

  public void setZoomableController(ZoomableController zoomableController) {
    Preconditions.checkNotNull(zoomableController);
    mZoomableController.setListener(null);
    mZoomableController = zoomableController;
    mZoomableController.setListener(this);
  }

  @Override
  public void setController(@Nullable DraweeController controller) {
    setControllers(controller, null);
  }

  private void setControllersInternal(
          @Nullable DraweeController controller,
          @Nullable DraweeController hugeImageController) {
    removeControllerListener(getController());
    addControllerListener(controller);
    mHugeImageController = hugeImageController;
    super.setController(controller);
  }

  /**
   * Sets the controllers for the normal and huge image.
   *
   * <p> IMPORTANT: in order to avoid a flicker when switching to the huge image, the huge image
   * controller should have the normal-image-uri set as its low-res-uri.
   *
   * @param controller controller to be initially used
   * @param hugeImageController controller to be used after the client starts zooming-in
   */
  public void setControllers(
          @Nullable DraweeController controller,
          @Nullable DraweeController hugeImageController) {
    setControllersInternal(null, null);
    mZoomableController.setEnabled(false);
    setControllersInternal(controller, hugeImageController);
  }

  private void maybeSetHugeImageController() {
    if (mHugeImageController != null &&
            mZoomableController.getScaleFactor() > HUGE_IMAGE_SCALE_FACTOR_THRESHOLD) {
      setControllersInternal(mHugeImageController, null);
    }
  }

  private void removeControllerListener(DraweeController controller) {
    if (controller instanceof AbstractDraweeController) {
      ((AbstractDraweeController) controller)
              .removeControllerListener(mControllerListener);
    }
  }

  private void addControllerListener(DraweeController controller) {
    if (controller instanceof AbstractDraweeController) {
      ((AbstractDraweeController) controller)
              .addControllerListener(mControllerListener);
    }
  }

  @Override
  protected void onDraw(Canvas canvas) {
    int saveCount = canvas.save();
    canvas.concat(mZoomableController.getTransform());
    super.onDraw(canvas);
    canvas.restoreToCount(saveCount);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (mZoomableController.onTouchEvent(event)) {
      if (mZoomableController.getScaleFactor() > 1.0f) {
        getParent().requestDisallowInterceptTouchEvent(true);
      }
      FLog.v(TAG, "onTouchEvent: view %x, handled by zoomable controller", this.hashCode());
      return true;
    }
    FLog.v(TAG, "onTouchEvent: view %x, handled by the super", this.hashCode());
    return super.onTouchEvent(event);
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    FLog.v(TAG, "onLayout: view %x", this.hashCode());
    super.onLayout(changed, left, top, right, bottom);
    updateZoomableControllerBounds();
  }

  private void onFinalImageSet() {
    FLog.v(TAG, "onFinalImageSet: view %x", this.hashCode());
    if (!mZoomableController.isEnabled()) {
      updateZoomableControllerBounds();
      mZoomableController.setEnabled(true);
    }
  }

  private void onRelease() {
    FLog.v(TAG, "onRelease: view %x", this.hashCode());
    mZoomableController.setEnabled(false);
  }

  @Override
  public void onTransformChanged(Matrix transform) {
    FLog.v(TAG, "onTransformChanged: view %x", this.hashCode());
    maybeSetHugeImageController();
    invalidate();
  }

  private void updateZoomableControllerBounds() {
    getHierarchy().getActualImageBounds(mImageBounds);
    mViewBounds.set(0, 0, getWidth(), getHeight());
    mZoomableController.setImageBounds(mImageBounds);
    mZoomableController.setViewBounds(mViewBounds);
    FLog.v(
            TAG,
            "updateZoomableControllerBounds: view %x, view bounds: %s, image bounds: %s",
            this.hashCode(),
            mViewBounds,
            mImageBounds);
  }
}
