package li.auna.patches.telegram.bypassintegrity

import app.revanced.patcher.Fingerprint
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction

@Suppress("unused")
val bypassIntegrityPatch = bytecodePatch(
    name = "Bypass Integrity",
    description = "Bypass integrity check to allow login",
) {
    compatibleWith(
        "org.telegram.messenger",
        "org.telegram.messenger.web",
        "uz.unnarsx.cherrygram"
    )

    // Enhanced fingerprint for better matching
    val enhancedIntegrityFingerprint = fingerprint {
        accessFlags(AccessFlags.PRIVATE or AccessFlags.SYNTHETIC)
        returnType("V")
        strings("basicIntegrity", "ctsProfileMatch")
        // Add additional matching criteria for CherryGram
        opcodes(
            Opcode.CONST_STRING,
            Opcode.INVOKE_VIRTUAL
        )
    }

    val enhancedSignatureFingerprint = fingerprint {
        className("org/telegram/messenger/AndroidUtilities")
        methodName("getCertificateSHA256Fingerprint")
        returnType("Ljava/lang/String;")
        accessFlags(AccessFlags.PUBLIC or AccessFlags.STATIC)
    }

    execute {
        fun patchIntegrityCheck(fingerprint: MethodFingerprint) {
            fingerprint.result?.let { result ->
                result.method.apply {
                    val stringMatches = result.scanResult.stringsScanResult!!.matches
                    
                    // Find all integrity check conditions
                    stringMatches.forEach { match ->
                        var index = match.index
                        // Look for the conditional check instruction
                        while (index < implementation!!.instructions.size) {
                            val instruction = getInstruction(index)
                            if (instruction.opcode == Opcode.IF_EQZ || instruction.opcode == Opcode.IF_NEZ) {
                                val register = (instruction as OneRegisterInstruction).registerA
                                // Force the check to pass
                                replaceInstruction(index, "const/4 v$register, 0x1")
                                break
                            }
                            index++
                        }
                    }
                }
            } ?: throw PatchException("Failed to find integrity check method")
        }

        // Patch signature check
        enhancedSignatureFingerprint.result?.let { result ->
            result.method.apply {
                addInstructions(
                    0,
                    """
                    const-string v0, "49C1522548EBACD46CE322B6FD47F6092BB745D0F88082145CAF35E14DCC38E1"
                    return-object v0
                    """
                )
            }
        } ?: throw PatchException("Failed to find signature method")

        // Attempt to patch integrity check
        patchIntegrityCheck(enhancedIntegrityFingerprint)

        PatchResultSuccess()
    }
}

class PatchException(message: String) : Exception(message)
