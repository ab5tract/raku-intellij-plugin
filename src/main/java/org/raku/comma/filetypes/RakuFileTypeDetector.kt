package org.raku.comma.filetypes

import com.intellij.openapi.extensions.InternalIgnoreDependencyViolation
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.FileTypeRegistry
import com.intellij.openapi.util.io.ByteSequence
import com.intellij.openapi.vfs.VirtualFile
import org.raku.comma.services.project.RakuMetaDataComponent

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
        //
        // Both meta file names, because a distribution is allowed to be old:
        // canOpenFileAsProject and RakuMetaDataComponent.checkOldMetaFile
        // both honour META.info, and recognising only META6.json here would
        // silently stop typing an old-style distribution's bin/ scripts.
        // These are `const val`s, so the names are inlined and nothing is
        // loaded at detection time.
        val distRoot = parent.parent ?: return null
        if (distRoot.findChild(RakuMetaDataComponent.META6_JSON_NAME) == null
            && distRoot.findChild(RakuMetaDataComponent.META_OBSOLETE_NAME) == null) return null
        return RakuScriptFileType.INSTANCE
    }

    // No getVersion() override. On the target platform it is @Deprecated and
    // @ApiStatus.ScheduledForRemoval, and FileTypeDetectionService never
    // calls it -- its only getVersion() call is FileAttribute's. What
    // actually invalidates the detection cache is getDetectorListString(),
    // built from the registered detectors' class names, compared at service
    // construction. So a pure logic change inside detect() invalidates
    // nothing on its own: previously-detected files keep their cached type
    // until the registered detector list changes or the plugin is reloaded.
    // Bear that in mind when changing the rules above -- a version bump is
    // not the lever it looks like.
}
