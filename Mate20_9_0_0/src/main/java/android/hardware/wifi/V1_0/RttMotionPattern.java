package android.hardware.wifi.V1_0;

import java.util.ArrayList;

public final class RttMotionPattern {
    public static final int EXPECTED = 1;
    public static final int NOT_EXPECTED = 0;
    public static final int UNKNOWN = 2;

    public static final String toString(int o) {
        if (o == 0) {
            return "NOT_EXPECTED";
        }
        if (o == 1) {
            return "EXPECTED";
        }
        if (o == 2) {
            return "UNKNOWN";
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("0x");
        stringBuilder.append(Integer.toHexString(o));
        return stringBuilder.toString();
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList();
        int flipped = 0;
        list.add("NOT_EXPECTED");
        if ((o & 1) == 1) {
            list.add("EXPECTED");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("UNKNOWN");
            flipped |= 2;
        }
        if (o != flipped) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("0x");
            stringBuilder.append(Integer.toHexString((~flipped) & o));
            list.add(stringBuilder.toString());
        }
        return String.join(" | ", list);
    }
}
