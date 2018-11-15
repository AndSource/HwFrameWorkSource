package org.bouncycastle.math.ec.custom.sec;

import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.ECPoint.AbstractFp;
import org.bouncycastle.math.raw.Nat;
import org.bouncycastle.math.raw.Nat256;

public class SecP256R1Point extends AbstractFp {
    public SecP256R1Point(ECCurve eCCurve, ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2) {
        this(eCCurve, eCFieldElement, eCFieldElement2, false);
    }

    public SecP256R1Point(ECCurve eCCurve, ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2, boolean z) {
        super(eCCurve, eCFieldElement, eCFieldElement2);
        Object obj = null;
        Object obj2 = eCFieldElement == null ? 1 : null;
        if (eCFieldElement2 == null) {
            obj = 1;
        }
        if (obj2 == obj) {
            this.withCompression = z;
            return;
        }
        throw new IllegalArgumentException("Exactly one of the field elements is null");
    }

    SecP256R1Point(ECCurve eCCurve, ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2, ECFieldElement[] eCFieldElementArr, boolean z) {
        super(eCCurve, eCFieldElement, eCFieldElement2, eCFieldElementArr);
        this.withCompression = z;
    }

    public ECPoint add(ECPoint eCPoint) {
        if (isInfinity()) {
            return eCPoint;
        }
        if (eCPoint.isInfinity()) {
            return this;
        }
        if (this == eCPoint) {
            return twice();
        }
        int[] iArr;
        int[] iArr2;
        int[] iArr3;
        int[] iArr4;
        ECCurve curve = getCurve();
        SecP256R1FieldElement secP256R1FieldElement = (SecP256R1FieldElement) this.x;
        SecP256R1FieldElement secP256R1FieldElement2 = (SecP256R1FieldElement) this.y;
        SecP256R1FieldElement secP256R1FieldElement3 = (SecP256R1FieldElement) eCPoint.getXCoord();
        SecP256R1FieldElement secP256R1FieldElement4 = (SecP256R1FieldElement) eCPoint.getYCoord();
        SecP256R1FieldElement secP256R1FieldElement5 = (SecP256R1FieldElement) this.zs[0];
        SecP256R1FieldElement secP256R1FieldElement6 = (SecP256R1FieldElement) eCPoint.getZCoord(0);
        int[] createExt = Nat256.createExt();
        int[] create = Nat256.create();
        int[] create2 = Nat256.create();
        int[] create3 = Nat256.create();
        boolean isOne = secP256R1FieldElement5.isOne();
        if (isOne) {
            iArr = secP256R1FieldElement3.x;
            iArr2 = secP256R1FieldElement4.x;
        } else {
            SecP256R1Field.square(secP256R1FieldElement5.x, create2);
            SecP256R1Field.multiply(create2, secP256R1FieldElement3.x, create);
            SecP256R1Field.multiply(create2, secP256R1FieldElement5.x, create2);
            SecP256R1Field.multiply(create2, secP256R1FieldElement4.x, create2);
            iArr = create;
            iArr2 = create2;
        }
        boolean isOne2 = secP256R1FieldElement6.isOne();
        if (isOne2) {
            iArr3 = secP256R1FieldElement.x;
            iArr4 = secP256R1FieldElement2.x;
        } else {
            SecP256R1Field.square(secP256R1FieldElement6.x, create3);
            SecP256R1Field.multiply(create3, secP256R1FieldElement.x, createExt);
            SecP256R1Field.multiply(create3, secP256R1FieldElement6.x, create3);
            SecP256R1Field.multiply(create3, secP256R1FieldElement2.x, create3);
            iArr3 = createExt;
            iArr4 = create3;
        }
        int[] create4 = Nat256.create();
        SecP256R1Field.subtract(iArr3, iArr, create4);
        SecP256R1Field.subtract(iArr4, iArr2, create);
        if (Nat256.isZero(create4)) {
            return Nat256.isZero(create) ? twice() : curve.getInfinity();
        } else {
            SecP256R1Field.square(create4, create2);
            iArr = Nat256.create();
            SecP256R1Field.multiply(create2, create4, iArr);
            SecP256R1Field.multiply(create2, iArr3, create2);
            SecP256R1Field.negate(iArr, iArr);
            Nat256.mul(iArr4, iArr, createExt);
            SecP256R1Field.reduce32(Nat256.addBothTo(create2, create2, iArr), iArr);
            ECFieldElement secP256R1FieldElement7 = new SecP256R1FieldElement(create3);
            SecP256R1Field.square(create, secP256R1FieldElement7.x);
            SecP256R1Field.subtract(secP256R1FieldElement7.x, iArr, secP256R1FieldElement7.x);
            ECFieldElement secP256R1FieldElement8 = new SecP256R1FieldElement(iArr);
            SecP256R1Field.subtract(create2, secP256R1FieldElement7.x, secP256R1FieldElement8.x);
            SecP256R1Field.multiplyAddToExt(secP256R1FieldElement8.x, create, createExt);
            SecP256R1Field.reduce(createExt, secP256R1FieldElement8.x);
            secP256R1FieldElement = new SecP256R1FieldElement(create4);
            if (!isOne) {
                SecP256R1Field.multiply(secP256R1FieldElement.x, secP256R1FieldElement5.x, secP256R1FieldElement.x);
            }
            if (!isOne2) {
                SecP256R1Field.multiply(secP256R1FieldElement.x, secP256R1FieldElement6.x, secP256R1FieldElement.x);
            }
            return new SecP256R1Point(curve, secP256R1FieldElement7, secP256R1FieldElement8, new ECFieldElement[]{secP256R1FieldElement}, this.withCompression);
        }
    }

