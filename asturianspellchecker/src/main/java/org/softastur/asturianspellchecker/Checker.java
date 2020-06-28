package org.softastur.asturianspellchecker;

import android.content.Context;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Arrays;


/**
 * Created by Matthew Stephen Stuckwisch on 11 May 2015.
 */
public class Checker {
    private static boolean dataLoaded = false;
    private static final int PREFIX_RECURSION_DEPTH = 1;
    private static final int SUFFIX_RECURSION_DEPTH = 2;
    private static final RootResult blankRootResult = new RootResult();
    private static final int LETTER_COUNT = 37;
    private static final int TRIGRAM_OFFSET = LETTER_COUNT*LETTER_COUNT;
    private static final int TRIGRAM_BASE = 0x8000_0000;
    private static volatile int[] booleanTrigram;
    private static volatile boolean[] booleanTrigramNew;

    private static byte[] ascii = new byte[]{-128, 37, 39, 40, 41, 42, 44, 45, 46, 48, 50, 51, 52,
            //                                  @,  A,  B,  C,  D,  E,  F,  G,  H,  I,  J,  K,  L,
            54, 55, 57, 59, 60, 61, 62, 63, 64, 67, 68, 69, 70, 71, -128, -128, -128, -128, -128,
            //M, N,  O,  P,  Q,  R,  S,  T,  U,  V,  W,  X,  Y,  Z,    [,   bs,    ],    ^,    _,
            -128, 0, 2, 3, 4, 5, 7, 8, 9, 11, 13, 14, 15, 17, 18, 20, 22, 23, 24, 25, 26, 27, 30,
            // ´. a, b, c, d, e, f, g, h,  i,  j,  k,  l,  m,  n,  o,  p,  q,  r,  s,  t,  u,  v,
            31, 32, 33, 34, -128, -128, -128, -128, -128
            //w, x,  y,  z,
    };
    public static final String[] letters = {"a", "a\u0301", "b", "c", "d", "e", "e\u0301", "f", "g",
            "h", "h\u0323", "i", "i\u0301", "j", "k", "l", "l\u0323", "m", "n", "n\u0303", "o",
            "o\u0301", "p", "q", "r", "s", "t", "u", "u\u0301", "u\u0308", "v", "w", "x", "y", "z", "-", "\u2019",
            "A", "A\u0301", "B", "C", "D", "E", "E\u0301", "F", "G", "H", "H\u0323", "I", "I\u0301",
            "J", "K", "L", "L\u0323", "M", "N", "N\u0303", "O", "O\u0301", "P", "Q", "R", "S", "T",
            "U", "U\u0301", "U\u0308", "V", "W", "X", "Y", "Z", "\u0301", "[", "]",
            "^", "."};

    Context context;

    public Checker(Context _context) {
        context = _context;

        if(!dataLoaded) {
            loadData();
        }
    }

