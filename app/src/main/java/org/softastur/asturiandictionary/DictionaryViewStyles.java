package org.softastur.asturiandictionary;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.ReplacementSpan;
import android.text.style.StyleSpan;
import android.view.View;

/**
 * Created by guifa on 5/8/15.
 */
public class DictionaryViewStyles {
    private ActivityMain activity;
    private static final int ERROR_ENTRY_NUMBER = 17383;
    private  static final String THIN_SPACE = "\u2009";

    public DictionaryViewStyles(ActivityMain activity) {
        this.activity = activity;
    }
    public DictionaryViewStyles() {

    }

    public static final int DARK_TEXT = 0xff000000;
    public static final int LIGHT_TEXT = 0xff555555; //#ff555555
    public static final int COLOR_YELLOW = 0xfff7ea60; // used for highlighting
    public static final int COLOR_ONE = 0xff001166;
    public static final int COLOR_TWO = 0xff004278; // used for interior ledes


    private static final int FOREGROUND_PRIMARY = 0;
    private static final int FOREGROUND_SECONDARY = 1;
    private static final int FOREGROUND_CONTRAST = 2;
    private static final int BACKGROUND_PRIMARY = 3;
    private static final int HIGHLIGHT_PRIMARY = 4;


    private int getColor(int label) {
        switch (label) {
            case FOREGROUND_PRIMARY:
                return (activity == null) ? DARK_TEXT : activity.getResources().getColor(R.color.foreground_primary);
            case FOREGROUND_SECONDARY:
                return (activity == null) ? LIGHT_TEXT : activity.getResources().getColor(R.color.foreground_secondary);
            case FOREGROUND_CONTRAST:
                return (activity == null) ? COLOR_TWO : activity.getResources().getColor(R.color.foreground_contrast);
            case BACKGROUND_PRIMARY:
                return (activity == null) ? 0xffffffff : activity.getResources().getColor(R.color.background_primary);
            case HIGHLIGHT_PRIMARY:
                return (activity == null) ? COLOR_YELLOW : activity.getResources().getColor(R.color.highlight_primary);
            default: return 0;
        }
    }

    public Object[] plain() {
        return new Object[] {};
    }

    public Object[] definitionNumber() {
        return new Object[]{
                new StyleSpan(Typeface.BOLD),
                new ForegroundColorSpan(getColor(FOREGROUND_PRIMARY))
        };
    }
    public Object[] explicativeStar() {
        return new Object[]{
                new StyleSpan(Typeface.BOLD_ITALIC),
                new ForegroundColorSpan(COLOR_ONE)
        };
    }
    public Object[] normalDefinition() {
        return new Object[]{
                new ForegroundColorSpan(getColor(FOREGROUND_PRIMARY)),
        };
    }
    public Object[] secondaryDefinition() {
        return new Object[]{
                new ForegroundColorSpan(getColor(FOREGROUND_SECONDARY))
        };
    }
    public Object[] explicativeDefinition() {
            return new Object[]{
                    new StyleSpan(Typeface.ITALIC),
                    new ForegroundColorSpan(getColor(FOREGROUND_PRIMARY))
            };
        }
    public Object[] secondaryExplicativeDefinition() {
        return new Object[]{
                new StyleSpan(Typeface.ITALIC),
                new ForegroundColorSpan(getColor(FOREGROUND_SECONDARY))
        };
    }
    public Object[] definitionExample() {
        return new Object[]{
                new StyleSpan(Typeface.ITALIC),
                new ForegroundColorSpan(getColor(FOREGROUND_CONTRAST)),
                new LeadingMarginSpan.Standard(75, 100)
        };
    }
    public Object[] phraseLede() {
        return new Object[]{
                new StyleSpan(Typeface.BOLD),
                new ForegroundColorSpan(getColor(FOREGROUND_CONTRAST)),
                new LeadingMarginSpan.Standard(0, 75)
        };
    }
    public Object[] phraseDefinition() {
        return new Object[]{
                new StyleSpan(Typeface.BOLD),
                new ForegroundColorSpan(getColor(FOREGROUND_PRIMARY)),
                new LeadingMarginSpan.Standard(75, 100)
        };
    }
    public Object[] phraseDefinitionExample() {
        return new Object[]{
                new StyleSpan(Typeface.ITALIC),
                new ForegroundColorSpan(getColor(FOREGROUND_PRIMARY)),
                new LeadingMarginSpan.Standard(100,125)
        };
    }

