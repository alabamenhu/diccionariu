package org.softastur.asturiandictionary;

import java.text.BreakIterator;

/**
 * Created by mateo on 16.08.06.
 */
public class Grapheme {

    public Grapheme () {}

    public static int length(String text) {
        int grapheme = 0;

        BreakIterator iterator = java.text.BreakIterator.getCharacterInstance();
        iterator.setText(text);
        while(iterator.next() != BreakIterator.DONE) grapheme += 1;


        return grapheme;
    }

    public static String subString(String text, int start, int finish) {
        BreakIterator iterator = java.text.BreakIterator.getCharacterInstance();
        iterator.setText(text);
        int grapheme = 0;
        StringBuilder builder = new StringBuilder();

        while(iterator.next() != BreakIterator.DONE && grapheme < finish) {

            if (grapheme >= start) {
                builder.append(
                        text.substring(
                                iterator.last() == BreakIterator.DONE ? 0 : iterator.last(),
                                iterator.current()
                        )
                );
                grapheme += 1;
            }
        }
        return builder.toString();
    }
}
