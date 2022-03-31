package com.awpghost.auth.validators;

import com.google.common.base.Joiner;
import org.passay.CharacterData;
import org.passay.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static org.passay.IllegalCharacterRule.ERROR_CODE;

@Component
public class PasswordRequirementsValidator implements ConstraintValidator<ValidPassword, String> {

    private final int lowerCaseCharacterLength;

    private final int upperCaseCharacterLength;

    private final int digitNumberCharacterLength;

    private final String specialCharactersList;

    private final int specialCharactersLength;

    private final int minimumPasswordLength;

    private final int maximumPasswordLength;

    private PasswordValidator passwordValidator;

    private CharacterRule lowerCaseRule;

    private CharacterRule upperCaseRule;

    private CharacterRule digitRule;

    private CharacterRule specialCharacterRule;

    private LengthRule lengthRule;

    private UsernameRule usernameRule;

    @Autowired
    PasswordRequirementsValidator(@Value("${password.lower.case.character.length}") int lowerCaseCharacterLength,
                                  @Value("${password.upper.case.character.length}") int upperCaseCharacterLength,
                                  @Value("${password.digit.number.character.length}") int digitNumberCharacterLength,
                                  @Value("${password.special.character.list}") String specialCharactersList,
                                  @Value("${password.special.character.length}") int specialCharactersLength,
                                  @Value("${password.minimum.length}") int minimumPasswordLength,
                                  @Value("${password.maximum.length}") int maximumPasswordLength) {
        this.lowerCaseCharacterLength = lowerCaseCharacterLength;
        this.upperCaseCharacterLength = upperCaseCharacterLength;
        this.digitNumberCharacterLength = digitNumberCharacterLength;
        this.specialCharactersList = specialCharactersList;
        this.specialCharactersLength = specialCharactersLength;
        this.minimumPasswordLength = minimumPasswordLength;
        this.maximumPasswordLength = maximumPasswordLength;
    }

    @Override
    public void initialize(final ValidPassword arg0) {
        generatePasswordRules();
        generatePasswordValidator();
    }

    private void generatePasswordRules() {
        createLowerCaseCharacterRule();
        createUpperCaseCharacterRule();
        createDigitNumberRule();
        createSpecialCharacterRule();
        createLengthRule();
        createUsernameRule();
    }

    private void generatePasswordValidator() {
        passwordValidator = new PasswordValidator(lowerCaseRule, upperCaseRule, digitRule, specialCharacterRule, lengthRule, usernameRule, new WhitespaceRule());
    }

    private void createLowerCaseCharacterRule() {
        CharacterData lowerCaseChars = EnglishCharacterData.LowerCase;
        lowerCaseRule = new CharacterRule(lowerCaseChars);
        lowerCaseRule.setNumberOfCharacters(lowerCaseCharacterLength);
    }

    private void createUpperCaseCharacterRule() {
        upperCaseRule = new CharacterRule(EnglishCharacterData.UpperCase);
        upperCaseRule.setNumberOfCharacters(upperCaseCharacterLength);
    }

    private void createDigitNumberRule() {
        digitRule = new CharacterRule(EnglishCharacterData.Digit);
        digitRule.setNumberOfCharacters(digitNumberCharacterLength);
    }

    private void createSpecialCharacterRule() {
        CharacterData specialChars = new CharacterData() {
            public String getErrorCode() {
                return ERROR_CODE; // If added any special characters that doesn't accept by the self defined characters. IllegalCharacterRule will be thrown
            }

            public String getCharacters() {
                return specialCharactersList;
            }
        };

        specialCharacterRule = new CharacterRule(specialChars);
        specialCharacterRule.setNumberOfCharacters(specialCharactersLength);
    }

    private void createLengthRule() {
        lengthRule = new LengthRule(minimumPasswordLength, maximumPasswordLength);
    }

    private void createUsernameRule() {
        usernameRule = new UsernameRule();
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        RuleResult result = passwordValidator.validate(new PasswordData(password));

        if (result.isValid()) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(
                        Joiner.on(",").join(passwordValidator.getMessages(result)))
                .addConstraintViolation();
        return false;
    }
}
