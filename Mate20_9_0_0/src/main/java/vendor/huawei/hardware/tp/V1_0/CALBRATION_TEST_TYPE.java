package vendor.huawei.hardware.tp.V1_0;

import java.util.ArrayList;

public final class CALBRATION_TEST_TYPE {
    public static final int TOUCHSCREEN_CALIBRATION_TEST = 0;
    public static final int TOUCHSCREEN_MAXIMUM_TEST = 1;

    public static final String toString(int o) {
        if (o == 0) {
            return "TOUCHSCREEN_CALIBRATION_TEST";
        }
        if (o == 1) {
            return "TOUCHSCREEN_MAXIMUM_TEST";
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("0x");
        stringBuilder.append(Integer.toHexString(o));
        return stringBuilder.toString();
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList();
        int flipped = 0;
        list.add("TOUCHSCREEN_CALIBRATION_TEST");
        if ((o & 1) == 1) {
            list.add("TOUCHSCREEN_MAXIMUM_TEST");
            flipped = 0 | 1;
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
