package backend;

import java.util.Arrays;

/**
 * Created by virgil on 28.12.2015.
 */
public class Commit {

    private byte[] bytes;

    public Commit(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] getBytes() {
        return this.bytes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Commit commit = (Commit) o;

        return Arrays.equals(bytes, commit.bytes);

    }

    @Override
    public int hashCode() {
        return bytes != null ? Arrays.hashCode(bytes) : 0;
    }

}
