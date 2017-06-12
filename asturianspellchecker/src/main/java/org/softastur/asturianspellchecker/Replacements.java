package org.softastur.asturianspellchecker;

import java.util.Arrays;

/**
 * Created by guifa on 5/21/15.
 */
public class Replacements {
    private static int[][] data;

    private Replacements() {}

    public static void setData(int[] _data) {
        int count = _data[0];
        data = new int[count][];
        int offset = 1;
        for(int i = 0; i < count; i++) {
            int size = _data[offset++];
            data[i] = new int[size];

            System.arraycopy(
                    _data,
                    offset,
                    data[i],
                    0,
                    size
            );

            offset += size;
        }
    }

    public static int[] get(int i) {
        return data[i];
    }
}
