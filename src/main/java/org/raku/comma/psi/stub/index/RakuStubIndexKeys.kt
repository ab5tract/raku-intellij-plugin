package org.raku.comma.psi.stub.index

import com.intellij.psi.stubs.StubIndexKey
import org.raku.comma.psi.RakuConstant
import org.raku.comma.psi.RakuFile
import org.raku.comma.psi.RakuRegexDecl
import org.raku.comma.psi.RakuRoutineDecl
import org.raku.comma.psi.RakuVariableDecl

object RakuStubIndexKeys {
    @JvmField
    val PROJECT_MODULES: StubIndexKey<String, RakuFile> = StubIndexKey.createIndexKey("raku.projectModules")
    @JvmField
    val GLOBAL_TYPES: StubIndexKey<String, RakuIndexableType> = StubIndexKey.createIndexKey("raku.globalTypes")
    @JvmField
    val LEXICAL_TYPES: StubIndexKey<String, RakuIndexableType> = StubIndexKey.createIndexKey("raku.lexicalTypes")
    @JvmField
    val ALL_CONSTANTS: StubIndexKey<String, RakuConstant> = StubIndexKey.createIndexKey("raku.allConstants")
    @JvmField
    val ALL_ATTRIBUTES: StubIndexKey<String, RakuVariableDecl> = StubIndexKey.createIndexKey("raku.allAttributes")
    @JvmField
    val ALL_ROUTINES: StubIndexKey<String, RakuRoutineDecl> = StubIndexKey.createIndexKey("raku.allRoutines")
    @JvmField
    val ALL_REGEXES: StubIndexKey<String, RakuRegexDecl> = StubIndexKey.createIndexKey("raku.allRegexes")
    @JvmField
    val DYNAMIC_VARIABLES: StubIndexKey<String, RakuVariableDecl> = StubIndexKey.createIndexKey("raku.dynamicVariables")
}
