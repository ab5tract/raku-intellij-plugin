package org.raku.comma.parsing;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;

public class RakuParser implements PsiParser {

    @Override
    public ASTNode parse(IElementType root, PsiBuilder builder) {
        PsiBuilder.Marker rootMarker;
        rootMarker = builder.mark();
        this.TOP_4(builder);
        rootMarker.done(root);
        return builder.getTreeBuilt();
    }

    private boolean ENDSTMT_1_alt_1(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean ENDSTMT_1_alt_2(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean ENDSTMT_1_quant_3(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker2;
        altMarker2 = builder.mark();
        if (this.ENDSTMT_1_alt_2(builder, opp)) {
            altMarker2.drop();
        } else {
            altMarker2.rollbackTo();
            PsiBuilder.Marker altMarker1;
            altMarker1 = builder.mark();
            if (this.ENDSTMT_1_alt_1(builder, opp)) {
                altMarker1.drop();
            } else {
                altMarker1.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean ENDSTMT_1(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker quantMarker3;
        quantMarker3 = builder.mark();
        if (this.ENDSTMT_1_quant_3(builder, opp)) {
            quantMarker3.drop();
        } else {
            quantMarker3.rollbackTo();
        }
        return true;
    }

    private boolean EXPR_2_alt_1(PsiBuilder builder, OPP opp) {
        opp.endPrefixes();
        if (!(this.term_224(builder))) {
            return false;
        }
        return true;
    }

    private boolean EXPR_2_quant_2(PsiBuilder builder, OPP opp) {
        if (!(this.prefixish_122(builder))) {
            return false;
        }
        opp.pushPrefix();
        return true;
    }

    private boolean EXPR_2_quant_3(PsiBuilder builder, OPP opp) {
        if (!(this.term_224(builder))) {
            return false;
        }
        return true;
    }

    private boolean EXPR_2_alt_4(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker quantMarker2;
        quantMarker2 = builder.mark();
        if (this.EXPR_2_quant_2(builder, opp)) {
            quantMarker2.drop();
        } else {
            quantMarker2.rollbackTo();
            return false;
        }
        while (true) {
            PsiBuilder.Marker quantMarker3;
            quantMarker3 = builder.mark();
            if (this.EXPR_2_quant_2(builder, opp)) {
                quantMarker3.drop();
            } else {
                quantMarker3.rollbackTo();
                break;
            }
        }
        opp.endPrefixes();
        PsiBuilder.Marker quantMarker4;
        quantMarker4 = builder.mark();
        if (this.EXPR_2_quant_3(builder, opp)) {
            quantMarker4.drop();
        } else {
            quantMarker4.rollbackTo();
        }
        return true;
    }

    private boolean EXPR_2_quant_5(PsiBuilder builder, OPP opp) {
        if (!(this.postfixish_117(builder))) {
            return false;
        }
        opp.pushPostfix();
        return true;
    }

    private boolean EXPR_2_quant_6(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean EXPR_2_alt_7(PsiBuilder builder, OPP opp) {
        opp.startPostfixes();
        return true;
    }

    private boolean EXPR_2_quant_8(PsiBuilder builder, OPP opp) {
        if (!(this.postfixish_117(builder))) {
            return false;
        }
        opp.pushPostfix();
        return true;
    }

    private boolean EXPR_2_alt_9(PsiBuilder builder, OPP opp) {
        if (!(this.nextterm_81(builder))) {
            return false;
        }
        opp.startPostfixes();
        while (true) {
            PsiBuilder.Marker quantMarker9;
            quantMarker9 = builder.mark();
            if (this.EXPR_2_quant_8(builder, opp)) {
                quantMarker9.drop();
            } else {
                quantMarker9.rollbackTo();
                break;
            }
        }
        return true;
    }

    private boolean EXPR_2_alt_10(PsiBuilder builder, OPP opp) {
        opp.endPrefixes();
        PsiBuilder.Marker altMarker10;
        altMarker10 = builder.mark();
        if (this.EXPR_2_alt_9(builder, opp)) {
            altMarker10.drop();
        } else {
            altMarker10.rollbackTo();
            PsiBuilder.Marker altMarker8;
            altMarker8 = builder.mark();
            if (this.EXPR_2_alt_7(builder, opp)) {
                altMarker8.drop();
            } else {
                altMarker8.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean EXPR_2_quant_11(PsiBuilder builder, OPP opp) {
        if (!(this.prefixish_122(builder))) {
            return false;
        }
        opp.pushPrefix();
        return true;
    }

    private boolean EXPR_2_alt_12(PsiBuilder builder, OPP opp) {
        opp.startPostfixes();
        return true;
    }

    private boolean EXPR_2_quant_13(PsiBuilder builder, OPP opp) {
        if (!(this.postfixish_117(builder))) {
            return false;
        }
        opp.pushPostfix();
        return true;
    }

    private boolean EXPR_2_alt_14(PsiBuilder builder, OPP opp) {
        if (!(this.nextterm_81(builder))) {
            return false;
        }
        opp.startPostfixes();
        while (true) {
            PsiBuilder.Marker quantMarker15;
            quantMarker15 = builder.mark();
            if (this.EXPR_2_quant_13(builder, opp)) {
                quantMarker15.drop();
            } else {
                quantMarker15.rollbackTo();
                break;
            }
        }
        return true;
    }

    private boolean EXPR_2_alt_15(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker quantMarker12;
        quantMarker12 = builder.mark();
        if (this.EXPR_2_quant_11(builder, opp)) {
            quantMarker12.drop();
        } else {
            quantMarker12.rollbackTo();
            return false;
        }
        while (true) {
            PsiBuilder.Marker quantMarker13;
            quantMarker13 = builder.mark();
            if (this.EXPR_2_quant_11(builder, opp)) {
                quantMarker13.drop();
            } else {
                quantMarker13.rollbackTo();
                break;
            }
        }
        opp.endPrefixes();
        PsiBuilder.Marker altMarker16;
        altMarker16 = builder.mark();
        if (this.EXPR_2_alt_14(builder, opp)) {
            altMarker16.drop();
        } else {
            altMarker16.rollbackTo();
            PsiBuilder.Marker altMarker14;
            altMarker14 = builder.mark();
            if (this.EXPR_2_alt_12(builder, opp)) {
                altMarker14.drop();
            } else {
                altMarker14.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean EXPR_2_alt_16(PsiBuilder builder, OPP opp) {
        opp.startPrefixes();
        PsiBuilder.Marker altMarker17;
        altMarker17 = builder.mark();
        if (this.EXPR_2_alt_15(builder, opp)) {
            altMarker17.drop();
        } else {
            altMarker17.rollbackTo();
            PsiBuilder.Marker altMarker11;
            altMarker11 = builder.mark();
            if (this.EXPR_2_alt_10(builder, opp)) {
                altMarker11.drop();
            } else {
                altMarker11.rollbackTo();
                return false;
            }
        }
        opp.endPostfixes();
        return true;
    }

    private boolean EXPR_2_alt_17(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.FAKE_INFIX) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean EXPR_2_quant_18(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        opp.startInfix();
        if (!(this.infixish_57(builder))) {
            return false;
        }
        opp.endInfix();
        PsiBuilder.Marker quantMarker7;
        quantMarker7 = builder.mark();
        if (this.EXPR_2_quant_6(builder, opp)) {
            quantMarker7.drop();
        } else {
            quantMarker7.rollbackTo();
        }
        PsiBuilder.Marker altMarker19;
        altMarker19 = builder.mark();
        if (this.EXPR_2_alt_17(builder, opp)) {
            altMarker19.drop();
        } else {
            altMarker19.rollbackTo();
            PsiBuilder.Marker altMarker18;
            altMarker18 = builder.mark();
            if (this.EXPR_2_alt_16(builder, opp)) {
                altMarker18.drop();
            } else {
                altMarker18.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean EXPR_2_alt_19(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean EXPR_2_alt_20(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.END_OF_EXPR) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean EXPR_2(PsiBuilder builder) {
        OPP opp;
        opp = new OPP(builder);
        opp.startExpr();
        opp.startPrefixes();
        PsiBuilder.Marker altMarker5;
        altMarker5 = builder.mark();
        if (this.EXPR_2_alt_4(builder, opp)) {
            altMarker5.drop();
        } else {
            altMarker5.rollbackTo();
            PsiBuilder.Marker altMarker1;
            altMarker1 = builder.mark();
            if (this.EXPR_2_alt_1(builder, opp)) {
                altMarker1.drop();
            } else {
                altMarker1.rollbackTo();
                return false;
            }
        }
        opp.startPostfixes();
        while (true) {
            PsiBuilder.Marker quantMarker6;
            quantMarker6 = builder.mark();
            if (this.EXPR_2_quant_5(builder, opp)) {
                quantMarker6.drop();
            } else {
                quantMarker6.rollbackTo();
                break;
            }
        }
        opp.endPostfixes();
        while (true) {
            PsiBuilder.Marker quantMarker20;
            quantMarker20 = builder.mark();
            if (this.EXPR_2_quant_18(builder, opp)) {
                quantMarker20.drop();
            } else {
                quantMarker20.rollbackTo();
                break;
            }
        }
        opp.endExpr();
        PsiBuilder.Marker altMarker22;
        altMarker22 = builder.mark();
        if (this.EXPR_2_alt_20(builder, opp)) {
            altMarker22.drop();
        } else {
            altMarker22.rollbackTo();
            PsiBuilder.Marker altMarker21;
            altMarker21 = builder.mark();
            if (this.EXPR_2_alt_19(builder, opp)) {
                altMarker21.drop();
            } else {
                altMarker21.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean SIGOK_3_quant_1(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean SIGOK_3(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker quantMarker1;
        quantMarker1 = builder.mark();
        if (this.SIGOK_3_quant_1(builder, opp)) {
            quantMarker1.drop();
        } else {
            quantMarker1.rollbackTo();
        }
        return true;
    }

    private boolean TOP_4_quant_1(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean TOP_4_quant_2(PsiBuilder builder, OPP opp) {
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.BAD_CHARACTER) && (tt1.equals("}"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker quantMarker1;
        quantMarker1 = builder.mark();
        if (this.TOP_4_quant_1(builder, opp)) {
            quantMarker1.drop();
        } else {
            quantMarker1.rollbackTo();
        }
        if (!(this.statementlist_221(builder))) {
            return false;
        }
        return true;
    }

    private boolean TOP_4_quant_3(PsiBuilder builder, OPP opp) {
        if (!(this.bogus_end_18(builder))) {
            return false;
        }
        return true;
    }

    private boolean TOP_4_alt_4(PsiBuilder builder, OPP opp) {
        while (true) {
            PsiBuilder.Marker quantMarker2;
            quantMarker2 = builder.mark();
            if (this.TOP_4_quant_2(builder, opp)) {
                quantMarker2.drop();
            } else {
                quantMarker2.rollbackTo();
                break;
            }
        }
        PsiBuilder.Marker quantMarker3;
        quantMarker3 = builder.mark();
        if (this.TOP_4_quant_3(builder, opp)) {
            quantMarker3.drop();
        } else {
            quantMarker3.rollbackTo();
        }
        return true;
    }

    private boolean TOP_4_alt_5(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean TOP_4(PsiBuilder builder) {
        OPP opp;
        opp = null;
        if (!(this.statementlist_221(builder))) {
            return false;
        }
        PsiBuilder.Marker altMarker5;
        altMarker5 = builder.mark();
        if (this.TOP_4_alt_5(builder, opp)) {
            altMarker5.drop();
        } else {
            altMarker5.rollbackTo();
            PsiBuilder.Marker altMarker4;
            altMarker4 = builder.mark();
            if (this.TOP_4_alt_4(builder, opp)) {
                altMarker4.drop();
            } else {
                altMarker4.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean apostrophe_5(PsiBuilder builder) {
        OPP opp;
        opp = null;
        return true;
    }

    private boolean arglist_6_quant_1(PsiBuilder builder, OPP opp) {
        if (!(this.EXPR_2(builder))) {
            return false;
        }
        return true;
    }

    private boolean arglist_6_alt_2(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker quantMarker1;
        quantMarker1 = builder.mark();
        if (this.arglist_6_quant_1(builder, opp)) {
            quantMarker1.drop();
        } else {
            quantMarker1.rollbackTo();
        }
        return true;
    }

    private boolean arglist_6_alt_3(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.ARGLIST_EMPTY) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean arglist_6(PsiBuilder builder) {
        OPP opp;
        opp = null;
        if (!(this.ws_258(builder))) {
            return false;
        }
        if ((builder.getTokenType()) == RakuTokenTypes.ARGLIST_START) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker altMarker3;
        altMarker3 = builder.mark();
        if (this.arglist_6_alt_3(builder, opp)) {
            altMarker3.drop();
        } else {
            altMarker3.rollbackTo();
            PsiBuilder.Marker altMarker2;
            altMarker2 = builder.mark();
            if (this.arglist_6_alt_2(builder, opp)) {
                altMarker2.drop();
            } else {
                altMarker2.rollbackTo();
                return false;
            }
        }
        if ((builder.getTokenType()) == RakuTokenTypes.ARGLIST_END) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean args_7_alt_1(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.NO_ARGS) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean args_7_alt_2(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.WHITE_SPACE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.arglist_6(builder))) {
            return false;
        }
        return true;
    }

    private boolean args_7_quant_3(PsiBuilder builder, OPP opp) {
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.PARENTHESES_CLOSE) && (tt2.equals(")"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean args_7_alt_4(PsiBuilder builder, OPP opp) {
        if (!(this.unsp_250(builder))) {
            return false;
        }
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.PARENTHESES_OPEN) && (tt1.equals("("))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.semiarglist_167(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker3;
        quantMarker3 = builder.mark();
        if (this.args_7_quant_3(builder, opp)) {
            quantMarker3.drop();
        } else {
            quantMarker3.rollbackTo();
        }
        return true;
    }

    private boolean args_7_quant_5(PsiBuilder builder, OPP opp) {
        String tt4;
        tt4 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.PARENTHESES_CLOSE) && (tt4.equals(")"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean args_7_alt_6(PsiBuilder builder, OPP opp) {
        String tt3;
        tt3 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.PARENTHESES_OPEN) && (tt3.equals("("))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.semiarglist_167(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker5;
        quantMarker5 = builder.mark();
        if (this.args_7_quant_5(builder, opp)) {
            quantMarker5.drop();
        } else {
            quantMarker5.rollbackTo();
        }
        return true;
    }

    private boolean args_7(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker6;
        altMarker6 = builder.mark();
        if (this.args_7_alt_6(builder, opp)) {
            altMarker6.drop();
        } else {
            altMarker6.rollbackTo();
            PsiBuilder.Marker altMarker4;
            altMarker4 = builder.mark();
            if (this.args_7_alt_4(builder, opp)) {
                altMarker4.drop();
            } else {
                altMarker4.rollbackTo();
                PsiBuilder.Marker altMarker2;
                altMarker2 = builder.mark();
                if (this.args_7_alt_2(builder, opp)) {
                    altMarker2.drop();
                } else {
                    altMarker2.rollbackTo();
                    PsiBuilder.Marker altMarker1;
                    altMarker1 = builder.mark();
                    if (this.args_7_alt_1(builder, opp)) {
                        altMarker1.drop();
                    } else {
                        altMarker1.rollbackTo();
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean assertion_8_alt_1(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.REGEX_MISSING_ASSERTION) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean assertion_8_quant_2(PsiBuilder builder, OPP opp) {
        if (!(this.cclass_elem_22(builder))) {
            return false;
        }
        return true;
    }

    private boolean assertion_8_alt_3(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker2;
        marker2 = builder.mark();
        PsiBuilder.Marker quantMarker3;
        quantMarker3 = builder.mark();
        if (this.assertion_8_quant_2(builder, opp)) {
            quantMarker3.drop();
        } else {
            quantMarker3.rollbackTo();
            return false;
        }
        while (true) {
            PsiBuilder.Marker quantMarker4;
            quantMarker4 = builder.mark();
            if (this.assertion_8_quant_2(builder, opp)) {
                quantMarker4.drop();
            } else {
                quantMarker4.rollbackTo();
                break;
            }
        }
        marker2.done(RakuElementTypes.REGEX_CCLASS);
        return true;
    }

    private boolean assertion_8_alt_4(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.REGEX_CAPTURE_NAME) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean assertion_8_alt_5(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.REGEX_CAPTURE_NAME) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean assertion_8_alt_6(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.REGEX_ASSERTION_END) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean assertion_8_quant_7(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker8;
        altMarker8 = builder.mark();
        if (this.assertion_8_alt_6(builder, opp)) {
            altMarker8.drop();
        } else {
            altMarker8.rollbackTo();
            PsiBuilder.Marker altMarker7;
            altMarker7 = builder.mark();
            if (this.assertion_8_alt_5(builder, opp)) {
                altMarker7.drop();
            } else {
                altMarker7.rollbackTo();
                PsiBuilder.Marker altMarker6;
                altMarker6 = builder.mark();
                if (this.assertion_8_alt_4(builder, opp)) {
                    altMarker6.drop();
                } else {
                    altMarker6.rollbackTo();
                    return false;
                }
            }
        }
        return true;
    }

    private boolean assertion_8_alt_8(PsiBuilder builder, OPP opp) {
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.REGEX_INFIX) && (tt1.equals("~~"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker quantMarker9;
        quantMarker9 = builder.mark();
        if (this.assertion_8_quant_7(builder, opp)) {
            quantMarker9.drop();
        } else {
            quantMarker9.rollbackTo();
        }
        return true;
    }

    private boolean assertion_8_alt_9(PsiBuilder builder, OPP opp) {
        if (!(this.variable_253(builder))) {
            return false;
        }
        return true;
    }

    private boolean assertion_8_quant_10(PsiBuilder builder, OPP opp) {
        String tt3;
        tt3 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.PARENTHESES_CLOSE) && (tt3.equals(")"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean assertion_8_alt_11(PsiBuilder builder, OPP opp) {
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.PARENTHESES_OPEN) && (tt2.equals("("))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.rxarglist_157(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker12;
        quantMarker12 = builder.mark();
        if (this.assertion_8_quant_10(builder, opp)) {
            quantMarker12.drop();
        } else {
            quantMarker12.rollbackTo();
        }
        return true;
    }

    private boolean assertion_8_alt_12(PsiBuilder builder, OPP opp) {
        String tt4;
        tt4 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.INVOCANT_MARKER) && (tt4.equals(":"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.rxarglist_157(builder))) {
            return false;
        }
        return true;
    }

    private boolean assertion_8_quant_13(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker14;
        altMarker14 = builder.mark();
        if (this.assertion_8_alt_12(builder, opp)) {
            altMarker14.drop();
        } else {
            altMarker14.rollbackTo();
            PsiBuilder.Marker altMarker13;
            altMarker13 = builder.mark();
            if (this.assertion_8_alt_11(builder, opp)) {
                altMarker13.drop();
            } else {
                altMarker13.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean assertion_8_alt_14(PsiBuilder builder, OPP opp) {
        if (!(this.variable_253(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker15;
        quantMarker15 = builder.mark();
        if (this.assertion_8_quant_13(builder, opp)) {
            quantMarker15.drop();
        } else {
            quantMarker15.rollbackTo();
        }
        return true;
    }

    private boolean assertion_8_alt_15(PsiBuilder builder, OPP opp) {
        if (!(this.rxcodeblock_158(builder))) {
            return false;
        }
        return true;
    }

    private boolean assertion_8_alt_16(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.REGEX_ANCHOR) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean assertion_8_alt_17(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker19;
        marker19 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.REGEX_CAPTURE_NAME) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.longname_colonpairs_67(builder))) {
            return false;
        }
        marker19.done(RakuElementTypes.REGEX_CALL);
        return true;
    }

    private boolean assertion_8_alt_18(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker21;
        marker21 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.METHOD_CALL_NAME) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.longname_colonpairs_67(builder))) {
            return false;
        }
        marker21.done(RakuElementTypes.REGEX_CALL);
        return true;
    }

    private boolean assertion_8_alt_19(PsiBuilder builder, OPP opp) {
        if (!(this.normspace_82(builder))) {
            return false;
        }
        if (!(this.regex_nibbler_151(builder))) {
            return false;
        }
        return true;
    }

    private boolean assertion_8_quant_20(PsiBuilder builder, OPP opp) {
        String tt6;
        tt6 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.PARENTHESES_CLOSE) && (tt6.equals(")"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean assertion_8_alt_21(PsiBuilder builder, OPP opp) {
        String tt5;
        tt5 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.PARENTHESES_OPEN) && (tt5.equals("("))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.rxarglist_157(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker24;
        quantMarker24 = builder.mark();
        if (this.assertion_8_quant_20(builder, opp)) {
            quantMarker24.drop();
        } else {
            quantMarker24.rollbackTo();
        }
        return true;
    }

    private boolean assertion_8_alt_22(PsiBuilder builder, OPP opp) {
        String tt7;
        tt7 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.INVOCANT_MARKER) && (tt7.equals(":"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.rxarglist_157(builder))) {
            return false;
        }
        return true;
    }

    private boolean assertion_8_alt_23(PsiBuilder builder, OPP opp) {
        String tt8;
        tt8 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.INFIX) && (tt8.equals("="))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.assertion_8(builder))) {
            return false;
        }
        return true;
    }

    private boolean assertion_8_alt_24(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.REGEX_ASSERTION_END) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean assertion_8_quant_25(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker28;
        altMarker28 = builder.mark();
        if (this.assertion_8_alt_24(builder, opp)) {
            altMarker28.drop();
        } else {
            altMarker28.rollbackTo();
            PsiBuilder.Marker altMarker27;
            altMarker27 = builder.mark();
            if (this.assertion_8_alt_23(builder, opp)) {
                altMarker27.drop();
            } else {
                altMarker27.rollbackTo();
                PsiBuilder.Marker altMarker26;
                altMarker26 = builder.mark();
                if (this.assertion_8_alt_22(builder, opp)) {
                    altMarker26.drop();
                } else {
                    altMarker26.rollbackTo();
                    PsiBuilder.Marker altMarker25;
                    altMarker25 = builder.mark();
                    if (this.assertion_8_alt_21(builder, opp)) {
                        altMarker25.drop();
                    } else {
                        altMarker25.rollbackTo();
                        PsiBuilder.Marker altMarker23;
                        altMarker23 = builder.mark();
                        if (this.assertion_8_alt_19(builder, opp)) {
                            altMarker23.drop();
                        } else {
                            altMarker23.rollbackTo();
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean assertion_8_alt_26(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker22;
        altMarker22 = builder.mark();
        if (this.assertion_8_alt_18(builder, opp)) {
            altMarker22.drop();
        } else {
            altMarker22.rollbackTo();
            PsiBuilder.Marker altMarker20;
            altMarker20 = builder.mark();
            if (this.assertion_8_alt_17(builder, opp)) {
                altMarker20.drop();
            } else {
                altMarker20.rollbackTo();
                return false;
            }
        }
        PsiBuilder.Marker quantMarker29;
        quantMarker29 = builder.mark();
        if (this.assertion_8_quant_25(builder, opp)) {
            quantMarker29.drop();
        } else {
            quantMarker29.rollbackTo();
        }
        return true;
    }

    private boolean assertion_8_alt_27(PsiBuilder builder, OPP opp) {
        String tt9;
        tt9 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.REGEX_LOOKAROUND) && (tt9.equals("!"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.assertion_8(builder))) {
            return false;
        }
        return true;
    }

    private boolean assertion_8_alt_28(PsiBuilder builder, OPP opp) {
        String tt10;
        tt10 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.REGEX_LOOKAROUND) && (tt10.equals("?"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.assertion_8(builder))) {
            return false;
        }
        return true;
    }

    private boolean assertion_8_alt_29(PsiBuilder builder, OPP opp) {
        String tt11;
        tt11 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.METHOD_CALL_OPERATOR) && (tt11.equals("."))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.assertion_8(builder))) {
            return false;
        }
        return true;
    }

    private boolean assertion_8_alt_30(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.REGEX_ANCHOR) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean assertion_8_alt_31(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.REGEX_ANCHOR) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean assertion_8(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker35;
        altMarker35 = builder.mark();
        if (this.assertion_8_alt_31(builder, opp)) {
            altMarker35.drop();
        } else {
            altMarker35.rollbackTo();
            PsiBuilder.Marker altMarker34;
            altMarker34 = builder.mark();
            if (this.assertion_8_alt_30(builder, opp)) {
                altMarker34.drop();
            } else {
                altMarker34.rollbackTo();
                PsiBuilder.Marker altMarker33;
                altMarker33 = builder.mark();
                if (this.assertion_8_alt_29(builder, opp)) {
                    altMarker33.drop();
                } else {
                    altMarker33.rollbackTo();
                    PsiBuilder.Marker altMarker32;
                    altMarker32 = builder.mark();
                    if (this.assertion_8_alt_28(builder, opp)) {
                        altMarker32.drop();
                    } else {
                        altMarker32.rollbackTo();
                        PsiBuilder.Marker altMarker31;
                        altMarker31 = builder.mark();
                        if (this.assertion_8_alt_27(builder, opp)) {
                            altMarker31.drop();
                        } else {
                            altMarker31.rollbackTo();
                            PsiBuilder.Marker altMarker30;
                            altMarker30 = builder.mark();
                            if (this.assertion_8_alt_26(builder, opp)) {
                                altMarker30.drop();
                            } else {
                                altMarker30.rollbackTo();
                                PsiBuilder.Marker altMarker18;
                                altMarker18 = builder.mark();
                                if (this.assertion_8_alt_16(builder, opp)) {
                                    altMarker18.drop();
                                } else {
                                    altMarker18.rollbackTo();
                                    PsiBuilder.Marker altMarker17;
                                    altMarker17 = builder.mark();
                                    if (this.assertion_8_alt_15(builder, opp)) {
                                        altMarker17.drop();
                                    } else {
                                        altMarker17.rollbackTo();
                                        PsiBuilder.Marker altMarker16;
                                        altMarker16 = builder.mark();
                                        if (this.assertion_8_alt_14(builder, opp)) {
                                            altMarker16.drop();
                                        } else {
                                            altMarker16.rollbackTo();
                                            PsiBuilder.Marker altMarker11;
                                            altMarker11 = builder.mark();
                                            if (this.assertion_8_alt_9(builder, opp)) {
                                                altMarker11.drop();
                                            } else {
                                                altMarker11.rollbackTo();
                                                PsiBuilder.Marker altMarker10;
                                                altMarker10 = builder.mark();
                                                if (this.assertion_8_alt_8(builder, opp)) {
                                                    altMarker10.drop();
                                                } else {
                                                    altMarker10.rollbackTo();
                                                    PsiBuilder.Marker altMarker5;
                                                    altMarker5 = builder.mark();
                                                    if (this.assertion_8_alt_3(builder, opp)) {
                                                        altMarker5.drop();
                                                    } else {
                                                        altMarker5.rollbackTo();
                                                        PsiBuilder.Marker altMarker1;
                                                        altMarker1 = builder.mark();
                                                        if (this.assertion_8_alt_1(builder, opp)) {
                                                            altMarker1.drop();
                                                        } else {
                                                            altMarker1.rollbackTo();
                                                            return false;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean atom_9_alt_1(PsiBuilder builder, OPP opp) {
        if (!(this.metachar_68(builder))) {
            return false;
        }
        return true;
    }

    private boolean atom_9_alt_2(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker2;
        marker2 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_CHAR) {
            builder.advanceLexer();
        } else {
            return false;
        }
        marker2.done(RakuElementTypes.REGEX_LITERAL);
        if (!(this.SIGOK_3(builder))) {
            return false;
        }
        return true;
    }

    private boolean atom_9(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker3;
        altMarker3 = builder.mark();
        if (this.atom_9_alt_2(builder, opp)) {
            altMarker3.drop();
        } else {
            altMarker3.rollbackTo();
            PsiBuilder.Marker altMarker1;
            altMarker1 = builder.mark();
            if (this.atom_9_alt_1(builder, opp)) {
                altMarker1.drop();
            } else {
                altMarker1.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean backmod_10_quant_1(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean backmod_10_alt_2(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean backmod_10_alt_3(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean backmod_10_alt_4(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean backmod_10(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker quantMarker1;
        quantMarker1 = builder.mark();
        if (this.backmod_10_quant_1(builder, opp)) {
            quantMarker1.drop();
        } else {
            quantMarker1.rollbackTo();
        }
        PsiBuilder.Marker altMarker4;
        altMarker4 = builder.mark();
        if (this.backmod_10_alt_4(builder, opp)) {
            altMarker4.drop();
        } else {
            altMarker4.rollbackTo();
            PsiBuilder.Marker altMarker3;
            altMarker3 = builder.mark();
            if (this.backmod_10_alt_3(builder, opp)) {
                altMarker3.drop();
            } else {
                altMarker3.rollbackTo();
                PsiBuilder.Marker altMarker2;
                altMarker2 = builder.mark();
                if (this.backmod_10_alt_2(builder, opp)) {
                    altMarker2.drop();
                } else {
                    altMarker2.rollbackTo();
                    return false;
                }
            }
        }
        return true;
    }

    private boolean backslash_11_alt_1(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.REGEX_BACKSLASH_BAD) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean backslash_11_alt_2(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.REGEX_BUILTIN_CCLASS) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean backslash_11(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        PsiBuilder.Marker altMarker3;
        altMarker3 = builder.mark();
        if (this.backslash_11_alt_2(builder, opp)) {
            altMarker3.drop();
        } else {
            altMarker3.rollbackTo();
            PsiBuilder.Marker altMarker2;
            altMarker2 = builder.mark();
            if (this.backslash_11_alt_1(builder, opp)) {
                altMarker2.drop();
            } else {
                altMarker2.rollbackTo();
                return false;
            }
        }
        marker1.done(RakuElementTypes.REGEX_BUILTIN_CCLASS);
        return true;
    }

    private boolean bare_complex_number_12(PsiBuilder builder) {
        OPP opp;
        opp = null;
        if (!(this.signed_number_176(builder))) {
            return false;
        }
        if ((builder.getTokenType()) == RakuTokenTypes.COMPLEX_LITERAL) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.signed_number_176(builder))) {
            return false;
        }
        if ((builder.getTokenType()) == RakuTokenTypes.COMPLEX_LITERAL) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean bare_rat_number_13(PsiBuilder builder) {
        OPP opp;
        opp = null;
        if (!(this.signed_integer_175(builder))) {
            return false;
        }
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.RAT_LITERAL) && (tt1.equals("/"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.integer_61(builder))) {
            return false;
        }
        return true;
    }

    private boolean binint_14_quant_1(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean binint_14_quant_2(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker quantMarker1;
        quantMarker1 = builder.mark();
        if (this.binint_14_quant_1(builder, opp)) {
            quantMarker1.drop();
        } else {
            quantMarker1.rollbackTo();
            return false;
        }
        while (true) {
            PsiBuilder.Marker quantMarker2;
            quantMarker2 = builder.mark();
            if (this.binint_14_quant_1(builder, opp)) {
                quantMarker2.drop();
            } else {
                quantMarker2.rollbackTo();
                break;
            }
        }
        return true;
    }

    private boolean binint_14(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker quantMarker3;
        quantMarker3 = builder.mark();
        if (this.binint_14_quant_2(builder, opp)) {
            quantMarker3.drop();
        } else {
            quantMarker3.rollbackTo();
            return false;
        }
        while (true) {
            PsiBuilder.Marker quantMarker4;
            quantMarker4 = builder.mark();
            if (this.binint_14_quant_2(builder, opp)) {
                quantMarker4.drop();
            } else {
                quantMarker4.rollbackTo();
                break;
            }
        }
        return true;
    }

    private boolean block_15(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        if (!(this.blockoid_16(builder))) {
            return false;
        }
        marker1.done(RakuElementTypes.BLOCK);
        return true;
    }

    private boolean blockoid_16_quant_1(PsiBuilder builder, OPP opp) {
        if (!(this.unv_251(builder))) {
            return false;
        }
        return true;
    }

    private boolean blockoid_16_quant_2(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker quantMarker2;
        quantMarker2 = builder.mark();
        if (this.blockoid_16_quant_1(builder, opp)) {
            quantMarker2.drop();
        } else {
            quantMarker2.rollbackTo();
        }
        if ((builder.getTokenType()) == RakuTokenTypes.WHITE_SPACE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean blockoid_16_quant_3(PsiBuilder builder, OPP opp) {
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.BLOCK_CURLY_BRACKET_CLOSE) && (tt2.equals("}"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean blockoid_16(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.BLOCK_CURLY_BRACKET_OPEN) && (tt1.equals("{"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker quantMarker3;
        quantMarker3 = builder.mark();
        if (this.blockoid_16_quant_2(builder, opp)) {
            quantMarker3.drop();
        } else {
            quantMarker3.rollbackTo();
        }
        if (!(this.statementlist_221(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker4;
        quantMarker4 = builder.mark();
        if (this.blockoid_16_quant_3(builder, opp)) {
            quantMarker4.drop();
        } else {
            quantMarker4.rollbackTo();
        }
        marker1.done(RakuElementTypes.BLOCKOID);
        return true;
    }

    private boolean blorst_17_alt_1(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.MISSING_BLORST) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean blorst_17_alt_2(PsiBuilder builder, OPP opp) {
        if (!(this.statement_179(builder))) {
            return false;
        }
        return true;
    }

    private boolean blorst_17_alt_3(PsiBuilder builder, OPP opp) {
        if (!(this.block_15(builder))) {
            return false;
        }
        return true;
    }

    private boolean blorst_17(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker3;
        altMarker3 = builder.mark();
        if (this.blorst_17_alt_3(builder, opp)) {
            altMarker3.drop();
        } else {
            altMarker3.rollbackTo();
            PsiBuilder.Marker altMarker2;
            altMarker2 = builder.mark();
            if (this.blorst_17_alt_2(builder, opp)) {
                altMarker2.drop();
            } else {
                altMarker2.rollbackTo();
                PsiBuilder.Marker altMarker1;
                altMarker1 = builder.mark();
                if (this.blorst_17_alt_1(builder, opp)) {
                    altMarker1.drop();
                } else {
                    altMarker1.rollbackTo();
                    return false;
                }
            }
        }
        return true;
    }

    private boolean bogus_end_18(PsiBuilder builder) {
        OPP opp;
        opp = null;
        if ((builder.getTokenType()) == RakuTokenTypes.BAD_CHARACTER) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean bogus_statement_19(PsiBuilder builder) {
        OPP opp;
        opp = null;
        if ((builder.getTokenType()) == RakuTokenTypes.BAD_CHARACTER) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean capterm_20_alt_1(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.CAPTURE_INVALID) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean capterm_20_alt_2(PsiBuilder builder, OPP opp) {
        if (!(this.termish_239(builder))) {
            return false;
        }
        return true;
    }

    private boolean capterm_20_quant_3(PsiBuilder builder, OPP opp) {
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.CAPTURE_TERM) && (tt2.equals(")"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean capterm_20_alt_4(PsiBuilder builder, OPP opp) {
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.CAPTURE_TERM) && (tt1.equals("("))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.semiarglist_167(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker4;
        quantMarker4 = builder.mark();
        if (this.capterm_20_quant_3(builder, opp)) {
            quantMarker4.drop();
        } else {
            quantMarker4.rollbackTo();
        }
        return true;
    }

    private boolean capterm_20(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.CAPTURE_TERM) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker altMarker5;
        altMarker5 = builder.mark();
        if (this.capterm_20_alt_4(builder, opp)) {
            altMarker5.drop();
        } else {
            altMarker5.rollbackTo();
            PsiBuilder.Marker altMarker3;
            altMarker3 = builder.mark();
            if (this.capterm_20_alt_2(builder, opp)) {
                altMarker3.drop();
            } else {
                altMarker3.rollbackTo();
                PsiBuilder.Marker altMarker2;
                altMarker2 = builder.mark();
                if (this.capterm_20_alt_1(builder, opp)) {
                    altMarker2.drop();
                } else {
                    altMarker2.rollbackTo();
                    return false;
                }
            }
        }
        marker1.done(RakuElementTypes.CAPTURE);
        return true;
    }

    private boolean cclass_backslash_21(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.REGEX_BUILTIN_CCLASS) {
            builder.advanceLexer();
        } else {
            return false;
        }
        marker1.done(RakuElementTypes.REGEX_BUILTIN_CCLASS);
        return true;
    }

    private boolean cclass_elem_22_alt_1(PsiBuilder builder, OPP opp) {
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.REGEX_CCLASS_SYNTAX) && (tt1.equals("-"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean cclass_elem_22_alt_2(PsiBuilder builder, OPP opp) {
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.REGEX_CCLASS_SYNTAX) && (tt2.equals("+"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean cclass_elem_22_quant_3(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker3;
        altMarker3 = builder.mark();
        if (this.cclass_elem_22_alt_2(builder, opp)) {
            altMarker3.drop();
        } else {
            altMarker3.rollbackTo();
            PsiBuilder.Marker altMarker2;
            altMarker2 = builder.mark();
            if (this.cclass_elem_22_alt_1(builder, opp)) {
                altMarker2.drop();
            } else {
                altMarker2.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean cclass_elem_22_quant_4(PsiBuilder builder, OPP opp) {
        if (!(this.normspace_82(builder))) {
            return false;
        }
        return true;
    }

    private boolean cclass_elem_22_alt_5(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.REGEX_CCLASS_INCOMPLETE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean cclass_elem_22_alt_6(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.REGEX_BUILTIN_CCLASS) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean cclass_elem_22_alt_7(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.METHOD_CALL_NAME) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean cclass_elem_22_quant_8(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.WHITE_SPACE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean cclass_elem_22_alt_9(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_CHAR) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean cclass_elem_22_alt_10(PsiBuilder builder, OPP opp) {
        if (!(this.cclass_backslash_21(builder))) {
            return false;
        }
        return true;
    }

    private boolean cclass_elem_22_quant_11(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.WHITE_SPACE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean cclass_elem_22_alt_12(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.REGEX_CCLASS_INCOMPLETE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean cclass_elem_22_quant_13(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.WHITE_SPACE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean cclass_elem_22_alt_14(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_CHAR) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean cclass_elem_22_alt_15(PsiBuilder builder, OPP opp) {
        if (!(this.cclass_backslash_21(builder))) {
            return false;
        }
        return true;
    }

    private boolean cclass_elem_22_alt_16(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker quantMarker14;
        quantMarker14 = builder.mark();
        if (this.cclass_elem_22_quant_13(builder, opp)) {
            quantMarker14.drop();
        } else {
            quantMarker14.rollbackTo();
        }
        PsiBuilder.Marker altMarker16;
        altMarker16 = builder.mark();
        if (this.cclass_elem_22_alt_15(builder, opp)) {
            altMarker16.drop();
        } else {
            altMarker16.rollbackTo();
            PsiBuilder.Marker altMarker15;
            altMarker15 = builder.mark();
            if (this.cclass_elem_22_alt_14(builder, opp)) {
                altMarker15.drop();
            } else {
                altMarker15.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean cclass_elem_22_quant_17(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker quantMarker12;
        quantMarker12 = builder.mark();
        if (this.cclass_elem_22_quant_11(builder, opp)) {
            quantMarker12.drop();
        } else {
            quantMarker12.rollbackTo();
        }
        String tt4;
        tt4 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.REGEX_CCLASS_SYNTAX) && (tt4.equals(".."))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker altMarker17;
        altMarker17 = builder.mark();
        if (this.cclass_elem_22_alt_16(builder, opp)) {
            altMarker17.drop();
        } else {
            altMarker17.rollbackTo();
            PsiBuilder.Marker altMarker13;
            altMarker13 = builder.mark();
            if (this.cclass_elem_22_alt_12(builder, opp)) {
                altMarker13.drop();
            } else {
                altMarker13.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean cclass_elem_22_quant_18(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.REGEX_CCLASS_ATOM) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker quantMarker9;
        quantMarker9 = builder.mark();
        if (this.cclass_elem_22_quant_8(builder, opp)) {
            quantMarker9.drop();
        } else {
            quantMarker9.rollbackTo();
        }
        PsiBuilder.Marker altMarker11;
        altMarker11 = builder.mark();
        if (this.cclass_elem_22_alt_10(builder, opp)) {
            altMarker11.drop();
        } else {
            altMarker11.rollbackTo();
            PsiBuilder.Marker altMarker10;
            altMarker10 = builder.mark();
            if (this.cclass_elem_22_alt_9(builder, opp)) {
                altMarker10.drop();
            } else {
                altMarker10.rollbackTo();
                return false;
            }
        }
        PsiBuilder.Marker quantMarker18;
        quantMarker18 = builder.mark();
        if (this.cclass_elem_22_quant_17(builder, opp)) {
            quantMarker18.drop();
        } else {
            quantMarker18.rollbackTo();
        }
        return true;
    }

    private boolean cclass_elem_22_quant_19(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.WHITE_SPACE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean cclass_elem_22_alt_20(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.REGEX_CCLASS_INCOMPLETE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean cclass_elem_22_alt_21(PsiBuilder builder, OPP opp) {
        String tt5;
        tt5 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.REGEX_CCLASS_SYNTAX) && (tt5.equals("]"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean cclass_elem_22_alt_22(PsiBuilder builder, OPP opp) {
        String tt3;
        tt3 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.REGEX_CCLASS_SYNTAX) && (tt3.equals("["))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        while (true) {
            PsiBuilder.Marker quantMarker19;
            quantMarker19 = builder.mark();
            if (this.cclass_elem_22_quant_18(builder, opp)) {
                quantMarker19.drop();
            } else {
                quantMarker19.rollbackTo();
                break;
            }
        }
        PsiBuilder.Marker quantMarker20;
        quantMarker20 = builder.mark();
        if (this.cclass_elem_22_quant_19(builder, opp)) {
            quantMarker20.drop();
        } else {
            quantMarker20.rollbackTo();
        }
        PsiBuilder.Marker altMarker22;
        altMarker22 = builder.mark();
        if (this.cclass_elem_22_alt_21(builder, opp)) {
            altMarker22.drop();
        } else {
            altMarker22.rollbackTo();
            PsiBuilder.Marker altMarker21;
            altMarker21 = builder.mark();
            if (this.cclass_elem_22_alt_20(builder, opp)) {
                altMarker21.drop();
            } else {
                altMarker21.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean cclass_elem_22_quant_23(PsiBuilder builder, OPP opp) {
        if (!(this.normspace_82(builder))) {
            return false;
        }
        return true;
    }

    private boolean cclass_elem_22(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        PsiBuilder.Marker quantMarker4;
        quantMarker4 = builder.mark();
        if (this.cclass_elem_22_quant_3(builder, opp)) {
            quantMarker4.drop();
        } else {
            quantMarker4.rollbackTo();
        }
        PsiBuilder.Marker quantMarker5;
        quantMarker5 = builder.mark();
        if (this.cclass_elem_22_quant_4(builder, opp)) {
            quantMarker5.drop();
        } else {
            quantMarker5.rollbackTo();
        }
        PsiBuilder.Marker altMarker23;
        altMarker23 = builder.mark();
        if (this.cclass_elem_22_alt_22(builder, opp)) {
            altMarker23.drop();
        } else {
            altMarker23.rollbackTo();
            PsiBuilder.Marker altMarker8;
            altMarker8 = builder.mark();
            if (this.cclass_elem_22_alt_7(builder, opp)) {
                altMarker8.drop();
            } else {
                altMarker8.rollbackTo();
                PsiBuilder.Marker altMarker7;
                altMarker7 = builder.mark();
                if (this.cclass_elem_22_alt_6(builder, opp)) {
                    altMarker7.drop();
                } else {
                    altMarker7.rollbackTo();
                    PsiBuilder.Marker altMarker6;
                    altMarker6 = builder.mark();
                    if (this.cclass_elem_22_alt_5(builder, opp)) {
                        altMarker6.drop();
                    } else {
                        altMarker6.rollbackTo();
                        return false;
                    }
                }
            }
        }
        PsiBuilder.Marker quantMarker24;
        quantMarker24 = builder.mark();
        if (this.cclass_elem_22_quant_23(builder, opp)) {
            quantMarker24.drop();
        } else {
            quantMarker24.rollbackTo();
        }
        marker1.done(RakuElementTypes.REGEX_CCLASS_ELEM);
        return true;
    }

    private boolean ccstate_23_alt_1(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean ccstate_23_alt_2(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean ccstate_23(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker2;
        altMarker2 = builder.mark();
        if (this.ccstate_23_alt_2(builder, opp)) {
            altMarker2.drop();
        } else {
            altMarker2.rollbackTo();
            PsiBuilder.Marker altMarker1;
            altMarker1 = builder.mark();
            if (this.ccstate_23_alt_1(builder, opp)) {
                altMarker1.drop();
            } else {
                altMarker1.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean charname_24_quant_1(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean charname_24_alt_2(PsiBuilder builder, OPP opp) {
        while (true) {
            PsiBuilder.Marker quantMarker1;
            quantMarker1 = builder.mark();
            if (this.charname_24_quant_1(builder, opp)) {
                quantMarker1.drop();
            } else {
                quantMarker1.rollbackTo();
                break;
            }
        }
        return true;
    }

    private boolean charname_24_alt_3(PsiBuilder builder, OPP opp) {
        if (!(this.integer_lex_62(builder))) {
            return false;
        }
        return true;
    }

    private boolean charname_24(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker3;
        altMarker3 = builder.mark();
        if (this.charname_24_alt_3(builder, opp)) {
            altMarker3.drop();
        } else {
            altMarker3.rollbackTo();
            PsiBuilder.Marker altMarker2;
            altMarker2 = builder.mark();
            if (this.charname_24_alt_2(builder, opp)) {
                altMarker2.drop();
            } else {
                altMarker2.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean charnames_25_quant_1(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean charnames_25_quant_2(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean charnames_25_quant_3(PsiBuilder builder, OPP opp) {
        while (true) {
            PsiBuilder.Marker quantMarker1;
            quantMarker1 = builder.mark();
            if (this.charnames_25_quant_1(builder, opp)) {
                quantMarker1.drop();
            } else {
                quantMarker1.rollbackTo();
                break;
            }
        }
        if (!(this.charname_24(builder))) {
            return false;
        }
        while (true) {
            PsiBuilder.Marker quantMarker2;
            quantMarker2 = builder.mark();
            if (this.charnames_25_quant_2(builder, opp)) {
                quantMarker2.drop();
            } else {
                quantMarker2.rollbackTo();
                break;
            }
        }
        return true;
    }

    private boolean charnames_25(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker quantMarker3;
        quantMarker3 = builder.mark();
        if (this.charnames_25_quant_3(builder, opp)) {
            quantMarker3.drop();
        } else {
            quantMarker3.rollbackTo();
            return false;
        }
        while (true) {
            PsiBuilder.Marker quantMarker4;
            quantMarker4 = builder.mark();
            if (this.charnames_25_quant_3(builder, opp)) {
                quantMarker4.drop();
            } else {
                quantMarker4.rollbackTo();
                break;
            }
        }
        return true;
    }

    private boolean charspec_26_alt_1(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean charspec_26_quant_2(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean charspec_26_quant_3(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean charspec_26_quant_4(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker quantMarker4;
        quantMarker4 = builder.mark();
        if (this.charspec_26_quant_3(builder, opp)) {
            quantMarker4.drop();
        } else {
            quantMarker4.rollbackTo();
            return false;
        }
        while (true) {
            PsiBuilder.Marker quantMarker5;
            quantMarker5 = builder.mark();
            if (this.charspec_26_quant_3(builder, opp)) {
                quantMarker5.drop();
            } else {
                quantMarker5.rollbackTo();
                break;
            }
        }
        return true;
    }

    private boolean charspec_26_alt_5(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker quantMarker2;
        quantMarker2 = builder.mark();
        if (this.charspec_26_quant_2(builder, opp)) {
            quantMarker2.drop();
        } else {
            quantMarker2.rollbackTo();
            return false;
        }
        while (true) {
            PsiBuilder.Marker quantMarker3;
            quantMarker3 = builder.mark();
            if (this.charspec_26_quant_2(builder, opp)) {
                quantMarker3.drop();
            } else {
                quantMarker3.rollbackTo();
                break;
            }
        }
        while (true) {
            PsiBuilder.Marker quantMarker6;
            quantMarker6 = builder.mark();
            if (this.charspec_26_quant_4(builder, opp)) {
                quantMarker6.drop();
            } else {
                quantMarker6.rollbackTo();
                break;
            }
        }
        return true;
    }

    private boolean charspec_26_alt_6(PsiBuilder builder, OPP opp) {
        if (!(this.charnames_25(builder))) {
            return false;
        }
        return true;
    }

    private boolean charspec_26(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker8;
        altMarker8 = builder.mark();
        if (this.charspec_26_alt_6(builder, opp)) {
            altMarker8.drop();
        } else {
            altMarker8.rollbackTo();
            PsiBuilder.Marker altMarker7;
            altMarker7 = builder.mark();
            if (this.charspec_26_alt_5(builder, opp)) {
                altMarker7.drop();
            } else {
                altMarker7.rollbackTo();
                PsiBuilder.Marker altMarker1;
                altMarker1 = builder.mark();
                if (this.charspec_26_alt_1(builder, opp)) {
                    altMarker1.drop();
                } else {
                    altMarker1.rollbackTo();
                    return false;
                }
            }
        }
        return true;
    }

    private boolean circumfix_27_quant_1(PsiBuilder builder, OPP opp) {
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_CLOSE) && (tt2.equals(">"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean circumfix_27_quant_2(PsiBuilder builder, OPP opp) {
        if (!(this.quote_q_135(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker2;
        quantMarker2 = builder.mark();
        if (this.circumfix_27_quant_1(builder, opp)) {
            quantMarker2.drop();
        } else {
            quantMarker2.rollbackTo();
        }
        return true;
    }

    private boolean circumfix_27_alt_3(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_OPEN) && (tt1.equals("<"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker quantMarker3;
        quantMarker3 = builder.mark();
        if (this.circumfix_27_quant_2(builder, opp)) {
            quantMarker3.drop();
        } else {
            quantMarker3.rollbackTo();
        }
        marker1.done(RakuElementTypes.STRING_LITERAL);
        return true;
    }

    private boolean circumfix_27_quant_4(PsiBuilder builder, OPP opp) {
        String tt4;
        tt4 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_CLOSE) && (tt4.equals("\u00BB"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean circumfix_27_quant_5(PsiBuilder builder, OPP opp) {
        if (!(this.quote_qq_137(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker6;
        quantMarker6 = builder.mark();
        if (this.circumfix_27_quant_4(builder, opp)) {
            quantMarker6.drop();
        } else {
            quantMarker6.rollbackTo();
        }
        return true;
    }

    private boolean circumfix_27_alt_6(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker5;
        marker5 = builder.mark();
        String tt3;
        tt3 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_OPEN) && (tt3.equals("\u00AB"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker quantMarker7;
        quantMarker7 = builder.mark();
        if (this.circumfix_27_quant_5(builder, opp)) {
            quantMarker7.drop();
        } else {
            quantMarker7.rollbackTo();
        }
        marker5.done(RakuElementTypes.STRING_LITERAL);
        return true;
    }

    private boolean circumfix_27_quant_7(PsiBuilder builder, OPP opp) {
        String tt6;
        tt6 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_CLOSE) && (tt6.equals(">>"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean circumfix_27_quant_8(PsiBuilder builder, OPP opp) {
        if (!(this.quote_qq_137(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker10;
        quantMarker10 = builder.mark();
        if (this.circumfix_27_quant_7(builder, opp)) {
            quantMarker10.drop();
        } else {
            quantMarker10.rollbackTo();
        }
        return true;
    }

    private boolean circumfix_27_alt_9(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker9;
        marker9 = builder.mark();
        String tt5;
        tt5 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_OPEN) && (tt5.equals("<<"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker quantMarker11;
        quantMarker11 = builder.mark();
        if (this.circumfix_27_quant_8(builder, opp)) {
            quantMarker11.drop();
        } else {
            quantMarker11.rollbackTo();
        }
        marker9.done(RakuElementTypes.STRING_LITERAL);
        return true;
    }

    private boolean circumfix_27_alt_10(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.BARE_BLOCK) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker marker13;
        marker13 = builder.mark();
        if (!(this.blockoid_16(builder))) {
            return false;
        }
        marker13.done(RakuElementTypes.BLOCK_OR_HASH);
        return true;
    }

    private boolean circumfix_27_quant_11(PsiBuilder builder, OPP opp) {
        String tt8;
        tt8 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.ARRAY_COMPOSER_CLOSE) && (tt8.equals("]"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean circumfix_27_alt_12(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker15;
        marker15 = builder.mark();
        String tt7;
        tt7 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.ARRAY_COMPOSER_OPEN) && (tt7.equals("["))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.semilist_168(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker16;
        quantMarker16 = builder.mark();
        if (this.circumfix_27_quant_11(builder, opp)) {
            quantMarker16.drop();
        } else {
            quantMarker16.rollbackTo();
        }
        marker15.done(RakuElementTypes.ARRAY_COMPOSER);
        return true;
    }

    private boolean circumfix_27_quant_13(PsiBuilder builder, OPP opp) {
        String tt10;
        tt10 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.PARENTHESES_CLOSE) && (tt10.equals(")"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean circumfix_27_alt_14(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker18;
        marker18 = builder.mark();
        String tt9;
        tt9 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.PARENTHESES_OPEN) && (tt9.equals("("))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.semilist_168(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker19;
        quantMarker19 = builder.mark();
        if (this.circumfix_27_quant_13(builder, opp)) {
            quantMarker19.drop();
        } else {
            quantMarker19.rollbackTo();
        }
        marker18.done(RakuElementTypes.PARENTHESIZED_EXPRESSION);
        return true;
    }

    private boolean circumfix_27(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker20;
        altMarker20 = builder.mark();
        if (this.circumfix_27_alt_14(builder, opp)) {
            altMarker20.drop();
        } else {
            altMarker20.rollbackTo();
            PsiBuilder.Marker altMarker17;
            altMarker17 = builder.mark();
            if (this.circumfix_27_alt_12(builder, opp)) {
                altMarker17.drop();
            } else {
                altMarker17.rollbackTo();
                PsiBuilder.Marker altMarker14;
                altMarker14 = builder.mark();
                if (this.circumfix_27_alt_10(builder, opp)) {
                    altMarker14.drop();
                } else {
                    altMarker14.rollbackTo();
                    PsiBuilder.Marker altMarker12;
                    altMarker12 = builder.mark();
                    if (this.circumfix_27_alt_9(builder, opp)) {
                        altMarker12.drop();
                    } else {
                        altMarker12.rollbackTo();
                        PsiBuilder.Marker altMarker8;
                        altMarker8 = builder.mark();
                        if (this.circumfix_27_alt_6(builder, opp)) {
                            altMarker8.drop();
                        } else {
                            altMarker8.rollbackTo();
                            PsiBuilder.Marker altMarker4;
                            altMarker4 = builder.mark();
                            if (this.circumfix_27_alt_3(builder, opp)) {
                                altMarker4.drop();
                            } else {
                                altMarker4.rollbackTo();
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean coloncircumfix_28(PsiBuilder builder) {
        OPP opp;
        opp = null;
        if (!(this.circumfix_27(builder))) {
            return false;
        }
        return true;
    }

    private boolean colonpair_29_alt_1(PsiBuilder builder, OPP opp) {
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.COLON_PAIR) && (tt1.equals(":"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.coloncircumfix_28(builder))) {
            return false;
        }
        return true;
    }

    private boolean colonpair_29_quant_2(PsiBuilder builder, OPP opp) {
        if (!(this.unsp_250(builder))) {
            return false;
        }
        return true;
    }

    private boolean colonpair_29_quant_3(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.COLON_PAIR_HAS_VALUE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker quantMarker3;
        quantMarker3 = builder.mark();
        if (this.colonpair_29_quant_2(builder, opp)) {
            quantMarker3.drop();
        } else {
            quantMarker3.rollbackTo();
        }
        if (!(this.coloncircumfix_28(builder))) {
            return false;
        }
        return true;
    }

    private boolean colonpair_29_alt_4(PsiBuilder builder, OPP opp) {
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.COLON_PAIR) && (tt2.equals(":"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if ((builder.getTokenType()) == RakuTokenTypes.COLON_PAIR) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker quantMarker4;
        quantMarker4 = builder.mark();
        if (this.colonpair_29_quant_3(builder, opp)) {
            quantMarker4.drop();
        } else {
            quantMarker4.rollbackTo();
        }
        return true;
    }

    private boolean colonpair_29_alt_5(PsiBuilder builder, OPP opp) {
        String tt3;
        tt3 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.COLON_PAIR) && (tt3.equals(":"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.colonpair_variable_30(builder))) {
            return false;
        }
        return true;
    }

    private boolean colonpair_29_quant_6(PsiBuilder builder, OPP opp) {
        String tt5;
        tt5 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.COLON_PAIR) && (tt5.equals(")"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean colonpair_29_alt_7(PsiBuilder builder, OPP opp) {
        String tt4;
        tt4 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.COLON_PAIR) && (tt4.equals(":("))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker marker7;
        marker7 = builder.mark();
        if (!(this.signature_174(builder))) {
            return false;
        }
        marker7.done(RakuElementTypes.SIGNATURE);
        PsiBuilder.Marker quantMarker8;
        quantMarker8 = builder.mark();
        if (this.colonpair_29_quant_6(builder, opp)) {
            quantMarker8.drop();
        } else {
            quantMarker8.rollbackTo();
        }
        return true;
    }

    private boolean colonpair_29_alt_8(PsiBuilder builder, OPP opp) {
        String tt6;
        tt6 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.COLON_PAIR) && (tt6.equals(":!"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if ((builder.getTokenType()) == RakuTokenTypes.COLON_PAIR) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean colonpair_29_alt_9(PsiBuilder builder, OPP opp) {
        String tt7;
        tt7 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.COLON_PAIR) && (tt7.equals(":"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if ((builder.getTokenType()) == RakuTokenTypes.INTEGER_LITERAL) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if ((builder.getTokenType()) == RakuTokenTypes.COLON_PAIR) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean colonpair_29(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        PsiBuilder.Marker altMarker11;
        altMarker11 = builder.mark();
        if (this.colonpair_29_alt_9(builder, opp)) {
            altMarker11.drop();
        } else {
            altMarker11.rollbackTo();
            PsiBuilder.Marker altMarker10;
            altMarker10 = builder.mark();
            if (this.colonpair_29_alt_8(builder, opp)) {
                altMarker10.drop();
            } else {
                altMarker10.rollbackTo();
                PsiBuilder.Marker altMarker9;
                altMarker9 = builder.mark();
                if (this.colonpair_29_alt_7(builder, opp)) {
                    altMarker9.drop();
                } else {
                    altMarker9.rollbackTo();
                    PsiBuilder.Marker altMarker6;
                    altMarker6 = builder.mark();
                    if (this.colonpair_29_alt_5(builder, opp)) {
                        altMarker6.drop();
                    } else {
                        altMarker6.rollbackTo();
                        PsiBuilder.Marker altMarker5;
                        altMarker5 = builder.mark();
                        if (this.colonpair_29_alt_4(builder, opp)) {
                            altMarker5.drop();
                        } else {
                            altMarker5.rollbackTo();
                            PsiBuilder.Marker altMarker2;
                            altMarker2 = builder.mark();
                            if (this.colonpair_29_alt_1(builder, opp)) {
                                altMarker2.drop();
                            } else {
                                altMarker2.rollbackTo();
                                return false;
                            }
                        }
                    }
                }
            }
        }
        marker1.done(RakuElementTypes.COLON_PAIR);
        return true;
    }

    private boolean colonpair_variable_30_alt_1(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.REGEX_CAPTURE_NAME) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean colonpair_variable_30_alt_2(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.VARIABLE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean colonpair_variable_30(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        PsiBuilder.Marker altMarker3;
        altMarker3 = builder.mark();
        if (this.colonpair_variable_30_alt_2(builder, opp)) {
            altMarker3.drop();
        } else {
            altMarker3.rollbackTo();
            PsiBuilder.Marker altMarker2;
            altMarker2 = builder.mark();
            if (this.colonpair_variable_30_alt_1(builder, opp)) {
                altMarker2.drop();
            } else {
                altMarker2.rollbackTo();
                return false;
            }
        }
        marker1.done(RakuElementTypes.VARIABLE);
        return true;
    }

    private boolean comment_31_alt_1(PsiBuilder builder, OPP opp) {
        if (!(this.plaincomment_97(builder))) {
            return false;
        }
        return true;
    }

    private boolean comment_31_alt_2(PsiBuilder builder, OPP opp) {
        if (!(this.multilinecomment_77(builder))) {
            return false;
        }
        return true;
    }

    private boolean comment_31_alt_3(PsiBuilder builder, OPP opp) {
        if (!(this.postcomment_112(builder))) {
            return false;
        }
        return true;
    }

    private boolean comment_31_alt_4(PsiBuilder builder, OPP opp) {
        if (!(this.postcommentmulti_113(builder))) {
            return false;
        }
        return true;
    }

    private boolean comment_31_alt_5(PsiBuilder builder, OPP opp) {
        if (!(this.precomment_119(builder))) {
            return false;
        }
        return true;
    }

    private boolean comment_31_alt_6(PsiBuilder builder, OPP opp) {
        if (!(this.precommentmulti_120(builder))) {
            return false;
        }
        return true;
    }

    private boolean comment_31(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker6;
        altMarker6 = builder.mark();
        if (this.comment_31_alt_6(builder, opp)) {
            altMarker6.drop();
        } else {
            altMarker6.rollbackTo();
            PsiBuilder.Marker altMarker5;
            altMarker5 = builder.mark();
            if (this.comment_31_alt_5(builder, opp)) {
                altMarker5.drop();
            } else {
                altMarker5.rollbackTo();
                PsiBuilder.Marker altMarker4;
                altMarker4 = builder.mark();
                if (this.comment_31_alt_4(builder, opp)) {
                    altMarker4.drop();
                } else {
                    altMarker4.rollbackTo();
                    PsiBuilder.Marker altMarker3;
                    altMarker3 = builder.mark();
                    if (this.comment_31_alt_3(builder, opp)) {
                        altMarker3.drop();
                    } else {
                        altMarker3.rollbackTo();
                        PsiBuilder.Marker altMarker2;
                        altMarker2 = builder.mark();
                        if (this.comment_31_alt_2(builder, opp)) {
                            altMarker2.drop();
                        } else {
                            altMarker2.rollbackTo();
                            PsiBuilder.Marker altMarker1;
                            altMarker1 = builder.mark();
                            if (this.comment_31_alt_1(builder, opp)) {
                                altMarker1.drop();
                            } else {
                                altMarker1.rollbackTo();
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean complex_number_32(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.COMPLEX_LITERAL) && (tt1.equals("<"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.bare_complex_number_12(builder))) {
            return false;
        }
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.COMPLEX_LITERAL) && (tt2.equals(">"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        marker1.done(RakuElementTypes.COMPLEX_LITERAL);
        return true;
    }

    private boolean contextualizer_33_quant_1(PsiBuilder builder, OPP opp) {
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.CONTEXTUALIZER_CLOSE) && (tt2.equals(")"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean contextualizer_33_alt_2(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.CONTEXTUALIZER) {
            builder.advanceLexer();
        } else {
            return false;
        }
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.CONTEXTUALIZER_OPEN) && (tt1.equals("("))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.semilist_168(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker2;
        quantMarker2 = builder.mark();
        if (this.contextualizer_33_quant_1(builder, opp)) {
            quantMarker2.drop();
        } else {
            quantMarker2.rollbackTo();
        }
        return true;
    }

    private boolean contextualizer_33_alt_3(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.CIRCUMFIX_CONTEXTUALIZER) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if ((builder.getTokenType()) == RakuTokenTypes.CONTEXTUALIZER) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.circumfix_27(builder))) {
            return false;
        }
        return true;
    }

    private boolean contextualizer_33(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        PsiBuilder.Marker altMarker4;
        altMarker4 = builder.mark();
        if (this.contextualizer_33_alt_3(builder, opp)) {
            altMarker4.drop();
        } else {
            altMarker4.rollbackTo();
            PsiBuilder.Marker altMarker3;
            altMarker3 = builder.mark();
            if (this.contextualizer_33_alt_2(builder, opp)) {
                altMarker3.drop();
            } else {
                altMarker3.rollbackTo();
                return false;
            }
        }
        marker1.done(RakuElementTypes.CONTEXTUALIZER);
        return true;
    }

    private boolean dec_number_34_alt_1(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.RAT_LITERAL) {
            builder.advanceLexer();
        } else {
            return false;
        }
        marker1.done(RakuElementTypes.RAT_LITERAL);
        return true;
    }

    private boolean dec_number_34_alt_2(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker3;
        marker3 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.NUMBER_LITERAL) {
            builder.advanceLexer();
        } else {
            return false;
        }
        marker3.done(RakuElementTypes.NUMBER_LITERAL);
        return true;
    }

    private boolean dec_number_34(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker4;
        altMarker4 = builder.mark();
        if (this.dec_number_34_alt_2(builder, opp)) {
            altMarker4.drop();
        } else {
            altMarker4.rollbackTo();
            PsiBuilder.Marker altMarker2;
            altMarker2 = builder.mark();
            if (this.dec_number_34_alt_1(builder, opp)) {
                altMarker2.drop();
            } else {
                altMarker2.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean decint_35_quant_1(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean decint_35_quant_2(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker quantMarker1;
        quantMarker1 = builder.mark();
        if (this.decint_35_quant_1(builder, opp)) {
            quantMarker1.drop();
        } else {
            quantMarker1.rollbackTo();
            return false;
        }
        while (true) {
            PsiBuilder.Marker quantMarker2;
            quantMarker2 = builder.mark();
            if (this.decint_35_quant_1(builder, opp)) {
                quantMarker2.drop();
            } else {
                quantMarker2.rollbackTo();
                break;
            }
        }
        return true;
    }

    private boolean decint_35(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker quantMarker3;
        quantMarker3 = builder.mark();
        if (this.decint_35_quant_2(builder, opp)) {
            quantMarker3.drop();
        } else {
            quantMarker3.rollbackTo();
            return false;
        }
        while (true) {
            PsiBuilder.Marker quantMarker4;
            quantMarker4 = builder.mark();
            if (this.decint_35_quant_2(builder, opp)) {
                quantMarker4.drop();
            } else {
                quantMarker4.rollbackTo();
                break;
            }
        }
        return true;
    }

    private boolean declarator_36_alt_1(PsiBuilder builder, OPP opp) {
        if (!(this.type_declarator_248(builder))) {
            return false;
        }
        return true;
    }

    private boolean declarator_36_alt_2(PsiBuilder builder, OPP opp) {
        if (!(this.regex_declarator_149(builder))) {
            return false;
        }
        return true;
    }

    private boolean declarator_36_alt_3(PsiBuilder builder, OPP opp) {
        if (!(this.routine_declarator_153(builder))) {
            return false;
        }
        return true;
    }

    private boolean declarator_36_quant_4(PsiBuilder builder, OPP opp) {
        if (!(this.trait_242(builder))) {
            return false;
        }
        return true;
    }

    private boolean declarator_36_quant_5(PsiBuilder builder, OPP opp) {
        if (!(this.initializer_60(builder))) {
            return false;
        }
        return true;
    }

    private boolean declarator_36_quant_6(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker7;
        quantMarker7 = builder.mark();
        if (this.declarator_36_quant_5(builder, opp)) {
            quantMarker7.drop();
        } else {
            quantMarker7.rollbackTo();
        }
        return true;
    }

    private boolean declarator_36_quant_7(PsiBuilder builder, OPP opp) {
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.PARENTHESES_CLOSE) && (tt2.equals(")"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        while (true) {
            PsiBuilder.Marker quantMarker6;
            quantMarker6 = builder.mark();
            if (this.declarator_36_quant_4(builder, opp)) {
                quantMarker6.drop();
            } else {
                quantMarker6.rollbackTo();
                break;
            }
        }
        PsiBuilder.Marker quantMarker8;
        quantMarker8 = builder.mark();
        if (this.declarator_36_quant_6(builder, opp)) {
            quantMarker8.drop();
        } else {
            quantMarker8.rollbackTo();
        }
        return true;
    }

    private boolean declarator_36_alt_8(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker4;
        marker4 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.PARENTHESES_OPEN) && (tt1.equals("("))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker marker5;
        marker5 = builder.mark();
        if (!(this.signature_174(builder))) {
            return false;
        }
        marker5.done(RakuElementTypes.SIGNATURE);
        PsiBuilder.Marker quantMarker9;
        quantMarker9 = builder.mark();
        if (this.declarator_36_quant_7(builder, opp)) {
            quantMarker9.drop();
        } else {
            quantMarker9.rollbackTo();
        }
        marker4.done(RakuElementTypes.VARIABLE_DECLARATION);
        return true;
    }

    private boolean declarator_36_quant_9(PsiBuilder builder, OPP opp) {
        if (!(this.initializer_60(builder))) {
            return false;
        }
        return true;
    }

    private boolean declarator_36_quant_10(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker12;
        quantMarker12 = builder.mark();
        if (this.declarator_36_quant_9(builder, opp)) {
            quantMarker12.drop();
        } else {
            quantMarker12.rollbackTo();
        }
        return true;
    }

    private boolean declarator_36_alt_11(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker11;
        marker11 = builder.mark();
        if (!(this.variable_declarator_254(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker13;
        quantMarker13 = builder.mark();
        if (this.declarator_36_quant_10(builder, opp)) {
            quantMarker13.drop();
        } else {
            quantMarker13.rollbackTo();
        }
        marker11.done(RakuElementTypes.VARIABLE_DECLARATION);
        return true;
    }

    private boolean declarator_36_quant_12(PsiBuilder builder, OPP opp) {
        if (!(this.initializer_60(builder))) {
            return false;
        }
        return true;
    }

    private boolean declarator_36_quant_13(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker16;
        quantMarker16 = builder.mark();
        if (this.declarator_36_quant_12(builder, opp)) {
            quantMarker16.drop();
        } else {
            quantMarker16.rollbackTo();
        }
        return true;
    }

    private boolean declarator_36_alt_14(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker15;
        marker15 = builder.mark();
        String tt3;
        tt3 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.TERM_DECLARATION_BACKSLASH) && (tt3.equals("\\"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.defterm_38(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker17;
        quantMarker17 = builder.mark();
        if (this.declarator_36_quant_13(builder, opp)) {
            quantMarker17.drop();
        } else {
            quantMarker17.rollbackTo();
        }
        marker15.done(RakuElementTypes.VARIABLE_DECLARATION);
        return true;
    }

    private boolean declarator_36(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker18;
        altMarker18 = builder.mark();
        if (this.declarator_36_alt_14(builder, opp)) {
            altMarker18.drop();
        } else {
            altMarker18.rollbackTo();
            PsiBuilder.Marker altMarker14;
            altMarker14 = builder.mark();
            if (this.declarator_36_alt_11(builder, opp)) {
                altMarker14.drop();
            } else {
                altMarker14.rollbackTo();
                PsiBuilder.Marker altMarker10;
                altMarker10 = builder.mark();
                if (this.declarator_36_alt_8(builder, opp)) {
                    altMarker10.drop();
                } else {
                    altMarker10.rollbackTo();
                    PsiBuilder.Marker altMarker3;
                    altMarker3 = builder.mark();
                    if (this.declarator_36_alt_3(builder, opp)) {
                        altMarker3.drop();
                    } else {
                        altMarker3.rollbackTo();
                        PsiBuilder.Marker altMarker2;
                        altMarker2 = builder.mark();
                        if (this.declarator_36_alt_2(builder, opp)) {
                            altMarker2.drop();
                        } else {
                            altMarker2.rollbackTo();
                            PsiBuilder.Marker altMarker1;
                            altMarker1 = builder.mark();
                            if (this.declarator_36_alt_1(builder, opp)) {
                                altMarker1.drop();
                            } else {
                                altMarker1.rollbackTo();
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean default_value_37_quant_1(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean default_value_37_quant_2(PsiBuilder builder, OPP opp) {
        if (!(this.EXPR_2(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker2;
        quantMarker2 = builder.mark();
        if (this.default_value_37_quant_1(builder, opp)) {
            quantMarker2.drop();
        } else {
            quantMarker2.rollbackTo();
        }
        return true;
    }

    private boolean default_value_37(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.INFIX) && (tt1.equals("="))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker3;
        quantMarker3 = builder.mark();
        if (this.default_value_37_quant_2(builder, opp)) {
            quantMarker3.drop();
        } else {
            quantMarker3.rollbackTo();
        }
        marker1.done(RakuElementTypes.PARAMETER_DEFAULT);
        return true;
    }

    private boolean defterm_38(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.TERM) {
            builder.advanceLexer();
        } else {
            return false;
        }
        marker1.done(RakuElementTypes.TERM_DEFINITION);
        return true;
    }

    private boolean desigilname_39(PsiBuilder builder) {
        OPP opp;
        opp = null;
        if (!(this.name_79(builder))) {
            return false;
        }
        return true;
    }

    private boolean dotty_40(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.METHOD_CALL_OPERATOR) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.dottyop_41(builder))) {
            return false;
        }
        marker1.done(RakuElementTypes.METHOD_CALL);
        return true;
    }

    private boolean dottyop_41_quant_1(PsiBuilder builder, OPP opp) {
        if (!(this.unsp_250(builder))) {
            return false;
        }
        return true;
    }

    private boolean dottyop_41_quant_2(PsiBuilder builder, OPP opp) {
        if (!(this.methodop_71(builder))) {
            return false;
        }
        return true;
    }

    private boolean dottyop_41(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker quantMarker1;
        quantMarker1 = builder.mark();
        if (this.dottyop_41_quant_1(builder, opp)) {
            quantMarker1.drop();
        } else {
            quantMarker1.rollbackTo();
        }
        PsiBuilder.Marker quantMarker2;
        quantMarker2 = builder.mark();
        if (this.dottyop_41_quant_2(builder, opp)) {
            quantMarker2.drop();
        } else {
            quantMarker2.rollbackTo();
        }
        return true;
    }

    private boolean eat_terminator_42_quant_1(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean eat_terminator_42_quant_2(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.EAT_TERMINATOR_HAS_HEREDOC) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker quantMarker1;
        quantMarker1 = builder.mark();
        if (this.eat_terminator_42_quant_1(builder, opp)) {
            quantMarker1.drop();
        } else {
            quantMarker1.rollbackTo();
        }
        return true;
    }

    private boolean eat_terminator_42_alt_3(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean eat_terminator_42_quant_4(PsiBuilder builder, OPP opp) {
        if (!(this.unv_251(builder))) {
            return false;
        }
        return true;
    }

    private boolean eat_terminator_42_alt_5(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.END_OF_STATEMENT) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean eat_terminator_42_alt_6(PsiBuilder builder, OPP opp) {
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STATEMENT_TERMINATOR) && (tt1.equals(";"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean eat_terminator_42_alt_7(PsiBuilder builder, OPP opp) {
        while (true) {
            PsiBuilder.Marker quantMarker4;
            quantMarker4 = builder.mark();
            if (this.eat_terminator_42_quant_4(builder, opp)) {
                quantMarker4.drop();
            } else {
                quantMarker4.rollbackTo();
                break;
            }
        }
        PsiBuilder.Marker marker5;
        marker5 = builder.mark();
        marker5.done(RakuElementTypes.UNTERMINATED_STATEMENT);
        if (!(this.bogus_statement_19(builder))) {
            return false;
        }
        PsiBuilder.Marker altMarker7;
        altMarker7 = builder.mark();
        if (this.eat_terminator_42_alt_6(builder, opp)) {
            altMarker7.drop();
        } else {
            altMarker7.rollbackTo();
            PsiBuilder.Marker altMarker6;
            altMarker6 = builder.mark();
            if (this.eat_terminator_42_alt_5(builder, opp)) {
                altMarker6.drop();
            } else {
                altMarker6.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean eat_terminator_42_quant_8(PsiBuilder builder, OPP opp) {
        if (!(this.unv_251(builder))) {
            return false;
        }
        return true;
    }

    private boolean eat_terminator_42_alt_9(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.END_OF_STATEMENT_STOPPER) {
            builder.advanceLexer();
        } else {
            return false;
        }
        while (true) {
            PsiBuilder.Marker quantMarker9;
            quantMarker9 = builder.mark();
            if (this.eat_terminator_42_quant_8(builder, opp)) {
                quantMarker9.drop();
            } else {
                quantMarker9.rollbackTo();
                break;
            }
        }
        PsiBuilder.Marker marker10;
        marker10 = builder.mark();
        marker10.done(RakuElementTypes.UNTERMINATED_STATEMENT);
        return true;
    }

    private boolean eat_terminator_42_alt_10(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.END_OF_STATEMENT_MARK) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean eat_terminator_42_alt_11(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STATEMENT_TERMINATOR) && (tt2.equals(";"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean eat_terminator_42_alt_12(PsiBuilder builder, OPP opp) {
        String tt3;
        tt3 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STATEMENT_TERMINATOR) && (tt3.equals(";"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean eat_terminator_42_quant_13(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker14;
        altMarker14 = builder.mark();
        if (this.eat_terminator_42_alt_12(builder, opp)) {
            altMarker14.drop();
        } else {
            altMarker14.rollbackTo();
            PsiBuilder.Marker altMarker13;
            altMarker13 = builder.mark();
            if (this.eat_terminator_42_alt_11(builder, opp)) {
                altMarker13.drop();
            } else {
                altMarker13.rollbackTo();
                PsiBuilder.Marker altMarker12;
                altMarker12 = builder.mark();
                if (this.eat_terminator_42_alt_10(builder, opp)) {
                    altMarker12.drop();
                } else {
                    altMarker12.rollbackTo();
                    PsiBuilder.Marker altMarker11;
                    altMarker11 = builder.mark();
                    if (this.eat_terminator_42_alt_9(builder, opp)) {
                        altMarker11.drop();
                    } else {
                        altMarker11.rollbackTo();
                        PsiBuilder.Marker altMarker8;
                        altMarker8 = builder.mark();
                        if (this.eat_terminator_42_alt_7(builder, opp)) {
                            altMarker8.drop();
                        } else {
                            altMarker8.rollbackTo();
                            PsiBuilder.Marker altMarker3;
                            altMarker3 = builder.mark();
                            if (this.eat_terminator_42_alt_3(builder, opp)) {
                                altMarker3.drop();
                            } else {
                                altMarker3.rollbackTo();
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean eat_terminator_42(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker quantMarker2;
        quantMarker2 = builder.mark();
        if (this.eat_terminator_42_quant_2(builder, opp)) {
            quantMarker2.drop();
        } else {
            quantMarker2.rollbackTo();
        }
        PsiBuilder.Marker quantMarker15;
        quantMarker15 = builder.mark();
        if (this.eat_terminator_42_quant_13(builder, opp)) {
            quantMarker15.drop();
        } else {
            quantMarker15.rollbackTo();
        }
        return true;
    }

    private boolean end_keyword_43(PsiBuilder builder) {
        OPP opp;
        opp = null;
        return true;
    }

    private boolean end_prefix_44(PsiBuilder builder) {
        OPP opp;
        opp = null;
        if (!(this.end_keyword_43(builder))) {
            return false;
        }
        if ((builder.getTokenType()) == RakuTokenTypes.WHITE_SPACE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean enter_regex_nibbler_45(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        if (!(this.regex_nibbler_151(builder))) {
            return false;
        }
        marker1.done(RakuElementTypes.REGEX);
        return true;
    }

    private boolean escale_46(PsiBuilder builder) {
        OPP opp;
        opp = null;
        if (!(this.sign_173(builder))) {
            return false;
        }
        if (!(this.decint_35(builder))) {
            return false;
        }
        return true;
    }

    private boolean fatarrow_47_quant_1(PsiBuilder builder, OPP opp) {
        if (!(this.EXPR_2(builder))) {
            return false;
        }
        return true;
    }

    private boolean fatarrow_47(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.PAIR_KEY) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if ((builder.getTokenType()) == RakuTokenTypes.WHITE_SPACE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.INFIX) && (tt1.equals("=>"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker2;
        quantMarker2 = builder.mark();
        if (this.fatarrow_47_quant_1(builder, opp)) {
            quantMarker2.drop();
        } else {
            quantMarker2.rollbackTo();
        }
        marker1.done(RakuElementTypes.FATARROW);
        return true;
    }

    private boolean hasdelimiter_48(PsiBuilder builder) {
        OPP opp;
        opp = null;
        return true;
    }

    private boolean heredoc_49_quant_1(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_CLOSE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean heredoc_49_quant_2(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.HEREDOC) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.quote_nibbler_134(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker2;
        quantMarker2 = builder.mark();
        if (this.heredoc_49_quant_1(builder, opp)) {
            quantMarker2.drop();
        } else {
            quantMarker2.rollbackTo();
        }
        marker1.done(RakuElementTypes.HEREDOC);
        return true;
    }

    private boolean heredoc_49(PsiBuilder builder) {
        OPP opp;
        opp = null;
        while (true) {
            PsiBuilder.Marker quantMarker3;
            quantMarker3 = builder.mark();
            if (this.heredoc_49_quant_2(builder, opp)) {
                quantMarker3.drop();
            } else {
                quantMarker3.rollbackTo();
                break;
            }
        }
        return true;
    }

    private boolean hexint_50_alt_1(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean hexint_50_alt_2(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean hexint_50_quant_3(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker2;
        altMarker2 = builder.mark();
        if (this.hexint_50_alt_2(builder, opp)) {
            altMarker2.drop();
        } else {
            altMarker2.rollbackTo();
            PsiBuilder.Marker altMarker1;
            altMarker1 = builder.mark();
            if (this.hexint_50_alt_1(builder, opp)) {
                altMarker1.drop();
            } else {
                altMarker1.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean hexint_50_quant_4(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker quantMarker3;
        quantMarker3 = builder.mark();
        if (this.hexint_50_quant_3(builder, opp)) {
            quantMarker3.drop();
        } else {
            quantMarker3.rollbackTo();
            return false;
        }
        while (true) {
            PsiBuilder.Marker quantMarker4;
            quantMarker4 = builder.mark();
            if (this.hexint_50_quant_3(builder, opp)) {
                quantMarker4.drop();
            } else {
                quantMarker4.rollbackTo();
                break;
            }
        }
        return true;
    }

    private boolean hexint_50(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker quantMarker5;
        quantMarker5 = builder.mark();
        if (this.hexint_50_quant_4(builder, opp)) {
            quantMarker5.drop();
        } else {
            quantMarker5.rollbackTo();
            return false;
        }
        while (true) {
            PsiBuilder.Marker quantMarker6;
            quantMarker6 = builder.mark();
            if (this.hexint_50_quant_4(builder, opp)) {
                quantMarker6.drop();
            } else {
                quantMarker6.rollbackTo();
                break;
            }
        }
        return true;
    }

    private boolean hexints_51_quant_1(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean hexints_51_quant_2(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean hexints_51_quant_3(PsiBuilder builder, OPP opp) {
        while (true) {
            PsiBuilder.Marker quantMarker1;
            quantMarker1 = builder.mark();
            if (this.hexints_51_quant_1(builder, opp)) {
                quantMarker1.drop();
            } else {
                quantMarker1.rollbackTo();
                break;
            }
        }
        if (!(this.hexint_50(builder))) {
            return false;
        }
        while (true) {
            PsiBuilder.Marker quantMarker2;
            quantMarker2 = builder.mark();
            if (this.hexints_51_quant_2(builder, opp)) {
                quantMarker2.drop();
            } else {
                quantMarker2.rollbackTo();
                break;
            }
        }
        return true;
    }

    private boolean hexints_51(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker quantMarker3;
        quantMarker3 = builder.mark();
        if (this.hexints_51_quant_3(builder, opp)) {
            quantMarker3.drop();
        } else {
            quantMarker3.rollbackTo();
            return false;
        }
        while (true) {
            PsiBuilder.Marker quantMarker4;
            quantMarker4 = builder.mark();
            if (this.hexints_51_quant_3(builder, opp)) {
                quantMarker4.drop();
            } else {
                quantMarker4.rollbackTo();
                break;
            }
        }
        return true;
    }

    private boolean ident_52_alt_1(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean ident_52_alt_2(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean ident_52_quant_3(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean ident_52(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker2;
        altMarker2 = builder.mark();
        if (this.ident_52_alt_2(builder, opp)) {
            altMarker2.drop();
        } else {
            altMarker2.rollbackTo();
            PsiBuilder.Marker altMarker1;
            altMarker1 = builder.mark();
            if (this.ident_52_alt_1(builder, opp)) {
                altMarker1.drop();
            } else {
                altMarker1.rollbackTo();
                return false;
            }
        }
        while (true) {
            PsiBuilder.Marker quantMarker3;
            quantMarker3 = builder.mark();
            if (this.ident_52_quant_3(builder, opp)) {
                quantMarker3.drop();
            } else {
                quantMarker3.rollbackTo();
                break;
            }
        }
        return true;
    }

    private boolean identifier_53_quant_1(PsiBuilder builder, OPP opp) {
        if (!(this.apostrophe_5(builder))) {
            return false;
        }
        if (!(this.ident_52(builder))) {
            return false;
        }
        return true;
    }

    private boolean identifier_53(PsiBuilder builder) {
        OPP opp;
        opp = null;
        if (!(this.ident_52(builder))) {
            return false;
        }
        while (true) {
            PsiBuilder.Marker quantMarker1;
            quantMarker1 = builder.mark();
            if (this.identifier_53_quant_1(builder, opp)) {
                quantMarker1.drop();
            } else {
                quantMarker1.rollbackTo();
                break;
            }
        }
        return true;
    }

    private boolean infix_54_alt_1(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.INFIX) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean infix_54_alt_2(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.COND_OP_INCOMPLETE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean infix_54_alt_3(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.COND_OP_INCOMPLETE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean infix_54_alt_4(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.INFIX) && (tt2.equals("!!"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean infix_54_alt_5(PsiBuilder builder, OPP opp) {
        if (!(this.EXPR_2(builder))) {
            return false;
        }
        PsiBuilder.Marker altMarker4;
        altMarker4 = builder.mark();
        if (this.infix_54_alt_4(builder, opp)) {
            altMarker4.drop();
        } else {
            altMarker4.rollbackTo();
            PsiBuilder.Marker altMarker3;
            altMarker3 = builder.mark();
            if (this.infix_54_alt_3(builder, opp)) {
                altMarker3.drop();
            } else {
                altMarker3.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean infix_54_alt_6(PsiBuilder builder, OPP opp) {
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.INFIX) && (tt1.equals("??"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker altMarker5;
        altMarker5 = builder.mark();
        if (this.infix_54_alt_5(builder, opp)) {
            altMarker5.drop();
        } else {
            altMarker5.rollbackTo();
            PsiBuilder.Marker altMarker2;
            altMarker2 = builder.mark();
            if (this.infix_54_alt_2(builder, opp)) {
                altMarker2.drop();
            } else {
                altMarker2.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean infix_54(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker6;
        altMarker6 = builder.mark();
        if (this.infix_54_alt_6(builder, opp)) {
            altMarker6.drop();
        } else {
            altMarker6.rollbackTo();
            PsiBuilder.Marker altMarker1;
            altMarker1 = builder.mark();
            if (this.infix_54_alt_1(builder, opp)) {
                altMarker1.drop();
            } else {
                altMarker1.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean infix_circumfix_meta_operator_55_alt_1(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.HYPER_METAOP_MISSING) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean infix_circumfix_meta_operator_55_alt_2(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.METAOP) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean infix_circumfix_meta_operator_55_alt_3(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.METAOP) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.infixish_57(builder))) {
            return false;
        }
        PsiBuilder.Marker altMarker3;
        altMarker3 = builder.mark();
        if (this.infix_circumfix_meta_operator_55_alt_2(builder, opp)) {
            altMarker3.drop();
        } else {
            altMarker3.rollbackTo();
            PsiBuilder.Marker altMarker2;
            altMarker2 = builder.mark();
            if (this.infix_circumfix_meta_operator_55_alt_1(builder, opp)) {
                altMarker2.drop();
            } else {
                altMarker2.rollbackTo();
                return false;
            }
        }
        marker1.done(RakuElementTypes.HYPER_METAOP);
        return true;
    }

    private boolean infix_circumfix_meta_operator_55_alt_4(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.HYPER_METAOP_MISSING) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean infix_circumfix_meta_operator_55_alt_5(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.METAOP) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean infix_circumfix_meta_operator_55_alt_6(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker5;
        marker5 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.METAOP) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.infixish_57(builder))) {
            return false;
        }
        PsiBuilder.Marker altMarker7;
        altMarker7 = builder.mark();
        if (this.infix_circumfix_meta_operator_55_alt_5(builder, opp)) {
            altMarker7.drop();
        } else {
            altMarker7.rollbackTo();
            PsiBuilder.Marker altMarker6;
            altMarker6 = builder.mark();
            if (this.infix_circumfix_meta_operator_55_alt_4(builder, opp)) {
                altMarker6.drop();
            } else {
                altMarker6.rollbackTo();
                return false;
            }
        }
        marker5.done(RakuElementTypes.HYPER_METAOP);
        return true;
    }

    private boolean infix_circumfix_meta_operator_55(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker8;
        altMarker8 = builder.mark();
        if (this.infix_circumfix_meta_operator_55_alt_6(builder, opp)) {
            altMarker8.drop();
        } else {
            altMarker8.rollbackTo();
            PsiBuilder.Marker altMarker4;
            altMarker4 = builder.mark();
            if (this.infix_circumfix_meta_operator_55_alt_3(builder, opp)) {
                altMarker4.drop();
            } else {
                altMarker4.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean infix_prefix_meta_operator_56_alt_1(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.METAOP) && (tt1.equals("Z"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.infixish_57(builder))) {
            return false;
        }
        marker1.done(RakuElementTypes.ZIP_METAOP);
        return true;
    }

    private boolean infix_prefix_meta_operator_56_alt_2(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker3;
        marker3 = builder.mark();
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.METAOP) && (tt2.equals("X"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.infixish_57(builder))) {
            return false;
        }
        marker3.done(RakuElementTypes.CROSS_METAOP);
        return true;
    }

    private boolean infix_prefix_meta_operator_56_alt_3(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker5;
        marker5 = builder.mark();
        String tt3;
        tt3 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.METAOP) && (tt3.equals("S"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.infixish_57(builder))) {
            return false;
        }
        marker5.done(RakuElementTypes.SEQUENTIAL_METAOP);
        return true;
    }

    private boolean infix_prefix_meta_operator_56_alt_4(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean infix_prefix_meta_operator_56_alt_5(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean infix_prefix_meta_operator_56_alt_6(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean infix_prefix_meta_operator_56_alt_7(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker7;
        marker7 = builder.mark();
        String tt4;
        tt4 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.METAOP) && (tt4.equals("R"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.infixish_57(builder))) {
            return false;
        }
        marker7.done(RakuElementTypes.REVERSE_METAOP);
        PsiBuilder.Marker altMarker10;
        altMarker10 = builder.mark();
        if (this.infix_prefix_meta_operator_56_alt_6(builder, opp)) {
            altMarker10.drop();
        } else {
            altMarker10.rollbackTo();
            PsiBuilder.Marker altMarker9;
            altMarker9 = builder.mark();
            if (this.infix_prefix_meta_operator_56_alt_5(builder, opp)) {
                altMarker9.drop();
            } else {
                altMarker9.rollbackTo();
                PsiBuilder.Marker altMarker8;
                altMarker8 = builder.mark();
                if (this.infix_prefix_meta_operator_56_alt_4(builder, opp)) {
                    altMarker8.drop();
                } else {
                    altMarker8.rollbackTo();
                    return false;
                }
            }
        }
        return true;
    }

    private boolean infix_prefix_meta_operator_56_alt_8(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker12;
        marker12 = builder.mark();
        String tt5;
        tt5 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.METAOP) && (tt5.equals("!"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.infixish_57(builder))) {
            return false;
        }
        marker12.done(RakuElementTypes.NEGATION_METAOP);
        return true;
    }

    private boolean infix_prefix_meta_operator_56(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker13;
        altMarker13 = builder.mark();
        if (this.infix_prefix_meta_operator_56_alt_8(builder, opp)) {
            altMarker13.drop();
        } else {
            altMarker13.rollbackTo();
            PsiBuilder.Marker altMarker11;
            altMarker11 = builder.mark();
            if (this.infix_prefix_meta_operator_56_alt_7(builder, opp)) {
                altMarker11.drop();
            } else {
                altMarker11.rollbackTo();
                PsiBuilder.Marker altMarker6;
                altMarker6 = builder.mark();
                if (this.infix_prefix_meta_operator_56_alt_3(builder, opp)) {
                    altMarker6.drop();
                } else {
                    altMarker6.rollbackTo();
                    PsiBuilder.Marker altMarker4;
                    altMarker4 = builder.mark();
                    if (this.infix_prefix_meta_operator_56_alt_2(builder, opp)) {
                        altMarker4.drop();
                    } else {
                        altMarker4.rollbackTo();
                        PsiBuilder.Marker altMarker2;
                        altMarker2 = builder.mark();
                        if (this.infix_prefix_meta_operator_56_alt_1(builder, opp)) {
                            altMarker2.drop();
                        } else {
                            altMarker2.rollbackTo();
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean infixish_57_alt_1(PsiBuilder builder, OPP opp) {
        if (!(this.infixish_non_assignment_meta_58(builder))) {
            return false;
        }
        return true;
    }

    private boolean infixish_57_quant_2(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean infixish_57_alt_3(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker2;
        marker2 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.ASSIGN_METAOP) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.infixish_non_assignment_meta_58(builder))) {
            return false;
        }
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.METAOP) && (tt1.equals("="))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        marker2.done(RakuElementTypes.ASSIGN_METAOP);
        PsiBuilder.Marker quantMarker3;
        quantMarker3 = builder.mark();
        if (this.infixish_57_quant_2(builder, opp)) {
            quantMarker3.drop();
        } else {
            quantMarker3.rollbackTo();
        }
        return true;
    }

    private boolean infixish_57_alt_4(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker5;
        marker5 = builder.mark();
        if (!(this.colonpair_29(builder))) {
            return false;
        }
        marker5.done(RakuElementTypes.OPERATOR_ADVERB);
        return true;
    }

    private boolean infixish_57(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker6;
        altMarker6 = builder.mark();
        if (this.infixish_57_alt_4(builder, opp)) {
            altMarker6.drop();
        } else {
            altMarker6.rollbackTo();
            PsiBuilder.Marker altMarker4;
            altMarker4 = builder.mark();
            if (this.infixish_57_alt_3(builder, opp)) {
                altMarker4.drop();
            } else {
                altMarker4.rollbackTo();
                PsiBuilder.Marker altMarker1;
                altMarker1 = builder.mark();
                if (this.infixish_57_alt_1(builder, opp)) {
                    altMarker1.drop();
                } else {
                    altMarker1.rollbackTo();
                    return false;
                }
            }
        }
        return true;
    }

    private boolean infixish_non_assignment_meta_58_alt_1(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        if (!(this.infix_54(builder))) {
            return false;
        }
        marker1.done(RakuElementTypes.INFIX);
        return true;
    }

    private boolean infixish_non_assignment_meta_58_alt_2(PsiBuilder builder, OPP opp) {
        if (!(this.infix_circumfix_meta_operator_55(builder))) {
            return false;
        }
        return true;
    }

    private boolean infixish_non_assignment_meta_58_alt_3(PsiBuilder builder, OPP opp) {
        if (!(this.infix_prefix_meta_operator_56(builder))) {
            return false;
        }
        return true;
    }

    private boolean infixish_non_assignment_meta_58_alt_4(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.BRACKETED_INFIX_INCOMPLETE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean infixish_non_assignment_meta_58_alt_5(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.BRACKETED_INFIX_INCOMPLETE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean infixish_non_assignment_meta_58_alt_6(PsiBuilder builder, OPP opp) {
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.INFIX) && (tt2.equals("]"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean infixish_non_assignment_meta_58_alt_7(PsiBuilder builder, OPP opp) {
        if (!(this.infixish_57(builder))) {
            return false;
        }
        PsiBuilder.Marker altMarker8;
        altMarker8 = builder.mark();
        if (this.infixish_non_assignment_meta_58_alt_6(builder, opp)) {
            altMarker8.drop();
        } else {
            altMarker8.rollbackTo();
            PsiBuilder.Marker altMarker7;
            altMarker7 = builder.mark();
            if (this.infixish_non_assignment_meta_58_alt_5(builder, opp)) {
                altMarker7.drop();
            } else {
                altMarker7.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean infixish_non_assignment_meta_58_alt_8(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.BRACKETED_INFIX_INCOMPLETE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean infixish_non_assignment_meta_58_alt_9(PsiBuilder builder, OPP opp) {
        String tt3;
        tt3 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.INFIX) && (tt3.equals("]"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean infixish_non_assignment_meta_58_alt_10(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.INFIX_FUNCTION) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.variable_253(builder))) {
            return false;
        }
        PsiBuilder.Marker altMarker11;
        altMarker11 = builder.mark();
        if (this.infixish_non_assignment_meta_58_alt_9(builder, opp)) {
            altMarker11.drop();
        } else {
            altMarker11.rollbackTo();
            PsiBuilder.Marker altMarker10;
            altMarker10 = builder.mark();
            if (this.infixish_non_assignment_meta_58_alt_8(builder, opp)) {
                altMarker10.drop();
            } else {
                altMarker10.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean infixish_non_assignment_meta_58_alt_11(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker5;
        marker5 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.INFIX) && (tt1.equals("["))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker altMarker12;
        altMarker12 = builder.mark();
        if (this.infixish_non_assignment_meta_58_alt_10(builder, opp)) {
            altMarker12.drop();
        } else {
            altMarker12.rollbackTo();
            PsiBuilder.Marker altMarker9;
            altMarker9 = builder.mark();
            if (this.infixish_non_assignment_meta_58_alt_7(builder, opp)) {
                altMarker9.drop();
            } else {
                altMarker9.rollbackTo();
                PsiBuilder.Marker altMarker6;
                altMarker6 = builder.mark();
                if (this.infixish_non_assignment_meta_58_alt_4(builder, opp)) {
                    altMarker6.drop();
                } else {
                    altMarker6.rollbackTo();
                    return false;
                }
            }
        }
        marker5.done(RakuElementTypes.BRACKETED_INFIX);
        return true;
    }

    private boolean infixish_non_assignment_meta_58(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker13;
        altMarker13 = builder.mark();
        if (this.infixish_non_assignment_meta_58_alt_11(builder, opp)) {
            altMarker13.drop();
        } else {
            altMarker13.rollbackTo();
            PsiBuilder.Marker altMarker4;
            altMarker4 = builder.mark();
            if (this.infixish_non_assignment_meta_58_alt_3(builder, opp)) {
                altMarker4.drop();
            } else {
                altMarker4.rollbackTo();
                PsiBuilder.Marker altMarker3;
                altMarker3 = builder.mark();
                if (this.infixish_non_assignment_meta_58_alt_2(builder, opp)) {
                    altMarker3.drop();
                } else {
                    altMarker3.rollbackTo();
                    PsiBuilder.Marker altMarker2;
                    altMarker2 = builder.mark();
                    if (this.infixish_non_assignment_meta_58_alt_1(builder, opp)) {
                        altMarker2.drop();
                    } else {
                        altMarker2.rollbackTo();
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean infixstopper_59_alt_1(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean infixstopper_59_alt_2(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean infixstopper_59_alt_3(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker2;
        altMarker2 = builder.mark();
        if (this.infixstopper_59_alt_2(builder, opp)) {
            altMarker2.drop();
        } else {
            altMarker2.rollbackTo();
            PsiBuilder.Marker altMarker1;
            altMarker1 = builder.mark();
            if (this.infixstopper_59_alt_1(builder, opp)) {
                altMarker1.drop();
            } else {
                altMarker1.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean infixstopper_59_alt_4(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean infixstopper_59(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker4;
        altMarker4 = builder.mark();
        if (this.infixstopper_59_alt_4(builder, opp)) {
            altMarker4.drop();
        } else {
            altMarker4.rollbackTo();
            PsiBuilder.Marker altMarker3;
            altMarker3 = builder.mark();
            if (this.infixstopper_59_alt_3(builder, opp)) {
                altMarker3.drop();
            } else {
                altMarker3.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean initializer_60_alt_1(PsiBuilder builder, OPP opp) {
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.INFIX) && (tt1.equals(".="))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean initializer_60_alt_2(PsiBuilder builder, OPP opp) {
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.INFIX) && (tt2.equals("::="))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean initializer_60_alt_3(PsiBuilder builder, OPP opp) {
        String tt3;
        tt3 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.INFIX) && (tt3.equals(":="))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean initializer_60_quant_4(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean initializer_60_alt_5(PsiBuilder builder, OPP opp) {
        String tt4;
        tt4 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.INFIX) && (tt4.equals("="))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker quantMarker5;
        quantMarker5 = builder.mark();
        if (this.initializer_60_quant_4(builder, opp)) {
            quantMarker5.drop();
        } else {
            quantMarker5.rollbackTo();
        }
        return true;
    }

    private boolean initializer_60_alt_6(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.INITIALIZER_MISSING) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean initializer_60_alt_7(PsiBuilder builder, OPP opp) {
        if (!(this.EXPR_2(builder))) {
            return false;
        }
        return true;
    }

    private boolean initializer_60_alt_8(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.NOT_DOTTY) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker altMarker8;
        altMarker8 = builder.mark();
        if (this.initializer_60_alt_7(builder, opp)) {
            altMarker8.drop();
        } else {
            altMarker8.rollbackTo();
            PsiBuilder.Marker altMarker7;
            altMarker7 = builder.mark();
            if (this.initializer_60_alt_6(builder, opp)) {
                altMarker7.drop();
            } else {
                altMarker7.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean initializer_60_alt_9(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.INITIALIZER_MISSING) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean initializer_60_alt_10(PsiBuilder builder, OPP opp) {
        if (!(this.dottyop_41(builder))) {
            return false;
        }
        return true;
    }

    private boolean initializer_60_alt_11(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.IS_DOTTY) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker altMarker11;
        altMarker11 = builder.mark();
        if (this.initializer_60_alt_10(builder, opp)) {
            altMarker11.drop();
        } else {
            altMarker11.rollbackTo();
            PsiBuilder.Marker altMarker10;
            altMarker10 = builder.mark();
            if (this.initializer_60_alt_9(builder, opp)) {
                altMarker10.drop();
            } else {
                altMarker10.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean initializer_60(PsiBuilder builder) {
        OPP opp;
        opp = null;
        if ((builder.getTokenType()) == RakuTokenTypes.PARSING_INITIALIZER) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        PsiBuilder.Marker altMarker6;
        altMarker6 = builder.mark();
        if (this.initializer_60_alt_5(builder, opp)) {
            altMarker6.drop();
        } else {
            altMarker6.rollbackTo();
            PsiBuilder.Marker altMarker4;
            altMarker4 = builder.mark();
            if (this.initializer_60_alt_3(builder, opp)) {
                altMarker4.drop();
            } else {
                altMarker4.rollbackTo();
                PsiBuilder.Marker altMarker3;
                altMarker3 = builder.mark();
                if (this.initializer_60_alt_2(builder, opp)) {
                    altMarker3.drop();
                } else {
                    altMarker3.rollbackTo();
                    PsiBuilder.Marker altMarker2;
                    altMarker2 = builder.mark();
                    if (this.initializer_60_alt_1(builder, opp)) {
                        altMarker2.drop();
                    } else {
                        altMarker2.rollbackTo();
                        return false;
                    }
                }
            }
        }
        marker1.done(RakuElementTypes.INFIX);
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker altMarker12;
        altMarker12 = builder.mark();
        if (this.initializer_60_alt_11(builder, opp)) {
            altMarker12.drop();
        } else {
            altMarker12.rollbackTo();
            PsiBuilder.Marker altMarker9;
            altMarker9 = builder.mark();
            if (this.initializer_60_alt_8(builder, opp)) {
                altMarker9.drop();
            } else {
                altMarker9.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean integer_61(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.INTEGER_LITERAL) {
            builder.advanceLexer();
        } else {
            return false;
        }
        marker1.done(RakuElementTypes.INTEGER_LITERAL);
        return true;
    }

    private boolean integer_lex_62_alt_1(PsiBuilder builder, OPP opp) {
        if (!(this.decint_35(builder))) {
            return false;
        }
        return true;
    }

    private boolean integer_lex_62_alt_2(PsiBuilder builder, OPP opp) {
        if (!(this.decint_35(builder))) {
            return false;
        }
        return true;
    }

    private boolean integer_lex_62_quant_3(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean integer_lex_62_alt_4(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker quantMarker3;
        quantMarker3 = builder.mark();
        if (this.integer_lex_62_quant_3(builder, opp)) {
            quantMarker3.drop();
        } else {
            quantMarker3.rollbackTo();
        }
        if (!(this.decint_35(builder))) {
            return false;
        }
        return true;
    }

    private boolean integer_lex_62_quant_5(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean integer_lex_62_alt_6(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker quantMarker5;
        quantMarker5 = builder.mark();
        if (this.integer_lex_62_quant_5(builder, opp)) {
            quantMarker5.drop();
        } else {
            quantMarker5.rollbackTo();
        }
        if (!(this.hexint_50(builder))) {
            return false;
        }
        return true;
    }

    private boolean integer_lex_62_quant_7(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean integer_lex_62_alt_8(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker quantMarker7;
        quantMarker7 = builder.mark();
        if (this.integer_lex_62_quant_7(builder, opp)) {
            quantMarker7.drop();
        } else {
            quantMarker7.rollbackTo();
        }
        if (!(this.octint_85(builder))) {
            return false;
        }
        return true;
    }

    private boolean integer_lex_62_quant_9(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean integer_lex_62_alt_10(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker quantMarker9;
        quantMarker9 = builder.mark();
        if (this.integer_lex_62_quant_9(builder, opp)) {
            quantMarker9.drop();
        } else {
            quantMarker9.rollbackTo();
        }
        if (!(this.binint_14(builder))) {
            return false;
        }
        return true;
    }

    private boolean integer_lex_62_alt_11(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker10;
        altMarker10 = builder.mark();
        if (this.integer_lex_62_alt_10(builder, opp)) {
            altMarker10.drop();
        } else {
            altMarker10.rollbackTo();
            PsiBuilder.Marker altMarker8;
            altMarker8 = builder.mark();
            if (this.integer_lex_62_alt_8(builder, opp)) {
                altMarker8.drop();
            } else {
                altMarker8.rollbackTo();
                PsiBuilder.Marker altMarker6;
                altMarker6 = builder.mark();
                if (this.integer_lex_62_alt_6(builder, opp)) {
                    altMarker6.drop();
                } else {
                    altMarker6.rollbackTo();
                    PsiBuilder.Marker altMarker4;
                    altMarker4 = builder.mark();
                    if (this.integer_lex_62_alt_4(builder, opp)) {
                        altMarker4.drop();
                    } else {
                        altMarker4.rollbackTo();
                        PsiBuilder.Marker altMarker2;
                        altMarker2 = builder.mark();
                        if (this.integer_lex_62_alt_2(builder, opp)) {
                            altMarker2.drop();
                        } else {
                            altMarker2.rollbackTo();
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean integer_lex_62(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker11;
        altMarker11 = builder.mark();
        if (this.integer_lex_62_alt_11(builder, opp)) {
            altMarker11.drop();
        } else {
            altMarker11.rollbackTo();
            PsiBuilder.Marker altMarker1;
            altMarker1 = builder.mark();
            if (this.integer_lex_62_alt_1(builder, opp)) {
                altMarker1.drop();
            } else {
                altMarker1.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean interpolation_opener_63_quant_1(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean interpolation_opener_63_alt_2(PsiBuilder builder, OPP opp) {
        if (!(this.quote_128(builder))) {
            return false;
        }
        return true;
    }

    private boolean interpolation_opener_63_alt_3(PsiBuilder builder, OPP opp) {
        if (!(this.name_79(builder))) {
            return false;
        }
        if (!(this.longname_colonpairs_67(builder))) {
            return false;
        }
        return true;
    }

    private boolean interpolation_opener_63_alt_4(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker quantMarker1;
        quantMarker1 = builder.mark();
        if (this.interpolation_opener_63_quant_1(builder, opp)) {
            quantMarker1.drop();
        } else {
            quantMarker1.rollbackTo();
        }
        PsiBuilder.Marker altMarker3;
        altMarker3 = builder.mark();
        if (this.interpolation_opener_63_alt_3(builder, opp)) {
            altMarker3.drop();
        } else {
            altMarker3.rollbackTo();
            PsiBuilder.Marker altMarker2;
            altMarker2 = builder.mark();
            if (this.interpolation_opener_63_alt_2(builder, opp)) {
                altMarker2.drop();
            } else {
                altMarker2.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean interpolation_opener_63_alt_5(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean interpolation_opener_63(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker5;
        altMarker5 = builder.mark();
        if (this.interpolation_opener_63_alt_5(builder, opp)) {
            altMarker5.drop();
        } else {
            altMarker5.rollbackTo();
            PsiBuilder.Marker altMarker4;
            altMarker4 = builder.mark();
            if (this.interpolation_opener_63_alt_4(builder, opp)) {
                altMarker4.drop();
            } else {
                altMarker4.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean kok_64(PsiBuilder builder) {
        OPP opp;
        opp = null;
        if (!(this.end_keyword_43(builder))) {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean label_65(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.LABEL_NAME) {
            builder.advanceLexer();
        } else {
            return false;
        }
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.LABEL_COLON) && (tt1.equals(":"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        marker1.done(RakuElementTypes.LABEL);
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean lambda_66_alt_1(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean lambda_66_alt_2(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean lambda_66(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker2;
        altMarker2 = builder.mark();
        if (this.lambda_66_alt_2(builder, opp)) {
            altMarker2.drop();
        } else {
            altMarker2.rollbackTo();
            PsiBuilder.Marker altMarker1;
            altMarker1 = builder.mark();
            if (this.lambda_66_alt_1(builder, opp)) {
                altMarker1.drop();
            } else {
                altMarker1.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean longname_colonpairs_67_quant_1(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.LONGNAME_COLONPAIR) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.colonpair_29(builder))) {
            return false;
        }
        return true;
    }

    private boolean longname_colonpairs_67(PsiBuilder builder) {
        OPP opp;
        opp = null;
        while (true) {
            PsiBuilder.Marker quantMarker1;
            quantMarker1 = builder.mark();
            if (this.longname_colonpairs_67_quant_1(builder, opp)) {
                quantMarker1.drop();
            } else {
                quantMarker1.rollbackTo();
                break;
            }
        }
        return true;
    }

    private boolean metachar_68_alt_1(PsiBuilder builder, OPP opp) {
        if (!(this.mod_internal_73(builder))) {
            return false;
        }
        return true;
    }

    private boolean metachar_68_quant_2(PsiBuilder builder, OPP opp) {
        if (!(this.quantified_atom_124(builder))) {
            return false;
        }
        return true;
    }

    private boolean metachar_68_quant_3(PsiBuilder builder, OPP opp) {
        if (!(this.quantified_atom_124(builder))) {
            return false;
        }
        if (!(this.rxws_165(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker3;
        quantMarker3 = builder.mark();
        if (this.metachar_68_quant_2(builder, opp)) {
            quantMarker3.drop();
        } else {
            quantMarker3.rollbackTo();
        }
        return true;
    }

    private boolean metachar_68_alt_4(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker2;
        marker2 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.REGEX_INFIX) && (tt1.equals("~"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.rxws_165(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker4;
        quantMarker4 = builder.mark();
        if (this.metachar_68_quant_3(builder, opp)) {
            quantMarker4.drop();
        } else {
            quantMarker4.rollbackTo();
        }
        marker2.done(RakuElementTypes.REGEX_GOAL);
        return true;
    }

    private boolean metachar_68_alt_5(PsiBuilder builder, OPP opp) {
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.SCOPE_DECLARATOR) && (tt2.equals(":"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.statement_179(builder))) {
            return false;
        }
        if (!(this.eat_terminator_42(builder))) {
            return false;
        }
        return true;
    }

    private boolean metachar_68_alt_6(PsiBuilder builder, OPP opp) {
        if (!(this.rxcodeblock_158(builder))) {
            return false;
        }
        return true;
    }

    private boolean metachar_68_alt_7(PsiBuilder builder, OPP opp) {
        if (!(this.rxqq_161(builder))) {
            return false;
        }
        if (!(this.SIGOK_3(builder))) {
            return false;
        }
        return true;
    }

    private boolean metachar_68_alt_8(PsiBuilder builder, OPP opp) {
        if (!(this.rxq_160(builder))) {
            return false;
        }
        if (!(this.SIGOK_3(builder))) {
            return false;
        }
        return true;
    }

    private boolean metachar_68_alt_9(PsiBuilder builder, OPP opp) {
        if (!(this.rxQ_156(builder))) {
            return false;
        }
        if (!(this.SIGOK_3(builder))) {
            return false;
        }
        return true;
    }

    private boolean metachar_68_quant_10(PsiBuilder builder, OPP opp) {
        String tt4;
        tt4 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.REGEX_ASSERTION_ANGLE_CLOSE) && (tt4.equals(">"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.SIGOK_3(builder))) {
            return false;
        }
        return true;
    }

    private boolean metachar_68_alt_11(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker11;
        marker11 = builder.mark();
        String tt3;
        tt3 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.REGEX_ASSERTION_ANGLE_OPEN) && (tt3.equals("<"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.assertion_8(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker12;
        quantMarker12 = builder.mark();
        if (this.metachar_68_quant_10(builder, opp)) {
            quantMarker12.drop();
        } else {
            quantMarker12.rollbackTo();
        }
        marker11.done(RakuElementTypes.REGEX_ASSERTION);
        return true;
    }

    private boolean metachar_68_alt_12(PsiBuilder builder, OPP opp) {
        if (!(this.rxqw_162(builder))) {
            return false;
        }
        if (!(this.SIGOK_3(builder))) {
            return false;
        }
        return true;
    }

    private boolean metachar_68_alt_13(PsiBuilder builder, OPP opp) {
        if (!(this.backslash_11(builder))) {
            return false;
        }
        if (!(this.SIGOK_3(builder))) {
            return false;
        }
        return true;
    }

    private boolean metachar_68_quant_14(PsiBuilder builder, OPP opp) {
        if (!(this.regex_nibbler_fresh_rx_152(builder))) {
            return false;
        }
        return true;
    }

    private boolean metachar_68_quant_15(PsiBuilder builder, OPP opp) {
        String tt6;
        tt6 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.REGEX_CAPTURE_PARENTHESES_CLOSE) && (tt6.equals(")"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean metachar_68_alt_16(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker16;
        marker16 = builder.mark();
        String tt5;
        tt5 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.REGEX_CAPTURE_PARENTHESES_OPEN) && (tt5.equals("("))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker quantMarker17;
        quantMarker17 = builder.mark();
        if (this.metachar_68_quant_14(builder, opp)) {
            quantMarker17.drop();
        } else {
            quantMarker17.rollbackTo();
        }
        PsiBuilder.Marker quantMarker18;
        quantMarker18 = builder.mark();
        if (this.metachar_68_quant_15(builder, opp)) {
            quantMarker18.drop();
        } else {
            quantMarker18.rollbackTo();
        }
        marker16.done(RakuElementTypes.REGEX_CAPTURE_POSITIONAL);
        return true;
    }

    private boolean metachar_68_quant_17(PsiBuilder builder, OPP opp) {
        if (!(this.regex_nibbler_fresh_rx_152(builder))) {
            return false;
        }
        return true;
    }

    private boolean metachar_68_quant_18(PsiBuilder builder, OPP opp) {
        String tt8;
        tt8 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.REGEX_GROUP_BRACKET_CLOSE) && (tt8.equals("]"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean metachar_68_alt_19(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker20;
        marker20 = builder.mark();
        String tt7;
        tt7 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.REGEX_GROUP_BRACKET_OPEN) && (tt7.equals("["))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker quantMarker21;
        quantMarker21 = builder.mark();
        if (this.metachar_68_quant_17(builder, opp)) {
            quantMarker21.drop();
        } else {
            quantMarker21.rollbackTo();
        }
        PsiBuilder.Marker quantMarker22;
        quantMarker22 = builder.mark();
        if (this.metachar_68_quant_18(builder, opp)) {
            quantMarker22.drop();
        } else {
            quantMarker22.rollbackTo();
        }
        marker20.done(RakuElementTypes.REGEX_GROUP);
        return true;
    }

    private boolean metachar_68_alt_20(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker24;
        marker24 = builder.mark();
        String tt9;
        tt9 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.REGEX_BUILTIN_CCLASS) && (tt9.equals("."))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        marker24.done(RakuElementTypes.REGEX_BUILTIN_CCLASS);
        return true;
    }

    private boolean metachar_68_alt_21(PsiBuilder builder, OPP opp) {
        String tt10;
        tt10 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.REGEX_ANCHOR) && (tt10.equals(")>"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.SIGOK_3(builder))) {
            return false;
        }
        return true;
    }

    private boolean metachar_68_alt_22(PsiBuilder builder, OPP opp) {
        String tt11;
        tt11 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.REGEX_ANCHOR) && (tt11.equals("<("))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.SIGOK_3(builder))) {
            return false;
        }
        return true;
    }

    private boolean metachar_68_alt_23(PsiBuilder builder, OPP opp) {
        String tt12;
        tt12 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.REGEX_ANCHOR) && (tt12.equals("\u00BB"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.SIGOK_3(builder))) {
            return false;
        }
        return true;
    }

    private boolean metachar_68_alt_24(PsiBuilder builder, OPP opp) {
        String tt13;
        tt13 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.REGEX_ANCHOR) && (tt13.equals(">>"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.SIGOK_3(builder))) {
            return false;
        }
        return true;
    }

    private boolean metachar_68_alt_25(PsiBuilder builder, OPP opp) {
        String tt14;
        tt14 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.REGEX_ANCHOR) && (tt14.equals("\u00AB"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.SIGOK_3(builder))) {
            return false;
        }
        return true;
    }

    private boolean metachar_68_alt_26(PsiBuilder builder, OPP opp) {
        String tt15;
        tt15 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.REGEX_ANCHOR) && (tt15.equals("<<"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.SIGOK_3(builder))) {
            return false;
        }
        return true;
    }

    private boolean metachar_68_alt_27(PsiBuilder builder, OPP opp) {
        String tt16;
        tt16 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.REGEX_ANCHOR) && (tt16.equals("$"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.SIGOK_3(builder))) {
            return false;
        }
        return true;
    }

    private boolean metachar_68_alt_28(PsiBuilder builder, OPP opp) {
        String tt17;
        tt17 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.REGEX_ANCHOR) && (tt17.equals("$$"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.SIGOK_3(builder))) {
            return false;
        }
        return true;
    }

    private boolean metachar_68_alt_29(PsiBuilder builder, OPP opp) {
        String tt18;
        tt18 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.REGEX_ANCHOR) && (tt18.equals("^"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.SIGOK_3(builder))) {
            return false;
        }
        return true;
    }

    private boolean metachar_68_alt_30(PsiBuilder builder, OPP opp) {
        String tt19;
        tt19 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.REGEX_ANCHOR) && (tt19.equals("^^"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.SIGOK_3(builder))) {
            return false;
        }
        return true;
    }

    private boolean metachar_68_alt_31(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker26;
        marker26 = builder.mark();
        PsiBuilder.Marker altMarker36;
        altMarker36 = builder.mark();
        if (this.metachar_68_alt_30(builder, opp)) {
            altMarker36.drop();
        } else {
            altMarker36.rollbackTo();
            PsiBuilder.Marker altMarker35;
            altMarker35 = builder.mark();
            if (this.metachar_68_alt_29(builder, opp)) {
                altMarker35.drop();
            } else {
                altMarker35.rollbackTo();
                PsiBuilder.Marker altMarker34;
                altMarker34 = builder.mark();
                if (this.metachar_68_alt_28(builder, opp)) {
                    altMarker34.drop();
                } else {
                    altMarker34.rollbackTo();
                    PsiBuilder.Marker altMarker33;
                    altMarker33 = builder.mark();
                    if (this.metachar_68_alt_27(builder, opp)) {
                        altMarker33.drop();
                    } else {
                        altMarker33.rollbackTo();
                        PsiBuilder.Marker altMarker32;
                        altMarker32 = builder.mark();
                        if (this.metachar_68_alt_26(builder, opp)) {
                            altMarker32.drop();
                        } else {
                            altMarker32.rollbackTo();
                            PsiBuilder.Marker altMarker31;
                            altMarker31 = builder.mark();
                            if (this.metachar_68_alt_25(builder, opp)) {
                                altMarker31.drop();
                            } else {
                                altMarker31.rollbackTo();
                                PsiBuilder.Marker altMarker30;
                                altMarker30 = builder.mark();
                                if (this.metachar_68_alt_24(builder, opp)) {
                                    altMarker30.drop();
                                } else {
                                    altMarker30.rollbackTo();
                                    PsiBuilder.Marker altMarker29;
                                    altMarker29 = builder.mark();
                                    if (this.metachar_68_alt_23(builder, opp)) {
                                        altMarker29.drop();
                                    } else {
                                        altMarker29.rollbackTo();
                                        PsiBuilder.Marker altMarker28;
                                        altMarker28 = builder.mark();
                                        if (this.metachar_68_alt_22(builder, opp)) {
                                            altMarker28.drop();
                                        } else {
                                            altMarker28.rollbackTo();
                                            PsiBuilder.Marker altMarker27;
                                            altMarker27 = builder.mark();
                                            if (this.metachar_68_alt_21(builder, opp)) {
                                                altMarker27.drop();
                                            } else {
                                                altMarker27.rollbackTo();
                                                return false;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        marker26.done(RakuElementTypes.REGEX_ANCHOR);
        return true;
    }

    private boolean metachar_68_alt_32(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.REGEX_VARIABLE_BINDING_INCOMPLETE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean metachar_68_alt_33(PsiBuilder builder, OPP opp) {
        if (!(this.quantified_atom_124(builder))) {
            return false;
        }
        return true;
    }

    private boolean metachar_68_quant_34(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.REGEX_VARIABLE_BINDING) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if ((builder.getTokenType()) == RakuTokenTypes.WHITE_SPACE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        String tt20;
        tt20 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.INFIX) && (tt20.equals("="))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if ((builder.getTokenType()) == RakuTokenTypes.WHITE_SPACE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker altMarker40;
        altMarker40 = builder.mark();
        if (this.metachar_68_alt_33(builder, opp)) {
            altMarker40.drop();
        } else {
            altMarker40.rollbackTo();
            PsiBuilder.Marker altMarker39;
            altMarker39 = builder.mark();
            if (this.metachar_68_alt_32(builder, opp)) {
                altMarker39.drop();
            } else {
                altMarker39.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean metachar_68_alt_35(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker38;
        marker38 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.REGEX_VARIABLE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.variable_253(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker41;
        quantMarker41 = builder.mark();
        if (this.metachar_68_quant_34(builder, opp)) {
            quantMarker41.drop();
        } else {
            quantMarker41.rollbackTo();
        }
        marker38.done(RakuElementTypes.REGEX_VARIABLE);
        return true;
    }

    private boolean metachar_68(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker42;
        altMarker42 = builder.mark();
        if (this.metachar_68_alt_35(builder, opp)) {
            altMarker42.drop();
        } else {
            altMarker42.rollbackTo();
            PsiBuilder.Marker altMarker37;
            altMarker37 = builder.mark();
            if (this.metachar_68_alt_31(builder, opp)) {
                altMarker37.drop();
            } else {
                altMarker37.rollbackTo();
                PsiBuilder.Marker altMarker25;
                altMarker25 = builder.mark();
                if (this.metachar_68_alt_20(builder, opp)) {
                    altMarker25.drop();
                } else {
                    altMarker25.rollbackTo();
                    PsiBuilder.Marker altMarker23;
                    altMarker23 = builder.mark();
                    if (this.metachar_68_alt_19(builder, opp)) {
                        altMarker23.drop();
                    } else {
                        altMarker23.rollbackTo();
                        PsiBuilder.Marker altMarker19;
                        altMarker19 = builder.mark();
                        if (this.metachar_68_alt_16(builder, opp)) {
                            altMarker19.drop();
                        } else {
                            altMarker19.rollbackTo();
                            PsiBuilder.Marker altMarker15;
                            altMarker15 = builder.mark();
                            if (this.metachar_68_alt_13(builder, opp)) {
                                altMarker15.drop();
                            } else {
                                altMarker15.rollbackTo();
                                PsiBuilder.Marker altMarker14;
                                altMarker14 = builder.mark();
                                if (this.metachar_68_alt_12(builder, opp)) {
                                    altMarker14.drop();
                                } else {
                                    altMarker14.rollbackTo();
                                    PsiBuilder.Marker altMarker13;
                                    altMarker13 = builder.mark();
                                    if (this.metachar_68_alt_11(builder, opp)) {
                                        altMarker13.drop();
                                    } else {
                                        altMarker13.rollbackTo();
                                        PsiBuilder.Marker altMarker10;
                                        altMarker10 = builder.mark();
                                        if (this.metachar_68_alt_9(builder, opp)) {
                                            altMarker10.drop();
                                        } else {
                                            altMarker10.rollbackTo();
                                            PsiBuilder.Marker altMarker9;
                                            altMarker9 = builder.mark();
                                            if (this.metachar_68_alt_8(builder, opp)) {
                                                altMarker9.drop();
                                            } else {
                                                altMarker9.rollbackTo();
                                                PsiBuilder.Marker altMarker8;
                                                altMarker8 = builder.mark();
                                                if (this.metachar_68_alt_7(builder, opp)) {
                                                    altMarker8.drop();
                                                } else {
                                                    altMarker8.rollbackTo();
                                                    PsiBuilder.Marker altMarker7;
                                                    altMarker7 = builder.mark();
                                                    if (this.metachar_68_alt_6(builder, opp)) {
                                                        altMarker7.drop();
                                                    } else {
                                                        altMarker7.rollbackTo();
                                                        PsiBuilder.Marker altMarker6;
                                                        altMarker6 = builder.mark();
                                                        if (this.metachar_68_alt_5(builder, opp)) {
                                                            altMarker6.drop();
                                                        } else {
                                                            altMarker6.rollbackTo();
                                                            PsiBuilder.Marker altMarker5;
                                                            altMarker5 = builder.mark();
                                                            if (this.metachar_68_alt_4(builder, opp)) {
                                                                altMarker5.drop();
                                                            } else {
                                                                altMarker5.rollbackTo();
                                                                PsiBuilder.Marker altMarker1;
                                                                altMarker1 = builder.mark();
                                                                if (this.metachar_68_alt_1(builder, opp)) {
                                                                    altMarker1.drop();
                                                                } else {
                                                                    altMarker1.rollbackTo();
                                                                    return false;
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean method_def_69_quant_1(PsiBuilder builder, OPP opp) {
        if (!(this.method_name_70(builder))) {
            return false;
        }
        return true;
    }

    private boolean method_def_69_quant_2(PsiBuilder builder, OPP opp) {
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.PARENTHESES_CLOSE) && (tt2.equals(")"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean method_def_69_quant_3(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker2;
        marker2 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.PARENTHESES_OPEN) && (tt1.equals("("))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.signature_174(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker3;
        quantMarker3 = builder.mark();
        if (this.method_def_69_quant_2(builder, opp)) {
            quantMarker3.drop();
        } else {
            quantMarker3.rollbackTo();
        }
        marker2.done(RakuElementTypes.SIGNATURE);
        return true;
    }

    private boolean method_def_69_quant_4(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean method_def_69_quant_5(PsiBuilder builder, OPP opp) {
        if (!(this.trait_242(builder))) {
            return false;
        }
        return true;
    }

    private boolean method_def_69_alt_6(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean method_def_69_alt_7(PsiBuilder builder, OPP opp) {
        if (!(this.blockoid_16(builder))) {
            return false;
        }
        return true;
    }

    private boolean method_def_69_alt_8(PsiBuilder builder, OPP opp) {
        if (!(this.onlystar_87(builder))) {
            return false;
        }
        return true;
    }

    private boolean method_def_69(PsiBuilder builder) {
        OPP opp;
        opp = null;
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker1;
        quantMarker1 = builder.mark();
        if (this.method_def_69_quant_1(builder, opp)) {
            quantMarker1.drop();
        } else {
            quantMarker1.rollbackTo();
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker4;
        quantMarker4 = builder.mark();
        if (this.method_def_69_quant_3(builder, opp)) {
            quantMarker4.drop();
        } else {
            quantMarker4.rollbackTo();
        }
        PsiBuilder.Marker quantMarker5;
        quantMarker5 = builder.mark();
        if (this.method_def_69_quant_4(builder, opp)) {
            quantMarker5.drop();
        } else {
            quantMarker5.rollbackTo();
        }
        while (true) {
            PsiBuilder.Marker quantMarker6;
            quantMarker6 = builder.mark();
            if (this.method_def_69_quant_5(builder, opp)) {
                quantMarker6.drop();
            } else {
                quantMarker6.rollbackTo();
                break;
            }
        }
        PsiBuilder.Marker altMarker9;
        altMarker9 = builder.mark();
        if (this.method_def_69_alt_8(builder, opp)) {
            altMarker9.drop();
        } else {
            altMarker9.rollbackTo();
            PsiBuilder.Marker altMarker8;
            altMarker8 = builder.mark();
            if (this.method_def_69_alt_7(builder, opp)) {
                altMarker8.drop();
            } else {
                altMarker8.rollbackTo();
                PsiBuilder.Marker altMarker7;
                altMarker7 = builder.mark();
                if (this.method_def_69_alt_6(builder, opp)) {
                    altMarker7.drop();
                } else {
                    altMarker7.rollbackTo();
                    return false;
                }
            }
        }
        return true;
    }

    private boolean method_name_70(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.ROUTINE_NAME) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.longname_colonpairs_67(builder))) {
            return false;
        }
        marker1.done(RakuElementTypes.LONG_NAME);
        return true;
    }

    private boolean methodop_71_alt_1(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean methodop_71_alt_2(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean methodop_71_alt_3(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker2;
        altMarker2 = builder.mark();
        if (this.methodop_71_alt_2(builder, opp)) {
            altMarker2.drop();
        } else {
            altMarker2.rollbackTo();
            PsiBuilder.Marker altMarker1;
            altMarker1 = builder.mark();
            if (this.methodop_71_alt_1(builder, opp)) {
                altMarker1.drop();
            } else {
                altMarker1.rollbackTo();
                return false;
            }
        }
        if (!(this.quote_128(builder))) {
            return false;
        }
        return true;
    }

    private boolean methodop_71_alt_4(PsiBuilder builder, OPP opp) {
        if (!(this.variable_253(builder))) {
            return false;
        }
        return true;
    }

    private boolean methodop_71_alt_5(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker5;
        marker5 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.METHOD_CALL_NAME) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.longname_colonpairs_67(builder))) {
            return false;
        }
        marker5.done(RakuElementTypes.LONG_NAME);
        return true;
    }

    private boolean methodop_71_quant_6(PsiBuilder builder, OPP opp) {
        if (!(this.unsp_250(builder))) {
            return false;
        }
        return true;
    }

    private boolean methodop_71_alt_7(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean methodop_71_alt_8(PsiBuilder builder, OPP opp) {
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.INVOCANT_MARKER) && (tt1.equals(":"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.arglist_6(builder))) {
            return false;
        }
        return true;
    }

    private boolean methodop_71_alt_9(PsiBuilder builder, OPP opp) {
        if (!(this.args_7(builder))) {
            return false;
        }
        return true;
    }

    private boolean methodop_71_alt_10(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker10;
        altMarker10 = builder.mark();
        if (this.methodop_71_alt_9(builder, opp)) {
            altMarker10.drop();
        } else {
            altMarker10.rollbackTo();
            PsiBuilder.Marker altMarker9;
            altMarker9 = builder.mark();
            if (this.methodop_71_alt_8(builder, opp)) {
                altMarker9.drop();
            } else {
                altMarker9.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean methodop_71_quant_11(PsiBuilder builder, OPP opp) {
        if (!(this.unsp_250(builder))) {
            return false;
        }
        return true;
    }

    private boolean methodop_71(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker6;
        altMarker6 = builder.mark();
        if (this.methodop_71_alt_5(builder, opp)) {
            altMarker6.drop();
        } else {
            altMarker6.rollbackTo();
            PsiBuilder.Marker altMarker4;
            altMarker4 = builder.mark();
            if (this.methodop_71_alt_4(builder, opp)) {
                altMarker4.drop();
            } else {
                altMarker4.rollbackTo();
                PsiBuilder.Marker altMarker3;
                altMarker3 = builder.mark();
                if (this.methodop_71_alt_3(builder, opp)) {
                    altMarker3.drop();
                } else {
                    altMarker3.rollbackTo();
                    return false;
                }
            }
        }
        PsiBuilder.Marker quantMarker7;
        quantMarker7 = builder.mark();
        if (this.methodop_71_quant_6(builder, opp)) {
            quantMarker7.drop();
        } else {
            quantMarker7.rollbackTo();
        }
        PsiBuilder.Marker altMarker11;
        altMarker11 = builder.mark();
        if (this.methodop_71_alt_10(builder, opp)) {
            altMarker11.drop();
        } else {
            altMarker11.rollbackTo();
            PsiBuilder.Marker altMarker8;
            altMarker8 = builder.mark();
            if (this.methodop_71_alt_7(builder, opp)) {
                altMarker8.drop();
            } else {
                altMarker8.rollbackTo();
                return false;
            }
        }
        PsiBuilder.Marker quantMarker12;
        quantMarker12 = builder.mark();
        if (this.methodop_71_quant_11(builder, opp)) {
            quantMarker12.drop();
        } else {
            quantMarker12.rollbackTo();
        }
        return true;
    }

    private boolean mod_ident_72_alt_1(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.REGEX_MOD_UNKNOWN) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean mod_ident_72_alt_2(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.REGEX_MOD_INTERNAL) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean mod_ident_72(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker2;
        altMarker2 = builder.mark();
        if (this.mod_ident_72_alt_2(builder, opp)) {
            altMarker2.drop();
        } else {
            altMarker2.rollbackTo();
            PsiBuilder.Marker altMarker1;
            altMarker1 = builder.mark();
            if (this.mod_ident_72_alt_1(builder, opp)) {
                altMarker1.drop();
            } else {
                altMarker1.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean mod_internal_73_alt_1(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean mod_internal_73_alt_2(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean mod_internal_73_quant_3(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker2;
        altMarker2 = builder.mark();
        if (this.mod_internal_73_alt_2(builder, opp)) {
            altMarker2.drop();
        } else {
            altMarker2.rollbackTo();
            PsiBuilder.Marker altMarker1;
            altMarker1 = builder.mark();
            if (this.mod_internal_73_alt_1(builder, opp)) {
                altMarker1.drop();
            } else {
                altMarker1.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean mod_internal_73_quant_4(PsiBuilder builder, OPP opp) {
        String tt3;
        tt3 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.REGEX_MOD_INTERNAL) && (tt3.equals(")"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean mod_internal_73_quant_5(PsiBuilder builder, OPP opp) {
        if (!(this.value_252(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker5;
        quantMarker5 = builder.mark();
        if (this.mod_internal_73_quant_4(builder, opp)) {
            quantMarker5.drop();
        } else {
            quantMarker5.rollbackTo();
        }
        return true;
    }

    private boolean mod_internal_73_quant_6(PsiBuilder builder, OPP opp) {
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.REGEX_MOD_INTERNAL) && (tt2.equals("("))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker quantMarker6;
        quantMarker6 = builder.mark();
        if (this.mod_internal_73_quant_5(builder, opp)) {
            quantMarker6.drop();
        } else {
            quantMarker6.rollbackTo();
        }
        return true;
    }

    private boolean mod_internal_73_alt_7(PsiBuilder builder, OPP opp) {
        if (!(this.mod_ident_72(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker7;
        quantMarker7 = builder.mark();
        if (this.mod_internal_73_quant_6(builder, opp)) {
            quantMarker7.drop();
        } else {
            quantMarker7.rollbackTo();
        }
        return true;
    }

    private boolean mod_internal_73_alt_8(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.REGEX_MOD_INTERNAL_NUMERIC) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if ((builder.getTokenType()) == RakuTokenTypes.REGEX_MOD_INTERNAL) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.mod_ident_72(builder))) {
            return false;
        }
        return true;
    }

    private boolean mod_internal_73_alt_9(PsiBuilder builder, OPP opp) {
        String tt4;
        tt4 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.REGEX_MOD_INTERNAL) && (tt4.equals("!"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.mod_ident_72(builder))) {
            return false;
        }
        return true;
    }

    private boolean mod_internal_73(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker quantMarker3;
        quantMarker3 = builder.mark();
        if (this.mod_internal_73_quant_3(builder, opp)) {
            quantMarker3.drop();
        } else {
            quantMarker3.rollbackTo();
        }
        PsiBuilder.Marker marker4;
        marker4 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.REGEX_MOD_INTERNAL) && (tt1.equals(":"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker altMarker10;
        altMarker10 = builder.mark();
        if (this.mod_internal_73_alt_9(builder, opp)) {
            altMarker10.drop();
        } else {
            altMarker10.rollbackTo();
            PsiBuilder.Marker altMarker9;
            altMarker9 = builder.mark();
            if (this.mod_internal_73_alt_8(builder, opp)) {
                altMarker9.drop();
            } else {
                altMarker9.rollbackTo();
                PsiBuilder.Marker altMarker8;
                altMarker8 = builder.mark();
                if (this.mod_internal_73_alt_7(builder, opp)) {
                    altMarker8.drop();
                } else {
                    altMarker8.rollbackTo();
                    return false;
                }
            }
        }
        marker4.done(RakuElementTypes.REGEX_MOD_INTERNAL);
        return true;
    }

    private boolean module_name_74(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        PsiBuilder.Marker marker2;
        marker2 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.NAME) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.longname_colonpairs_67(builder))) {
            return false;
        }
        marker2.done(RakuElementTypes.LONG_NAME);
        marker1.done(RakuElementTypes.MODULE_NAME);
        return true;
    }

    private boolean morename_75_quant_1(PsiBuilder builder, OPP opp) {
        if (!(this.identifier_53(builder))) {
            return false;
        }
        return true;
    }

    private boolean morename_75(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker quantMarker1;
        quantMarker1 = builder.mark();
        if (this.morename_75_quant_1(builder, opp)) {
            quantMarker1.drop();
        } else {
            quantMarker1.rollbackTo();
        }
        return true;
    }

    private boolean multi_declarator_76_alt_1(PsiBuilder builder, OPP opp) {
        if (!(this.declarator_36(builder))) {
            return false;
        }
        return true;
    }

    private boolean multi_declarator_76_alt_2(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean multi_declarator_76_alt_3(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker4;
        marker4 = builder.mark();
        if (!(this.routine_def_154(builder))) {
            return false;
        }
        marker4.done(RakuElementTypes.ROUTINE_DECLARATION);
        return true;
    }

    private boolean multi_declarator_76_alt_4(PsiBuilder builder, OPP opp) {
        if (!(this.declarator_36(builder))) {
            return false;
        }
        return true;
    }

    private boolean multi_declarator_76_alt_5(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker2;
        marker2 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.MULTI_DECLARATOR) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.kok_64(builder))) {
            return false;
        }
        PsiBuilder.Marker altMarker6;
        altMarker6 = builder.mark();
        if (this.multi_declarator_76_alt_4(builder, opp)) {
            altMarker6.drop();
        } else {
            altMarker6.rollbackTo();
            PsiBuilder.Marker altMarker5;
            altMarker5 = builder.mark();
            if (this.multi_declarator_76_alt_3(builder, opp)) {
                altMarker5.drop();
            } else {
                altMarker5.rollbackTo();
                PsiBuilder.Marker altMarker3;
                altMarker3 = builder.mark();
                if (this.multi_declarator_76_alt_2(builder, opp)) {
                    altMarker3.drop();
                } else {
                    altMarker3.rollbackTo();
                    return false;
                }
            }
        }
        marker2.done(RakuElementTypes.MULTI_DECLARATION);
        return true;
    }

    private boolean multi_declarator_76(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker7;
        altMarker7 = builder.mark();
        if (this.multi_declarator_76_alt_5(builder, opp)) {
            altMarker7.drop();
        } else {
            altMarker7.rollbackTo();
            PsiBuilder.Marker altMarker1;
            altMarker1 = builder.mark();
            if (this.multi_declarator_76_alt_1(builder, opp)) {
                altMarker1.drop();
            } else {
                altMarker1.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean multilinecomment_77_quant_1(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.COMMENT_QUOTE_CLOSE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean multilinecomment_77(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.COMMENT_STARTER) && (tt1.equals("#`"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if ((builder.getTokenType()) == RakuTokenTypes.COMMENT_QUOTE_OPEN) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.multilinecommentnibbler_78(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker2;
        quantMarker2 = builder.mark();
        if (this.multilinecomment_77_quant_1(builder, opp)) {
            quantMarker2.drop();
        } else {
            quantMarker2.rollbackTo();
        }
        marker1.done(RakuElementTypes.COMMENT);
        return true;
    }

    private boolean multilinecommentnibbler_78_alt_1(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.COMMENT) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean multilinecommentnibbler_78_alt_2(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.COMMENT) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean multilinecommentnibbler_78_quant_3(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.COMMENT_QUOTE_CLOSE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean multilinecommentnibbler_78_alt_4(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.COMMENT_QUOTE_OPEN) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.multilinecommentnibbler_78(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker3;
        quantMarker3 = builder.mark();
        if (this.multilinecommentnibbler_78_quant_3(builder, opp)) {
            quantMarker3.drop();
        } else {
            quantMarker3.rollbackTo();
        }
        return true;
    }

    private boolean multilinecommentnibbler_78_quant_5(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker4;
        altMarker4 = builder.mark();
        if (this.multilinecommentnibbler_78_alt_4(builder, opp)) {
            altMarker4.drop();
        } else {
            altMarker4.rollbackTo();
            PsiBuilder.Marker altMarker2;
            altMarker2 = builder.mark();
            if (this.multilinecommentnibbler_78_alt_2(builder, opp)) {
                altMarker2.drop();
            } else {
                altMarker2.rollbackTo();
                PsiBuilder.Marker altMarker1;
                altMarker1 = builder.mark();
                if (this.multilinecommentnibbler_78_alt_1(builder, opp)) {
                    altMarker1.drop();
                } else {
                    altMarker1.rollbackTo();
                    return false;
                }
            }
        }
        return true;
    }

    private boolean multilinecommentnibbler_78(PsiBuilder builder) {
        OPP opp;
        opp = null;
        while (true) {
            PsiBuilder.Marker quantMarker5;
            quantMarker5 = builder.mark();
            if (this.multilinecommentnibbler_78_quant_5(builder, opp)) {
                quantMarker5.drop();
            } else {
                quantMarker5.rollbackTo();
                break;
            }
        }
        return true;
    }

    private boolean name_79_quant_1(PsiBuilder builder, OPP opp) {
        if (!(this.morename_75(builder))) {
            return false;
        }
        return true;
    }

    private boolean name_79_alt_2(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker quantMarker1;
        quantMarker1 = builder.mark();
        if (this.name_79_quant_1(builder, opp)) {
            quantMarker1.drop();
        } else {
            quantMarker1.rollbackTo();
            return false;
        }
        while (true) {
            PsiBuilder.Marker quantMarker2;
            quantMarker2 = builder.mark();
            if (this.name_79_quant_1(builder, opp)) {
                quantMarker2.drop();
            } else {
                quantMarker2.rollbackTo();
                break;
            }
        }
        return true;
    }

    private boolean name_79_quant_3(PsiBuilder builder, OPP opp) {
        if (!(this.morename_75(builder))) {
            return false;
        }
        return true;
    }

    private boolean name_79_alt_4(PsiBuilder builder, OPP opp) {
        if (!(this.identifier_53(builder))) {
            return false;
        }
        while (true) {
            PsiBuilder.Marker quantMarker4;
            quantMarker4 = builder.mark();
            if (this.name_79_quant_3(builder, opp)) {
                quantMarker4.drop();
            } else {
                quantMarker4.rollbackTo();
                break;
            }
        }
        return true;
    }

    private boolean name_79(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker5;
        altMarker5 = builder.mark();
        if (this.name_79_alt_4(builder, opp)) {
            altMarker5.drop();
        } else {
            altMarker5.rollbackTo();
            PsiBuilder.Marker altMarker3;
            altMarker3 = builder.mark();
            if (this.name_79_alt_2(builder, opp)) {
                altMarker3.drop();
            } else {
                altMarker3.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean named_param_80_alt_1(PsiBuilder builder, OPP opp) {
        if (!(this.param_var_93(builder))) {
            return false;
        }
        return true;
    }

    private boolean named_param_80_alt_2(PsiBuilder builder, OPP opp) {
        if (!(this.param_var_93(builder))) {
            return false;
        }
        return true;
    }

    private boolean named_param_80_alt_3(PsiBuilder builder, OPP opp) {
        if (!(this.named_param_80(builder))) {
            return false;
        }
        return true;
    }

    private boolean named_param_80_quant_4(PsiBuilder builder, OPP opp) {
        String tt3;
        tt3 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.NAMED_PARAMETER_SYNTAX) && (tt3.equals(")"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean named_param_80_quant_5(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker5;
        quantMarker5 = builder.mark();
        if (this.named_param_80_quant_4(builder, opp)) {
            quantMarker5.drop();
        } else {
            quantMarker5.rollbackTo();
        }
        return true;
    }

    private boolean named_param_80_quant_6(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker4;
        altMarker4 = builder.mark();
        if (this.named_param_80_alt_3(builder, opp)) {
            altMarker4.drop();
        } else {
            altMarker4.rollbackTo();
            PsiBuilder.Marker altMarker3;
            altMarker3 = builder.mark();
            if (this.named_param_80_alt_2(builder, opp)) {
                altMarker3.drop();
            } else {
                altMarker3.rollbackTo();
                return false;
            }
        }
        PsiBuilder.Marker quantMarker6;
        quantMarker6 = builder.mark();
        if (this.named_param_80_quant_5(builder, opp)) {
            quantMarker6.drop();
        } else {
            quantMarker6.rollbackTo();
        }
        return true;
    }

    private boolean named_param_80_quant_7(PsiBuilder builder, OPP opp) {
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.NAMED_PARAMETER_SYNTAX) && (tt2.equals("("))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker7;
        quantMarker7 = builder.mark();
        if (this.named_param_80_quant_6(builder, opp)) {
            quantMarker7.drop();
        } else {
            quantMarker7.rollbackTo();
        }
        return true;
    }

    private boolean named_param_80_alt_8(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.NAMED_PARAMETER_NAME_ALIAS) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker quantMarker8;
        quantMarker8 = builder.mark();
        if (this.named_param_80_quant_7(builder, opp)) {
            quantMarker8.drop();
        } else {
            quantMarker8.rollbackTo();
        }
        return true;
    }

    private boolean named_param_80_quant_9(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker9;
        altMarker9 = builder.mark();
        if (this.named_param_80_alt_8(builder, opp)) {
            altMarker9.drop();
        } else {
            altMarker9.rollbackTo();
            PsiBuilder.Marker altMarker2;
            altMarker2 = builder.mark();
            if (this.named_param_80_alt_1(builder, opp)) {
                altMarker2.drop();
            } else {
                altMarker2.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean named_param_80(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.NAMED_PARAMETER_SYNTAX) && (tt1.equals(":"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker quantMarker10;
        quantMarker10 = builder.mark();
        if (this.named_param_80_quant_9(builder, opp)) {
            quantMarker10.drop();
        } else {
            quantMarker10.rollbackTo();
        }
        marker1.done(RakuElementTypes.NAMED_PARAMETER);
        return true;
    }

    private boolean nextterm_81_alt_1(PsiBuilder builder, OPP opp) {
        if (!(this.term_224(builder))) {
            return false;
        }
        return true;
    }

    private boolean nextterm_81_alt_2(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker2;
        marker2 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.DOTTY_NEXT_TERM) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.dottyop_41(builder))) {
            return false;
        }
        marker2.done(RakuElementTypes.METHOD_CALL);
        return true;
    }

    private boolean nextterm_81_alt_3(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker4;
        marker4 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.NULL_TERM) {
            builder.advanceLexer();
        } else {
            return false;
        }
        marker4.done(RakuElementTypes.NULL_TERM);
        return true;
    }

    private boolean nextterm_81_alt_4(PsiBuilder builder, OPP opp) {
        if (!(this.term_224(builder))) {
            return false;
        }
        return true;
    }

    private boolean nextterm_81_alt_5(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker6;
        altMarker6 = builder.mark();
        if (this.nextterm_81_alt_4(builder, opp)) {
            altMarker6.drop();
        } else {
            altMarker6.rollbackTo();
            PsiBuilder.Marker altMarker5;
            altMarker5 = builder.mark();
            if (this.nextterm_81_alt_3(builder, opp)) {
                altMarker5.drop();
            } else {
                altMarker5.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean nextterm_81(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker7;
        altMarker7 = builder.mark();
        if (this.nextterm_81_alt_5(builder, opp)) {
            altMarker7.drop();
        } else {
            altMarker7.rollbackTo();
            PsiBuilder.Marker altMarker3;
            altMarker3 = builder.mark();
            if (this.nextterm_81_alt_2(builder, opp)) {
                altMarker3.drop();
            } else {
                altMarker3.rollbackTo();
                PsiBuilder.Marker altMarker1;
                altMarker1 = builder.mark();
                if (this.nextterm_81_alt_1(builder, opp)) {
                    altMarker1.drop();
                } else {
                    altMarker1.rollbackTo();
                    return false;
                }
            }
        }
        return true;
    }

    private boolean normspace_82(PsiBuilder builder) {
        OPP opp;
        opp = null;
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean number_83(PsiBuilder builder) {
        OPP opp;
        opp = null;
        if (!(this.numish_84(builder))) {
            return false;
        }
        return true;
    }

    private boolean numish_84_alt_1(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.NUMBER_LITERAL) {
            builder.advanceLexer();
        } else {
            return false;
        }
        marker1.done(RakuElementTypes.NUMBER_LITERAL);
        return true;
    }

    private boolean numish_84_alt_2(PsiBuilder builder, OPP opp) {
        if (!(this.integer_61(builder))) {
            return false;
        }
        return true;
    }

    private boolean numish_84_alt_3(PsiBuilder builder, OPP opp) {
        if (!(this.dec_number_34(builder))) {
            return false;
        }
        return true;
    }

    private boolean numish_84_alt_4(PsiBuilder builder, OPP opp) {
        if (!(this.rad_number_146(builder))) {
            return false;
        }
        return true;
    }

    private boolean numish_84_alt_5(PsiBuilder builder, OPP opp) {
        if (!(this.rat_number_148(builder))) {
            return false;
        }
        return true;
    }

    private boolean numish_84_alt_6(PsiBuilder builder, OPP opp) {
        if (!(this.complex_number_32(builder))) {
            return false;
        }
        return true;
    }

    private boolean numish_84(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker7;
        altMarker7 = builder.mark();
        if (this.numish_84_alt_6(builder, opp)) {
            altMarker7.drop();
        } else {
            altMarker7.rollbackTo();
            PsiBuilder.Marker altMarker6;
            altMarker6 = builder.mark();
            if (this.numish_84_alt_5(builder, opp)) {
                altMarker6.drop();
            } else {
                altMarker6.rollbackTo();
                PsiBuilder.Marker altMarker5;
                altMarker5 = builder.mark();
                if (this.numish_84_alt_4(builder, opp)) {
                    altMarker5.drop();
                } else {
                    altMarker5.rollbackTo();
                    PsiBuilder.Marker altMarker4;
                    altMarker4 = builder.mark();
                    if (this.numish_84_alt_3(builder, opp)) {
                        altMarker4.drop();
                    } else {
                        altMarker4.rollbackTo();
                        PsiBuilder.Marker altMarker3;
                        altMarker3 = builder.mark();
                        if (this.numish_84_alt_2(builder, opp)) {
                            altMarker3.drop();
                        } else {
                            altMarker3.rollbackTo();
                            PsiBuilder.Marker altMarker2;
                            altMarker2 = builder.mark();
                            if (this.numish_84_alt_1(builder, opp)) {
                                altMarker2.drop();
                            } else {
                                altMarker2.rollbackTo();
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean octint_85_quant_1(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean octint_85_quant_2(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker quantMarker1;
        quantMarker1 = builder.mark();
        if (this.octint_85_quant_1(builder, opp)) {
            quantMarker1.drop();
        } else {
            quantMarker1.rollbackTo();
            return false;
        }
        while (true) {
            PsiBuilder.Marker quantMarker2;
            quantMarker2 = builder.mark();
            if (this.octint_85_quant_1(builder, opp)) {
                quantMarker2.drop();
            } else {
                quantMarker2.rollbackTo();
                break;
            }
        }
        return true;
    }

    private boolean octint_85(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker quantMarker3;
        quantMarker3 = builder.mark();
        if (this.octint_85_quant_2(builder, opp)) {
            quantMarker3.drop();
        } else {
            quantMarker3.rollbackTo();
            return false;
        }
        while (true) {
            PsiBuilder.Marker quantMarker4;
            quantMarker4 = builder.mark();
            if (this.octint_85_quant_2(builder, opp)) {
                quantMarker4.drop();
            } else {
                quantMarker4.rollbackTo();
                break;
            }
        }
        return true;
    }

    private boolean octints_86_quant_1(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean octints_86_quant_2(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean octints_86_quant_3(PsiBuilder builder, OPP opp) {
        while (true) {
            PsiBuilder.Marker quantMarker1;
            quantMarker1 = builder.mark();
            if (this.octints_86_quant_1(builder, opp)) {
                quantMarker1.drop();
            } else {
                quantMarker1.rollbackTo();
                break;
            }
        }
        if (!(this.octint_85(builder))) {
            return false;
        }
        while (true) {
            PsiBuilder.Marker quantMarker2;
            quantMarker2 = builder.mark();
            if (this.octints_86_quant_2(builder, opp)) {
                quantMarker2.drop();
            } else {
                quantMarker2.rollbackTo();
                break;
            }
        }
        return true;
    }

    private boolean octints_86(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker quantMarker3;
        quantMarker3 = builder.mark();
        if (this.octints_86_quant_3(builder, opp)) {
            quantMarker3.drop();
        } else {
            quantMarker3.rollbackTo();
            return false;
        }
        while (true) {
            PsiBuilder.Marker quantMarker4;
            quantMarker4 = builder.mark();
            if (this.octints_86_quant_3(builder, opp)) {
                quantMarker4.drop();
            } else {
                quantMarker4.rollbackTo();
                break;
            }
        }
        return true;
    }

    private boolean onlystar_87(PsiBuilder builder) {
        OPP opp;
        opp = null;
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.BLOCK_CURLY_BRACKET_OPEN) && (tt1.equals("{"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.ONLY_STAR) && (tt2.equals("*"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        String tt3;
        tt3 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.BLOCK_CURLY_BRACKET_CLOSE) && (tt3.equals("}"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean package_declarator_88_quant_1(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean package_declarator_88_quant_2(PsiBuilder builder, OPP opp) {
        if (!(this.typename_249(builder))) {
            return false;
        }
        return true;
    }

    private boolean package_declarator_88_quant_3(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean package_declarator_88_alt_4(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.TRUSTS) && (tt1.equals("trusts"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.kok_64(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker2;
        quantMarker2 = builder.mark();
        if (this.package_declarator_88_quant_1(builder, opp)) {
            quantMarker2.drop();
        } else {
            quantMarker2.rollbackTo();
        }
        PsiBuilder.Marker quantMarker3;
        quantMarker3 = builder.mark();
        if (this.package_declarator_88_quant_2(builder, opp)) {
            quantMarker3.drop();
        } else {
            quantMarker3.rollbackTo();
        }
        PsiBuilder.Marker quantMarker4;
        quantMarker4 = builder.mark();
        if (this.package_declarator_88_quant_3(builder, opp)) {
            quantMarker4.drop();
        } else {
            quantMarker4.rollbackTo();
        }
        marker1.done(RakuElementTypes.TRUSTS);
        return true;
    }

    private boolean package_declarator_88_quant_5(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean package_declarator_88_quant_6(PsiBuilder builder, OPP opp) {
        if (!(this.trait_242(builder))) {
            return false;
        }
        return true;
    }

    private boolean package_declarator_88_quant_7(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean package_declarator_88_alt_8(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker6;
        marker6 = builder.mark();
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.ALSO) && (tt2.equals("also"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.kok_64(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker7;
        quantMarker7 = builder.mark();
        if (this.package_declarator_88_quant_5(builder, opp)) {
            quantMarker7.drop();
        } else {
            quantMarker7.rollbackTo();
        }
        while (true) {
            PsiBuilder.Marker quantMarker8;
            quantMarker8 = builder.mark();
            if (this.package_declarator_88_quant_6(builder, opp)) {
                quantMarker8.drop();
            } else {
                quantMarker8.rollbackTo();
                break;
            }
        }
        PsiBuilder.Marker quantMarker9;
        quantMarker9 = builder.mark();
        if (this.package_declarator_88_quant_7(builder, opp)) {
            quantMarker9.drop();
        } else {
            quantMarker9.rollbackTo();
        }
        marker6.done(RakuElementTypes.ALSO);
        return true;
    }

    private boolean package_declarator_88_alt_9(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker11;
        marker11 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.PACKAGE_DECLARATOR) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.kok_64(builder))) {
            return false;
        }
        if (!(this.package_def_89(builder))) {
            return false;
        }
        marker11.done(RakuElementTypes.PACKAGE_DECLARATION);
        return true;
    }

    private boolean package_declarator_88(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker12;
        altMarker12 = builder.mark();
        if (this.package_declarator_88_alt_9(builder, opp)) {
            altMarker12.drop();
        } else {
            altMarker12.rollbackTo();
            PsiBuilder.Marker altMarker10;
            altMarker10 = builder.mark();
            if (this.package_declarator_88_alt_8(builder, opp)) {
                altMarker10.drop();
            } else {
                altMarker10.rollbackTo();
                PsiBuilder.Marker altMarker5;
                altMarker5 = builder.mark();
                if (this.package_declarator_88_alt_4(builder, opp)) {
                    altMarker5.drop();
                } else {
                    altMarker5.rollbackTo();
                    return false;
                }
            }
        }
        return true;
    }

    private boolean package_def_89_quant_1(PsiBuilder builder, OPP opp) {
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.TYPE_PARAMETER_BRACKET) && (tt2.equals("]"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean package_def_89_quant_2(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean package_def_89_quant_3(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker2;
        marker2 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.TYPE_PARAMETER_BRACKET) && (tt1.equals("["))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.signature_174(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker3;
        quantMarker3 = builder.mark();
        if (this.package_def_89_quant_1(builder, opp)) {
            quantMarker3.drop();
        } else {
            quantMarker3.rollbackTo();
        }
        marker2.done(RakuElementTypes.ROLE_SIGNATURE);
        PsiBuilder.Marker quantMarker4;
        quantMarker4 = builder.mark();
        if (this.package_def_89_quant_2(builder, opp)) {
            quantMarker4.drop();
        } else {
            quantMarker4.rollbackTo();
        }
        return true;
    }

    private boolean package_def_89_quant_4(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.PACKAGE_NAME) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.longname_colonpairs_67(builder))) {
            return false;
        }
        marker1.done(RakuElementTypes.LONG_NAME);
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker5;
        quantMarker5 = builder.mark();
        if (this.package_def_89_quant_3(builder, opp)) {
            quantMarker5.drop();
        } else {
            quantMarker5.rollbackTo();
        }
        return true;
    }

    private boolean package_def_89_quant_5(PsiBuilder builder, OPP opp) {
        if (!(this.trait_242(builder))) {
            return false;
        }
        return true;
    }

    private boolean package_def_89_alt_6(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean package_def_89_quant_7(PsiBuilder builder, OPP opp) {
        if (!(this.statementlist_221(builder))) {
            return false;
        }
        return true;
    }

    private boolean package_def_89_alt_8(PsiBuilder builder, OPP opp) {
        String tt3;
        tt3 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STATEMENT_TERMINATOR) && (tt3.equals(";"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker9;
        quantMarker9 = builder.mark();
        if (this.package_def_89_quant_7(builder, opp)) {
            quantMarker9.drop();
        } else {
            quantMarker9.rollbackTo();
        }
        return true;
    }

    private boolean package_def_89_alt_9(PsiBuilder builder, OPP opp) {
        if (!(this.blockoid_16(builder))) {
            return false;
        }
        return true;
    }

    private boolean package_def_89(PsiBuilder builder) {
        OPP opp;
        opp = null;
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker6;
        quantMarker6 = builder.mark();
        if (this.package_def_89_quant_4(builder, opp)) {
            quantMarker6.drop();
        } else {
            quantMarker6.rollbackTo();
        }
        while (true) {
            PsiBuilder.Marker quantMarker7;
            quantMarker7 = builder.mark();
            if (this.package_def_89_quant_5(builder, opp)) {
                quantMarker7.drop();
            } else {
                quantMarker7.rollbackTo();
                break;
            }
        }
        PsiBuilder.Marker altMarker11;
        altMarker11 = builder.mark();
        if (this.package_def_89_alt_9(builder, opp)) {
            altMarker11.drop();
        } else {
            altMarker11.rollbackTo();
            PsiBuilder.Marker altMarker10;
            altMarker10 = builder.mark();
            if (this.package_def_89_alt_8(builder, opp)) {
                altMarker10.drop();
            } else {
                altMarker10.rollbackTo();
                PsiBuilder.Marker altMarker8;
                altMarker8 = builder.mark();
                if (this.package_def_89_alt_6(builder, opp)) {
                    altMarker8.drop();
                } else {
                    altMarker8.rollbackTo();
                    return false;
                }
            }
        }
        return true;
    }

    private boolean package_kind_90_alt_1(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean package_kind_90_alt_2(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean package_kind_90_alt_3(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean package_kind_90_alt_4(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean package_kind_90_alt_5(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean package_kind_90_alt_6(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean package_kind_90_alt_7(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean package_kind_90_alt_8(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean package_kind_90_alt_9(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean package_kind_90_alt_10(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean package_kind_90_alt_11(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean package_kind_90(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker11;
        altMarker11 = builder.mark();
        if (this.package_kind_90_alt_11(builder, opp)) {
            altMarker11.drop();
        } else {
            altMarker11.rollbackTo();
            PsiBuilder.Marker altMarker10;
            altMarker10 = builder.mark();
            if (this.package_kind_90_alt_10(builder, opp)) {
                altMarker10.drop();
            } else {
                altMarker10.rollbackTo();
                PsiBuilder.Marker altMarker9;
                altMarker9 = builder.mark();
                if (this.package_kind_90_alt_9(builder, opp)) {
                    altMarker9.drop();
                } else {
                    altMarker9.rollbackTo();
                    PsiBuilder.Marker altMarker8;
                    altMarker8 = builder.mark();
                    if (this.package_kind_90_alt_8(builder, opp)) {
                        altMarker8.drop();
                    } else {
                        altMarker8.rollbackTo();
                        PsiBuilder.Marker altMarker7;
                        altMarker7 = builder.mark();
                        if (this.package_kind_90_alt_7(builder, opp)) {
                            altMarker7.drop();
                        } else {
                            altMarker7.rollbackTo();
                            PsiBuilder.Marker altMarker6;
                            altMarker6 = builder.mark();
                            if (this.package_kind_90_alt_6(builder, opp)) {
                                altMarker6.drop();
                            } else {
                                altMarker6.rollbackTo();
                                PsiBuilder.Marker altMarker5;
                                altMarker5 = builder.mark();
                                if (this.package_kind_90_alt_5(builder, opp)) {
                                    altMarker5.drop();
                                } else {
                                    altMarker5.rollbackTo();
                                    PsiBuilder.Marker altMarker4;
                                    altMarker4 = builder.mark();
                                    if (this.package_kind_90_alt_4(builder, opp)) {
                                        altMarker4.drop();
                                    } else {
                                        altMarker4.rollbackTo();
                                        PsiBuilder.Marker altMarker3;
                                        altMarker3 = builder.mark();
                                        if (this.package_kind_90_alt_3(builder, opp)) {
                                            altMarker3.drop();
                                        } else {
                                            altMarker3.rollbackTo();
                                            PsiBuilder.Marker altMarker2;
                                            altMarker2 = builder.mark();
                                            if (this.package_kind_90_alt_2(builder, opp)) {
                                                altMarker2.drop();
                                            } else {
                                                altMarker2.rollbackTo();
                                                PsiBuilder.Marker altMarker1;
                                                altMarker1 = builder.mark();
                                                if (this.package_kind_90_alt_1(builder, opp)) {
                                                    altMarker1.drop();
                                                } else {
                                                    altMarker1.rollbackTo();
                                                    return false;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean param_sep_91(PsiBuilder builder) {
        OPP opp;
        opp = null;
        if (!(this.ws_258(builder))) {
            return false;
        }
        if ((builder.getTokenType()) == RakuTokenTypes.PARAMETER_SEPARATOR) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean param_term_92_quant_1(PsiBuilder builder, OPP opp) {
        if (!(this.defterm_38(builder))) {
            return false;
        }
        return true;
    }

    private boolean param_term_92(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker quantMarker1;
        quantMarker1 = builder.mark();
        if (this.param_term_92_quant_1(builder, opp)) {
            quantMarker1.drop();
        } else {
            quantMarker1.rollbackTo();
        }
        return true;
    }

    private boolean param_var_93_quant_1(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.PARAM_ARRAY_SHAPE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker marker2;
        marker2 = builder.mark();
        if (!(this.postcircumfix_115(builder))) {
            return false;
        }
        marker2.done(RakuElementTypes.ARRAY_SHAPE);
        return true;
    }

    private boolean param_var_93_alt_2(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        if (!(this.variable_253(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker3;
        quantMarker3 = builder.mark();
        if (this.param_var_93_quant_1(builder, opp)) {
            quantMarker3.drop();
        } else {
            quantMarker3.rollbackTo();
        }
        marker1.done(RakuElementTypes.PARAMETER_VARIABLE);
        return true;
    }

    private boolean param_var_93_quant_3(PsiBuilder builder, OPP opp) {
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.PARENTHESES_CLOSE) && (tt2.equals(")"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean param_var_93_alt_4(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker5;
        marker5 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.PARENTHESES_OPEN) && (tt1.equals("("))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.signature_174(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker6;
        quantMarker6 = builder.mark();
        if (this.param_var_93_quant_3(builder, opp)) {
            quantMarker6.drop();
        } else {
            quantMarker6.rollbackTo();
        }
        marker5.done(RakuElementTypes.SIGNATURE);
        return true;
    }

    private boolean param_var_93_quant_5(PsiBuilder builder, OPP opp) {
        String tt4;
        tt4 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.SIGNATURE_BRACKET_CLOSE) && (tt4.equals("]"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean param_var_93_alt_6(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker8;
        marker8 = builder.mark();
        String tt3;
        tt3 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.SIGNATURE_BRACKET_OPEN) && (tt3.equals("["))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.signature_174(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker9;
        quantMarker9 = builder.mark();
        if (this.param_var_93_quant_5(builder, opp)) {
            quantMarker9.drop();
        } else {
            quantMarker9.rollbackTo();
        }
        marker8.done(RakuElementTypes.SIGNATURE);
        return true;
    }

    private boolean param_var_93(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker10;
        altMarker10 = builder.mark();
        if (this.param_var_93_alt_6(builder, opp)) {
            altMarker10.drop();
        } else {
            altMarker10.rollbackTo();
            PsiBuilder.Marker altMarker7;
            altMarker7 = builder.mark();
            if (this.param_var_93_alt_4(builder, opp)) {
                altMarker7.drop();
            } else {
                altMarker7.rollbackTo();
                PsiBuilder.Marker altMarker4;
                altMarker4 = builder.mark();
                if (this.param_var_93_alt_2(builder, opp)) {
                    altMarker4.drop();
                } else {
                    altMarker4.rollbackTo();
                    return false;
                }
            }
        }
        return true;
    }

    private boolean parameter_94_alt_1(PsiBuilder builder, OPP opp) {
        if (!(this.named_param_80(builder))) {
            return false;
        }
        return true;
    }

    private boolean parameter_94_alt_2(PsiBuilder builder, OPP opp) {
        if (!(this.param_var_93(builder))) {
            return false;
        }
        return true;
    }

    private boolean parameter_94_quant_3(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.PARAMETER_QUANTIFIER) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean parameter_94_alt_4(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker3;
        altMarker3 = builder.mark();
        if (this.parameter_94_alt_2(builder, opp)) {
            altMarker3.drop();
        } else {
            altMarker3.rollbackTo();
            PsiBuilder.Marker altMarker2;
            altMarker2 = builder.mark();
            if (this.parameter_94_alt_1(builder, opp)) {
                altMarker2.drop();
            } else {
                altMarker2.rollbackTo();
                return false;
            }
        }
        PsiBuilder.Marker quantMarker4;
        quantMarker4 = builder.mark();
        if (this.parameter_94_quant_3(builder, opp)) {
            quantMarker4.drop();
        } else {
            quantMarker4.rollbackTo();
        }
        return true;
    }

    private boolean parameter_94_alt_5(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.PARAMETER_INCOMPLETE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean parameter_94_alt_6(PsiBuilder builder, OPP opp) {
        if (!(this.param_var_93(builder))) {
            return false;
        }
        return true;
    }

    private boolean parameter_94_alt_7(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.PARAMETER_QUANTIFIER) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker altMarker7;
        altMarker7 = builder.mark();
        if (this.parameter_94_alt_6(builder, opp)) {
            altMarker7.drop();
        } else {
            altMarker7.rollbackTo();
            PsiBuilder.Marker altMarker6;
            altMarker6 = builder.mark();
            if (this.parameter_94_alt_5(builder, opp)) {
                altMarker6.drop();
            } else {
                altMarker6.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean parameter_94_alt_8(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.IS_PARAM_TERM_QUANT) {
            builder.advanceLexer();
        } else {
            return false;
        }
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.PARAMETER_QUANTIFIER) && (tt1.equals("+"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean parameter_94_alt_9(PsiBuilder builder, OPP opp) {
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.PARAMETER_QUANTIFIER) && (tt2.equals("|"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean parameter_94_alt_10(PsiBuilder builder, OPP opp) {
        String tt3;
        tt3 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.PARAMETER_QUANTIFIER) && (tt3.equals("\\"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean parameter_94_alt_11(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker11;
        altMarker11 = builder.mark();
        if (this.parameter_94_alt_10(builder, opp)) {
            altMarker11.drop();
        } else {
            altMarker11.rollbackTo();
            PsiBuilder.Marker altMarker10;
            altMarker10 = builder.mark();
            if (this.parameter_94_alt_9(builder, opp)) {
                altMarker10.drop();
            } else {
                altMarker10.rollbackTo();
                PsiBuilder.Marker altMarker9;
                altMarker9 = builder.mark();
                if (this.parameter_94_alt_8(builder, opp)) {
                    altMarker9.drop();
                } else {
                    altMarker9.rollbackTo();
                    return false;
                }
            }
        }
        if (!(this.param_term_92(builder))) {
            return false;
        }
        return true;
    }

    private boolean parameter_94_quant_12(PsiBuilder builder, OPP opp) {
        if (!(this.type_constraint_247(builder))) {
            return false;
        }
        return true;
    }

    private boolean parameter_94_alt_13(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.PARAMETER_ANON) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean parameter_94_alt_14(PsiBuilder builder, OPP opp) {
        if (!(this.named_param_80(builder))) {
            return false;
        }
        return true;
    }

    private boolean parameter_94_alt_15(PsiBuilder builder, OPP opp) {
        if (!(this.param_var_93(builder))) {
            return false;
        }
        return true;
    }

    private boolean parameter_94_quant_16(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.PARAMETER_QUANTIFIER) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean parameter_94_alt_17(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker17;
        altMarker17 = builder.mark();
        if (this.parameter_94_alt_15(builder, opp)) {
            altMarker17.drop();
        } else {
            altMarker17.rollbackTo();
            PsiBuilder.Marker altMarker16;
            altMarker16 = builder.mark();
            if (this.parameter_94_alt_14(builder, opp)) {
                altMarker16.drop();
            } else {
                altMarker16.rollbackTo();
                return false;
            }
        }
        PsiBuilder.Marker quantMarker18;
        quantMarker18 = builder.mark();
        if (this.parameter_94_quant_16(builder, opp)) {
            quantMarker18.drop();
        } else {
            quantMarker18.rollbackTo();
        }
        return true;
    }

    private boolean parameter_94_alt_18(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.PARAMETER_INCOMPLETE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean parameter_94_alt_19(PsiBuilder builder, OPP opp) {
        if (!(this.param_var_93(builder))) {
            return false;
        }
        return true;
    }

    private boolean parameter_94_alt_20(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.PARAMETER_QUANTIFIER) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker altMarker21;
        altMarker21 = builder.mark();
        if (this.parameter_94_alt_19(builder, opp)) {
            altMarker21.drop();
        } else {
            altMarker21.rollbackTo();
            PsiBuilder.Marker altMarker20;
            altMarker20 = builder.mark();
            if (this.parameter_94_alt_18(builder, opp)) {
                altMarker20.drop();
            } else {
                altMarker20.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean parameter_94_alt_21(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.IS_PARAM_TERM_QUANT) {
            builder.advanceLexer();
        } else {
            return false;
        }
        String tt4;
        tt4 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.PARAMETER_QUANTIFIER) && (tt4.equals("+"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean parameter_94_alt_22(PsiBuilder builder, OPP opp) {
        String tt5;
        tt5 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.PARAMETER_QUANTIFIER) && (tt5.equals("|"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean parameter_94_alt_23(PsiBuilder builder, OPP opp) {
        String tt6;
        tt6 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.TERM_DECLARATION_BACKSLASH) && (tt6.equals("\\"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean parameter_94_alt_24(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker25;
        altMarker25 = builder.mark();
        if (this.parameter_94_alt_23(builder, opp)) {
            altMarker25.drop();
        } else {
            altMarker25.rollbackTo();
            PsiBuilder.Marker altMarker24;
            altMarker24 = builder.mark();
            if (this.parameter_94_alt_22(builder, opp)) {
                altMarker24.drop();
            } else {
                altMarker24.rollbackTo();
                PsiBuilder.Marker altMarker23;
                altMarker23 = builder.mark();
                if (this.parameter_94_alt_21(builder, opp)) {
                    altMarker23.drop();
                } else {
                    altMarker23.rollbackTo();
                    return false;
                }
            }
        }
        if (!(this.param_term_92(builder))) {
            return false;
        }
        return true;
    }

    private boolean parameter_94_alt_25(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker quantMarker13;
        quantMarker13 = builder.mark();
        if (this.parameter_94_quant_12(builder, opp)) {
            quantMarker13.drop();
        } else {
            quantMarker13.rollbackTo();
            return false;
        }
        while (true) {
            PsiBuilder.Marker quantMarker14;
            quantMarker14 = builder.mark();
            if (this.parameter_94_quant_12(builder, opp)) {
                quantMarker14.drop();
            } else {
                quantMarker14.rollbackTo();
                break;
            }
        }
        PsiBuilder.Marker altMarker26;
        altMarker26 = builder.mark();
        if (this.parameter_94_alt_24(builder, opp)) {
            altMarker26.drop();
        } else {
            altMarker26.rollbackTo();
            PsiBuilder.Marker altMarker22;
            altMarker22 = builder.mark();
            if (this.parameter_94_alt_20(builder, opp)) {
                altMarker22.drop();
            } else {
                altMarker22.rollbackTo();
                PsiBuilder.Marker altMarker19;
                altMarker19 = builder.mark();
                if (this.parameter_94_alt_17(builder, opp)) {
                    altMarker19.drop();
                } else {
                    altMarker19.rollbackTo();
                    PsiBuilder.Marker altMarker15;
                    altMarker15 = builder.mark();
                    if (this.parameter_94_alt_13(builder, opp)) {
                        altMarker15.drop();
                    } else {
                        altMarker15.rollbackTo();
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean parameter_94_quant_26(PsiBuilder builder, OPP opp) {
        if (!(this.trait_242(builder))) {
            return false;
        }
        return true;
    }

    private boolean parameter_94_quant_27(PsiBuilder builder, OPP opp) {
        if (!(this.post_constraint_114(builder))) {
            return false;
        }
        return true;
    }

    private boolean parameter_94_quant_28(PsiBuilder builder, OPP opp) {
        if (!(this.default_value_37(builder))) {
            return false;
        }
        return true;
    }

    private boolean parameter_94(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        PsiBuilder.Marker altMarker27;
        altMarker27 = builder.mark();
        if (this.parameter_94_alt_25(builder, opp)) {
            altMarker27.drop();
        } else {
            altMarker27.rollbackTo();
            PsiBuilder.Marker altMarker12;
            altMarker12 = builder.mark();
            if (this.parameter_94_alt_11(builder, opp)) {
                altMarker12.drop();
            } else {
                altMarker12.rollbackTo();
                PsiBuilder.Marker altMarker8;
                altMarker8 = builder.mark();
                if (this.parameter_94_alt_7(builder, opp)) {
                    altMarker8.drop();
                } else {
                    altMarker8.rollbackTo();
                    PsiBuilder.Marker altMarker5;
                    altMarker5 = builder.mark();
                    if (this.parameter_94_alt_4(builder, opp)) {
                        altMarker5.drop();
                    } else {
                        altMarker5.rollbackTo();
                        return false;
                    }
                }
            }
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        while (true) {
            PsiBuilder.Marker quantMarker28;
            quantMarker28 = builder.mark();
            if (this.parameter_94_quant_26(builder, opp)) {
                quantMarker28.drop();
            } else {
                quantMarker28.rollbackTo();
                break;
            }
        }
        while (true) {
            PsiBuilder.Marker quantMarker29;
            quantMarker29 = builder.mark();
            if (this.parameter_94_quant_27(builder, opp)) {
                quantMarker29.drop();
            } else {
                quantMarker29.rollbackTo();
                break;
            }
        }
        PsiBuilder.Marker quantMarker30;
        quantMarker30 = builder.mark();
        if (this.parameter_94_quant_28(builder, opp)) {
            quantMarker30.drop();
        } else {
            quantMarker30.rollbackTo();
        }
        marker1.done(RakuElementTypes.PARAMETER);
        return true;
    }

    private boolean pblock_95_alt_1(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.MISSING_BLOCK) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean pblock_95_alt_2(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker2;
        marker2 = builder.mark();
        if (!(this.blockoid_16(builder))) {
            return false;
        }
        marker2.done(RakuElementTypes.BLOCK);
        return true;
    }

    private boolean pblock_95_quant_3(PsiBuilder builder, OPP opp) {
        if (!(this.blockoid_16(builder))) {
            return false;
        }
        return true;
    }

    private boolean pblock_95_alt_4(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker4;
        marker4 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.LAMBDA) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker marker5;
        marker5 = builder.mark();
        if (!(this.signature_174(builder))) {
            return false;
        }
        marker5.done(RakuElementTypes.SIGNATURE);
        PsiBuilder.Marker quantMarker6;
        quantMarker6 = builder.mark();
        if (this.pblock_95_quant_3(builder, opp)) {
            quantMarker6.drop();
        } else {
            quantMarker6.rollbackTo();
        }
        marker4.done(RakuElementTypes.POINTY_BLOCK);
        return true;
    }

    private boolean pblock_95(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker7;
        altMarker7 = builder.mark();
        if (this.pblock_95_alt_4(builder, opp)) {
            altMarker7.drop();
        } else {
            altMarker7.rollbackTo();
            PsiBuilder.Marker altMarker3;
            altMarker3 = builder.mark();
            if (this.pblock_95_alt_2(builder, opp)) {
                altMarker3.drop();
            } else {
                altMarker3.rollbackTo();
                PsiBuilder.Marker altMarker1;
                altMarker1 = builder.mark();
                if (this.pblock_95_alt_1(builder, opp)) {
                    altMarker1.drop();
                } else {
                    altMarker1.rollbackTo();
                    return false;
                }
            }
        }
        return true;
    }

    private boolean phaser_name_96_alt_1(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean phaser_name_96_alt_2(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean phaser_name_96_alt_3(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean phaser_name_96_alt_4(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean phaser_name_96_alt_5(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean phaser_name_96_alt_6(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean phaser_name_96_alt_7(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean phaser_name_96_alt_8(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean phaser_name_96_alt_9(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean phaser_name_96_alt_10(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean phaser_name_96_alt_11(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean phaser_name_96_alt_12(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean phaser_name_96_alt_13(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean phaser_name_96_alt_14(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean phaser_name_96_alt_15(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean phaser_name_96_alt_16(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean phaser_name_96(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker16;
        altMarker16 = builder.mark();
        if (this.phaser_name_96_alt_16(builder, opp)) {
            altMarker16.drop();
        } else {
            altMarker16.rollbackTo();
            PsiBuilder.Marker altMarker15;
            altMarker15 = builder.mark();
            if (this.phaser_name_96_alt_15(builder, opp)) {
                altMarker15.drop();
            } else {
                altMarker15.rollbackTo();
                PsiBuilder.Marker altMarker14;
                altMarker14 = builder.mark();
                if (this.phaser_name_96_alt_14(builder, opp)) {
                    altMarker14.drop();
                } else {
                    altMarker14.rollbackTo();
                    PsiBuilder.Marker altMarker13;
                    altMarker13 = builder.mark();
                    if (this.phaser_name_96_alt_13(builder, opp)) {
                        altMarker13.drop();
                    } else {
                        altMarker13.rollbackTo();
                        PsiBuilder.Marker altMarker12;
                        altMarker12 = builder.mark();
                        if (this.phaser_name_96_alt_12(builder, opp)) {
                            altMarker12.drop();
                        } else {
                            altMarker12.rollbackTo();
                            PsiBuilder.Marker altMarker11;
                            altMarker11 = builder.mark();
                            if (this.phaser_name_96_alt_11(builder, opp)) {
                                altMarker11.drop();
                            } else {
                                altMarker11.rollbackTo();
                                PsiBuilder.Marker altMarker10;
                                altMarker10 = builder.mark();
                                if (this.phaser_name_96_alt_10(builder, opp)) {
                                    altMarker10.drop();
                                } else {
                                    altMarker10.rollbackTo();
                                    PsiBuilder.Marker altMarker9;
                                    altMarker9 = builder.mark();
                                    if (this.phaser_name_96_alt_9(builder, opp)) {
                                        altMarker9.drop();
                                    } else {
                                        altMarker9.rollbackTo();
                                        PsiBuilder.Marker altMarker8;
                                        altMarker8 = builder.mark();
                                        if (this.phaser_name_96_alt_8(builder, opp)) {
                                            altMarker8.drop();
                                        } else {
                                            altMarker8.rollbackTo();
                                            PsiBuilder.Marker altMarker7;
                                            altMarker7 = builder.mark();
                                            if (this.phaser_name_96_alt_7(builder, opp)) {
                                                altMarker7.drop();
                                            } else {
                                                altMarker7.rollbackTo();
                                                PsiBuilder.Marker altMarker6;
                                                altMarker6 = builder.mark();
                                                if (this.phaser_name_96_alt_6(builder, opp)) {
                                                    altMarker6.drop();
                                                } else {
                                                    altMarker6.rollbackTo();
                                                    PsiBuilder.Marker altMarker5;
                                                    altMarker5 = builder.mark();
                                                    if (this.phaser_name_96_alt_5(builder, opp)) {
                                                        altMarker5.drop();
                                                    } else {
                                                        altMarker5.rollbackTo();
                                                        PsiBuilder.Marker altMarker4;
                                                        altMarker4 = builder.mark();
                                                        if (this.phaser_name_96_alt_4(builder, opp)) {
                                                            altMarker4.drop();
                                                        } else {
                                                            altMarker4.rollbackTo();
                                                            PsiBuilder.Marker altMarker3;
                                                            altMarker3 = builder.mark();
                                                            if (this.phaser_name_96_alt_3(builder, opp)) {
                                                                altMarker3.drop();
                                                            } else {
                                                                altMarker3.rollbackTo();
                                                                PsiBuilder.Marker altMarker2;
                                                                altMarker2 = builder.mark();
                                                                if (this.phaser_name_96_alt_2(builder, opp)) {
                                                                    altMarker2.drop();
                                                                } else {
                                                                    altMarker2.rollbackTo();
                                                                    PsiBuilder.Marker altMarker1;
                                                                    altMarker1 = builder.mark();
                                                                    if (this.phaser_name_96_alt_1(builder, opp)) {
                                                                        altMarker1.drop();
                                                                    } else {
                                                                        altMarker1.rollbackTo();
                                                                        return false;
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean plaincomment_97(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.COMMENT_STARTER) && (tt1.equals("#"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if ((builder.getTokenType()) == RakuTokenTypes.COMMENT) {
            builder.advanceLexer();
        } else {
            return false;
        }
        marker1.done(RakuElementTypes.COMMENT);
        return true;
    }

    private boolean pod_block_98_alt_1(PsiBuilder builder, OPP opp) {
        if (!(this.pod_block_abbreviated_99(builder))) {
            return false;
        }
        return true;
    }

    private boolean pod_block_98_alt_2(PsiBuilder builder, OPP opp) {
        if (!(this.pod_block_paragraph_103(builder))) {
            return false;
        }
        return true;
    }

    private boolean pod_block_98_alt_3(PsiBuilder builder, OPP opp) {
        if (!(this.pod_block_delimited_101(builder))) {
            return false;
        }
        return true;
    }

    private boolean pod_block_98_alt_4(PsiBuilder builder, OPP opp) {
        if (!(this.pod_block_finish_102(builder))) {
            return false;
        }
        return true;
    }

    private boolean pod_block_98(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker4;
        altMarker4 = builder.mark();
        if (this.pod_block_98_alt_4(builder, opp)) {
            altMarker4.drop();
        } else {
            altMarker4.rollbackTo();
            PsiBuilder.Marker altMarker3;
            altMarker3 = builder.mark();
            if (this.pod_block_98_alt_3(builder, opp)) {
                altMarker3.drop();
            } else {
                altMarker3.rollbackTo();
                PsiBuilder.Marker altMarker2;
                altMarker2 = builder.mark();
                if (this.pod_block_98_alt_2(builder, opp)) {
                    altMarker2.drop();
                } else {
                    altMarker2.rollbackTo();
                    PsiBuilder.Marker altMarker1;
                    altMarker1 = builder.mark();
                    if (this.pod_block_98_alt_1(builder, opp)) {
                        altMarker1.drop();
                    } else {
                        altMarker1.rollbackTo();
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean pod_block_abbreviated_99_quant_1(PsiBuilder builder, OPP opp) {
        if (!(this.pod_newline_109(builder))) {
            return false;
        }
        return true;
    }

    private boolean pod_block_abbreviated_99_quant_2(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.POD_WHITESPACE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.pod_para_content_110(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker2;
        quantMarker2 = builder.mark();
        if (this.pod_block_abbreviated_99_quant_1(builder, opp)) {
            quantMarker2.drop();
        } else {
            quantMarker2.rollbackTo();
        }
        return true;
    }

    private boolean pod_block_abbreviated_99(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.POD_REMOVED_WHITESPACE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.POD_DIRECTIVE) && (tt1.equals("="))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.pod_code_check_104(builder))) {
            return false;
        }
        if ((builder.getTokenType()) == RakuTokenTypes.POD_TYPENAME) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker quantMarker3;
        quantMarker3 = builder.mark();
        if (this.pod_block_abbreviated_99_quant_2(builder, opp)) {
            quantMarker3.drop();
        } else {
            quantMarker3.rollbackTo();
        }
        marker1.done(RakuElementTypes.POD_BLOCK_ABBREVIATED);
        return true;
    }

    private boolean pod_block_content_100_alt_1(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean pod_block_content_100_alt_2(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean pod_block_content_100_alt_3(PsiBuilder builder, OPP opp) {
        if (!(this.pod_newline_109(builder))) {
            return false;
        }
        return true;
    }

    private boolean pod_block_content_100_alt_4(PsiBuilder builder, OPP opp) {
        if (!(this.pod_formatting_code_108(builder))) {
            return false;
        }
        return true;
    }

    private boolean pod_block_content_100_alt_5(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.POD_TEXT) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean pod_block_content_100_quant_6(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker5;
        altMarker5 = builder.mark();
        if (this.pod_block_content_100_alt_5(builder, opp)) {
            altMarker5.drop();
        } else {
            altMarker5.rollbackTo();
            PsiBuilder.Marker altMarker4;
            altMarker4 = builder.mark();
            if (this.pod_block_content_100_alt_4(builder, opp)) {
                altMarker4.drop();
            } else {
                altMarker4.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean pod_block_content_100_alt_7(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker quantMarker6;
        quantMarker6 = builder.mark();
        if (this.pod_block_content_100_quant_6(builder, opp)) {
            quantMarker6.drop();
        } else {
            quantMarker6.rollbackTo();
            return false;
        }
        while (true) {
            PsiBuilder.Marker quantMarker7;
            quantMarker7 = builder.mark();
            if (this.pod_block_content_100_quant_6(builder, opp)) {
                quantMarker7.drop();
            } else {
                quantMarker7.rollbackTo();
                break;
            }
        }
        return true;
    }

    private boolean pod_block_content_100_alt_8(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean pod_block_content_100_alt_9(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean pod_block_content_100_alt_10(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker10;
        altMarker10 = builder.mark();
        if (this.pod_block_content_100_alt_9(builder, opp)) {
            altMarker10.drop();
        } else {
            altMarker10.rollbackTo();
            PsiBuilder.Marker altMarker9;
            altMarker9 = builder.mark();
            if (this.pod_block_content_100_alt_8(builder, opp)) {
                altMarker9.drop();
            } else {
                altMarker9.rollbackTo();
                return false;
            }
        }
        if ((builder.getTokenType()) == RakuTokenTypes.POD_CODE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean pod_block_content_100_quant_11(PsiBuilder builder, OPP opp) {
        if (!(this.pod_newline_109(builder))) {
            return false;
        }
        return true;
    }

    private boolean pod_block_content_100_alt_12(PsiBuilder builder, OPP opp) {
        if (!(this.pod_removed_whitespace_111(builder))) {
            return false;
        }
        PsiBuilder.Marker altMarker11;
        altMarker11 = builder.mark();
        if (this.pod_block_content_100_alt_10(builder, opp)) {
            altMarker11.drop();
        } else {
            altMarker11.rollbackTo();
            PsiBuilder.Marker altMarker8;
            altMarker8 = builder.mark();
            if (this.pod_block_content_100_alt_7(builder, opp)) {
                altMarker8.drop();
            } else {
                altMarker8.rollbackTo();
                return false;
            }
        }
        PsiBuilder.Marker quantMarker12;
        quantMarker12 = builder.mark();
        if (this.pod_block_content_100_quant_11(builder, opp)) {
            quantMarker12.drop();
        } else {
            quantMarker12.rollbackTo();
        }
        return true;
    }

    private boolean pod_block_content_100_alt_13(PsiBuilder builder, OPP opp) {
        if (!(this.pod_block_98(builder))) {
            return false;
        }
        return true;
    }

    private boolean pod_block_content_100_quant_14(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker2;
        altMarker2 = builder.mark();
        if (this.pod_block_content_100_alt_2(builder, opp)) {
            altMarker2.drop();
        } else {
            altMarker2.rollbackTo();
            PsiBuilder.Marker altMarker1;
            altMarker1 = builder.mark();
            if (this.pod_block_content_100_alt_1(builder, opp)) {
                altMarker1.drop();
            } else {
                altMarker1.rollbackTo();
                return false;
            }
        }
        if ((builder.getTokenType()) == RakuTokenTypes.POD_HAVE_CONTENT) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker altMarker14;
        altMarker14 = builder.mark();
        if (this.pod_block_content_100_alt_13(builder, opp)) {
            altMarker14.drop();
        } else {
            altMarker14.rollbackTo();
            PsiBuilder.Marker altMarker13;
            altMarker13 = builder.mark();
            if (this.pod_block_content_100_alt_12(builder, opp)) {
                altMarker13.drop();
            } else {
                altMarker13.rollbackTo();
                PsiBuilder.Marker altMarker3;
                altMarker3 = builder.mark();
                if (this.pod_block_content_100_alt_3(builder, opp)) {
                    altMarker3.drop();
                } else {
                    altMarker3.rollbackTo();
                    return false;
                }
            }
        }
        return true;
    }

    private boolean pod_block_content_100(PsiBuilder builder) {
        OPP opp;
        opp = null;
        while (true) {
            PsiBuilder.Marker quantMarker15;
            quantMarker15 = builder.mark();
            if (this.pod_block_content_100_quant_14(builder, opp)) {
                quantMarker15.drop();
            } else {
                quantMarker15.rollbackTo();
                break;
            }
        }
        return true;
    }

    private boolean pod_block_delimited_101_quant_1(PsiBuilder builder, OPP opp) {
        if (!(this.pod_configuration_105(builder))) {
            return false;
        }
        return true;
    }

    private boolean pod_block_delimited_101_quant_2(PsiBuilder builder, OPP opp) {
        if (!(this.pod_newline_109(builder))) {
            return false;
        }
        return true;
    }

    private boolean pod_block_delimited_101_quant_3(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.POD_WHITESPACE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if ((builder.getTokenType()) == RakuTokenTypes.POD_TYPENAME) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker quantMarker3;
        quantMarker3 = builder.mark();
        if (this.pod_block_delimited_101_quant_2(builder, opp)) {
            quantMarker3.drop();
        } else {
            quantMarker3.rollbackTo();
        }
        return true;
    }

    private boolean pod_block_delimited_101_quant_4(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.POD_WHITESPACE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.POD_DIRECTIVE) && (tt2.equals("=end"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker quantMarker4;
        quantMarker4 = builder.mark();
        if (this.pod_block_delimited_101_quant_3(builder, opp)) {
            quantMarker4.drop();
        } else {
            quantMarker4.rollbackTo();
        }
        return true;
    }

    private boolean pod_block_delimited_101_quant_5(PsiBuilder builder, OPP opp) {
        if (!(this.pod_block_content_100(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker5;
        quantMarker5 = builder.mark();
        if (this.pod_block_delimited_101_quant_4(builder, opp)) {
            quantMarker5.drop();
        } else {
            quantMarker5.rollbackTo();
        }
        return true;
    }

    private boolean pod_block_delimited_101_quant_6(PsiBuilder builder, OPP opp) {
        if (!(this.pod_newline_109(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker6;
        quantMarker6 = builder.mark();
        if (this.pod_block_delimited_101_quant_5(builder, opp)) {
            quantMarker6.drop();
        } else {
            quantMarker6.rollbackTo();
        }
        return true;
    }

    private boolean pod_block_delimited_101_quant_7(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.POD_WHITESPACE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.pod_code_check_104(builder))) {
            return false;
        }
        if ((builder.getTokenType()) == RakuTokenTypes.POD_TYPENAME) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker quantMarker2;
        quantMarker2 = builder.mark();
        if (this.pod_block_delimited_101_quant_1(builder, opp)) {
            quantMarker2.drop();
        } else {
            quantMarker2.rollbackTo();
        }
        PsiBuilder.Marker quantMarker7;
        quantMarker7 = builder.mark();
        if (this.pod_block_delimited_101_quant_6(builder, opp)) {
            quantMarker7.drop();
        } else {
            quantMarker7.rollbackTo();
        }
        return true;
    }

    private boolean pod_block_delimited_101(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.POD_REMOVED_WHITESPACE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.POD_DIRECTIVE) && (tt1.equals("=begin"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker quantMarker8;
        quantMarker8 = builder.mark();
        if (this.pod_block_delimited_101_quant_7(builder, opp)) {
            quantMarker8.drop();
        } else {
            quantMarker8.rollbackTo();
        }
        marker1.done(RakuElementTypes.POD_BLOCK_DELIMITED);
        return true;
    }

    private boolean pod_block_finish_102_alt_1(PsiBuilder builder, OPP opp) {
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.POD_DIRECTIVE) && (tt1.equals("=finish"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean pod_block_finish_102_alt_2(PsiBuilder builder, OPP opp) {
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.POD_DIRECTIVE) && (tt2.equals("=for"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if ((builder.getTokenType()) == RakuTokenTypes.POD_WHITESPACE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        String tt3;
        tt3 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.POD_TYPENAME) && (tt3.equals("finish"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean pod_block_finish_102_alt_3(PsiBuilder builder, OPP opp) {
        String tt4;
        tt4 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.POD_DIRECTIVE) && (tt4.equals("=begin"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if ((builder.getTokenType()) == RakuTokenTypes.POD_WHITESPACE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        String tt5;
        tt5 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.POD_TYPENAME) && (tt5.equals("finish"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean pod_block_finish_102_quant_4(PsiBuilder builder, OPP opp) {
        if (!(this.pod_newline_109(builder))) {
            return false;
        }
        return true;
    }

    private boolean pod_block_finish_102(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.POD_WHITESPACE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker altMarker4;
        altMarker4 = builder.mark();
        if (this.pod_block_finish_102_alt_3(builder, opp)) {
            altMarker4.drop();
        } else {
            altMarker4.rollbackTo();
            PsiBuilder.Marker altMarker3;
            altMarker3 = builder.mark();
            if (this.pod_block_finish_102_alt_2(builder, opp)) {
                altMarker3.drop();
            } else {
                altMarker3.rollbackTo();
                PsiBuilder.Marker altMarker2;
                altMarker2 = builder.mark();
                if (this.pod_block_finish_102_alt_1(builder, opp)) {
                    altMarker2.drop();
                } else {
                    altMarker2.rollbackTo();
                    return false;
                }
            }
        }
        PsiBuilder.Marker quantMarker5;
        quantMarker5 = builder.mark();
        if (this.pod_block_finish_102_quant_4(builder, opp)) {
            quantMarker5.drop();
        } else {
            quantMarker5.rollbackTo();
        }
        if ((builder.getTokenType()) == RakuTokenTypes.POD_FINISH_TEXT) {
            builder.advanceLexer();
        } else {
            return false;
        }
        marker1.done(RakuElementTypes.POD_BLOCK_FINISH);
        return true;
    }

    private boolean pod_block_paragraph_103_quant_1(PsiBuilder builder, OPP opp) {
        if (!(this.pod_configuration_105(builder))) {
            return false;
        }
        return true;
    }

    private boolean pod_block_paragraph_103_quant_2(PsiBuilder builder, OPP opp) {
        if (!(this.pod_newline_109(builder))) {
            return false;
        }
        return true;
    }

    private boolean pod_block_paragraph_103_quant_3(PsiBuilder builder, OPP opp) {
        if (!(this.pod_newline_109(builder))) {
            return false;
        }
        if (!(this.pod_para_content_110(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker3;
        quantMarker3 = builder.mark();
        if (this.pod_block_paragraph_103_quant_2(builder, opp)) {
            quantMarker3.drop();
        } else {
            quantMarker3.rollbackTo();
        }
        return true;
    }

    private boolean pod_block_paragraph_103_quant_4(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.POD_WHITESPACE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.pod_code_check_104(builder))) {
            return false;
        }
        if ((builder.getTokenType()) == RakuTokenTypes.POD_TYPENAME) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker quantMarker2;
        quantMarker2 = builder.mark();
        if (this.pod_block_paragraph_103_quant_1(builder, opp)) {
            quantMarker2.drop();
        } else {
            quantMarker2.rollbackTo();
        }
        PsiBuilder.Marker quantMarker4;
        quantMarker4 = builder.mark();
        if (this.pod_block_paragraph_103_quant_3(builder, opp)) {
            quantMarker4.drop();
        } else {
            quantMarker4.rollbackTo();
        }
        return true;
    }

    private boolean pod_block_paragraph_103(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.POD_REMOVED_WHITESPACE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.POD_DIRECTIVE) && (tt1.equals("=for"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker quantMarker5;
        quantMarker5 = builder.mark();
        if (this.pod_block_paragraph_103_quant_4(builder, opp)) {
            quantMarker5.drop();
        } else {
            quantMarker5.rollbackTo();
        }
        marker1.done(RakuElementTypes.POD_BLOCK_PARAGRAPH);
        return true;
    }

    private boolean pod_code_check_104_alt_1(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean pod_code_check_104_alt_2(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean pod_code_check_104_alt_3(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean pod_code_check_104_alt_4(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean pod_code_check_104(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker4;
        altMarker4 = builder.mark();
        if (this.pod_code_check_104_alt_4(builder, opp)) {
            altMarker4.drop();
        } else {
            altMarker4.rollbackTo();
            PsiBuilder.Marker altMarker3;
            altMarker3 = builder.mark();
            if (this.pod_code_check_104_alt_3(builder, opp)) {
                altMarker3.drop();
            } else {
                altMarker3.rollbackTo();
                PsiBuilder.Marker altMarker2;
                altMarker2 = builder.mark();
                if (this.pod_code_check_104_alt_2(builder, opp)) {
                    altMarker2.drop();
                } else {
                    altMarker2.rollbackTo();
                    PsiBuilder.Marker altMarker1;
                    altMarker1 = builder.mark();
                    if (this.pod_code_check_104_alt_1(builder, opp)) {
                        altMarker1.drop();
                    } else {
                        altMarker1.rollbackTo();
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean pod_configuration_105(PsiBuilder builder) {
        OPP opp;
        opp = null;
        if ((builder.getTokenType()) == RakuTokenTypes.POD_WHITESPACE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.POD_CONFIGURATION) {
            builder.advanceLexer();
        } else {
            return false;
        }
        marker1.done(RakuElementTypes.POD_CONFIGURATION);
        return true;
    }

    private boolean pod_content_toplevel_106(PsiBuilder builder) {
        OPP opp;
        opp = null;
        if (!(this.pod_block_98(builder))) {
            return false;
        }
        return true;
    }

    private boolean pod_formatted_text_107_alt_1(PsiBuilder builder, OPP opp) {
        if (!(this.pod_newline_109(builder))) {
            return false;
        }
        return true;
    }

    private boolean pod_formatted_text_107_alt_2(PsiBuilder builder, OPP opp) {
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.POD_FORMAT_SEPARATOR) && (tt1.equals("|"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean pod_formatted_text_107_alt_3(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.POD_TEXT) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean pod_formatted_text_107_alt_4(PsiBuilder builder, OPP opp) {
        if (!(this.pod_formatting_code_108(builder))) {
            return false;
        }
        return true;
    }

    private boolean pod_formatted_text_107_quant_5(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker5;
        altMarker5 = builder.mark();
        if (this.pod_formatted_text_107_alt_4(builder, opp)) {
            altMarker5.drop();
        } else {
            altMarker5.rollbackTo();
            PsiBuilder.Marker altMarker4;
            altMarker4 = builder.mark();
            if (this.pod_formatted_text_107_alt_3(builder, opp)) {
                altMarker4.drop();
            } else {
                altMarker4.rollbackTo();
                PsiBuilder.Marker altMarker3;
                altMarker3 = builder.mark();
                if (this.pod_formatted_text_107_alt_2(builder, opp)) {
                    altMarker3.drop();
                } else {
                    altMarker3.rollbackTo();
                    PsiBuilder.Marker altMarker2;
                    altMarker2 = builder.mark();
                    if (this.pod_formatted_text_107_alt_1(builder, opp)) {
                        altMarker2.drop();
                    } else {
                        altMarker2.rollbackTo();
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean pod_formatted_text_107(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        while (true) {
            PsiBuilder.Marker quantMarker6;
            quantMarker6 = builder.mark();
            if (this.pod_formatted_text_107_quant_5(builder, opp)) {
                quantMarker6.drop();
            } else {
                quantMarker6.rollbackTo();
                break;
            }
        }
        marker1.done(RakuElementTypes.POD_TEXT);
        return true;
    }

    private boolean pod_formatting_code_108_quant_1(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean pod_formatting_code_108_quant_2(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.POD_FORMAT_STOPPER) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean pod_formatting_code_108(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker quantMarker1;
        quantMarker1 = builder.mark();
        if (this.pod_formatting_code_108_quant_1(builder, opp)) {
            quantMarker1.drop();
        } else {
            quantMarker1.rollbackTo();
        }
        PsiBuilder.Marker marker2;
        marker2 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.FORMAT_CODE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if ((builder.getTokenType()) == RakuTokenTypes.POD_FORMAT_STARTER) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.pod_formatted_text_107(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker3;
        quantMarker3 = builder.mark();
        if (this.pod_formatting_code_108_quant_2(builder, opp)) {
            quantMarker3.drop();
        } else {
            quantMarker3.rollbackTo();
        }
        marker2.done(RakuElementTypes.POD_FORMATTED);
        return true;
    }

    private boolean pod_newline_109(PsiBuilder builder) {
        OPP opp;
        opp = null;
        if ((builder.getTokenType()) == RakuTokenTypes.POD_NEWLINE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean pod_para_content_110_alt_1(PsiBuilder builder, OPP opp) {
        if (!(this.pod_formatting_code_108(builder))) {
            return false;
        }
        return true;
    }

    private boolean pod_para_content_110_alt_2(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.POD_TEXT) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean pod_para_content_110_quant_3(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker2;
        altMarker2 = builder.mark();
        if (this.pod_para_content_110_alt_2(builder, opp)) {
            altMarker2.drop();
        } else {
            altMarker2.rollbackTo();
            PsiBuilder.Marker altMarker1;
            altMarker1 = builder.mark();
            if (this.pod_para_content_110_alt_1(builder, opp)) {
                altMarker1.drop();
            } else {
                altMarker1.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean pod_para_content_110_alt_4(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker quantMarker3;
        quantMarker3 = builder.mark();
        if (this.pod_para_content_110_quant_3(builder, opp)) {
            quantMarker3.drop();
        } else {
            quantMarker3.rollbackTo();
            return false;
        }
        while (true) {
            PsiBuilder.Marker quantMarker4;
            quantMarker4 = builder.mark();
            if (this.pod_para_content_110_quant_3(builder, opp)) {
                quantMarker4.drop();
            } else {
                quantMarker4.rollbackTo();
                break;
            }
        }
        return true;
    }

    private boolean pod_para_content_110_alt_5(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean pod_para_content_110_alt_6(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean pod_para_content_110_alt_7(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker7;
        altMarker7 = builder.mark();
        if (this.pod_para_content_110_alt_6(builder, opp)) {
            altMarker7.drop();
        } else {
            altMarker7.rollbackTo();
            PsiBuilder.Marker altMarker6;
            altMarker6 = builder.mark();
            if (this.pod_para_content_110_alt_5(builder, opp)) {
                altMarker6.drop();
            } else {
                altMarker6.rollbackTo();
                return false;
            }
        }
        if ((builder.getTokenType()) == RakuTokenTypes.POD_CODE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean pod_para_content_110_quant_8(PsiBuilder builder, OPP opp) {
        if (!(this.pod_newline_109(builder))) {
            return false;
        }
        return true;
    }

    private boolean pod_para_content_110_quant_9(PsiBuilder builder, OPP opp) {
        if (!(this.pod_removed_whitespace_111(builder))) {
            return false;
        }
        PsiBuilder.Marker altMarker8;
        altMarker8 = builder.mark();
        if (this.pod_para_content_110_alt_7(builder, opp)) {
            altMarker8.drop();
        } else {
            altMarker8.rollbackTo();
            PsiBuilder.Marker altMarker5;
            altMarker5 = builder.mark();
            if (this.pod_para_content_110_alt_4(builder, opp)) {
                altMarker5.drop();
            } else {
                altMarker5.rollbackTo();
                return false;
            }
        }
        PsiBuilder.Marker quantMarker9;
        quantMarker9 = builder.mark();
        if (this.pod_para_content_110_quant_8(builder, opp)) {
            quantMarker9.drop();
        } else {
            quantMarker9.rollbackTo();
        }
        return true;
    }

    private boolean pod_para_content_110(PsiBuilder builder) {
        OPP opp;
        opp = null;
        while (true) {
            PsiBuilder.Marker quantMarker10;
            quantMarker10 = builder.mark();
            if (this.pod_para_content_110_quant_9(builder, opp)) {
                quantMarker10.drop();
            } else {
                quantMarker10.rollbackTo();
                break;
            }
        }
        return true;
    }

    private boolean pod_removed_whitespace_111_quant_1(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.POD_REMOVED_WHITESPACE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean pod_removed_whitespace_111(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker quantMarker1;
        quantMarker1 = builder.mark();
        if (this.pod_removed_whitespace_111_quant_1(builder, opp)) {
            quantMarker1.drop();
        } else {
            quantMarker1.rollbackTo();
        }
        return true;
    }

    private boolean postcomment_112(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.COMMENT_STARTER) && (tt1.equals("#="))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if ((builder.getTokenType()) == RakuTokenTypes.COMMENT) {
            builder.advanceLexer();
        } else {
            return false;
        }
        marker1.done(RakuElementTypes.POD_POST_COMMENT);
        return true;
    }

    private boolean postcommentmulti_113_quant_1(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.COMMENT_QUOTE_CLOSE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean postcommentmulti_113(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.COMMENT_STARTER) && (tt1.equals("#="))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if ((builder.getTokenType()) == RakuTokenTypes.COMMENT_QUOTE_OPEN) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.multilinecommentnibbler_78(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker2;
        quantMarker2 = builder.mark();
        if (this.postcommentmulti_113_quant_1(builder, opp)) {
            quantMarker2.drop();
        } else {
            quantMarker2.rollbackTo();
        }
        marker1.done(RakuElementTypes.POD_POST_COMMENT);
        return true;
    }

    private boolean post_constraint_114_quant_1(PsiBuilder builder, OPP opp) {
        if (!(this.EXPR_2(builder))) {
            return false;
        }
        return true;
    }

    private boolean post_constraint_114_alt_2(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.WHERE_CONSTRAINT) && (tt1.equals("where"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker2;
        quantMarker2 = builder.mark();
        if (this.post_constraint_114_quant_1(builder, opp)) {
            quantMarker2.drop();
        } else {
            quantMarker2.rollbackTo();
        }
        marker1.done(RakuElementTypes.WHERE_CONSTRAINT);
        return true;
    }

    private boolean post_constraint_114_quant_3(PsiBuilder builder, OPP opp) {
        String tt3;
        tt3 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.PARENTHESES_CLOSE) && (tt3.equals(")"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean post_constraint_114_alt_4(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker4;
        marker4 = builder.mark();
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.PARENTHESES_OPEN) && (tt2.equals("("))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.signature_174(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker5;
        quantMarker5 = builder.mark();
        if (this.post_constraint_114_quant_3(builder, opp)) {
            quantMarker5.drop();
        } else {
            quantMarker5.rollbackTo();
        }
        marker4.done(RakuElementTypes.SIGNATURE);
        return true;
    }

    private boolean post_constraint_114_quant_5(PsiBuilder builder, OPP opp) {
        String tt5;
        tt5 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.SIGNATURE_BRACKET_CLOSE) && (tt5.equals("]"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean post_constraint_114_alt_6(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker7;
        marker7 = builder.mark();
        String tt4;
        tt4 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.SIGNATURE_BRACKET_OPEN) && (tt4.equals("["))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.signature_174(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker8;
        quantMarker8 = builder.mark();
        if (this.post_constraint_114_quant_5(builder, opp)) {
            quantMarker8.drop();
        } else {
            quantMarker8.rollbackTo();
        }
        marker7.done(RakuElementTypes.SIGNATURE);
        return true;
    }

    private boolean post_constraint_114_quant_7(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean post_constraint_114(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker9;
        altMarker9 = builder.mark();
        if (this.post_constraint_114_alt_6(builder, opp)) {
            altMarker9.drop();
        } else {
            altMarker9.rollbackTo();
            PsiBuilder.Marker altMarker6;
            altMarker6 = builder.mark();
            if (this.post_constraint_114_alt_4(builder, opp)) {
                altMarker6.drop();
            } else {
                altMarker6.rollbackTo();
                PsiBuilder.Marker altMarker3;
                altMarker3 = builder.mark();
                if (this.post_constraint_114_alt_2(builder, opp)) {
                    altMarker3.drop();
                } else {
                    altMarker3.rollbackTo();
                    return false;
                }
            }
        }
        PsiBuilder.Marker quantMarker10;
        quantMarker10 = builder.mark();
        if (this.post_constraint_114_quant_7(builder, opp)) {
            quantMarker10.drop();
        } else {
            quantMarker10.rollbackTo();
        }
        return true;
    }

    private boolean postcircumfix_115_quant_1(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean postcircumfix_115_quant_2(PsiBuilder builder, OPP opp) {
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.PARENTHESES_CLOSE) && (tt2.equals(")"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean postcircumfix_115_alt_3(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.PARENTHESES_OPEN) && (tt1.equals("("))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.arglist_6(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker2;
        quantMarker2 = builder.mark();
        if (this.postcircumfix_115_quant_1(builder, opp)) {
            quantMarker2.drop();
        } else {
            quantMarker2.rollbackTo();
        }
        PsiBuilder.Marker quantMarker3;
        quantMarker3 = builder.mark();
        if (this.postcircumfix_115_quant_2(builder, opp)) {
            quantMarker3.drop();
        } else {
            quantMarker3.rollbackTo();
        }
        marker1.done(RakuElementTypes.CALL);
        return true;
    }

    private boolean postcircumfix_115_quant_4(PsiBuilder builder, OPP opp) {
        String tt4;
        tt4 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.HASH_INDEX_BRACKET_CLOSE) && (tt4.equals(">"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean postcircumfix_115_quant_5(PsiBuilder builder, OPP opp) {
        if (!(this.quote_q_135(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker6;
        quantMarker6 = builder.mark();
        if (this.postcircumfix_115_quant_4(builder, opp)) {
            quantMarker6.drop();
        } else {
            quantMarker6.rollbackTo();
        }
        return true;
    }

    private boolean postcircumfix_115_alt_6(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker5;
        marker5 = builder.mark();
        String tt3;
        tt3 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.HASH_INDEX_BRACKET_OPEN) && (tt3.equals("<"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker quantMarker7;
        quantMarker7 = builder.mark();
        if (this.postcircumfix_115_quant_5(builder, opp)) {
            quantMarker7.drop();
        } else {
            quantMarker7.rollbackTo();
        }
        marker5.done(RakuElementTypes.HASH_INDEX);
        return true;
    }

    private boolean postcircumfix_115_quant_7(PsiBuilder builder, OPP opp) {
        String tt6;
        tt6 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.HASH_INDEX_BRACKET_CLOSE) && (tt6.equals("\u00BB"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean postcircumfix_115_quant_8(PsiBuilder builder, OPP opp) {
        if (!(this.quote_qq_137(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker10;
        quantMarker10 = builder.mark();
        if (this.postcircumfix_115_quant_7(builder, opp)) {
            quantMarker10.drop();
        } else {
            quantMarker10.rollbackTo();
        }
        return true;
    }

    private boolean postcircumfix_115_alt_9(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker9;
        marker9 = builder.mark();
        String tt5;
        tt5 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.HASH_INDEX_BRACKET_OPEN) && (tt5.equals("\u00AB"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker quantMarker11;
        quantMarker11 = builder.mark();
        if (this.postcircumfix_115_quant_8(builder, opp)) {
            quantMarker11.drop();
        } else {
            quantMarker11.rollbackTo();
        }
        marker9.done(RakuElementTypes.HASH_INDEX);
        return true;
    }

    private boolean postcircumfix_115_quant_10(PsiBuilder builder, OPP opp) {
        String tt8;
        tt8 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.HASH_INDEX_BRACKET_CLOSE) && (tt8.equals(">>"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean postcircumfix_115_quant_11(PsiBuilder builder, OPP opp) {
        if (!(this.quote_qq_137(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker14;
        quantMarker14 = builder.mark();
        if (this.postcircumfix_115_quant_10(builder, opp)) {
            quantMarker14.drop();
        } else {
            quantMarker14.rollbackTo();
        }
        return true;
    }

    private boolean postcircumfix_115_alt_12(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker13;
        marker13 = builder.mark();
        String tt7;
        tt7 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.HASH_INDEX_BRACKET_OPEN) && (tt7.equals("<<"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker quantMarker15;
        quantMarker15 = builder.mark();
        if (this.postcircumfix_115_quant_11(builder, opp)) {
            quantMarker15.drop();
        } else {
            quantMarker15.rollbackTo();
        }
        marker13.done(RakuElementTypes.HASH_INDEX);
        return true;
    }

    private boolean postcircumfix_115_quant_13(PsiBuilder builder, OPP opp) {
        String tt10;
        tt10 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.HASH_INDEX_BRACKET_CLOSE) && (tt10.equals("}"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean postcircumfix_115_quant_14(PsiBuilder builder, OPP opp) {
        if (!(this.semilist_168(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker18;
        quantMarker18 = builder.mark();
        if (this.postcircumfix_115_quant_13(builder, opp)) {
            quantMarker18.drop();
        } else {
            quantMarker18.rollbackTo();
        }
        return true;
    }

    private boolean postcircumfix_115_alt_15(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker17;
        marker17 = builder.mark();
        String tt9;
        tt9 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.HASH_INDEX_BRACKET_OPEN) && (tt9.equals("{"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker quantMarker19;
        quantMarker19 = builder.mark();
        if (this.postcircumfix_115_quant_14(builder, opp)) {
            quantMarker19.drop();
        } else {
            quantMarker19.rollbackTo();
        }
        marker17.done(RakuElementTypes.HASH_INDEX);
        return true;
    }

    private boolean postcircumfix_115_quant_16(PsiBuilder builder, OPP opp) {
        String tt12;
        tt12 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.ARRAY_INDEX_BRACKET_CLOSE) && (tt12.equals("]"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean postcircumfix_115_quant_17(PsiBuilder builder, OPP opp) {
        if (!(this.semilist_168(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker22;
        quantMarker22 = builder.mark();
        if (this.postcircumfix_115_quant_16(builder, opp)) {
            quantMarker22.drop();
        } else {
            quantMarker22.rollbackTo();
        }
        return true;
    }

    private boolean postcircumfix_115_alt_18(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker21;
        marker21 = builder.mark();
        String tt11;
        tt11 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.ARRAY_INDEX_BRACKET_OPEN) && (tt11.equals("["))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker quantMarker23;
        quantMarker23 = builder.mark();
        if (this.postcircumfix_115_quant_17(builder, opp)) {
            quantMarker23.drop();
        } else {
            quantMarker23.rollbackTo();
        }
        marker21.done(RakuElementTypes.ARRAY_INDEX);
        return true;
    }

    private boolean postcircumfix_115(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker24;
        altMarker24 = builder.mark();
        if (this.postcircumfix_115_alt_18(builder, opp)) {
            altMarker24.drop();
        } else {
            altMarker24.rollbackTo();
            PsiBuilder.Marker altMarker20;
            altMarker20 = builder.mark();
            if (this.postcircumfix_115_alt_15(builder, opp)) {
                altMarker20.drop();
            } else {
                altMarker20.rollbackTo();
                PsiBuilder.Marker altMarker16;
                altMarker16 = builder.mark();
                if (this.postcircumfix_115_alt_12(builder, opp)) {
                    altMarker16.drop();
                } else {
                    altMarker16.rollbackTo();
                    PsiBuilder.Marker altMarker12;
                    altMarker12 = builder.mark();
                    if (this.postcircumfix_115_alt_9(builder, opp)) {
                        altMarker12.drop();
                    } else {
                        altMarker12.rollbackTo();
                        PsiBuilder.Marker altMarker8;
                        altMarker8 = builder.mark();
                        if (this.postcircumfix_115_alt_6(builder, opp)) {
                            altMarker8.drop();
                        } else {
                            altMarker8.rollbackTo();
                            PsiBuilder.Marker altMarker4;
                            altMarker4 = builder.mark();
                            if (this.postcircumfix_115_alt_3(builder, opp)) {
                                altMarker4.drop();
                            } else {
                                altMarker4.rollbackTo();
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean postfix_116(PsiBuilder builder) {
        OPP opp;
        opp = null;
        if ((builder.getTokenType()) == RakuTokenTypes.POSTFIX) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean postfixish_117_alt_1(PsiBuilder builder, OPP opp) {
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.WHITE_SPACE) && (tt1.equals("\\"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean postfixish_117_alt_2(PsiBuilder builder, OPP opp) {
        if (!(this.unsp_250(builder))) {
            return false;
        }
        return true;
    }

    private boolean postfixish_117_quant_3(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.UNSPACED_POSTFIX) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker altMarker2;
        altMarker2 = builder.mark();
        if (this.postfixish_117_alt_2(builder, opp)) {
            altMarker2.drop();
        } else {
            altMarker2.rollbackTo();
            PsiBuilder.Marker altMarker1;
            altMarker1 = builder.mark();
            if (this.postfixish_117_alt_1(builder, opp)) {
                altMarker1.drop();
            } else {
                altMarker1.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean postfixish_117_alt_4(PsiBuilder builder, OPP opp) {
        if (!(this.postfixish_nometa_118(builder))) {
            return false;
        }
        return true;
    }

    private boolean postfixish_117_quant_5(PsiBuilder builder, OPP opp) {
        if (!(this.unsp_250(builder))) {
            return false;
        }
        return true;
    }

    private boolean postfixish_117_quant_6(PsiBuilder builder, OPP opp) {
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.METAOP) && (tt2.equals("."))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker quantMarker6;
        quantMarker6 = builder.mark();
        if (this.postfixish_117_quant_5(builder, opp)) {
            quantMarker6.drop();
        } else {
            quantMarker6.rollbackTo();
        }
        return true;
    }

    private boolean postfixish_117_alt_7(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.HYPER_METAOP_MISSING) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean postfixish_117_alt_8(PsiBuilder builder, OPP opp) {
        if (!(this.postfixish_nometa_118(builder))) {
            return false;
        }
        return true;
    }

    private boolean postfixish_117_alt_9(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker5;
        marker5 = builder.mark();
        PsiBuilder.Marker quantMarker7;
        quantMarker7 = builder.mark();
        if (this.postfixish_117_quant_6(builder, opp)) {
            quantMarker7.drop();
        } else {
            quantMarker7.rollbackTo();
        }
        if ((builder.getTokenType()) == RakuTokenTypes.METAOP) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker altMarker9;
        altMarker9 = builder.mark();
        if (this.postfixish_117_alt_8(builder, opp)) {
            altMarker9.drop();
        } else {
            altMarker9.rollbackTo();
            PsiBuilder.Marker altMarker8;
            altMarker8 = builder.mark();
            if (this.postfixish_117_alt_7(builder, opp)) {
                altMarker8.drop();
            } else {
                altMarker8.rollbackTo();
                return false;
            }
        }
        marker5.done(RakuElementTypes.HYPER_METAOP);
        return true;
    }

    private boolean postfixish_117(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker quantMarker3;
        quantMarker3 = builder.mark();
        if (this.postfixish_117_quant_3(builder, opp)) {
            quantMarker3.drop();
        } else {
            quantMarker3.rollbackTo();
        }
        PsiBuilder.Marker altMarker10;
        altMarker10 = builder.mark();
        if (this.postfixish_117_alt_9(builder, opp)) {
            altMarker10.drop();
        } else {
            altMarker10.rollbackTo();
            PsiBuilder.Marker altMarker4;
            altMarker4 = builder.mark();
            if (this.postfixish_117_alt_4(builder, opp)) {
                altMarker4.drop();
            } else {
                altMarker4.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean postfixish_nometa_118_alt_1(PsiBuilder builder, OPP opp) {
        if (!(this.privop_123(builder))) {
            return false;
        }
        return true;
    }

    private boolean postfixish_nometa_118_alt_2(PsiBuilder builder, OPP opp) {
        if (!(this.dotty_40(builder))) {
            return false;
        }
        return true;
    }

    private boolean postfixish_nometa_118_alt_3(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker3;
        marker3 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.METHOD_CALL_OPERATOR) && (tt1.equals("."))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.postcircumfix_115(builder))) {
            return false;
        }
        marker3.done(RakuElementTypes.METHOD_CALL);
        return true;
    }

    private boolean postfixish_nometa_118_alt_4(PsiBuilder builder, OPP opp) {
        if (!(this.postcircumfix_115(builder))) {
            return false;
        }
        return true;
    }

    private boolean postfixish_nometa_118_alt_5(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker6;
        marker6 = builder.mark();
        if (!(this.postfix_116(builder))) {
            return false;
        }
        marker6.done(RakuElementTypes.POSTFIX);
        return true;
    }

    private boolean postfixish_nometa_118_alt_6(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker8;
        marker8 = builder.mark();
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.POSTFIX) && (tt2.equals("."))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.postfix_116(builder))) {
            return false;
        }
        marker8.done(RakuElementTypes.POSTFIX);
        return true;
    }

    private boolean postfixish_nometa_118(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker9;
        altMarker9 = builder.mark();
        if (this.postfixish_nometa_118_alt_6(builder, opp)) {
            altMarker9.drop();
        } else {
            altMarker9.rollbackTo();
            PsiBuilder.Marker altMarker7;
            altMarker7 = builder.mark();
            if (this.postfixish_nometa_118_alt_5(builder, opp)) {
                altMarker7.drop();
            } else {
                altMarker7.rollbackTo();
                PsiBuilder.Marker altMarker5;
                altMarker5 = builder.mark();
                if (this.postfixish_nometa_118_alt_4(builder, opp)) {
                    altMarker5.drop();
                } else {
                    altMarker5.rollbackTo();
                    PsiBuilder.Marker altMarker4;
                    altMarker4 = builder.mark();
                    if (this.postfixish_nometa_118_alt_3(builder, opp)) {
                        altMarker4.drop();
                    } else {
                        altMarker4.rollbackTo();
                        PsiBuilder.Marker altMarker2;
                        altMarker2 = builder.mark();
                        if (this.postfixish_nometa_118_alt_2(builder, opp)) {
                            altMarker2.drop();
                        } else {
                            altMarker2.rollbackTo();
                            PsiBuilder.Marker altMarker1;
                            altMarker1 = builder.mark();
                            if (this.postfixish_nometa_118_alt_1(builder, opp)) {
                                altMarker1.drop();
                            } else {
                                altMarker1.rollbackTo();
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean precomment_119(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.COMMENT_STARTER) && (tt1.equals("#|"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if ((builder.getTokenType()) == RakuTokenTypes.COMMENT) {
            builder.advanceLexer();
        } else {
            return false;
        }
        marker1.done(RakuElementTypes.POD_PRE_COMMENT);
        return true;
    }

    private boolean precommentmulti_120_quant_1(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.COMMENT_QUOTE_CLOSE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean precommentmulti_120(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.COMMENT_STARTER) && (tt1.equals("#|"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if ((builder.getTokenType()) == RakuTokenTypes.COMMENT_QUOTE_OPEN) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.multilinecommentnibbler_78(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker2;
        quantMarker2 = builder.mark();
        if (this.precommentmulti_120_quant_1(builder, opp)) {
            quantMarker2.drop();
        } else {
            quantMarker2.rollbackTo();
        }
        marker1.done(RakuElementTypes.POD_PRE_COMMENT);
        return true;
    }

    private boolean prefix_121_alt_1(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.PREFIX) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean prefix_121_alt_2(PsiBuilder builder, OPP opp) {
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.PREFIX) && (tt1.equals("not"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.end_prefix_44(builder))) {
            return false;
        }
        return true;
    }

    private boolean prefix_121_alt_3(PsiBuilder builder, OPP opp) {
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.PREFIX) && (tt2.equals("so"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.end_prefix_44(builder))) {
            return false;
        }
        return true;
    }

    private boolean prefix_121_alt_4(PsiBuilder builder, OPP opp) {
        String tt3;
        tt3 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.PREFIX) && (tt3.equals("temp"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.kok_64(builder))) {
            return false;
        }
        return true;
    }

    private boolean prefix_121_alt_5(PsiBuilder builder, OPP opp) {
        String tt4;
        tt4 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.PREFIX) && (tt4.equals("let"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.kok_64(builder))) {
            return false;
        }
        return true;
    }

    private boolean prefix_121(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker5;
        altMarker5 = builder.mark();
        if (this.prefix_121_alt_5(builder, opp)) {
            altMarker5.drop();
        } else {
            altMarker5.rollbackTo();
            PsiBuilder.Marker altMarker4;
            altMarker4 = builder.mark();
            if (this.prefix_121_alt_4(builder, opp)) {
                altMarker4.drop();
            } else {
                altMarker4.rollbackTo();
                PsiBuilder.Marker altMarker3;
                altMarker3 = builder.mark();
                if (this.prefix_121_alt_3(builder, opp)) {
                    altMarker3.drop();
                } else {
                    altMarker3.rollbackTo();
                    PsiBuilder.Marker altMarker2;
                    altMarker2 = builder.mark();
                    if (this.prefix_121_alt_2(builder, opp)) {
                        altMarker2.drop();
                    } else {
                        altMarker2.rollbackTo();
                        PsiBuilder.Marker altMarker1;
                        altMarker1 = builder.mark();
                        if (this.prefix_121_alt_1(builder, opp)) {
                            altMarker1.drop();
                        } else {
                            altMarker1.rollbackTo();
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean prefixish_122_alt_1(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        if (!(this.prefix_121(builder))) {
            return false;
        }
        marker1.done(RakuElementTypes.PREFIX);
        return true;
    }

    private boolean prefixish_122_alt_2(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker3;
        marker3 = builder.mark();
        if (!(this.prefix_121(builder))) {
            return false;
        }
        if ((builder.getTokenType()) == RakuTokenTypes.METAOP) {
            builder.advanceLexer();
        } else {
            return false;
        }
        marker3.done(RakuElementTypes.HYPER_METAOP);
        return true;
    }

    private boolean prefixish_122_quant_3(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean prefixish_122(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker4;
        altMarker4 = builder.mark();
        if (this.prefixish_122_alt_2(builder, opp)) {
            altMarker4.drop();
        } else {
            altMarker4.rollbackTo();
            PsiBuilder.Marker altMarker2;
            altMarker2 = builder.mark();
            if (this.prefixish_122_alt_1(builder, opp)) {
                altMarker2.drop();
            } else {
                altMarker2.rollbackTo();
                return false;
            }
        }
        PsiBuilder.Marker quantMarker5;
        quantMarker5 = builder.mark();
        if (this.prefixish_122_quant_3(builder, opp)) {
            quantMarker5.drop();
        } else {
            quantMarker5.rollbackTo();
        }
        return true;
    }

    private boolean privop_123_quant_1(PsiBuilder builder, OPP opp) {
        if (!(this.methodop_71(builder))) {
            return false;
        }
        return true;
    }

    private boolean privop_123(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.METHOD_CALL_OPERATOR) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker quantMarker2;
        quantMarker2 = builder.mark();
        if (this.privop_123_quant_1(builder, opp)) {
            quantMarker2.drop();
        } else {
            quantMarker2.rollbackTo();
        }
        marker1.done(RakuElementTypes.METHOD_CALL);
        return true;
    }

    private boolean quantified_atom_124_quant_1(PsiBuilder builder, OPP opp) {
        if (!(this.sigmaybe_172(builder))) {
            return false;
        }
        return true;
    }

    private boolean quantified_atom_124_alt_2(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker3;
        marker3 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.REGEX_QUANTIFIER) {
            builder.advanceLexer();
        } else {
            return false;
        }
        marker3.done(RakuElementTypes.REGEX_QUANTIFIER);
        return true;
    }

    private boolean quantified_atom_124_alt_3(PsiBuilder builder, OPP opp) {
        if (!(this.quantifier_125(builder))) {
            return false;
        }
        return true;
    }

    private boolean quantified_atom_124_quant_4(PsiBuilder builder, OPP opp) {
        if (!(this.SIGOK_3(builder))) {
            return false;
        }
        if (!(this.sigmaybe_172(builder))) {
            return false;
        }
        return true;
    }

    private boolean quantified_atom_124_quant_5(PsiBuilder builder, OPP opp) {
        if (!(this.rxws_165(builder))) {
            return false;
        }
        if (!(this.separator_169(builder))) {
            return false;
        }
        return true;
    }

    private boolean quantified_atom_124_quant_6(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker5;
        altMarker5 = builder.mark();
        if (this.quantified_atom_124_alt_3(builder, opp)) {
            altMarker5.drop();
        } else {
            altMarker5.rollbackTo();
            PsiBuilder.Marker altMarker4;
            altMarker4 = builder.mark();
            if (this.quantified_atom_124_alt_2(builder, opp)) {
                altMarker4.drop();
            } else {
                altMarker4.rollbackTo();
                return false;
            }
        }
        PsiBuilder.Marker quantMarker6;
        quantMarker6 = builder.mark();
        if (this.quantified_atom_124_quant_4(builder, opp)) {
            quantMarker6.drop();
        } else {
            quantMarker6.rollbackTo();
        }
        PsiBuilder.Marker quantMarker7;
        quantMarker7 = builder.mark();
        if (this.quantified_atom_124_quant_5(builder, opp)) {
            quantMarker7.drop();
        } else {
            quantMarker7.rollbackTo();
        }
        return true;
    }

    private boolean quantified_atom_124(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        if (!(this.atom_9(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker2;
        quantMarker2 = builder.mark();
        if (this.quantified_atom_124_quant_1(builder, opp)) {
            quantMarker2.drop();
        } else {
            quantMarker2.rollbackTo();
        }
        PsiBuilder.Marker quantMarker8;
        quantMarker8 = builder.mark();
        if (this.quantified_atom_124_quant_6(builder, opp)) {
            quantMarker8.drop();
        } else {
            quantMarker8.rollbackTo();
        }
        marker1.done(RakuElementTypes.REGEX_ATOM);
        return true;
    }

    private boolean quantifier_125_alt_1(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.REGEX_QUANTIFIER) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean quantifier_125_quant_2(PsiBuilder builder, OPP opp) {
        if (!(this.normspace_82(builder))) {
            return false;
        }
        return true;
    }

    private boolean quantifier_125_quant_3(PsiBuilder builder, OPP opp) {
        if (!(this.normspace_82(builder))) {
            return false;
        }
        return true;
    }

    private boolean quantifier_125_alt_4(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.REGEX_QUANTIFIER_MISSING) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean quantifier_125_alt_5(PsiBuilder builder, OPP opp) {
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.WHATEVER) && (tt2.equals("*"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean quantifier_125_alt_6(PsiBuilder builder, OPP opp) {
        if (!(this.integer_61(builder))) {
            return false;
        }
        return true;
    }

    private boolean quantifier_125_quant_7(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker7;
        altMarker7 = builder.mark();
        if (this.quantifier_125_alt_6(builder, opp)) {
            altMarker7.drop();
        } else {
            altMarker7.rollbackTo();
            PsiBuilder.Marker altMarker6;
            altMarker6 = builder.mark();
            if (this.quantifier_125_alt_5(builder, opp)) {
                altMarker6.drop();
            } else {
                altMarker6.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean quantifier_125_quant_8(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.INFIX) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker quantMarker8;
        quantMarker8 = builder.mark();
        if (this.quantifier_125_quant_7(builder, opp)) {
            quantMarker8.drop();
        } else {
            quantMarker8.rollbackTo();
        }
        return true;
    }

    private boolean quantifier_125_alt_9(PsiBuilder builder, OPP opp) {
        if (!(this.integer_61(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker9;
        quantMarker9 = builder.mark();
        if (this.quantifier_125_quant_8(builder, opp)) {
            quantMarker9.drop();
        } else {
            quantMarker9.rollbackTo();
        }
        return true;
    }

    private boolean quantifier_125_alt_10(PsiBuilder builder, OPP opp) {
        String tt3;
        tt3 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.PREFIX) && (tt3.equals("^"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.integer_61(builder))) {
            return false;
        }
        return true;
    }

    private boolean quantifier_125_alt_11(PsiBuilder builder, OPP opp) {
        if (!(this.rxcodeblock_158(builder))) {
            return false;
        }
        return true;
    }

    private boolean quantifier_125_alt_12(PsiBuilder builder, OPP opp) {
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.REGEX_QUANTIFIER) && (tt1.equals("**"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker quantMarker3;
        quantMarker3 = builder.mark();
        if (this.quantifier_125_quant_2(builder, opp)) {
            quantMarker3.drop();
        } else {
            quantMarker3.rollbackTo();
        }
        if ((builder.getTokenType()) == RakuTokenTypes.REGEX_QUANTIFIER) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker quantMarker4;
        quantMarker4 = builder.mark();
        if (this.quantifier_125_quant_3(builder, opp)) {
            quantMarker4.drop();
        } else {
            quantMarker4.rollbackTo();
        }
        PsiBuilder.Marker altMarker12;
        altMarker12 = builder.mark();
        if (this.quantifier_125_alt_11(builder, opp)) {
            altMarker12.drop();
        } else {
            altMarker12.rollbackTo();
            PsiBuilder.Marker altMarker11;
            altMarker11 = builder.mark();
            if (this.quantifier_125_alt_10(builder, opp)) {
                altMarker11.drop();
            } else {
                altMarker11.rollbackTo();
                PsiBuilder.Marker altMarker10;
                altMarker10 = builder.mark();
                if (this.quantifier_125_alt_9(builder, opp)) {
                    altMarker10.drop();
                } else {
                    altMarker10.rollbackTo();
                    PsiBuilder.Marker altMarker5;
                    altMarker5 = builder.mark();
                    if (this.quantifier_125_alt_4(builder, opp)) {
                        altMarker5.drop();
                    } else {
                        altMarker5.rollbackTo();
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean quantifier_125(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        PsiBuilder.Marker altMarker13;
        altMarker13 = builder.mark();
        if (this.quantifier_125_alt_12(builder, opp)) {
            altMarker13.drop();
        } else {
            altMarker13.rollbackTo();
            PsiBuilder.Marker altMarker2;
            altMarker2 = builder.mark();
            if (this.quantifier_125_alt_1(builder, opp)) {
                altMarker2.drop();
            } else {
                altMarker2.rollbackTo();
                return false;
            }
        }
        marker1.done(RakuElementTypes.REGEX_QUANTIFIER);
        return true;
    }

    private boolean quibble_126_quant_1(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_CLOSE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean quibble_126_alt_2(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_OPEN) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.quote_nibbler_134(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker1;
        quantMarker1 = builder.mark();
        if (this.quibble_126_quant_1(builder, opp)) {
            quantMarker1.drop();
        } else {
            quantMarker1.rollbackTo();
        }
        return true;
    }

    private boolean quibble_126_quant_3(PsiBuilder builder, OPP opp) {
        if (!(this.quotepair_Q_142(builder))) {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean quibble_126_quant_4(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean quibble_126_quant_5(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean quibble_126_quant_6(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_CLOSE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean quibble_126_quant_7(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_OPEN) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker quantMarker5;
        quantMarker5 = builder.mark();
        if (this.quibble_126_quant_4(builder, opp)) {
            quantMarker5.drop();
        } else {
            quantMarker5.rollbackTo();
        }
        if (!(this.quote_nibbler_134(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker6;
        quantMarker6 = builder.mark();
        if (this.quibble_126_quant_5(builder, opp)) {
            quantMarker6.drop();
        } else {
            quantMarker6.rollbackTo();
        }
        PsiBuilder.Marker quantMarker7;
        quantMarker7 = builder.mark();
        if (this.quibble_126_quant_6(builder, opp)) {
            quantMarker7.drop();
        } else {
            quantMarker7.rollbackTo();
        }
        return true;
    }

    private boolean quibble_126_alt_8(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker quantMarker3;
        quantMarker3 = builder.mark();
        if (this.quibble_126_quant_3(builder, opp)) {
            quantMarker3.drop();
        } else {
            quantMarker3.rollbackTo();
            return false;
        }
        while (true) {
            PsiBuilder.Marker quantMarker4;
            quantMarker4 = builder.mark();
            if (this.quibble_126_quant_3(builder, opp)) {
                quantMarker4.drop();
            } else {
                quantMarker4.rollbackTo();
                break;
            }
        }
        PsiBuilder.Marker quantMarker8;
        quantMarker8 = builder.mark();
        if (this.quibble_126_quant_7(builder, opp)) {
            quantMarker8.drop();
        } else {
            quantMarker8.rollbackTo();
        }
        return true;
    }

    private boolean quibble_126(PsiBuilder builder) {
        OPP opp;
        opp = null;
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker altMarker9;
        altMarker9 = builder.mark();
        if (this.quibble_126_alt_8(builder, opp)) {
            altMarker9.drop();
        } else {
            altMarker9.rollbackTo();
            PsiBuilder.Marker altMarker2;
            altMarker2 = builder.mark();
            if (this.quibble_126_alt_2(builder, opp)) {
                altMarker2.drop();
            } else {
                altMarker2.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean quibble_rx_127_quant_1(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.QUOTE_REGEX) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean quibble_rx_127_alt_2(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.QUOTE_REGEX) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.enter_regex_nibbler_45(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker1;
        quantMarker1 = builder.mark();
        if (this.quibble_rx_127_quant_1(builder, opp)) {
            quantMarker1.drop();
        } else {
            quantMarker1.rollbackTo();
        }
        return true;
    }

    private boolean quibble_rx_127_quant_3(PsiBuilder builder, OPP opp) {
        if (!(this.quotepair_rx_143(builder))) {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean quibble_rx_127_quant_4(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.QUOTE_REGEX) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean quibble_rx_127_quant_5(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.QUOTE_REGEX) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.enter_regex_nibbler_45(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker5;
        quantMarker5 = builder.mark();
        if (this.quibble_rx_127_quant_4(builder, opp)) {
            quantMarker5.drop();
        } else {
            quantMarker5.rollbackTo();
        }
        return true;
    }

    private boolean quibble_rx_127_alt_6(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker quantMarker3;
        quantMarker3 = builder.mark();
        if (this.quibble_rx_127_quant_3(builder, opp)) {
            quantMarker3.drop();
        } else {
            quantMarker3.rollbackTo();
            return false;
        }
        while (true) {
            PsiBuilder.Marker quantMarker4;
            quantMarker4 = builder.mark();
            if (this.quibble_rx_127_quant_3(builder, opp)) {
                quantMarker4.drop();
            } else {
                quantMarker4.rollbackTo();
                break;
            }
        }
        PsiBuilder.Marker quantMarker6;
        quantMarker6 = builder.mark();
        if (this.quibble_rx_127_quant_5(builder, opp)) {
            quantMarker6.drop();
        } else {
            quantMarker6.rollbackTo();
        }
        return true;
    }

    private boolean quibble_rx_127(PsiBuilder builder) {
        OPP opp;
        opp = null;
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker altMarker7;
        altMarker7 = builder.mark();
        if (this.quibble_rx_127_alt_6(builder, opp)) {
            altMarker7.drop();
        } else {
            altMarker7.rollbackTo();
            PsiBuilder.Marker altMarker2;
            altMarker2 = builder.mark();
            if (this.quibble_rx_127_alt_2(builder, opp)) {
                altMarker2.drop();
            } else {
                altMarker2.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean quote_128_alt_1(PsiBuilder builder, OPP opp) {
        if (!(this.quote_qlang_136(builder))) {
            return false;
        }
        return true;
    }

    private boolean quote_128_alt_2(PsiBuilder builder, OPP opp) {
        if (!(this.quote_tr_140(builder))) {
            return false;
        }
        return true;
    }

    private boolean quote_128_alt_3(PsiBuilder builder, OPP opp) {
        if (!(this.quote_rxlang_139(builder))) {
            return false;
        }
        return true;
    }

    private boolean quote_128_alt_4(PsiBuilder builder, OPP opp) {
        if (!(this.quote_quasi_138(builder))) {
            return false;
        }
        return true;
    }

    private boolean quote_128(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker4;
        altMarker4 = builder.mark();
        if (this.quote_128_alt_4(builder, opp)) {
            altMarker4.drop();
        } else {
            altMarker4.rollbackTo();
            PsiBuilder.Marker altMarker3;
            altMarker3 = builder.mark();
            if (this.quote_128_alt_3(builder, opp)) {
                altMarker3.drop();
            } else {
                altMarker3.rollbackTo();
                PsiBuilder.Marker altMarker2;
                altMarker2 = builder.mark();
                if (this.quote_128_alt_2(builder, opp)) {
                    altMarker2.drop();
                } else {
                    altMarker2.rollbackTo();
                    PsiBuilder.Marker altMarker1;
                    altMarker1 = builder.mark();
                    if (this.quote_128_alt_1(builder, opp)) {
                        altMarker1.drop();
                    } else {
                        altMarker1.rollbackTo();
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean quote_Q_129(PsiBuilder builder) {
        OPP opp;
        opp = null;
        if (!(this.quote_nibbler_134(builder))) {
            return false;
        }
        return true;
    }

    private boolean quote_escape_130_alt_1(PsiBuilder builder, OPP opp) {
        if (!(this.block_15(builder))) {
            return false;
        }
        return true;
    }

    private boolean quote_escape_130_alt_2(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_ESCAPE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean quote_escape_130_alt_3(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.BAD_ESCAPE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean quote_escape_130_alt_4(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean quote_escape_130_alt_5(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean quote_escape_130_quant_6(PsiBuilder builder, OPP opp) {
        if (!(this.quote_128(builder))) {
            return false;
        }
        return true;
    }

    private boolean quote_escape_130_alt_7(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker5;
        altMarker5 = builder.mark();
        if (this.quote_escape_130_alt_5(builder, opp)) {
            altMarker5.drop();
        } else {
            altMarker5.rollbackTo();
            PsiBuilder.Marker altMarker4;
            altMarker4 = builder.mark();
            if (this.quote_escape_130_alt_4(builder, opp)) {
                altMarker4.drop();
            } else {
                altMarker4.rollbackTo();
                return false;
            }
        }
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_REQUOTE_ESCAPE) && (tt1.equals("\\"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker quantMarker6;
        quantMarker6 = builder.mark();
        if (this.quote_escape_130_quant_6(builder, opp)) {
            quantMarker6.drop();
        } else {
            quantMarker6.rollbackTo();
        }
        return true;
    }

    private boolean quote_escape_130_alt_8(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_ESCAPE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean quote_escape_130_alt_9(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.BAD_ESCAPE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean quote_escape_130_alt_10(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_ESCAPE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean quote_escape_130_quant_11(PsiBuilder builder, OPP opp) {
        if (!(this.quote_interpolation_postfix_131(builder))) {
            return false;
        }
        return true;
    }

    private boolean quote_escape_130_alt_12(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.ESCAPE_FUNCTION) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.variable_253(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker11;
        quantMarker11 = builder.mark();
        if (this.quote_escape_130_quant_11(builder, opp)) {
            quantMarker11.drop();
        } else {
            quantMarker11.rollbackTo();
            return false;
        }
        while (true) {
            PsiBuilder.Marker quantMarker12;
            quantMarker12 = builder.mark();
            if (this.quote_escape_130_quant_11(builder, opp)) {
                quantMarker12.drop();
            } else {
                quantMarker12.rollbackTo();
                break;
            }
        }
        return true;
    }

    private boolean quote_escape_130_quant_13(PsiBuilder builder, OPP opp) {
        if (!(this.quote_interpolation_postfix_131(builder))) {
            return false;
        }
        return true;
    }

    private boolean quote_escape_130_alt_14(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.ESCAPE_HASH) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.variable_253(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker14;
        quantMarker14 = builder.mark();
        if (this.quote_escape_130_quant_13(builder, opp)) {
            quantMarker14.drop();
        } else {
            quantMarker14.rollbackTo();
            return false;
        }
        while (true) {
            PsiBuilder.Marker quantMarker15;
            quantMarker15 = builder.mark();
            if (this.quote_escape_130_quant_13(builder, opp)) {
                quantMarker15.drop();
            } else {
                quantMarker15.rollbackTo();
                break;
            }
        }
        return true;
    }

    private boolean quote_escape_130_quant_15(PsiBuilder builder, OPP opp) {
        if (!(this.quote_interpolation_postfix_131(builder))) {
            return false;
        }
        return true;
    }

    private boolean quote_escape_130_alt_16(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.ESCAPE_ARRAY) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.variable_253(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker17;
        quantMarker17 = builder.mark();
        if (this.quote_escape_130_quant_15(builder, opp)) {
            quantMarker17.drop();
        } else {
            quantMarker17.rollbackTo();
            return false;
        }
        while (true) {
            PsiBuilder.Marker quantMarker18;
            quantMarker18 = builder.mark();
            if (this.quote_escape_130_quant_15(builder, opp)) {
                quantMarker18.drop();
            } else {
                quantMarker18.rollbackTo();
                break;
            }
        }
        return true;
    }

    private boolean quote_escape_130_alt_17(PsiBuilder builder, OPP opp) {
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.BAD_ESCAPE) && (tt2.equals("$"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean quote_escape_130_alt_18(PsiBuilder builder, OPP opp) {
        String tt3;
        tt3 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.BAD_ESCAPE) && (tt3.equals("$"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean quote_escape_130_quant_19(PsiBuilder builder, OPP opp) {
        if (!(this.quote_interpolation_postfix_131(builder))) {
            return false;
        }
        return true;
    }

    private boolean quote_escape_130_alt_20(PsiBuilder builder, OPP opp) {
        if (!(this.variable_253(builder))) {
            return false;
        }
        while (true) {
            PsiBuilder.Marker quantMarker22;
            quantMarker22 = builder.mark();
            if (this.quote_escape_130_quant_19(builder, opp)) {
                quantMarker22.drop();
            } else {
                quantMarker22.rollbackTo();
                break;
            }
        }
        return true;
    }

    private boolean quote_escape_130_alt_21(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.ESCAPE_SCALAR) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker altMarker23;
        altMarker23 = builder.mark();
        if (this.quote_escape_130_alt_20(builder, opp)) {
            altMarker23.drop();
        } else {
            altMarker23.rollbackTo();
            PsiBuilder.Marker altMarker21;
            altMarker21 = builder.mark();
            if (this.quote_escape_130_alt_18(builder, opp)) {
                altMarker21.drop();
            } else {
                altMarker21.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean quote_escape_130_alt_22(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker24;
        altMarker24 = builder.mark();
        if (this.quote_escape_130_alt_21(builder, opp)) {
            altMarker24.drop();
        } else {
            altMarker24.rollbackTo();
            PsiBuilder.Marker altMarker20;
            altMarker20 = builder.mark();
            if (this.quote_escape_130_alt_17(builder, opp)) {
                altMarker20.drop();
            } else {
                altMarker20.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean quote_escape_130(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker25;
        altMarker25 = builder.mark();
        if (this.quote_escape_130_alt_22(builder, opp)) {
            altMarker25.drop();
        } else {
            altMarker25.rollbackTo();
            PsiBuilder.Marker altMarker19;
            altMarker19 = builder.mark();
            if (this.quote_escape_130_alt_16(builder, opp)) {
                altMarker19.drop();
            } else {
                altMarker19.rollbackTo();
                PsiBuilder.Marker altMarker16;
                altMarker16 = builder.mark();
                if (this.quote_escape_130_alt_14(builder, opp)) {
                    altMarker16.drop();
                } else {
                    altMarker16.rollbackTo();
                    PsiBuilder.Marker altMarker13;
                    altMarker13 = builder.mark();
                    if (this.quote_escape_130_alt_12(builder, opp)) {
                        altMarker13.drop();
                    } else {
                        altMarker13.rollbackTo();
                        PsiBuilder.Marker altMarker10;
                        altMarker10 = builder.mark();
                        if (this.quote_escape_130_alt_10(builder, opp)) {
                            altMarker10.drop();
                        } else {
                            altMarker10.rollbackTo();
                            PsiBuilder.Marker altMarker9;
                            altMarker9 = builder.mark();
                            if (this.quote_escape_130_alt_9(builder, opp)) {
                                altMarker9.drop();
                            } else {
                                altMarker9.rollbackTo();
                                PsiBuilder.Marker altMarker8;
                                altMarker8 = builder.mark();
                                if (this.quote_escape_130_alt_8(builder, opp)) {
                                    altMarker8.drop();
                                } else {
                                    altMarker8.rollbackTo();
                                    PsiBuilder.Marker altMarker7;
                                    altMarker7 = builder.mark();
                                    if (this.quote_escape_130_alt_7(builder, opp)) {
                                        altMarker7.drop();
                                    } else {
                                        altMarker7.rollbackTo();
                                        PsiBuilder.Marker altMarker3;
                                        altMarker3 = builder.mark();
                                        if (this.quote_escape_130_alt_3(builder, opp)) {
                                            altMarker3.drop();
                                        } else {
                                            altMarker3.rollbackTo();
                                            PsiBuilder.Marker altMarker2;
                                            altMarker2 = builder.mark();
                                            if (this.quote_escape_130_alt_2(builder, opp)) {
                                                altMarker2.drop();
                                            } else {
                                                altMarker2.rollbackTo();
                                                PsiBuilder.Marker altMarker1;
                                                altMarker1 = builder.mark();
                                                if (this.quote_escape_130_alt_1(builder, opp)) {
                                                    altMarker1.drop();
                                                } else {
                                                    altMarker1.rollbackTo();
                                                    return false;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean quote_interpolation_postfix_131(PsiBuilder builder) {
        OPP opp;
        opp = null;
        if ((builder.getTokenType()) == RakuTokenTypes.POSTFIX_INTERPOLATIN) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.postfixish_117(builder))) {
            return false;
        }
        return true;
    }

    private boolean quote_mod_132(PsiBuilder builder) {
        OPP opp;
        opp = null;
        if ((builder.getTokenType()) == RakuTokenTypes.QUOTE_MOD) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean quote_mod_Q_133_alt_1(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean quote_mod_Q_133_alt_2(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean quote_mod_Q_133_alt_3(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean quote_mod_Q_133_alt_4(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean quote_mod_Q_133_alt_5(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean quote_mod_Q_133_alt_6(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean quote_mod_Q_133_quant_7(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker6;
        altMarker6 = builder.mark();
        if (this.quote_mod_Q_133_alt_6(builder, opp)) {
            altMarker6.drop();
        } else {
            altMarker6.rollbackTo();
            PsiBuilder.Marker altMarker5;
            altMarker5 = builder.mark();
            if (this.quote_mod_Q_133_alt_5(builder, opp)) {
                altMarker5.drop();
            } else {
                altMarker5.rollbackTo();
                PsiBuilder.Marker altMarker4;
                altMarker4 = builder.mark();
                if (this.quote_mod_Q_133_alt_4(builder, opp)) {
                    altMarker4.drop();
                } else {
                    altMarker4.rollbackTo();
                    PsiBuilder.Marker altMarker3;
                    altMarker3 = builder.mark();
                    if (this.quote_mod_Q_133_alt_3(builder, opp)) {
                        altMarker3.drop();
                    } else {
                        altMarker3.rollbackTo();
                        PsiBuilder.Marker altMarker2;
                        altMarker2 = builder.mark();
                        if (this.quote_mod_Q_133_alt_2(builder, opp)) {
                            altMarker2.drop();
                        } else {
                            altMarker2.rollbackTo();
                            PsiBuilder.Marker altMarker1;
                            altMarker1 = builder.mark();
                            if (this.quote_mod_Q_133_alt_1(builder, opp)) {
                                altMarker1.drop();
                            } else {
                                altMarker1.rollbackTo();
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean quote_mod_Q_133(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker quantMarker7;
        quantMarker7 = builder.mark();
        if (this.quote_mod_Q_133_quant_7(builder, opp)) {
            quantMarker7.drop();
        } else {
            quantMarker7.rollbackTo();
        }
        if (!(this.quote_mod_132(builder))) {
            return false;
        }
        return true;
    }

    private boolean quote_nibbler_134_alt_1(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_CHAR) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean quote_nibbler_134_alt_2(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_CHAR) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean quote_nibbler_134_alt_3(PsiBuilder builder, OPP opp) {
        if (!(this.quote_escape_130(builder))) {
            return false;
        }
        return true;
    }

    private boolean quote_nibbler_134_quant_4(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_CLOSE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean quote_nibbler_134_alt_5(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_OPEN) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.quote_nibbler_134(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker4;
        quantMarker4 = builder.mark();
        if (this.quote_nibbler_134_quant_4(builder, opp)) {
            quantMarker4.drop();
        } else {
            quantMarker4.rollbackTo();
        }
        return true;
    }

    private boolean quote_nibbler_134_quant_6(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker5;
        altMarker5 = builder.mark();
        if (this.quote_nibbler_134_alt_5(builder, opp)) {
            altMarker5.drop();
        } else {
            altMarker5.rollbackTo();
            PsiBuilder.Marker altMarker3;
            altMarker3 = builder.mark();
            if (this.quote_nibbler_134_alt_3(builder, opp)) {
                altMarker3.drop();
            } else {
                altMarker3.rollbackTo();
                PsiBuilder.Marker altMarker2;
                altMarker2 = builder.mark();
                if (this.quote_nibbler_134_alt_2(builder, opp)) {
                    altMarker2.drop();
                } else {
                    altMarker2.rollbackTo();
                    PsiBuilder.Marker altMarker1;
                    altMarker1 = builder.mark();
                    if (this.quote_nibbler_134_alt_1(builder, opp)) {
                        altMarker1.drop();
                    } else {
                        altMarker1.rollbackTo();
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean quote_nibbler_134(PsiBuilder builder) {
        OPP opp;
        opp = null;
        while (true) {
            PsiBuilder.Marker quantMarker6;
            quantMarker6 = builder.mark();
            if (this.quote_nibbler_134_quant_6(builder, opp)) {
                quantMarker6.drop();
            } else {
                quantMarker6.rollbackTo();
                break;
            }
        }
        return true;
    }

    private boolean quote_q_135(PsiBuilder builder) {
        OPP opp;
        opp = null;
        if (!(this.quote_nibbler_134(builder))) {
            return false;
        }
        return true;
    }

    private boolean quote_qlang_136_quant_1(PsiBuilder builder, OPP opp) {
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_CLOSE) && (tt2.equals("\uFF63"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean quote_qlang_136_alt_2(PsiBuilder builder, OPP opp) {
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_OPEN) && (tt1.equals("\uFF62"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.quote_Q_129(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker2;
        quantMarker2 = builder.mark();
        if (this.quote_qlang_136_quant_1(builder, opp)) {
            quantMarker2.drop();
        } else {
            quantMarker2.rollbackTo();
        }
        return true;
    }

    private boolean quote_qlang_136_quant_3(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_CLOSE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean quote_qlang_136_alt_4(PsiBuilder builder, OPP opp) {
        String tt3;
        tt3 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_OPEN) && (tt3.equals("\u201D"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.quote_qq_137(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker4;
        quantMarker4 = builder.mark();
        if (this.quote_qlang_136_quant_3(builder, opp)) {
            quantMarker4.drop();
        } else {
            quantMarker4.rollbackTo();
        }
        return true;
    }

    private boolean quote_qlang_136_quant_5(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_CLOSE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean quote_qlang_136_alt_6(PsiBuilder builder, OPP opp) {
        String tt4;
        tt4 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_OPEN) && (tt4.equals("\u201E"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.quote_qq_137(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker6;
        quantMarker6 = builder.mark();
        if (this.quote_qlang_136_quant_5(builder, opp)) {
            quantMarker6.drop();
        } else {
            quantMarker6.rollbackTo();
        }
        return true;
    }

    private boolean quote_qlang_136_quant_7(PsiBuilder builder, OPP opp) {
        String tt6;
        tt6 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_CLOSE) && (tt6.equals("\u201D"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean quote_qlang_136_alt_8(PsiBuilder builder, OPP opp) {
        String tt5;
        tt5 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_OPEN) && (tt5.equals("\u201C"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.quote_qq_137(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker8;
        quantMarker8 = builder.mark();
        if (this.quote_qlang_136_quant_7(builder, opp)) {
            quantMarker8.drop();
        } else {
            quantMarker8.rollbackTo();
        }
        return true;
    }

    private boolean quote_qlang_136_quant_9(PsiBuilder builder, OPP opp) {
        String tt8;
        tt8 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_CLOSE) && (tt8.equals("\""))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean quote_qlang_136_alt_10(PsiBuilder builder, OPP opp) {
        String tt7;
        tt7 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_OPEN) && (tt7.equals("\""))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.quote_qq_137(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker10;
        quantMarker10 = builder.mark();
        if (this.quote_qlang_136_quant_9(builder, opp)) {
            quantMarker10.drop();
        } else {
            quantMarker10.rollbackTo();
        }
        return true;
    }

    private boolean quote_qlang_136_quant_11(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_CLOSE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean quote_qlang_136_alt_12(PsiBuilder builder, OPP opp) {
        String tt9;
        tt9 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_OPEN) && (tt9.equals("\u2019"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.quote_q_135(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker12;
        quantMarker12 = builder.mark();
        if (this.quote_qlang_136_quant_11(builder, opp)) {
            quantMarker12.drop();
        } else {
            quantMarker12.rollbackTo();
        }
        return true;
    }

    private boolean quote_qlang_136_quant_13(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_CLOSE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean quote_qlang_136_alt_14(PsiBuilder builder, OPP opp) {
        String tt10;
        tt10 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_OPEN) && (tt10.equals("\u201A"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.quote_q_135(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker14;
        quantMarker14 = builder.mark();
        if (this.quote_qlang_136_quant_13(builder, opp)) {
            quantMarker14.drop();
        } else {
            quantMarker14.rollbackTo();
        }
        return true;
    }

    private boolean quote_qlang_136_quant_15(PsiBuilder builder, OPP opp) {
        String tt12;
        tt12 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_CLOSE) && (tt12.equals("\u2019"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean quote_qlang_136_alt_16(PsiBuilder builder, OPP opp) {
        String tt11;
        tt11 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_OPEN) && (tt11.equals("\u2018"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.quote_q_135(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker16;
        quantMarker16 = builder.mark();
        if (this.quote_qlang_136_quant_15(builder, opp)) {
            quantMarker16.drop();
        } else {
            quantMarker16.rollbackTo();
        }
        return true;
    }

    private boolean quote_qlang_136_quant_17(PsiBuilder builder, OPP opp) {
        String tt14;
        tt14 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_CLOSE) && (tt14.equals("'"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean quote_qlang_136_alt_18(PsiBuilder builder, OPP opp) {
        String tt13;
        tt13 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_OPEN) && (tt13.equals("'"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.quote_q_135(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker18;
        quantMarker18 = builder.mark();
        if (this.quote_qlang_136_quant_17(builder, opp)) {
            quantMarker18.drop();
        } else {
            quantMarker18.rollbackTo();
        }
        return true;
    }

    private boolean quote_qlang_136_quant_19(PsiBuilder builder, OPP opp) {
        if (!(this.quote_mod_Q_133(builder))) {
            return false;
        }
        return true;
    }

    private boolean quote_qlang_136_alt_20(PsiBuilder builder, OPP opp) {
        String tt15;
        tt15 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_SYNTAX) && (tt15.equals("q"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker quantMarker20;
        quantMarker20 = builder.mark();
        if (this.quote_qlang_136_quant_19(builder, opp)) {
            quantMarker20.drop();
        } else {
            quantMarker20.rollbackTo();
        }
        if (!(this.quibble_126(builder))) {
            return false;
        }
        return true;
    }

    private boolean quote_qlang_136_quant_21(PsiBuilder builder, OPP opp) {
        if (!(this.quote_mod_Q_133(builder))) {
            return false;
        }
        return true;
    }

    private boolean quote_qlang_136_alt_22(PsiBuilder builder, OPP opp) {
        String tt16;
        tt16 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_SYNTAX) && (tt16.equals("qq"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker quantMarker22;
        quantMarker22 = builder.mark();
        if (this.quote_qlang_136_quant_21(builder, opp)) {
            quantMarker22.drop();
        } else {
            quantMarker22.rollbackTo();
        }
        if (!(this.quibble_126(builder))) {
            return false;
        }
        return true;
    }

    private boolean quote_qlang_136_quant_23(PsiBuilder builder, OPP opp) {
        if (!(this.quote_mod_Q_133(builder))) {
            return false;
        }
        return true;
    }

    private boolean quote_qlang_136_alt_24(PsiBuilder builder, OPP opp) {
        String tt17;
        tt17 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_SYNTAX) && (tt17.equals("Q"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker quantMarker24;
        quantMarker24 = builder.mark();
        if (this.quote_qlang_136_quant_23(builder, opp)) {
            quantMarker24.drop();
        } else {
            quantMarker24.rollbackTo();
        }
        if (!(this.quibble_126(builder))) {
            return false;
        }
        return true;
    }

    private boolean quote_qlang_136(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        PsiBuilder.Marker altMarker25;
        altMarker25 = builder.mark();
        if (this.quote_qlang_136_alt_24(builder, opp)) {
            altMarker25.drop();
        } else {
            altMarker25.rollbackTo();
            PsiBuilder.Marker altMarker23;
            altMarker23 = builder.mark();
            if (this.quote_qlang_136_alt_22(builder, opp)) {
                altMarker23.drop();
            } else {
                altMarker23.rollbackTo();
                PsiBuilder.Marker altMarker21;
                altMarker21 = builder.mark();
                if (this.quote_qlang_136_alt_20(builder, opp)) {
                    altMarker21.drop();
                } else {
                    altMarker21.rollbackTo();
                    PsiBuilder.Marker altMarker19;
                    altMarker19 = builder.mark();
                    if (this.quote_qlang_136_alt_18(builder, opp)) {
                        altMarker19.drop();
                    } else {
                        altMarker19.rollbackTo();
                        PsiBuilder.Marker altMarker17;
                        altMarker17 = builder.mark();
                        if (this.quote_qlang_136_alt_16(builder, opp)) {
                            altMarker17.drop();
                        } else {
                            altMarker17.rollbackTo();
                            PsiBuilder.Marker altMarker15;
                            altMarker15 = builder.mark();
                            if (this.quote_qlang_136_alt_14(builder, opp)) {
                                altMarker15.drop();
                            } else {
                                altMarker15.rollbackTo();
                                PsiBuilder.Marker altMarker13;
                                altMarker13 = builder.mark();
                                if (this.quote_qlang_136_alt_12(builder, opp)) {
                                    altMarker13.drop();
                                } else {
                                    altMarker13.rollbackTo();
                                    PsiBuilder.Marker altMarker11;
                                    altMarker11 = builder.mark();
                                    if (this.quote_qlang_136_alt_10(builder, opp)) {
                                        altMarker11.drop();
                                    } else {
                                        altMarker11.rollbackTo();
                                        PsiBuilder.Marker altMarker9;
                                        altMarker9 = builder.mark();
                                        if (this.quote_qlang_136_alt_8(builder, opp)) {
                                            altMarker9.drop();
                                        } else {
                                            altMarker9.rollbackTo();
                                            PsiBuilder.Marker altMarker7;
                                            altMarker7 = builder.mark();
                                            if (this.quote_qlang_136_alt_6(builder, opp)) {
                                                altMarker7.drop();
                                            } else {
                                                altMarker7.rollbackTo();
                                                PsiBuilder.Marker altMarker5;
                                                altMarker5 = builder.mark();
                                                if (this.quote_qlang_136_alt_4(builder, opp)) {
                                                    altMarker5.drop();
                                                } else {
                                                    altMarker5.rollbackTo();
                                                    PsiBuilder.Marker altMarker3;
                                                    altMarker3 = builder.mark();
                                                    if (this.quote_qlang_136_alt_2(builder, opp)) {
                                                        altMarker3.drop();
                                                    } else {
                                                        altMarker3.rollbackTo();
                                                        return false;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        marker1.done(RakuElementTypes.STRING_LITERAL);
        return true;
    }

    private boolean quote_qq_137(PsiBuilder builder) {
        OPP opp;
        opp = null;
        if (!(this.quote_nibbler_134(builder))) {
            return false;
        }
        return true;
    }

    private boolean quote_quasi_138_quant_1(PsiBuilder builder, OPP opp) {
        if (!(this.block_15(builder))) {
            return false;
        }
        return true;
    }

    private boolean quote_quasi_138(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.QUASI) && (tt1.equals("quasi"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker2;
        quantMarker2 = builder.mark();
        if (this.quote_quasi_138_quant_1(builder, opp)) {
            quantMarker2.drop();
        } else {
            quantMarker2.rollbackTo();
        }
        marker1.done(RakuElementTypes.QUASI);
        return true;
    }

    private boolean quote_rxlang_139_alt_1(PsiBuilder builder, OPP opp) {
        if (!(this.sibble_170(builder))) {
            return false;
        }
        return true;
    }

    private boolean quote_rxlang_139_quant_2(PsiBuilder builder, OPP opp) {
        if (!(this.quotepair_rx_143(builder))) {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean quote_rxlang_139_quant_3(PsiBuilder builder, OPP opp) {
        if (!(this.sibble_170(builder))) {
            return false;
        }
        return true;
    }

    private boolean quote_rxlang_139_alt_4(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker quantMarker3;
        quantMarker3 = builder.mark();
        if (this.quote_rxlang_139_quant_2(builder, opp)) {
            quantMarker3.drop();
        } else {
            quantMarker3.rollbackTo();
            return false;
        }
        while (true) {
            PsiBuilder.Marker quantMarker4;
            quantMarker4 = builder.mark();
            if (this.quote_rxlang_139_quant_2(builder, opp)) {
                quantMarker4.drop();
            } else {
                quantMarker4.rollbackTo();
                break;
            }
        }
        PsiBuilder.Marker quantMarker5;
        quantMarker5 = builder.mark();
        if (this.quote_rxlang_139_quant_3(builder, opp)) {
            quantMarker5.drop();
        } else {
            quantMarker5.rollbackTo();
        }
        return true;
    }

    private boolean quote_rxlang_139_alt_5(PsiBuilder builder, OPP opp) {
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.QUOTE_REGEX) && (tt1.equals("s"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker altMarker6;
        altMarker6 = builder.mark();
        if (this.quote_rxlang_139_alt_4(builder, opp)) {
            altMarker6.drop();
        } else {
            altMarker6.rollbackTo();
            PsiBuilder.Marker altMarker2;
            altMarker2 = builder.mark();
            if (this.quote_rxlang_139_alt_1(builder, opp)) {
                altMarker2.drop();
            } else {
                altMarker2.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean quote_rxlang_139_alt_6(PsiBuilder builder, OPP opp) {
        if (!(this.sibble_170(builder))) {
            return false;
        }
        return true;
    }

    private boolean quote_rxlang_139_quant_7(PsiBuilder builder, OPP opp) {
        if (!(this.quotepair_rx_143(builder))) {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean quote_rxlang_139_quant_8(PsiBuilder builder, OPP opp) {
        if (!(this.sibble_170(builder))) {
            return false;
        }
        return true;
    }

    private boolean quote_rxlang_139_alt_9(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker quantMarker9;
        quantMarker9 = builder.mark();
        if (this.quote_rxlang_139_quant_7(builder, opp)) {
            quantMarker9.drop();
        } else {
            quantMarker9.rollbackTo();
            return false;
        }
        while (true) {
            PsiBuilder.Marker quantMarker10;
            quantMarker10 = builder.mark();
            if (this.quote_rxlang_139_quant_7(builder, opp)) {
                quantMarker10.drop();
            } else {
                quantMarker10.rollbackTo();
                break;
            }
        }
        PsiBuilder.Marker quantMarker11;
        quantMarker11 = builder.mark();
        if (this.quote_rxlang_139_quant_8(builder, opp)) {
            quantMarker11.drop();
        } else {
            quantMarker11.rollbackTo();
        }
        return true;
    }

    private boolean quote_rxlang_139_alt_10(PsiBuilder builder, OPP opp) {
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.QUOTE_REGEX) && (tt2.equals("S"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker altMarker12;
        altMarker12 = builder.mark();
        if (this.quote_rxlang_139_alt_9(builder, opp)) {
            altMarker12.drop();
        } else {
            altMarker12.rollbackTo();
            PsiBuilder.Marker altMarker8;
            altMarker8 = builder.mark();
            if (this.quote_rxlang_139_alt_6(builder, opp)) {
                altMarker8.drop();
            } else {
                altMarker8.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean quote_rxlang_139_alt_11(PsiBuilder builder, OPP opp) {
        if (!(this.sibble_170(builder))) {
            return false;
        }
        return true;
    }

    private boolean quote_rxlang_139_quant_12(PsiBuilder builder, OPP opp) {
        if (!(this.quotepair_rx_143(builder))) {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean quote_rxlang_139_quant_13(PsiBuilder builder, OPP opp) {
        if (!(this.sibble_170(builder))) {
            return false;
        }
        return true;
    }

    private boolean quote_rxlang_139_alt_14(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker quantMarker15;
        quantMarker15 = builder.mark();
        if (this.quote_rxlang_139_quant_12(builder, opp)) {
            quantMarker15.drop();
        } else {
            quantMarker15.rollbackTo();
            return false;
        }
        while (true) {
            PsiBuilder.Marker quantMarker16;
            quantMarker16 = builder.mark();
            if (this.quote_rxlang_139_quant_12(builder, opp)) {
                quantMarker16.drop();
            } else {
                quantMarker16.rollbackTo();
                break;
            }
        }
        PsiBuilder.Marker quantMarker17;
        quantMarker17 = builder.mark();
        if (this.quote_rxlang_139_quant_13(builder, opp)) {
            quantMarker17.drop();
        } else {
            quantMarker17.rollbackTo();
        }
        return true;
    }

    private boolean quote_rxlang_139_alt_15(PsiBuilder builder, OPP opp) {
        String tt3;
        tt3 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.QUOTE_REGEX) && (tt3.equals("ss"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker altMarker18;
        altMarker18 = builder.mark();
        if (this.quote_rxlang_139_alt_14(builder, opp)) {
            altMarker18.drop();
        } else {
            altMarker18.rollbackTo();
            PsiBuilder.Marker altMarker14;
            altMarker14 = builder.mark();
            if (this.quote_rxlang_139_alt_11(builder, opp)) {
                altMarker14.drop();
            } else {
                altMarker14.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean quote_rxlang_139_alt_16(PsiBuilder builder, OPP opp) {
        if (!(this.sibble_170(builder))) {
            return false;
        }
        return true;
    }

    private boolean quote_rxlang_139_quant_17(PsiBuilder builder, OPP opp) {
        if (!(this.quotepair_rx_143(builder))) {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean quote_rxlang_139_quant_18(PsiBuilder builder, OPP opp) {
        if (!(this.sibble_170(builder))) {
            return false;
        }
        return true;
    }

    private boolean quote_rxlang_139_alt_19(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker quantMarker21;
        quantMarker21 = builder.mark();
        if (this.quote_rxlang_139_quant_17(builder, opp)) {
            quantMarker21.drop();
        } else {
            quantMarker21.rollbackTo();
            return false;
        }
        while (true) {
            PsiBuilder.Marker quantMarker22;
            quantMarker22 = builder.mark();
            if (this.quote_rxlang_139_quant_17(builder, opp)) {
                quantMarker22.drop();
            } else {
                quantMarker22.rollbackTo();
                break;
            }
        }
        PsiBuilder.Marker quantMarker23;
        quantMarker23 = builder.mark();
        if (this.quote_rxlang_139_quant_18(builder, opp)) {
            quantMarker23.drop();
        } else {
            quantMarker23.rollbackTo();
        }
        return true;
    }

    private boolean quote_rxlang_139_alt_20(PsiBuilder builder, OPP opp) {
        String tt4;
        tt4 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.QUOTE_REGEX) && (tt4.equals("Ss"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker altMarker24;
        altMarker24 = builder.mark();
        if (this.quote_rxlang_139_alt_19(builder, opp)) {
            altMarker24.drop();
        } else {
            altMarker24.rollbackTo();
            PsiBuilder.Marker altMarker20;
            altMarker20 = builder.mark();
            if (this.quote_rxlang_139_alt_16(builder, opp)) {
                altMarker20.drop();
            } else {
                altMarker20.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean quote_rxlang_139_alt_21(PsiBuilder builder, OPP opp) {
        String tt5;
        tt5 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.QUOTE_REGEX) && (tt5.equals("m"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.quibble_rx_127(builder))) {
            return false;
        }
        return true;
    }

    private boolean quote_rxlang_139_alt_22(PsiBuilder builder, OPP opp) {
        String tt6;
        tt6 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.QUOTE_REGEX) && (tt6.equals("ms"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.quibble_rx_127(builder))) {
            return false;
        }
        return true;
    }

    private boolean quote_rxlang_139_alt_23(PsiBuilder builder, OPP opp) {
        String tt7;
        tt7 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.QUOTE_REGEX) && (tt7.equals("rx"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.quibble_rx_127(builder))) {
            return false;
        }
        return true;
    }

    private boolean quote_rxlang_139_alt_24(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.MISSING_REGEX) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean quote_rxlang_139_alt_25(PsiBuilder builder, OPP opp) {
        if (!(this.enter_regex_nibbler_45(builder))) {
            return false;
        }
        return true;
    }

    private boolean quote_rxlang_139_quant_26(PsiBuilder builder, OPP opp) {
        String tt9;
        tt9 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.QUOTE_REGEX) && (tt9.equals("/"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean quote_rxlang_139_alt_27(PsiBuilder builder, OPP opp) {
        String tt8;
        tt8 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.QUOTE_REGEX) && (tt8.equals("/"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker altMarker30;
        altMarker30 = builder.mark();
        if (this.quote_rxlang_139_alt_25(builder, opp)) {
            altMarker30.drop();
        } else {
            altMarker30.rollbackTo();
            PsiBuilder.Marker altMarker29;
            altMarker29 = builder.mark();
            if (this.quote_rxlang_139_alt_24(builder, opp)) {
                altMarker29.drop();
            } else {
                altMarker29.rollbackTo();
                return false;
            }
        }
        PsiBuilder.Marker quantMarker31;
        quantMarker31 = builder.mark();
        if (this.quote_rxlang_139_quant_26(builder, opp)) {
            quantMarker31.drop();
        } else {
            quantMarker31.rollbackTo();
        }
        return true;
    }

    private boolean quote_rxlang_139(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        PsiBuilder.Marker altMarker32;
        altMarker32 = builder.mark();
        if (this.quote_rxlang_139_alt_27(builder, opp)) {
            altMarker32.drop();
        } else {
            altMarker32.rollbackTo();
            PsiBuilder.Marker altMarker28;
            altMarker28 = builder.mark();
            if (this.quote_rxlang_139_alt_23(builder, opp)) {
                altMarker28.drop();
            } else {
                altMarker28.rollbackTo();
                PsiBuilder.Marker altMarker27;
                altMarker27 = builder.mark();
                if (this.quote_rxlang_139_alt_22(builder, opp)) {
                    altMarker27.drop();
                } else {
                    altMarker27.rollbackTo();
                    PsiBuilder.Marker altMarker26;
                    altMarker26 = builder.mark();
                    if (this.quote_rxlang_139_alt_21(builder, opp)) {
                        altMarker26.drop();
                    } else {
                        altMarker26.rollbackTo();
                        PsiBuilder.Marker altMarker25;
                        altMarker25 = builder.mark();
                        if (this.quote_rxlang_139_alt_20(builder, opp)) {
                            altMarker25.drop();
                        } else {
                            altMarker25.rollbackTo();
                            PsiBuilder.Marker altMarker19;
                            altMarker19 = builder.mark();
                            if (this.quote_rxlang_139_alt_15(builder, opp)) {
                                altMarker19.drop();
                            } else {
                                altMarker19.rollbackTo();
                                PsiBuilder.Marker altMarker13;
                                altMarker13 = builder.mark();
                                if (this.quote_rxlang_139_alt_10(builder, opp)) {
                                    altMarker13.drop();
                                } else {
                                    altMarker13.rollbackTo();
                                    PsiBuilder.Marker altMarker7;
                                    altMarker7 = builder.mark();
                                    if (this.quote_rxlang_139_alt_5(builder, opp)) {
                                        altMarker7.drop();
                                    } else {
                                        altMarker7.rollbackTo();
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        marker1.done(RakuElementTypes.QUOTE_REGEX);
        return true;
    }

    private boolean quote_tr_140_alt_1(PsiBuilder builder, OPP opp) {
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.QUOTE_REGEX) && (tt1.equals("TR"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean quote_tr_140_alt_2(PsiBuilder builder, OPP opp) {
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.QUOTE_REGEX) && (tt2.equals("tr"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean quote_tr_140_alt_3(PsiBuilder builder, OPP opp) {
        if (!(this.tribble_244(builder))) {
            return false;
        }
        return true;
    }

    private boolean quote_tr_140_quant_4(PsiBuilder builder, OPP opp) {
        if (!(this.quotepair_rx_143(builder))) {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean quote_tr_140_alt_5(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker quantMarker5;
        quantMarker5 = builder.mark();
        if (this.quote_tr_140_quant_4(builder, opp)) {
            quantMarker5.drop();
        } else {
            quantMarker5.rollbackTo();
            return false;
        }
        while (true) {
            PsiBuilder.Marker quantMarker6;
            quantMarker6 = builder.mark();
            if (this.quote_tr_140_quant_4(builder, opp)) {
                quantMarker6.drop();
            } else {
                quantMarker6.rollbackTo();
                break;
            }
        }
        if (!(this.tribble_244(builder))) {
            return false;
        }
        return true;
    }

    private boolean quote_tr_140(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        PsiBuilder.Marker altMarker3;
        altMarker3 = builder.mark();
        if (this.quote_tr_140_alt_2(builder, opp)) {
            altMarker3.drop();
        } else {
            altMarker3.rollbackTo();
            PsiBuilder.Marker altMarker2;
            altMarker2 = builder.mark();
            if (this.quote_tr_140_alt_1(builder, opp)) {
                altMarker2.drop();
            } else {
                altMarker2.rollbackTo();
                return false;
            }
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker altMarker7;
        altMarker7 = builder.mark();
        if (this.quote_tr_140_alt_5(builder, opp)) {
            altMarker7.drop();
        } else {
            altMarker7.rollbackTo();
            PsiBuilder.Marker altMarker4;
            altMarker4 = builder.mark();
            if (this.quote_tr_140_alt_3(builder, opp)) {
                altMarker4.drop();
            } else {
                altMarker4.rollbackTo();
                return false;
            }
        }
        marker1.done(RakuElementTypes.TRANSLITERATION);
        return true;
    }

    private boolean quotepair_141_quant_1(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.COLON_PAIR_HAS_VALUE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.circumfix_27(builder))) {
            return false;
        }
        return true;
    }

    private boolean quotepair_141_alt_2(PsiBuilder builder, OPP opp) {
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.QUOTE_PAIR) && (tt1.equals(":"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if ((builder.getTokenType()) == RakuTokenTypes.QUOTE_PAIR) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker quantMarker2;
        quantMarker2 = builder.mark();
        if (this.quotepair_141_quant_1(builder, opp)) {
            quantMarker2.drop();
        } else {
            quantMarker2.rollbackTo();
        }
        return true;
    }

    private boolean quotepair_141_alt_3(PsiBuilder builder, OPP opp) {
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.QUOTE_PAIR) && (tt2.equals(":!"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if ((builder.getTokenType()) == RakuTokenTypes.QUOTE_PAIR) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean quotepair_141_alt_4(PsiBuilder builder, OPP opp) {
        String tt3;
        tt3 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.QUOTE_PAIR) && (tt3.equals(":"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if ((builder.getTokenType()) == RakuTokenTypes.INTEGER_LITERAL) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if ((builder.getTokenType()) == RakuTokenTypes.QUOTE_PAIR) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean quotepair_141(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        PsiBuilder.Marker altMarker5;
        altMarker5 = builder.mark();
        if (this.quotepair_141_alt_4(builder, opp)) {
            altMarker5.drop();
        } else {
            altMarker5.rollbackTo();
            PsiBuilder.Marker altMarker4;
            altMarker4 = builder.mark();
            if (this.quotepair_141_alt_3(builder, opp)) {
                altMarker4.drop();
            } else {
                altMarker4.rollbackTo();
                PsiBuilder.Marker altMarker3;
                altMarker3 = builder.mark();
                if (this.quotepair_141_alt_2(builder, opp)) {
                    altMarker3.drop();
                } else {
                    altMarker3.rollbackTo();
                    return false;
                }
            }
        }
        marker1.done(RakuElementTypes.QUOTE_PAIR);
        return true;
    }

    private boolean quotepair_Q_142_alt_1(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean quotepair_Q_142_alt_2(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean quotepair_Q_142_alt_3(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker2;
        altMarker2 = builder.mark();
        if (this.quotepair_Q_142_alt_2(builder, opp)) {
            altMarker2.drop();
        } else {
            altMarker2.rollbackTo();
            PsiBuilder.Marker altMarker1;
            altMarker1 = builder.mark();
            if (this.quotepair_Q_142_alt_1(builder, opp)) {
                altMarker1.drop();
            } else {
                altMarker1.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean quotepair_Q_142_alt_4(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean quotepair_Q_142_alt_5(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean quotepair_Q_142_alt_6(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean quotepair_Q_142_alt_7(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean quotepair_Q_142_alt_8(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean quotepair_Q_142_alt_9(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean quotepair_Q_142_alt_10(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean quotepair_Q_142_alt_11(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean quotepair_Q_142_alt_12(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean quotepair_Q_142_alt_13(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean quotepair_Q_142_alt_14(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean quotepair_Q_142_alt_15(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean quotepair_Q_142_alt_16(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean quotepair_Q_142_quant_17(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker16;
        altMarker16 = builder.mark();
        if (this.quotepair_Q_142_alt_16(builder, opp)) {
            altMarker16.drop();
        } else {
            altMarker16.rollbackTo();
            PsiBuilder.Marker altMarker15;
            altMarker15 = builder.mark();
            if (this.quotepair_Q_142_alt_15(builder, opp)) {
                altMarker15.drop();
            } else {
                altMarker15.rollbackTo();
                PsiBuilder.Marker altMarker14;
                altMarker14 = builder.mark();
                if (this.quotepair_Q_142_alt_14(builder, opp)) {
                    altMarker14.drop();
                } else {
                    altMarker14.rollbackTo();
                    PsiBuilder.Marker altMarker13;
                    altMarker13 = builder.mark();
                    if (this.quotepair_Q_142_alt_13(builder, opp)) {
                        altMarker13.drop();
                    } else {
                        altMarker13.rollbackTo();
                        PsiBuilder.Marker altMarker12;
                        altMarker12 = builder.mark();
                        if (this.quotepair_Q_142_alt_12(builder, opp)) {
                            altMarker12.drop();
                        } else {
                            altMarker12.rollbackTo();
                            PsiBuilder.Marker altMarker11;
                            altMarker11 = builder.mark();
                            if (this.quotepair_Q_142_alt_11(builder, opp)) {
                                altMarker11.drop();
                            } else {
                                altMarker11.rollbackTo();
                                PsiBuilder.Marker altMarker10;
                                altMarker10 = builder.mark();
                                if (this.quotepair_Q_142_alt_10(builder, opp)) {
                                    altMarker10.drop();
                                } else {
                                    altMarker10.rollbackTo();
                                    PsiBuilder.Marker altMarker9;
                                    altMarker9 = builder.mark();
                                    if (this.quotepair_Q_142_alt_9(builder, opp)) {
                                        altMarker9.drop();
                                    } else {
                                        altMarker9.rollbackTo();
                                        PsiBuilder.Marker altMarker8;
                                        altMarker8 = builder.mark();
                                        if (this.quotepair_Q_142_alt_8(builder, opp)) {
                                            altMarker8.drop();
                                        } else {
                                            altMarker8.rollbackTo();
                                            PsiBuilder.Marker altMarker7;
                                            altMarker7 = builder.mark();
                                            if (this.quotepair_Q_142_alt_7(builder, opp)) {
                                                altMarker7.drop();
                                            } else {
                                                altMarker7.rollbackTo();
                                                PsiBuilder.Marker altMarker6;
                                                altMarker6 = builder.mark();
                                                if (this.quotepair_Q_142_alt_6(builder, opp)) {
                                                    altMarker6.drop();
                                                } else {
                                                    altMarker6.rollbackTo();
                                                    PsiBuilder.Marker altMarker5;
                                                    altMarker5 = builder.mark();
                                                    if (this.quotepair_Q_142_alt_5(builder, opp)) {
                                                        altMarker5.drop();
                                                    } else {
                                                        altMarker5.rollbackTo();
                                                        PsiBuilder.Marker altMarker4;
                                                        altMarker4 = builder.mark();
                                                        if (this.quotepair_Q_142_alt_4(builder, opp)) {
                                                            altMarker4.drop();
                                                        } else {
                                                            altMarker4.rollbackTo();
                                                            PsiBuilder.Marker altMarker3;
                                                            altMarker3 = builder.mark();
                                                            if (this.quotepair_Q_142_alt_3(builder, opp)) {
                                                                altMarker3.drop();
                                                            } else {
                                                                altMarker3.rollbackTo();
                                                                return false;
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean quotepair_Q_142(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker quantMarker17;
        quantMarker17 = builder.mark();
        if (this.quotepair_Q_142_quant_17(builder, opp)) {
            quantMarker17.drop();
        } else {
            quantMarker17.rollbackTo();
        }
        if (!(this.quotepair_141(builder))) {
            return false;
        }
        return true;
    }

    private boolean quotepair_rx_143_alt_1(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean quotepair_rx_143_alt_2(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean quotepair_rx_143_quant_3(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker2;
        altMarker2 = builder.mark();
        if (this.quotepair_rx_143_alt_2(builder, opp)) {
            altMarker2.drop();
        } else {
            altMarker2.rollbackTo();
            PsiBuilder.Marker altMarker1;
            altMarker1 = builder.mark();
            if (this.quotepair_rx_143_alt_1(builder, opp)) {
                altMarker1.drop();
            } else {
                altMarker1.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean quotepair_rx_143(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker quantMarker3;
        quantMarker3 = builder.mark();
        if (this.quotepair_rx_143_quant_3(builder, opp)) {
            quantMarker3.drop();
        } else {
            quantMarker3.rollbackTo();
        }
        if (!(this.quotepair_141(builder))) {
            return false;
        }
        return true;
    }

    private boolean rad_digit_144_alt_1(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean rad_digit_144_alt_2(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean rad_digit_144(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker2;
        altMarker2 = builder.mark();
        if (this.rad_digit_144_alt_2(builder, opp)) {
            altMarker2.drop();
        } else {
            altMarker2.rollbackTo();
            PsiBuilder.Marker altMarker1;
            altMarker1 = builder.mark();
            if (this.rad_digit_144_alt_1(builder, opp)) {
                altMarker1.drop();
            } else {
                altMarker1.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean rad_digits_145_quant_1(PsiBuilder builder, OPP opp) {
        if (!(this.rad_digit_144(builder))) {
            return false;
        }
        return true;
    }

    private boolean rad_digits_145_quant_2(PsiBuilder builder, OPP opp) {
        if (!(this.rad_digit_144(builder))) {
            return false;
        }
        return true;
    }

    private boolean rad_digits_145_quant_3(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker quantMarker3;
        quantMarker3 = builder.mark();
        if (this.rad_digits_145_quant_2(builder, opp)) {
            quantMarker3.drop();
        } else {
            quantMarker3.rollbackTo();
            return false;
        }
        while (true) {
            PsiBuilder.Marker quantMarker4;
            quantMarker4 = builder.mark();
            if (this.rad_digits_145_quant_2(builder, opp)) {
                quantMarker4.drop();
            } else {
                quantMarker4.rollbackTo();
                break;
            }
        }
        return true;
    }

    private boolean rad_digits_145(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker quantMarker1;
        quantMarker1 = builder.mark();
        if (this.rad_digits_145_quant_1(builder, opp)) {
            quantMarker1.drop();
        } else {
            quantMarker1.rollbackTo();
            return false;
        }
        while (true) {
            PsiBuilder.Marker quantMarker2;
            quantMarker2 = builder.mark();
            if (this.rad_digits_145_quant_1(builder, opp)) {
                quantMarker2.drop();
            } else {
                quantMarker2.rollbackTo();
                break;
            }
        }
        while (true) {
            PsiBuilder.Marker quantMarker5;
            quantMarker5 = builder.mark();
            if (this.rad_digits_145_quant_3(builder, opp)) {
                quantMarker5.drop();
            } else {
                quantMarker5.rollbackTo();
                break;
            }
        }
        return true;
    }

    private boolean rad_number_146_quant_1(PsiBuilder builder, OPP opp) {
        if (!(this.unsp_250(builder))) {
            return false;
        }
        return true;
    }

    private boolean rad_number_146_quant_2(PsiBuilder builder, OPP opp) {
        String tt3;
        tt3 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.RADIX_NUMBER) && (tt3.equals(")"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean rad_number_146_alt_3(PsiBuilder builder, OPP opp) {
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.RADIX_NUMBER) && (tt2.equals("("))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.semilist_168(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker3;
        quantMarker3 = builder.mark();
        if (this.rad_number_146_quant_2(builder, opp)) {
            quantMarker3.drop();
        } else {
            quantMarker3.rollbackTo();
        }
        return true;
    }

    private boolean rad_number_146_quant_4(PsiBuilder builder, OPP opp) {
        String tt5;
        tt5 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.RADIX_NUMBER) && (tt5.equals("]"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean rad_number_146_alt_5(PsiBuilder builder, OPP opp) {
        String tt4;
        tt4 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.RADIX_NUMBER) && (tt4.equals("["))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.semilist_168(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker5;
        quantMarker5 = builder.mark();
        if (this.rad_number_146_quant_4(builder, opp)) {
            quantMarker5.drop();
        } else {
            quantMarker5.rollbackTo();
        }
        return true;
    }

    private boolean rad_number_146_alt_6(PsiBuilder builder, OPP opp) {
        String tt7;
        tt7 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.RADIX_NUMBER) && (tt7.equals("0b"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean rad_number_146_alt_7(PsiBuilder builder, OPP opp) {
        String tt8;
        tt8 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.RADIX_NUMBER) && (tt8.equals("0d"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean rad_number_146_alt_8(PsiBuilder builder, OPP opp) {
        String tt9;
        tt9 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.RADIX_NUMBER) && (tt9.equals("0o"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean rad_number_146_alt_9(PsiBuilder builder, OPP opp) {
        String tt10;
        tt10 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.RADIX_NUMBER) && (tt10.equals("0x"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean rad_number_146_quant_10(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker10;
        altMarker10 = builder.mark();
        if (this.rad_number_146_alt_9(builder, opp)) {
            altMarker10.drop();
        } else {
            altMarker10.rollbackTo();
            PsiBuilder.Marker altMarker9;
            altMarker9 = builder.mark();
            if (this.rad_number_146_alt_8(builder, opp)) {
                altMarker9.drop();
            } else {
                altMarker9.rollbackTo();
                PsiBuilder.Marker altMarker8;
                altMarker8 = builder.mark();
                if (this.rad_number_146_alt_7(builder, opp)) {
                    altMarker8.drop();
                } else {
                    altMarker8.rollbackTo();
                    PsiBuilder.Marker altMarker7;
                    altMarker7 = builder.mark();
                    if (this.rad_number_146_alt_6(builder, opp)) {
                        altMarker7.drop();
                    } else {
                        altMarker7.rollbackTo();
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean rad_number_146_quant_11(PsiBuilder builder, OPP opp) {
        String tt11;
        tt11 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.RADIX_NUMBER) && (tt11.equals(">"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean rad_number_146_alt_12(PsiBuilder builder, OPP opp) {
        String tt6;
        tt6 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.RADIX_NUMBER) && (tt6.equals("<"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker quantMarker11;
        quantMarker11 = builder.mark();
        if (this.rad_number_146_quant_10(builder, opp)) {
            quantMarker11.drop();
        } else {
            quantMarker11.rollbackTo();
        }
        if ((builder.getTokenType()) == RakuTokenTypes.RADIX_NUMBER) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker quantMarker12;
        quantMarker12 = builder.mark();
        if (this.rad_number_146_quant_11(builder, opp)) {
            quantMarker12.drop();
        } else {
            quantMarker12.rollbackTo();
        }
        return true;
    }

    private boolean rad_number_146(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.RADIX_NUMBER) && (tt1.equals(":"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if ((builder.getTokenType()) == RakuTokenTypes.RADIX_NUMBER) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker quantMarker2;
        quantMarker2 = builder.mark();
        if (this.rad_number_146_quant_1(builder, opp)) {
            quantMarker2.drop();
        } else {
            quantMarker2.rollbackTo();
        }
        PsiBuilder.Marker altMarker13;
        altMarker13 = builder.mark();
        if (this.rad_number_146_alt_12(builder, opp)) {
            altMarker13.drop();
        } else {
            altMarker13.rollbackTo();
            PsiBuilder.Marker altMarker6;
            altMarker6 = builder.mark();
            if (this.rad_number_146_alt_5(builder, opp)) {
                altMarker6.drop();
            } else {
                altMarker6.rollbackTo();
                PsiBuilder.Marker altMarker4;
                altMarker4 = builder.mark();
                if (this.rad_number_146_alt_3(builder, opp)) {
                    altMarker4.drop();
                } else {
                    altMarker4.rollbackTo();
                    return false;
                }
            }
        }
        marker1.done(RakuElementTypes.RADIX_NUMBER);
        return true;
    }

    private boolean radint_147(PsiBuilder builder) {
        OPP opp;
        opp = null;
        if (!(this.integer_lex_62(builder))) {
            return false;
        }
        return true;
    }

    private boolean rat_number_148(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.RAT_LITERAL) && (tt1.equals("<"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.bare_rat_number_13(builder))) {
            return false;
        }
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.RAT_LITERAL) && (tt2.equals(">"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        marker1.done(RakuElementTypes.RAT_LITERAL);
        return true;
    }

    private boolean regex_declarator_149_alt_1(PsiBuilder builder, OPP opp) {
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.REGEX_DECLARATOR) && (tt1.equals("token"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.kok_64(builder))) {
            return false;
        }
        if (!(this.regex_def_150(builder))) {
            return false;
        }
        return true;
    }

    private boolean regex_declarator_149_alt_2(PsiBuilder builder, OPP opp) {
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.REGEX_DECLARATOR) && (tt2.equals("rule"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.kok_64(builder))) {
            return false;
        }
        if (!(this.regex_def_150(builder))) {
            return false;
        }
        return true;
    }

    private boolean regex_declarator_149_alt_3(PsiBuilder builder, OPP opp) {
        String tt3;
        tt3 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.REGEX_DECLARATOR) && (tt3.equals("regex"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.kok_64(builder))) {
            return false;
        }
        if (!(this.regex_def_150(builder))) {
            return false;
        }
        return true;
    }

    private boolean regex_declarator_149(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        PsiBuilder.Marker altMarker4;
        altMarker4 = builder.mark();
        if (this.regex_declarator_149_alt_3(builder, opp)) {
            altMarker4.drop();
        } else {
            altMarker4.rollbackTo();
            PsiBuilder.Marker altMarker3;
            altMarker3 = builder.mark();
            if (this.regex_declarator_149_alt_2(builder, opp)) {
                altMarker3.drop();
            } else {
                altMarker3.rollbackTo();
                PsiBuilder.Marker altMarker2;
                altMarker2 = builder.mark();
                if (this.regex_declarator_149_alt_1(builder, opp)) {
                    altMarker2.drop();
                } else {
                    altMarker2.rollbackTo();
                    return false;
                }
            }
        }
        marker1.done(RakuElementTypes.REGEX_DECLARATION);
        return true;
    }

    private boolean regex_def_150_quant_1(PsiBuilder builder, OPP opp) {
        if (!(this.routine_name_155(builder))) {
            return false;
        }
        return true;
    }

    private boolean regex_def_150_quant_2(PsiBuilder builder, OPP opp) {
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.PARENTHESES_CLOSE) && (tt2.equals(")"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean regex_def_150_quant_3(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker2;
        marker2 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.PARENTHESES_OPEN) && (tt1.equals("("))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.signature_174(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker3;
        quantMarker3 = builder.mark();
        if (this.regex_def_150_quant_2(builder, opp)) {
            quantMarker3.drop();
        } else {
            quantMarker3.rollbackTo();
        }
        marker2.done(RakuElementTypes.SIGNATURE);
        return true;
    }

    private boolean regex_def_150_quant_4(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean regex_def_150_quant_5(PsiBuilder builder, OPP opp) {
        if (!(this.trait_242(builder))) {
            return false;
        }
        return true;
    }

    private boolean regex_def_150_alt_6(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.MISSING_REGEX) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean regex_def_150_alt_7(PsiBuilder builder, OPP opp) {
        if (!(this.enter_regex_nibbler_45(builder))) {
            return false;
        }
        return true;
    }

    private boolean regex_def_150_alt_8(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.ONLY_STAR) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean regex_def_150_quant_9(PsiBuilder builder, OPP opp) {
        String tt4;
        tt4 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.BLOCK_CURLY_BRACKET_CLOSE) && (tt4.equals("}"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean regex_def_150_quant_10(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker7;
        marker7 = builder.mark();
        String tt3;
        tt3 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.BLOCK_CURLY_BRACKET_OPEN) && (tt3.equals("{"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker altMarker10;
        altMarker10 = builder.mark();
        if (this.regex_def_150_alt_8(builder, opp)) {
            altMarker10.drop();
        } else {
            altMarker10.rollbackTo();
            PsiBuilder.Marker altMarker9;
            altMarker9 = builder.mark();
            if (this.regex_def_150_alt_7(builder, opp)) {
                altMarker9.drop();
            } else {
                altMarker9.rollbackTo();
                PsiBuilder.Marker altMarker8;
                altMarker8 = builder.mark();
                if (this.regex_def_150_alt_6(builder, opp)) {
                    altMarker8.drop();
                } else {
                    altMarker8.rollbackTo();
                    return false;
                }
            }
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker11;
        quantMarker11 = builder.mark();
        if (this.regex_def_150_quant_9(builder, opp)) {
            quantMarker11.drop();
        } else {
            quantMarker11.rollbackTo();
        }
        marker7.done(RakuElementTypes.BLOCKOID);
        return true;
    }

    private boolean regex_def_150(PsiBuilder builder) {
        OPP opp;
        opp = null;
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker1;
        quantMarker1 = builder.mark();
        if (this.regex_def_150_quant_1(builder, opp)) {
            quantMarker1.drop();
        } else {
            quantMarker1.rollbackTo();
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker4;
        quantMarker4 = builder.mark();
        if (this.regex_def_150_quant_3(builder, opp)) {
            quantMarker4.drop();
        } else {
            quantMarker4.rollbackTo();
        }
        PsiBuilder.Marker quantMarker5;
        quantMarker5 = builder.mark();
        if (this.regex_def_150_quant_4(builder, opp)) {
            quantMarker5.drop();
        } else {
            quantMarker5.rollbackTo();
        }
        while (true) {
            PsiBuilder.Marker quantMarker6;
            quantMarker6 = builder.mark();
            if (this.regex_def_150_quant_5(builder, opp)) {
                quantMarker6.drop();
            } else {
                quantMarker6.rollbackTo();
                break;
            }
        }
        PsiBuilder.Marker quantMarker12;
        quantMarker12 = builder.mark();
        if (this.regex_def_150_quant_10(builder, opp)) {
            quantMarker12.drop();
        } else {
            quantMarker12.rollbackTo();
        }
        return true;
    }

    private boolean regex_nibbler_151_alt_1(PsiBuilder builder, OPP opp) {
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.REGEX_INFIX) && (tt1.equals("&"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean regex_nibbler_151_alt_2(PsiBuilder builder, OPP opp) {
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.REGEX_INFIX) && (tt2.equals("&&"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean regex_nibbler_151_alt_3(PsiBuilder builder, OPP opp) {
        String tt3;
        tt3 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.REGEX_INFIX) && (tt3.equals("|"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean regex_nibbler_151_alt_4(PsiBuilder builder, OPP opp) {
        String tt4;
        tt4 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.REGEX_INFIX) && (tt4.equals("||"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean regex_nibbler_151_quant_5(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker4;
        altMarker4 = builder.mark();
        if (this.regex_nibbler_151_alt_4(builder, opp)) {
            altMarker4.drop();
        } else {
            altMarker4.rollbackTo();
            PsiBuilder.Marker altMarker3;
            altMarker3 = builder.mark();
            if (this.regex_nibbler_151_alt_3(builder, opp)) {
                altMarker3.drop();
            } else {
                altMarker3.rollbackTo();
                PsiBuilder.Marker altMarker2;
                altMarker2 = builder.mark();
                if (this.regex_nibbler_151_alt_2(builder, opp)) {
                    altMarker2.drop();
                } else {
                    altMarker2.rollbackTo();
                    PsiBuilder.Marker altMarker1;
                    altMarker1 = builder.mark();
                    if (this.regex_nibbler_151_alt_1(builder, opp)) {
                        altMarker1.drop();
                    } else {
                        altMarker1.rollbackTo();
                        return false;
                    }
                }
            }
        }
        if (!(this.rxws_165(builder))) {
            return false;
        }
        return true;
    }

    private boolean regex_nibbler_151_quant_6(PsiBuilder builder, OPP opp) {
        if (!(this.termseq_240(builder))) {
            return false;
        }
        return true;
    }

    private boolean regex_nibbler_151(PsiBuilder builder) {
        OPP opp;
        opp = null;
        if (!(this.rxws_165(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker5;
        quantMarker5 = builder.mark();
        if (this.regex_nibbler_151_quant_5(builder, opp)) {
            quantMarker5.drop();
        } else {
            quantMarker5.rollbackTo();
        }
        PsiBuilder.Marker quantMarker6;
        quantMarker6 = builder.mark();
        if (this.regex_nibbler_151_quant_6(builder, opp)) {
            quantMarker6.drop();
        } else {
            quantMarker6.rollbackTo();
        }
        return true;
    }

    private boolean regex_nibbler_fresh_rx_152(PsiBuilder builder) {
        OPP opp;
        opp = null;
        if (!(this.regex_nibbler_151(builder))) {
            return false;
        }
        return true;
    }

    private boolean routine_declarator_153_alt_1(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.ROUTINE_DECLARATOR) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.method_def_69(builder))) {
            return false;
        }
        return true;
    }

    private boolean routine_declarator_153_alt_2(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.ROUTINE_DECLARATOR) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.method_def_69(builder))) {
            return false;
        }
        return true;
    }

    private boolean routine_declarator_153_alt_3(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.ROUTINE_DECLARATOR) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.method_def_69(builder))) {
            return false;
        }
        return true;
    }

    private boolean routine_declarator_153_alt_4(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.ROUTINE_DECLARATOR) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.routine_def_154(builder))) {
            return false;
        }
        return true;
    }

    private boolean routine_declarator_153(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        PsiBuilder.Marker altMarker5;
        altMarker5 = builder.mark();
        if (this.routine_declarator_153_alt_4(builder, opp)) {
            altMarker5.drop();
        } else {
            altMarker5.rollbackTo();
            PsiBuilder.Marker altMarker4;
            altMarker4 = builder.mark();
            if (this.routine_declarator_153_alt_3(builder, opp)) {
                altMarker4.drop();
            } else {
                altMarker4.rollbackTo();
                PsiBuilder.Marker altMarker3;
                altMarker3 = builder.mark();
                if (this.routine_declarator_153_alt_2(builder, opp)) {
                    altMarker3.drop();
                } else {
                    altMarker3.rollbackTo();
                    PsiBuilder.Marker altMarker2;
                    altMarker2 = builder.mark();
                    if (this.routine_declarator_153_alt_1(builder, opp)) {
                        altMarker2.drop();
                    } else {
                        altMarker2.rollbackTo();
                        return false;
                    }
                }
            }
        }
        marker1.done(RakuElementTypes.ROUTINE_DECLARATION);
        return true;
    }

    private boolean routine_def_154_quant_1(PsiBuilder builder, OPP opp) {
        if (!(this.routine_name_155(builder))) {
            return false;
        }
        return true;
    }

    private boolean routine_def_154_quant_2(PsiBuilder builder, OPP opp) {
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.PARENTHESES_CLOSE) && (tt2.equals(")"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean routine_def_154_quant_3(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker2;
        marker2 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.PARENTHESES_OPEN) && (tt1.equals("("))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.signature_174(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker3;
        quantMarker3 = builder.mark();
        if (this.routine_def_154_quant_2(builder, opp)) {
            quantMarker3.drop();
        } else {
            quantMarker3.rollbackTo();
        }
        marker2.done(RakuElementTypes.SIGNATURE);
        return true;
    }

    private boolean routine_def_154_quant_4(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean routine_def_154_quant_5(PsiBuilder builder, OPP opp) {
        if (!(this.trait_242(builder))) {
            return false;
        }
        return true;
    }

    private boolean routine_def_154_alt_6(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean routine_def_154_quant_7(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean routine_def_154_quant_8(PsiBuilder builder, OPP opp) {
        if (!(this.statementlist_221(builder))) {
            return false;
        }
        return true;
    }

    private boolean routine_def_154_alt_9(PsiBuilder builder, OPP opp) {
        String tt3;
        tt3 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STATEMENT_TERMINATOR) && (tt3.equals(";"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker quantMarker8;
        quantMarker8 = builder.mark();
        if (this.routine_def_154_quant_7(builder, opp)) {
            quantMarker8.drop();
        } else {
            quantMarker8.rollbackTo();
        }
        PsiBuilder.Marker quantMarker9;
        quantMarker9 = builder.mark();
        if (this.routine_def_154_quant_8(builder, opp)) {
            quantMarker9.drop();
        } else {
            quantMarker9.rollbackTo();
        }
        return true;
    }

    private boolean routine_def_154_alt_10(PsiBuilder builder, OPP opp) {
        if (!(this.blockoid_16(builder))) {
            return false;
        }
        return true;
    }

    private boolean routine_def_154_alt_11(PsiBuilder builder, OPP opp) {
        if (!(this.onlystar_87(builder))) {
            return false;
        }
        return true;
    }

    private boolean routine_def_154(PsiBuilder builder) {
        OPP opp;
        opp = null;
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker1;
        quantMarker1 = builder.mark();
        if (this.routine_def_154_quant_1(builder, opp)) {
            quantMarker1.drop();
        } else {
            quantMarker1.rollbackTo();
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker4;
        quantMarker4 = builder.mark();
        if (this.routine_def_154_quant_3(builder, opp)) {
            quantMarker4.drop();
        } else {
            quantMarker4.rollbackTo();
        }
        PsiBuilder.Marker quantMarker5;
        quantMarker5 = builder.mark();
        if (this.routine_def_154_quant_4(builder, opp)) {
            quantMarker5.drop();
        } else {
            quantMarker5.rollbackTo();
        }
        while (true) {
            PsiBuilder.Marker quantMarker6;
            quantMarker6 = builder.mark();
            if (this.routine_def_154_quant_5(builder, opp)) {
                quantMarker6.drop();
            } else {
                quantMarker6.rollbackTo();
                break;
            }
        }
        PsiBuilder.Marker altMarker12;
        altMarker12 = builder.mark();
        if (this.routine_def_154_alt_11(builder, opp)) {
            altMarker12.drop();
        } else {
            altMarker12.rollbackTo();
            PsiBuilder.Marker altMarker11;
            altMarker11 = builder.mark();
            if (this.routine_def_154_alt_10(builder, opp)) {
                altMarker11.drop();
            } else {
                altMarker11.rollbackTo();
                PsiBuilder.Marker altMarker10;
                altMarker10 = builder.mark();
                if (this.routine_def_154_alt_9(builder, opp)) {
                    altMarker10.drop();
                } else {
                    altMarker10.rollbackTo();
                    PsiBuilder.Marker altMarker7;
                    altMarker7 = builder.mark();
                    if (this.routine_def_154_alt_6(builder, opp)) {
                        altMarker7.drop();
                    } else {
                        altMarker7.rollbackTo();
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean routine_name_155(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.ROUTINE_NAME) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.longname_colonpairs_67(builder))) {
            return false;
        }
        marker1.done(RakuElementTypes.LONG_NAME);
        return true;
    }

    private boolean rxQ_156_quant_1(PsiBuilder builder, OPP opp) {
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_CLOSE) && (tt2.equals("\uFF63"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean rxQ_156(PsiBuilder builder) {
        OPP opp;
        opp = null;
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_OPEN) && (tt1.equals("\uFF62"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.quote_Q_129(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker1;
        quantMarker1 = builder.mark();
        if (this.rxQ_156_quant_1(builder, opp)) {
            quantMarker1.drop();
        } else {
            quantMarker1.rollbackTo();
        }
        return true;
    }

    private boolean rxarglist_157(PsiBuilder builder) {
        OPP opp;
        opp = null;
        if (!(this.arglist_6(builder))) {
            return false;
        }
        /* HAND-EDIT (not generator output): matches the MAINBraid.java
         * _254_rxarglist hand-edit -- a regex assertion's colon-argument list
         * may be followed by whitespace before the closing '>', so the lexer
         * now emits ws tokens here and the parser must consume them. Mirrored
         * in tools/p6-grammar-to-idea/perl6.pm6 token rxarglist. */
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean rxcodeblock_158(PsiBuilder builder) {
        OPP opp;
        opp = null;
        if (!(this.pblock_95(builder))) {
            return false;
        }
        return true;
    }

    private boolean rxinfixstopper_159_alt_1(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean rxinfixstopper_159_alt_2(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean rxinfixstopper_159_alt_3(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean rxinfixstopper_159(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker3;
        altMarker3 = builder.mark();
        if (this.rxinfixstopper_159_alt_3(builder, opp)) {
            altMarker3.drop();
        } else {
            altMarker3.rollbackTo();
            PsiBuilder.Marker altMarker2;
            altMarker2 = builder.mark();
            if (this.rxinfixstopper_159_alt_2(builder, opp)) {
                altMarker2.drop();
            } else {
                altMarker2.rollbackTo();
                PsiBuilder.Marker altMarker1;
                altMarker1 = builder.mark();
                if (this.rxinfixstopper_159_alt_1(builder, opp)) {
                    altMarker1.drop();
                } else {
                    altMarker1.rollbackTo();
                    return false;
                }
            }
        }
        return true;
    }

    private boolean rxq_160_quant_1(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_CLOSE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean rxq_160_alt_2(PsiBuilder builder, OPP opp) {
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_OPEN) && (tt1.equals("\u2019"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.quote_q_135(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker2;
        quantMarker2 = builder.mark();
        if (this.rxq_160_quant_1(builder, opp)) {
            quantMarker2.drop();
        } else {
            quantMarker2.rollbackTo();
        }
        return true;
    }

    private boolean rxq_160_quant_3(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_CLOSE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean rxq_160_alt_4(PsiBuilder builder, OPP opp) {
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_OPEN) && (tt2.equals("\u201A"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.quote_q_135(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker4;
        quantMarker4 = builder.mark();
        if (this.rxq_160_quant_3(builder, opp)) {
            quantMarker4.drop();
        } else {
            quantMarker4.rollbackTo();
        }
        return true;
    }

    private boolean rxq_160_quant_5(PsiBuilder builder, OPP opp) {
        String tt4;
        tt4 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_CLOSE) && (tt4.equals("\u2019"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean rxq_160_alt_6(PsiBuilder builder, OPP opp) {
        String tt3;
        tt3 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_OPEN) && (tt3.equals("\u2018"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.quote_q_135(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker6;
        quantMarker6 = builder.mark();
        if (this.rxq_160_quant_5(builder, opp)) {
            quantMarker6.drop();
        } else {
            quantMarker6.rollbackTo();
        }
        return true;
    }

    private boolean rxq_160_quant_7(PsiBuilder builder, OPP opp) {
        String tt6;
        tt6 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_CLOSE) && (tt6.equals("'"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean rxq_160_alt_8(PsiBuilder builder, OPP opp) {
        String tt5;
        tt5 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_OPEN) && (tt5.equals("'"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.quote_q_135(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker8;
        quantMarker8 = builder.mark();
        if (this.rxq_160_quant_7(builder, opp)) {
            quantMarker8.drop();
        } else {
            quantMarker8.rollbackTo();
        }
        return true;
    }

    private boolean rxq_160(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        PsiBuilder.Marker altMarker9;
        altMarker9 = builder.mark();
        if (this.rxq_160_alt_8(builder, opp)) {
            altMarker9.drop();
        } else {
            altMarker9.rollbackTo();
            PsiBuilder.Marker altMarker7;
            altMarker7 = builder.mark();
            if (this.rxq_160_alt_6(builder, opp)) {
                altMarker7.drop();
            } else {
                altMarker7.rollbackTo();
                PsiBuilder.Marker altMarker5;
                altMarker5 = builder.mark();
                if (this.rxq_160_alt_4(builder, opp)) {
                    altMarker5.drop();
                } else {
                    altMarker5.rollbackTo();
                    PsiBuilder.Marker altMarker3;
                    altMarker3 = builder.mark();
                    if (this.rxq_160_alt_2(builder, opp)) {
                        altMarker3.drop();
                    } else {
                        altMarker3.rollbackTo();
                        return false;
                    }
                }
            }
        }
        marker1.done(RakuElementTypes.STRING_LITERAL);
        return true;
    }

    private boolean rxqq_161_quant_1(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_CLOSE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean rxqq_161_alt_2(PsiBuilder builder, OPP opp) {
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_OPEN) && (tt1.equals("\u201D"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.quote_qq_137(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker2;
        quantMarker2 = builder.mark();
        if (this.rxqq_161_quant_1(builder, opp)) {
            quantMarker2.drop();
        } else {
            quantMarker2.rollbackTo();
        }
        return true;
    }

    private boolean rxqq_161_quant_3(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_CLOSE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean rxqq_161_alt_4(PsiBuilder builder, OPP opp) {
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_OPEN) && (tt2.equals("\u201E"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.quote_qq_137(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker4;
        quantMarker4 = builder.mark();
        if (this.rxqq_161_quant_3(builder, opp)) {
            quantMarker4.drop();
        } else {
            quantMarker4.rollbackTo();
        }
        return true;
    }

    private boolean rxqq_161_quant_5(PsiBuilder builder, OPP opp) {
        String tt4;
        tt4 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_CLOSE) && (tt4.equals("\u201D"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean rxqq_161_alt_6(PsiBuilder builder, OPP opp) {
        String tt3;
        tt3 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_OPEN) && (tt3.equals("\u201C"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.quote_qq_137(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker6;
        quantMarker6 = builder.mark();
        if (this.rxqq_161_quant_5(builder, opp)) {
            quantMarker6.drop();
        } else {
            quantMarker6.rollbackTo();
        }
        return true;
    }

    private boolean rxqq_161_quant_7(PsiBuilder builder, OPP opp) {
        String tt6;
        tt6 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_CLOSE) && (tt6.equals("\""))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean rxqq_161_alt_8(PsiBuilder builder, OPP opp) {
        String tt5;
        tt5 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_OPEN) && (tt5.equals("\""))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.quote_qq_137(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker8;
        quantMarker8 = builder.mark();
        if (this.rxqq_161_quant_7(builder, opp)) {
            quantMarker8.drop();
        } else {
            quantMarker8.rollbackTo();
        }
        return true;
    }

    private boolean rxqq_161(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        PsiBuilder.Marker altMarker9;
        altMarker9 = builder.mark();
        if (this.rxqq_161_alt_8(builder, opp)) {
            altMarker9.drop();
        } else {
            altMarker9.rollbackTo();
            PsiBuilder.Marker altMarker7;
            altMarker7 = builder.mark();
            if (this.rxqq_161_alt_6(builder, opp)) {
                altMarker7.drop();
            } else {
                altMarker7.rollbackTo();
                PsiBuilder.Marker altMarker5;
                altMarker5 = builder.mark();
                if (this.rxqq_161_alt_4(builder, opp)) {
                    altMarker5.drop();
                } else {
                    altMarker5.rollbackTo();
                    PsiBuilder.Marker altMarker3;
                    altMarker3 = builder.mark();
                    if (this.rxqq_161_alt_2(builder, opp)) {
                        altMarker3.drop();
                    } else {
                        altMarker3.rollbackTo();
                        return false;
                    }
                }
            }
        }
        marker1.done(RakuElementTypes.STRING_LITERAL);
        return true;
    }

    private boolean rxqw_162_quant_1(PsiBuilder builder, OPP opp) {
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_CLOSE) && (tt2.equals(">"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean rxqw_162(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STRING_LITERAL_QUOTE_OPEN) && (tt1.equals("<"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.quote_q_135(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker2;
        quantMarker2 = builder.mark();
        if (this.rxqw_162_quant_1(builder, opp)) {
            quantMarker2.drop();
        } else {
            quantMarker2.rollbackTo();
        }
        marker1.done(RakuElementTypes.STRING_LITERAL);
        return true;
    }

    private boolean rxstopper_163(PsiBuilder builder) {
        OPP opp;
        opp = null;
        if (!(this.stopper_223(builder))) {
            return false;
        }
        return true;
    }

    private boolean rxtermish_164_quant_1(PsiBuilder builder, OPP opp) {
        if (!(this.quantified_atom_124(builder))) {
            return false;
        }
        return true;
    }

    private boolean rxtermish_164(PsiBuilder builder) {
        OPP opp;
        opp = null;
        while (true) {
            PsiBuilder.Marker quantMarker1;
            quantMarker1 = builder.mark();
            if (this.rxtermish_164_quant_1(builder, opp)) {
                quantMarker1.drop();
            } else {
                quantMarker1.rollbackTo();
                break;
            }
        }
        return true;
    }

    private boolean rxws_165_alt_1(PsiBuilder builder, OPP opp) {
        if (!(this.plaincomment_97(builder))) {
            return false;
        }
        return true;
    }

    private boolean rxws_165_alt_2(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.UNV_WHITE_SPACE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean rxws_165_alt_3(PsiBuilder builder, OPP opp) {
        if (!(this.vws_257(builder))) {
            return false;
        }
        return true;
    }

    private boolean rxws_165_alt_4(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.WHITE_SPACE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean rxws_165_quant_5(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker4;
        altMarker4 = builder.mark();
        if (this.rxws_165_alt_4(builder, opp)) {
            altMarker4.drop();
        } else {
            altMarker4.rollbackTo();
            PsiBuilder.Marker altMarker3;
            altMarker3 = builder.mark();
            if (this.rxws_165_alt_3(builder, opp)) {
                altMarker3.drop();
            } else {
                altMarker3.rollbackTo();
                PsiBuilder.Marker altMarker2;
                altMarker2 = builder.mark();
                if (this.rxws_165_alt_2(builder, opp)) {
                    altMarker2.drop();
                } else {
                    altMarker2.rollbackTo();
                    PsiBuilder.Marker altMarker1;
                    altMarker1 = builder.mark();
                    if (this.rxws_165_alt_1(builder, opp)) {
                        altMarker1.drop();
                    } else {
                        altMarker1.rollbackTo();
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean rxws_165(PsiBuilder builder) {
        OPP opp;
        opp = null;
        while (true) {
            PsiBuilder.Marker quantMarker5;
            quantMarker5 = builder.mark();
            if (this.rxws_165_quant_5(builder, opp)) {
                quantMarker5.drop();
            } else {
                quantMarker5.rollbackTo();
                break;
            }
        }
        return true;
    }

    private boolean scope_declarator_166_alt_1(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.INCOMPLETE_SCOPED_DECLARATION) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean scope_declarator_166_alt_2(PsiBuilder builder, OPP opp) {
        if (!(this.multi_declarator_76(builder))) {
            return false;
        }
        return true;
    }

    private boolean scope_declarator_166_quant_3(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean scope_declarator_166_quant_4(PsiBuilder builder, OPP opp) {
        if (!(this.typename_249(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker4;
        quantMarker4 = builder.mark();
        if (this.scope_declarator_166_quant_3(builder, opp)) {
            quantMarker4.drop();
        } else {
            quantMarker4.rollbackTo();
        }
        return true;
    }

    private boolean scope_declarator_166_quant_5(PsiBuilder builder, OPP opp) {
        if (!(this.multi_declarator_76(builder))) {
            return false;
        }
        return true;
    }

    private boolean scope_declarator_166_alt_6(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker quantMarker5;
        quantMarker5 = builder.mark();
        if (this.scope_declarator_166_quant_4(builder, opp)) {
            quantMarker5.drop();
        } else {
            quantMarker5.rollbackTo();
            return false;
        }
        while (true) {
            PsiBuilder.Marker quantMarker6;
            quantMarker6 = builder.mark();
            if (this.scope_declarator_166_quant_4(builder, opp)) {
                quantMarker6.drop();
            } else {
                quantMarker6.rollbackTo();
                break;
            }
        }
        PsiBuilder.Marker quantMarker7;
        quantMarker7 = builder.mark();
        if (this.scope_declarator_166_quant_5(builder, opp)) {
            quantMarker7.drop();
        } else {
            quantMarker7.rollbackTo();
        }
        return true;
    }

    private boolean scope_declarator_166_alt_7(PsiBuilder builder, OPP opp) {
        if (!(this.package_declarator_88(builder))) {
            return false;
        }
        return true;
    }

    private boolean scope_declarator_166_alt_8(PsiBuilder builder, OPP opp) {
        if (!(this.declarator_36(builder))) {
            return false;
        }
        return true;
    }

    private boolean scope_declarator_166(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.SCOPE_DECLARATOR) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker altMarker10;
        altMarker10 = builder.mark();
        if (this.scope_declarator_166_alt_8(builder, opp)) {
            altMarker10.drop();
        } else {
            altMarker10.rollbackTo();
            PsiBuilder.Marker altMarker9;
            altMarker9 = builder.mark();
            if (this.scope_declarator_166_alt_7(builder, opp)) {
                altMarker9.drop();
            } else {
                altMarker9.rollbackTo();
                PsiBuilder.Marker altMarker8;
                altMarker8 = builder.mark();
                if (this.scope_declarator_166_alt_6(builder, opp)) {
                    altMarker8.drop();
                } else {
                    altMarker8.rollbackTo();
                    PsiBuilder.Marker altMarker3;
                    altMarker3 = builder.mark();
                    if (this.scope_declarator_166_alt_2(builder, opp)) {
                        altMarker3.drop();
                    } else {
                        altMarker3.rollbackTo();
                        PsiBuilder.Marker altMarker2;
                        altMarker2 = builder.mark();
                        if (this.scope_declarator_166_alt_1(builder, opp)) {
                            altMarker2.drop();
                        } else {
                            altMarker2.rollbackTo();
                            return false;
                        }
                    }
                }
            }
        }
        marker1.done(RakuElementTypes.SCOPED_DECLARATION);
        return true;
    }

    private boolean semiarglist_167_quant_1(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean semiarglist_167_quant_2(PsiBuilder builder, OPP opp) {
        if (!(this.arglist_6(builder))) {
            return false;
        }
        return true;
    }

    private boolean semiarglist_167_quant_3(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean semiarglist_167_quant_4(PsiBuilder builder, OPP opp) {
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STATEMENT_TERMINATOR) && (tt1.equals(";"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker quantMarker2;
        quantMarker2 = builder.mark();
        if (this.semiarglist_167_quant_2(builder, opp)) {
            quantMarker2.drop();
        } else {
            quantMarker2.rollbackTo();
        }
        PsiBuilder.Marker quantMarker3;
        quantMarker3 = builder.mark();
        if (this.semiarglist_167_quant_3(builder, opp)) {
            quantMarker3.drop();
        } else {
            quantMarker3.rollbackTo();
        }
        return true;
    }

    private boolean semiarglist_167(PsiBuilder builder) {
        OPP opp;
        opp = null;
        if (!(this.arglist_6(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker1;
        quantMarker1 = builder.mark();
        if (this.semiarglist_167_quant_1(builder, opp)) {
            quantMarker1.drop();
        } else {
            quantMarker1.rollbackTo();
        }
        while (true) {
            PsiBuilder.Marker quantMarker4;
            quantMarker4 = builder.mark();
            if (this.semiarglist_167_quant_4(builder, opp)) {
                quantMarker4.drop();
            } else {
                quantMarker4.rollbackTo();
                break;
            }
        }
        return true;
    }

    private boolean semilist_168_quant_1(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean semilist_168_quant_2(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker2;
        marker2 = builder.mark();
        if (!(this.statement_179(builder))) {
            return false;
        }
        if (!(this.eat_terminator_42(builder))) {
            return false;
        }
        marker2.done(RakuElementTypes.STATEMENT);
        PsiBuilder.Marker quantMarker3;
        quantMarker3 = builder.mark();
        if (this.semilist_168_quant_1(builder, opp)) {
            quantMarker3.drop();
        } else {
            quantMarker3.rollbackTo();
        }
        return true;
    }

    private boolean semilist_168_alt_3(PsiBuilder builder, OPP opp) {
        while (true) {
            PsiBuilder.Marker quantMarker4;
            quantMarker4 = builder.mark();
            if (this.semilist_168_quant_2(builder, opp)) {
                quantMarker4.drop();
            } else {
                quantMarker4.rollbackTo();
                break;
            }
        }
        return true;
    }

    private boolean semilist_168_alt_4(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.SEMI_LIST_END) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean semilist_168(PsiBuilder builder) {
        OPP opp;
        opp = null;
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        PsiBuilder.Marker altMarker6;
        altMarker6 = builder.mark();
        if (this.semilist_168_alt_4(builder, opp)) {
            altMarker6.drop();
        } else {
            altMarker6.rollbackTo();
            PsiBuilder.Marker altMarker5;
            altMarker5 = builder.mark();
            if (this.semilist_168_alt_3(builder, opp)) {
                altMarker5.drop();
            } else {
                altMarker5.rollbackTo();
                return false;
            }
        }
        marker1.done(RakuElementTypes.SEMI_LIST);
        return true;
    }

    private boolean separator_169_quant_1(PsiBuilder builder, OPP opp) {
        if (!(this.quantified_atom_124(builder))) {
            return false;
        }
        return true;
    }

    private boolean separator_169(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.REGEX_QUANTIFIER) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.rxws_165(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker2;
        quantMarker2 = builder.mark();
        if (this.separator_169_quant_1(builder, opp)) {
            quantMarker2.drop();
        } else {
            quantMarker2.rollbackTo();
        }
        if (!(this.rxws_165(builder))) {
            return false;
        }
        marker1.done(RakuElementTypes.REGEX_SEPARATOR);
        return true;
    }

    private boolean sibble_170_quant_1(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.QUOTE_REGEX) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean sibble_170_alt_2(PsiBuilder builder, OPP opp) {
        if (!(this.quote_nibbler_134(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker1;
        quantMarker1 = builder.mark();
        if (this.sibble_170_quant_1(builder, opp)) {
            quantMarker1.drop();
        } else {
            quantMarker1.rollbackTo();
        }
        return true;
    }

    private boolean sibble_170_alt_3(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker3;
        marker3 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.INFIX) && (tt1.equals("="))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        marker3.done(RakuElementTypes.INFIX);
        return true;
    }

    private boolean sibble_170_alt_4(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker5;
        marker5 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.ASSIGN_METAOP) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.infixish_non_assignment_meta_58(builder))) {
            return false;
        }
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.METAOP) && (tt2.equals("="))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        marker5.done(RakuElementTypes.ASSIGN_METAOP);
        return true;
    }

    private boolean sibble_170_quant_5(PsiBuilder builder, OPP opp) {
        if (!(this.EXPR_2(builder))) {
            return false;
        }
        return true;
    }

    private boolean sibble_170_quant_6(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker7;
        quantMarker7 = builder.mark();
        if (this.sibble_170_quant_5(builder, opp)) {
            quantMarker7.drop();
        } else {
            quantMarker7.rollbackTo();
        }
        return true;
    }

    private boolean sibble_170_quant_7(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker6;
        altMarker6 = builder.mark();
        if (this.sibble_170_alt_4(builder, opp)) {
            altMarker6.drop();
        } else {
            altMarker6.rollbackTo();
            PsiBuilder.Marker altMarker4;
            altMarker4 = builder.mark();
            if (this.sibble_170_alt_3(builder, opp)) {
                altMarker4.drop();
            } else {
                altMarker4.rollbackTo();
                return false;
            }
        }
        PsiBuilder.Marker quantMarker8;
        quantMarker8 = builder.mark();
        if (this.sibble_170_quant_6(builder, opp)) {
            quantMarker8.drop();
        } else {
            quantMarker8.rollbackTo();
        }
        return true;
    }

    private boolean sibble_170_alt_8(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.SUBST_ASSIGNISH) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker9;
        quantMarker9 = builder.mark();
        if (this.sibble_170_quant_7(builder, opp)) {
            quantMarker9.drop();
        } else {
            quantMarker9.rollbackTo();
        }
        return true;
    }

    private boolean sibble_170_quant_9(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker10;
        altMarker10 = builder.mark();
        if (this.sibble_170_alt_8(builder, opp)) {
            altMarker10.drop();
        } else {
            altMarker10.rollbackTo();
            PsiBuilder.Marker altMarker2;
            altMarker2 = builder.mark();
            if (this.sibble_170_alt_2(builder, opp)) {
                altMarker2.drop();
            } else {
                altMarker2.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean sibble_170_quant_10(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.QUOTE_REGEX) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker quantMarker11;
        quantMarker11 = builder.mark();
        if (this.sibble_170_quant_9(builder, opp)) {
            quantMarker11.drop();
        } else {
            quantMarker11.rollbackTo();
        }
        return true;
    }

    private boolean sibble_170(PsiBuilder builder) {
        OPP opp;
        opp = null;
        if ((builder.getTokenType()) == RakuTokenTypes.QUOTE_REGEX) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.enter_regex_nibbler_45(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker12;
        quantMarker12 = builder.mark();
        if (this.sibble_170_quant_10(builder, opp)) {
            quantMarker12.drop();
        } else {
            quantMarker12.rollbackTo();
        }
        return true;
    }

    private boolean sigil_171(PsiBuilder builder) {
        OPP opp;
        opp = null;
        return true;
    }

    private boolean sigmaybe_172_alt_1(PsiBuilder builder, OPP opp) {
        if (!(this.normspace_82(builder))) {
            return false;
        }
        return true;
    }

    private boolean sigmaybe_172_alt_2(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker2;
        marker2 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.REGEX_SIGSPACE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.normspace_82(builder))) {
            return false;
        }
        marker2.done(RakuElementTypes.REGEX_SIGSPACE);
        return true;
    }

    private boolean sigmaybe_172(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker3;
        altMarker3 = builder.mark();
        if (this.sigmaybe_172_alt_2(builder, opp)) {
            altMarker3.drop();
        } else {
            altMarker3.rollbackTo();
            PsiBuilder.Marker altMarker1;
            altMarker1 = builder.mark();
            if (this.sigmaybe_172_alt_1(builder, opp)) {
                altMarker1.drop();
            } else {
                altMarker1.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean sign_173_alt_1(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean sign_173_alt_2(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean sign_173_alt_3(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean sign_173_alt_4(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean sign_173(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker4;
        altMarker4 = builder.mark();
        if (this.sign_173_alt_4(builder, opp)) {
            altMarker4.drop();
        } else {
            altMarker4.rollbackTo();
            PsiBuilder.Marker altMarker3;
            altMarker3 = builder.mark();
            if (this.sign_173_alt_3(builder, opp)) {
                altMarker3.drop();
            } else {
                altMarker3.rollbackTo();
                PsiBuilder.Marker altMarker2;
                altMarker2 = builder.mark();
                if (this.sign_173_alt_2(builder, opp)) {
                    altMarker2.drop();
                } else {
                    altMarker2.rollbackTo();
                    PsiBuilder.Marker altMarker1;
                    altMarker1 = builder.mark();
                    if (this.sign_173_alt_1(builder, opp)) {
                        altMarker1.drop();
                    } else {
                        altMarker1.rollbackTo();
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean signature_174_alt_1(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean signature_174_alt_2(PsiBuilder builder, OPP opp) {
        if (!(this.parameter_94(builder))) {
            return false;
        }
        return true;
    }

    private boolean signature_174_alt_3(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.END_OF_PARAMETERS) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean signature_174_quant_4(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker3;
        altMarker3 = builder.mark();
        if (this.signature_174_alt_3(builder, opp)) {
            altMarker3.drop();
        } else {
            altMarker3.rollbackTo();
            PsiBuilder.Marker altMarker2;
            altMarker2 = builder.mark();
            if (this.signature_174_alt_2(builder, opp)) {
                altMarker2.drop();
            } else {
                altMarker2.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean signature_174_quant_5(PsiBuilder builder, OPP opp) {
        if (!(this.param_sep_91(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker4;
        quantMarker4 = builder.mark();
        if (this.signature_174_quant_4(builder, opp)) {
            quantMarker4.drop();
        } else {
            quantMarker4.rollbackTo();
        }
        return true;
    }

    private boolean signature_174_alt_6(PsiBuilder builder, OPP opp) {
        if (!(this.parameter_94(builder))) {
            return false;
        }
        while (true) {
            PsiBuilder.Marker quantMarker5;
            quantMarker5 = builder.mark();
            if (this.signature_174_quant_5(builder, opp)) {
                quantMarker5.drop();
            } else {
                quantMarker5.rollbackTo();
                break;
            }
        }
        return true;
    }

    private boolean signature_174_alt_7(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.END_OF_PARAMETERS) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean signature_174_quant_8(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean signature_174_alt_9(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.MISSING_RETURN_CONSTRAINT) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean signature_174_alt_10(PsiBuilder builder, OPP opp) {
        if (!(this.value_252(builder))) {
            return false;
        }
        return true;
    }

    private boolean signature_174_alt_11(PsiBuilder builder, OPP opp) {
        if (!(this.typename_249(builder))) {
            return false;
        }
        return true;
    }

    private boolean signature_174_quant_12(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean signature_174_alt_13(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker12;
        altMarker12 = builder.mark();
        if (this.signature_174_alt_11(builder, opp)) {
            altMarker12.drop();
        } else {
            altMarker12.rollbackTo();
            PsiBuilder.Marker altMarker11;
            altMarker11 = builder.mark();
            if (this.signature_174_alt_10(builder, opp)) {
                altMarker11.drop();
            } else {
                altMarker11.rollbackTo();
                return false;
            }
        }
        PsiBuilder.Marker quantMarker13;
        quantMarker13 = builder.mark();
        if (this.signature_174_quant_12(builder, opp)) {
            quantMarker13.drop();
        } else {
            quantMarker13.rollbackTo();
        }
        return true;
    }

    private boolean signature_174_quant_14(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker9;
        marker9 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.RETURN_ARROW) && (tt1.equals("-->"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker altMarker14;
        altMarker14 = builder.mark();
        if (this.signature_174_alt_13(builder, opp)) {
            altMarker14.drop();
        } else {
            altMarker14.rollbackTo();
            PsiBuilder.Marker altMarker10;
            altMarker10 = builder.mark();
            if (this.signature_174_alt_9(builder, opp)) {
                altMarker10.drop();
            } else {
                altMarker10.rollbackTo();
                return false;
            }
        }
        marker9.done(RakuElementTypes.RETURN_CONSTRAINT);
        return true;
    }

    private boolean signature_174(PsiBuilder builder) {
        OPP opp;
        opp = null;
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker altMarker7;
        altMarker7 = builder.mark();
        if (this.signature_174_alt_7(builder, opp)) {
            altMarker7.drop();
        } else {
            altMarker7.rollbackTo();
            PsiBuilder.Marker altMarker6;
            altMarker6 = builder.mark();
            if (this.signature_174_alt_6(builder, opp)) {
                altMarker6.drop();
            } else {
                altMarker6.rollbackTo();
                PsiBuilder.Marker altMarker1;
                altMarker1 = builder.mark();
                if (this.signature_174_alt_1(builder, opp)) {
                    altMarker1.drop();
                } else {
                    altMarker1.rollbackTo();
                    return false;
                }
            }
        }
        PsiBuilder.Marker quantMarker8;
        quantMarker8 = builder.mark();
        if (this.signature_174_quant_8(builder, opp)) {
            quantMarker8.drop();
        } else {
            quantMarker8.rollbackTo();
        }
        PsiBuilder.Marker quantMarker15;
        quantMarker15 = builder.mark();
        if (this.signature_174_quant_14(builder, opp)) {
            quantMarker15.drop();
        } else {
            quantMarker15.rollbackTo();
        }
        return true;
    }

    private boolean signed_integer_175(PsiBuilder builder) {
        OPP opp;
        opp = null;
        if ((builder.getTokenType()) == RakuTokenTypes.RAT_LITERAL) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.integer_61(builder))) {
            return false;
        }
        return true;
    }

    private boolean signed_number_176(PsiBuilder builder) {
        OPP opp;
        opp = null;
        if ((builder.getTokenType()) == RakuTokenTypes.COMPLEX_LITERAL) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.number_83(builder))) {
            return false;
        }
        return true;
    }

    private boolean spacey_177_alt_1(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean spacey_177_alt_2(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean spacey_177(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker2;
        altMarker2 = builder.mark();
        if (this.spacey_177_alt_2(builder, opp)) {
            altMarker2.drop();
        } else {
            altMarker2.rollbackTo();
            PsiBuilder.Marker altMarker1;
            altMarker1 = builder.mark();
            if (this.spacey_177_alt_1(builder, opp)) {
                altMarker1.drop();
            } else {
                altMarker1.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean starter_178(PsiBuilder builder) {
        OPP opp;
        opp = null;
        return true;
    }

    private boolean statement_179_alt_1(PsiBuilder builder, OPP opp) {
        if (!(this.bogus_statement_19(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_179_alt_2(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean statement_179_alt_3(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean statement_179_alt_4(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker3;
        altMarker3 = builder.mark();
        if (this.statement_179_alt_3(builder, opp)) {
            altMarker3.drop();
        } else {
            altMarker3.rollbackTo();
            PsiBuilder.Marker altMarker2;
            altMarker2 = builder.mark();
            if (this.statement_179_alt_2(builder, opp)) {
                altMarker2.drop();
            } else {
                altMarker2.rollbackTo();
                return false;
            }
        }
        if ((builder.getTokenType()) == RakuTokenTypes.EMPTY_STATEMENT) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean statement_179_alt_5(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        if (!(this.statement_mod_loop_203(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_179_quant_6(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        if (!(this.statement_mod_loop_203(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_179_alt_7(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        if (!(this.statement_mod_cond_201(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker6;
        quantMarker6 = builder.mark();
        if (this.statement_179_quant_6(builder, opp)) {
            quantMarker6.drop();
        } else {
            quantMarker6.rollbackTo();
        }
        return true;
    }

    private boolean statement_179_alt_8(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.END_OF_STATEMENT) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean statement_179_quant_9(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker8;
        altMarker8 = builder.mark();
        if (this.statement_179_alt_8(builder, opp)) {
            altMarker8.drop();
        } else {
            altMarker8.rollbackTo();
            PsiBuilder.Marker altMarker7;
            altMarker7 = builder.mark();
            if (this.statement_179_alt_7(builder, opp)) {
                altMarker7.drop();
            } else {
                altMarker7.rollbackTo();
                PsiBuilder.Marker altMarker5;
                altMarker5 = builder.mark();
                if (this.statement_179_alt_5(builder, opp)) {
                    altMarker5.drop();
                } else {
                    altMarker5.rollbackTo();
                    return false;
                }
            }
        }
        return true;
    }

    private boolean statement_179_alt_10(PsiBuilder builder, OPP opp) {
        if (!(this.EXPR_2(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker9;
        quantMarker9 = builder.mark();
        if (this.statement_179_quant_9(builder, opp)) {
            quantMarker9.drop();
        } else {
            quantMarker9.rollbackTo();
        }
        return true;
    }

    private boolean statement_179_alt_11(PsiBuilder builder, OPP opp) {
        if (!(this.statement_control_180(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_179_quant_12(PsiBuilder builder, OPP opp) {
        if (!(this.statement_179(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_179_alt_13(PsiBuilder builder, OPP opp) {
        if (!(this.label_65(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker12;
        quantMarker12 = builder.mark();
        if (this.statement_179_quant_12(builder, opp)) {
            quantMarker12.drop();
        } else {
            quantMarker12.rollbackTo();
        }
        return true;
    }

    private boolean statement_179(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker13;
        altMarker13 = builder.mark();
        if (this.statement_179_alt_13(builder, opp)) {
            altMarker13.drop();
        } else {
            altMarker13.rollbackTo();
            PsiBuilder.Marker altMarker11;
            altMarker11 = builder.mark();
            if (this.statement_179_alt_11(builder, opp)) {
                altMarker11.drop();
            } else {
                altMarker11.rollbackTo();
                PsiBuilder.Marker altMarker10;
                altMarker10 = builder.mark();
                if (this.statement_179_alt_10(builder, opp)) {
                    altMarker10.drop();
                } else {
                    altMarker10.rollbackTo();
                    PsiBuilder.Marker altMarker4;
                    altMarker4 = builder.mark();
                    if (this.statement_179_alt_4(builder, opp)) {
                        altMarker4.drop();
                    } else {
                        altMarker4.rollbackTo();
                        PsiBuilder.Marker altMarker1;
                        altMarker1 = builder.mark();
                        if (this.statement_179_alt_1(builder, opp)) {
                            altMarker1.drop();
                        } else {
                            altMarker1.rollbackTo();
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean statement_control_180_alt_1(PsiBuilder builder, OPP opp) {
        if (!(this.statement_control_QUIT_183(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_180_alt_2(PsiBuilder builder, OPP opp) {
        if (!(this.statement_control_CONTROL_182(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_180_alt_3(PsiBuilder builder, OPP opp) {
        if (!(this.statement_control_CATCH_181(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_180_alt_4(PsiBuilder builder, OPP opp) {
        if (!(this.statement_control_default_184(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_180_alt_5(PsiBuilder builder, OPP opp) {
        if (!(this.statement_control_when_197(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_180_alt_6(PsiBuilder builder, OPP opp) {
        if (!(this.statement_control_given_186(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_180_alt_7(PsiBuilder builder, OPP opp) {
        if (!(this.statement_control_require_193(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_180_alt_8(PsiBuilder builder, OPP opp) {
        if (!(this.statement_control_use_196(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_180_alt_9(PsiBuilder builder, OPP opp) {
        if (!(this.statement_control_no_191(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_180_alt_10(PsiBuilder builder, OPP opp) {
        if (!(this.statement_control_import_188(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_180_alt_11(PsiBuilder builder, OPP opp) {
        if (!(this.statement_control_need_190(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_180_alt_12(PsiBuilder builder, OPP opp) {
        if (!(this.statement_control_loop_189(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_180_alt_13(PsiBuilder builder, OPP opp) {
        if (!(this.statement_control_whenever_198(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_180_alt_14(PsiBuilder builder, OPP opp) {
        if (!(this.statement_control_for_185(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_180_alt_15(PsiBuilder builder, OPP opp) {
        if (!(this.statement_control_repeat_192(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_180_alt_16(PsiBuilder builder, OPP opp) {
        if (!(this.statement_control_until_195(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_180_alt_17(PsiBuilder builder, OPP opp) {
        if (!(this.statement_control_while_199(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_180_alt_18(PsiBuilder builder, OPP opp) {
        if (!(this.statement_control_without_200(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_180_alt_19(PsiBuilder builder, OPP opp) {
        if (!(this.statement_control_unless_194(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_180_alt_20(PsiBuilder builder, OPP opp) {
        if (!(this.statement_control_if_187(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_180(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker20;
        altMarker20 = builder.mark();
        if (this.statement_control_180_alt_20(builder, opp)) {
            altMarker20.drop();
        } else {
            altMarker20.rollbackTo();
            PsiBuilder.Marker altMarker19;
            altMarker19 = builder.mark();
            if (this.statement_control_180_alt_19(builder, opp)) {
                altMarker19.drop();
            } else {
                altMarker19.rollbackTo();
                PsiBuilder.Marker altMarker18;
                altMarker18 = builder.mark();
                if (this.statement_control_180_alt_18(builder, opp)) {
                    altMarker18.drop();
                } else {
                    altMarker18.rollbackTo();
                    PsiBuilder.Marker altMarker17;
                    altMarker17 = builder.mark();
                    if (this.statement_control_180_alt_17(builder, opp)) {
                        altMarker17.drop();
                    } else {
                        altMarker17.rollbackTo();
                        PsiBuilder.Marker altMarker16;
                        altMarker16 = builder.mark();
                        if (this.statement_control_180_alt_16(builder, opp)) {
                            altMarker16.drop();
                        } else {
                            altMarker16.rollbackTo();
                            PsiBuilder.Marker altMarker15;
                            altMarker15 = builder.mark();
                            if (this.statement_control_180_alt_15(builder, opp)) {
                                altMarker15.drop();
                            } else {
                                altMarker15.rollbackTo();
                                PsiBuilder.Marker altMarker14;
                                altMarker14 = builder.mark();
                                if (this.statement_control_180_alt_14(builder, opp)) {
                                    altMarker14.drop();
                                } else {
                                    altMarker14.rollbackTo();
                                    PsiBuilder.Marker altMarker13;
                                    altMarker13 = builder.mark();
                                    if (this.statement_control_180_alt_13(builder, opp)) {
                                        altMarker13.drop();
                                    } else {
                                        altMarker13.rollbackTo();
                                        PsiBuilder.Marker altMarker12;
                                        altMarker12 = builder.mark();
                                        if (this.statement_control_180_alt_12(builder, opp)) {
                                            altMarker12.drop();
                                        } else {
                                            altMarker12.rollbackTo();
                                            PsiBuilder.Marker altMarker11;
                                            altMarker11 = builder.mark();
                                            if (this.statement_control_180_alt_11(builder, opp)) {
                                                altMarker11.drop();
                                            } else {
                                                altMarker11.rollbackTo();
                                                PsiBuilder.Marker altMarker10;
                                                altMarker10 = builder.mark();
                                                if (this.statement_control_180_alt_10(builder, opp)) {
                                                    altMarker10.drop();
                                                } else {
                                                    altMarker10.rollbackTo();
                                                    PsiBuilder.Marker altMarker9;
                                                    altMarker9 = builder.mark();
                                                    if (this.statement_control_180_alt_9(builder, opp)) {
                                                        altMarker9.drop();
                                                    } else {
                                                        altMarker9.rollbackTo();
                                                        PsiBuilder.Marker altMarker8;
                                                        altMarker8 = builder.mark();
                                                        if (this.statement_control_180_alt_8(builder, opp)) {
                                                            altMarker8.drop();
                                                        } else {
                                                            altMarker8.rollbackTo();
                                                            PsiBuilder.Marker altMarker7;
                                                            altMarker7 = builder.mark();
                                                            if (this.statement_control_180_alt_7(builder, opp)) {
                                                                altMarker7.drop();
                                                            } else {
                                                                altMarker7.rollbackTo();
                                                                PsiBuilder.Marker altMarker6;
                                                                altMarker6 = builder.mark();
                                                                if (this.statement_control_180_alt_6(builder, opp)) {
                                                                    altMarker6.drop();
                                                                } else {
                                                                    altMarker6.rollbackTo();
                                                                    PsiBuilder.Marker altMarker5;
                                                                    altMarker5 = builder.mark();
                                                                    if (this.statement_control_180_alt_5(builder, opp)) {
                                                                        altMarker5.drop();
                                                                    } else {
                                                                        altMarker5.rollbackTo();
                                                                        PsiBuilder.Marker altMarker4;
                                                                        altMarker4 = builder.mark();
                                                                        if (this.statement_control_180_alt_4(builder, opp)) {
                                                                            altMarker4.drop();
                                                                        } else {
                                                                            altMarker4.rollbackTo();
                                                                            PsiBuilder.Marker altMarker3;
                                                                            altMarker3 = builder.mark();
                                                                            if (this.statement_control_180_alt_3(builder, opp)) {
                                                                                altMarker3.drop();
                                                                            } else {
                                                                                altMarker3.rollbackTo();
                                                                                PsiBuilder.Marker altMarker2;
                                                                                altMarker2 = builder.mark();
                                                                                if (this.statement_control_180_alt_2(builder, opp)) {
                                                                                    altMarker2.drop();
                                                                                } else {
                                                                                    altMarker2.rollbackTo();
                                                                                    PsiBuilder.Marker altMarker1;
                                                                                    altMarker1 = builder.mark();
                                                                                    if (this.statement_control_180_alt_1(builder, opp)) {
                                                                                        altMarker1.drop();
                                                                                    } else {
                                                                                        altMarker1.rollbackTo();
                                                                                        return false;
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean statement_control_CATCH_181_quant_1(PsiBuilder builder, OPP opp) {
        if (!(this.block_15(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_CATCH_181(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STATEMENT_CONTROL) && (tt1.equals("CATCH"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.kok_64(builder))) {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker2;
        quantMarker2 = builder.mark();
        if (this.statement_control_CATCH_181_quant_1(builder, opp)) {
            quantMarker2.drop();
        } else {
            quantMarker2.rollbackTo();
        }
        marker1.done(RakuElementTypes.CATCH_STATEMENT);
        return true;
    }

    private boolean statement_control_CONTROL_182_quant_1(PsiBuilder builder, OPP opp) {
        if (!(this.block_15(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_CONTROL_182(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STATEMENT_CONTROL) && (tt1.equals("CONTROL"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.kok_64(builder))) {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker2;
        quantMarker2 = builder.mark();
        if (this.statement_control_CONTROL_182_quant_1(builder, opp)) {
            quantMarker2.drop();
        } else {
            quantMarker2.rollbackTo();
        }
        marker1.done(RakuElementTypes.CONTROL_STATEMENT);
        return true;
    }

    private boolean statement_control_QUIT_183_quant_1(PsiBuilder builder, OPP opp) {
        if (!(this.block_15(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_QUIT_183(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STATEMENT_CONTROL) && (tt1.equals("QUIT"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.kok_64(builder))) {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker2;
        quantMarker2 = builder.mark();
        if (this.statement_control_QUIT_183_quant_1(builder, opp)) {
            quantMarker2.drop();
        } else {
            quantMarker2.rollbackTo();
        }
        marker1.done(RakuElementTypes.QUIT_STATEMENT);
        return true;
    }

    private boolean statement_control_default_184_quant_1(PsiBuilder builder, OPP opp) {
        if (!(this.block_15(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_default_184(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STATEMENT_CONTROL) && (tt1.equals("default"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.kok_64(builder))) {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker2;
        quantMarker2 = builder.mark();
        if (this.statement_control_default_184_quant_1(builder, opp)) {
            quantMarker2.drop();
        } else {
            quantMarker2.rollbackTo();
        }
        marker1.done(RakuElementTypes.DEFAULT_STATEMENT);
        return true;
    }

    private boolean statement_control_for_185_quant_1(PsiBuilder builder, OPP opp) {
        if (!(this.xblock_259(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_for_185(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STATEMENT_CONTROL) && (tt1.equals("for"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.kok_64(builder))) {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker2;
        quantMarker2 = builder.mark();
        if (this.statement_control_for_185_quant_1(builder, opp)) {
            quantMarker2.drop();
        } else {
            quantMarker2.rollbackTo();
        }
        marker1.done(RakuElementTypes.FOR_STATEMENT);
        return true;
    }

    private boolean statement_control_given_186_quant_1(PsiBuilder builder, OPP opp) {
        if (!(this.xblock_259(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_given_186(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STATEMENT_CONTROL) && (tt1.equals("given"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.kok_64(builder))) {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker2;
        quantMarker2 = builder.mark();
        if (this.statement_control_given_186_quant_1(builder, opp)) {
            quantMarker2.drop();
        } else {
            quantMarker2.rollbackTo();
        }
        marker1.done(RakuElementTypes.GIVEN_STATEMENT);
        return true;
    }

    private boolean statement_control_if_187_alt_1(PsiBuilder builder, OPP opp) {
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STATEMENT_CONTROL) && (tt1.equals("with"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean statement_control_if_187_alt_2(PsiBuilder builder, OPP opp) {
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STATEMENT_CONTROL) && (tt2.equals("if"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean statement_control_if_187_quant_3(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_if_187_alt_4(PsiBuilder builder, OPP opp) {
        String tt3;
        tt3 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STATEMENT_CONTROL) && (tt3.equals("orwith"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean statement_control_if_187_alt_5(PsiBuilder builder, OPP opp) {
        String tt4;
        tt4 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STATEMENT_CONTROL) && (tt4.equals("elsif"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean statement_control_if_187_quant_6(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_if_187_quant_7(PsiBuilder builder, OPP opp) {
        if (!(this.xblock_259(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker7;
        quantMarker7 = builder.mark();
        if (this.statement_control_if_187_quant_6(builder, opp)) {
            quantMarker7.drop();
        } else {
            quantMarker7.rollbackTo();
        }
        return true;
    }

    private boolean statement_control_if_187_quant_8(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker6;
        altMarker6 = builder.mark();
        if (this.statement_control_if_187_alt_5(builder, opp)) {
            altMarker6.drop();
        } else {
            altMarker6.rollbackTo();
            PsiBuilder.Marker altMarker5;
            altMarker5 = builder.mark();
            if (this.statement_control_if_187_alt_4(builder, opp)) {
                altMarker5.drop();
            } else {
                altMarker5.rollbackTo();
                return false;
            }
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker8;
        quantMarker8 = builder.mark();
        if (this.statement_control_if_187_quant_7(builder, opp)) {
            quantMarker8.drop();
        } else {
            quantMarker8.rollbackTo();
        }
        return true;
    }

    private boolean statement_control_if_187_quant_9(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_if_187_quant_10(PsiBuilder builder, OPP opp) {
        if (!(this.pblock_95(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker10;
        quantMarker10 = builder.mark();
        if (this.statement_control_if_187_quant_9(builder, opp)) {
            quantMarker10.drop();
        } else {
            quantMarker10.rollbackTo();
        }
        return true;
    }

    private boolean statement_control_if_187_quant_11(PsiBuilder builder, OPP opp) {
        String tt5;
        tt5 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STATEMENT_CONTROL) && (tt5.equals("else"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker11;
        quantMarker11 = builder.mark();
        if (this.statement_control_if_187_quant_10(builder, opp)) {
            quantMarker11.drop();
        } else {
            quantMarker11.rollbackTo();
        }
        return true;
    }

    private boolean statement_control_if_187_quant_12(PsiBuilder builder, OPP opp) {
        while (true) {
            PsiBuilder.Marker quantMarker9;
            quantMarker9 = builder.mark();
            if (this.statement_control_if_187_quant_8(builder, opp)) {
                quantMarker9.drop();
            } else {
                quantMarker9.rollbackTo();
                break;
            }
        }
        PsiBuilder.Marker quantMarker12;
        quantMarker12 = builder.mark();
        if (this.statement_control_if_187_quant_11(builder, opp)) {
            quantMarker12.drop();
        } else {
            quantMarker12.rollbackTo();
        }
        return true;
    }

    private boolean statement_control_if_187_quant_13(PsiBuilder builder, OPP opp) {
        if (!(this.xblock_259(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker4;
        quantMarker4 = builder.mark();
        if (this.statement_control_if_187_quant_3(builder, opp)) {
            quantMarker4.drop();
        } else {
            quantMarker4.rollbackTo();
        }
        PsiBuilder.Marker quantMarker13;
        quantMarker13 = builder.mark();
        if (this.statement_control_if_187_quant_12(builder, opp)) {
            quantMarker13.drop();
        } else {
            quantMarker13.rollbackTo();
        }
        return true;
    }

    private boolean statement_control_if_187(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        PsiBuilder.Marker altMarker3;
        altMarker3 = builder.mark();
        if (this.statement_control_if_187_alt_2(builder, opp)) {
            altMarker3.drop();
        } else {
            altMarker3.rollbackTo();
            PsiBuilder.Marker altMarker2;
            altMarker2 = builder.mark();
            if (this.statement_control_if_187_alt_1(builder, opp)) {
                altMarker2.drop();
            } else {
                altMarker2.rollbackTo();
                return false;
            }
        }
        if (!(this.kok_64(builder))) {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker14;
        quantMarker14 = builder.mark();
        if (this.statement_control_if_187_quant_13(builder, opp)) {
            quantMarker14.drop();
        } else {
            quantMarker14.rollbackTo();
        }
        marker1.done(RakuElementTypes.IF_STATEMENT);
        return true;
    }

    private boolean statement_control_import_188_quant_1(PsiBuilder builder, OPP opp) {
        if (!(this.spacey_177(builder))) {
            return false;
        }
        if (!(this.arglist_6(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_import_188_quant_2(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_import_188_quant_3(PsiBuilder builder, OPP opp) {
        if (!(this.module_name_74(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker2;
        quantMarker2 = builder.mark();
        if (this.statement_control_import_188_quant_1(builder, opp)) {
            quantMarker2.drop();
        } else {
            quantMarker2.rollbackTo();
        }
        PsiBuilder.Marker quantMarker3;
        quantMarker3 = builder.mark();
        if (this.statement_control_import_188_quant_2(builder, opp)) {
            quantMarker3.drop();
        } else {
            quantMarker3.rollbackTo();
        }
        return true;
    }

    private boolean statement_control_import_188(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STATEMENT_CONTROL) && (tt1.equals("import"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker4;
        quantMarker4 = builder.mark();
        if (this.statement_control_import_188_quant_3(builder, opp)) {
            quantMarker4.drop();
        } else {
            quantMarker4.rollbackTo();
        }
        marker1.done(RakuElementTypes.IMPORT_STATEMENT);
        return true;
    }

    private boolean statement_control_loop_189_quant_1(PsiBuilder builder, OPP opp) {
        if (!(this.EXPR_2(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_loop_189_quant_2(PsiBuilder builder, OPP opp) {
        if (!(this.EXPR_2(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_loop_189_quant_3(PsiBuilder builder, OPP opp) {
        if (!(this.EXPR_2(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_loop_189_quant_4(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_loop_189_quant_5(PsiBuilder builder, OPP opp) {
        String tt4;
        tt4 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STATEMENT_TERMINATOR) && (tt4.equals(";"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker4;
        quantMarker4 = builder.mark();
        if (this.statement_control_loop_189_quant_3(builder, opp)) {
            quantMarker4.drop();
        } else {
            quantMarker4.rollbackTo();
        }
        PsiBuilder.Marker quantMarker5;
        quantMarker5 = builder.mark();
        if (this.statement_control_loop_189_quant_4(builder, opp)) {
            quantMarker5.drop();
        } else {
            quantMarker5.rollbackTo();
        }
        return true;
    }

    private boolean statement_control_loop_189_quant_6(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker6;
        quantMarker6 = builder.mark();
        if (this.statement_control_loop_189_quant_5(builder, opp)) {
            quantMarker6.drop();
        } else {
            quantMarker6.rollbackTo();
        }
        return true;
    }

    private boolean statement_control_loop_189_quant_7(PsiBuilder builder, OPP opp) {
        String tt3;
        tt3 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STATEMENT_TERMINATOR) && (tt3.equals(";"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker3;
        quantMarker3 = builder.mark();
        if (this.statement_control_loop_189_quant_2(builder, opp)) {
            quantMarker3.drop();
        } else {
            quantMarker3.rollbackTo();
        }
        PsiBuilder.Marker quantMarker7;
        quantMarker7 = builder.mark();
        if (this.statement_control_loop_189_quant_6(builder, opp)) {
            quantMarker7.drop();
        } else {
            quantMarker7.rollbackTo();
        }
        return true;
    }

    private boolean statement_control_loop_189_quant_8(PsiBuilder builder, OPP opp) {
        String tt5;
        tt5 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.PARENTHESES_CLOSE) && (tt5.equals(")"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean statement_control_loop_189_quant_9(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_loop_189_quant_10(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker8;
        quantMarker8 = builder.mark();
        if (this.statement_control_loop_189_quant_7(builder, opp)) {
            quantMarker8.drop();
        } else {
            quantMarker8.rollbackTo();
        }
        PsiBuilder.Marker quantMarker9;
        quantMarker9 = builder.mark();
        if (this.statement_control_loop_189_quant_8(builder, opp)) {
            quantMarker9.drop();
        } else {
            quantMarker9.rollbackTo();
        }
        PsiBuilder.Marker quantMarker10;
        quantMarker10 = builder.mark();
        if (this.statement_control_loop_189_quant_9(builder, opp)) {
            quantMarker10.drop();
        } else {
            quantMarker10.rollbackTo();
        }
        return true;
    }

    private boolean statement_control_loop_189_quant_11(PsiBuilder builder, OPP opp) {
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.PARENTHESES_OPEN) && (tt2.equals("("))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker2;
        quantMarker2 = builder.mark();
        if (this.statement_control_loop_189_quant_1(builder, opp)) {
            quantMarker2.drop();
        } else {
            quantMarker2.rollbackTo();
        }
        PsiBuilder.Marker quantMarker11;
        quantMarker11 = builder.mark();
        if (this.statement_control_loop_189_quant_10(builder, opp)) {
            quantMarker11.drop();
        } else {
            quantMarker11.rollbackTo();
        }
        return true;
    }

    private boolean statement_control_loop_189_quant_12(PsiBuilder builder, OPP opp) {
        if (!(this.block_15(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_loop_189(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STATEMENT_CONTROL) && (tt1.equals("loop"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.kok_64(builder))) {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker12;
        quantMarker12 = builder.mark();
        if (this.statement_control_loop_189_quant_11(builder, opp)) {
            quantMarker12.drop();
        } else {
            quantMarker12.rollbackTo();
        }
        PsiBuilder.Marker quantMarker13;
        quantMarker13 = builder.mark();
        if (this.statement_control_loop_189_quant_12(builder, opp)) {
            quantMarker13.drop();
        } else {
            quantMarker13.rollbackTo();
        }
        marker1.done(RakuElementTypes.LOOP_STATEMENT);
        return true;
    }

    private boolean statement_control_need_190_alt_1(PsiBuilder builder, OPP opp) {
        if (!(this.module_name_74(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_need_190_alt_2(PsiBuilder builder, OPP opp) {
        if (!(this.version_255(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_need_190_quant_3(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_need_190_alt_4(PsiBuilder builder, OPP opp) {
        if (!(this.module_name_74(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_need_190_alt_5(PsiBuilder builder, OPP opp) {
        if (!(this.version_255(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_need_190_quant_6(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker6;
        altMarker6 = builder.mark();
        if (this.statement_control_need_190_alt_5(builder, opp)) {
            altMarker6.drop();
        } else {
            altMarker6.rollbackTo();
            PsiBuilder.Marker altMarker5;
            altMarker5 = builder.mark();
            if (this.statement_control_need_190_alt_4(builder, opp)) {
                altMarker5.drop();
            } else {
                altMarker5.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean statement_control_need_190_quant_7(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_need_190_quant_8(PsiBuilder builder, OPP opp) {
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.INFIX) && (tt2.equals(","))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker7;
        quantMarker7 = builder.mark();
        if (this.statement_control_need_190_quant_6(builder, opp)) {
            quantMarker7.drop();
        } else {
            quantMarker7.rollbackTo();
        }
        PsiBuilder.Marker quantMarker8;
        quantMarker8 = builder.mark();
        if (this.statement_control_need_190_quant_7(builder, opp)) {
            quantMarker8.drop();
        } else {
            quantMarker8.rollbackTo();
        }
        return true;
    }

    private boolean statement_control_need_190_quant_9(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker quantMarker4;
        quantMarker4 = builder.mark();
        if (this.statement_control_need_190_quant_3(builder, opp)) {
            quantMarker4.drop();
        } else {
            quantMarker4.rollbackTo();
        }
        while (true) {
            PsiBuilder.Marker quantMarker9;
            quantMarker9 = builder.mark();
            if (this.statement_control_need_190_quant_8(builder, opp)) {
                quantMarker9.drop();
            } else {
                quantMarker9.rollbackTo();
                break;
            }
        }
        return true;
    }

    private boolean statement_control_need_190_quant_10(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker3;
        altMarker3 = builder.mark();
        if (this.statement_control_need_190_alt_2(builder, opp)) {
            altMarker3.drop();
        } else {
            altMarker3.rollbackTo();
            PsiBuilder.Marker altMarker2;
            altMarker2 = builder.mark();
            if (this.statement_control_need_190_alt_1(builder, opp)) {
                altMarker2.drop();
            } else {
                altMarker2.rollbackTo();
                return false;
            }
        }
        PsiBuilder.Marker quantMarker10;
        quantMarker10 = builder.mark();
        if (this.statement_control_need_190_quant_9(builder, opp)) {
            quantMarker10.drop();
        } else {
            quantMarker10.rollbackTo();
        }
        return true;
    }

    private boolean statement_control_need_190(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STATEMENT_CONTROL) && (tt1.equals("need"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker11;
        quantMarker11 = builder.mark();
        if (this.statement_control_need_190_quant_10(builder, opp)) {
            quantMarker11.drop();
        } else {
            quantMarker11.rollbackTo();
        }
        marker1.done(RakuElementTypes.NEED_STATEMENT);
        return true;
    }

    private boolean statement_control_no_191_quant_1(PsiBuilder builder, OPP opp) {
        if (!(this.spacey_177(builder))) {
            return false;
        }
        if (!(this.arglist_6(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_no_191_quant_2(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_no_191_quant_3(PsiBuilder builder, OPP opp) {
        if (!(this.module_name_74(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker2;
        quantMarker2 = builder.mark();
        if (this.statement_control_no_191_quant_1(builder, opp)) {
            quantMarker2.drop();
        } else {
            quantMarker2.rollbackTo();
        }
        PsiBuilder.Marker quantMarker3;
        quantMarker3 = builder.mark();
        if (this.statement_control_no_191_quant_2(builder, opp)) {
            quantMarker3.drop();
        } else {
            quantMarker3.rollbackTo();
        }
        return true;
    }

    private boolean statement_control_no_191(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STATEMENT_CONTROL) && (tt1.equals("no"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker4;
        quantMarker4 = builder.mark();
        if (this.statement_control_no_191_quant_3(builder, opp)) {
            quantMarker4.drop();
        } else {
            quantMarker4.rollbackTo();
        }
        marker1.done(RakuElementTypes.NO_STATEMENT);
        return true;
    }

    private boolean statement_control_repeat_192_alt_1(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean statement_control_repeat_192_alt_2(PsiBuilder builder, OPP opp) {
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STATEMENT_CONTROL) && (tt2.equals("until"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean statement_control_repeat_192_alt_3(PsiBuilder builder, OPP opp) {
        String tt3;
        tt3 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STATEMENT_CONTROL) && (tt3.equals("while"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean statement_control_repeat_192_quant_4(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_repeat_192_quant_5(PsiBuilder builder, OPP opp) {
        if (!(this.EXPR_2(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_repeat_192_quant_6(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker4;
        altMarker4 = builder.mark();
        if (this.statement_control_repeat_192_alt_3(builder, opp)) {
            altMarker4.drop();
        } else {
            altMarker4.rollbackTo();
            PsiBuilder.Marker altMarker3;
            altMarker3 = builder.mark();
            if (this.statement_control_repeat_192_alt_2(builder, opp)) {
                altMarker3.drop();
            } else {
                altMarker3.rollbackTo();
                return false;
            }
        }
        PsiBuilder.Marker quantMarker5;
        quantMarker5 = builder.mark();
        if (this.statement_control_repeat_192_quant_4(builder, opp)) {
            quantMarker5.drop();
        } else {
            quantMarker5.rollbackTo();
        }
        PsiBuilder.Marker quantMarker6;
        quantMarker6 = builder.mark();
        if (this.statement_control_repeat_192_quant_5(builder, opp)) {
            quantMarker6.drop();
        } else {
            quantMarker6.rollbackTo();
        }
        return true;
    }

    private boolean statement_control_repeat_192_quant_7(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker7;
        quantMarker7 = builder.mark();
        if (this.statement_control_repeat_192_quant_6(builder, opp)) {
            quantMarker7.drop();
        } else {
            quantMarker7.rollbackTo();
        }
        return true;
    }

    private boolean statement_control_repeat_192_alt_8(PsiBuilder builder, OPP opp) {
        if (!(this.pblock_95(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker8;
        quantMarker8 = builder.mark();
        if (this.statement_control_repeat_192_quant_7(builder, opp)) {
            quantMarker8.drop();
        } else {
            quantMarker8.rollbackTo();
        }
        return true;
    }

    private boolean statement_control_repeat_192_alt_9(PsiBuilder builder, OPP opp) {
        String tt4;
        tt4 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STATEMENT_CONTROL) && (tt4.equals("until"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean statement_control_repeat_192_alt_10(PsiBuilder builder, OPP opp) {
        String tt5;
        tt5 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STATEMENT_CONTROL) && (tt5.equals("while"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean statement_control_repeat_192_quant_11(PsiBuilder builder, OPP opp) {
        if (!(this.xblock_259(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_repeat_192_quant_12(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker12;
        quantMarker12 = builder.mark();
        if (this.statement_control_repeat_192_quant_11(builder, opp)) {
            quantMarker12.drop();
        } else {
            quantMarker12.rollbackTo();
        }
        return true;
    }

    private boolean statement_control_repeat_192_alt_13(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker11;
        altMarker11 = builder.mark();
        if (this.statement_control_repeat_192_alt_10(builder, opp)) {
            altMarker11.drop();
        } else {
            altMarker11.rollbackTo();
            PsiBuilder.Marker altMarker10;
            altMarker10 = builder.mark();
            if (this.statement_control_repeat_192_alt_9(builder, opp)) {
                altMarker10.drop();
            } else {
                altMarker10.rollbackTo();
                return false;
            }
        }
        if (!(this.kok_64(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker13;
        quantMarker13 = builder.mark();
        if (this.statement_control_repeat_192_quant_12(builder, opp)) {
            quantMarker13.drop();
        } else {
            quantMarker13.rollbackTo();
        }
        return true;
    }

    private boolean statement_control_repeat_192(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STATEMENT_CONTROL) && (tt1.equals("repeat"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.kok_64(builder))) {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker altMarker14;
        altMarker14 = builder.mark();
        if (this.statement_control_repeat_192_alt_13(builder, opp)) {
            altMarker14.drop();
        } else {
            altMarker14.rollbackTo();
            PsiBuilder.Marker altMarker9;
            altMarker9 = builder.mark();
            if (this.statement_control_repeat_192_alt_8(builder, opp)) {
                altMarker9.drop();
            } else {
                altMarker9.rollbackTo();
                PsiBuilder.Marker altMarker2;
                altMarker2 = builder.mark();
                if (this.statement_control_repeat_192_alt_1(builder, opp)) {
                    altMarker2.drop();
                } else {
                    altMarker2.rollbackTo();
                    return false;
                }
            }
        }
        marker1.done(RakuElementTypes.REPEAT_STATEMENT);
        return true;
    }

    private boolean statement_control_require_193_alt_1(PsiBuilder builder, OPP opp) {
        if (!(this.term_224(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_require_193_alt_2(PsiBuilder builder, OPP opp) {
        if (!(this.variable_253(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_require_193_alt_3(PsiBuilder builder, OPP opp) {
        if (!(this.module_name_74(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_require_193_quant_4(PsiBuilder builder, OPP opp) {
        if (!(this.EXPR_2(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_require_193_quant_5(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker5;
        quantMarker5 = builder.mark();
        if (this.statement_control_require_193_quant_4(builder, opp)) {
            quantMarker5.drop();
        } else {
            quantMarker5.rollbackTo();
        }
        return true;
    }

    private boolean statement_control_require_193_quant_6(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_require_193_quant_7(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker4;
        altMarker4 = builder.mark();
        if (this.statement_control_require_193_alt_3(builder, opp)) {
            altMarker4.drop();
        } else {
            altMarker4.rollbackTo();
            PsiBuilder.Marker altMarker3;
            altMarker3 = builder.mark();
            if (this.statement_control_require_193_alt_2(builder, opp)) {
                altMarker3.drop();
            } else {
                altMarker3.rollbackTo();
                PsiBuilder.Marker altMarker2;
                altMarker2 = builder.mark();
                if (this.statement_control_require_193_alt_1(builder, opp)) {
                    altMarker2.drop();
                } else {
                    altMarker2.rollbackTo();
                    return false;
                }
            }
        }
        PsiBuilder.Marker quantMarker6;
        quantMarker6 = builder.mark();
        if (this.statement_control_require_193_quant_5(builder, opp)) {
            quantMarker6.drop();
        } else {
            quantMarker6.rollbackTo();
        }
        PsiBuilder.Marker quantMarker7;
        quantMarker7 = builder.mark();
        if (this.statement_control_require_193_quant_6(builder, opp)) {
            quantMarker7.drop();
        } else {
            quantMarker7.rollbackTo();
        }
        return true;
    }

    private boolean statement_control_require_193(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STATEMENT_CONTROL) && (tt1.equals("require"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker8;
        quantMarker8 = builder.mark();
        if (this.statement_control_require_193_quant_7(builder, opp)) {
            quantMarker8.drop();
        } else {
            quantMarker8.rollbackTo();
        }
        marker1.done(RakuElementTypes.REQUIRE_STATEMENT);
        return true;
    }

    private boolean statement_control_unless_194_quant_1(PsiBuilder builder, OPP opp) {
        if (!(this.xblock_259(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_unless_194(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STATEMENT_CONTROL) && (tt1.equals("unless"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.kok_64(builder))) {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker2;
        quantMarker2 = builder.mark();
        if (this.statement_control_unless_194_quant_1(builder, opp)) {
            quantMarker2.drop();
        } else {
            quantMarker2.rollbackTo();
        }
        marker1.done(RakuElementTypes.UNLESS_STATEMENT);
        return true;
    }

    private boolean statement_control_until_195_quant_1(PsiBuilder builder, OPP opp) {
        if (!(this.xblock_259(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_until_195(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STATEMENT_CONTROL) && (tt1.equals("until"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.kok_64(builder))) {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker2;
        quantMarker2 = builder.mark();
        if (this.statement_control_until_195_quant_1(builder, opp)) {
            quantMarker2.drop();
        } else {
            quantMarker2.rollbackTo();
        }
        marker1.done(RakuElementTypes.UNTIL_STATEMENT);
        return true;
    }

    private boolean statement_control_use_196_quant_1(PsiBuilder builder, OPP opp) {
        if (!(this.spacey_177(builder))) {
            return false;
        }
        if (!(this.arglist_6(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_use_196_alt_2(PsiBuilder builder, OPP opp) {
        if (!(this.module_name_74(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker2;
        quantMarker2 = builder.mark();
        if (this.statement_control_use_196_quant_1(builder, opp)) {
            quantMarker2.drop();
        } else {
            quantMarker2.rollbackTo();
        }
        return true;
    }

    private boolean statement_control_use_196_alt_3(PsiBuilder builder, OPP opp) {
        if (!(this.version_255(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_use_196_quant_4(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker4;
        altMarker4 = builder.mark();
        if (this.statement_control_use_196_alt_3(builder, opp)) {
            altMarker4.drop();
        } else {
            altMarker4.rollbackTo();
            PsiBuilder.Marker altMarker3;
            altMarker3 = builder.mark();
            if (this.statement_control_use_196_alt_2(builder, opp)) {
                altMarker3.drop();
            } else {
                altMarker3.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean statement_control_use_196_quant_5(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_use_196(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STATEMENT_CONTROL) && (tt1.equals("use"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker5;
        quantMarker5 = builder.mark();
        if (this.statement_control_use_196_quant_4(builder, opp)) {
            quantMarker5.drop();
        } else {
            quantMarker5.rollbackTo();
        }
        PsiBuilder.Marker quantMarker6;
        quantMarker6 = builder.mark();
        if (this.statement_control_use_196_quant_5(builder, opp)) {
            quantMarker6.drop();
        } else {
            quantMarker6.rollbackTo();
        }
        marker1.done(RakuElementTypes.USE_STATEMENT);
        return true;
    }

    private boolean statement_control_when_197_quant_1(PsiBuilder builder, OPP opp) {
        if (!(this.xblock_259(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_when_197(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STATEMENT_CONTROL) && (tt1.equals("when"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.kok_64(builder))) {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker2;
        quantMarker2 = builder.mark();
        if (this.statement_control_when_197_quant_1(builder, opp)) {
            quantMarker2.drop();
        } else {
            quantMarker2.rollbackTo();
        }
        marker1.done(RakuElementTypes.WHEN_STATEMENT);
        return true;
    }

    private boolean statement_control_whenever_198_quant_1(PsiBuilder builder, OPP opp) {
        if (!(this.xblock_259(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_whenever_198(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STATEMENT_CONTROL) && (tt1.equals("whenever"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.kok_64(builder))) {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker2;
        quantMarker2 = builder.mark();
        if (this.statement_control_whenever_198_quant_1(builder, opp)) {
            quantMarker2.drop();
        } else {
            quantMarker2.rollbackTo();
        }
        marker1.done(RakuElementTypes.WHENEVER_STATEMENT);
        return true;
    }

    private boolean statement_control_while_199_quant_1(PsiBuilder builder, OPP opp) {
        if (!(this.xblock_259(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_while_199(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STATEMENT_CONTROL) && (tt1.equals("while"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.kok_64(builder))) {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker2;
        quantMarker2 = builder.mark();
        if (this.statement_control_while_199_quant_1(builder, opp)) {
            quantMarker2.drop();
        } else {
            quantMarker2.rollbackTo();
        }
        marker1.done(RakuElementTypes.WHILE_STATEMENT);
        return true;
    }

    private boolean statement_control_without_200_quant_1(PsiBuilder builder, OPP opp) {
        if (!(this.xblock_259(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_control_without_200(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STATEMENT_CONTROL) && (tt1.equals("without"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.kok_64(builder))) {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker2;
        quantMarker2 = builder.mark();
        if (this.statement_control_without_200_quant_1(builder, opp)) {
            quantMarker2.drop();
        } else {
            quantMarker2.rollbackTo();
        }
        marker1.done(RakuElementTypes.WITHOUT_STATEMENT);
        return true;
    }

    private boolean statement_mod_cond_201_quant_1(PsiBuilder builder, OPP opp) {
        if (!(this.EXPR_2(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_mod_cond_201(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.STATEMENT_MOD_COND) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.kok_64(builder))) {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker2;
        quantMarker2 = builder.mark();
        if (this.statement_mod_cond_201_quant_1(builder, opp)) {
            quantMarker2.drop();
        } else {
            quantMarker2.rollbackTo();
        }
        marker1.done(RakuElementTypes.STATEMENT_MOD_COND);
        return true;
    }

    private boolean statement_mod_cond_keyword_202_alt_1(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean statement_mod_cond_keyword_202_alt_2(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean statement_mod_cond_keyword_202_alt_3(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean statement_mod_cond_keyword_202_alt_4(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean statement_mod_cond_keyword_202_alt_5(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean statement_mod_cond_keyword_202(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker5;
        altMarker5 = builder.mark();
        if (this.statement_mod_cond_keyword_202_alt_5(builder, opp)) {
            altMarker5.drop();
        } else {
            altMarker5.rollbackTo();
            PsiBuilder.Marker altMarker4;
            altMarker4 = builder.mark();
            if (this.statement_mod_cond_keyword_202_alt_4(builder, opp)) {
                altMarker4.drop();
            } else {
                altMarker4.rollbackTo();
                PsiBuilder.Marker altMarker3;
                altMarker3 = builder.mark();
                if (this.statement_mod_cond_keyword_202_alt_3(builder, opp)) {
                    altMarker3.drop();
                } else {
                    altMarker3.rollbackTo();
                    PsiBuilder.Marker altMarker2;
                    altMarker2 = builder.mark();
                    if (this.statement_mod_cond_keyword_202_alt_2(builder, opp)) {
                        altMarker2.drop();
                    } else {
                        altMarker2.rollbackTo();
                        PsiBuilder.Marker altMarker1;
                        altMarker1 = builder.mark();
                        if (this.statement_mod_cond_keyword_202_alt_1(builder, opp)) {
                            altMarker1.drop();
                        } else {
                            altMarker1.rollbackTo();
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean statement_mod_loop_203_quant_1(PsiBuilder builder, OPP opp) {
        if (!(this.EXPR_2(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_mod_loop_203(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.STATEMENT_MOD_LOOP) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.kok_64(builder))) {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker2;
        quantMarker2 = builder.mark();
        if (this.statement_mod_loop_203_quant_1(builder, opp)) {
            quantMarker2.drop();
        } else {
            quantMarker2.rollbackTo();
        }
        marker1.done(RakuElementTypes.STATEMENT_MOD_LOOP);
        return true;
    }

    private boolean statement_mod_loop_keyword_204_alt_1(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean statement_mod_loop_keyword_204_alt_2(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean statement_mod_loop_keyword_204_alt_3(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean statement_mod_loop_keyword_204_alt_4(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean statement_mod_loop_keyword_204(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker4;
        altMarker4 = builder.mark();
        if (this.statement_mod_loop_keyword_204_alt_4(builder, opp)) {
            altMarker4.drop();
        } else {
            altMarker4.rollbackTo();
            PsiBuilder.Marker altMarker3;
            altMarker3 = builder.mark();
            if (this.statement_mod_loop_keyword_204_alt_3(builder, opp)) {
                altMarker3.drop();
            } else {
                altMarker3.rollbackTo();
                PsiBuilder.Marker altMarker2;
                altMarker2 = builder.mark();
                if (this.statement_mod_loop_keyword_204_alt_2(builder, opp)) {
                    altMarker2.drop();
                } else {
                    altMarker2.rollbackTo();
                    PsiBuilder.Marker altMarker1;
                    altMarker1 = builder.mark();
                    if (this.statement_mod_loop_keyword_204_alt_1(builder, opp)) {
                        altMarker1.drop();
                    } else {
                        altMarker1.rollbackTo();
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean statement_prefix_205_alt_1(PsiBuilder builder, OPP opp) {
        if (!(this.statement_prefix_do_207(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_prefix_205_alt_2(PsiBuilder builder, OPP opp) {
        if (!(this.statement_prefix_react_216(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_prefix_205_alt_3(PsiBuilder builder, OPP opp) {
        if (!(this.statement_prefix_supply_219(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_prefix_205_alt_4(PsiBuilder builder, OPP opp) {
        if (!(this.statement_prefix_start_218(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_prefix_205_alt_5(PsiBuilder builder, OPP opp) {
        if (!(this.statement_prefix_once_212(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_prefix_205_alt_6(PsiBuilder builder, OPP opp) {
        if (!(this.statement_prefix_gather_209(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_prefix_205_alt_7(PsiBuilder builder, OPP opp) {
        if (!(this.statement_prefix_quietly_214(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_prefix_205_alt_8(PsiBuilder builder, OPP opp) {
        if (!(this.statement_prefix_try_220(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_prefix_205_alt_9(PsiBuilder builder, OPP opp) {
        if (!(this.statement_prefix_sink_217(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_prefix_205_alt_10(PsiBuilder builder, OPP opp) {
        if (!(this.statement_prefix_eager_208(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_prefix_205_alt_11(PsiBuilder builder, OPP opp) {
        if (!(this.statement_prefix_lazy_211(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_prefix_205_alt_12(PsiBuilder builder, OPP opp) {
        if (!(this.statement_prefix_hyper_210(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_prefix_205_alt_13(PsiBuilder builder, OPP opp) {
        if (!(this.statement_prefix_race_215(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_prefix_205_alt_14(PsiBuilder builder, OPP opp) {
        if (!(this.statement_prefix_phaser_213(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_prefix_205_alt_15(PsiBuilder builder, OPP opp) {
        if (!(this.statement_prefix_DOC_206(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_prefix_205(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker15;
        altMarker15 = builder.mark();
        if (this.statement_prefix_205_alt_15(builder, opp)) {
            altMarker15.drop();
        } else {
            altMarker15.rollbackTo();
            PsiBuilder.Marker altMarker14;
            altMarker14 = builder.mark();
            if (this.statement_prefix_205_alt_14(builder, opp)) {
                altMarker14.drop();
            } else {
                altMarker14.rollbackTo();
                PsiBuilder.Marker altMarker13;
                altMarker13 = builder.mark();
                if (this.statement_prefix_205_alt_13(builder, opp)) {
                    altMarker13.drop();
                } else {
                    altMarker13.rollbackTo();
                    PsiBuilder.Marker altMarker12;
                    altMarker12 = builder.mark();
                    if (this.statement_prefix_205_alt_12(builder, opp)) {
                        altMarker12.drop();
                    } else {
                        altMarker12.rollbackTo();
                        PsiBuilder.Marker altMarker11;
                        altMarker11 = builder.mark();
                        if (this.statement_prefix_205_alt_11(builder, opp)) {
                            altMarker11.drop();
                        } else {
                            altMarker11.rollbackTo();
                            PsiBuilder.Marker altMarker10;
                            altMarker10 = builder.mark();
                            if (this.statement_prefix_205_alt_10(builder, opp)) {
                                altMarker10.drop();
                            } else {
                                altMarker10.rollbackTo();
                                PsiBuilder.Marker altMarker9;
                                altMarker9 = builder.mark();
                                if (this.statement_prefix_205_alt_9(builder, opp)) {
                                    altMarker9.drop();
                                } else {
                                    altMarker9.rollbackTo();
                                    PsiBuilder.Marker altMarker8;
                                    altMarker8 = builder.mark();
                                    if (this.statement_prefix_205_alt_8(builder, opp)) {
                                        altMarker8.drop();
                                    } else {
                                        altMarker8.rollbackTo();
                                        PsiBuilder.Marker altMarker7;
                                        altMarker7 = builder.mark();
                                        if (this.statement_prefix_205_alt_7(builder, opp)) {
                                            altMarker7.drop();
                                        } else {
                                            altMarker7.rollbackTo();
                                            PsiBuilder.Marker altMarker6;
                                            altMarker6 = builder.mark();
                                            if (this.statement_prefix_205_alt_6(builder, opp)) {
                                                altMarker6.drop();
                                            } else {
                                                altMarker6.rollbackTo();
                                                PsiBuilder.Marker altMarker5;
                                                altMarker5 = builder.mark();
                                                if (this.statement_prefix_205_alt_5(builder, opp)) {
                                                    altMarker5.drop();
                                                } else {
                                                    altMarker5.rollbackTo();
                                                    PsiBuilder.Marker altMarker4;
                                                    altMarker4 = builder.mark();
                                                    if (this.statement_prefix_205_alt_4(builder, opp)) {
                                                        altMarker4.drop();
                                                    } else {
                                                        altMarker4.rollbackTo();
                                                        PsiBuilder.Marker altMarker3;
                                                        altMarker3 = builder.mark();
                                                        if (this.statement_prefix_205_alt_3(builder, opp)) {
                                                            altMarker3.drop();
                                                        } else {
                                                            altMarker3.rollbackTo();
                                                            PsiBuilder.Marker altMarker2;
                                                            altMarker2 = builder.mark();
                                                            if (this.statement_prefix_205_alt_2(builder, opp)) {
                                                                altMarker2.drop();
                                                            } else {
                                                                altMarker2.rollbackTo();
                                                                PsiBuilder.Marker altMarker1;
                                                                altMarker1 = builder.mark();
                                                                if (this.statement_prefix_205_alt_1(builder, opp)) {
                                                                    altMarker1.drop();
                                                                } else {
                                                                    altMarker1.rollbackTo();
                                                                    return false;
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean statement_prefix_DOC_206_quant_1(PsiBuilder builder, OPP opp) {
        if (!(this.statement_prefix_205(builder))) {
            return false;
        }
        return true;
    }

    private boolean statement_prefix_DOC_206(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.PHASER) && (tt1.equals("DOC"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.kok_64(builder))) {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker2;
        quantMarker2 = builder.mark();
        if (this.statement_prefix_DOC_206_quant_1(builder, opp)) {
            quantMarker2.drop();
        } else {
            quantMarker2.rollbackTo();
        }
        marker1.done(RakuElementTypes.PHASER);
        return true;
    }

    private boolean statement_prefix_do_207(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STATEMENT_PREFIX) && (tt1.equals("do"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.kok_64(builder))) {
            return false;
        }
        if (!(this.blorst_17(builder))) {
            return false;
        }
        marker1.done(RakuElementTypes.DO);
        return true;
    }

    private boolean statement_prefix_eager_208(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STATEMENT_PREFIX) && (tt1.equals("eager"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.kok_64(builder))) {
            return false;
        }
        if (!(this.blorst_17(builder))) {
            return false;
        }
        marker1.done(RakuElementTypes.EAGER);
        return true;
    }

    private boolean statement_prefix_gather_209(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STATEMENT_PREFIX) && (tt1.equals("gather"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.kok_64(builder))) {
            return false;
        }
        if (!(this.blorst_17(builder))) {
            return false;
        }
        marker1.done(RakuElementTypes.GATHER);
        return true;
    }

    private boolean statement_prefix_hyper_210(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STATEMENT_PREFIX) && (tt1.equals("hyper"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.kok_64(builder))) {
            return false;
        }
        if (!(this.blorst_17(builder))) {
            return false;
        }
        marker1.done(RakuElementTypes.HYPER);
        return true;
    }

    private boolean statement_prefix_lazy_211(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STATEMENT_PREFIX) && (tt1.equals("lazy"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.kok_64(builder))) {
            return false;
        }
        if (!(this.blorst_17(builder))) {
            return false;
        }
        marker1.done(RakuElementTypes.LAZY);
        return true;
    }

    private boolean statement_prefix_once_212(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STATEMENT_PREFIX) && (tt1.equals("once"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.kok_64(builder))) {
            return false;
        }
        if (!(this.blorst_17(builder))) {
            return false;
        }
        marker1.done(RakuElementTypes.ONCE);
        return true;
    }

    private boolean statement_prefix_phaser_213(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.PHASER) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.kok_64(builder))) {
            return false;
        }
        if (!(this.blorst_17(builder))) {
            return false;
        }
        marker1.done(RakuElementTypes.PHASER);
        return true;
    }

    private boolean statement_prefix_quietly_214(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STATEMENT_PREFIX) && (tt1.equals("quietly"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.kok_64(builder))) {
            return false;
        }
        if (!(this.blorst_17(builder))) {
            return false;
        }
        marker1.done(RakuElementTypes.QUIETLY);
        return true;
    }

    private boolean statement_prefix_race_215(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STATEMENT_PREFIX) && (tt1.equals("race"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.kok_64(builder))) {
            return false;
        }
        if (!(this.blorst_17(builder))) {
            return false;
        }
        marker1.done(RakuElementTypes.RACE);
        return true;
    }

    private boolean statement_prefix_react_216(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STATEMENT_PREFIX) && (tt1.equals("react"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.kok_64(builder))) {
            return false;
        }
        if (!(this.blorst_17(builder))) {
            return false;
        }
        marker1.done(RakuElementTypes.REACT);
        return true;
    }

    private boolean statement_prefix_sink_217(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STATEMENT_PREFIX) && (tt1.equals("sink"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.kok_64(builder))) {
            return false;
        }
        if (!(this.blorst_17(builder))) {
            return false;
        }
        marker1.done(RakuElementTypes.SINK);
        return true;
    }

    private boolean statement_prefix_start_218(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STATEMENT_PREFIX) && (tt1.equals("start"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.kok_64(builder))) {
            return false;
        }
        if (!(this.blorst_17(builder))) {
            return false;
        }
        marker1.done(RakuElementTypes.START);
        return true;
    }

    private boolean statement_prefix_supply_219(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STATEMENT_PREFIX) && (tt1.equals("supply"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.kok_64(builder))) {
            return false;
        }
        if (!(this.blorst_17(builder))) {
            return false;
        }
        marker1.done(RakuElementTypes.SUPPLY);
        return true;
    }

    private boolean statement_prefix_try_220(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STATEMENT_PREFIX) && (tt1.equals("try"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.kok_64(builder))) {
            return false;
        }
        if (!(this.blorst_17(builder))) {
            return false;
        }
        marker1.done(RakuElementTypes.TRY);
        return true;
    }

    private boolean statementlist_221_quant_1(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.WS_OUTSIDE_LIST) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean statementlist_221_alt_2(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean statementlist_221_alt_3(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean statementlist_221_quant_4(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean statementlist_221_quant_5(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker5;
        marker5 = builder.mark();
        if (!(this.statement_179(builder))) {
            return false;
        }
        if (!(this.eat_terminator_42(builder))) {
            return false;
        }
        marker5.done(RakuElementTypes.STATEMENT);
        PsiBuilder.Marker quantMarker6;
        quantMarker6 = builder.mark();
        if (this.statementlist_221_quant_4(builder, opp)) {
            quantMarker6.drop();
        } else {
            quantMarker6.rollbackTo();
        }
        return true;
    }

    private boolean statementlist_221(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker quantMarker1;
        quantMarker1 = builder.mark();
        if (this.statementlist_221_quant_1(builder, opp)) {
            quantMarker1.drop();
        } else {
            quantMarker1.rollbackTo();
        }
        PsiBuilder.Marker marker2;
        marker2 = builder.mark();
        PsiBuilder.Marker altMarker4;
        altMarker4 = builder.mark();
        if (this.statementlist_221_alt_3(builder, opp)) {
            altMarker4.drop();
        } else {
            altMarker4.rollbackTo();
            PsiBuilder.Marker altMarker3;
            altMarker3 = builder.mark();
            if (this.statementlist_221_alt_2(builder, opp)) {
                altMarker3.drop();
            } else {
                altMarker3.rollbackTo();
                return false;
            }
        }
        while (true) {
            PsiBuilder.Marker quantMarker7;
            quantMarker7 = builder.mark();
            if (this.statementlist_221_quant_5(builder, opp)) {
                quantMarker7.drop();
            } else {
                quantMarker7.rollbackTo();
                break;
            }
        }
        marker2.done(RakuElementTypes.STATEMENT_LIST);
        return true;
    }

    private boolean stdstopper_222_alt_1(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean stdstopper_222_alt_2(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean stdstopper_222_alt_3(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean stdstopper_222(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker3;
        altMarker3 = builder.mark();
        if (this.stdstopper_222_alt_3(builder, opp)) {
            altMarker3.drop();
        } else {
            altMarker3.rollbackTo();
            PsiBuilder.Marker altMarker2;
            altMarker2 = builder.mark();
            if (this.stdstopper_222_alt_2(builder, opp)) {
                altMarker2.drop();
            } else {
                altMarker2.rollbackTo();
                PsiBuilder.Marker altMarker1;
                altMarker1 = builder.mark();
                if (this.stdstopper_222_alt_1(builder, opp)) {
                    altMarker1.drop();
                } else {
                    altMarker1.rollbackTo();
                    return false;
                }
            }
        }
        return true;
    }

    private boolean stopper_223_alt_1(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean stopper_223_alt_2(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean stopper_223_alt_3(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker2;
        altMarker2 = builder.mark();
        if (this.stopper_223_alt_2(builder, opp)) {
            altMarker2.drop();
        } else {
            altMarker2.rollbackTo();
            PsiBuilder.Marker altMarker1;
            altMarker1 = builder.mark();
            if (this.stopper_223_alt_1(builder, opp)) {
                altMarker1.drop();
            } else {
                altMarker1.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean stopper_223_quant_4(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean stopper_223_quant_5(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean stopper_223_alt_6(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean stopper_223_alt_7(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean stopper_223_quant_8(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker7;
        altMarker7 = builder.mark();
        if (this.stopper_223_alt_7(builder, opp)) {
            altMarker7.drop();
        } else {
            altMarker7.rollbackTo();
            PsiBuilder.Marker altMarker6;
            altMarker6 = builder.mark();
            if (this.stopper_223_alt_6(builder, opp)) {
                altMarker6.drop();
            } else {
                altMarker6.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean stopper_223_alt_9(PsiBuilder builder, OPP opp) {
        while (true) {
            PsiBuilder.Marker quantMarker4;
            quantMarker4 = builder.mark();
            if (this.stopper_223_quant_4(builder, opp)) {
                quantMarker4.drop();
            } else {
                quantMarker4.rollbackTo();
                break;
            }
        }
        while (true) {
            PsiBuilder.Marker quantMarker5;
            quantMarker5 = builder.mark();
            if (this.stopper_223_quant_5(builder, opp)) {
                quantMarker5.drop();
            } else {
                quantMarker5.rollbackTo();
                break;
            }
        }
        PsiBuilder.Marker quantMarker8;
        quantMarker8 = builder.mark();
        if (this.stopper_223_quant_8(builder, opp)) {
            quantMarker8.drop();
        } else {
            quantMarker8.rollbackTo();
        }
        return true;
    }

    private boolean stopper_223(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker9;
        altMarker9 = builder.mark();
        if (this.stopper_223_alt_9(builder, opp)) {
            altMarker9.drop();
        } else {
            altMarker9.rollbackTo();
            PsiBuilder.Marker altMarker3;
            altMarker3 = builder.mark();
            if (this.stopper_223_alt_3(builder, opp)) {
                altMarker3.drop();
            } else {
                altMarker3.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean term_224_alt_1(PsiBuilder builder, OPP opp) {
        if (!(this.capterm_20(builder))) {
            return false;
        }
        return true;
    }

    private boolean term_224_alt_2(PsiBuilder builder, OPP opp) {
        if (!(this.term_name_228(builder))) {
            return false;
        }
        return true;
    }

    private boolean term_224_alt_3(PsiBuilder builder, OPP opp) {
        if (!(this.term_type_const_236(builder))) {
            return false;
        }
        return true;
    }

    private boolean term_224_alt_4(PsiBuilder builder, OPP opp) {
        if (!(this.term_hyperwhatever_226(builder))) {
            return false;
        }
        return true;
    }

    private boolean term_224_alt_5(PsiBuilder builder, OPP opp) {
        if (!(this.term_whatever_237(builder))) {
            return false;
        }
        return true;
    }

    private boolean term_224_alt_6(PsiBuilder builder, OPP opp) {
        if (!(this.term_rand_231(builder))) {
            return false;
        }
        return true;
    }

    private boolean term_224_alt_7(PsiBuilder builder, OPP opp) {
        if (!(this.term_empty_set_225(builder))) {
            return false;
        }
        return true;
    }

    private boolean term_224_alt_8(PsiBuilder builder, OPP opp) {
        if (!(this.term_time_235(builder))) {
            return false;
        }
        return true;
    }

    private boolean term_224_alt_9(PsiBuilder builder, OPP opp) {
        if (!(this.term_now_229(builder))) {
            return false;
        }
        return true;
    }

    private boolean term_224_alt_10(PsiBuilder builder, OPP opp) {
        if (!(this.pblock_95(builder))) {
            return false;
        }
        return true;
    }

    private boolean term_224_alt_11(PsiBuilder builder, OPP opp) {
        if (!(this.dotty_40(builder))) {
            return false;
        }
        return true;
    }

    private boolean term_224_alt_12(PsiBuilder builder, OPP opp) {
        if (!(this.term_stub_code_234(builder))) {
            return false;
        }
        return true;
    }

    private boolean term_224_alt_13(PsiBuilder builder, OPP opp) {
        if (!(this.circumfix_27(builder))) {
            return false;
        }
        return true;
    }

    private boolean term_224_alt_14(PsiBuilder builder, OPP opp) {
        if (!(this.term_reduce_232(builder))) {
            return false;
        }
        return true;
    }

    private boolean term_224_alt_15(PsiBuilder builder, OPP opp) {
        if (!(this.term_onlystar_230(builder))) {
            return false;
        }
        return true;
    }

    private boolean term_224_alt_16(PsiBuilder builder, OPP opp) {
        if (!(this.package_declarator_88(builder))) {
            return false;
        }
        return true;
    }

    private boolean term_224_alt_17(PsiBuilder builder, OPP opp) {
        if (!(this.statement_prefix_205(builder))) {
            return false;
        }
        return true;
    }

    private boolean term_224_alt_18(PsiBuilder builder, OPP opp) {
        if (!(this.type_declarator_248(builder))) {
            return false;
        }
        return true;
    }

    private boolean term_224_alt_19(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.TERM_IS_MULTI) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.multi_declarator_76(builder))) {
            return false;
        }
        return true;
    }

    private boolean term_224_alt_20(PsiBuilder builder, OPP opp) {
        if (!(this.regex_declarator_149(builder))) {
            return false;
        }
        return true;
    }

    private boolean term_224_alt_21(PsiBuilder builder, OPP opp) {
        if (!(this.routine_declarator_153(builder))) {
            return false;
        }
        return true;
    }

    private boolean term_224_alt_22(PsiBuilder builder, OPP opp) {
        if (!(this.scope_declarator_166(builder))) {
            return false;
        }
        return true;
    }

    private boolean term_224_alt_23(PsiBuilder builder, OPP opp) {
        if (!(this.value_252(builder))) {
            return false;
        }
        return true;
    }

    private boolean term_224_alt_24(PsiBuilder builder, OPP opp) {
        if (!(this.term_ident_227(builder))) {
            return false;
        }
        return true;
    }

    private boolean term_224_alt_25(PsiBuilder builder, OPP opp) {
        if (!(this.variable_253(builder))) {
            return false;
        }
        return true;
    }

    private boolean term_224_alt_26(PsiBuilder builder, OPP opp) {
        if (!(this.term_self_233(builder))) {
            return false;
        }
        return true;
    }

    private boolean term_224_alt_27(PsiBuilder builder, OPP opp) {
        if (!(this.colonpair_29(builder))) {
            return false;
        }
        return true;
    }

    private boolean term_224_alt_28(PsiBuilder builder, OPP opp) {
        if (!(this.fatarrow_47(builder))) {
            return false;
        }
        return true;
    }

    private boolean term_224(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker28;
        altMarker28 = builder.mark();
        if (this.term_224_alt_28(builder, opp)) {
            altMarker28.drop();
        } else {
            altMarker28.rollbackTo();
            PsiBuilder.Marker altMarker27;
            altMarker27 = builder.mark();
            if (this.term_224_alt_27(builder, opp)) {
                altMarker27.drop();
            } else {
                altMarker27.rollbackTo();
                PsiBuilder.Marker altMarker26;
                altMarker26 = builder.mark();
                if (this.term_224_alt_26(builder, opp)) {
                    altMarker26.drop();
                } else {
                    altMarker26.rollbackTo();
                    PsiBuilder.Marker altMarker25;
                    altMarker25 = builder.mark();
                    if (this.term_224_alt_25(builder, opp)) {
                        altMarker25.drop();
                    } else {
                        altMarker25.rollbackTo();
                        PsiBuilder.Marker altMarker24;
                        altMarker24 = builder.mark();
                        if (this.term_224_alt_24(builder, opp)) {
                            altMarker24.drop();
                        } else {
                            altMarker24.rollbackTo();
                            PsiBuilder.Marker altMarker23;
                            altMarker23 = builder.mark();
                            if (this.term_224_alt_23(builder, opp)) {
                                altMarker23.drop();
                            } else {
                                altMarker23.rollbackTo();
                                PsiBuilder.Marker altMarker22;
                                altMarker22 = builder.mark();
                                if (this.term_224_alt_22(builder, opp)) {
                                    altMarker22.drop();
                                } else {
                                    altMarker22.rollbackTo();
                                    PsiBuilder.Marker altMarker21;
                                    altMarker21 = builder.mark();
                                    if (this.term_224_alt_21(builder, opp)) {
                                        altMarker21.drop();
                                    } else {
                                        altMarker21.rollbackTo();
                                        PsiBuilder.Marker altMarker20;
                                        altMarker20 = builder.mark();
                                        if (this.term_224_alt_20(builder, opp)) {
                                            altMarker20.drop();
                                        } else {
                                            altMarker20.rollbackTo();
                                            PsiBuilder.Marker altMarker19;
                                            altMarker19 = builder.mark();
                                            if (this.term_224_alt_19(builder, opp)) {
                                                altMarker19.drop();
                                            } else {
                                                altMarker19.rollbackTo();
                                                PsiBuilder.Marker altMarker18;
                                                altMarker18 = builder.mark();
                                                if (this.term_224_alt_18(builder, opp)) {
                                                    altMarker18.drop();
                                                } else {
                                                    altMarker18.rollbackTo();
                                                    PsiBuilder.Marker altMarker17;
                                                    altMarker17 = builder.mark();
                                                    if (this.term_224_alt_17(builder, opp)) {
                                                        altMarker17.drop();
                                                    } else {
                                                        altMarker17.rollbackTo();
                                                        PsiBuilder.Marker altMarker16;
                                                        altMarker16 = builder.mark();
                                                        if (this.term_224_alt_16(builder, opp)) {
                                                            altMarker16.drop();
                                                        } else {
                                                            altMarker16.rollbackTo();
                                                            PsiBuilder.Marker altMarker15;
                                                            altMarker15 = builder.mark();
                                                            if (this.term_224_alt_15(builder, opp)) {
                                                                altMarker15.drop();
                                                            } else {
                                                                altMarker15.rollbackTo();
                                                                PsiBuilder.Marker altMarker14;
                                                                altMarker14 = builder.mark();
                                                                if (this.term_224_alt_14(builder, opp)) {
                                                                    altMarker14.drop();
                                                                } else {
                                                                    altMarker14.rollbackTo();
                                                                    PsiBuilder.Marker altMarker13;
                                                                    altMarker13 = builder.mark();
                                                                    if (this.term_224_alt_13(builder, opp)) {
                                                                        altMarker13.drop();
                                                                    } else {
                                                                        altMarker13.rollbackTo();
                                                                        PsiBuilder.Marker altMarker12;
                                                                        altMarker12 = builder.mark();
                                                                        if (this.term_224_alt_12(builder, opp)) {
                                                                            altMarker12.drop();
                                                                        } else {
                                                                            altMarker12.rollbackTo();
                                                                            PsiBuilder.Marker altMarker11;
                                                                            altMarker11 = builder.mark();
                                                                            if (this.term_224_alt_11(builder, opp)) {
                                                                                altMarker11.drop();
                                                                            } else {
                                                                                altMarker11.rollbackTo();
                                                                                PsiBuilder.Marker altMarker10;
                                                                                altMarker10 = builder.mark();
                                                                                if (this.term_224_alt_10(builder, opp)) {
                                                                                    altMarker10.drop();
                                                                                } else {
                                                                                    altMarker10.rollbackTo();
                                                                                    PsiBuilder.Marker altMarker9;
                                                                                    altMarker9 = builder.mark();
                                                                                    if (this.term_224_alt_9(builder, opp)) {
                                                                                        altMarker9.drop();
                                                                                    } else {
                                                                                        altMarker9.rollbackTo();
                                                                                        PsiBuilder.Marker altMarker8;
                                                                                        altMarker8 = builder.mark();
                                                                                        if (this.term_224_alt_8(builder, opp)) {
                                                                                            altMarker8.drop();
                                                                                        } else {
                                                                                            altMarker8.rollbackTo();
                                                                                            PsiBuilder.Marker altMarker7;
                                                                                            altMarker7 = builder.mark();
                                                                                            if (this.term_224_alt_7(builder, opp)) {
                                                                                                altMarker7.drop();
                                                                                            } else {
                                                                                                altMarker7.rollbackTo();
                                                                                                PsiBuilder.Marker altMarker6;
                                                                                                altMarker6 = builder.mark();
                                                                                                if (this.term_224_alt_6(builder, opp)) {
                                                                                                    altMarker6.drop();
                                                                                                } else {
                                                                                                    altMarker6.rollbackTo();
                                                                                                    PsiBuilder.Marker altMarker5;
                                                                                                    altMarker5 = builder.mark();
                                                                                                    if (this.term_224_alt_5(builder, opp)) {
                                                                                                        altMarker5.drop();
                                                                                                    } else {
                                                                                                        altMarker5.rollbackTo();
                                                                                                        PsiBuilder.Marker altMarker4;
                                                                                                        altMarker4 = builder.mark();
                                                                                                        if (this.term_224_alt_4(builder, opp)) {
                                                                                                            altMarker4.drop();
                                                                                                        } else {
                                                                                                            altMarker4.rollbackTo();
                                                                                                            PsiBuilder.Marker altMarker3;
                                                                                                            altMarker3 = builder.mark();
                                                                                                            if (this.term_224_alt_3(builder, opp)) {
                                                                                                                altMarker3.drop();
                                                                                                            } else {
                                                                                                                altMarker3.rollbackTo();
                                                                                                                PsiBuilder.Marker altMarker2;
                                                                                                                altMarker2 = builder.mark();
                                                                                                                if (this.term_224_alt_2(builder, opp)) {
                                                                                                                    altMarker2.drop();
                                                                                                                } else {
                                                                                                                    altMarker2.rollbackTo();
                                                                                                                    PsiBuilder.Marker altMarker1;
                                                                                                                    altMarker1 = builder.mark();
                                                                                                                    if (this.term_224_alt_1(builder, opp)) {
                                                                                                                        altMarker1.drop();
                                                                                                                    } else {
                                                                                                                        altMarker1.rollbackTo();
                                                                                                                        return false;
                                                                                                                    }
                                                                                                                }
                                                                                                            }
                                                                                                        }
                                                                                                    }
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean term_empty_set_225(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.TERM) && (tt1.equals("\u2205"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        marker1.done(RakuElementTypes.TERM);
        return true;
    }

    private boolean term_hyperwhatever_226(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.HYPER_WHATEVER) && (tt1.equals("*"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        marker1.done(RakuElementTypes.HYPER_WHATEVER);
        return true;
    }

    private boolean term_ident_227_quant_1(PsiBuilder builder, OPP opp) {
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.WHITE_SPACE) && (tt1.equals("\\"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean term_ident_227(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        PsiBuilder.Marker marker2;
        marker2 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.SUB_CALL_NAME) {
            builder.advanceLexer();
        } else {
            return false;
        }
        marker2.done(RakuElementTypes.SUB_CALL_NAME);
        PsiBuilder.Marker quantMarker3;
        quantMarker3 = builder.mark();
        if (this.term_ident_227_quant_1(builder, opp)) {
            quantMarker3.drop();
        } else {
            quantMarker3.rollbackTo();
        }
        if (!(this.args_7(builder))) {
            return false;
        }
        marker1.done(RakuElementTypes.SUB_CALL);
        return true;
    }

    private boolean term_name_228_quant_1(PsiBuilder builder, OPP opp) {
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.WHITE_SPACE) && (tt1.equals("\\"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean term_name_228_alt_2(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        PsiBuilder.Marker marker2;
        marker2 = builder.mark();
        PsiBuilder.Marker marker3;
        marker3 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.SUB_CALL_NAME) {
            builder.advanceLexer();
        } else {
            return false;
        }
        marker3.done(RakuElementTypes.SUB_CALL_NAME);
        if (!(this.longname_colonpairs_67(builder))) {
            return false;
        }
        marker2.done(RakuElementTypes.LONG_NAME);
        PsiBuilder.Marker quantMarker4;
        quantMarker4 = builder.mark();
        if (this.term_name_228_quant_1(builder, opp)) {
            quantMarker4.drop();
        } else {
            quantMarker4.rollbackTo();
        }
        if (!(this.args_7(builder))) {
            return false;
        }
        marker1.done(RakuElementTypes.SUB_CALL);
        return true;
    }

    private boolean term_name_228_alt_3(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker6;
        marker6 = builder.mark();
        PsiBuilder.Marker marker7;
        marker7 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.NAME) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.longname_colonpairs_67(builder))) {
            return false;
        }
        marker7.done(RakuElementTypes.LONG_NAME);
        marker6.done(RakuElementTypes.TYPE_NAME);
        return true;
    }

    private boolean term_name_228(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker8;
        altMarker8 = builder.mark();
        if (this.term_name_228_alt_3(builder, opp)) {
            altMarker8.drop();
        } else {
            altMarker8.rollbackTo();
            PsiBuilder.Marker altMarker5;
            altMarker5 = builder.mark();
            if (this.term_name_228_alt_2(builder, opp)) {
                altMarker5.drop();
            } else {
                altMarker5.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean term_now_229(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.TERM) && (tt1.equals("now"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.tok_241(builder))) {
            return false;
        }
        marker1.done(RakuElementTypes.TERM);
        return true;
    }

    private boolean term_onlystar_230(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.ONLY_STAR) && (tt1.equals("{*}"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        marker1.done(RakuElementTypes.ONLY_STAR);
        return true;
    }

    private boolean term_rand_231(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.TERM) && (tt1.equals("rand"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.end_keyword_43(builder))) {
            return false;
        }
        marker1.done(RakuElementTypes.TERM);
        return true;
    }

    private boolean term_reduce_232_alt_1(PsiBuilder builder, OPP opp) {
        if (!(this.infixish_57(builder))) {
            return false;
        }
        return true;
    }

    private boolean term_reduce_232_alt_2(PsiBuilder builder, OPP opp) {
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.METAOP) && (tt2.equals("\\"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.infixish_57(builder))) {
            return false;
        }
        return true;
    }

    private boolean term_reduce_232(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.METAOP) && (tt1.equals("["))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker altMarker3;
        altMarker3 = builder.mark();
        if (this.term_reduce_232_alt_2(builder, opp)) {
            altMarker3.drop();
        } else {
            altMarker3.rollbackTo();
            PsiBuilder.Marker altMarker2;
            altMarker2 = builder.mark();
            if (this.term_reduce_232_alt_1(builder, opp)) {
                altMarker2.drop();
            } else {
                altMarker2.rollbackTo();
                return false;
            }
        }
        String tt3;
        tt3 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.METAOP) && (tt3.equals("]"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.args_7(builder))) {
            return false;
        }
        marker1.done(RakuElementTypes.REDUCE_METAOP);
        return true;
    }

    private boolean term_self_233(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.SELF) && (tt1.equals("self"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        marker1.done(RakuElementTypes.SELF);
        if (!(this.end_keyword_43(builder))) {
            return false;
        }
        return true;
    }

    private boolean term_stub_code_234_alt_1(PsiBuilder builder, OPP opp) {
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STUB_CODE) && (tt1.equals("!!!"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean term_stub_code_234_alt_2(PsiBuilder builder, OPP opp) {
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STUB_CODE) && (tt2.equals("???"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean term_stub_code_234_alt_3(PsiBuilder builder, OPP opp) {
        String tt3;
        tt3 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STUB_CODE) && (tt3.equals("..."))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean term_stub_code_234_alt_4(PsiBuilder builder, OPP opp) {
        String tt4;
        tt4 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.STUB_CODE) && (tt4.equals("\u2026"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean term_stub_code_234(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        PsiBuilder.Marker altMarker5;
        altMarker5 = builder.mark();
        if (this.term_stub_code_234_alt_4(builder, opp)) {
            altMarker5.drop();
        } else {
            altMarker5.rollbackTo();
            PsiBuilder.Marker altMarker4;
            altMarker4 = builder.mark();
            if (this.term_stub_code_234_alt_3(builder, opp)) {
                altMarker4.drop();
            } else {
                altMarker4.rollbackTo();
                PsiBuilder.Marker altMarker3;
                altMarker3 = builder.mark();
                if (this.term_stub_code_234_alt_2(builder, opp)) {
                    altMarker3.drop();
                } else {
                    altMarker3.rollbackTo();
                    PsiBuilder.Marker altMarker2;
                    altMarker2 = builder.mark();
                    if (this.term_stub_code_234_alt_1(builder, opp)) {
                        altMarker2.drop();
                    } else {
                        altMarker2.rollbackTo();
                        return false;
                    }
                }
            }
        }
        if (!(this.args_7(builder))) {
            return false;
        }
        marker1.done(RakuElementTypes.STUB_CODE);
        return true;
    }

    private boolean term_time_235(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.TERM) && (tt1.equals("time"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.tok_241(builder))) {
            return false;
        }
        marker1.done(RakuElementTypes.TERM);
        return true;
    }

    private boolean term_type_const_236(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.TYPE_CONST) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if ((builder.getTokenType()) == RakuTokenTypes.NAME) {
            builder.advanceLexer();
        } else {
            return false;
        }
        marker1.done(RakuElementTypes.TYPE_NAME);
        return true;
    }

    private boolean term_whatever_237(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.WHATEVER) && (tt1.equals("*"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        marker1.done(RakuElementTypes.WHATEVER);
        return true;
    }

    private boolean terminator_238_alt_1(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean terminator_238_alt_2(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean terminator_238_alt_3(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean terminator_238_alt_4(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean terminator_238_alt_5(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean terminator_238_alt_6(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean terminator_238_alt_7(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean terminator_238_alt_8(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean terminator_238_alt_9(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean terminator_238_alt_10(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean terminator_238_alt_11(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker10;
        altMarker10 = builder.mark();
        if (this.terminator_238_alt_10(builder, opp)) {
            altMarker10.drop();
        } else {
            altMarker10.rollbackTo();
            PsiBuilder.Marker altMarker9;
            altMarker9 = builder.mark();
            if (this.terminator_238_alt_9(builder, opp)) {
                altMarker9.drop();
            } else {
                altMarker9.rollbackTo();
                PsiBuilder.Marker altMarker8;
                altMarker8 = builder.mark();
                if (this.terminator_238_alt_8(builder, opp)) {
                    altMarker8.drop();
                } else {
                    altMarker8.rollbackTo();
                    PsiBuilder.Marker altMarker7;
                    altMarker7 = builder.mark();
                    if (this.terminator_238_alt_7(builder, opp)) {
                        altMarker7.drop();
                    } else {
                        altMarker7.rollbackTo();
                        PsiBuilder.Marker altMarker6;
                        altMarker6 = builder.mark();
                        if (this.terminator_238_alt_6(builder, opp)) {
                            altMarker6.drop();
                        } else {
                            altMarker6.rollbackTo();
                            PsiBuilder.Marker altMarker5;
                            altMarker5 = builder.mark();
                            if (this.terminator_238_alt_5(builder, opp)) {
                                altMarker5.drop();
                            } else {
                                altMarker5.rollbackTo();
                                PsiBuilder.Marker altMarker4;
                                altMarker4 = builder.mark();
                                if (this.terminator_238_alt_4(builder, opp)) {
                                    altMarker4.drop();
                                } else {
                                    altMarker4.rollbackTo();
                                    PsiBuilder.Marker altMarker3;
                                    altMarker3 = builder.mark();
                                    if (this.terminator_238_alt_3(builder, opp)) {
                                        altMarker3.drop();
                                    } else {
                                        altMarker3.rollbackTo();
                                        PsiBuilder.Marker altMarker2;
                                        altMarker2 = builder.mark();
                                        if (this.terminator_238_alt_2(builder, opp)) {
                                            altMarker2.drop();
                                        } else {
                                            altMarker2.rollbackTo();
                                            return false;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (!(this.kok_64(builder))) {
            return false;
        }
        return true;
    }

    private boolean terminator_238_alt_12(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean terminator_238_alt_13(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean terminator_238(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker13;
        altMarker13 = builder.mark();
        if (this.terminator_238_alt_13(builder, opp)) {
            altMarker13.drop();
        } else {
            altMarker13.rollbackTo();
            PsiBuilder.Marker altMarker12;
            altMarker12 = builder.mark();
            if (this.terminator_238_alt_12(builder, opp)) {
                altMarker12.drop();
            } else {
                altMarker12.rollbackTo();
                PsiBuilder.Marker altMarker11;
                altMarker11 = builder.mark();
                if (this.terminator_238_alt_11(builder, opp)) {
                    altMarker11.drop();
                } else {
                    altMarker11.rollbackTo();
                    PsiBuilder.Marker altMarker1;
                    altMarker1 = builder.mark();
                    if (this.terminator_238_alt_1(builder, opp)) {
                        altMarker1.drop();
                    } else {
                        altMarker1.rollbackTo();
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean termish_239(PsiBuilder builder) {
        OPP opp;
        opp = null;
        if (!(this.term_224(builder))) {
            return false;
        }
        return true;
    }

    private boolean termseq_240_alt_1(PsiBuilder builder, OPP opp) {
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.REGEX_INFIX) && (tt1.equals("&"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean termseq_240_alt_2(PsiBuilder builder, OPP opp) {
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.REGEX_INFIX) && (tt2.equals("|"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean termseq_240_alt_3(PsiBuilder builder, OPP opp) {
        String tt3;
        tt3 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.REGEX_INFIX) && (tt3.equals("&&"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean termseq_240_alt_4(PsiBuilder builder, OPP opp) {
        String tt4;
        tt4 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.REGEX_INFIX) && (tt4.equals("||"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean termseq_240_alt_5(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.REGEX_MISSING_TERM) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean termseq_240_alt_6(PsiBuilder builder, OPP opp) {
        opp.startPrefixes();
        opp.endPrefixes();
        if (!(this.rxtermish_164(builder))) {
            return false;
        }
        opp.startPostfixes();
        opp.endPostfixes();
        return true;
    }

    private boolean termseq_240_quant_7(PsiBuilder builder, OPP opp) {
        opp.startInfix();
        PsiBuilder.Marker altMarker4;
        altMarker4 = builder.mark();
        if (this.termseq_240_alt_4(builder, opp)) {
            altMarker4.drop();
        } else {
            altMarker4.rollbackTo();
            PsiBuilder.Marker altMarker3;
            altMarker3 = builder.mark();
            if (this.termseq_240_alt_3(builder, opp)) {
                altMarker3.drop();
            } else {
                altMarker3.rollbackTo();
                PsiBuilder.Marker altMarker2;
                altMarker2 = builder.mark();
                if (this.termseq_240_alt_2(builder, opp)) {
                    altMarker2.drop();
                } else {
                    altMarker2.rollbackTo();
                    PsiBuilder.Marker altMarker1;
                    altMarker1 = builder.mark();
                    if (this.termseq_240_alt_1(builder, opp)) {
                        altMarker1.drop();
                    } else {
                        altMarker1.rollbackTo();
                        return false;
                    }
                }
            }
        }
        opp.endInfix();
        if (!(this.rxws_165(builder))) {
            return false;
        }
        PsiBuilder.Marker altMarker6;
        altMarker6 = builder.mark();
        if (this.termseq_240_alt_6(builder, opp)) {
            altMarker6.drop();
        } else {
            altMarker6.rollbackTo();
            PsiBuilder.Marker altMarker5;
            altMarker5 = builder.mark();
            if (this.termseq_240_alt_5(builder, opp)) {
                altMarker5.drop();
            } else {
                altMarker5.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean termseq_240(PsiBuilder builder) {
        OPP opp;
        opp = new OPP(builder);
        opp.startExpr();
        opp.regexMode();
        opp.startPrefixes();
        opp.endPrefixes();
        if (!(this.rxtermish_164(builder))) {
            return false;
        }
        opp.startPostfixes();
        opp.endPostfixes();
        while (true) {
            PsiBuilder.Marker quantMarker7;
            quantMarker7 = builder.mark();
            if (this.termseq_240_quant_7(builder, opp)) {
                quantMarker7.drop();
            } else {
                quantMarker7.rollbackTo();
                break;
            }
        }
        opp.endExpr();
        return true;
    }

    private boolean tok_241(PsiBuilder builder) {
        OPP opp;
        opp = null;
        if (!(this.end_keyword_43(builder))) {
            return false;
        }
        return true;
    }

    private boolean trait_242_quant_1(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean trait_242(PsiBuilder builder) {
        OPP opp;
        opp = null;
        if (!(this.trait_mod_243(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker1;
        quantMarker1 = builder.mark();
        if (this.trait_242_quant_1(builder, opp)) {
            quantMarker1.drop();
        } else {
            quantMarker1.rollbackTo();
        }
        return true;
    }

    private boolean trait_mod_243_alt_1(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.TRAIT_INCOMPLETE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean trait_mod_243_alt_2(PsiBuilder builder, OPP opp) {
        if (!(this.term_224(builder))) {
            return false;
        }
        return true;
    }

    private boolean trait_mod_243_alt_3(PsiBuilder builder, OPP opp) {
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.TRAIT) && (tt1.equals("handles"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker altMarker3;
        altMarker3 = builder.mark();
        if (this.trait_mod_243_alt_2(builder, opp)) {
            altMarker3.drop();
        } else {
            altMarker3.rollbackTo();
            PsiBuilder.Marker altMarker2;
            altMarker2 = builder.mark();
            if (this.trait_mod_243_alt_1(builder, opp)) {
                altMarker2.drop();
            } else {
                altMarker2.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean trait_mod_243_alt_4(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.TRAIT_INCOMPLETE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean trait_mod_243_alt_5(PsiBuilder builder, OPP opp) {
        if (!(this.typename_249(builder))) {
            return false;
        }
        return true;
    }

    private boolean trait_mod_243_alt_6(PsiBuilder builder, OPP opp) {
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.TRAIT) && (tt2.equals("returns"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker altMarker6;
        altMarker6 = builder.mark();
        if (this.trait_mod_243_alt_5(builder, opp)) {
            altMarker6.drop();
        } else {
            altMarker6.rollbackTo();
            PsiBuilder.Marker altMarker5;
            altMarker5 = builder.mark();
            if (this.trait_mod_243_alt_4(builder, opp)) {
                altMarker5.drop();
            } else {
                altMarker5.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean trait_mod_243_alt_7(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.TRAIT_INCOMPLETE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean trait_mod_243_alt_8(PsiBuilder builder, OPP opp) {
        if (!(this.typename_249(builder))) {
            return false;
        }
        return true;
    }

    private boolean trait_mod_243_alt_9(PsiBuilder builder, OPP opp) {
        String tt3;
        tt3 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.TRAIT) && (tt3.equals("of"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker altMarker9;
        altMarker9 = builder.mark();
        if (this.trait_mod_243_alt_8(builder, opp)) {
            altMarker9.drop();
        } else {
            altMarker9.rollbackTo();
            PsiBuilder.Marker altMarker8;
            altMarker8 = builder.mark();
            if (this.trait_mod_243_alt_7(builder, opp)) {
                altMarker8.drop();
            } else {
                altMarker8.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean trait_mod_243_alt_10(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.TRAIT_INCOMPLETE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean trait_mod_243_alt_11(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.TRAIT_INCOMPLETE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean trait_mod_243_alt_12(PsiBuilder builder, OPP opp) {
        if (!(this.pblock_95(builder))) {
            return false;
        }
        return true;
    }

    private boolean trait_mod_243_alt_13(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.NAME) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker altMarker13;
        altMarker13 = builder.mark();
        if (this.trait_mod_243_alt_12(builder, opp)) {
            altMarker13.drop();
        } else {
            altMarker13.rollbackTo();
            PsiBuilder.Marker altMarker12;
            altMarker12 = builder.mark();
            if (this.trait_mod_243_alt_11(builder, opp)) {
                altMarker12.drop();
            } else {
                altMarker12.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean trait_mod_243_alt_14(PsiBuilder builder, OPP opp) {
        String tt4;
        tt4 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.TRAIT) && (tt4.equals("will"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker altMarker14;
        altMarker14 = builder.mark();
        if (this.trait_mod_243_alt_13(builder, opp)) {
            altMarker14.drop();
        } else {
            altMarker14.rollbackTo();
            PsiBuilder.Marker altMarker11;
            altMarker11 = builder.mark();
            if (this.trait_mod_243_alt_10(builder, opp)) {
                altMarker11.drop();
            } else {
                altMarker11.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean trait_mod_243_alt_15(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.TRAIT_INCOMPLETE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean trait_mod_243_alt_16(PsiBuilder builder, OPP opp) {
        if (!(this.typename_249(builder))) {
            return false;
        }
        return true;
    }

    private boolean trait_mod_243_alt_17(PsiBuilder builder, OPP opp) {
        String tt5;
        tt5 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.TRAIT) && (tt5.equals("does"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker altMarker17;
        altMarker17 = builder.mark();
        if (this.trait_mod_243_alt_16(builder, opp)) {
            altMarker17.drop();
        } else {
            altMarker17.rollbackTo();
            PsiBuilder.Marker altMarker16;
            altMarker16 = builder.mark();
            if (this.trait_mod_243_alt_15(builder, opp)) {
                altMarker16.drop();
            } else {
                altMarker16.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean trait_mod_243_alt_18(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.TRAIT_INCOMPLETE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean trait_mod_243_alt_19(PsiBuilder builder, OPP opp) {
        if (!(this.typename_249(builder))) {
            return false;
        }
        return true;
    }

    private boolean trait_mod_243_alt_20(PsiBuilder builder, OPP opp) {
        String tt6;
        tt6 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.TRAIT) && (tt6.equals("hides"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker altMarker20;
        altMarker20 = builder.mark();
        if (this.trait_mod_243_alt_19(builder, opp)) {
            altMarker20.drop();
        } else {
            altMarker20.rollbackTo();
            PsiBuilder.Marker altMarker19;
            altMarker19 = builder.mark();
            if (this.trait_mod_243_alt_18(builder, opp)) {
                altMarker19.drop();
            } else {
                altMarker19.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean trait_mod_243_alt_21(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.TRAIT_INCOMPLETE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean trait_mod_243_quant_22(PsiBuilder builder, OPP opp) {
        if (!(this.circumfix_27(builder))) {
            return false;
        }
        return true;
    }

    private boolean trait_mod_243_alt_23(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker23;
        marker23 = builder.mark();
        PsiBuilder.Marker marker24;
        marker24 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.NAME) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.longname_colonpairs_67(builder))) {
            return false;
        }
        marker24.done(RakuElementTypes.LONG_NAME);
        marker23.done(RakuElementTypes.IS_TRAIT_NAME);
        PsiBuilder.Marker quantMarker25;
        quantMarker25 = builder.mark();
        if (this.trait_mod_243_quant_22(builder, opp)) {
            quantMarker25.drop();
        } else {
            quantMarker25.rollbackTo();
        }
        return true;
    }

    private boolean trait_mod_243_alt_24(PsiBuilder builder, OPP opp) {
        String tt7;
        tt7 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.TRAIT) && (tt7.equals("is"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker altMarker26;
        altMarker26 = builder.mark();
        if (this.trait_mod_243_alt_23(builder, opp)) {
            altMarker26.drop();
        } else {
            altMarker26.rollbackTo();
            PsiBuilder.Marker altMarker22;
            altMarker22 = builder.mark();
            if (this.trait_mod_243_alt_21(builder, opp)) {
                altMarker22.drop();
            } else {
                altMarker22.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean trait_mod_243(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        PsiBuilder.Marker altMarker27;
        altMarker27 = builder.mark();
        if (this.trait_mod_243_alt_24(builder, opp)) {
            altMarker27.drop();
        } else {
            altMarker27.rollbackTo();
            PsiBuilder.Marker altMarker21;
            altMarker21 = builder.mark();
            if (this.trait_mod_243_alt_20(builder, opp)) {
                altMarker21.drop();
            } else {
                altMarker21.rollbackTo();
                PsiBuilder.Marker altMarker18;
                altMarker18 = builder.mark();
                if (this.trait_mod_243_alt_17(builder, opp)) {
                    altMarker18.drop();
                } else {
                    altMarker18.rollbackTo();
                    PsiBuilder.Marker altMarker15;
                    altMarker15 = builder.mark();
                    if (this.trait_mod_243_alt_14(builder, opp)) {
                        altMarker15.drop();
                    } else {
                        altMarker15.rollbackTo();
                        PsiBuilder.Marker altMarker10;
                        altMarker10 = builder.mark();
                        if (this.trait_mod_243_alt_9(builder, opp)) {
                            altMarker10.drop();
                        } else {
                            altMarker10.rollbackTo();
                            PsiBuilder.Marker altMarker7;
                            altMarker7 = builder.mark();
                            if (this.trait_mod_243_alt_6(builder, opp)) {
                                altMarker7.drop();
                            } else {
                                altMarker7.rollbackTo();
                                PsiBuilder.Marker altMarker4;
                                altMarker4 = builder.mark();
                                if (this.trait_mod_243_alt_3(builder, opp)) {
                                    altMarker4.drop();
                                } else {
                                    altMarker4.rollbackTo();
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }
        marker1.done(RakuElementTypes.TRAIT);
        return true;
    }

    private boolean tribble_244_quant_1(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.QUOTE_REGEX) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean tribble_244_alt_2(PsiBuilder builder, OPP opp) {
        if (!(this.tribbler_245(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker1;
        quantMarker1 = builder.mark();
        if (this.tribble_244_quant_1(builder, opp)) {
            quantMarker1.drop();
        } else {
            quantMarker1.rollbackTo();
        }
        return true;
    }

    private boolean tribble_244_quant_3(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.QUOTE_REGEX) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean tribble_244_alt_4(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.TR_DISTINCT_START_STOP) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if ((builder.getTokenType()) == RakuTokenTypes.QUOTE_REGEX) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.tribbler_245(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker3;
        quantMarker3 = builder.mark();
        if (this.tribble_244_quant_3(builder, opp)) {
            quantMarker3.drop();
        } else {
            quantMarker3.rollbackTo();
        }
        return true;
    }

    private boolean tribble_244_quant_5(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker4;
        altMarker4 = builder.mark();
        if (this.tribble_244_alt_4(builder, opp)) {
            altMarker4.drop();
        } else {
            altMarker4.rollbackTo();
            PsiBuilder.Marker altMarker2;
            altMarker2 = builder.mark();
            if (this.tribble_244_alt_2(builder, opp)) {
                altMarker2.drop();
            } else {
                altMarker2.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean tribble_244_quant_6(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.QUOTE_REGEX) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker quantMarker5;
        quantMarker5 = builder.mark();
        if (this.tribble_244_quant_5(builder, opp)) {
            quantMarker5.drop();
        } else {
            quantMarker5.rollbackTo();
        }
        return true;
    }

    private boolean tribble_244(PsiBuilder builder) {
        OPP opp;
        opp = null;
        if ((builder.getTokenType()) == RakuTokenTypes.QUOTE_REGEX) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.tribbler_245(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker6;
        quantMarker6 = builder.mark();
        if (this.tribble_244_quant_6(builder, opp)) {
            quantMarker6.drop();
        } else {
            quantMarker6.rollbackTo();
        }
        return true;
    }

    private boolean tribbler_245_alt_1(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.TRANS_CHAR) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.ccstate_23(builder))) {
            return false;
        }
        return true;
    }

    private boolean tribbler_245_alt_2(PsiBuilder builder, OPP opp) {
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.TRANS_BAD) && (tt1.equals(".."))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean tribbler_245_alt_3(PsiBuilder builder, OPP opp) {
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.TRANS_RANGE) && (tt2.equals(".."))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean tribbler_245_alt_4(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker3;
        altMarker3 = builder.mark();
        if (this.tribbler_245_alt_3(builder, opp)) {
            altMarker3.drop();
        } else {
            altMarker3.rollbackTo();
            PsiBuilder.Marker altMarker2;
            altMarker2 = builder.mark();
            if (this.tribbler_245_alt_2(builder, opp)) {
                altMarker2.drop();
            } else {
                altMarker2.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean tribbler_245_alt_5(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.TRANS_BAD) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean tribbler_245_alt_6(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.TRANS_ESCAPE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.ccstate_23(builder))) {
            return false;
        }
        return true;
    }

    private boolean tribbler_245_quant_7(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean tribbler_245_alt_8(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.WHITE_SPACE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker quantMarker7;
        quantMarker7 = builder.mark();
        if (this.tribbler_245_quant_7(builder, opp)) {
            quantMarker7.drop();
        } else {
            quantMarker7.rollbackTo();
        }
        return true;
    }

    private boolean tribbler_245_quant_9(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker8;
        altMarker8 = builder.mark();
        if (this.tribbler_245_alt_8(builder, opp)) {
            altMarker8.drop();
        } else {
            altMarker8.rollbackTo();
            PsiBuilder.Marker altMarker6;
            altMarker6 = builder.mark();
            if (this.tribbler_245_alt_6(builder, opp)) {
                altMarker6.drop();
            } else {
                altMarker6.rollbackTo();
                PsiBuilder.Marker altMarker5;
                altMarker5 = builder.mark();
                if (this.tribbler_245_alt_5(builder, opp)) {
                    altMarker5.drop();
                } else {
                    altMarker5.rollbackTo();
                    PsiBuilder.Marker altMarker4;
                    altMarker4 = builder.mark();
                    if (this.tribbler_245_alt_4(builder, opp)) {
                        altMarker4.drop();
                    } else {
                        altMarker4.rollbackTo();
                        PsiBuilder.Marker altMarker1;
                        altMarker1 = builder.mark();
                        if (this.tribbler_245_alt_1(builder, opp)) {
                            altMarker1.drop();
                        } else {
                            altMarker1.rollbackTo();
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean tribbler_245(PsiBuilder builder) {
        OPP opp;
        opp = null;
        while (true) {
            PsiBuilder.Marker quantMarker9;
            quantMarker9 = builder.mark();
            if (this.tribbler_245_quant_9(builder, opp)) {
                quantMarker9.drop();
            } else {
                quantMarker9.rollbackTo();
                break;
            }
        }
        return true;
    }

    private boolean twigil_246(PsiBuilder builder) {
        OPP opp;
        opp = null;
        return true;
    }

    private boolean type_constraint_247_alt_1(PsiBuilder builder, OPP opp) {
        if (!(this.typename_249(builder))) {
            return false;
        }
        return true;
    }

    private boolean type_constraint_247_alt_2(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker2;
        marker2 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.PREFIX) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.numish_84(builder))) {
            return false;
        }
        marker2.done(RakuElementTypes.VALUE_CONSTRAINT);
        return true;
    }

    private boolean type_constraint_247_alt_3(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker4;
        marker4 = builder.mark();
        if (!(this.value_252(builder))) {
            return false;
        }
        marker4.done(RakuElementTypes.VALUE_CONSTRAINT);
        return true;
    }

    private boolean type_constraint_247_quant_4(PsiBuilder builder, OPP opp) {
        if (!(this.EXPR_2(builder))) {
            return false;
        }
        return true;
    }

    private boolean type_constraint_247_alt_5(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker6;
        marker6 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.WHERE_CONSTRAINT) && (tt1.equals("where"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker7;
        quantMarker7 = builder.mark();
        if (this.type_constraint_247_quant_4(builder, opp)) {
            quantMarker7.drop();
        } else {
            quantMarker7.rollbackTo();
        }
        marker6.done(RakuElementTypes.WHERE_CONSTRAINT);
        return true;
    }

    private boolean type_constraint_247_quant_6(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean type_constraint_247(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker8;
        altMarker8 = builder.mark();
        if (this.type_constraint_247_alt_5(builder, opp)) {
            altMarker8.drop();
        } else {
            altMarker8.rollbackTo();
            PsiBuilder.Marker altMarker5;
            altMarker5 = builder.mark();
            if (this.type_constraint_247_alt_3(builder, opp)) {
                altMarker5.drop();
            } else {
                altMarker5.rollbackTo();
                PsiBuilder.Marker altMarker3;
                altMarker3 = builder.mark();
                if (this.type_constraint_247_alt_2(builder, opp)) {
                    altMarker3.drop();
                } else {
                    altMarker3.rollbackTo();
                    PsiBuilder.Marker altMarker1;
                    altMarker1 = builder.mark();
                    if (this.type_constraint_247_alt_1(builder, opp)) {
                        altMarker1.drop();
                    } else {
                        altMarker1.rollbackTo();
                        return false;
                    }
                }
            }
        }
        PsiBuilder.Marker quantMarker9;
        quantMarker9 = builder.mark();
        if (this.type_constraint_247_quant_6(builder, opp)) {
            quantMarker9.drop();
        } else {
            quantMarker9.rollbackTo();
        }
        return true;
    }

    private boolean type_declarator_248_alt_1(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.CONSTANT_ANON) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean type_declarator_248_alt_2(PsiBuilder builder, OPP opp) {
        if (!(this.variable_253(builder))) {
            return false;
        }
        return true;
    }

    private boolean type_declarator_248_quant_3(PsiBuilder builder, OPP opp) {
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.TERM_DECLARATION_BACKSLASH) && (tt2.equals("\\"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean type_declarator_248_alt_4(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker quantMarker4;
        quantMarker4 = builder.mark();
        if (this.type_declarator_248_quant_3(builder, opp)) {
            quantMarker4.drop();
        } else {
            quantMarker4.rollbackTo();
        }
        if (!(this.defterm_38(builder))) {
            return false;
        }
        return true;
    }

    private boolean type_declarator_248_quant_5(PsiBuilder builder, OPP opp) {
        if (!(this.trait_242(builder))) {
            return false;
        }
        return true;
    }

    private boolean type_declarator_248_alt_6(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.CONSTANT_MISSING_INITIALIZER) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean type_declarator_248_alt_7(PsiBuilder builder, OPP opp) {
        if (!(this.initializer_60(builder))) {
            return false;
        }
        return true;
    }

    private boolean type_declarator_248_alt_8(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.TYPE_DECLARATOR) && (tt1.equals("constant"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.kok_64(builder))) {
            return false;
        }
        PsiBuilder.Marker altMarker5;
        altMarker5 = builder.mark();
        if (this.type_declarator_248_alt_4(builder, opp)) {
            altMarker5.drop();
        } else {
            altMarker5.rollbackTo();
            PsiBuilder.Marker altMarker3;
            altMarker3 = builder.mark();
            if (this.type_declarator_248_alt_2(builder, opp)) {
                altMarker3.drop();
            } else {
                altMarker3.rollbackTo();
                PsiBuilder.Marker altMarker2;
                altMarker2 = builder.mark();
                if (this.type_declarator_248_alt_1(builder, opp)) {
                    altMarker2.drop();
                } else {
                    altMarker2.rollbackTo();
                    return false;
                }
            }
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        while (true) {
            PsiBuilder.Marker quantMarker6;
            quantMarker6 = builder.mark();
            if (this.type_declarator_248_quant_5(builder, opp)) {
                quantMarker6.drop();
            } else {
                quantMarker6.rollbackTo();
                break;
            }
        }
        PsiBuilder.Marker altMarker8;
        altMarker8 = builder.mark();
        if (this.type_declarator_248_alt_7(builder, opp)) {
            altMarker8.drop();
        } else {
            altMarker8.rollbackTo();
            PsiBuilder.Marker altMarker7;
            altMarker7 = builder.mark();
            if (this.type_declarator_248_alt_6(builder, opp)) {
                altMarker7.drop();
            } else {
                altMarker7.rollbackTo();
                return false;
            }
        }
        marker1.done(RakuElementTypes.CONSTANT);
        return true;
    }

    private boolean type_declarator_248_alt_9(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.SUBSET_ANON) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean type_declarator_248_alt_10(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.NAME) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean type_declarator_248_quant_11(PsiBuilder builder, OPP opp) {
        if (!(this.trait_242(builder))) {
            return false;
        }
        return true;
    }

    private boolean type_declarator_248_alt_12(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.SUBSET_INCOMPLETE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean type_declarator_248_alt_13(PsiBuilder builder, OPP opp) {
        if (!(this.EXPR_2(builder))) {
            return false;
        }
        return true;
    }

    private boolean type_declarator_248_quant_14(PsiBuilder builder, OPP opp) {
        String tt4;
        tt4 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.WHERE_CONSTRAINT) && (tt4.equals("where"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker altMarker15;
        altMarker15 = builder.mark();
        if (this.type_declarator_248_alt_13(builder, opp)) {
            altMarker15.drop();
        } else {
            altMarker15.rollbackTo();
            PsiBuilder.Marker altMarker14;
            altMarker14 = builder.mark();
            if (this.type_declarator_248_alt_12(builder, opp)) {
                altMarker14.drop();
            } else {
                altMarker14.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean type_declarator_248_quant_15(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker16;
        quantMarker16 = builder.mark();
        if (this.type_declarator_248_quant_14(builder, opp)) {
            quantMarker16.drop();
        } else {
            quantMarker16.rollbackTo();
        }
        return true;
    }

    private boolean type_declarator_248_quant_16(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean type_declarator_248_quant_17(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker12;
        altMarker12 = builder.mark();
        if (this.type_declarator_248_alt_10(builder, opp)) {
            altMarker12.drop();
        } else {
            altMarker12.rollbackTo();
            PsiBuilder.Marker altMarker11;
            altMarker11 = builder.mark();
            if (this.type_declarator_248_alt_9(builder, opp)) {
                altMarker11.drop();
            } else {
                altMarker11.rollbackTo();
                return false;
            }
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        while (true) {
            PsiBuilder.Marker quantMarker13;
            quantMarker13 = builder.mark();
            if (this.type_declarator_248_quant_11(builder, opp)) {
                quantMarker13.drop();
            } else {
                quantMarker13.rollbackTo();
                break;
            }
        }
        PsiBuilder.Marker quantMarker17;
        quantMarker17 = builder.mark();
        if (this.type_declarator_248_quant_15(builder, opp)) {
            quantMarker17.drop();
        } else {
            quantMarker17.rollbackTo();
        }
        PsiBuilder.Marker quantMarker18;
        quantMarker18 = builder.mark();
        if (this.type_declarator_248_quant_16(builder, opp)) {
            quantMarker18.drop();
        } else {
            quantMarker18.rollbackTo();
        }
        return true;
    }

    private boolean type_declarator_248_alt_18(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker10;
        marker10 = builder.mark();
        String tt3;
        tt3 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.TYPE_DECLARATOR) && (tt3.equals("subset"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.kok_64(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker19;
        quantMarker19 = builder.mark();
        if (this.type_declarator_248_quant_17(builder, opp)) {
            quantMarker19.drop();
        } else {
            quantMarker19.rollbackTo();
        }
        marker10.done(RakuElementTypes.SUBSET);
        return true;
    }

    private boolean type_declarator_248_alt_19(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.ENUM_ANON) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean type_declarator_248_alt_20(PsiBuilder builder, OPP opp) {
        if (!(this.variable_253(builder))) {
            return false;
        }
        return true;
    }

    private boolean type_declarator_248_alt_21(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.NAME) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean type_declarator_248_quant_22(PsiBuilder builder, OPP opp) {
        if (!(this.trait_242(builder))) {
            return false;
        }
        return true;
    }

    private boolean type_declarator_248_alt_23(PsiBuilder builder, OPP opp) {
        if (!(this.term_224(builder))) {
            return false;
        }
        return true;
    }

    private boolean type_declarator_248_alt_24(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.ENUM_INCOMPLETE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean type_declarator_248_quant_25(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker altMarker27;
        altMarker27 = builder.mark();
        if (this.type_declarator_248_alt_24(builder, opp)) {
            altMarker27.drop();
        } else {
            altMarker27.rollbackTo();
            PsiBuilder.Marker altMarker26;
            altMarker26 = builder.mark();
            if (this.type_declarator_248_alt_23(builder, opp)) {
                altMarker26.drop();
            } else {
                altMarker26.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean type_declarator_248_quant_26(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean type_declarator_248_quant_27(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker24;
        altMarker24 = builder.mark();
        if (this.type_declarator_248_alt_21(builder, opp)) {
            altMarker24.drop();
        } else {
            altMarker24.rollbackTo();
            PsiBuilder.Marker altMarker23;
            altMarker23 = builder.mark();
            if (this.type_declarator_248_alt_20(builder, opp)) {
                altMarker23.drop();
            } else {
                altMarker23.rollbackTo();
                PsiBuilder.Marker altMarker22;
                altMarker22 = builder.mark();
                if (this.type_declarator_248_alt_19(builder, opp)) {
                    altMarker22.drop();
                } else {
                    altMarker22.rollbackTo();
                    return false;
                }
            }
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        while (true) {
            PsiBuilder.Marker quantMarker25;
            quantMarker25 = builder.mark();
            if (this.type_declarator_248_quant_22(builder, opp)) {
                quantMarker25.drop();
            } else {
                quantMarker25.rollbackTo();
                break;
            }
        }
        PsiBuilder.Marker quantMarker28;
        quantMarker28 = builder.mark();
        if (this.type_declarator_248_quant_25(builder, opp)) {
            quantMarker28.drop();
        } else {
            quantMarker28.rollbackTo();
        }
        PsiBuilder.Marker quantMarker29;
        quantMarker29 = builder.mark();
        if (this.type_declarator_248_quant_26(builder, opp)) {
            quantMarker29.drop();
        } else {
            quantMarker29.rollbackTo();
        }
        return true;
    }

    private boolean type_declarator_248_alt_28(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker21;
        marker21 = builder.mark();
        String tt5;
        tt5 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.TYPE_DECLARATOR) && (tt5.equals("enum"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.kok_64(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker30;
        quantMarker30 = builder.mark();
        if (this.type_declarator_248_quant_27(builder, opp)) {
            quantMarker30.drop();
        } else {
            quantMarker30.rollbackTo();
        }
        marker21.done(RakuElementTypes.ENUM);
        return true;
    }

    private boolean type_declarator_248(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker31;
        altMarker31 = builder.mark();
        if (this.type_declarator_248_alt_28(builder, opp)) {
            altMarker31.drop();
        } else {
            altMarker31.rollbackTo();
            PsiBuilder.Marker altMarker20;
            altMarker20 = builder.mark();
            if (this.type_declarator_248_alt_18(builder, opp)) {
                altMarker20.drop();
            } else {
                altMarker20.rollbackTo();
                PsiBuilder.Marker altMarker9;
                altMarker9 = builder.mark();
                if (this.type_declarator_248_alt_8(builder, opp)) {
                    altMarker9.drop();
                } else {
                    altMarker9.rollbackTo();
                    return false;
                }
            }
        }
        return true;
    }

    private boolean typename_249_alt_1(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.NAME) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.longname_colonpairs_67(builder))) {
            return false;
        }
        return true;
    }

    private boolean typename_249_alt_2(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.NAME) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.longname_colonpairs_67(builder))) {
            return false;
        }
        return true;
    }

    private boolean typename_249_quant_3(PsiBuilder builder, OPP opp) {
        if (!(this.unsp_250(builder))) {
            return false;
        }
        return true;
    }

    private boolean typename_249_quant_4(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean typename_249_quant_5(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker quantMarker6;
        quantMarker6 = builder.mark();
        if (this.typename_249_quant_4(builder, opp)) {
            quantMarker6.drop();
        } else {
            quantMarker6.rollbackTo();
        }
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.TYPE_PARAMETER_BRACKET) && (tt2.equals("]"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean typename_249_quant_6(PsiBuilder builder, OPP opp) {
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.TYPE_PARAMETER_BRACKET) && (tt1.equals("["))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.arglist_6(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker7;
        quantMarker7 = builder.mark();
        if (this.typename_249_quant_5(builder, opp)) {
            quantMarker7.drop();
        } else {
            quantMarker7.rollbackTo();
        }
        return true;
    }

    private boolean typename_249_quant_7(PsiBuilder builder, OPP opp) {
        if (!(this.unsp_250(builder))) {
            return false;
        }
        return true;
    }

    private boolean typename_249_quant_8(PsiBuilder builder, OPP opp) {
        if (!(this.typename_249(builder))) {
            return false;
        }
        return true;
    }

    private boolean typename_249_alt_9(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.INCOMPLETE_TYPE_NAME) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean typename_249_alt_10(PsiBuilder builder, OPP opp) {
        String tt4;
        tt4 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.TYPE_COERCION_PARENTHESES_CLOSE) && (tt4.equals(")"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean typename_249_quant_11(PsiBuilder builder, OPP opp) {
        String tt3;
        tt3 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.TYPE_COERCION_PARENTHESES_OPEN) && (tt3.equals("("))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker10;
        quantMarker10 = builder.mark();
        if (this.typename_249_quant_8(builder, opp)) {
            quantMarker10.drop();
        } else {
            quantMarker10.rollbackTo();
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker altMarker12;
        altMarker12 = builder.mark();
        if (this.typename_249_alt_10(builder, opp)) {
            altMarker12.drop();
        } else {
            altMarker12.rollbackTo();
            PsiBuilder.Marker altMarker11;
            altMarker11 = builder.mark();
            if (this.typename_249_alt_9(builder, opp)) {
                altMarker11.drop();
            } else {
                altMarker11.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean typename_249_alt_12(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.INCOMPLETE_TYPE_NAME) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean typename_249_alt_13(PsiBuilder builder, OPP opp) {
        if (!(this.typename_249(builder))) {
            return false;
        }
        return true;
    }

    private boolean typename_249_quant_14(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        String tt5;
        tt5 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.NAME) && (tt5.equals("of"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker altMarker15;
        altMarker15 = builder.mark();
        if (this.typename_249_alt_13(builder, opp)) {
            altMarker15.drop();
        } else {
            altMarker15.rollbackTo();
            PsiBuilder.Marker altMarker14;
            altMarker14 = builder.mark();
            if (this.typename_249_alt_12(builder, opp)) {
                altMarker14.drop();
            } else {
                altMarker14.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean typename_249(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        PsiBuilder.Marker marker2;
        marker2 = builder.mark();
        PsiBuilder.Marker altMarker4;
        altMarker4 = builder.mark();
        if (this.typename_249_alt_2(builder, opp)) {
            altMarker4.drop();
        } else {
            altMarker4.rollbackTo();
            PsiBuilder.Marker altMarker3;
            altMarker3 = builder.mark();
            if (this.typename_249_alt_1(builder, opp)) {
                altMarker3.drop();
            } else {
                altMarker3.rollbackTo();
                return false;
            }
        }
        marker2.done(RakuElementTypes.LONG_NAME);
        PsiBuilder.Marker quantMarker5;
        quantMarker5 = builder.mark();
        if (this.typename_249_quant_3(builder, opp)) {
            quantMarker5.drop();
        } else {
            quantMarker5.rollbackTo();
        }
        PsiBuilder.Marker quantMarker8;
        quantMarker8 = builder.mark();
        if (this.typename_249_quant_6(builder, opp)) {
            quantMarker8.drop();
        } else {
            quantMarker8.rollbackTo();
        }
        PsiBuilder.Marker quantMarker9;
        quantMarker9 = builder.mark();
        if (this.typename_249_quant_7(builder, opp)) {
            quantMarker9.drop();
        } else {
            quantMarker9.rollbackTo();
        }
        PsiBuilder.Marker quantMarker13;
        quantMarker13 = builder.mark();
        if (this.typename_249_quant_11(builder, opp)) {
            quantMarker13.drop();
        } else {
            quantMarker13.rollbackTo();
        }
        PsiBuilder.Marker quantMarker16;
        quantMarker16 = builder.mark();
        if (this.typename_249_quant_14(builder, opp)) {
            quantMarker16.drop();
        } else {
            quantMarker16.rollbackTo();
        }
        marker1.done(RakuElementTypes.TYPE_NAME);
        return true;
    }

    private boolean unsp_250_alt_1(PsiBuilder builder, OPP opp) {
        if (!(this.unsp_250(builder))) {
            return false;
        }
        return true;
    }

    private boolean unsp_250_alt_2(PsiBuilder builder, OPP opp) {
        if (!(this.unv_251(builder))) {
            return false;
        }
        return true;
    }

    private boolean unsp_250_alt_3(PsiBuilder builder, OPP opp) {
        if (!(this.vws_257(builder))) {
            return false;
        }
        return true;
    }

    private boolean unsp_250_quant_4(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker3;
        altMarker3 = builder.mark();
        if (this.unsp_250_alt_3(builder, opp)) {
            altMarker3.drop();
        } else {
            altMarker3.rollbackTo();
            PsiBuilder.Marker altMarker2;
            altMarker2 = builder.mark();
            if (this.unsp_250_alt_2(builder, opp)) {
                altMarker2.drop();
            } else {
                altMarker2.rollbackTo();
                PsiBuilder.Marker altMarker1;
                altMarker1 = builder.mark();
                if (this.unsp_250_alt_1(builder, opp)) {
                    altMarker1.drop();
                } else {
                    altMarker1.rollbackTo();
                    return false;
                }
            }
        }
        return true;
    }

    private boolean unsp_250(PsiBuilder builder) {
        OPP opp;
        opp = null;
        if ((builder.getTokenType()) == RakuTokenTypes.UNSP_WHITE_SPACE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        while (true) {
            PsiBuilder.Marker quantMarker4;
            quantMarker4 = builder.mark();
            if (this.unsp_250_quant_4(builder, opp)) {
                quantMarker4.drop();
            } else {
                quantMarker4.rollbackTo();
                break;
            }
        }
        return true;
    }

    private boolean unv_251_quant_1(PsiBuilder builder, OPP opp) {
        if (!(this.comment_31(builder))) {
            return false;
        }
        return true;
    }

    private boolean unv_251_alt_2(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.UNV_WHITE_SPACE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker quantMarker1;
        quantMarker1 = builder.mark();
        if (this.unv_251_quant_1(builder, opp)) {
            quantMarker1.drop();
        } else {
            quantMarker1.rollbackTo();
        }
        return true;
    }

    private boolean unv_251_alt_3(PsiBuilder builder, OPP opp) {
        if (!(this.pod_content_toplevel_106(builder))) {
            return false;
        }
        return true;
    }

    private boolean unv_251_alt_4(PsiBuilder builder, OPP opp) {
        if (!(this.comment_31(builder))) {
            return false;
        }
        return true;
    }

    private boolean unv_251(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker4;
        altMarker4 = builder.mark();
        if (this.unv_251_alt_4(builder, opp)) {
            altMarker4.drop();
        } else {
            altMarker4.rollbackTo();
            PsiBuilder.Marker altMarker3;
            altMarker3 = builder.mark();
            if (this.unv_251_alt_3(builder, opp)) {
                altMarker3.drop();
            } else {
                altMarker3.rollbackTo();
                PsiBuilder.Marker altMarker2;
                altMarker2 = builder.mark();
                if (this.unv_251_alt_2(builder, opp)) {
                    altMarker2.drop();
                } else {
                    altMarker2.rollbackTo();
                    return false;
                }
            }
        }
        return true;
    }

    private boolean value_252_alt_1(PsiBuilder builder, OPP opp) {
        if (!(this.version_255(builder))) {
            return false;
        }
        return true;
    }

    private boolean value_252_alt_2(PsiBuilder builder, OPP opp) {
        if (!(this.quote_128(builder))) {
            return false;
        }
        return true;
    }

    private boolean value_252_alt_3(PsiBuilder builder, OPP opp) {
        if (!(this.number_83(builder))) {
            return false;
        }
        return true;
    }

    private boolean value_252(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker3;
        altMarker3 = builder.mark();
        if (this.value_252_alt_3(builder, opp)) {
            altMarker3.drop();
        } else {
            altMarker3.rollbackTo();
            PsiBuilder.Marker altMarker2;
            altMarker2 = builder.mark();
            if (this.value_252_alt_2(builder, opp)) {
                altMarker2.drop();
            } else {
                altMarker2.rollbackTo();
                PsiBuilder.Marker altMarker1;
                altMarker1 = builder.mark();
                if (this.value_252_alt_1(builder, opp)) {
                    altMarker1.drop();
                } else {
                    altMarker1.rollbackTo();
                    return false;
                }
            }
        }
        return true;
    }

    private boolean variable_253_alt_1(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean variable_253_alt_2(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean variable_253_alt_3(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean variable_253_alt_4(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean variable_253_quant_5(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker4;
        altMarker4 = builder.mark();
        if (this.variable_253_alt_4(builder, opp)) {
            altMarker4.drop();
        } else {
            altMarker4.rollbackTo();
            PsiBuilder.Marker altMarker3;
            altMarker3 = builder.mark();
            if (this.variable_253_alt_3(builder, opp)) {
                altMarker3.drop();
            } else {
                altMarker3.rollbackTo();
                PsiBuilder.Marker altMarker2;
                altMarker2 = builder.mark();
                if (this.variable_253_alt_2(builder, opp)) {
                    altMarker2.drop();
                } else {
                    altMarker2.rollbackTo();
                    PsiBuilder.Marker altMarker1;
                    altMarker1 = builder.mark();
                    if (this.variable_253_alt_1(builder, opp)) {
                        altMarker1.drop();
                    } else {
                        altMarker1.rollbackTo();
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean variable_253_alt_6(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker6;
        marker6 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.VARIABLE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        marker6.done(RakuElementTypes.VARIABLE);
        return true;
    }

    private boolean variable_253_alt_7(PsiBuilder builder, OPP opp) {
        if (!(this.contextualizer_33(builder))) {
            return false;
        }
        return true;
    }

    private boolean variable_253_alt_8(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker9;
        marker9 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.REGEX_CAPTURE_NAME) {
            builder.advanceLexer();
        } else {
            return false;
        }
        marker9.done(RakuElementTypes.VARIABLE);
        return true;
    }

    private boolean variable_253_quant_9(PsiBuilder builder, OPP opp) {
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.REGEX_CAPTURE_NAME) && (tt1.equals(">"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean variable_253_alt_10(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.VARIABLE_REGEX_NAMED_CAPTURE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker marker11;
        marker11 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.REGEX_CAPTURE_NAME) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.quote_q_135(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker12;
        quantMarker12 = builder.mark();
        if (this.variable_253_quant_9(builder, opp)) {
            quantMarker12.drop();
        } else {
            quantMarker12.rollbackTo();
        }
        marker11.done(RakuElementTypes.VARIABLE);
        return true;
    }

    private boolean variable_253_alt_11(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker14;
        marker14 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.VARIABLE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        marker14.done(RakuElementTypes.VARIABLE);
        return true;
    }

    private boolean variable_253_alt_12(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker16;
        marker16 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.VARIABLE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.longname_colonpairs_67(builder))) {
            return false;
        }
        marker16.done(RakuElementTypes.VARIABLE);
        return true;
    }

    private boolean variable_253_alt_13(PsiBuilder builder, OPP opp) {
        String tt3;
        tt3 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.WHITE_SPACE) && (tt3.equals("\\"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean variable_253_alt_14(PsiBuilder builder, OPP opp) {
        if (!(this.unsp_250(builder))) {
            return false;
        }
        return true;
    }

    private boolean variable_253_quant_15(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker23;
        altMarker23 = builder.mark();
        if (this.variable_253_alt_14(builder, opp)) {
            altMarker23.drop();
        } else {
            altMarker23.rollbackTo();
            PsiBuilder.Marker altMarker22;
            altMarker22 = builder.mark();
            if (this.variable_253_alt_13(builder, opp)) {
                altMarker22.drop();
            } else {
                altMarker22.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean variable_253_quant_16(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.SELF_CALL_VARIABLE_ARGS) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker quantMarker24;
        quantMarker24 = builder.mark();
        if (this.variable_253_quant_15(builder, opp)) {
            quantMarker24.drop();
        } else {
            quantMarker24.rollbackTo();
        }
        if (!(this.postcircumfix_115(builder))) {
            return false;
        }
        return true;
    }

    private boolean variable_253_quant_17(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker21;
        marker21 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.METHOD_CALL_NAME) {
            builder.advanceLexer();
        } else {
            return false;
        }
        marker21.done(RakuElementTypes.LONG_NAME);
        PsiBuilder.Marker quantMarker25;
        quantMarker25 = builder.mark();
        if (this.variable_253_quant_16(builder, opp)) {
            quantMarker25.drop();
        } else {
            quantMarker25.rollbackTo();
        }
        return true;
    }

    private boolean variable_253_alt_18(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.SELF_CALL_VARIABLE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker marker18;
        marker18 = builder.mark();
        PsiBuilder.Marker marker19;
        marker19 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.SELF) {
            builder.advanceLexer();
        } else {
            return false;
        }
        marker19.done(RakuElementTypes.SELF);
        PsiBuilder.Marker marker20;
        marker20 = builder.mark();
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.METHOD_CALL_OPERATOR) && (tt2.equals("."))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker quantMarker26;
        quantMarker26 = builder.mark();
        if (this.variable_253_quant_17(builder, opp)) {
            quantMarker26.drop();
        } else {
            quantMarker26.rollbackTo();
        }
        marker20.done(RakuElementTypes.METHOD_CALL);
        marker18.done(RakuElementTypes.POSTFIX_APPLICATION);
        return true;
    }

    private boolean variable_253_quant_19(PsiBuilder builder, OPP opp) {
        String tt5;
        tt5 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.VARIABLE) && (tt5.equals("]"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean variable_253_quant_20(PsiBuilder builder, OPP opp) {
        if (!(this.infixish_57(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker29;
        quantMarker29 = builder.mark();
        if (this.variable_253_quant_19(builder, opp)) {
            quantMarker29.drop();
        } else {
            quantMarker29.rollbackTo();
        }
        return true;
    }

    private boolean variable_253_alt_21(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker marker28;
        marker28 = builder.mark();
        String tt4;
        tt4 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.VARIABLE) && (tt4.equals("&["))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if ((builder.getTokenType()) == RakuTokenTypes.INFIX_NOUN_VARIABLE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker quantMarker30;
        quantMarker30 = builder.mark();
        if (this.variable_253_quant_20(builder, opp)) {
            quantMarker30.drop();
        } else {
            quantMarker30.rollbackTo();
        }
        marker28.done(RakuElementTypes.VARIABLE);
        return true;
    }

    private boolean variable_253_alt_22(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.SIMPLE_CONTEXTUALIZER) {
            builder.advanceLexer();
        } else {
            return false;
        }
        PsiBuilder.Marker marker32;
        marker32 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.CONTEXTUALIZER) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.variable_253(builder))) {
            return false;
        }
        marker32.done(RakuElementTypes.CONTEXTUALIZER);
        return true;
    }

    private boolean variable_253(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker quantMarker5;
        quantMarker5 = builder.mark();
        if (this.variable_253_quant_5(builder, opp)) {
            quantMarker5.drop();
        } else {
            quantMarker5.rollbackTo();
        }
        PsiBuilder.Marker altMarker33;
        altMarker33 = builder.mark();
        if (this.variable_253_alt_22(builder, opp)) {
            altMarker33.drop();
        } else {
            altMarker33.rollbackTo();
            PsiBuilder.Marker altMarker31;
            altMarker31 = builder.mark();
            if (this.variable_253_alt_21(builder, opp)) {
                altMarker31.drop();
            } else {
                altMarker31.rollbackTo();
                PsiBuilder.Marker altMarker27;
                altMarker27 = builder.mark();
                if (this.variable_253_alt_18(builder, opp)) {
                    altMarker27.drop();
                } else {
                    altMarker27.rollbackTo();
                    PsiBuilder.Marker altMarker17;
                    altMarker17 = builder.mark();
                    if (this.variable_253_alt_12(builder, opp)) {
                        altMarker17.drop();
                    } else {
                        altMarker17.rollbackTo();
                        PsiBuilder.Marker altMarker15;
                        altMarker15 = builder.mark();
                        if (this.variable_253_alt_11(builder, opp)) {
                            altMarker15.drop();
                        } else {
                            altMarker15.rollbackTo();
                            PsiBuilder.Marker altMarker13;
                            altMarker13 = builder.mark();
                            if (this.variable_253_alt_10(builder, opp)) {
                                altMarker13.drop();
                            } else {
                                altMarker13.rollbackTo();
                                PsiBuilder.Marker altMarker10;
                                altMarker10 = builder.mark();
                                if (this.variable_253_alt_8(builder, opp)) {
                                    altMarker10.drop();
                                } else {
                                    altMarker10.rollbackTo();
                                    PsiBuilder.Marker altMarker8;
                                    altMarker8 = builder.mark();
                                    if (this.variable_253_alt_7(builder, opp)) {
                                        altMarker8.drop();
                                    } else {
                                        altMarker8.rollbackTo();
                                        PsiBuilder.Marker altMarker7;
                                        altMarker7 = builder.mark();
                                        if (this.variable_253_alt_6(builder, opp)) {
                                            altMarker7.drop();
                                        } else {
                                            altMarker7.rollbackTo();
                                            return false;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean variable_declarator_254_quant_1(PsiBuilder builder, OPP opp) {
        if (!(this.unsp_250(builder))) {
            return false;
        }
        return true;
    }

    private boolean variable_declarator_254_quant_2(PsiBuilder builder, OPP opp) {
        String tt2;
        tt2 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.SHAPE_DECLARATION) && (tt2.equals("}"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean variable_declarator_254_alt_3(PsiBuilder builder, OPP opp) {
        String tt1;
        tt1 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.SHAPE_DECLARATION) && (tt1.equals("{"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.semilist_168(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker2;
        quantMarker2 = builder.mark();
        if (this.variable_declarator_254_quant_2(builder, opp)) {
            quantMarker2.drop();
        } else {
            quantMarker2.rollbackTo();
        }
        return true;
    }

    private boolean variable_declarator_254_quant_4(PsiBuilder builder, OPP opp) {
        String tt4;
        tt4 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.SHAPE_DECLARATION) && (tt4.equals("]"))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean variable_declarator_254_alt_5(PsiBuilder builder, OPP opp) {
        String tt3;
        tt3 = builder.getTokenText();
        if (((builder.getTokenType()) == RakuTokenTypes.SHAPE_DECLARATION) && (tt3.equals("["))) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.semilist_168(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker4;
        quantMarker4 = builder.mark();
        if (this.variable_declarator_254_quant_4(builder, opp)) {
            quantMarker4.drop();
        } else {
            quantMarker4.rollbackTo();
        }
        return true;
    }

    private boolean variable_declarator_254_quant_6(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker5;
        altMarker5 = builder.mark();
        if (this.variable_declarator_254_alt_5(builder, opp)) {
            altMarker5.drop();
        } else {
            altMarker5.rollbackTo();
            PsiBuilder.Marker altMarker3;
            altMarker3 = builder.mark();
            if (this.variable_declarator_254_alt_3(builder, opp)) {
                altMarker3.drop();
            } else {
                altMarker3.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean variable_declarator_254_quant_7(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker quantMarker1;
        quantMarker1 = builder.mark();
        if (this.variable_declarator_254_quant_1(builder, opp)) {
            quantMarker1.drop();
        } else {
            quantMarker1.rollbackTo();
        }
        PsiBuilder.Marker quantMarker6;
        quantMarker6 = builder.mark();
        if (this.variable_declarator_254_quant_6(builder, opp)) {
            quantMarker6.drop();
        } else {
            quantMarker6.rollbackTo();
            return false;
        }
        while (true) {
            PsiBuilder.Marker quantMarker7;
            quantMarker7 = builder.mark();
            if (this.variable_declarator_254_quant_6(builder, opp)) {
                quantMarker7.drop();
            } else {
                quantMarker7.rollbackTo();
                break;
            }
        }
        return true;
    }

    private boolean variable_declarator_254_quant_8(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        return true;
    }

    private boolean variable_declarator_254_quant_9(PsiBuilder builder, OPP opp) {
        if (!(this.trait_242(builder))) {
            return false;
        }
        return true;
    }

    private boolean variable_declarator_254_quant_10(PsiBuilder builder, OPP opp) {
        if (!(this.post_constraint_114(builder))) {
            return false;
        }
        return true;
    }

    private boolean variable_declarator_254_quant_11(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        while (true) {
            PsiBuilder.Marker quantMarker11;
            quantMarker11 = builder.mark();
            if (this.variable_declarator_254_quant_10(builder, opp)) {
                quantMarker11.drop();
            } else {
                quantMarker11.rollbackTo();
                break;
            }
        }
        return true;
    }

    private boolean variable_declarator_254(PsiBuilder builder) {
        OPP opp;
        opp = null;
        if (!(this.variable_253(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker8;
        quantMarker8 = builder.mark();
        if (this.variable_declarator_254_quant_7(builder, opp)) {
            quantMarker8.drop();
        } else {
            quantMarker8.rollbackTo();
        }
        PsiBuilder.Marker quantMarker9;
        quantMarker9 = builder.mark();
        if (this.variable_declarator_254_quant_8(builder, opp)) {
            quantMarker9.drop();
        } else {
            quantMarker9.rollbackTo();
        }
        while (true) {
            PsiBuilder.Marker quantMarker10;
            quantMarker10 = builder.mark();
            if (this.variable_declarator_254_quant_9(builder, opp)) {
                quantMarker10.drop();
            } else {
                quantMarker10.rollbackTo();
                break;
            }
        }
        PsiBuilder.Marker quantMarker12;
        quantMarker12 = builder.mark();
        if (this.variable_declarator_254_quant_11(builder, opp)) {
            quantMarker12.drop();
        } else {
            quantMarker12.rollbackTo();
        }
        return true;
    }

    private boolean version_255(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker marker1;
        marker1 = builder.mark();
        if ((builder.getTokenType()) == RakuTokenTypes.VERSION) {
            builder.advanceLexer();
        } else {
            return false;
        }
        marker1.done(RakuElementTypes.VERSION);
        return true;
    }

    private boolean vnum_256_alt_1(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean vnum_256_quant_2(PsiBuilder builder, OPP opp) {
        return true;
    }

    private boolean vnum_256_alt_3(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker quantMarker2;
        quantMarker2 = builder.mark();
        if (this.vnum_256_quant_2(builder, opp)) {
            quantMarker2.drop();
        } else {
            quantMarker2.rollbackTo();
            return false;
        }
        while (true) {
            PsiBuilder.Marker quantMarker3;
            quantMarker3 = builder.mark();
            if (this.vnum_256_quant_2(builder, opp)) {
                quantMarker3.drop();
            } else {
                quantMarker3.rollbackTo();
                break;
            }
        }
        return true;
    }

    private boolean vnum_256(PsiBuilder builder) {
        OPP opp;
        opp = null;
        PsiBuilder.Marker altMarker4;
        altMarker4 = builder.mark();
        if (this.vnum_256_alt_3(builder, opp)) {
            altMarker4.drop();
        } else {
            altMarker4.rollbackTo();
            PsiBuilder.Marker altMarker1;
            altMarker1 = builder.mark();
            if (this.vnum_256_alt_1(builder, opp)) {
                altMarker1.drop();
            } else {
                altMarker1.rollbackTo();
                return false;
            }
        }
        return true;
    }

    private boolean vws_257(PsiBuilder builder) {
        OPP opp;
        opp = null;
        if ((builder.getTokenType()) == RakuTokenTypes.VERTICAL_WHITE_SPACE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        return true;
    }

    private boolean ws_258_alt_1(PsiBuilder builder, OPP opp) {
        if (!(this.unsp_250(builder))) {
            return false;
        }
        return true;
    }

    private boolean ws_258_alt_2(PsiBuilder builder, OPP opp) {
        if (!(this.unv_251(builder))) {
            return false;
        }
        return true;
    }

    private boolean ws_258_alt_3(PsiBuilder builder, OPP opp) {
        if ((builder.getTokenType()) == RakuTokenTypes.WHITE_SPACE) {
            builder.advanceLexer();
        } else {
            return false;
        }
        if (!(this.heredoc_49(builder))) {
            return false;
        }
        return true;
    }

    private boolean ws_258_quant_4(PsiBuilder builder, OPP opp) {
        PsiBuilder.Marker altMarker3;
        altMarker3 = builder.mark();
        if (this.ws_258_alt_3(builder, opp)) {
            altMarker3.drop();
        } else {
            altMarker3.rollbackTo();
            PsiBuilder.Marker altMarker2;
            altMarker2 = builder.mark();
            if (this.ws_258_alt_2(builder, opp)) {
                altMarker2.drop();
            } else {
                altMarker2.rollbackTo();
                PsiBuilder.Marker altMarker1;
                altMarker1 = builder.mark();
                if (this.ws_258_alt_1(builder, opp)) {
                    altMarker1.drop();
                } else {
                    altMarker1.rollbackTo();
                    return false;
                }
            }
        }
        return true;
    }

    private boolean ws_258(PsiBuilder builder) {
        OPP opp;
        opp = null;
        while (true) {
            PsiBuilder.Marker quantMarker4;
            quantMarker4 = builder.mark();
            if (this.ws_258_quant_4(builder, opp)) {
                quantMarker4.drop();
            } else {
                quantMarker4.rollbackTo();
                break;
            }
        }
        return true;
    }

    private boolean xblock_259_quant_1(PsiBuilder builder, OPP opp) {
        if (!(this.pblock_95(builder))) {
            return false;
        }
        return true;
    }

    private boolean xblock_259_quant_2(PsiBuilder builder, OPP opp) {
        if (!(this.ws_258(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker1;
        quantMarker1 = builder.mark();
        if (this.xblock_259_quant_1(builder, opp)) {
            quantMarker1.drop();
        } else {
            quantMarker1.rollbackTo();
        }
        return true;
    }

    private boolean xblock_259(PsiBuilder builder) {
        OPP opp;
        opp = null;
        if (!(this.EXPR_2(builder))) {
            return false;
        }
        PsiBuilder.Marker quantMarker2;
        quantMarker2 = builder.mark();
        if (this.xblock_259_quant_2(builder, opp)) {
            quantMarker2.drop();
        } else {
            quantMarker2.rollbackTo();
        }
        return true;
    }

}