    protected Object[] link(int id) {
        return new Object[]{
                new DefinitionLinkSpan(id),
                new RoundedBackgroundSpan(getColor(FOREGROUND_PRIMARY),getColor(HIGHLIGHT_PRIMARY),12)
        };
    }

    public Object[] searchExact() {
        return new Object[] {
                new StyleSpan(Typeface.BOLD),
                new ForegroundColorSpan(getColor(HIGHLIGHT_PRIMARY)),
//                new ForegroundColorSpan(0xffddbb00)
        };
    }
    public Object[] searchPartialMatch() {
        return new Object[] {
                new StyleSpan(Typeface.BOLD),
                new ForegroundColorSpan(getColor(FOREGROUND_CONTRAST)) // todo use foreground_bright
        };
    }
    public Object[] searchUnmatched() {
        return new Object[]{
                new ForegroundColorSpan(getColor(FOREGROUND_SECONDARY))
        };
    }
    public Object[] searchFuzzy() {
        return new Object[]{
                new ForegroundColorSpan(getColor(FOREGROUND_PRIMARY))
        };
    }
    public Object[] searchConjugated() {
        return new Object[]{
                new ForegroundColorSpan(getColor(FOREGROUND_PRIMARY))
        };
    }
    public Object[] searchConjugatedIntro() {
        return new Object[]{
                new ForegroundColorSpan(getColor(FOREGROUND_SECONDARY)),
                new RelativeSizeSpan(0.66f),
                new StyleSpan(Typeface.ITALIC)

        };
    }

    private class DefinitionLinkSpan extends ClickableSpan {
        final int id;
        public DefinitionLinkSpan(int id) {
            this.id = id;
        }
        public void onClick(View view) {
            System.out.println("asturianu - definition link got clicked on!");
            if(activity != null) {
                System.out.println("asturianu - passed on the message!");
                activity.onLoadEntry(id);
            }
        }
    }

    public static Object[] italic() {
        return new Object[]{
                new StyleSpan(Typeface.ITALIC)
        };
    }
    public static Object[] bold() {
        return new Object[]{
                new StyleSpan(Typeface.BOLD)
        };
    }
    static public class RoundedBackgroundSpan extends ReplacementSpan {

        private int CORNER_RADIUS = 8; // should be static but...
        private int backgroundColor = 0;
        private int textColor = 0;
        public int getCornerRadius() {return CORNER_RADIUS;}
        public int getForegroundColor() {return textColor;}
        public int getBackgroundColor() {return backgroundColor;}

        public RoundedBackgroundSpan(int textColor, int backgroundColor, int cornerRadius) {
            super();
            this.backgroundColor = backgroundColor;
            this.textColor = textColor;
            this.CORNER_RADIUS = cornerRadius;
        }

        public RoundedBackgroundSpan(Context context) {
            super();
            backgroundColor = getColorWrapper(context,R.color.asturian_blue4); // TODO YELLOW?
            textColor = getColorWrapper(context,R.color.white);
        }

        @Override
        public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
            RectF rect = new RectF(x, top, x + measureText(paint, text, start, end), bottom);
            paint.setColor(backgroundColor);
            canvas.drawRoundRect(rect, CORNER_RADIUS, CORNER_RADIUS, paint);
            paint.setColor(textColor);
            canvas.drawText(text, start, end, x, y, paint);
        }

