package info.jallaix.message.service;

import info.jallaix.message.dto.Language;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Validator for the language entity
 */
public class LanguageValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return Language.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {

        if (target == null)
            errors.reject("language.required", "The language is required");
        else {

            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "code", "language.code.required", "language.code.required");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "label", "language.label.required", "language.label.required");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "englishLabel", "language.englishLabel.required", "language.englishLabel.required");
        }
    }
}