    private void loadData() {
        long startTime = System.currentTimeMillis();
        //loadReplacementsFile();
        loadMatchersFile();
        loadExpansionFile();
        loadAffixFile();
        loadDictionaryFile();
        loadBooleanTrigramFile();
        dataLoaded = true;
        long endTime = System.currentTimeMillis();
        System.out.println("ASTv loaded data in " + (endTime - startTime) + "ms");

/*        startTime = System.currentTimeMillis();
        String[] a = getSuggestions("comprarom", 0);
        endTime = System.currentTimeMillis();
        System.out.println("ASTv ------ found the following suggestions (1x) in " + (endTime - startTime) + "ms");
        for(int i = 0; i < a.length; i++) {
            System.out.println("ASTv   - " + a[i]);
        }

        startTime = System.currentTimeMillis();
        a = getSuggestions("comprarom", 1);
        endTime = System.currentTimeMillis();
        System.out.println("ASTv ------ found the following suggestions (2x) in " + (endTime - startTime) + "ms");
        for(int i = 0; i < a.length; i++) {
            System.out.println("ASTv   - " + a[i]);
        }

        startTime = System.currentTimeMillis();
        a = getSuggestions("compra-yos", 1);
        endTime = System.currentTimeMillis();
        System.out.println("ASTvv ------ found the following suggestions (2x) in " + (endTime - startTime) + "ms");
        for(int i = 0; i < a.length; i++) {
            System.out.println("ASTvv   - " + a[i]);
        }
*/
        System.out.println("ASTv 'compremos-yos' " + (getRoot(textToIntArray("compremos-yos")) ? "exists" : "does not exist"));

//        System.out.println("ASTv 'compra-yos' " + (getRoot(textToIntArray("compra-yos")) ? "exists" : "does not exist"));
    }
    private void loadReplacementsFile() {
        byte[] file = QuickFileReader.readRawResource(context, R.raw.efficient_hardcoded);
        IntBuffer ib = ByteBuffer.wrap(file).order(ByteOrder.BIG_ENDIAN).asIntBuffer();

        int[] data = new int[ib.limit()];
        ib.get(data);

        Replacements.setData(data);
    }
    private void loadMatchersFile() {
        byte[] file = QuickFileReader.readRawResource(context, R.raw.efficient_matchers);
        IntBuffer ib = ByteBuffer.wrap(file).order(ByteOrder.BIG_ENDIAN).asIntBuffer();

        int[] data = new int[ib.limit()];
        ib.get(data);

        Matchers.setData(data);
    }
    private void loadExpansionFile() {
        byte[] file = QuickFileReader.readRawResource(context, R.raw.efficient_expansions);
        IntBuffer ib = ByteBuffer.wrap(file).order(ByteOrder.BIG_ENDIAN).asIntBuffer();

        int[] data = new int[ib.limit()];
        ib.get(data);

        Expansions.setData(data);
    }
    private void loadAffixFile() {
        byte[] suffixFile = QuickFileReader.readRawResource(context, R.raw.efficient_suffixes);
        byte[] finalSuffixFile = QuickFileReader.readRawResource(context, R.raw.efficient_suffixes_second_order);
        byte[] prefixFile = QuickFileReader.readRawResource(context, R.raw.efficient_prefixes);
        byte[] replacementFile = QuickFileReader.readRawResource(context, R.raw.efficient_hardcoded);
        IntBuffer suffixIb = ByteBuffer.wrap(suffixFile).order(ByteOrder.BIG_ENDIAN).asIntBuffer();
        IntBuffer finalSuffixIb = ByteBuffer.wrap(finalSuffixFile).order(ByteOrder.BIG_ENDIAN).asIntBuffer();
        IntBuffer prefixIb = ByteBuffer.wrap(prefixFile).order(ByteOrder.BIG_ENDIAN).asIntBuffer();
        IntBuffer replacementIb = ByteBuffer.wrap(replacementFile).order(ByteOrder.BIG_ENDIAN).asIntBuffer();

        int[] suffixData = new int[suffixIb.limit()];
        int[] finalSuffixData = new int[finalSuffixIb.limit()];
        int[] prefixData = new int[prefixIb.limit()];
        int[] replacementData = new int[replacementIb.limit()];
        suffixIb.get(suffixData);
        finalSuffixIb.get(finalSuffixData);
        prefixIb.get(prefixData);
        replacementIb.get(replacementData);

        Affixes.setData(prefixData, suffixData, finalSuffixData, replacementData);
    }
    private void loadBooleanTrigramFile() {
        byte[] file = QuickFileReader.readRawResource(context, R.raw.boolean_trigram);
        IntBuffer ib = ByteBuffer.wrap(file).order(ByteOrder.BIG_ENDIAN).asIntBuffer();

        int[] data = new int[ib.limit()];
        ib.get(data);

        int totalTrue = 0;
        booleanTrigram = data;
        int length = booleanTrigram.length;
        booleanTrigramNew = new boolean[length * 32];
        for(int i = 0, iMax = length; i < iMax; i++) {
            for(int j = 0; j < 32; j++) {
                booleanTrigramNew[i*32+j] = ((booleanTrigram[i] >>> j) & 0x1) == 0;
            }
        }
        for(int i = 0, iMax = length * 32; i < iMax; totalTrue += booleanTrigramNew[i++]?1:0) {}
        System.out.println("ASTv there are " + totalTrue + " trues out of " + (length * 32) + " trigrams");
    }
    private void loadDictionaryFile() {
        byte[] file = QuickFileReader.readRawResource(context, R.raw.efficient_dictionary);
        IntBuffer ib = ByteBuffer.wrap(file).order(ByteOrder.BIG_ENDIAN).asIntBuffer();

        int[] data = new int[ib.limit()];
        ib.get(data);

        Words.setData(data);
    }

