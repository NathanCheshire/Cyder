package cyder.user.creation;

import cyder.user.UserUtil;

/**
 * An enum representing the status of some field input.
 */
public enum InputValidation {
    /**
     * All details are valid.
     */
    VALID("Valid details"),

    /**
     * The username is already in use.
     */
    USERNAME_IN_USE("Username already in use"),

    /**
     * No username is present.
     */
    NO_USERNAME("No username"),

    /**
     * The username is invalid
     */
    INVALID_USERNAME("Invalid username"),

    /**
     * No password is present.
     */
    NO_PASSWORD("No password"),

    /**
     * No confirmation password is present.
     */
    NO_CONFIRMATION_PASSWORD("No confirmation password"),

    /**
     * The passwords do not match.
     */
    PASSWORDS_DO_NOT_MATCH("Passwords do not match"),

    /**
     * The password contains no letter.
     */
    NO_LETTER_IN_PASSWORD("Password needs a letter"),

    /**
     * The password is of an invalid length.
     */
    INVALID_PASSWORD_LENGTH("Password is not >= " + UserUtil.MIN_PASSWORD_LENGTH + " in length"),

    /**
     * The password contains no number.
     */
    NO_NUMBER_IN_PASSWORD("Password needs a number");

    /**
     * The message for this validation.
     */
    private final String message;

    InputValidation(String message) {
        this.message = message;
    }

    /**
     * Returns the message for this validation.
     *
     * @return the message for this validation
     */
    public String getMessage() {
        return message;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getMessage();
    }
}
