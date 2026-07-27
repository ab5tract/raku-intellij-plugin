package org.raku.comma.psi.stub

import com.intellij.psi.stubs.*
import com.intellij.util.io.StringRef
import org.raku.comma.RakuLanguage
import org.raku.comma.extensions.RakuFrameworkCall
import org.raku.comma.psi.RakuSubCall
import org.raku.comma.psi.impl.RakuSubCallImpl
import org.raku.comma.psi.stub.impl.RakuSubCallStubImpl
import java.io.IOException
import java.util.*

class RakuSubCallStubElementType : IStubElementType<RakuSubCallStub?, RakuSubCall?>("SUBCALL", RakuLanguage.INSTANCE) {
    override fun createPsi(stub: RakuSubCallStub): RakuSubCall {
        return RakuSubCallImpl(stub, this)
    }

    override fun createStub(call: RakuSubCall, parentStub: StubElement<*>?): RakuSubCallStub {
        val extensions = RakuFrameworkCall.EP_NAME.extensions
        val calleeName = call.getCallName()
        val frameworkData: MutableMap<String?, String?> = HashMap<String?, String?>()
        for (ext in extensions) {
            if (ext.isApplicable(call)) {
                val name: String = ext.frameworkName
                for (entry in ext.getFrameworkData(call).entries) {
                    frameworkData.put(name + "." + entry.key, entry.value)
                }
            }
        }
        return RakuSubCallStubImpl(parentStub, calleeName, frameworkData)
    }

    override fun getExternalId(): String {
        return "raku.stub.subcall"
    }

    @Throws(IOException::class)
    override fun serialize(stub: RakuSubCallStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.getName())
        val frameworkData = stub.getAllFrameworkData()
        dataStream.writeInt(frameworkData.size)
        for (data in frameworkData.entries) {
            dataStream.writeName(data.key)
            dataStream.writeUTF(data.value)
        }
    }

    @Throws(IOException::class)
    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): RakuSubCallStub {
        val name = Objects.requireNonNull<StringRef?>(dataStream.readName()).getString()
        val frameworkData: MutableMap<String?, String?> = HashMap<String?, String?>()
        val elements = dataStream.readInt()
        for (i in 0..<elements) {
            val key = Objects.requireNonNull<StringRef?>(dataStream.readName()).getString()
            val value = dataStream.readUTF()
            frameworkData.put(key, value)
        }
        return RakuSubCallStubImpl(parentStub, name, frameworkData)
    }

    override fun indexStub(stub: RakuSubCallStub, sink: IndexSink) {
        val extensions = RakuFrameworkCall.EP_NAME.extensions
        val allFrameworkData = stub.getAllFrameworkData()
        for (ext in extensions) {
            val prefix = ext.frameworkName
            val frameworkData: MutableMap<String, String> = mutableMapOf()
            for (entry in allFrameworkData.entries) {
                if (entry.key!!.startsWith("$prefix.")) {
                    frameworkData.put(entry.key!!.substring(prefix.length + 1), entry.value!!)
                }
            }
            if (!frameworkData.isEmpty()) {
                ext.indexStub(stub, frameworkData, sink)
            }
        }
    }
}