    public static int[] textToIntArray(String text) {
        char[] word = text.toCharArray();
        int elementCount = 0;
        int[] result = new int[word.length];

        for(int i = 0, iMax = word.length, val; i < iMax; i++) {
            val = word[i];
            if(val > 63 && val < 128) {
                result[elementCount++] = ascii[val - 64];
            }else{
                switch(val) {
                    case 0x0301: // acute accent
                        switch(result[elementCount-1]) {
                            case 34:case 39:case 47:case 54:case 61:case 0:case 5:case 11:case 20:case 27:
                                // add one to aeiouAEIOU
                                result[elementCount-1]++;
                                break;
                            default: result[elementCount-1] = -128;
                        }
                        break;
                    case 0x0303: // tilde
                        switch(result[elementCount-1]) {
                            case 52:case 18:
                                // add one to nN
                                result[elementCount - 1]++;
                                break;
                            default: result[elementCount-1] = -128;
                        }
                        break;
                    case 0x0308: // diaeresis
                        switch(result[elementCount-1]) {
                            case 52:case 18:
                                // add two to uU
                                result[elementCount - 1] += 2;
                                break;
                            default: result[elementCount-1] = -128;
                        }
                        break;
                    case 0x0323: // underdot
                        switch(result[elementCount-1]) {
                            case 43:case 49:case 9:case 15:
                                // add one to hlHL
                                result[elementCount - 1]++;
                                break;
                            default: result[elementCount-1] = -128;
                        }
                        break;
                    case 0x00e1: result[elementCount++] = 1; break; // á
                    case 0x00e9: result[elementCount++] = 6; break; // é
                    case 0x00ed: result[elementCount++] = 12; break; // í
                    case 0x00f1: result[elementCount++] = 19; break; // ñ
                    case 0x00f3: result[elementCount++] = 21; break; // ó
                    case 0x00fa: result[elementCount++] = 28; break; // ú
                    case 0x00fc: result[elementCount++] = 29; break; // ü
                    case 0x00c1: result[elementCount++] = 38; break; // Á
                    case 0x00c9: result[elementCount++] = 43; break; // É
                    case 0x00cd: result[elementCount++] = 49; break; // Í
                    case 0x00d1: result[elementCount++] = 56; break; // Ñ
                    case 0x00d3: result[elementCount++] = 58; break; // Ó
                    case 0x00da: result[elementCount++] = 65; break; // Ú
                    case 0x00dc: result[elementCount++] = 66; break; // Ü
                    case 0x002d: result[elementCount++] = 35; break; // -
                    case 0x0027: result[elementCount++] = 36; break; // '
                    case 0x2019: result[elementCount++] = 36;
                        break; // ’
                    default: result[elementCount++] = -128;
                }
            }
        }

        return Arrays.copyOfRange(result,0,elementCount);
    }

    public boolean rawWordExists(int[] word) {
        int[] wordInt = new int[word.length];
        for(int i=0; i<word.length; i++) {
            wordInt[i] = word[i];
        }
        return Words.exists(wordInt);
    }

    public boolean rawWordExists(String word) {
        return rawWordExists(
                textToIntArray(word)
        );
    }

    public static String intArrayToString(int[] ints) {
        if(ints == null) return "";
        StringBuilder builder = new StringBuilder();
        for(int i : ints) {
            builder.append(letters[i]);
        }
        return builder.toString();
    }

    public String[] getSuggestions(String word, int depth) {
        int[][] suggestions = getSuggestions(textToIntArray(word),depth);
        String[] asString = new String[suggestions.length];
        for(int i = 0; i < suggestions.length; i++) {
            asString[i] = intArrayToString(suggestions[i]);
        }
        return asString;
    }

