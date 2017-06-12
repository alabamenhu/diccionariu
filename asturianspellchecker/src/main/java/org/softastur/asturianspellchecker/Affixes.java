package org.softastur.asturianspellchecker;

/**
 * Created by Matthew Stephen Stuckwisch on 5/21/15.
 */
public class Affixes {
    private static int[] prefixData;
    private static int[] suffixData;
    private static int[] finalSuffixData;
    private static int[][] replacements;

    // these are the
    public static final int ID = 0;
    public static final int REMOVE = 1;
    public static final int EXPAND = 2;
    public static final int MATCH = 3;
    public static final int REPLACE = 4;
    public static final int TYPE = 5;

    public static final int TYPE_PREFIX = 0;
    public static final int TYPE_SUFFIX = 1;
    
    public static final int[][] EMPTY_MORPH = new int[0][];

    private Affixes() {}

    public static final void setData(int[] _prefixData, int[] _suffixData, int[] _finalSuffixData, int[] _replacementData) {
        prefixData = _prefixData;
        suffixData = _suffixData;
        finalSuffixData = _finalSuffixData;
        setReplacementData(_replacementData);
    }

    private static void setReplacementData(int[] _data) {
        int count = _data[0];
        replacements = new int[count][];
        int offset = 1;
        for(int i = 0; i < count; i++) {
            int size = _data[offset++];
            replacements[i] = new int[size];

            System.arraycopy(
                    _data,
                    offset,
                    replacements[i],
                    0,
                    size
            );

            offset += size;
        }
    }


    public static final int[][] getPrefixMorphs(int[] word) {
        return getMorphs(word,prefixData);
    }
    public static final int[][] getSuffixMorphs(int[] word) {
        int[] flippedWord = new int[word.length];
        for(int i = 0; i < word.length; i++) {
            flippedWord[i] = word[word.length-1-i];
        }
        return getMorphs(flippedWord,suffixData);
    }
    public static final int[][] getFinalSuffixMorphs(int[] word) {
        int[] flippedWord = new int[word.length];
        for(int i = 0; i < word.length; i++) {
            flippedWord[i] = word[word.length-1-i];
        }
        return getMorphs(flippedWord,finalSuffixData);
    }


    private static final int[][] getMorphs(int[] word, int[] data) {
        int[][][] morphs = new int[word.length][][];

        int position = 0;
        boolean log = false;

        //if(log) System.out.println("ASTv Beginning prefix/suffix check, wordLength="+word.length);

        traversal: // label for escape
        for(int i = 0, iMax = word.length; i < iMax; i++) {

            int letter = word[i];
            //if(log) System.out.println("ASTv   - Searching for " + letter + " (" + Checker.intArrayToString(new int[]{letter}) + ")");

            // The beginning of the branch indicates the number of offshoots
            int offsetMax = data[position];
            int offsetMin = 0;
            //if(log) System.out.println("ASTv    - current offset="+position + "   offseMax=" + offsetMax + "   offsetMin="+offsetMin);

            // Because we use a zero-index, add two.
            // (second element of the branch includes meta info)
            position+=2;

            // Binary search for the key
            while(offsetMax > offsetMin) {

                // Start checking at the midpoint, the >>> 1 divides by 2, but is safe from overflow
                int check = (offsetMax + offsetMin) >>> 1;
                //if(log) System.out.println("ASTv     - Offset range: " + offsetMin + "-" + offsetMax + ", checking value at " + check + " (" + data[2 * check + position] + ")");

                // Key values are even (0-count) indexes
                int compare = data[2 * check + position];

                if(compare == letter) {

                    // The odd bytes indicate where the offshoot branch is located
                    position = data[2 * check + position + 1];
                    //if(log) System.out.println("ASTv     ! Found " + letter + " (" + Checker.intArrayToString(new int[]{letter}) + ") at offset " + check);

                    // TODO capture the morphs along the way
                    // The morphs are located in an array whose position is defined at nodeStart + 1
                    int morphPosition = data[position+1];

                    // The first number indicates the number of morphs that we have
                    int partialMorphCount = data[morphPosition++];
                    int[][] partialMorphs = new int[partialMorphCount][];

                    if(morphPosition == 1) {
                        //if(log) System.out.println("ASTv     > Morphs are found at 0?");
                        morphs[i] = EMPTY_MORPH;
                        continue traversal;
                    }
                    //if(log) System.out.println("ASTv     > There are " + partialMorphCount + " morphs here at " + (morphPosition*4));

                    // Because everything is a reference id, we can consistently count in groups of
                    // six to fill in the data about the morph
                    for(int j = 0; j < partialMorphCount; j++) {
                        partialMorphs[j] = new int[6];
                        System.arraycopy(data,morphPosition,partialMorphs[j],0,6);
                        morphPosition += 6;
                    }

                    // Attach the list of morphs to the main one
                    morphs[i] = partialMorphs;

                    continue traversal;
                }else{
                    if(compare > letter) {
                        offsetMax = check;
                    }else{
                        offsetMin = check+1;
                    }
                }
            }
            //if(log) System.out.println("ASTv   - Unsuccessful search for " + letter + "at node with index " + position);

            // Fill the rest of the morphs[][][] array with empty arrays
            for(int j = i; j<iMax; j++) {
                morphs[j] = EMPTY_MORPH;
            }

            break traversal;
        }

        // The second byte in a node indicates expansions, 0 is a special value indicating no
        // word exists here

        int total = 0;
        for(int i = 0; i< word.length; i++) { total += morphs[i].length; }

        int[][] result = new int[total][];

        for(int i = 0, added = 0; i < word.length; i++) {
            System.arraycopy(morphs[i],0,result,added,morphs[i].length);
            added += morphs[i].length;
        }

        return result;
    }

    public static final int[] remove(int[] word, int[] affix) {
        return affix[TYPE] == TYPE_PREFIX ? removePrefix(word, affix) : removeSuffix(word, affix);
    }

    public static final int[] removeSuffix(int[] word, int[] affix){
        final int[] removed = replacements[affix[REMOVE]]; // removed = what was originally there
        final int baseLength = word.length - replacements[affix[REPLACE]].length; // so "nadamos" (7) - "amos" (4) = "nad" 3
        final int removedLength = removed.length;

        final int[] recomposed = new int[baseLength + removedLength];
        System.arraycopy(word, 0, recomposed, 0, baseLength);
        System.arraycopy(removed, 0, recomposed, baseLength, removedLength);
        return recomposed;
    }

    public static final int[] removePrefix(int[] word, int[] affix) {
        final int[] removed = replacements[affix[REMOVE]];
        final int[] replaced = replacements[affix[REPLACE]];

        // PERBONO
        // PER = replaced
        // "" = removed
        //
        // "",0, 0,0
        // PERBONO, 3,  , 0, 7-

        final int[] recomposed = new int[word.length + removed.length - replaced.length];
        System.arraycopy(removed, 0, recomposed, 0, removed.length);
        System.arraycopy(word, replaced.length, recomposed, removed.length, word.length - replaced.length);
        return recomposed;
    }
}
