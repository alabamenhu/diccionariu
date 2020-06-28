package org.softastur.asturiandictionary;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.softastur.asturianspellchecker.Checker;
import org.softastur.asturianspellchecker.QuickFileReader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by guifa on 5/7/15.
 * The SearchResultAdapter takes
 */
public class SearchResultAdapter extends ArrayAdapter {

    Context context;
    ArrayList mResults = new ArrayList<SearchResult>();
    String[] partsOfSpeech;
    String mQuery;
    volatile long mMostRecentResult = 0;
    private volatile int[] data;
    private static final int MAX_MATCHES = 10;
    private DictionaryViewStyles styles;
    private ListView listView;
    boolean specialTypeIsHint = true;
    String lastSearch = "";
    Checker checker;

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

    public SearchResultAdapter(Context context, int resource, List objects, ListView view) {
        super(context, resource, objects);
        this.context = context;
        styles = new DictionaryViewStyles((ActivityMain) context);
        partsOfSpeech = context.getResources().getStringArray(R.array.part_of_speech_abbreviations);
        listView = view;
        loadData();
    }

    // loads binary data needed
    private void loadData() {
        byte[] file = QuickFileReader.readRawResource(context, R.raw.fast_dict); // todo use util.java
        IntBuffer ib = ByteBuffer.wrap(file).order(ByteOrder.BIG_ENDIAN).asIntBuffer();

        data = new int[ib.limit()];
        ib.get(data);

        checker = new Checker(context);
    }

    // once items are in this is run
    public void doQuery(String query) {

        lastSearch = query;
        if(query.equals("")) {
            specialTypeIsHint = true;
            clear();
            return;
        }
        if(data == null) {loadData();}

        ArrayList<SearchResult> matches = quickMatch(query);

        final String q = query;
        Collections.sort(matches, new Comparator<SearchResult>() {
            @Override
            public int compare(SearchResult a, SearchResult b)
            {
                int[] word = textToIntArray(q);
                int[] resultA = textToIntArray(a.lede);
                int[] resultB = textToIntArray(b.lede);
                int scoreA = 1000;
                int scoreB = 1000;

                // scoreA
                if(a.type == SearchResult.FUZZY) {
                    int penalty = 333 / word.length;
                    for(int i = 0; i < word.length; i++) {
                        if(resultA[i] != word[i]) scoreA -= penalty;
                    }
                    scoreA -= (resultA.length - word.length) * penalty / 2;
                }else if(a.type == SearchResult.PARTIAL){
                    int penalty = 500 / word.length;
                    scoreB -= (resultA.length - word.length) * penalty / 2;
                }

                // scoreA
                if(b.type == SearchResult.FUZZY) {
                    int penalty = 333 / word.length;
                    for(int i = 0; i < word.length; i++) {
                        if(resultB[i] != word[i]) scoreB -= penalty;
                    }
                    scoreB -= (resultB.length - word.length) * penalty / 2;
                }else if(b.type == SearchResult.PARTIAL){
                    int penalty = 500 / word.length;
                    scoreB -= (resultB.length - word.length) * penalty / 2;
                }

                if(scoreA == scoreB) {
                    if(resultA.length == resultB.length) {
                        for(int j = 0; j < resultA.length; j++) {
                            if(resultA[j] != resultB[j]) {
                                return resultA[j] - resultB[j];
                            }
                        }
                        return a.partOfSpeech - b.partOfSpeech;
                    }else{
                        return resultA.length - resultB.length;
                    }
                }else{
                    return scoreA - scoreB;
                }

            }
        });

        clear();
        addAll(matches);
        resize();
        notifyDataSetChanged();
    }