        @Override
        public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
            return Math.round(paint.measureText(text, start, end));
        }

        private float measureText(Paint paint, CharSequence text, int start, int end) {
            return paint.measureText(text, start, end);
        }
    }


    public static int getColorWrapper(Context context, int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getColor(id);
        } else {
            //noinspection deprecation
            return context.getResources().getColor(id);
        }
    }

    /**
     * Returns a CharSequence that represents the text passed in, but
     * with the correct styling applied to it.  The currently supported
     * markup is as follows:
     * <p>
     * **bold** makes the text bold
     * <p>
     * //italic// makes the text italic
     * <p>
     * {{subtext}} currently places the text in [brackets] to show the less
     * important part of the definition.
     * <p>
     * [[link|id]] allows for a piece of text to be clickable, linking to the definition
     * provided by id (must be a number).
     *
     * @param  text  a String containing text with markup
     * @return      formatted text
     */
    public FormattedStringBuilder processMarkup(final String text, FormattedStringBuilder builder) {
        // so h
        final String ITALIC_START = "//";
        final String ITALIC_END = "//";
        final String BOLD_START = "**";
        final String BOLD_END = "**";
        final String SUBTEXT_START = "{{";
        final String SUBTEXT_END = "}}";
        final String LINK_START = "[[";
        final String LINK_END = "]]";
        final String LINK_SEPARATOR = "|";

        int i,j,anchor;
        for(i = 0, j = text.length() - 2,anchor = 0; i < j; i++) {
            switch(text.substring(i,i+2)) {
                case ITALIC_START:
                    int italicEnd = text.indexOf(ITALIC_END,i+2);
                    if(italicEnd == -1) {italicEnd = text.length();}
                    builder.append(text.substring(anchor,i))
                            .beginFormat(italic())
                            .append(text.substring(i+2,italicEnd))
                            .endFormat();
                    i = italicEnd+2;
                    anchor = i;
                    break;
                case BOLD_START:
                    int boldEnd = text.indexOf(BOLD_END,i+2);
                    if(boldEnd == -1) {boldEnd = text.length();}
                    builder.append(text.substring(anchor,i))
                            .beginFormat(bold())
                            .append(text.substring(i+2,boldEnd))
                            .endFormat();

                    i = boldEnd+2;
                    anchor = i;
                    break;
                case SUBTEXT_START:
                    int subEnd = text.indexOf(SUBTEXT_END,i+2);
                    if(subEnd == -1) {subEnd = text.length();}
                    builder.append(text.substring(anchor,i))
                            .beginFormat(secondaryDefinition())
                            .append("["+text.substring(i+2,subEnd)+"]")
                            .endFormat();
                    i = subEnd+2;
                    anchor = i;
                    break;
                case LINK_START:
                    int linkEnd = text.indexOf(LINK_END,i+2);
                    if(linkEnd == -1) {linkEnd = text.length();}

                    int pipe = text.indexOf(LINK_SEPARATOR);
                    builder.append(text.substring(anchor,i));
                    int id = 0;
                    if(pipe > linkEnd || pipe == -1) {
                        // the link is for some reason bad
                        builder.append(text.substring(i+2,linkEnd));
                    }else{
                        // link is AOK
                        String linkText = THIN_SPACE+text.substring(i+2,pipe)+THIN_SPACE;
                        String linkID = text.substring(pipe+1,linkEnd);
                        System.out.println("asturianu - linkID='" + linkID + "'" );
                        try {
                            id = Integer.parseInt(linkID);
                        }catch(NumberFormatException e) {
                            id = ERROR_ENTRY_NUMBER;
                            // todo popup a toast
                        }
                        builder.beginFormat(link(id))
                                .append(linkText)
                                .endFormat();
                    }
                    i = linkEnd + 2;
                    anchor = i;
                    break;

                default:
            }
        }
        if(i > anchor) {
            builder.append(text.substring(anchor,text.length()));
        }

        return builder;
    }
}
