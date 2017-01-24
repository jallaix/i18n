package info.jallaix.message.service.validator;

import info.jallaix.message.dao.DomainDao;
import info.jallaix.message.dto.Domain;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.Locale;

/**
 * Created by Julien on 22/01/2017.
 */
@Component
public class DomainValidatorOnUpdate implements Validator {

    @Autowired
    private DomainDao domainDao;

    @Override
    public boolean supports(Class<?> clazz) {
        return Domain.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {

        Domain domain = (Domain) target;
        Domain domainSaved = domainDao.findOne(domain.getId());

        // Code can't change
        if (domainSaved != null && !domainSaved.getCode().equals(domain.getCode()))
            errors.rejectValue("code", "domain.code.immutable", "domain.code.immutable");

        // Description, default language tag and available language tags can't be empty
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
