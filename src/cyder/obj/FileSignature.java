package cyder.obj;

public class FileSignature {

    private int[] signature;
    private String extension;

    private FileSignature() {}

    public FileSignature(String extension, int[] signature) {
        this.extension = extension;
        this.signature = signature;
    }

    public int[] getSignature() {
        return signature;
    }

    public String getExtension() {
        return extension;
    }

    public void setSignature(int[] signature) {
        this.signature = signature;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }
}
