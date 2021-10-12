package cyder.genobjects;

public class BoundsString {
    private int width;
    private int height;
    private String text;

    public BoundsString(int width, int height, String text) {
        this.width = width;
        this.height = height;
        this.text = text;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
