package org.softastur.asturiandictionary;

import android.text.SpannableString;
import android.text.Spanned;
import java.util.ArrayList;

/**
 * Created by Matthew Stuckwisch on 6 May 2015.
 */
public class FormattedStringBuilder  {

    private ArrayList<StringFormatPair> pieces = new ArrayList<StringFormatPair>();

    public FormattedStringBuilder() {

    }

    public FormattedStringBuilder append(String string, Object... format) {
        pieces.add(
                new StringFormatPair(string,format)
        );
        return this;
    }

    public FormattedStringBuilder append(String string) {
        pieces.add(
                new StringFormatPair(string,new Object[]{})
        );
        return this;
    }

    public CharSequence build() {

        // Generate the base string
        StringBuilder stringBuilder = new StringBuilder();
        for(StringFormatPair piece : pieces) {
            stringBuilder.append(piece.getString());
        }

        // String builder from our generated string
        SpannableString result = new SpannableString(stringBuilder.toString());

        int currentPosition = 0;

        // Selectively apply the formats
        for(StringFormatPair piece : pieces) {
            for(Object format : piece.getFormats()) {
                if(format != null) {
                    result.setSpan(
                            format,
                            currentPosition,
                            currentPosition + piece.getString().length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
            currentPosition += piece.getString().length();
        }
        return result.subSequence(0,result.length());
    }

    private class StringFormatPair {
        private String mString;
        private Object[] mFormats;
        public StringFormatPair(String string, Object... format) {
            mString = string;
            mFormats = format;
        }
        public String getString() {
            return mString;
        }
        public Object[] getFormats() {
            return mFormats;
        }
    }

}
