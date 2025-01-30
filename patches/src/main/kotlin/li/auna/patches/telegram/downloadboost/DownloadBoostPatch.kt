package li.auna.patches.telegram.downloadboost

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod

@Suppress("unused")
val downloadBoostPatch = bytecodePatch(
    name = "Download Speed Boost",
    description = "Boosts download speed",
) {
    compatibleWith(
        "org.telegram.messenger",
        "org.telegram.messenger.web",
        "uz.unnarsx.cherrygram"
    )

execute {
    val classDef = updateParamsFingerprint.classDef
    val originalMethod = updateParamsFingerprint.method
    
    val accessFlags = originalMethod.accessFlags
    
    val classPackage = classDef.type.substring(1).replace("/", ".")
    
    classDef.methods.removeIf { it.name == originalMethod.name }
    
    classDef.methods.add(
        ImmutableMethod(
            classDef.type,
            originalMethod.name,
            originalMethod.parameters,
            originalMethod.returnType,
            accessFlags,
            null,
            null,
            MutableMethodImplementation(originalMethod.implementation.instructions.size)
        ).toMutable().apply {
            addInstructions(
                """
                    iget v0, p0, $classPackage.FileLoadOperation->preloadPrefixSize:I
                    if-gtz v0, :cond_e
                    
                    iget v0, p0, $classPackage.FileLoadOperation->currentAccount:I
                    invoke-static {v0}, $classPackage.MessagesController->getInstance(I)L$classPackage.MessagesController;
                    move-result-object v0
                    
                    iget-boolean v0, v0, $classPackage.MessagesController->getfileExperimentalParams:Z
                    if-eqz v0, :cond_1d
                    
                    :cond_e
                    iget-boolean v0, p0, $classPackage.FileLoadOperation->forceSmallChunk:Z
                    if-nez v0, :cond_1d
                    
                    const/high16 v0, 0x80000
                    iput v0, p0, $classPackage.FileLoadOperation->downloadChunkSizeBig:I
                    
                    const/16 v0, 0x8
                    iput v0, p0, $classPackage.FileLoadOperation->maxDownloadRequests:I
                    iput v0, p0, $classPackage.FileLoadOperation->maxDownloadRequestsBig:I
                    goto :goto_26
                    
                    :cond_1d
                    const/high16 v0, 0x80000
                    iput v0, p0, $classPackage.FileLoadOperation->downloadChunkSizeBig:I
                    
                    const/16 v0, 0x8
                    iput v0, p0, $classPackage.FileLoadOperation->maxDownloadRequests:I
                    iput v0, p0, $classPackage.FileLoadOperation->maxDownloadRequestsBig:I
                    
                    :goto_26
                    const-wide/32 v0, 0x7d000000
                    iget v2, p0, $classPackage.FileLoadOperation->downloadChunkSizeBig:I
                    int-to-long v2, v2
                    div-long/2addr v0, v2
                    long-to-int v1, v0
                    iput v1, p0, $classPackage.FileLoadOperation->maxCdnParts:I
                    
                    return-void
                """
            )
        }
    )
}
