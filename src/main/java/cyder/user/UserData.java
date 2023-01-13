package cyder.user;

import com.google.common.base.Preconditions;
import cyder.logging.LogTag;
import cyder.logging.Logger;

/**
 * A data piece of a user.
 */
public final class UserData<T> {
    private final String id;
    private final String displayName;
    private final String description;
    private final T defaultValue;
    private final Class<?> type;
    private final Runnable onChangeRunnable;

    /**
     * Constructs a new UserData object.
     *
     * @param id               the id of the user data
     * @param displayName      the display name of the user data
     * @param description      the description of the user data
     * @param defaultValue     the default value of the user data
     * @param type             the type of the user data
     * @param onChangeRunnable the runnable to invoke when this property is updated
     */
    public UserData(String id,
                    String displayName,
                    String description,
                    T defaultValue,
                    Class<?> type,
                    Runnable onChangeRunnable) {
        Preconditions.checkNotNull(id);
        Preconditions.checkArgument(!id.isEmpty());
        Preconditions.checkNotNull(displayName);
        Preconditions.checkArgument(!displayName.isEmpty());
        Preconditions.checkNotNull(description);
        Preconditions.checkArgument(!description.isEmpty());
        Preconditions.checkNotNull(defaultValue);
        Preconditions.checkNotNull(type);
        Preconditions.checkNotNull(onChangeRunnable);

        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.defaultValue = defaultValue;
        this.type = type;
        this.onChangeRunnable = onChangeRunnable;

        Logger.log(LogTag.OBJECT_CREATION, this);
    }
}
