package utils;

import java.util.Arrays;

/**
 * Created by virgil on 28.12.2015.
 */
public class Payword {

    private byte[] bytes;

    public Payword(byte[] bytes) {
        this.bytes = Crypto.hashMessage(bytes);
    }

    public Payword(Payword payword) {
        this.bytes = Crypto.hashMessage(payword.getBytes());
    }

    public byte[] getBytes() {
        return this.bytes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Payword payword = (Payword) o;

        return Arrays.equals(bytes, payword.bytes);

    }

    @Override
    public int hashCode() {
        return bytes != null ? Arrays.hashCode(bytes) : 0;
    }

}
