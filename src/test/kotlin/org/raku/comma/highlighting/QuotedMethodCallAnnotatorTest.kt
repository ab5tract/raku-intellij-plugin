package org.raku.comma.highlighting

import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.filetypes.RakuScriptFileType

// A quoted method call (e.g. `$a."<"(5)`) has no simple name;
// RakuSemanticAnnotator used to pass the null through a non-null Kotlin
// parameter and crash. The test harness escalates the logged annotator
// exception to a failure.
class QuotedMethodCallAnnotatorTest : CommaFixtureTestCase() {

    fun testQuotedMethodCallDoesNotCrashAnnotator() {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, "my \$a = 4;\n\$a.\"<\"(5);\n")
        myFixture.doHighlighting()
    }
}