    private ArrayList<SearchResult> quickMatch(String query) {
        int remainingMatches = MAX_MATCHES;
        mQuery = query;
        boolean stillSearching = true;
        int[] word = textToIntArray(query);

        int currentPosition = 0;

        ArrayList<SearchResult> exactMatches = exactMatch(word);
        ArrayList<SearchResult> expandedMatches = expandedMatch(word);
        ArrayList<SearchResult> fuzzyMatches = fuzzyMatch(word);
        ArrayList<SearchResult> unconjugatedMatches = conjugatedMatch(word);
        // backwards matches
        unconjugated:
        for(SearchResult a : unconjugatedMatches) {
            for(SearchResult b : exactMatches) {
                if(a.id == b.id) continue unconjugated;
            }
            exactMatches.add(a);
        }

        expanded:
        for(SearchResult a : expandedMatches) {
            for(SearchResult b : exactMatches) {
                if(a.id == b.id) continue expanded;
            }
            exactMatches.add(a);
        }

        fuzzy:
        for(SearchResult a : fuzzyMatches) {
            for(SearchResult b : exactMatches) {
                if(a.id == b.id) continue fuzzy;
            }
            exactMatches.add(a);
        }

        if(exactMatches.size() == 0) {
            specialTypeIsHint = false;
        }
        return exactMatches;
    }

    @Override
    public int getCount() {
        int base = super.getCount();
        if (base == 0) return 1;
        return base;
    }

    private ArrayList<SearchResult> exactMatch(int[] word) {
        ArrayList<SearchResult> result = new ArrayList<>();
        int position = 0;

        traversal:
        // label for escape
        for (int i = 0, iMax = word.length; i < iMax; i++) {

            int letter = word[i];

            // The beginning of the branch indicates the number of offshoots
            int offsetMax = data[position];
            int offsetMin = 0;

            // Because we use a zero-index, add two.
            // (second element of the branch includes meta info)
            position += 2;

            // Binary search for the key
            while (offsetMax > offsetMin) {
                int check = (offsetMax + offsetMin) / 2;

                // Key values are even (0-count) indexes
                int compare = data[2 * check + position];

                if (compare == letter) {

                    // The odd bytes indicate where the offshoot branch is located
                    position = data[2 * check + position + 1];

                    System.out.println("asturianu Jumped to position " + position);
                    continue traversal;
                } else {
                    if (compare > letter) {
                        offsetMax = check;
                    } else {
                        offsetMin = check + 1;
                    }
                }
            }

            return result;

        }


        int exactMatches = data[position + 1];
        int furtherExpansion = data[position]; // if we've ended at "the", then this number
                                               // would indicate all letters that could expand
                                               // beyond, like b, c, etc.  We don't care about
                                               // them, but we have to skip them to get the data
                                               // we want

        position += furtherExpansion * 2 + 2;
        for(int i = 0; i < exactMatches; i++) {
            result.add(
                    new SearchResult(
                            intArrayToString(word),
                            data[position + (i*2)],
                            SearchResult.EXACT,
                            data[position+(i*2)+1]
                    )
            );
        }
        return result;
    }

