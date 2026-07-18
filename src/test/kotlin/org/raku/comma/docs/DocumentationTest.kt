package org.raku.comma.docs

import com.intellij.codeInsight.documentation.DocumentationManager
import com.intellij.psi.PsiPolyVariantReference
import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.psi.RakuMethodCall

class DocumentationTest : CommaFixtureTestCase() {
    override fun getTestDataPath(): String {
        return "testData/docs"
    }

    private fun testGeneratedDoc(result: String) {
        myFixture.configureByFile(getTestName(true) + ".p6")
        val element = myFixture.getElementAtCaret()
        val provider = DocumentationManager.getProviderFromElement(element)
        val generatedDoc = provider.generateDoc(element, null)
        assertEquals(result, generatedDoc)
    }

    private fun testQuickDoc(result: String) {
        myFixture.configureByFile(getTestName(true) + ".p6")
        val element = myFixture.getElementAtCaret()
        val provider = DocumentationManager.getProviderFromElement(element)
        val quickNavigateInfo = provider.getQuickNavigateInfo(element, null)
        assertEquals(result, quickNavigateInfo)
    }

    private fun testURL(result: String) {
        myFixture.configureByFile(getTestName(true) + ".p6")
        val element = myFixture.getElementAtCaret()
        val provider = DocumentationManager.getProviderFromElement(element)
        val urls = provider.getUrlFor(element, null)
        assertContainsElements(urls!!, result)
    }

    fun testQuickDocsClass() {
        testGeneratedDoc("Base class for magicians<br>Trailing one")
        testQuickDoc("class Magician is Cool does Int")
    }

    fun testQuickDocsRole() {
        testGeneratedDoc("Base role for magicians<br>Trailing one")
        testQuickDoc("role Magician")
    }

    fun testQuickDocsGrammar() {
        testGeneratedDoc("Base grammar for magicians<br>Trailing one")
        testQuickDoc("grammar Magician")
    }

    fun testQuickDocsRoutine() {
        testGeneratedDoc("Fight mechanics<br>Magicians only, no mortals.")
        testQuickDoc("sub duel(Magician \$a, Magician \$b)")
    }

    fun testQuickDocsAttribute() {
        testGeneratedDoc("One<br>Two<br>Three!<br>Very important variable")
        testQuickDoc("has \$!foo, \$.foo = 42 (attribute of Foo)")
    }

    fun testQuickDocsAttributeNoInit() {
        testQuickDoc("has \$!foo, \$.foo (attribute of Foo)")
    }

    fun testQuickDocsPrivateMethod() {
        testGeneratedDoc("Fight mechanics<br>Magicians only, no mortals.")
        testQuickDoc("method !duel(Magician \$a, Magician \$b)")
    }

    fun testQuickDocsMethod() {
        testGeneratedDoc("Fight mechanics<br>Magicians only, no mortals.")
        testQuickDoc("method duel(Magician \$a, Magician \$b)")
    }

    fun testQuickDocsLongPre() {
        testGeneratedDoc("Opening.<br>Second sentence")
        testQuickDoc("class QuickDocsLongPre")
    }

    fun testQuickDocsLongPost() {
        testGeneratedDoc("Opening.<br>Second sentence")
        testQuickDoc("class QuickDocsLongPost")
    }

    fun testQuickDocsLong() {
        testGeneratedDoc("Opening.<br>Second sentence<br>Ending.<br>Third sentence")
        testQuickDoc("class QuickDocsLongPre")
    }

    fun testQuickSubset() {
        testQuickDoc("subset Pos of Int")
    }

    fun testQuickEnum() {
        testQuickDoc("enum Nums (One, Two)")
    }

    fun testQuickParameter() {
        testGeneratedDoc("an important parameter<br>documented nicely")
        testQuickDoc(":\$parameter = 42")
    }

    fun testParamIntegration1() {
        testGeneratedDoc("documented int param 1<br>documented int param 2")
    }
    fun testParamIntegration2() {
        testGeneratedDoc("documented named foo 1<br>documented named foo 2<br>documented named foo 3")
    }
    fun testParamIntegration3() {
        testGeneratedDoc("foo docs 1<br>foo docs 2")
    }

    fun testQuickRegex() {
        testQuickDoc("regex foo { one two }")
    }

