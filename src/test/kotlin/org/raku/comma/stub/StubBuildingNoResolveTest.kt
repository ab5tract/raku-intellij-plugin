package org.raku.comma.stub

import org.raku.comma.CommaFixtureTestCase
import org.raku.comma.filetypes.RakuModuleFileType

// Regression test for a reported full-IDE freeze: building the stub for a
// variable declaration used to run full type inference, which resolves type
// names and initializer expressions through the file's `use` statements and
// so queries the stub index from inside stub building. The platform forbids
// that circular dependency (indexes -> stub building -> resolve -> indexes);
// in production it can deadlock the IDE. Under the test harness the
// platform's Logger.error escalates to a test failure, which is what these
// tests rely on to detect the illegal index access.
class StubBuildingNoResolveTest : CommaFixtureTestCase() {

    fun testStubbedTypedAttributeDoesNotQueryIndexes() {
        myFixture.configureByText(
            RakuModuleFileType.INSTANCE,
            "use Foo::Bar;\nclass C {\n    has Custom::Type \$!trace;\n}\n"
        )
        myFixture.doHighlighting()
    }

    fun testStubbedAttributeWithOfTraitDoesNotQueryIndexes() {
        myFixture.configureByText(
            RakuModuleFileType.INSTANCE,
            "use Foo::Bar;\nclass C {\n    has \$!counts of Custom::Type;\n}\n"
        )
        myFixture.doHighlighting()
    }

    fun testStubbedAttributeWithInitializerDoesNotQueryIndexes() {
        myFixture.configureByText(
            RakuModuleFileType.INSTANCE,
            "use Foo::Bar;\nclass C {\n    has \$!state = Custom::Type.new;\n}\n"
        )
        myFixture.doHighlighting()
    }
}
