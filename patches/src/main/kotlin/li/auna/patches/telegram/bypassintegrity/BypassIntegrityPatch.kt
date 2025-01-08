package li.auna.patches.telegram.bypassintegrity

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patcher.fingerprint.method.annotation.FingerprintAnnotation
import app.revanced.patcher.fingerprint.method.annotation.StringsAnnotation
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Bypass Integrity",
    description = "Bypasses integrity check to allow login",
    compatiblePackages = [
        CompatiblePackage("org.telegram.messenger"),
        CompatiblePackage("org.telegram.messenger.web"),
        CompatiblePackage("uz.unnarsx.cherrygram")
    ]
)
class BypassIntegrityPatch : BytecodePatch(
    setOf(IntegrityCheckFingerprint, SignatureFingerprint)
) {
    override fun execute(context: BytecodeContext): PatchResult {
        val integrityResult = IntegrityCheckFingerprint.result ?: throw PatchException(
            "Could not find integrity check method"
        )
        
        integrityResult.mutableMethod.apply {
            val stringMatches = IntegrityCheckFingerprint.strings
            stringMatches.forEachIndexed { _, string ->
                var currentIndex = implementation!!.instructions.indexOfFirst { 
                    it is OneRegisterInstruction && it.opcode == Opcode.CONST_STRING 
                        && (it as OneRegisterInstruction).registerA == string
                }
                
                if (currentIndex != -1) {
                    currentIndex += 2 // Move to the check instruction
                    val checkInstruction = getInstruction<OneRegisterInstruction>(currentIndex)
                    replaceInstruction(
                        currentIndex,
                        "const/4 v${checkInstruction.registerA}, 0x1"
                    )
                }
            }
        }

        // Patch signature check
        val signatureResult = SignatureFingerprint.result ?: throw PatchException(
            "Could not find signature method"
        )
        
        signatureResult.mutableMethod.apply {
            addInstructions(
                0,
                """
                const-string v0, "49C1522548EBACD46CE322B6FD47F6092BB745D0F88082145CAF35E14DCC38E1"
                return-object v0
                """
            )
        }

        return PatchResult.Success()
    }
}

@FingerprintAnnotation(
    strings = [
        StringsAnnotation(["basicIntegrity", "ctsProfileMatch"])
    ],
    accessFlags = AccessFlags.PRIVATE or AccessFlags.SYNTHETIC
)
object IntegrityCheckFingerprint : MethodFingerprint(
    returnType = "V",
    parameters = listOf(),
    customFingerprint = { methodDef, _ ->
        methodDef.implementation?.instructions?.any { 
            it.opcode == Opcode.CONST_STRING 
        } == true
    }
)

@FingerprintAnnotation
object SignatureFingerprint : MethodFingerprint(
    returnType = "Ljava/lang/String;",
    strings = listOf(),
    customFingerprint = { methodDef, classDef ->
        methodDef.name == "getCertificateSHA256Fingerprint" 
            && classDef.type.endsWith("Lorg/telegram/messenger/AndroidUtilities;")
    }
)

class PatchException(message: String) : Exception(message)
