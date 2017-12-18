package io.github.eduardvasilache.formfield.view;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;

import io.github.eduardvasilache.formfield.R;

class FormFieldEditText extends AppCompatEditText {

    private boolean invalidStateVisible;

    public FormFieldEditText(Context context) {
        this(context, null);
    }

    public FormFieldEditText(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public FormFieldEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (invalidStateVisible) {
            mergeDrawableStates(drawableState, new int[]{R.attr.state_invalid});
        }
        return drawableState;
    }

    public void setInvalidStateVisibility(boolean visible) {
        invalidStateVisible = visible;
        refreshDrawableState();
    }

}