package cyder.parsers;

import java.awt.*;

/**
 * A response for a complementary color.
 */
public class ColorResponse {
    /**
     * The complementary color in the form "r,g,b".
     */
    String color;

    /**
     * Constructs a new color response.
     *
     * @param color the color of the response
     */
    public ColorResponse(String color) {
        this.color = color;
    }

    /**
     * Returns the color string of the response.
     *
     * @return the color string of the response
     */
    public String getColor() {
        return color;
    }

    /**
     * Sets the color string of the response.
     *
     * @param color the color string of the response
     */
    public void setColor(String color) {
        this.color = color;
    }

    /**
     * Returns the color object represented by the internal color response.
     *
     * @return the color object represented by the internal color response
     */
    public Color generateColor() {
        String[] parts = color.split(",");
        return new Color(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
    }
}
