package org.softastur.asturianspellchecker;

import android.util.SparseArray;

/**
 * Created by Matthew Stephen Stuckwisch on 14 May 2015.
 */

public final class Words {
    private static int[] data;

    private Words() {

    }

    public final static void setData(int[] _data) {
        data = _data;
    }

    public final static boolean exists(int[] word) {
        return getExpansion(word) > 0;
    }


    public final static int getExpansion(int[] word) {
        int position = 0;

        traversal: // label for escape
        for(int i = 0, iMax = word.length; i < iMax; i++) {

            int letter = word[i];

            // The beginning of the branch indicates the number of offshoots
            int offsetMax = data[position];
            int offsetMin = 0;

            // Because we use a zero-index, add two.
            // (second element of the branch includes meta info)
            position+=2;

            // Binary search for the key
            while(offsetMax > offsetMin) {
                int check = (offsetMax + offsetMin) / 2;

                // Key values are even (0-count) indexes
                int compare = data[2 * check + position];

                if(compare == letter) {

                    // The odd bytes indicate where the offshoot branch is located
                    position = data[2 * check + position + 1];

                    continue traversal;
                }else{
                    if(compare > letter) {
                        offsetMax = check;
                    }else{
                        offsetMin = check+1;
                    }
                }
            }

            return 0;
        }

        // The second byte in a node indicates expansions, 0 is a special value indicating no
        // word exists here
        return data[position+1];
    }

}