    private ArrayList<SearchResult> expandedMatch(int[] word) {
        // the initial part of expanded match subroutine is identical
        // to the exactMatch one.  The only difference is once we reach
        // the index of the final letter, we look for expanded versions of it
        // and stop once we have X number of expansions.  I'd say it's best to stop at the following
        // lengths:
        // INPUT    MAX_SEARCH_LENGTH    MAX_SEARCH_RESULTS
        // 1        3                    30
        // 2        4                    40
        // 3        5                    60
        // 4        7                    100
        // 5        9                    100
        // 6        12                   100
        // 7        14                   100
        // 8        99                   100
        int[] MAX_DEPTH = new int[]{2,2,2,3,4,6,7,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99};
        int[] MAX_SEARCH_RESULTS = new int[]{12,18,24,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100};
        // The idea is, of course, to limit too many results since we later
        // need to sort by best matches, but also because the input string "a" or "e" will result in
        // half the dictionary being returned.
        // In the future, the search results will continue until past the max depth if and only if
        // the max search results have not yet been obtained.

        ArrayList<SearchResult> result = new ArrayList<>();
        int position = 0;

        traversal:
        // label for escape
        for (int i = 0, iMax = word.length; i < iMax; i++) {

            int letter = word[i];

            // The beginning of the branch indicates the number of offshoots
            int offsetMax = data[position];
            int offsetMin = 0;

            // Because we use a zero-index, add two.
            // (second element of the branch includes meta info)
            position += 2;

            // Binary search for the key
            while (offsetMax > offsetMin) {
                int check = (offsetMax + offsetMin) / 2;

                // Key values are even (0-count) indexes
                int compare = data[2 * check + position];

                if (compare == letter) {

                    // The odd bytes indicate where the offshoot branch is located
                    position = data[2 * check + position + 1];

                    continue traversal;
                } else {
                    if (compare > letter) {
                        offsetMax = check;
                    } else {
                        offsetMin = check + 1;
                    }
                }
            }
            return result;
        }


        int initialExpansions = data[position];
        if (initialExpansions == 0) return result;
        // this is the number of initial expansions we have, if it's 0, we can't move forward

        LinkedList<ExpansionPartial> partial = new LinkedList<>();

        // first we add all partial results from our current position to the list
        // For example, if we had the word "the", we'll now add in "thea...", "theb..."
        for(int i = 0; i < initialExpansions; i++) {
            partial.add(
                    new ExpansionPartial(
                            word,
                            data[position + (i*2) + 3],
                            data[position + (i*2) + 2]
                    )
            );
        }

        for(int remaining = MAX_DEPTH[word.length]; remaining > 0; remaining--) {
            // at this stage, we parse the entire linked list, doing two things for each element:
            //   a) checking if any words exist, and if so adding them to our result list
            //   b) checking if any expansions exist, and if so adding them to the partial list
            // because we add to the partial list, and after doing (a) and (b) we remove the item,
            // the loop increases in depth without needing create multidimensional lists or anything like that

            for(int i = 0, j = partial.size(); i < j; i++) {
                ExpansionPartial partialResult = partial.removeFirst();
                int matches = data[partialResult.position+1];
                int expansions = data[partialResult.position];

                // A, check for existing words

                for(int k = 0; k < matches; k++) {
                    result.add(
                            new SearchResult(
                                    intArrayToString(partialResult.base),
                                    data[partialResult.position + 2 + (expansions*2) + (k*2)],
                                    SearchResult.PARTIAL,
                                    data[partialResult.position + 2 + (expansions*2) + (k*2) + 1]
                            )
                    );
                }

                // B, check for expansions
                for(int k = 0; k < expansions; k++) {
                    partial.add(
                            new ExpansionPartial(
                                    partialResult.base,
                                    data[partialResult.position + 2 + (k*2) + 1],
                                    data[partialResult.position + 2 + (k*2)]
                            )
                    );
                }

                if(result.size() > MAX_SEARCH_RESULTS[word.length]) {
                    break;
                }else if(partial.size() == 0) {
                    break;
                }
            }
            if(partial.size() > 0 && remaining == 1);
        }


        // lastly, check any final partial expansions (basically, step A)

        for(int i = 0; i < partial.size(); i++) {
            ExpansionPartial partialResult = partial.removeFirst();
            int matches = data[partialResult.position+1];
            int expansions = data[partialResult.position];

            // A, check for existing words

            for(int k = 0; k < matches; k++) {
                result.add(
                        new SearchResult(
                                intArrayToString(partialResult.base),
                                data[partialResult.position + 2 + (expansions*2) + (k*2)],
                                SearchResult.PARTIAL,
                                data[partialResult.position + 2 + (expansions*2) + (k*2) + 1]
                        )
                );
            }
        }

        return result;
    }

