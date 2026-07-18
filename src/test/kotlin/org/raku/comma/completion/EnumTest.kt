package org.raku.comma.completion

import com.intellij.codeInsight.completion.CompletionType
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.filetypes.RakuScriptFileType

import java.util.Arrays
import java.util.Collections

class EnumTest : CommaFixtureTestCase() {
    fun testEnumCompletionStringLiteral() {
        doTest("enum Phospho <FooName1 FooName2>; my FooName<caret>",
               Arrays.asList("FooName1", "FooName2", "Phospho::FooName1", "Phospho::FooName2"), 4)
    }

    fun testEnumCompletionNamedValues() {
        doTest("enum Phospho ( FooName1 => 1, FooName2 => 2 ); my Phosph<caret>",
               Arrays.asList("Phospho::FooName1", "Phospho::FooName2", "Phospho"), 3)
    }

    fun testEnumFullReference() {
        doTest("enum Phospho <\nFoo1\nFoo2\n>; my Phosph<caret>", Arrays.asList("Phospho", "Phospho::Foo1", "Phospho::Foo2"), 3)
    }

    fun testExternalEnum() {
        doTest("SeekTy<caret>", Arrays.asList("SeekType", "SeekType::SeekFromBeginning", "SeekType::SeekFromCurrent", "SeekType::SeekFromEnd"), 4)
    }

    fun testExternalSubset() {
        doTest("UIn<caret>", Collections.singletonList("UInt"), 5)
    }

    private fun doTest(text: String, values: List<String>, len: Int) {
        myFixture.configureByText(RakuScriptFileType.INSTANCE, text)
        myFixture.complete(CompletionType.BASIC, 1)
        val types = myFixture.getLookupElementStrings()!!
        assertNotNull(types)
        assertContainsElements(types, values)
        assertEquals(len, types.size)
    }
}
