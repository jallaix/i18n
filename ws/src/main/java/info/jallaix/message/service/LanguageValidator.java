package info.jallaix.message.service;

import info.jallaix.message.dto.Language;
import org.springframework.http.RequestEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Created by Julien on 02/06/2016.
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

            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "code", "language.code.required", "The code is required");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "label", "language.code.required", "The label is required");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "englishLabel", "language.code.required", "The english label is required");
        }
    }
}