    private ArrayList<SearchResult> fuzzyMatch(int[] word) {
        // the end part of this method exactly matches the expanded match
        // one.  The only difference is initially, we make several substitutions that
        // could represent common spelling errors for Asturian speakers (but not typos), and then once
        // we reach the index of the final letter, we look for expanded versions of it
        // and stop once we have X number of expansions.  I'd say it's best to stop at the following
        // lengths:
        // INPUT    MAX_SEARCH_LENGTH    MAX_SEARCH_RESULTS
        // 1        3                    30
        // 2        4                    40
        // 3        5                    60
        // 4        7                    100
        // 5        8                    100
        // 6        9                    100
        // 7        10                   100
        // 8        12                   100
        int[] MAX_DEPTH = new int[]{2,2,2,3,4,6,7,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99};
        int[] MAX_SEARCH_RESULTS = new int[]{30,40,60,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100};
        // The idea is, of course, to limit too many results since we later
        // need to sort by best matches, but also because the input string "a" or "e" will result in
        // half the dictionary being returned.

        ArrayList<SearchResult> result = new ArrayList<>();
        int position = 0;

        LinkedList<FuzzyPartial> partial = new LinkedList<>();

        partial.add(new FuzzyPartial (new int[] {}, 0, 0));

        for(int i = 0; i < word.length; i++) {
            // here we loop through each letter of the word, but we have to go based on the partial
            // result for each one =/
            int[] fuzzyLetters = singleSwap(word[i]);

            System.out.println("asturianu - " + intArrayToString(new int[]{word[i]}) + "->" + intArrayToString(fuzzyLetters));


            for(int j = 0, jMax = partial.size(); j < jMax; j++) {
                System.out.println("asturianu - j=" + j + ", partials=" + partial.size());
                FuzzyPartial base = partial.removeFirst();
                // we get the first element, and then peek to see if any of the matching elements are
                // possible for us.

                int expansionCount = data[base.position];
                System.out.println("asturianu - found " + expansionCount + " expansions");

                for(int k = 0; k < expansionCount; k++) {
                    // expansion letters alternate in position, with a +2 offset
                    int letter = data[base.position + (k*2) + 2];

                    for(int l = 0; l < fuzzyLetters.length; l++) {
                        System.out.println("asturianu -   does " + letter + "=" + fuzzyLetters[l] + "?");
                        if(letter == fuzzyLetters[l]) {
                            // we have a match for this fuzzy letter
                            partial.add(
                                    new FuzzyPartial(
                                            base.base,
                                            data[base.position + (k*2) + 2 + 1],
                                            i,
                                            letter
                                    )
                            );
                        }
                    }
                }
            }
        }

        // next we need to expand these if possible



        int initialExpansions = data[position];
        if (initialExpansions == 0) return result;
        // this is the number of initial expansions we have, if it's 0, we can't move forward


        for(int remaining = MAX_DEPTH[word.length]; remaining > 0; remaining--) {
            // at this stage, we parse the entire linked list, doing two things for each element:
            //   a) checking if any words exist, and if so adding them to our result list
            //   b) checking if any expansions exist, and if so adding them to the partial list
            // because we add to the partial list, and after doing (a) and (b) we remove the item,
            // the loop increases in depth without needing create multidimensional lists or anything like that


            for(int i = 0, j = partial.size(); i < j; i++) {
                FuzzyPartial partialResult = partial.removeFirst();
                int matches = data[partialResult.position+1];
                int expansions = data[partialResult.position];

                // A, check for existing words

                for(int k = 0; k < matches; k++) {
                    result.add(
                            new SearchResult(
                                    intArrayToString(partialResult.base),
                                    data[partialResult.position + 2 + (expansions*2) + (k*2)],
                                    SearchResult.PARTIAL,
                                    data[partialResult.position + 2 + (expansions*2) + (k*2) + 1]
                            )
                    );
                }

                // B, check for expansions
                for(int k = 0; k < expansions; k++) {
                    partial.add(
                            new FuzzyPartial(
                                    partialResult.base,
                                    data[partialResult.position + 2 + (k*2) + 1],
                                    partialResult.progress + 1,
                                    data[partialResult.position + 2 + (k*2)]
                            )
                    );
                }

                if(result.size() > MAX_SEARCH_RESULTS[word.length]) {
                    break;
                }
            }

            // lastly, check any final partial expansions (basically, step A)

            for(int i = 0; i < partial.size(); i++) {
                FuzzyPartial partialResult = partial.removeFirst();
                int matches = data[partialResult.position+1];
                int expansions = data[partialResult.position];

                // A, check for existing words

                for(int k = 0; k < matches; k++) {
                    result.add(
                            new SearchResult(
                                    intArrayToString(partialResult.base),
                                    data[partialResult.position + 2 + (expansions*2) + (k*2)],
                                    SearchResult.PARTIAL,
                                    data[partialResult.position + 2 + (expansions*2) + (k*2) + 1]
                            )
                    );
                }
            }

        }

        return result;
    }

