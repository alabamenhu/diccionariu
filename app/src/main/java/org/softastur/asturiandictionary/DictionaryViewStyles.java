package org.softastur.asturiandictionary;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.style.ForegroundColorSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.StyleSpan;

/**
 * Created by guifa on 5/8/15.
 */
public class DictionaryViewStyles {
    public DictionaryViewStyles() {

    }

    public static final int DARK_TEXT = 0xff000000;
    public static final int LIGHT_TEXT = 0xff555555;
    public static final int COLOR_ONE = 0xff001166;
    public static final int COLOR_TWO = 0xff007733;

    public static Object[] definitionNumber() {
        return new Object[]{
                new StyleSpan(Typeface.BOLD),
                new ForegroundColorSpan(DARK_TEXT)
        };
    }
    public static Object[] explicativeStar() {
        return new Object[]{
                new StyleSpan(Typeface.BOLD_ITALIC),
                new ForegroundColorSpan(COLOR_ONE)
        };
    }
    public static Object[] normalDefinition() {
        return new Object[]{
                new ForegroundColorSpan(Color.BLACK)
        };
    }
    public static Object[] secondaryDefinition() {
        return new Object[]{
                new ForegroundColorSpan(LIGHT_TEXT)
        };
    }
    public static Object[] explicativeDefinition() {
            return new Object[]{
                    new StyleSpan(Typeface.ITALIC),
                    new ForegroundColorSpan(DARK_TEXT)
            };
        }
    public static Object[] secondaryExplicativeDefinition() {
        return new Object[]{
                new StyleSpan(Typeface.ITALIC),
                new ForegroundColorSpan(LIGHT_TEXT)
        };
    }
    public static Object[] definitionExample() {
        return new Object[]{
                new StyleSpan(Typeface.ITALIC),
                new ForegroundColorSpan(COLOR_ONE),
                new LeadingMarginSpan.Standard(75, 100)
        };
    }
    public static Object[] phraseLede() {
        return new Object[]{
                new StyleSpan(Typeface.BOLD),
                new ForegroundColorSpan(COLOR_TWO),
                new LeadingMarginSpan.Standard(0, 75)
        };
    }
    public static Object[] phraseDefinition() {
        return new Object[]{
                new StyleSpan(Typeface.BOLD),
                new ForegroundColorSpan(DARK_TEXT),
                new LeadingMarginSpan.Standard(75, 100)
        };
    }
    public static Object[] phraseDefinitionExample() {
        return new Object[]{
                new StyleSpan(Typeface.ITALIC),
                new ForegroundColorSpan(DARK_TEXT),
                new LeadingMarginSpan.Standard(100,125)

        };
    }

}
