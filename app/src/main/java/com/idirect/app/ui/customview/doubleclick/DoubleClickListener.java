package com.idirect.app.ui.customview.doubleclick;

import android.view.View;

public interface DoubleClickListener {

    /**
     * Called when the user make a single click.
     */
    void onSingleClick(final View view);

    /**
     * Called when the user make a double click.
     */
    void onDoubleClick(final View view);
}