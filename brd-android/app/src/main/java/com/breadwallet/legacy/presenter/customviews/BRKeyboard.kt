/**
 * BreadWallet
 *
 * Created by Mihail Gutan <mihail@breadwallet.com> on 2/22/17.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.legacy.presenter.customviews

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.RelativeSizeSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import com.breadwallet.R
import com.breadwallet.databinding.PinPadBinding
import com.breadwallet.tools.util.Utils

class BRKeyboard : ConstraintLayout, View.OnClickListener {
    private var mKeyInsertListener: OnInsertListener? = null

    val binding = PinPadBinding.inflate(LayoutInflater.from(context), this, true)
    private var mPinButtons: List<Button> = with(binding) {
        listOf(num0, num1, num2, num3, num4, num5, num6, num7, num8, num9, decimal)
    }

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) :
        super(context, attrs, defStyleAttr) {
            init(attrs)
        }

    constructor(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(attrs)
    }

    private val vibrator by lazy { context?.getSystemService<Vibrator>() }

    private fun init(attrs: AttributeSet?) {
        var showAlphabet = false
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.BRKeyboard)
        val attributeCount = attributes.indexCount
        for (i in 0 until attributeCount) {
            when (val attr = attributes.getIndex(i)) {
                R.styleable.BRKeyboard_showAlphabet -> {
                    showAlphabet = attributes.getBoolean(attr, false)
                }
            }
        }
        attributes.recycle()
        setWillNotDraw(false)
        mPinButtons = with(binding) {
            listOf(num0, num1, num2, num3, num4, num5, num6, num7, num8, num9, decimal)
        }
        val bottomPaddingDimen = context.resources.getInteger(R.integer.pin_keyboard_bottom_padding)
        val bottomPaddingPixels = Utils.getPixelsFromDps(context, bottomPaddingDimen)
        for (i in mPinButtons.indices) {
            val button = mPinButtons[i]
            button.setOnClickListener(this)
            if (i <= LAST_NUMBER_INDEX) {
                button.text = getText(i, showAlphabet)
            }
            if (showAlphabet) {
                button.setPadding(0, 0, 0, bottomPaddingPixels)
            }
        }
        binding.deleteHitBox.setOnClickListener(this)
        if (showAlphabet) {
            binding.delete.setPadding(0, 0, 0, bottomPaddingPixels)
        }
        invalidate()
    }

    private fun getText(index: Int, showAlphabet: Boolean): CharSequence {
        val span1 = SpannableString(index.toString())
        return if (showAlphabet) {
            val span2: SpannableString = when (index) {
                2 -> SpannableString("ABC")
                3 -> SpannableString("DEF")
                4 -> SpannableString("GHI")
                5 -> SpannableString("JKL")
                6 -> SpannableString("MNO")
                7 -> SpannableString("PQRS")
                8 -> SpannableString("TUV")
                9 -> SpannableString("WXYZ")
                else -> SpannableString(" ")
            }
            span1.setSpan(RelativeSizeSpan(1f), 0, 1, 0)
            span2.setSpan(RelativeSizeSpan(0.35f), 0, span2.length, 0)
            TextUtils.concat(span1, "\n", span2)
        } else {
            span1
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        invalidate()
    }

    fun setOnInsertListener(listener: OnInsertListener?) {
        mKeyInsertListener = listener
    }

    override fun onClick(v: View) {
        mKeyInsertListener?.onKeyInsert(if (v is Button) v.text.toString() else "")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(50)
        }
    }

    fun interface OnInsertListener {
        fun onKeyInsert(key: String)
    }

    fun setBRKeyboardColor(color: Int) {
        setBackgroundColor(ContextCompat.getColor(context, color))
    }

    fun setBRButtonBackgroundResId(resId: Int, isBuyController: Boolean = false) {
        for (button in mPinButtons) {
            button.setBackgroundResource(resId)
        }
        if (isBuyController) {
            binding.deleteHitBox.setBackgroundResource(resId)
        } else {
            binding.delete.setBackgroundResource(resId)
        }
        invalidate()
    }

    fun setShowDecimal(showDecimal: Boolean) {
        mPinButtons[DECIMAL_INDEX].isVisible = showDecimal
        invalidate()
    }

    /**
     * Change the background of a specific button
     *
     * @param color the color to be used
     */
    fun setDeleteButtonBackgroundColor(color: Int) {
        binding.delete.setBackgroundColor(color)
        invalidate()
    }

    fun setDeleteImage(resourceId: Int) {
        binding.delete.setImageDrawable(ResourcesCompat.getDrawable(resources, resourceId, null))
        invalidate()
    }

    fun setButtonTextColor(colors: IntArray?) {
        colors ?: return
        for (i in mPinButtons.indices) {
            mPinButtons[i].setTextColor(colors[i])
        }
        invalidate()
    }

    fun setDeleteButtonTint(color: Int) {
        DrawableCompat.setTint(binding.delete.drawable.mutate(), color)
    }

    companion object {
        const val TAG = "BRKeyboard"
        private const val LAST_NUMBER_INDEX = 9
        private const val DECIMAL_INDEX = 10
    }
}
