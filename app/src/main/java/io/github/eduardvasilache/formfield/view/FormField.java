package io.github.eduardvasilache.formfield.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Set;

import io.github.eduardvasilache.formfield.R;
import io.github.eduardvasilache.formfield.util.SimpleTextWatcher;
import io.github.eduardvasilache.formfield.validator.EmailValidator;
import io.github.eduardvasilache.formfield.validator.Validator;

public class FormField extends FrameLayout {

    public interface OnFocusChangeListener {
        void onFocusChanged(boolean hasFocus);
    }

    private static final long ANIMATION_DURATION = 300;

    private static final int VALIDATOR_EMAIL = 1;

    private final FormFieldEditText editText;
    private final TextView tvMessage;

    private final Set<OnFocusChangeListener> focusChangeListeners = new HashSet<>();
    private OnClickListener listener;

    private boolean editable;
    private boolean required;
    private boolean trimText;
    private int minLength;
    private String requiredMessage;
    private String invalidMessage;
    private String errorMessage;
    private Validator validator;

    private boolean requiredMessageVisible;
    private boolean invalidMessageVisible;
    private boolean errorMessageVisible;

    public FormField(@NonNull Context context) {
        this(context, null, 0);
    }

    public FormField(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FormField(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.FormField);

        editable = a.getBoolean(R.styleable.FormField_formField_editable, true);
        requiredMessage = a.getString(R.styleable.FormField_formField_requiredMessage);
        invalidMessage = a.getString(R.styleable.FormField_formField_invalidMessage);
        required = a.getBoolean(R.styleable.FormField_formField_required, false);
        trimText = a.getBoolean(R.styleable.FormField_formField_trim, false);
        minLength = a.getInt(R.styleable.FormField_formField_minLength, 0);
        String hint = a.getString(R.styleable.FormField_formField_hint);
        int editTextAppearance = a.getResourceId(R.styleable.FormField_formField_textAppearance, -1);
        int errorTextAppearance = a.getResourceId(R.styleable.FormField_formField_messageTextAppearance, -1);
        int inputType = a.getInt(R.styleable.FormField_android_inputType, InputType.TYPE_CLASS_TEXT);
        int imeOptions = a.getInt(R.styleable.FormField_android_imeOptions, EditorInfo.IME_ACTION_UNSPECIFIED);
        int validatorIndex = a.getInt(R.styleable.FormField_formField_validator, 0);

        a.recycle();

        // Input
        LayoutParams etLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER_VERTICAL);
        editText = new FormFieldEditText(getContext());
        editText.setId(R.id.form_field_input);
        editText.setLayoutParams(etLayoutParams);
        editText.setBackgroundResource(0);
        editText.setPadding(0, 0, 0, 0);
        editText.setInputType(inputType);
        editText.setImeOptions(imeOptions);
        editText.setMaxLines(1);
        editText.setHint(hint);
        editText.setEnabled(editable);

        if (editTextAppearance != -1) {
            editText.setTextAppearance(context, editTextAppearance);
        }

