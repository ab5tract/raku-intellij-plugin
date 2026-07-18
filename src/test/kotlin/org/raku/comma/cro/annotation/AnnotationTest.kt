package org.raku.comma.cro.annotation

import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.cro.template.CroTemplateFileType

class AnnotationTest : CommaFixtureTestCase() {
    fun testDuplicateCroTemplateSeparator() {
        myFixture.configureByText(CroTemplateFileType.INSTANCE,
            "<@things>\n" +
            "  <\$_>\n" +
            "  <:separator><hr></:>\n" +
            "  <error descr=\"Duplicate separator\"><:separator><hr></:></error>\n" +
            "</@>")
        myFixture.checkHighlighting()
    }

    fun testMisplacedCroTemplateSeparator() {
        myFixture.configureByText(CroTemplateFileType.INSTANCE,
            "<?.foo>\n" +
            "  <error descr=\"Separator may only occur directly in an iteration\"><:separator><hr></:></error>\n" +
            "</?>")
        myFixture.checkHighlighting()
    }
}
