package io.github.eduardvasilache.formfield.view;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import io.github.eduardvasilache.formfield.R;

class FormFieldTextView extends AppCompatTextView {

    private boolean invalidStateVisible;
    private boolean filledStateVisible;

    public FormFieldTextView(Context context) {
        this(context, null);
    }

    public FormFieldTextView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public FormFieldTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 2);
        if (invalidStateVisible) {
            mergeDrawableStates(drawableState, new int[]{R.attr.state_invalid});
        }
        if (filledStateVisible) {
            mergeDrawableStates(drawableState, new int[]{R.attr.state_filled});
        }
        return drawableState;
    }

    public void setInvalidStateVisible(boolean invalidStateVisible) {
        this.invalidStateVisible = invalidStateVisible;
        refreshDrawableState();
    }

    public void setFilledStateVisible(boolean filledStateVisible) {
        this.filledStateVisible = filledStateVisible;
        refreshDrawableState();
    }

}