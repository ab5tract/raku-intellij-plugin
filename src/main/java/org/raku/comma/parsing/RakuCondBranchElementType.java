package org.raku.comma.parsing;

import com.intellij.psi.tree.ILazyParseableElementType;
import org.raku.comma.RakuLanguage;

/**
 * One inactive #?if branch, carried through the outer parse as a single
 * "whitespace" token so the generated parser never sees it, and lazily
 * parsed as a Raku statement list ("as if its directive were true") when the
 * PSI below it is first needed. The default ILazyParseableElementType
 * parseContents implementation reuses the language's ParserDefinition, which
 * is exactly what we want: pure lex/parse, no resolution, safe during stub
 * building.
 */
public class RakuCondBranchElementType extends ILazyParseableElementType {
    public RakuCondBranchElementType() {
        super("CONDITIONAL_BRANCH", RakuLanguage.INSTANCE);
    }

    @Override
    public String toString() {
        return "Raku:" + super.toString();
    }
}
