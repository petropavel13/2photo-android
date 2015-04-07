package com.github.petropavel13.twophoto.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import com.github.petropavel13.twophoto.R

/**
 * Created by petropavel on 07/04/15.
 */

class RetryView: LinearLayout {
    constructor(ctx: Context): super(ctx) { }

    constructor(ctx: Context, attrs: AttributeSet): super(ctx, attrs) { }

    constructor(ctx: Context, attrs: AttributeSet, defStyleAttr: Int): super(ctx, attrs, defStyleAttr) { }

    constructor(ctx: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int): super(ctx, attrs, defStyleAttr, defStyleRes) { }

    var button: ImageButton? = null
    var textView: TextView? = null

    override fun onFinishInflate() {
        super.onFinishInflate()

        button = findViewById(R.id.retry_view_refresh_button) as? ImageButton
        textView = findViewById(R.id.retry_view_text_view) as? TextView
    }

    private var _onClickListener: View.OnClickListener? = null

    var onRetryListener: View.OnClickListener?
        get() = _onClickListener
        set(newValue) {
            _onClickListener = newValue

            button?.setOnClickListener(_onClickListener)
        }

    var errorText: CharSequence
        get() = textView?.getText() ?: ""
        set(newValue) {
            textView?.setText(newValue)
        }

    private var _textResource = R.string.retry_view_failed_to_load_content

    var errorTextResource: Int
        get() = _textResource
        set(newValue) {
            _textResource = newValue

            textView?.setText(_textResource)
        }
}
