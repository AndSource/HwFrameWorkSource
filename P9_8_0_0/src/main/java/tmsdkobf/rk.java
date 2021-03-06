package tmsdkobf;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import tmsdk.common.TMSDKContext;

public class rk {
    private static List<String> Ph = new ArrayList();
    private static List<String> Pi = new ArrayList();
    public static int Pq = 86400000;
    private static List<String> Pr = new ArrayList();

    public static String E(String -l_2_R, String -l_3_R) {
        Object -l_2_R2 = -l_2_R;
        while (-l_2_R2.startsWith("/")) {
            -l_2_R2 = -l_2_R2.substring(1);
        }
        Object -l_3_R2 = -l_3_R;
        while (-l_3_R2.startsWith("/")) {
            -l_3_R2 = -l_3_R2.substring(1);
        }
        Object -l_4_R = -l_2_R2.split("/");
        Object -l_5_R = -l_3_R2.split("/");
        int -l_6_I = 0;
        if (-l_4_R.length < -l_5_R.length) {
            -l_4_R = -l_5_R;
            -l_5_R = (String[]) -l_4_R.clone();
            Object -l_8_R = -l_2_R2;
            -l_2_R2 = -l_3_R2;
            -l_3_R2 = -l_8_R;
            -l_6_I = 1;
        }
        int -l_7_I = 0;
        int -l_8_I = 0;
        int -l_9_I = 0;
        while (-l_9_I < -l_4_R.length) {
            if (-l_4_R[-l_9_I].equals(-l_5_R[0])) {
                -l_8_I++;
                int -l_10_I = 1;
                while (-l_10_I < -l_5_R.length) {
                    if (-l_9_I + -l_10_I >= -l_4_R.length || !-l_4_R[-l_9_I + -l_10_I].equals(-l_5_R[-l_10_I])) {
                        -l_8_I = 0;
                        break;
                    }
                    -l_8_I++;
                    -l_10_I++;
                }
                if (-l_8_I == -l_5_R.length) {
                    -l_7_I = 1;
                    break;
                }
            }
            -l_9_I++;
        }
        if (-l_7_I == 0) {
            return null;
        }
        return -l_6_I == 0 ? -l_2_R : -l_3_R;
    }

    public static void E(List<String> list) {
        Pi.clear();
        Pi.addAll(list);
    }

    private static void F(List<String> list) {
        for (String -l_2_R : Pi) {
            if (list.contains(-l_2_R)) {
                list.remove(-l_2_R);
            }
        }
    }

    private static List<String> G(List<String> list) {
        int -l_1_I = 0;
        for (int -l_2_I = 0; -l_2_I < list.size(); -l_2_I++) {
            for (int -l_3_I = -l_2_I + 1; -l_3_I < list.size(); -l_3_I++) {
                Object -l_4_R = E((String) list.get(-l_2_I), (String) list.get(-l_3_I));
                if (-l_4_R != null) {
                    list.remove(-l_4_R);
                    -l_1_I = 1;
                    break;
                }
            }
            if (-l_1_I != 0) {
                return G(list);
            }
        }
        return list;
    }

    public static String a(String str, List<String> list) {
        if (str == null || list == null || list.isEmpty()) {
            return str;
        }
        for (String -l_3_R : list) {
            if (str.startsWith(-l_3_R)) {
                return str.substring(-l_3_R.length());
            }
        }
        return str;
    }

    public static void appendCustomSdcardRoots(String str) {
        if (str != null) {
            Ph.add(str);
        }
    }

    public static void clearCustomSdcardRoots() {
        Ph.clear();
    }

    public static void dl(String str) {
        if (str != null) {
            Pr.add(str);
        }
    }

    public static List<String> jZ() {
        Object -l_0_R = lu.s(TMSDKContext.getApplicaionContext());
        if (-l_0_R.size() <= 0) {
            -l_0_R = G(kn.cM());
            if (-l_0_R == null) {
                return null;
            }
            -l_0_R.addAll(Ph);
            F(-l_0_R);
            return -l_0_R;
        }
        -l_0_R.addAll(Ph);
        F(-l_0_R);
        return -l_0_R;
    }

    public static List<String> ke() {
        Object -l_0_R = lu.s(TMSDKContext.getApplicaionContext());
        if (-l_0_R.size() > 0) {
            return -l_0_R;
        }
        -l_0_R = G(kn.cM());
        return -l_0_R != null ? -l_0_R : null;
    }

    public static List<String> kh() {
        return Pr;
    }

    public static List<File> ki() {
        Object<String> -l_0_R = ke();
        Object -l_1_R = new ArrayList();
        if (-l_0_R != null) {
            for (String -l_3_R : -l_0_R) {
                Object -l_5_R = new File(-l_3_R).listFiles();
                if (-l_5_R != null) {
                    -l_1_R.addAll(Arrays.asList(-l_5_R));
                }
            }
        }
        return -l_1_R;
    }
}
