package io.github.eduardvasilache.formfield;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.DatePicker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import io.github.eduardvasilache.formfield.databinding.ActivityMainBinding;
import io.github.eduardvasilache.formfield.util.ImeUtils;
import io.github.eduardvasilache.formfield.validator.Validator;

public class MainActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener,
        DialogInterface.OnDismissListener, DialogInterface.OnCancelListener {

    private static final String KEY_BIRTH_DATE_DIALOG = "birthDateDialog";

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.US);

    private ActivityMainBinding binding;
    private DatePickerDialog birthDateDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setHost(this);

        binding.fieldBirthDate.setValidator(new Validator() {
            @Override
            public boolean validate(String value) {
                try {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(DATE_FORMAT.parse(value));
                    return calendar.get(Calendar.DAY_OF_MONTH) > 10;
                } catch (ParseException e) {
                    return false;
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (birthDateDialog != null && birthDateDialog.isShowing()) {
            outState.putBundle(KEY_BIRTH_DATE_DIALOG, birthDateDialog.onSaveInstanceState());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey(KEY_BIRTH_DATE_DIALOG)) {
            showBirthDateDialog();
            Bundle bundle = savedInstanceState.getBundle(KEY_BIRTH_DATE_DIALOG);
            if (bundle != null) {
                birthDateDialog.onRestoreInstanceState(bundle);
            }
        }
    }

    public void onBirthDateClicked(View view) {
        showBirthDateDialog();
    }

    public void onOkClicked(View view) {
        ImeUtils.hideKeyboard(this);
        clearStatus();

        if (isFormValid()) {
            appendStatusLine("Checking data with server...");
            if (isDataValid(binding.fieldFirstName.getValue(), binding.fieldLastName.getValue())) {
                appendStatusLine("Everything looks good");
            } else {
                binding.fieldFirstName.showErrorMessage("Server error");
                binding.fieldLastName.showErrorMessage("Server error");
                binding.fieldBirthDate.showErrorMessage("Server error");
                appendStatusLine("Completed with errors");
            }
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        Calendar birthDateCalendar = new GregorianCalendar();
        birthDateCalendar.set(year, month, day);
        binding.fieldBirthDate.setText(DATE_FORMAT.format(new Date(birthDateCalendar.getTimeInMillis())));
        binding.fieldBirthDate.validate();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        binding.fieldBirthDate.validate();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        binding.fieldBirthDate.validate();
    }

    private boolean isFormValid() {
        boolean valid;
        valid = binding.fieldFirstName.validate();
        valid &= binding.fieldLastName.validate();
        valid &= binding.fieldBirthDate.validate();
        return valid;
    }

    private void showBirthDateDialog() {
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(System.currentTimeMillis());

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        birthDateDialog = new DatePickerDialog(this, this, year, month, day);
        birthDateDialog.setOnDismissListener(this);
        birthDateDialog.setOnCancelListener(this);
        birthDateDialog.show();
    }

    private boolean isDataValid(String firstName, String lastName) {
        return firstName.toLowerCase().contains("admin");
    }

    private void appendStatusLine(String text) {
        String currentText = binding.tvStatus.getText().toString();
        String newText;
        if (TextUtils.isEmpty(currentText)) {
            newText = text;
        } else {
            newText = currentText + "\n" + text;
        }
        binding.tvStatus.setText(newText);
    }

    private void clearStatus() {
        binding.tvStatus.setText("");
    }

}