    fun testQuickLongRegex() {
        testQuickDoc("rule foo { ... }")
    }

    fun testQuickConstant() {
        testQuickDoc("constant \\zero = 0")
    }

    fun testExternalTypeFromCORE() {
        testQuickDoc("class IntStr")
        testGeneratedDoc("<p><pre><code>class IntStr is Int is Str { }</code></pre></p><p>The dual value types (often referred to as allomorphs) allow for the representation of a value as both a string and a numeric type. Typically they will be created for you when the context is \"stringy\" but they can be determined to be numbers, such as in some quoting constructs:</p><p><pre><code>my \$f = &lt;42&gt;; say \$f.^name; # OUTPUT: «IntStr␤»</code></pre></p><p>As a subclass of both Int and Str, an IntStr will be accepted where either is expected. However, IntStr does not share object identity with Int- or Str-only variants:</p><p><pre><code>my \$int-str = &lt;42&gt;;<br>my Int \$int = \$int-str; # OK!<br>my Str \$str = \$int-str; # OK!<br>say 42 ∈ &lt;42  55  1&gt;;   # False; ∈ operator cares about object identity</code></pre></p>")
        testURL("https://docs.perl6.org/type/IntStr")
    }

    fun testMethodExternalFromCORE() {
        testQuickDoc("method Capture(Mu *%_ --&gt; Mu)")
        testGeneratedDoc("<p>Defined as:</p><p><pre><code>method Capture()</code></pre></p><p>Throws X::Cannot::Capture.</p>")
        testURL("https://docs.perl6.org/routine/Capture")
    }

    fun testMethodExternalFromCORERole() {
        testQuickDoc("method roots(Cool \$n, Mu *%_ --&gt; Mu)")
        testGeneratedDoc("<p><pre><code>multi method roots(Numeric:D: Int:D \$n --&gt; Positional)</code></pre></p><p>Returns a list of the \$n complex roots, which evaluate to the original number when raised to the \$nth power.</p>")
        testURL("https://docs.perl6.org/routine/roots")
    }

    fun testMethodExternalFromCOREClass() {
        myFixture.configureByFile(getTestName(true) + ".p6")
        val element = myFixture.findElementByText(".end", RakuMethodCall::class.java)
        val provider = DocumentationManager.getProviderFromElement(element)
        val resolved = element.reference as PsiPolyVariantReference
        val decls = resolved.multiResolve(false)
        assertTrue(decls.isNotEmpty())
        assertEquals("method end(Mu *%_ --&gt; Int)", provider.getQuickNavigateInfo(decls[0].element, null))
        assertEquals("<p><pre><code>multi method end(Any:U: --&gt; 0)<br>multi method end(Any:D:)</code></pre></p><p>Interprets the invocant as a list, and returns the last index of that list.</p><p><pre><code>say 6.end;                      # OUTPUT: «0␤»<br>say &lt;a b c&gt;.end;                # OUTPUT: «2␤»</code></pre></p>",  provider.generateDoc(decls[0].element, element))
        assertContainsElements(provider.getUrlFor(decls[0].element, element)!!, "https://docs.perl6.org/routine/end")
    }

    fun testOperatorDocs() {
        testQuickDoc("our &amp;infix:&lt;(+)&gt;")
        testGeneratedDoc("<p><pre><code>multi sub infix:&lt;(+)&gt;(**@p)<br>multi sub infix:&lt;⊎&gt;(**@p)</code></pre></p><p>Baggy addition operator.</p><p>Returns the Baggy addition of its arguments. This creates a new Bag from each element of the arguments with the weights of the element added together to get the new weight, if none of the arguments are a Mix or MixHash.</p><p><pre><code>say &lt;a a b c a d&gt; (+) &lt;a a b c c&gt;; # OUTPUT: «Bag(a(5), b(2), c(3), d)␤»<br></code></pre></p><p>If any of the arguments is a Mixy, the result is a new Mix.</p><p><pre><code>say &lt;a b c&gt; (+) (a =&gt; 2.5, b =&gt; 3.14).Mix; # OUTPUT: «Mix(a(3.5), b(4.14), c)␤»<br></code></pre></p><p>⊎ is equivalent to (+), at codepoint U+228E (MULTISET UNION).</p>")
    }
}
