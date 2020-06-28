package org.softastur.asturiandictionary;

public class FormAnalyzer {
    // masks
    private static final int NUMBER   = 0b00000000000000000000000000000011;
    private static final int GENDER   = 0b00000000000000000000000000001100;
    private static final int PERSON   = 0b00000000000000000000000000110000;
    private static final int TENSE    = 0b00000000000000000000000011000000;
    private static final int MOOD     = 0b00000000000000000000011100000000;
    private static final int TYPE     = 0b00000000000000000111100000000000;
    private static final int BASEWORD = 0b11111111111111110000000000000000;

    // specific data points
    private static final int UNSET     = 0b00000000000000000000000000000000;
    // number
    private static final int SINGULAR  = 0b00000000000000000000000000000001;
    private static final int PLURAL    = 0b00000000000000000000000000000010;
    // gender
    private static final int MASCULINE = 0b00000000000000000000000000000100;
    private static final int FEMININE  = 0b00000000000000000000000000001000;
    private static final int NEUTER    = 0b00000000000000000000000000001100;
    // person
    private static final int FIRST     = 0b00000000000000000000000000010000;
    private static final int SECOND    = 0b00000000000000000000000000100000;
    private static final int THIRD     = 0b00000000000000000000000000110000;
    // tense
    private static final int PRESENT   = 0b00000000000000000000000001000000;
    private static final int PAST      = 0b00000000000000000000000010000000;
    private static final int IMPERFECT = 0b00000000000000000000000011000000;
    // mood
    private static final int INDICATIVE= 0b00000000000000000000000100000000;
    private static final int SUBJUNCTIVE=0b00000000000000000000001000000000;
    private static final int POTENTIAL = 0b00000000000000000000001100000000;
    private static final int IMPERATIVE  = 0b00000000000000000000010000000000;
    // word type
    private static final int NOUN        = 0b00000000000000000000000100000000;
    private static final int VERB        = 0b00000000000000000000001000000000;
    private static final int ADVERB      = 0b00000000000000000000001100000000;
    private static final int ADJECTIVE   = 0b00000000000000000000010000000000;
    private static final int PREPOSITION = 0b00000000000000000000010100000000;
    private static final int PRONOUN     = 0b00000000000000000000011000000000;
    private static final int CONJUNCTION = 0b00000000000000000000011100000000;
    private static final int APOCOPE     = 0b00000000000000000000100000000000;
    private static final int ARTICLE     = 0b00000000000000000000100100000000;
    private static final int INTERJECTION= 0b00000000000000000000101000000000;
    private static final int CONTRACTION = 0b00000000000000000000101100000000;
    private static final int PHRASE      = 0b00000000000000000000110000000000;



}
