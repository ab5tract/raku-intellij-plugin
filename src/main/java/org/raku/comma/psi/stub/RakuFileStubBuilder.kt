package org.raku.comma.psi.stub

import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.stubs.DefaultStubBuilder
import com.intellij.psi.stubs.StubElement
import com.intellij.testFramework.LightVirtualFile
import org.raku.comma.filetypes.RakuModuleFileType
import org.raku.comma.psi.RakuFile
import org.raku.comma.psi.stub.impl.RakuFileStubImpl
import org.raku.comma.vfs.RakuFileSystem
import java.nio.file.FileSystems

class RakuFileStubBuilder : DefaultStubBuilder() {
    override fun createStubForFile(file: PsiFile): StubElement<*> {
        return if (file is RakuFile && file.isReal) {
            RakuFileStubImpl(file, generateCompilationUnitName(file))
        } else {
            super.createStubForFile(file)
        }
    }

    companion object {
        private fun generateCompilationUnitName(file: PsiFile): String? {
            var vf = file.viewProvider.virtualFile

            if (vf is LightVirtualFile) {
                vf = vf.originalFile ?: return null
                if (vf.fileSystem is RakuFileSystem) {
                    return vf.nameWithoutExtension
                }
            }

            val filePath = vf.path
            if (FileTypeManager.getInstance().getFileTypeByFile(vf) is RakuModuleFileType) {
                val parentModule = ModuleUtilCore.findModuleForFile(vf, file.project) ?: return null

                val entries = ModuleRootManager.getInstance(parentModule).sourceRoots
                for (sourceRoot in entries) {
                    if (filePath.startsWith(sourceRoot.path + FileSystems.getDefault().separator)) {
                        val relPath = sourceRoot.toNioPath().relativize(vf.toNioPath()).toString()
                        val parts = relPath.split(Regex("[/\\\\]")).toTypedArray()
                        val lastDot = parts[parts.size - 1].lastIndexOf('.')
                        if (lastDot > 0)
                            parts[parts.size - 1] = parts[parts.size - 1].substring(0, lastDot)
                        return parts.joinToString("::")
                    }
                }
            }

            // Not a module, outside of project, or otherwise odd.
            return null
        }
    }
}