    public boolean getRoot(int[] word) {
        for(int i = 0, j = word.length; i < j; i++) {
            if(word[i] < 0) return false;
            word[i] = word[i]%37;
        }
        int baseExpansion = Words.getExpansion(word);
        if(baseExpansion > 1) {
            //System.out.println("ASTv     - quickchecking '" + intArrayToString( word) + "'  exp id " + baseExpansion);
            if(!Expansions.forbidden(baseExpansion) && !Expansions.needsAffix(baseExpansion)) {
                return true;
            }else if(Expansions.forbidden(baseExpansion)) {
                return false;
            }
        }

        //DeaffixedList morphs = getMorphs(word, PREFIX_RECURSION_DEPTH, SUFFIX_RECURSION_DEPTH, 0, 0, blankRootResult);
        DeaffixedList morphs = getMorphs(word, PREFIX_RECURSION_DEPTH, 2, 0, 0, blankRootResult);

        //System.out.println("ASTv   - retrieved " + morphs.count + " semi-valid morphs...");

        RootResult potentialMorph = morphs.head;
        finalCheck:
        for(int i = 0, iMax = morphs.count; i < iMax; i++) {
            potentialMorph = potentialMorph.next;
            int expansionId = Words.getExpansion(potentialMorph.recomposed);
            //System.out.println("ASTv     - recomposed '" + intArrayToString( morphs.get(i).recomposed) + "'  exp id " + expansionId);
            // If expansionId == 0, the word does not exist;
            // If expansionId == 1, the word does not take affixes
            if(expansionId < 2) continue finalCheck;

            if(
                    Expansions.isPossible(
                            expansionId,
                            potentialMorph.morphs[0][Affixes.ID]
                    )
            ) {

                return true;
            }
        }

        //System.out.println("ASTv - found misspelled word " + intArrayToString(word) + " (base expansion " + baseExpansion + ")");
        return false;
    }


    public String[] getRoots(int[] word) {
        for(int i = 0, j = word.length; i < j; i++) {
            if(word[i] < 0) return new String[] {};
            word[i] = word[i]%37;
        }
        int baseExpansion = Words.getExpansion(word);
        if(baseExpansion > 1) {
            //System.out.println("ASTv     - quickchecking '" + intArrayToString( word) + "'  exp id " + baseExpansion);
            if(!Expansions.forbidden(baseExpansion) && !Expansions.needsAffix(baseExpansion)) {
                return new String[] {intArrayToString(word)};
            }else if(Expansions.forbidden(baseExpansion)) {
                return new String[] {};
            }
        }

        //DeaffixedList morphs = getMorphs(word, PREFIX_RECURSION_DEPTH, SUFFIX_RECURSION_DEPTH, 0, 0, blankRootResult);
        DeaffixedList morphs = getMorphs(word, PREFIX_RECURSION_DEPTH, 2, 0, 0, blankRootResult);

        //System.out.println("ASTv   - retrieved " + morphs.count + " semi-valid morphs...");

        RootResult potentialMorph = morphs.head;
        finalCheck:
        for(int i = 0, iMax = morphs.count; i < iMax; i++) {
            potentialMorph = potentialMorph.next;
            int expansionId = Words.getExpansion(potentialMorph.recomposed);
            //System.out.println("ASTv     - recomposed '" + intArrayToString( morphs.get(i).recomposed) + "'  exp id " + expansionId);
            // If expansionId == 0, the word does not exist;
            // If expansionId == 1, the word does not take affixes
            if(expansionId < 2) continue finalCheck;

            if(
                    Expansions.isPossible(
                            expansionId,
                            potentialMorph.morphs[0][Affixes.ID]
                    )
                    ) {

                return new String[]{intArrayToString(potentialMorph.recomposed)};
            }
        }

        //System.out.println("ASTv - found misspelled word " + intArrayToString(word) + " (base expansion " + baseExpansion + ")");
        return new String[]{};
    }