    protected ECPoint detach() {
        return new SecP256R1Point(null, getAffineXCoord(), getAffineYCoord());
    }

    public ECPoint negate() {
        return isInfinity() ? this : new SecP256R1Point(this.curve, this.x, this.y.negate(), this.zs, this.withCompression);
    }

    public ECPoint threeTimes() {
        return (isInfinity() || this.y.isZero()) ? this : twice().add(this);
    }

    public ECPoint twice() {
        if (isInfinity()) {
            return this;
        }
        ECCurve curve = getCurve();
        SecP256R1FieldElement secP256R1FieldElement = (SecP256R1FieldElement) this.y;
        if (secP256R1FieldElement.isZero()) {
            return curve.getInfinity();
        }
        SecP256R1FieldElement secP256R1FieldElement2 = (SecP256R1FieldElement) this.x;
        SecP256R1FieldElement secP256R1FieldElement3 = (SecP256R1FieldElement) this.zs[0];
        int[] create = Nat256.create();
        int[] create2 = Nat256.create();
        int[] create3 = Nat256.create();
        SecP256R1Field.square(secP256R1FieldElement.x, create3);
        int[] create4 = Nat256.create();
        SecP256R1Field.square(create3, create4);
        boolean isOne = secP256R1FieldElement3.isOne();
        int[] iArr = secP256R1FieldElement3.x;
        if (!isOne) {
            SecP256R1Field.square(secP256R1FieldElement3.x, create2);
            iArr = create2;
        }
        SecP256R1Field.subtract(secP256R1FieldElement2.x, iArr, create);
        SecP256R1Field.add(secP256R1FieldElement2.x, iArr, create2);
        SecP256R1Field.multiply(create2, create, create2);
        SecP256R1Field.reduce32(Nat256.addBothTo(create2, create2, create2), create2);
        SecP256R1Field.multiply(create3, secP256R1FieldElement2.x, create3);
        SecP256R1Field.reduce32(Nat.shiftUpBits(8, create3, 2, 0), create3);
        SecP256R1Field.reduce32(Nat.shiftUpBits(8, create4, 3, 0, create), create);
        ECFieldElement secP256R1FieldElement4 = new SecP256R1FieldElement(create4);
        SecP256R1Field.square(create2, secP256R1FieldElement4.x);
        SecP256R1Field.subtract(secP256R1FieldElement4.x, create3, secP256R1FieldElement4.x);
        SecP256R1Field.subtract(secP256R1FieldElement4.x, create3, secP256R1FieldElement4.x);
        ECFieldElement secP256R1FieldElement5 = new SecP256R1FieldElement(create3);
        SecP256R1Field.subtract(create3, secP256R1FieldElement4.x, secP256R1FieldElement5.x);
        SecP256R1Field.multiply(secP256R1FieldElement5.x, create2, secP256R1FieldElement5.x);
        SecP256R1Field.subtract(secP256R1FieldElement5.x, create, secP256R1FieldElement5.x);
        secP256R1FieldElement2 = new SecP256R1FieldElement(create2);
        SecP256R1Field.twice(secP256R1FieldElement.x, secP256R1FieldElement2.x);
        if (!isOne) {
            SecP256R1Field.multiply(secP256R1FieldElement2.x, secP256R1FieldElement3.x, secP256R1FieldElement2.x);
        }
        return new SecP256R1Point(curve, secP256R1FieldElement4, secP256R1FieldElement5, new ECFieldElement[]{secP256R1FieldElement2}, this.withCompression);
    }

    public ECPoint twicePlus(ECPoint eCPoint) {
        return this == eCPoint ? threeTimes() : isInfinity() ? eCPoint : eCPoint.isInfinity() ? twice() : this.y.isZero() ? eCPoint : twice().add(eCPoint);
    }
}