        // Message
        LayoutParams tvLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM);
        tvMessage = new TextView(getContext());
        tvMessage.setId(R.id.form_field_message);
        tvMessage.setLayoutParams(tvLayoutParams);
        tvMessage.setText(requiredMessage);
        tvMessage.setMaxLines(1);
        tvMessage.setEllipsize(TextUtils.TruncateAt.END);
        tvMessage.setVisibility(GONE);
        tvMessage.setAlpha(0.0f);

        if (errorTextAppearance != -1) {
            tvMessage.setTextAppearance(context, errorTextAppearance);
        }

        switch (validatorIndex) {
            case VALIDATOR_EMAIL:
                setValidator(new EmailValidator());
                break;
        }

        addView(editText);
        addView(tvMessage);

        setupListeners();

        setSaveEnabled(true);
        setClickable(true);
        setFocusable(true);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.childrenStates = new SparseArray();
        savedState.errorMessage = errorMessage;
        savedState.requiredMessageVisible = requiredMessageVisible;
        savedState.invalidMessageVisible = invalidMessageVisible;
        savedState.errorMessageVisible = errorMessageVisible;
        for (int i = 0; i < getChildCount(); i++) {
            //noinspection unchecked
            getChildAt(i).saveHierarchyState(savedState.childrenStates);
        }
        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        for (int i = 0; i < getChildCount(); i++) {
            //noinspection unchecked
            getChildAt(i).restoreHierarchyState(savedState.childrenStates);
        }

        errorMessage = savedState.errorMessage;

        if (savedState.requiredMessageVisible) {
            showRequiredMessage();
        }
        if (savedState.invalidMessageVisible) {
            showInvalidMessage();
        }
        if (savedState.errorMessageVisible) {
            showErrorMessage();
        }
    }

    @Override
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        dispatchFreezeSelfOnly(container);
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (requiredMessageVisible || invalidMessageVisible || errorMessageVisible) {
            mergeDrawableStates(drawableState, new int[]{R.attr.state_invalid});
        }
        return drawableState;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        editText.setEnabled(enabled);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (editable) {
                editText.requestFocus();
                InputMethodManager imm = (InputMethodManager) getContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
                }
            }

            if (listener != null) {
                listener.onClick(this);
            }
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public void setOnClickListener(OnClickListener listener) {
        this.listener = listener;
    }

    public void setText(String text) {
        editText.setText(text);
    }

    public void showErrorMessage(String message) {
        this.errorMessage = message;
        showErrorMessage();
    }

    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    public void setOnEditorActionListener(TextView.OnEditorActionListener listener) {
        editText.setOnEditorActionListener(listener);
    }

    public void setCursorVisible(boolean visible) {
        editText.setCursorVisible(visible);
    }

    public void addTextChangedListener(TextWatcher watcher) {
        editText.addTextChangedListener(watcher);
    }

    public void removeTextChangedListener(TextWatcher watcher) {
        editText.removeTextChangedListener(watcher);
    }

    public void addOnFocusChangeListener(OnFocusChangeListener onFocusChangeListener) {
        focusChangeListeners.add(onFocusChangeListener);
    }

    public void removeOnFocusChangeListener(OnFocusChangeListener onFocusChangeListener) {
        focusChangeListeners.remove(onFocusChangeListener);
    }

    public boolean validate() {
        boolean valid = true;
        String value = getValue();

        if (TextUtils.isEmpty(value)) {
            if (required) {
                valid = false;
                showRequiredMessage();
            }
        } else {
            valid = isValid();
            if (!valid) {
                showInvalidMessage();
            }
        }

        if (valid && (requiredMessageVisible || invalidMessageVisible || errorMessageVisible)) {
            clearErrorState();
        }

        return valid;
    }

    public String getValue() {
        String value = editText.getText().toString();
        if (trimText) {
            value = value.trim();
        }
        return value;
    }

    private void setupListeners() {
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (view.getWindowVisibility() == View.VISIBLE) {
                    if (hasFocus) {
                        if (requiredMessageVisible || invalidMessageVisible || errorMessageVisible) {
                            //clearErrorState();
                        }
                    } else {
                        validate();
                    }
                    setSelected(hasFocus);
                    dispatchFocusChange(hasFocus);
                }
            }
        });

        editText.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO
                if (s.length() > 0) {
                    validate();
                }
            }
        });
    }

    private void dispatchFocusChange(boolean hasFocus) {
        for (OnFocusChangeListener listener : focusChangeListeners) {
            listener.onFocusChanged(hasFocus);
        }
    }

    private boolean isValid() {
        String value = getValue();

        if (minLength > 0 && value.length() < minLength) {
            return false;
        }

        if (validator != null) {
            return validator.validate(value);
        }

        return true;
    }

    private void showRequiredMessage() {
        requiredMessageVisible = true;
        switchToErrorState(requiredMessage);
    }

    private void showInvalidMessage() {
        invalidMessageVisible = true;
        switchToErrorState(invalidMessage);
    }

    private void showErrorMessage() {
        errorMessageVisible = true;
        switchToErrorState(errorMessage);
    }

    private void switchToErrorState(String message) {
        editText.setInvalidStateVisibility(true);
        tvMessage.setText(message);
        refreshDrawableState();
        showMessage();
    }

    private void clearErrorState() {
        requiredMessageVisible = false;
        invalidMessageVisible = false;
        errorMessageVisible = false;
        editText.setInvalidStateVisibility(false);
        refreshDrawableState();
        hideMessage();
    }

    private void showMessage() {
        editText.post(new Runnable() {
            @Override
            public void run() {
                editText.animate()
                        .y(getHeight() / 2 - editText.getHeight())
                        .setDuration(ANIMATION_DURATION)
                        .start();
            }
        });

        tvMessage.setVisibility(VISIBLE);
        tvMessage.post(new Runnable() {
            @Override
            public void run() {
                tvMessage.animate()
                        .y(getHeight() / 2)
                        .alpha(1.0f)
                        .setDuration(ANIMATION_DURATION)
                        .start();
            }
        });
    }

    private void hideMessage() {
        editText.post(new Runnable() {
            @Override
            public void run() {
                editText.animate()
                        .y(getHeight() / 2 - editText.getHeight() / 2)
                        .setDuration(ANIMATION_DURATION)
                        .start();
            }
        });

        tvMessage.post(new Runnable() {
            @Override
            public void run() {
                tvMessage.animate()
                        .y(getHeight())
                        .alpha(0.0f)
                        .setDuration(ANIMATION_DURATION)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                tvMessage.setVisibility(GONE);
                            }
                        })
                        .start();
            }
        });
    }

    private static class SavedState extends BaseSavedState {

        SparseArray childrenStates;
        String errorMessage;
        boolean requiredMessageVisible;
        boolean invalidMessageVisible;
        boolean errorMessageVisible;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in, ClassLoader classLoader) {
            super(in);
            childrenStates = in.readSparseArray(classLoader);
            errorMessage = in.readString();
            requiredMessageVisible = in.readInt() == 1;
            invalidMessageVisible = in.readInt() == 1;
            errorMessageVisible = in.readInt() == 1;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            //noinspection unchecked
            out.writeSparseArray(childrenStates);
            out.writeString(errorMessage);
            out.writeInt(requiredMessageVisible ? 1 : 0);
            out.writeInt(invalidMessageVisible ? 1 : 0);
            out.writeInt(errorMessageVisible ? 1 : 0);
        }

        public static final ClassLoaderCreator<SavedState> CREATOR = new ClassLoaderCreator<FormField.SavedState>() {
            @Override
            public FormField.SavedState createFromParcel(Parcel source, ClassLoader loader) {
                return new FormField.SavedState(source, loader);
            }

            @Override
            public FormField.SavedState createFromParcel(Parcel source) {
                return createFromParcel(null);
            }

            public FormField.SavedState[] newArray(int size) {
                return new FormField.SavedState[size];
            }
        };
    }

}