    public final DeaffixedList getMorphs(int[] word, int prefixDepth, int suffixDepth, int currentPrefix, int currentSuffix, RootResult baseResult) {
        DeaffixedList[] suffixLexemes = new DeaffixedList[suffixDepth+1];
            for(int i = 0, iMax = suffixDepth+1; i < iMax; i++) {
                suffixLexemes[i] = new DeaffixedList();
            }
        boolean firstSuffix = currentSuffix == 0;
        boolean firstPrefix = currentPrefix == 0;
        boolean log = false;



        //if(log) System.out.println("ASTv begin rooting for " + intArrayToString(word) + (firstSuffix ? " (first round)":""));

        /*
            Note to future developers:
            This probably should be made into a loop of sorts, but the iterative should work better
            since Java doesn't seem to figure out that this is a tail end(ish) thing.
         */

        // so that when we do the second round, it goes faster

        suffixLexemes[0].add(new RootResult(word, new int[0], new int[0][]));
        suffixLexemes[1].add(new RootResult(word, new int[0], new int[0][]));
        boolean firstRound = true;

        affix:
        for(int i = 0; i < suffixDepth; i++) {

            DeaffixedList lastLevel = suffixLexemes[currentSuffix];      // technically, currentSuffix isn't accurate, TODO better naming
            DeaffixedList currentLevel = suffixLexemes[++currentSuffix];

            // the first potential lexeme is a dummy
            RootResult potentialLexeme = lastLevel.head;

            // loop through each morph that we find from each result in the previous round
            // (The first round [j=0] should always have a count of 1, and run with the input word)
            //System.out.println("ASTv - Current suffix peeling level " + i);
            morph:
            for(int j = 0, jMax = lastLevel.count; j < jMax; j++) {

                // grab the next lexeme
                // ('next' field allows any RootResult to function as a linked list);
                potentialLexeme = potentialLexeme.next;
                int[] testWord = potentialLexeme.recomposed;

                // future improvements would call this with a single method that adjusts by
                // using optimized suffix tries for each level (not necessary for Asturian)
                int[][] morphs = firstRound ? Affixes.getFinalSuffixMorphs(testWord) : Affixes.getSuffixMorphs(testWord);
                //System.out.println("ASTv   - found " + morphs.length + "morphs");

                // loop through the morphs, only adding the ones that are potentially valid
                check:
                for (int k = 0, kMax = morphs.length; k < kMax; k++) {
                    int[] morph = morphs[k];

                    int[] recomposed = Affixes.removeSuffix(testWord, morph);

                    // This might not seem necessary, but some endings have multiple forms for
                    // orthopgrahic reasons, and we only want the one that matches our word
                    if (Matchers.suffixMatch(morph[Affixes.MATCH], recomposed)) {
                        currentLevel.add(new RootResult(recomposed, morph, baseResult.morphs));
                    }
                }
            }
            firstRound = false;
        }



/*
        //////////////////////////////
        //  OLD VERSION OF THE CODE //
        //////////////////////////////

        if(suffixDepth > currentSuffix) {
            currentSuffix++;

            // obtain the potential morphs
            int[][] suffixMorphs = firstSuffix ? Affixes.getFinalSuffixMorphs(word) : Affixes.getSuffixMorphs(word);
            if(log) System.out.println("ASTv   - found " + suffixMorphs.length + " " + (firstSuffix ? "final" : "") + " suffix morphs");

            suffixes:
            for (int i = 0; i < suffixMorphs.length; i++) {
                int[] morph = suffixMorphs[i];

                int[] recomposed = Affixes.removeSuffix(word, morph);

                // Although often not necessary, we must verify the regex matches the word
                if (Matchers.suffixMatch(morph[Affixes.MATCH], recomposed)) {
                    results.add(new RootResult(recomposed, morph, baseResult.morphs));
                }
            }
        }

        if(prefixDepth > currentPrefix) {
            currentPrefix++;

            // get the prefix morphs to work with
            int[][] prefixMorphs = Affixes.getPrefixMorphs(word);
            if(log) System.out.println("ASTv   - found " + prefixMorphs.length + " prefix morphs");

            prefixes:
            for (int i = 0; i < prefixMorphs.length; i++) {
                int[] recomposed = Affixes.apply(word, prefixMorphs[i]);

                // In all rounds but the first, we can do a quick check for affix matching to prune away
                // To fully optimize, this check should go outside of the loop, but that means C&Ping
                if (!firstPrefix) {
                    if (!Expansions.isPossible(prefixMorphs[i][Affixes.EXPAND], baseResult.morphs[0][Affixes.EXPAND])) {
                        continue prefixes;
                    }
                }

                // Although often not necessary, we must verify the regex matches the word
                if (Matchers.prefixMatch(prefixMorphs[i][Affixes.MATCH], recomposed)) {
                    results.add(new RootResult(recomposed, prefixMorphs[i], baseResult.morphs));
                }
            }
        }

        // Enter recursion, if permitted, for each affix that is still plausible
        if(prefixDepth+suffixDepth-currentSuffix-currentPrefix > 0) {
            for(int i = 0, max = results.size(); i < max; i++) {
                results.addAll(
                        getMorphs(results.get(i).recomposed,prefixDepth,suffixDepth,currentPrefix,currentSuffix,results.get(i))
                );
            }
            results.addAll(getMorphs(word,prefixDepth,suffixDepth,currentPrefix,currentSuffix,blankRootResult));
        }
*/
        DeaffixedList results = new DeaffixedList();
        addSuffixes:
        for(int i = 0, iMax = suffixDepth; i <= iMax; i++) {
            results.append(suffixLexemes[i]);
        }
        return results;
    }



