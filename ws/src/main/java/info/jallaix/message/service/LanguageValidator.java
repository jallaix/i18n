package info.jallaix.message.service;

import info.jallaix.message.dto.Language;
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

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "code", "field.required");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "label", "field.required");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "englishLabel", "field.required");
    }
}
