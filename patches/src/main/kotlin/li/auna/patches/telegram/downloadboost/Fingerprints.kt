package li.auna.patches.telegram.downloadboost

import app.revanced.patcher.fingerprint

internal val updateParamsFingerprint = fingerprint {
    returns("V")
    strings("preloadPrefixSize", "downloadChunkSizeBig")
    custom { methodDef, classDef ->
        methodDef.name == "updateParams" && 
        classDef.type.let {
            it.endsWith("FileLoadOperation;") && 
            (it.contains("org/telegram") || it.contains("cherrygram")) 
        } &&
        methodDef.parameters.isEmpty()
    }
}
