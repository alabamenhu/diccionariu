package org.softastur.asturianspellchecker;

import android.service.textservice.SpellCheckerService;
import android.view.textservice.SuggestionsInfo;
import android.view.textservice.TextInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Created by Matthew Stephen Stuckwisch on 11 May 20155.
 */
public class AsturianSpellCheckerService extends SpellCheckerService {
    private Checker checker;

    public AsturianSpellCheckerService() {
        super();

        System.out.println("ASTv created service");
    }

    @Override
    public Session createSession() {
        if(this != null) {
            checker = new Checker(this);
        }
        return new AsturianSpellCheckerSession();

    }


    public class AsturianSpellCheckerSession extends SpellCheckerService.Session {


        public void onCreate() {

            System.out.println("ASTvv created session");
        }



        @Override
        public SuggestionsInfo onGetSuggestions(TextInfo textInfo, int suggestionsLimit) {
            SuggestionsInfo suggestionsInfo;

            System.out.println("ASTvv Begin suggestions for '"+textInfo.getText()+"' (max "+suggestionsLimit+")");
            //int attribute;
            //attribute = SuggestionsInfo.RESULT_ATTR_LOOKS_LIKE_TYPO;
            //attribute = SuggestionsInfo.RESULT_ATTR_IN_THE_DICTIONARY;
            //attribute = SuggestionsInfo.RESULT_ATTR_HAS_RECOMMENDED_SUGGESTIONS;



            if(
                checker.getRoot(Checker.textToIntArray(textInfo.getText()))
            ){
                suggestionsInfo = new SuggestionsInfo(
                        SuggestionsInfo.RESULT_ATTR_IN_THE_DICTIONARY,
                        new String[0],
                        textInfo.getCookie(),
                        textInfo.getSequence()
                );
            }else {
                String[] results = orderByProbability(textInfo.getText(), checker.getSuggestions(textInfo.getText(), 1));
                String[] finalSuggestions = new String[Math.min(suggestionsLimit,results.length)];
                System.arraycopy(results,0,finalSuggestions,0,Math.min(suggestionsLimit,results.length));


                suggestionsInfo = new SuggestionsInfo(
                        SuggestionsInfo.RESULT_ATTR_LOOKS_LIKE_TYPO,
                        finalSuggestions,
                        textInfo.getCookie(),
                        textInfo.getSequence()
                );
            }
            System.out.println("ASTvv Finished suggestions for '"+textInfo.getText()+"' (max "+suggestionsLimit+")");
            return suggestionsInfo;
        }
    }


    private String[] orderByProbability(String word, String[] suggestions) {
        Set<String> unique = new HashSet<>(Arrays.asList(suggestions));
        List<String> sorted = Arrays.asList(unique.toArray(new String[]{}));
        Collections.sort(sorted, new LevenshteinComparator(word));
        return (String[]) sorted.toArray();
    }

    private static int distance(String a, String b) {
        a = a.toLowerCase();
        b = b.toLowerCase();
        // i == 0
        int [] costs = new int [b.length() + 1];
        for (int j = 0; j < costs.length; j++)
            costs[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            // j == 0; nw = lev(i - 1, j)
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= b.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]), a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[b.length()];
    }

    public class LevenshteinComparator implements java.util.Comparator<String> {
        private String reference;
        public LevenshteinComparator(String reference) {
            super();
            this.reference = reference;
        }
        public int compare(String s1, String s2) {
            int dist1 = distance(reference,s1);
            int dist2 = distance(reference, s2);
            return dist1 - dist2;
        }
    }

}
