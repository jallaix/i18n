package info.jallaix.message.service.validator;

import info.jallaix.message.dao.DomainDao;
import info.jallaix.message.dto.Language;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Validator for the language entity
 */
public class LanguageValidatorOnDelete implements Validator {

    private DomainDao domainDao;

    public LanguageValidatorOnDelete(DomainDao domainDao) {
        this.domainDao = domainDao;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return Language.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {

        Language language = (Language)target;
        if (domainDao.isLanguageUsed(language.getCode()))
            errors.rejectValue("code", "language.message.existing", "language.message.existing");
    }
}
