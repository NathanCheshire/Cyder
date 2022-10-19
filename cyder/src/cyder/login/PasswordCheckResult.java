package cyder.login;

import com.google.common.base.Preconditions;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;

import java.util.Optional;

/**
 * The result returned by a {@link LoginHandler} checkPassword invocation.
 */
class PasswordCheckResult {
    /**
     * The result of the password check.
     */
    private final Result result;

    /**
     * The uuid to use if the result is of type {@link Result#SUCCESS}.
     */
    private String uuid;

    /**
     * Suppress default constructor.
     */
    public PasswordCheckResult() {
        throw new IllegalMethodException(CyderStrings.ILLEGAL_CONSTRUCTOR);
    }

    /**
     * Constructs a new password check result.
     *
     * @param result the result of this password check result
     */
    public PasswordCheckResult(Result result) {
        this.result = Preconditions.checkNotNull(result);
    }

    /**
     * Constructs a new password check result
     *
     * @param result the result of this password check result
     * @param uuid   the uuid of this password check result
     */
    public PasswordCheckResult(Result result, String uuid) {
        this.result = Preconditions.checkNotNull(result);
        this.uuid = Preconditions.checkNotNull(uuid);
    }

    /**
     * Returns the result of this password check result.
     *
     * @return the result of this password check result
     */
    public Result getResult() {
        return result;
    }

    /**
     * Returns the uuid of this password check result.
     *
     * @return the uuid of this password check result
     */
    public Optional<String> getUuid() {
        if (uuid == null) return Optional.empty();
        return Optional.of(uuid);
    }
}
