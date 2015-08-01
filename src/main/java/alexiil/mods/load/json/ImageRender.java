package alexiil.mods.load.json;

public class ImageRender {
    public final String resourceLocation;
    public final EPosition positionType;
    public final EType type;
    public final Area texture;
    public final Area position;
    public final String colour;
    public final String text;

    public ImageRender(String resourceLocation, EPosition positionType, EType type, Area texture, Area position, String colour, String text) {
        this.resourceLocation = resourceLocation;
        this.positionType = positionType;
        this.type = type;
        this.texture = texture;
        this.position = position;
        this.colour = colour;
        this.text = text;
    }

    public ImageRender(String resourceLocation, EPosition positionType, EType type, Area texture, Area position) {
        this(resourceLocation, positionType, type, texture, position, null, null);
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
