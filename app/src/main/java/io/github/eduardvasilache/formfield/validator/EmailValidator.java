package io.github.eduardvasilache.formfield.validator;

import android.util.Patterns;

public class EmailValidator extends RegexValidator {

    public EmailValidator() {
        super(Patterns.EMAIL_ADDRESS);
    }

}