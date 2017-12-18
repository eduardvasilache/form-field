package io.github.eduardvasilache.formfield.validator;

import java.util.regex.Pattern;

public class RegexValidator implements Validator {

    protected final Pattern pattern;

    public RegexValidator(Pattern pattern) {
        this.pattern = pattern;
    }

    public RegexValidator(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    public RegexValidator(String regex, int flags) {
        this.pattern = Pattern.compile(regex, flags);
    }

    @Override
    public boolean validate(String value) {
        return pattern.matcher(value).matches();
    }

}