package li.auna.patches.telegram.disableautoupdate

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val checkAppUpdateFingerprint = fingerprint {
    returns("V")
    custom { methodDef, classDef ->
        methodDef.name == "checkAppUpdate" && 
        (classDef.type.endsWith("Lorg/telegram/ui/LaunchActivity;") ||
         classDef.type.endsWith("Luz/unnarsx/cherrygram/ui/LaunchActivity;"))
    }
    opcodes(
        Opcode.IF_NEZ,
        Opcode.SGET_BOOLEAN,
        Opcode.IF_NEZ,
        Opcode.IF_NEZ,
    )
}

internal val setNewAppVersionAvailableFingerprint = fingerprint {
    returns("Z")
    custom { methodDef, classDef ->
        methodDef.name == "setNewAppVersionAvailable" && 
        (classDef.type.endsWith("Lorg/telegram/messenger/SharedConfig;") ||
         classDef.type.endsWith("Luz/unnarsx/cherrygram/messenger/SharedConfig;")) 
    }
}

internal val blockViewUpdateFingerprint = fingerprint {
    returns("V")
    custom { methodDef, classDef ->
        methodDef.name == "show" && 
        (classDef.type.endsWith("Lorg/telegram/ui/Components/BlockingUpdateView;") ||
         classDef.type.endsWith("Luz/unnarsx/cherrygram/ui/Components/BlockingUpdateView;"))
    }
}