    public final int[][] getFinalSuggestions(int[] word) {
        IntArrayArrayList suggestions = new IntArrayArrayList();
        int length = word.length;

        int[] shorter = new int[length-1];
        int[] same = new int[length];
        int[] longer = new int[length+1];

        // shorter ones
        for(int i = 0; i < length; i++) {
            System.arraycopy(word,0,shorter,0,i);
            System.arraycopy(word,i+1,shorter,i,length-i-1);
            if(quickTrigram(shorter) && getRoot(shorter)) suggestions.add(Arrays.copyOf(shorter,length-1));
        }

        // flipped ones
        for(int i = 0, max = length-1; i < max; i++) {
            System.arraycopy(word,0,same,0,i);

            same[i] = word[i+1];
            same[i+1] = word[i];

            System.arraycopy(word,i+2,same,i+2,length-i-2);
            if(quickTrigram(same) && getRoot(same)) suggestions.add(Arrays.copyOf(same,length));
        }

        // replaced ones
        for(int i = 0; i < length; i++) {
            System.arraycopy(word,0,same,0,length);
            for(int j = 0; j < 34; j++) {
                same[i] = j;
                if(quickTrigram(same) && getRoot(same)) suggestions.add(Arrays.copyOf(same,length));
            }
        }

        // added ones
        for(int i = 0; i < length; i++) {
            System.arraycopy(word,0,longer,0,i);
            System.arraycopy(word,i+1,longer,i+2,length-i-1);
            for(int j = 0, k=i+1; j < 34; j++) {
                longer[k] = j;
                if(quickTrigram(longer) && getRoot(longer)) suggestions.add(Arrays.copyOf(longer,length));
            }
        }

        return suggestions.toPrimitive();
    }

    public int[][] getSuggestions(int[] word, int depth) {
        if(depth == 0) return getFinalSuggestions(word);
        depth--;
        int length = word.length;
        int[] shorter = new int[length-1];
        int[] same = new int[length];
        int[] longer = new int[length+1];
        for(int i = 0, max = word.length; i < max; i++) word[i] = word[i]%37;

        IntArrayArrayList suggestions = new IntArrayArrayList();
        for(int i = 0; i < length; i++) {
            System.arraycopy(word,0,shorter,0,i);
            System.arraycopy(word,i+1,shorter,i,length-i-1);
            /*if(deepTrigram(shorter))*/ suggestions.add(getSuggestions(shorter, depth));
        }

        // flipped ones
        for(int i = 0, max = length-1; i < max; i++) {
            System.arraycopy(word,0,same,0,i);

            same[i] = word[i+1];
            same[i+1] = word[i];

            System.arraycopy(word,i+2,same,i+2,length-i-2);
            /*if(deepTrigram(same))*/ suggestions.add(getSuggestions(shorter, depth));
        }


        // replaced ones
        for(int i = 0; i < length; i++) {
            System.arraycopy(word,0,same,0,length);
            for(int j = 0; j < 34; j++) {
                same[i] = j;
                /*if(deepTrigram(same))*/ suggestions.add(getSuggestions(shorter, depth));
            }
        }

        // added ones
        for(int i = 0; i < length; i++) {
            System.arraycopy(word,0,longer,0,i);
            System.arraycopy(word,i+1,longer,i+2,length-i-1);
            for(int j = 0, k=i+1; j < 34; j++) {
                longer[k] = j;
                /*if(deepTrigram(longer))*/ suggestions.add(getSuggestions(shorter, depth));
            }
        }

        return suggestions.toPrimitive();
    }


