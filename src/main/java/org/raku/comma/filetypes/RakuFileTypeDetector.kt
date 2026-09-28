package org.raku.comma.filetypes

import com.intellij.openapi.extensions.InternalIgnoreDependencyViolation
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.FileTypeRegistry
import com.intellij.openapi.util.io.ByteSequence
import com.intellij.openapi.vfs.VirtualFile

class RakuFileTypeDetector : FileTypeRegistry.FileTypeDetector {
    override fun detect(file: VirtualFile, firstBytes: ByteSequence, firstCharsIfText: CharSequence?): FileType? {
        if (file.extension != null) return null
        val parent = file.parent ?: return null
        // Exact name, not endsWith: that also matched `sbin`, and any
        // directory whose name happens to end in those three letters.
        if (parent.name != "bin") return null
        // And only inside a distribution. A bin/ directory full of
        // extensionless executables is the norm in Perl, Python venvs, node
        // and Go; typing those as Raku is what made the plugin wake up in
        // projects with no Raku in them at all. Real shebang scripts are
        // handled by the HashBang detectors regardless of where they live.
        if (parent.parent?.findChild("META6.json") == null) return null
        return RakuScriptFileType.INSTANCE
    }
}
