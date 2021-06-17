/**
 * BreadWallet
 *
 * Created by Mihail Gutan <mihail@breadwallet.com> on 2/14/19.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */

package com.breadwallet.legacy.presenter.customviews;

import android.content.Context;
import androidx.appcompat.widget.Toolbar;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * From ToolBar onTouchEvent doc:
 * "Toolbars always eat touch events, but should still respect the touch event dispatch
 * contract. If the normal View implementation doesn't want the events, we'll just silently
 * eat the rest of the gesture without reporting the events to the default implementation
 * since that's what it expects"
 * <p>
 * Thus this class to override onTouchEvent.
 */
public class NonClickableToolbar extends Toolbar {

    public NonClickableToolbar(Context context) {
        super(context);
    }

    public NonClickableToolbar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NonClickableToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return false;
    }
}
