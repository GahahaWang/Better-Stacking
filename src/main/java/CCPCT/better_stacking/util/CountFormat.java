package CCPCT.better_stacking.util;

/**
 * Formats cluster counts without going through {@link String#format}, whose locale and formatter
 * machinery dominated the cost of building a label.
 */
public final class CountFormat {

    public static final int MODE_PLAIN = 0;
    public static final int MODE_ENGINEERING = 1;
    public static final int MODE_MINECRAFT = 2;

    private static final String[] ENGINEERING_SUFFIXES = {"k", "M", "G", "T"};

    private static final long STACK = 64L;
    private static final long SHULKER_BOX = STACK * 27L;
    private static final long SHULKER_DOUBLE_CHEST = SHULKER_BOX * 54L;

    private CountFormat() {
    }

    public static String format(int value, int mode) {
        return switch (mode) {
            case MODE_ENGINEERING -> engineering(value);
            case MODE_MINECRAFT -> minecraft(value);
            default -> Integer.toString(value);
        };
    }

    /** 1500 -> "1.5k", 1000000 -> "1.0M". */
    public static String engineering(int value) {
        if (value < 1000) return Integer.toString(value);

        long unit = 1000L;
        int exponent = 0;
        while (exponent < ENGINEERING_SUFFIXES.length - 1 && value >= unit * 1000L) {
            unit *= 1000L;
            exponent++;
        }
        return oneDecimal(value, unit, ENGINEERING_SUFFIXES[exponent]);
    }

    /** 64 -> "1.0s" (stack), 1728 -> "1.0sb" (shulker box), 93312 -> "1.0sbc" (shulker double chest). */
    public static String minecraft(int value) {
        if (value >= SHULKER_DOUBLE_CHEST) return oneDecimal(value, SHULKER_DOUBLE_CHEST, "sbc");
        if (value >= SHULKER_BOX) return oneDecimal(value, SHULKER_BOX, "sb");
        if (value >= STACK) return oneDecimal(value, STACK, "s");
        return Integer.toString(value);
    }

    private static String oneDecimal(int value, long unit, String suffix) {
        long tenths = value * 10L / unit;
        return new StringBuilder(8)
                .append(tenths / 10)
                .append('.')
                .append(tenths % 10)
                .append(suffix)
                .toString();
    }
}
