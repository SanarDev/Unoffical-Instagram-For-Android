/*
 * Copyright (C) 2016 - Niklas Baudy, Ruben Gees, Mario Đanić and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.vanniktech.emoji;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.view.autofill.AutofillManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.PopupWindow;

import androidx.annotation.CheckResult;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.viewpager.widget.ViewPager;

import com.vanniktech.emoji.emoji.Emoji;
import com.vanniktech.emoji.listeners.OnEmojiBackspaceClickListener;
import com.vanniktech.emoji.listeners.OnEmojiClickListener;
import com.vanniktech.emoji.listeners.OnEmojiLongClickListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupDismissListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupShownListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardCloseListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardOpenListener;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.O;
import static androidx.core.view.ViewCompat.requestApplyInsets;
import static com.vanniktech.emoji.Utils.backspace;
import static com.vanniktech.emoji.Utils.checkNotNull;

@SuppressWarnings("PMD.GodClass")
public final class EmojiPopup implements EmojiResultReceiver.Receiver {
    static final int MIN_KEYBOARD_HEIGHT = 50;
    static final int APPLY_WINDOW_INSETS_DURATION = 250;

    final View rootView;
    final Activity context;

    @NonNull
    final RecentEmoji recentEmoji;
    @NonNull
    final VariantEmoji variantEmoji;
    @NonNull
    final EmojiVariantPopup variantPopup;

    final PopupWindow popupWindow;
    final EditText editText;

    boolean isPendingOpen;
    boolean isKeyboardOpen;

    private int globalKeyboardHeight;
    private int delay;

    @Nullable
    OnEmojiPopupShownListener onEmojiPopupShownListener;
    @Nullable
    OnSoftKeyboardCloseListener onSoftKeyboardCloseListener;
    @Nullable
    OnSoftKeyboardOpenListener onSoftKeyboardOpenListener;

    @Nullable
    OnEmojiBackspaceClickListener onEmojiBackspaceClickListener;
    @Nullable
    OnEmojiClickListener onEmojiClickListener;
    @Nullable
    OnEmojiPopupDismissListener onEmojiPopupDismissListener;

    int popupWindowHeight;
    int originalImeOptions = -1;

    final EmojiResultReceiver emojiResultReceiver = new EmojiResultReceiver(new Handler(Looper.getMainLooper()));

    final ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        @SuppressWarnings("PMD.CyclomaticComplexity")
        public void onGlobalLayout() {
            updateKeyboardState();
        }
    };

    final View.OnAttachStateChangeListener onAttachStateChangeListener = new View.OnAttachStateChangeListener() {
        @Override
        public void onViewAttachedToWindow(final View v) {
            start();
        }

        @Override
        public void onViewDetachedFromWindow(final View v) {
            releaseMemory();
        }
    };

    public void releaseMemory() {
        stop();

        popupWindow.setOnDismissListener(null);

        if (SDK_INT < LOLLIPOP) {
            rootView.getViewTreeObserver().removeGlobalOnLayoutListener(onGlobalLayoutListener);
        }

        rootView.removeOnAttachStateChangeListener(onAttachStateChangeListener);
    }

    final OnEmojiClickListener internalOnEmojiClickListener = new OnEmojiClickListener() {
        @Override
        public void onEmojiClick(@NonNull final EmojiImageView imageView, @NonNull final Emoji emoji) {
            Utils.input(editText, emoji);

            recentEmoji.addEmoji(emoji);
            variantEmoji.addVariant(emoji);
            imageView.updateEmoji(emoji);

            if (onEmojiClickListener != null) {
                onEmojiClickListener.onEmojiClick(imageView, emoji);
            }

            variantPopup.dismiss();
        }
    };

    final OnEmojiLongClickListener internalOnEmojiLongClickListener = new OnEmojiLongClickListener() {
        @Override
        public void onEmojiLongClick(@NonNull final EmojiImageView view, @NonNull final Emoji emoji) {
            variantPopup.show(view, emoji);
        }
    };

    final OnEmojiBackspaceClickListener internalOnEmojiBackspaceClickListener = new OnEmojiBackspaceClickListener() {
        @Override
        public void onEmojiBackspaceClick(final View v) {
            backspace(editText);

            if (onEmojiBackspaceClickListener != null) {
                onEmojiBackspaceClickListener.onEmojiBackspaceClick(v);
            }
        }
    };

    final PopupWindow.OnDismissListener onDismissListener = new PopupWindow.OnDismissListener() {
        @Override
        public void onDismiss() {
            if (editText instanceof EmojiEditText && ((EmojiEditText) editText).isKeyboardInputDisabled()) {
                editText.clearFocus();
            }
            if (onEmojiPopupDismissListener != null) {
                onEmojiPopupDismissListener.onEmojiPopupDismiss();
            }
        }
    };

    EmojiPopup(@NonNull final EmojiPopup.Builder builder, @NonNull final EditText editText) {
        this.context = Utils.asActivity(builder.rootView.getContext());
        this.rootView = builder.rootView.getRootView();
        this.editText = editText;
        this.recentEmoji = builder.recentEmoji;
        this.variantEmoji = builder.variantEmoji;

        popupWindow = new PopupWindow(context);
        variantPopup = new EmojiVariantPopup(rootView, internalOnEmojiClickListener);

        final EmojiView emojiView = new EmojiView(context,
                internalOnEmojiClickListener, internalOnEmojiLongClickListener, builder);

        emojiView.setOnEmojiBackspaceClickListener(internalOnEmojiBackspaceClickListener);

        popupWindow.setContentView(emojiView);
        popupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NOT_NEEDED);
        popupWindow.setBackgroundDrawable(new BitmapDrawable(context.getResources(), (Bitmap) null)); // To avoid borders and overdraw.
        popupWindow.setOnDismissListener(onDismissListener);

        if (builder.keyboardAnimationStyle != 0) {
            popupWindow.setAnimationStyle(builder.keyboardAnimationStyle);
        }

        // Root view might already be laid out in which case we need to manually call start()
        if (rootView.getParent() != null) {
            start();
        }

        rootView.addOnAttachStateChangeListener(onAttachStateChangeListener);
    }

    void updateKeyboardState() {
        final int keyboardHeight = Utils.getInputMethodHeight(context, rootView);

        if (keyboardHeight > Utils.dpToPx(context, MIN_KEYBOARD_HEIGHT)) {
            updateKeyboardStateOpened(keyboardHeight);
        } else {
            updateKeyboardStateClosed();
        }
    }

    void start() {
        if (SDK_INT >= LOLLIPOP) {
            context.getWindow().getDecorView().setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                int previousOffset;

                @Override
                public WindowInsets onApplyWindowInsets(final View v, final WindowInsets insets) {
                    final int offset;

                    if (insets.getSystemWindowInsetBottom() < insets.getStableInsetBottom()) {
                        offset = insets.getSystemWindowInsetBottom();
                    } else {
                        offset = insets.getSystemWindowInsetBottom() - insets.getStableInsetBottom();
                    }

                    if (offset != previousOffset || offset == 0) {
                        previousOffset = offset;

                        if (offset > Utils.dpToPx(context, MIN_KEYBOARD_HEIGHT)) {
                            updateKeyboardStateOpened(offset);
                        } else {
                            updateKeyboardStateClosed();
                        }
                    }

                    return context.getWindow().getDecorView().onApplyWindowInsets(insets);
                }
            });
        } else {
            rootView.getViewTreeObserver().removeGlobalOnLayoutListener(onGlobalLayoutListener);
            rootView.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
        }
    }

    public void stop() {
        dismiss();

        if (SDK_INT >= LOLLIPOP) {
            context.getWindow().getDecorView().setOnApplyWindowInsetsListener(null);
        }
    }

    @SuppressWarnings("PMD.CyclomaticComplexity")
    void updateKeyboardStateOpened(final int keyboardHeight) {
        if (popupWindowHeight > 0 && popupWindow.getHeight() != popupWindowHeight) {
            popupWindow.setHeight(popupWindowHeight);
        } else if (popupWindowHeight == 0 && popupWindow.getHeight() != keyboardHeight) {
            popupWindow.setHeight(keyboardHeight);
        }

        if (globalKeyboardHeight != keyboardHeight) {
            globalKeyboardHeight = keyboardHeight;
            delay = APPLY_WINDOW_INSETS_DURATION;
        } else {
            delay = 0;
        }

        final int properWidth = Utils.getProperWidth(context);

        if (popupWindow.getWidth() != properWidth) {
            popupWindow.setWidth(properWidth);
        }

        if (!isKeyboardOpen) {
            isKeyboardOpen = true;
            if (onSoftKeyboardOpenListener != null) {
                onSoftKeyboardOpenListener.onKeyboardOpen(keyboardHeight);
            }
        }

        if (isPendingOpen) {
            showAtBottom();
        }
    }

    void updateKeyboardStateClosed() {
        isKeyboardOpen = false;

        if (onSoftKeyboardCloseListener != null) {
            onSoftKeyboardCloseListener.onKeyboardClose();
        }

        if (isShowing()) {
            dismiss();
        }
    }

    /**
     * Set PopUpWindow's height.
     * If height is greater than 0 then this value will be used later on. If it is 0 then the
     * keyboard height will be dynamically calculated and set as {@link PopupWindow} height.
     *
     * @param popupWindowHeight - the height of {@link PopupWindow}
     */
    public void setPopupWindowHeight(final int popupWindowHeight) {
        this.popupWindowHeight = popupWindowHeight >= 0 ? popupWindowHeight : 0;
    }

    public void toggle() {
        if (!popupWindow.isShowing()) {
            // this is needed because something might have cleared the insets listener
            start();
            requestApplyInsets(context.getWindow().getDecorView());
            show();
        } else {
            dismiss();
        }
    }

    public void show() {
        if (Utils.shouldOverrideRegularCondition(context, editText) && originalImeOptions == -1) {
            originalImeOptions = editText.getImeOptions();
        }

        editText.setFocusableInTouchMode(true);
        editText.requestFocus();

        showAtBottomPending();
    }

    private void showAtBottomPending() {
        isPendingOpen = true;
        final InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

        if (Utils.shouldOverrideRegularCondition(context, editText)) {
            editText.setImeOptions(editText.getImeOptions() | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
            if (inputMethodManager != null) {
                inputMethodManager.restartInput(editText);
            }
        }

        if (inputMethodManager != null) {
            emojiResultReceiver.setReceiver(this);
            inputMethodManager.showSoftInput(editText, InputMethodManager.RESULT_UNCHANGED_SHOWN, emojiResultReceiver);
        }
    }

    public boolean isShowing() {
        return popupWindow.isShowing();
    }

    public void dismiss() {
        popupWindow.dismiss();
        variantPopup.dismiss();
        recentEmoji.persist();
        variantEmoji.persist();

        emojiResultReceiver.setReceiver(null);

        if (originalImeOptions != -1) {
            editText.setImeOptions(originalImeOptions);
            final InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

            if (inputMethodManager != null) {
                inputMethodManager.restartInput(editText);
            }

            if (SDK_INT >= O) {
                final AutofillManager autofillManager = context.getSystemService(AutofillManager.class);
                if (autofillManager != null) {
                    autofillManager.cancel();
                }
            }
        }
    }

    void showAtBottom() {
        isPendingOpen = false;
        editText.postDelayed(new Runnable() {
            @Override
            public void run() {
                popupWindow.showAtLocation(rootView, Gravity.NO_GRAVITY, 0,
                        Utils.getProperHeight(context) + popupWindowHeight);
            }
        }, delay);

        if (onEmojiPopupShownListener != null) {
            onEmojiPopupShownListener.onEmojiPopupShown();
        }
    }

    @Override
    public void onReceiveResult(final int resultCode, final Bundle data) {
        if (resultCode == 0 || resultCode == 1) {
            showAtBottom();
        }
    }

    public static final class Builder {
        @NonNull
        final View rootView;
        @StyleRes
        int keyboardAnimationStyle;
        @ColorInt
        int backgroundColor;
        @ColorInt
        int iconColor;
        @ColorInt
        int selectedIconColor;
        @ColorInt
        int dividerColor;
        @Nullable
        ViewPager.PageTransformer pageTransformer;
        @Nullable
        OnEmojiPopupShownListener onEmojiPopupShownListener;
        @Nullable
        OnSoftKeyboardCloseListener onSoftKeyboardCloseListener;
        @Nullable
        OnSoftKeyboardOpenListener onSoftKeyboardOpenListener;
        @Nullable
        OnEmojiBackspaceClickListener onEmojiBackspaceClickListener;
        @Nullable
        OnEmojiClickListener onEmojiClickListener;
        @Nullable
        OnEmojiPopupDismissListener onEmojiPopupDismissListener;
        @NonNull
        RecentEmoji recentEmoji;
        @NonNull
        VariantEmoji variantEmoji;
        int popupWindowHeight;

        private Builder(final View rootView) {
            this.rootView = checkNotNull(rootView, "The root View can't be null");
            this.recentEmoji = new RecentEmojiManager(rootView.getContext());
            this.variantEmoji = new VariantEmojiManager(rootView.getContext());
        }

        /**
         * @param rootView The root View of your layout.xml which will be used for calculating the height
         *                 of the keyboard.
         * @return builder For building the {@link EmojiPopup}.
         */
        @CheckResult
        public static Builder fromRootView(final View rootView) {
            return new Builder(rootView);
        }

        @CheckResult
        public Builder setOnSoftKeyboardCloseListener(@Nullable final OnSoftKeyboardCloseListener listener) {
            onSoftKeyboardCloseListener = listener;
            return this;
        }

        @CheckResult
        public Builder setOnEmojiClickListener(@Nullable final OnEmojiClickListener listener) {
            onEmojiClickListener = listener;
            return this;
        }

        @CheckResult
        public Builder setOnSoftKeyboardOpenListener(@Nullable final OnSoftKeyboardOpenListener listener) {
            onSoftKeyboardOpenListener = listener;
            return this;
        }

        @CheckResult
        public Builder setOnEmojiPopupShownListener(@Nullable final OnEmojiPopupShownListener listener) {
            onEmojiPopupShownListener = listener;
            return this;
        }

        @CheckResult
        public Builder setOnEmojiPopupDismissListener(@Nullable final OnEmojiPopupDismissListener listener) {
            onEmojiPopupDismissListener = listener;
            return this;
        }

        @CheckResult
        public Builder setOnEmojiBackspaceClickListener(@Nullable final OnEmojiBackspaceClickListener listener) {
            onEmojiBackspaceClickListener = listener;
            return this;
        }

        /**
         * Set PopUpWindow's height.
         * If height is not 0 then this value will be used later on. If it is 0 then the keyboard height will
         * be dynamically calculated and set as {@link PopupWindow} height.
         *
         * @param windowHeight - the height of {@link PopupWindow}
         * @since 0.7.0
         */
        @CheckResult
        public Builder setPopupWindowHeight(final int windowHeight) {
            this.popupWindowHeight = Math.max(windowHeight, 0);
            return this;
        }

        /**
         * Allows you to pass your own implementation of recent emojis. If not provided the default one
         * {@link RecentEmojiManager} will be used.
         *
         * @since 0.2.0
         */
        @CheckResult
        public Builder setRecentEmoji(@NonNull final RecentEmoji recent) {
            recentEmoji = checkNotNull(recent, "recent can't be null");
            return this;
        }

        /**
         * Allows you to pass your own implementation of variant emojis. If not provided the default one
         * {@link VariantEmojiManager} will be used.
         *
         * @since 0.5.0
         */
        @CheckResult
        public Builder setVariantEmoji(@NonNull final VariantEmoji variant) {
            variantEmoji = checkNotNull(variant, "variant can't be null");
            return this;
        }

        @CheckResult
        public Builder setBackgroundColor(@ColorInt final int color) {
            backgroundColor = color;
            return this;
        }

        @CheckResult
        public Builder setIconColor(@ColorInt final int color) {
            iconColor = color;
            return this;
        }

        @CheckResult
        public Builder setSelectedIconColor(@ColorInt final int color) {
            selectedIconColor = color;
            return this;
        }

        @CheckResult
        public Builder setDividerColor(@ColorInt final int color) {
            dividerColor = color;
            return this;
        }

        @CheckResult
        public Builder setKeyboardAnimationStyle(@StyleRes final int animation) {
            keyboardAnimationStyle = animation;
            return this;
        }

        @CheckResult
        public Builder setPageTransformer(@Nullable final ViewPager.PageTransformer transformer) {
            pageTransformer = transformer;
            return this;
        }

        @CheckResult
        public EmojiPopup build(@NonNull final EditText editText) {
            EmojiManager.getInstance().verifyInstalled();
            checkNotNull(editText, "EditText can't be null");

            final EmojiPopup emojiPopup = new EmojiPopup(this, editText);
            emojiPopup.onSoftKeyboardCloseListener = onSoftKeyboardCloseListener;
            emojiPopup.onEmojiClickListener = onEmojiClickListener;
            emojiPopup.onSoftKeyboardOpenListener = onSoftKeyboardOpenListener;
            emojiPopup.onEmojiPopupShownListener = onEmojiPopupShownListener;
            emojiPopup.onEmojiPopupDismissListener = onEmojiPopupDismissListener;
            emojiPopup.onEmojiBackspaceClickListener = onEmojiBackspaceClickListener;
            emojiPopup.popupWindowHeight = Math.max(popupWindowHeight, 0);
            return emojiPopup;
        }
    }
}
