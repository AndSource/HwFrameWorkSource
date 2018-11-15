package org.bouncycastle.math.ec;

public class FixedPointPreCompInfo implements PreCompInfo {
    protected ECLookupTable lookupTable = null;
    protected ECPoint offset = null;
    protected ECPoint[] preComp = null;
    protected int width = -1;

    public ECLookupTable getLookupTable() {
        return this.lookupTable;
    }

    public ECPoint getOffset() {
        return this.offset;
    }

    public ECPoint[] getPreComp() {
        return this.preComp;
    }

    public int getWidth() {
        return this.width;
    }

    public void setLookupTable(ECLookupTable eCLookupTable) {
        this.lookupTable = eCLookupTable;
    }

    public void setOffset(ECPoint eCPoint) {
        this.offset = eCPoint;
    }

    public void setPreComp(ECPoint[] eCPointArr) {
        this.preComp = eCPointArr;
    }

    public void setWidth(int i) {
        this.width = i;
    }
}
