/*
 * BreadWallet
 *
 * Created by Mihail Gutan on <mihail@breadwallet.com> on 5/3/17.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.legacy.presenter.customviews;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("AppCompatCustomView") // we don't need to support older versions
public class BREdit extends EditText {
    private static final String TAG = BREdit.class.getName();
    private List<EditTextEventListener> mEditTextEventListeners = new ArrayList<>();

    public enum EditTextEvent {
        CUT,
        PASTE,
        COPY
    }

    public BREdit(Context context) {
        super(context);
    }

    public BREdit(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BREdit(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public BREdit(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * Here you can catch paste, copy and cut events
     */
    @Override
    public boolean onTextContextMenuItem(int id) {
        boolean consumed = super.onTextContextMenuItem(id);
        switch (id) {
            case android.R.id.cut:
                fireEditTextEventListeners(EditTextEvent.CUT);
                break;
            case android.R.id.paste:
                fireEditTextEventListeners(EditTextEvent.PASTE);
                break;
            case android.R.id.copy:
                fireEditTextEventListeners(EditTextEvent.COPY);
                break;
        }
        return consumed;
    }

    public void fireEditTextEventListeners(EditTextEvent editTextEvent) {
        for (EditTextEventListener editTextEventListener : mEditTextEventListeners) {
            editTextEventListener.onEvent(editTextEvent);
        }
    }

    public interface EditTextEventListener {
        void onEvent(EditTextEvent editTextEvent);
    }

    public void addEditTextEventListener(EditTextEventListener editTextEventListener) {
        if (!mEditTextEventListeners.contains(editTextEventListener)) {
            mEditTextEventListeners.add(editTextEventListener);
        }
    }

    public void removeEditTextEventListener(EditTextEventListener editTextEventListener) {
        mEditTextEventListeners.remove(editTextEventListener);
    }

}
