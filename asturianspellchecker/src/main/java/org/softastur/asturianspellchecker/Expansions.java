package org.softastur.asturianspellchecker;

/**
 * Created by guifa on 5/20/15.
 */
public class Expansions {

    private static int[] data;
    /*
        Data structure:

        First elements point to the location of each expansion datum

        Expansion datum format is
          offset 0   :  meta information
          offset 1   :  affix count
          offset 2...:  affixes
     */

    private Expansions() {

    }

    public final static void setData(int[] _data) {
        data = _data;
    }

    public final static boolean needsAffix(int i) {
        return (data[data[i]] & 0b0001) == 0b0001;
    }
    public final static boolean forbidden(int i) {
        return (data[data[i]] & 0b0010) == 0b0010;
    }
    public final static boolean keepsCase(int i) {
        return (data[data[i]] & 0b0100) == 0b0100;
    }

    public final static int[] getAffixIds(int i) {
        int offset = data[i];
        int[] result = new int[offset+1];

        System.arraycopy(
                data,
                data[offset+2],
                result,
                0,
                data[offset+1]
        );

        return result;
    }

    public static boolean isPossible(int baseExpansionGroup, int expandedAffixId) {

        int offset = data[baseExpansionGroup]+1;
        int maxOffset = offset+data[offset];
        while(offset++ < maxOffset && offset < data.length) {
            if(data[offset] == expandedAffixId) return true;
        }

        return false;
    }
}
