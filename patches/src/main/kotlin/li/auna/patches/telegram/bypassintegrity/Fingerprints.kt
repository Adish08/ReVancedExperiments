package li.auna.patches.telegram.bypassintegrity

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val bypassIntegrityFingerprint = fingerprint {
    accessFlags = AccessFlags.PRIVATE or AccessFlags.SYNTHETIC
    returnType = "V"
    strings = listOf("basicIntegrity", "ctsProfileMatch", "MEQ")
    customFingerprint = { methodDef, _ ->
        methodDef.parameters.size == 1 && 
        methodDef.parameterTypes.first() == "Lcom/google/android/gms/tasks/Task;"
    }
}

internal val spoofSignatureFingerprint = fingerprint {
    custom { methodDef, classDef ->
        methodDef.name == "getCertificateSHA256Fingerprint" && 
        (classDef.type.endsWith("Lorg/telegram/messenger/AndroidUtilities;") || 
         classDef.type.endsWith("Luz/unnarsx/cherrygram/AndroidUtilities;")) // Added Cherrygram path
    }
}
