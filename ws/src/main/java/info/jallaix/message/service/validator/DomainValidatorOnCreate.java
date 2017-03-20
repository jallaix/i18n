package info.jallaix.message.service.validator;

import info.jallaix.message.bean.Domain;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.Locale;

/**
 * Created by Julien on 22/01/2017.
 */
public class DomainValidatorOnCreate implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return Domain.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {

        Domain domain = (Domain) target;

        // Code, description, default language tag and available language tags can't be empty
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "code", "domain.code.required", "domain.code.required");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "description", "domain.description.required", "domain.description.required");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "defaultLanguageTag", "domain.defaultLanguageTag.required", "domain.defaultLanguageTag.required");
        if (CollectionUtils.isEmpty(domain.getAvailableLanguageTags()))
            errors.rejectValue("availableLanguageTags", "domain.availableLanguageTags.required", "domain.availableLanguageTags.required");

        // Default language tag must exist in available language tags
        if (!StringUtils.isBlank(domain.getDefaultLanguageTag()) && !CollectionUtils.isEmpty(domain.getAvailableLanguageTags()))
            if (!domain.getAvailableLanguageTags().contains(domain.getDefaultLanguageTag()))
                errors.rejectValue("defaultLanguageTag", "domain.defaultLanguageTag.matchAvailable", "domain.defaultLanguageTag.matchAvailable");

        // Default language tag must match an existing locale
        if (!StringUtils.isBlank(domain.getDefaultLanguageTag()))
            if (!LocaleUtils.isAvailableLocale(Locale.forLanguageTag(domain.getDefaultLanguageTag())))
                errors.rejectValue("defaultLanguageTag", "domain.defaultLanguageTag.unavailable", "domain.defaultLanguageTag.unavailable");

        // Available language tags must match an existing locales
        if (!CollectionUtils.isEmpty(domain.getAvailableLanguageTags()))
            domain.getAvailableLanguageTags().forEach(languageTag -> {
                        if (!LocaleUtils.isAvailableLocale(Locale.forLanguageTag(languageTag)))
                            errors.rejectValue("availableLanguageTags", "domain.availableLanguageTags.unavailable", "domain.availableLanguageTags.unavailable");
                    }
            );

    }
}
