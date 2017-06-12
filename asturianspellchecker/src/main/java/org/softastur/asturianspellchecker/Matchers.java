package org.softastur.asturianspellchecker;

import java.util.Arrays;

/**
 * Created by Matthew Stephen Stuckwisch on 5/21/15.
 */
public class Matchers {
    private static volatile long[][] data;
    private static final long MATCH_BASE = 0x8000_0000_0000_0000L; // only the first bit set, ready for shifting

    public static final int START_GROUP = 73;
    public static final int END_GROUP = 74;
    public static final int NEGATOR = 75;
    public static final int WILDCARD = 76;

    private static final int CAPITALIZATION_OFFSET = 37; // lc(x) == x % CAP…
    private static final long MATCH_WILDCARD = 0xffff_ffff_ffff_ffffL; // with all bits set, matches everything
    private static final long MATCH_BLANK = 0x0000_0000_0000_0000L;

    private Matchers() {}

    public static final void setData(int[] _data) {
        int count = _data[0];
        data = new long[count][];

        int offset = 1;

        for(int i = 0; i < count; i++) {
            int size = _data[offset++];
            data[i] = createMatchers(
                    Arrays.copyOfRange(
                            _data,
                            offset,
                            offset+size
                    )
            );
            offset += size;
        }
    }

    public static final boolean prefixMatch(int matcher, int[] word) {
        long[] matchers = data[matcher];
        if(word.length < matchers.length) return false;

        for(int i = 0; i < matchers.length; i++) {
            if((matchers[i] & (MATCH_BASE >>> word[i])) == 0) return false;
        }
        return true;
    }

    public static final boolean suffixMatch(int matcher, int[] word) {
        long[] matchers = data[matcher];
        if(word.length < matchers.length) return false;

        for(int i = 0, iMax = matchers.length, differential = word.length - iMax; i < iMax; i++) {
            if( (matchers[i] & (MATCH_BASE >>> word[i+differential])) == 0) return false;
        }
        return true;
    }

    /**
     * Returns an array of longs that can be used to check if a
     * a series of byte matches some set of conditions.  It produces a single
     * long for each character not in a group.  Grouped characters
     * are passed collectively to the instantiators of the appopriate group matcher.
     * <p>
     * This method always uses three special values (69-71) for grouping.  If you
     * need to include them, you must create the objects manually rather
     * than use this helper function.
     *
     * @param  ints  an array of bytes to be processed
     * @return      the appropriate FastMatcher object based on the input
     */
    private final static long[] createMatchers(int[] ints) {

        long[] matchers = new long[ints.length];
        int matcherCount = 0;

        for(int i = 0, l = ints.length; i < l; i++) {
            int b = ints[i];
            if(b==START_GROUP) {
                boolean isNegative = false;
                if (ints[i + 1] == NEGATOR) {
                    isNegative = true;
                    i++;
                }

                i++;

                int groupLength = 0;
                while(ints[i+groupLength] != END_GROUP) {
                    groupLength++;
                }

                int[] group = new int[groupLength];
                for(int j = 0; j < groupLength; group[j] = ints[i+j++] % CAPITALIZATION_OFFSET);
                i += groupLength;

                matchers[matcherCount++] = groupMask(group, isNegative);
            }else if (b==WILDCARD){
                matchers[matcherCount++] = MATCH_WILDCARD;
            }else{
                matchers[matcherCount++] = MATCH_BASE >>> (b % CAPITALIZATION_OFFSET);
            }
        }
        return Arrays.copyOf(matchers, matcherCount);
    }

    private final static long groupMask(int[] i,boolean isNegative) {
        long mask = MATCH_BLANK;
        for (int j = 0; j < i.length; j++) {
            mask |= MATCH_BASE >>> i[j];
        }
        return isNegative ? ~mask : mask;
    }

}
