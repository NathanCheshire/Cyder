package cyder.user;

public enum UserFile {
    MUSIC("Music", false),
    BACKGROUNDS("Backgrounds", false),
    NOTES("Notes", false),
    USERDATA("Userdata.json", true),
    FILES("Files", false);

    private final String name;
    private final boolean isFile;

    public String getName() {
        return name;
    }

    public boolean isFile() {
        return isFile;
    }

    UserFile(String name, boolean isFile) {
        this.name = name;
        this.isFile = isFile;
    }
}
