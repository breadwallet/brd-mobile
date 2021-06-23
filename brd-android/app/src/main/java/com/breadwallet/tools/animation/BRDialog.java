package com.breadwallet.tools.animation;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import androidx.annotation.NonNull;
import android.text.SpannableString;
import android.util.Log;

import com.breadwallet.R;
import com.breadwallet.app.BreadApp;
import com.breadwallet.legacy.presenter.customviews.BRDialogView;
import com.breadwallet.tools.threads.executor.BRExecutor;

/**
 * BreadWallet
 *
 * Created by Mihail Gutan <mihail@breadwallet.com> on 3/15/17.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
public class BRDialog {
    private static final String TAG = BRDialog.class.getName();

    /**
     * Safe from any threads
     *
     * @param context needs to be activity
     */
    // TODO: This method was moved to DialogActivity. This can be removed once all the callers have been moved to DialogActivity.
    public static void showCustomDialog(@NonNull final Activity activity, @NonNull final String title, @NonNull final String message,
                                        @NonNull final String posButton, final String negButton, final BRDialogView.BROnClickListener posListener,
                                        final BRDialogView.BROnClickListener negListener, final DialogInterface.OnDismissListener dismissListener, final int iconRes) {
        if (activity.isDestroyed()) {
            Log.e(TAG, "showCustomDialog: FAILED, context is destroyed");
            return;
        }
        Log.e(TAG, "showCustomDialog: Title:" + title + ", message: " + message);
        BRExecutor.getInstance().forMainThreadTasks().execute(new Runnable() {
            @Override
            public void run() {
                BRDialogView dialog = new BRDialogView();
                dialog.setTitle(title);
                dialog.setMessage(message);
                dialog.setPosButton(posButton);
                dialog.setNegButton(negButton);
                dialog.setPosListener(posListener);
                dialog.setNegListener(negListener);
                dialog.setDismissListener(dismissListener);
                if (!activity.isDestroyed()) {
                    activity.getFragmentManager().beginTransaction().add(dialog, dialog.getTag()).commitAllowingStateLoss();
                }
            }
        });

    }

    public static void showHelpDialog(@NonNull final Context app, @NonNull final String title, @NonNull final String message, @NonNull final String posButton, @NonNull final String negButton, final BRDialogView.BROnClickListener posListener, final BRDialogView.BROnClickListener negListener, final BRDialogView.BROnClickListener helpListener) {

        if (((Activity) app).isDestroyed()) {
            Log.e(TAG, "showCustomDialog: FAILED, context is destroyed");
            return;
        }

        BRExecutor.getInstance().forMainThreadTasks().execute(new Runnable() {
            @Override
            public void run() {
                BRDialogView dialog = new BRDialogView();
                dialog.setTitle(title);
                dialog.setMessage(message);
                dialog.setPosButton(posButton);
                dialog.setNegButton(negButton);
                dialog.setPosListener(posListener);
                dialog.setNegListener(negListener);
                dialog.setHelpListener(helpListener);
                dialog.showHelpIcon(true);
                dialog.show(((Activity) app).getFragmentManager(), dialog.getClass().getName());

            }
        });
    }

    public static void showSimpleDialog(@NonNull final Activity app, @NonNull final String title, @NonNull final String message) {
        showCustomDialog(app, title, message, app.getString(R.string.AccessibilityLabels_close), null, new BRDialogView.BROnClickListener() {
            @Override
            public void onClick(BRDialogView brDialogView) {
                brDialogView.dismissWithAnimation();
            }
        }, null, null, 0);
    }

    //same but with a SpannableString as message to be able to click on a portion of the text with a listener
    public static void showCustomDialog(@NonNull final Context app, @NonNull final String title, @NonNull final SpannableString message,
                                        @NonNull final String posButton, final String negButton, final BRDialogView.BROnClickListener posListener,
                                        final BRDialogView.BROnClickListener negListener, final DialogInterface.OnDismissListener dismissListener, final int iconRes) {
        if (((Activity) app).isDestroyed()) {
            Log.e(TAG, "showCustomDialog: FAILED, context is destroyed");
            return;
        }

        BRExecutor.getInstance().forMainThreadTasks().execute(new Runnable() {
            @Override
            public void run() {
                BRDialogView dialog = new BRDialogView();
                dialog.setTitle(title);
                dialog.setSpan(message);//setting Span instead of String
                dialog.setPosButton(posButton);
                dialog.setNegButton(negButton);
                dialog.setPosListener(posListener);
                dialog.setNegListener(negListener);
                dialog.setDismissListener(dismissListener);
                dialog.show(((Activity) app).getFragmentManager(), dialog.getClass().getName());
            }
        });
    }

    /**
     * Safe from any threads
     *
     * @param context needs to be activity
     */
    public static void showCustomDialog(@NonNull final Context context,
                                        @NonNull final String title,
                                        @NonNull final String message,
                                        @NonNull final String posButton,
                                        final String negButton,
                                        final BRDialogView.BROnClickListener posListener,
                                        final BRDialogView.BROnClickListener negListener,
                                        final DialogInterface.OnDismissListener dismissListener,
                                        final boolean mAlignTextToStart) {
        final Activity activity = (context instanceof Activity) ? (Activity) context : (Activity) BreadApp.getBreadContext();
        if (activity.isDestroyed()) {
            Log.e(TAG, "showCustomDialog: FAILED, context is destroyed");
            return;
        }
        Log.e(TAG, "showCustomDialog: Title:" + title + ", message: " + message);
        BRExecutor.getInstance().forMainThreadTasks().execute(new Runnable() {
            @Override
            public void run() {
                BRDialogView dialog = new BRDialogView();
                dialog.setTitle(title);
                dialog.setMessage(message);
                dialog.setPosButton(posButton);
                dialog.setNegButton(negButton);
                dialog.setPosListener(posListener);
                dialog.setNegListener(negListener);
                dialog.setDismissListener(dismissListener);
                dialog.setAlignTextToStart(mAlignTextToStart);
                if (!activity.isDestroyed()) {
                    activity.getFragmentManager().beginTransaction().add(dialog, dialog.getTag()).commitAllowingStateLoss();
                }
            }
        });

    }
}
