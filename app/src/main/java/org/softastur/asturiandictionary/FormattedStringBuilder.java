package org.softastur.asturiandictionary;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import java.util.ArrayList;

/**
 * Created by guifa on 9/11/17.
 */

public class FormattedStringBuilder {
    private ArrayList<Format> stack;
    private SpannableStringBuilder builder;
    private final boolean overlappable;
    private Format base;
    private Format currentFormat;

    public FormattedStringBuilder(boolean overlappable) {
        builder = new SpannableStringBuilder();
        stack = new ArrayList<>();
        base = new Format(0,new Object[]{},null);
        currentFormat = base;
        this.overlappable = overlappable;
    }


    public FormattedStringBuilder append(String text, Object[] spans) {
        beginFormat(spans);
        append(text);
        endFormat();
        return this;
    }
    public FormattedStringBuilder append(String text) {
        builder.append(text);
        return this;
    }
    public FormattedStringBuilder append(CharSequence text) {
        builder.append(text);
        return this;
    }
    public FormattedStringBuilder beginFormat(Object[] spans) {
        currentFormat = currentFormat.addChild(
                new Format(
                        builder.length(),
                        spans,
                        currentFormat
                )
        );
        return this;
    }

    private void endWithoutOverlap() {
        Format format = stack.remove(0);
        int start = format.start;
        int end = builder.length();

        for(Object span : format.spans) {
            builder.setSpan(
                    span, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            );
        }
    }

    public FormattedStringBuilder endFormat() {
        if(currentFormat.parent != null) {
            currentFormat.end = builder.length();
            currentFormat = currentFormat.parent;
        }else{
            throw new IllegalStateException("asturianu - the formatted string builder cannot end a format when there isn't any left.");
        }
        return this;
    }

    public CharSequence build() {
        // find the top parent
        apply(base.children);
        return builder;
    }

    private void apply(ArrayList<Format> formats) {
        for(Format format : formats) {
            for(Object span : format.spans) {
                builder.setSpan(span,format.start,format.end,Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            }
            apply(format.children);
        }
    }

    private static class Format {
        final int start;
        int end;
        final Object[] spans;
        final ArrayList<Format> children = new ArrayList<>();;
        final Format parent;

        public Format(int start, Object[] spans,Format parent) {
            this.start = start;
            this.spans = spans;
            this.parent = parent;
        }
        public Format addChild(Format format) {
            children.add(format);
            return format;
        }
    }

}
