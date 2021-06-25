package com.breadwallet.legacy.presenter.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import androidx.annotation.Nullable;
import android.util.AttributeSet;

import com.breadwallet.R;

/**
 * BreadWallet
 *
 * Created by Mihail Gutan <mihail@breadwallet.com> on 5/8/17.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
public class BRNotificationBar extends androidx.appcompat.widget.Toolbar {

    private static final String TAG = BRNotificationBar.class.getName();

    private BaseTextView description;
    private BRButton close;

    public BRNotificationBar(Context context) {
        super(context);
        init(null);
    }

    public BRNotificationBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public BRNotificationBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        inflate(getContext(), R.layout.notification_bar, this);
        description = findViewById(R.id.description);
        close = findViewById(R.id.cancel_button);

        TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.BRNotificationBar);
        final int N = attributes.getIndexCount();
        for (int i = 0; i < N; ++i) {
            int attr = attributes.getIndex(i);
            switch (attr) {
                case R.styleable.BRNotificationBar_breadText:
                    String text = attributes.getString(0);
                    description.setText(text);
                    break;
            }
        }
        attributes.recycle();

        close.setOnClickListener(view -> this.setVisibility(GONE));

    }

}