    private ArrayList<SearchResult> conjugatedMatch(int[] word) {
        // TODO THIS IS WHERE WE CAN RETURN CONJUGATED SHIT FOR THE SEARCH BAR!!!
        // The roots returned by the checker are, in effect, guaranteed to be in the dictionary
        // as an exact match.  The only way that they might not is if there is version mismatch.
        // however, because it returns a *string* it's possible that there are two different
        // versions and we cannot currently distinguish between a word with a noun and an adj entry,
        // even if the form is unambiguous.  Additionally, at the present moment, words that
        // are returned as baseform-only roots (say, "sedr" for "ser" which would be returned
        // if one searched for "sedremos") will rarely if ever produce a valid result at the
        // the present time.
        //
        String[] roots = checker.getRoots(word);
        ArrayList<SearchResult> conjugatedMatches = new ArrayList<>();
        for(String root : roots) {
            Log.d("asturianu","Found a root and trying to add it: " + root);
            conjugatedMatches.addAll(
                    exactMatch(textToIntArray(root))
            );
        }
        for(SearchResult result : conjugatedMatches) {
            result.type = SearchResult.CONJUGATED;
        }

        return conjugatedMatches;
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

    public static String intArrayToString(int[] ints) {
        if(ints == null) return "";
        StringBuilder builder = new StringBuilder();
        for(int i : ints) {
            if(i>-1) builder.append(letters[i]);
        }
        return builder.toString();
    }


    @Override
    public long getItemId(int position) {
        if(super.getCount() > 0) {
            return (long) ((SearchResult) getItem(position)).id;
        }
        return 0;
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();

        // catch special types
        if(position == 0 && super.getCount() == 0) {
            if(specialTypeIsHint) {
                return inflater.inflate(R.layout.search_result_row_hint,parent,false);
            }else{
                View error =  inflater.inflate(R.layout.search_result_row_no_result, parent,false);
                ((TextView)error.findViewById(R.id.lede)).setText(
                        String.format(context.getResources().getString(R.string.no_search_results),lastSearch)
                );
                return error;
            }
        }


        View view = inflater.inflate(R.layout.search_result_row, parent,false);
        SearchResult searchResult = (SearchResult) getItem(position);

        TextView lede = (TextView)view.findViewById(R.id.lede);
        TextView id = (TextView)view.findViewById(R.id.entry_id);
        TextView pos = (TextView)view.findViewById(R.id.part_of_speech);

        FormattedStringBuilder builder = new FormattedStringBuilder(true);

        if(searchResult.type == SearchResult.EXACT) {
            builder.beginFormat(styles.searchExact())
                    .append(searchResult.lede)
                    .endFormat();
        }else if(searchResult.type == SearchResult.PARTIAL) {
            builder.beginFormat(styles.searchPartialMatch())
                    .append(
                            searchResult.lede.substring(0,mQuery.length() < searchResult.lede.length()
                                    ? mQuery.length()
                                    : searchResult.lede.length()))
                    .endFormat()
                    .beginFormat(styles.searchUnmatched())
                    .append(
                            searchResult.lede.substring(mQuery.length() < searchResult.lede.length()
                                    ? mQuery.length()
                                    : searchResult.lede.length())
                            )
                    .endFormat();
        }else if(searchResult.type == SearchResult.FUZZY) {
            builder.beginFormat(styles.searchFuzzy())
                    .append(searchResult.lede)
                    .endFormat();
        }else if(searchResult.type == SearchResult.CONJUGATED) {
            builder.beginFormat(styles.searchConjugatedIntro())
                    .append("de  ")
                    .endFormat()
                    .beginFormat(styles.searchConjugated())
                    .append(searchResult.lede)
                    .endFormat();
        }else {
            builder.append(searchResult.lede + "?");
//            lede.setTextSize(TypedValue.COMPLEX_UNIT_DIP,18);
        }

        lede.setText(builder.build());

        id.setText(Integer.toString(searchResult.id));

        if(searchResult.type != SearchResult.NO_MATCH) {
            pos.setText(partsOfSpeech[searchResult.partOfSpeech]);
        }else{
            pos.setText("");
        }

        return view;
    }

    private class SearchResult {
        public String lede;
        public int id;
        public int type;
        public int partOfSpeech;

        public static final int EXACT = 0;
        public static final int PARTIAL = 1;
        public static final int FUZZY = 2;
        public static final int NO_MATCH = 3;
        public static final int CONJUGATED = 4;

        public SearchResult(String _lede, int _id, int _type, int _pos) {
            lede = _lede;
            id = _id;
            type = _type;
            partOfSpeech = _pos;
            System.out.println("asturianu SearchResult('" + _lede + "'," + _id + ",…," + _pos + ")");
        }
    }

    private class ExpansionPartial {
        public int[] base;
        public int position;
        public ExpansionPartial(int[] _base, int _position) {
            base = _base;
            position = _position;
        }
        public ExpansionPartial(int[] start, int position, int add) {
            this.base = new int[start.length + 1];
            System.arraycopy(start,0,this.base,0,start.length);
            this.base[start.length] = add;
            this.position = position;
        }
    }

    private class FuzzyPartial {
        public int[] base;
        public int position;
        public int progress;
        public FuzzyPartial (int[] _base, int _position, int _progress) {
            base = _base;
            position = _position;
            progress = _progress;
        }
        public FuzzyPartial (int[] _base, int _position, int _progress, int add) {
            base = new int[_base.length + 1];
            System.arraycopy(_base,0,base,0,_base.length);
            base[_base.length] = add;
            position = _position;
            progress = _progress;
        }
    }

    private static int[] singleSwap(int a) {
        // this should eventually be put into a file and read in
        switch(a) {
            case 0: return new int[]{0,1}; // a --> áa
            case 1: return new int[]{1,0}; // á --> aá
            case 5: return new int[]{6,5}; // e --> ée
            case 6: return new int[]{5,6}; // é --> eé
            case 9: return new int[]{10,9}; // h --> h.h
            case 10: return new int[]{9,10}; // h. --> hh.
            case 11: return new int[]{12,11}; // i --> íi
            case 12: return new int[]{11,12}; // í --> ií
            case 15: return new int[]{16,15}; // l --> l.l
            case 16: return new int[]{15,16}; // l. --> ll.
            case 18: return new int[]{19,18}; // n --> ñn
            case 19: return new int[]{18,19}; // ñ --> nñ
            case 20: return new int[]{21,20}; // o --> óo
            case 21: return new int[]{20,21}; // ó --> oó
            case 27: return new int[]{28,29,27}; // u --> úüu
            case 28: return new int[]{27,29,28}; // ú --> uüú
            case 29: return new int[]{27,28,29}; // ü --> uúü
        }

        return new int[] {a};
    }

    private void resize() {
        int totalHeight = 0;
        for (int i = 0; i < getCount(); i++) {
            View listItem = getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();

    }

}
