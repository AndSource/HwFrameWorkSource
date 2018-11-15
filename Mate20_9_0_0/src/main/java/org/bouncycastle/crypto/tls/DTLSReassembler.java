package org.bouncycastle.crypto.tls;

import java.util.Vector;

class DTLSReassembler {
    private byte[] body;
    private Vector missing = new Vector();
    private short msg_type;

    private static class Range {
        private int end;
        private int start;

        Range(int i, int i2) {
            this.start = i;
            this.end = i2;
        }

        public int getEnd() {
            return this.end;
        }

        public int getStart() {
            return this.start;
        }

        public void setEnd(int i) {
            this.end = i;
        }

        public void setStart(int i) {
            this.start = i;
        }
    }

    DTLSReassembler(short s, int i) {
        this.msg_type = s;
        this.body = new byte[i];
        this.missing.addElement(new Range(0, i));
    }

    void contributeFragment(short s, int i, byte[] bArr, int i2, int i3, int i4) {
        int i5 = i3 + i4;
        if (this.msg_type == s && this.body.length == i && i5 <= i) {
            int i6 = 0;
            if (i4 == 0) {
                if (i3 == 0 && !this.missing.isEmpty() && ((Range) this.missing.firstElement()).getEnd() == 0) {
                    this.missing.removeElementAt(0);
                }
                return;
            }
            while (i6 < this.missing.size()) {
                Range range = (Range) this.missing.elementAt(i6);
                if (range.getStart() < i5) {
                    if (range.getEnd() > i3) {
                        i4 = Math.max(range.getStart(), i3);
                        int min = Math.min(range.getEnd(), i5);
                        System.arraycopy(bArr, (i2 + i4) - i3, this.body, i4, min - i4);
                        if (i4 != range.getStart()) {
                            if (min != range.getEnd()) {
                                i6++;
                                this.missing.insertElementAt(new Range(min, range.getEnd()), i6);
                            }
                            range.setEnd(i4);
                        } else if (min == range.getEnd()) {
                            i4 = i6 - 1;
                            this.missing.removeElementAt(i6);
                            i6 = i4;
                        } else {
                            range.setStart(min);
                        }
                    }
                    i6++;
                } else {
                    return;
                }
            }
        }
    }

    byte[] getBodyIfComplete() {
        return this.missing.isEmpty() ? this.body : null;
    }

    short getMsgType() {
        return this.msg_type;
    }

    void reset() {
        this.missing.removeAllElements();
        this.missing.addElement(new Range(0, this.body.length));
    }
}
