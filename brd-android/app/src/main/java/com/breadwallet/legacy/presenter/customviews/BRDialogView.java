/*
 * BreadWallet
 *
 * Created by Mihail Gutan on <mihail@breadwallet.com> on 3/15/17.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.legacy.presenter.customviews;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.breadwallet.R;
import com.breadwallet.tools.animation.UiUtils;
import com.breadwallet.tools.manager.BRReportsManager;
import com.breadwallet.tools.util.AndroidExtensionsKt;
import com.breadwallet.tools.util.Utils;

public class BRDialogView extends DialogFragment {

    public static final String POSITIVE_BUTTON_COLOR = "#4b77f3";
    public static final String NEGATIVE_BUTTON_COLOR = "#b3c0c8";
    public static final int MAX_LINE_COUNT = 4;
    public static final int SMALLER_TEXT_SIZE = 16;
    public static final int MESSAGE_PADDING_END = 16;

    private String mTitle = "";
    private String mMessage = "";
    private String mPositiveButtonText = "";
    private String mNegativeButtonText = "";
    private BRDialogView.BROnClickListener mPositiveListener;
    private BRDialogView.BROnClickListener mNegativeListener;
    private BRDialogView.BROnClickListener mHelpListener;
    private DialogInterface.OnDismissListener mDismissListener;
    private BRButton mNegativeButton;
    private LinearLayout mButtonsLayout;
    private ImageButton mHelpButton;
    private LinearLayout mMainLayout;
    private boolean mAlignTextToStart = false;

    //provide the way to have clickable span in the message
    private SpannableString mSpanMessage;

    private boolean mShowHelpIcon;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.bread_alert_dialog, null);
        TextView titleText = view.findViewById(R.id.dialog_title);
        TextView messageText = view.findViewById(R.id.dialog_text);
        BRButton positiveButton = view.findViewById(R.id.pos_button);
        mNegativeButton = view.findViewById(R.id.neg_button);
        mMainLayout = view.findViewById(R.id.main_layout);
        mButtonsLayout = view.findViewById(R.id.linearLayout3);
        mHelpButton = view.findViewById(R.id.help_icon);

        //assuming that is the last text to bet set.
        if (Utils.isNullOrEmpty(mTitle)) {
            mMainLayout.removeView(titleText);
        }
        if (Utils.isNullOrEmpty(mMessage) && (mSpanMessage == null || Utils.isNullOrEmpty(mSpanMessage.toString()))) {
            mMainLayout.removeView(messageText);
        }

        // Resize the title text if it is greater than 4 lines
        titleText.setText(mTitle);
        if (titleText.getLineCount() > MAX_LINE_COUNT) {
            titleText.setTextSize(SMALLER_TEXT_SIZE);
        }

        // Resize the message text if it is greater than 4 lines
        if (!Utils.isNullOrEmpty(mMessage)) {
            messageText.setText(mMessage);

        }
        if (mSpanMessage != null && !Utils.isNullOrEmpty(mSpanMessage.toString())) {
            messageText.setText(mSpanMessage);
            messageText.setMovementMethod(LinkMovementMethod.getInstance());
        }

        if (messageText.getLineCount() > MAX_LINE_COUNT) {
            messageText.setTextSize(SMALLER_TEXT_SIZE);
        }
        if (mAlignTextToStart) {
            messageText.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        }

        if (Utils.isNullOrEmpty(mPositiveButtonText)) {
            // No buttons case. Remove the buttons.
            mButtonsLayout.removeView(positiveButton);
            mButtonsLayout.removeView(mNegativeButton);
            mButtonsLayout.requestLayout();

            // Make the dialog modal.
            setCancelable(false);
        } else {
            // Setup the first button.  Use this button to achieve a one button dialogs.
            positiveButton.setColor(Color.parseColor(POSITIVE_BUTTON_COLOR));
            positiveButton.setHasShadow(false);
            positiveButton.setText(mPositiveButtonText);
            positiveButton.setOnClickListener(v -> {
                if (!UiUtils.isClickAllowed()) {
                    return;
                }
                if (mPositiveListener != null) {
                    mPositiveListener.onClick(BRDialogView.this);
                }
            });

            // If there is no negative button, remove it from the view.
            if (Utils.isNullOrEmpty(mNegativeButtonText)) {
                mButtonsLayout.removeView(mNegativeButton);
                mButtonsLayout.requestLayout();

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.CENTER_HORIZONTAL;
                params.weight = 1.0f;
                positiveButton.setLayoutParams(params);
            }

            // Setup the second button. Use this button in addition to the first to achieve two button dialogs.
            mNegativeButton.setColor(Color.parseColor(NEGATIVE_BUTTON_COLOR));
            mNegativeButton.setHasShadow(false);
            mNegativeButton.setText(mNegativeButtonText);
            mNegativeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!UiUtils.isClickAllowed()) {
                        return;
                    }
                    if (mNegativeListener != null) {
                        mNegativeListener.onClick(BRDialogView.this);
                    }
                }
            });
        }

        builder.setView(view);

        if (mShowHelpIcon) {
            mHelpButton.setVisibility(View.VISIBLE);

            messageText.setPadding(0, 0, 0, AndroidExtensionsKt.getPixelsFromDps(getContext(), MESSAGE_PADDING_END));

            mHelpButton.setOnClickListener(view1 -> {
                if (!UiUtils.isClickAllowed()) {
                    return;
                }
                if (mHelpListener != null) {
                    mHelpListener.onClick(BRDialogView.this);
                }
            });

        } else {
            mHelpButton.setVisibility(View.INVISIBLE);

        }
        // Create the AlertDialog object and return it
        return builder.create();
    }

    public void showHelpIcon(boolean show) {
        this.mShowHelpIcon = show;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mDismissListener != null) {
            mDismissListener.onDismiss(dialog);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public void setMessage(String message) {
        this.mMessage = message;
    }

    public void setSpan(SpannableString message) {
        if (message == null) {
            BRReportsManager.reportBug(new NullPointerException("setSpan with null message"));
            return;
        }
        this.mSpanMessage = message;
    }

    public void setPosButton(@NonNull String posButton) {
        this.mPositiveButtonText = posButton;
    }

    public void setNegButton(String negButton) {
        this.mNegativeButtonText = negButton;
    }

    public void setPosListener(BRDialogView.BROnClickListener posListener) {
        this.mPositiveListener = posListener;
    }

    public void setNegListener(BRDialogView.BROnClickListener negListener) {
        this.mNegativeListener = negListener;
    }

    public void setHelpListener(BROnClickListener helpListener) {
        this.mHelpListener = helpListener;
    }

    public void setDismissListener(DialogInterface.OnDismissListener dismissListener) {
        this.mDismissListener = dismissListener;
    }

    public void setAlignTextToStart(boolean alignTextToStart) {
        mAlignTextToStart = alignTextToStart;
    }

    public interface BROnClickListener {
        void onClick(BRDialogView brDialogView);
    }

    public void dismissWithAnimation() {
        BRDialogView.this.dismiss();

    }

}
