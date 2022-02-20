package cyder.enums;

/**
 * Hashes used for AutoCyphering upon Cyder entry.
 */
public enum DebugHash {
    /**
     * Nathan's hash.
     */
    Nathan("Nathan","3789d9735ab15d29a3595ef3f70c377d2d7b4927c222a06d36f0abba335504a2");

    private final String name;
    private final String pass;

    /**
     * Creates a new DebugHash.
     *
     * @param name the username to use
     * @param pass the already once hashed password to use
     */
    DebugHash(String name, String pass) {
        this.name = name;
        this.pass = pass;
    }

    /**
     * Returns the username associated with this DebugHash.
     *
     * @return the username associated with this DebugHash
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the password associated with this DebugHash.
     *
     * @return the password associated with this DebugHash
     */
    public String getPass() {
        return pass;
    }
}
