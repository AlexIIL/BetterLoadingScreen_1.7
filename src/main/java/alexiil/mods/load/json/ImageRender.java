package alexiil.mods.load.json;

import java.awt.Color;

public class ImageRender {
    public String resourceLocation;
    public EPosition positionType;
    public EType type;
    public Area texture;
    public Area position;
    public String colour;
    public String text;
    public String comment;

    public ImageRender(String resourceLocation, EPosition positionType, EType type, Area texture, Area position, String colour, String text,
            String comment) {
        this.resourceLocation = resourceLocation;
        this.positionType = positionType;
        this.type = type;
        this.texture = texture;
        this.position = position;
        this.colour = colour;
        this.text = text;
        this.comment = comment;
    }

    public ImageRender(String resourceLocation, EPosition positionType, EType type, Area texture, Area position) {
        this(resourceLocation, positionType, type, texture, position, null, null, "None");
    }

    public int transformX(int screenWidth) {
        if (position == null || positionType == null)
            return 0;
        int trueX = position.width;
        if (trueX == 0)
            trueX = screenWidth;
        return positionType.transformX(position.x, screenWidth - trueX);
    }

    public int transformY(int screenHeight) {
        if (position == null || positionType == null)
            return 0;
        return positionType.transformY(position.y, screenHeight - position.height);
    }

    public int getColour() {
        if (colour == null)
            return 0xFFFFFF;
        else {
            try {
                return Integer.parseInt(colour, 16);
            }
            catch (NumberFormatException nfe) {
                return 0xFFFFFF;
            }
        }
    }

    public void setColour(Color color) {
        String red = Integer.toHexString(color.getRed());
        if (red.length() == 1) {
            red = "0" + red;
        }
        String green = Integer.toHexString(color.getGreen());
        if (green.length() == 1) {
            green = "0" + green;
        }
        String blue = Integer.toHexString(color.getBlue());
        if (blue.length() == 1) {
            blue = "0" + blue;
        }
        colour = red + green + blue;
    }

    private float getColourPart(int bitStart) {
        return ((getColour() >> bitStart) & 0xFF) / 255F;
    }

    public float getRed() {
        return getColourPart(16);
    }

    public float getGreen() {
        return getColourPart(8);
    }

    public float getBlue() {
        return getColourPart(0);
    }
}
