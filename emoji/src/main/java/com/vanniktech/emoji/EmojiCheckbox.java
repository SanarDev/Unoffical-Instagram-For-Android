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

import android.content.Context;
import android.graphics.Paint;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import androidx.annotation.CallSuper;
import androidx.annotation.DimenRes;
import androidx.annotation.Px;
import androidx.appcompat.widget.AppCompatCheckBox;

@SuppressWarnings("CPD-START") public class EmojiCheckbox extends AppCompatCheckBox implements EmojiDisplayable {
  private float emojiSize;

  public EmojiCheckbox(final Context context) {
    this(context, null);
  }

  public EmojiCheckbox(final Context context, final AttributeSet attrs) {
    super(context, attrs);
    emojiSize = Utils.initTextView(this, attrs);
  }

  public EmojiCheckbox(final Context context, final AttributeSet attrs, final int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    emojiSize = Utils.initTextView(this, attrs);
  }

  @Override @CallSuper public void setText(final CharSequence rawText, final BufferType type) {
    if (isInEditMode()) {
      super.setText(rawText, type);
      return;
    }

    final CharSequence text = rawText == null ? "" : rawText;
    final SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(text);
    final Paint.FontMetrics fontMetrics = getPaint().getFontMetrics();
    final float defaultEmojiSize = fontMetrics.descent - fontMetrics.ascent;
    EmojiManager.getInstance().replaceWithImages(getContext(), spannableStringBuilder, emojiSize != 0 ? emojiSize : defaultEmojiSize);
    super.setText(spannableStringBuilder, type);
  }

  @Override public float getEmojiSize() {
    return emojiSize;
  }

  @Override public final void setEmojiSize(@Px final int pixels) {
    setEmojiSize(pixels, true);
  }

  @Override public final void setEmojiSize(@Px final int pixels, final boolean shouldInvalidate) {
    emojiSize = pixels;

    if (shouldInvalidate) {
      setText(getText());
    }
  }

  @Override public final void setEmojiSizeRes(@DimenRes final int res) {
    setEmojiSizeRes(res, true);
  }

  @Override public final void setEmojiSizeRes(@DimenRes final int res, final boolean shouldInvalidate) {
    setEmojiSize(getResources().getDimensionPixelSize(res), shouldInvalidate);
  }
}
