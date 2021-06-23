package com.breadwallet.tools.animation;

import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;

/**
 * BreadWallet
 *
 * Created by Mihail Gutan <mihail@breadwallet.com> on 6/19/16.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */

public class DecelerateOvershootInterpolator implements Interpolator {
    private DecelerateInterpolator accelerate;
    private OvershootInterpolator overshoot;

    public DecelerateOvershootInterpolator(float factor, float tension) {
        accelerate = new DecelerateInterpolator(factor);
        overshoot = new OvershootInterpolator(tension);
    }

    @Override
    public float getInterpolation(float input) {
        return overshoot.getInterpolation(accelerate.getInterpolation(input));
    }

}