package li.auna.patches.telegram.bypassintegrity

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val bypassIntegrityFingerprint = fingerprint {
    accessFlags(AccessFlags.PRIVATE.value or AccessFlags.SYNTHETIC.value)
    returns("V")
    strings("basicIntegrity", "ctsProfileMatch", "MEQ")
    custom { methodDef, _ ->
        methodDef.parameterTypes?.let { params ->
            params.size == 1 &&
            params.first() == "Lcom/google/android/gms/tasks/Task;"
        } ?: false
    }
}

internal val spoofSignatureFingerprint = fingerprint {
    custom { methodDef, classDef ->
        methodDef.name == "getCertificateSHA256Fingerprint" && 
        (classDef.type.endsWith("Lorg/telegram/messenger/AndroidUtilities;") || 
         classDef.type.endsWith("Luz/unnarsx/cherrygram/AndroidUtilities;"))
    }
}
