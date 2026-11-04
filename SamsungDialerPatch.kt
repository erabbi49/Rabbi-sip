package app.rabbii.samsungdialer

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.InstructionLocation.MatchAfterImmediately
import app.morphe.patcher.extensions.InstructionExtensions.removeInstruction
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.methodCall
import app.morphe.patcher.opcode
import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

private val samsungDialerCompatibility = Compatibility(
    name = "Samsung Phone",
    packageName = "com.samsung.android.dialer",
    apkFileType = ApkFileType.APK,
    appIconColor = 0x1428A0,
    targets = listOf(AppTarget(version = "15.9.72"))
)

/**
 * Matches the Samsung Phone 15.9.72 startup lookup reported at:
 * Pf.c.<clinit>() -> SemSystemProperties.get("ro.product_ship", "none").
 */
private object SamsungDialerShipBuildFingerprint : Fingerprint(
    definingClass = "LPf/c;",
    name = "<clinit>",
    returnType = "V",
    parameters = emptyList(),
    filters = listOf(
        string("ro.product_ship"),
        string("none"),
        methodCall(
            definingClass = "Landroid/os/SemSystemProperties;",
            name = "get",
            parameters = listOf("Ljava/lang/String;", "Ljava/lang/String;"),
            returnType = "Ljava/lang/String;"
        ),
        opcode(Opcode.MOVE_RESULT_OBJECT, MatchAfterImmediately())
    )
)

/**
 * Stage 1 only: bypass the Samsung-only ro.product_ship lookup that crashes
 * startup on non-One UI firmware. It does not emulate other Samsung APIs.
 */
@Suppress("unused")
val samsungDialerNonOneUiStartupPatch = bytecodePatch(
    name = "Samsung Phone: Non-One UI startup compatibility",
    description = "Bypasses the missing SemSystemProperties ro.product_ship lookup during startup.",
    default = true
) {
    compatibleWith(samsungDialerCompatibility)

    execute {
        val method = SamsungDialerShipBuildFingerprint.method
        val matches = SamsungDialerShipBuildFingerprint.instructionMatches
        val invokeMatch = matches[2]
        val moveResultMatch = matches[3]
        val resultRegister =
            moveResultMatch.getInstruction<OneRegisterInstruction>().registerA

        method.replaceInstruction(
            invokeMatch.index,
            """const-string v$resultRegister, "true""""
        )
        method.removeInstruction(moveResultMatch.index)
    }
}
