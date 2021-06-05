package cyder.exception;

public class CyderException extends Exception  {
    public CyderException(String str) {
        super(str);
    }

    @Override
    public String toString() {
        return "CyderException object, hash=" + this.hashCode();
    }
}