    private static final class RootResult {
        public final int[][] morphs;
        public final int[] recomposed;
        public RootResult next; // So technically, this shouldn't go here,
                                // but I'm trying to be memory efficient.
                                // This object only exists to help out the DeaffixedList

        public RootResult() {
            recomposed = new int[0];
            morphs = new int[0][];
        }
        public RootResult(int[] _recomposed, int[] _morphs, int[][] _previousMorphs) {
            recomposed = _recomposed;
            morphs = new int[_previousMorphs.length+1][];
            morphs[0] = _morphs;
            System.arraycopy(_previousMorphs,0,morphs,1,_previousMorphs.length);
        }
    }

    private static final class DeaffixedList {
        public final RootResult head = new RootResult();
        public RootResult tail = head;
        public int count = 0;
        public final void add(RootResult result) {
            tail.next = result;
            tail = result;
            count++;
        }
        public final void add(RootResult[] results) {
            for(int i = 0, iMax = results.length; i < iMax; i++) {
                tail.next = results[i];
                tail = tail.next;
            }
        }
        public final void append(DeaffixedList that) {
            count += that.count;
            tail.next = that.head.next;
            tail = that.tail;
        }
    }

    public final boolean quickTrigram(int[] word) {
        if(word.length < 3) return true;
//        for(int i = 0, max = word.length; i < max; i++) word[i] = word[i]%37;
        for(int i = 0, max = word.length - 2; i < max; i++) {
            int bit = word[i] * TRIGRAM_OFFSET + word[i + 1] * LETTER_COUNT + word[i + 2];
            if (!booleanTrigramNew[bit]) return false;
        }
/*            if(
                    ((booleanTrigram[bit/32] >>> bit%32) & 0x1) == 0
            ) {
                return false;
            }
        }
*/
        return true;
    }

    private static final int DEEP_TRIGRAM_ERROR_LIMIT = 4;
    public final boolean deepTrigram(int[] word) {
        if(word.length < 3) return true;
        int bad = 0;
//        for(int i = 0, max = word.length; i < max; i++) word[i] = word[i]%37;
        for(int i = 0, max = word.length - 2; i < max; i++) {
            int bit = word[i]*TRIGRAM_OFFSET + word[i+1]*LETTER_COUNT + word[i+2];
            if(
                    (booleanTrigram[bit/32]
                            & (TRIGRAM_BASE >>> bit%32)
                    ) == 0
                    ) {
                bad++;
            }
        }
        return bad < DEEP_TRIGRAM_ERROR_LIMIT;
    }

    private static final class IntArrayArrayList {
        private int _count = 0;
        private int _size = 36;
        public static final int INCREMENT_SIZE = 16;
        private int[][] _data = new int[INCREMENT_SIZE][];
        public IntArrayArrayList() {}

        public final void add(int[] add) {
            if(_count == _size) {
                _size += INCREMENT_SIZE;
                int[][] temp = new int[_size][];
                System.arraycopy(_data,0,temp,0,_count);
                _data = temp;
            }
            _data[_count++] = add;
        }

        public final void add(int[][] add) {
            int addLength = add.length;
            if(_count + addLength >= _size) {
                _size = _count + addLength + INCREMENT_SIZE;
                int[][] temp = new int[_size][];
                System.arraycopy(_data,0,temp,0,_count);
                System.arraycopy(add,0,temp,_count,addLength);
                _count += addLength;
                _data = temp;
            }
        }

        public final int[][] toPrimitive() {
            int[][] temp = new int[_count][];
            System.arraycopy(_data,0,temp,0,_count);
            return temp;
        }
    }
}
