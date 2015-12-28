package utils;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by virgil on 28.12.2015.
 */
public class Payment {

    private byte[] bytes;

    public Payment(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public Payword getPayword() {
        byte[] paywordBytes = new byte[20];

        for (int i = 0; i < 20; ++i)
            paywordBytes[i] = this.bytes[i];

        Payword payword = new Payword(paywordBytes);

        return payword;
    }

    public int getPaywordNo() {
        byte[] paywordNoBytes = new byte[4];

        for (int i = 20; i < 24; ++i)
            paywordNoBytes[i - 20] = this.bytes[i];

        return ByteBuffer.wrap(paywordNoBytes).getInt();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Payment payment = (Payment) o;

        return Arrays.equals(bytes, payment.bytes);

    }

    @Override
    public int hashCode() {
        return bytes != null ? Arrays.hashCode(bytes) : 0;
    }

}
