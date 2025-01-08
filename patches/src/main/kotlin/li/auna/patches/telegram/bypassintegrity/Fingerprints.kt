package li.auna.patches.telegram.bypassintegrity

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patcher.fingerprint.method.annotation.FingerprintAnnotation
import app.revanced.patcher.fingerprint.method.annotation.StringsAnnotation
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

@FingerprintAnnotation(
    strings = [
        StringsAnnotation(["basicIntegrity", "ctsProfileMatch"])
    ],
    accessFlags = AccessFlags.PRIVATE or AccessFlags.SYNTHETIC
)
internal object IntegrityCheckFingerprint : MethodFingerprint(
    returnType = "V",
    parameters = listOf(),
    customFingerprint = { methodDef, _ ->
        methodDef.implementation?.instructions?.any { 
            it.opcode == Opcode.CONST_STRING 
        } == true
    }
)

@FingerprintAnnotation
internal object SignatureFingerprint : MethodFingerprint(
    returnType = "Ljava/lang/String;",
    strings = listOf(),
    customFingerprint = { methodDef, classDef ->
        methodDef.name == "getCertificateSHA256Fingerprint" 
            && classDef.type.endsWith("Lorg/telegram/messenger/AndroidUtilities;")
    }
)
