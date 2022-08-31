package org.linphone.views

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.button.MaterialButton
import org.linphone.R
import kotlin.math.min

open class NethSquareButton @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.style.NethCTIButton
) : MaterialButton(ctx, attrs, defStyleAttr) {

    fun recalculateIconPosition() {
        iconGravity = ICON_GRAVITY_START
        iconGravity = ICON_GRAVITY_TEXT_START
    }

    fun setSize(size: Int) {
        setMeasuredDimension(size, size)
        recalculateIconPosition()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        val width = measuredWidth
        val height = measuredHeight
        val dimen = min(width, height)
        setSize(dimen)
    }
}