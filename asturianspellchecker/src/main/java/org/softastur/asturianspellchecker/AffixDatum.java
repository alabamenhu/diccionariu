package org.softastur.asturianspellchecker;

/**
 * Created by guifa on 5/12/15.
 */
public class AffixDatum {
    public static final byte FORBIDDEN = 1;
    public static final byte NEEDS_AFFIX = 2;
    public static final byte KEEPS_CASE = 4;

    public final byte affix;
    public final int remove;
    final byte[] replace;
    final FastMatcher[] matchers;
    final byte[] prefixes;
    final byte[] suffixes;
    final byte meta;

    /**
     * Creates a new AffixDatum with the given parameters.  You should virtually
     * always instantiate a subclasses instead of this parent class.
     *
     * @param _affix the id of the affix
     * @param _remove the length of text to be removed when applying the affix
     * @param _replace the text (as a byte array) to add when applying the affix
     * @param _matchers the matchers used to check if the affix matches words
     * @param _prefixes additional prefixes (ids) that can be applied
     * @param _suffixes additional suffixes (ids) that can be applied
     * @param _meta bitmasked information about the affix, see FORBIDDEN /
     *              NEEDS_AFFIX / KEEPS_CASE values.
     * @return      an AffixDatum object
     */
    public AffixDatum(byte _affix, int _remove, byte[] _replace, FastMatcher[] _matchers, byte[] _prefixes, byte[] _suffixes, byte _meta) {
        affix = _affix;
        remove = _remove;
        replace = _replace;
        matchers = _matchers;
        prefixes = _prefixes;
        suffixes = _suffixes;
        meta = _meta;
    }

    /**
     * Returns a boolean indicating whether the byte sequence fits this
     * affix.  Subclasses MUST override this function, the default is to return
     * false always.
     * @param text a byte array to be matched
     * @return      true if matches
     */
    public boolean matches(byte[] text) {
        return false;
    }

    /**
     * Returns a boolean that shows whether the word associated with this
     * datum is forbidden.
     *
     * @return      true if forbidden
     */
    public final boolean isForbbiden(){
        return 0 != (meta & FORBIDDEN);
    }

    /**
     * Returns a boolean that shows whether the word associated with this
     * datum exists only when an affix modifies it.
     *
     * @return      true if an affix is necessary
     */
    public final boolean needsAffix(){
        return 0 != (meta & NEEDS_AFFIX);
    }

    /**
     * Returns a boolean that shows whether the word associated with this
     * datum exists only when it has an explicit capitalization pattern.
     *
     * @return      true if a particular capitalization is required
     */
    public final boolean keepsCase(){
        return 0 != (meta & KEEPS_CASE);
    }

}
