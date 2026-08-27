package app.morphe.patches.youtube.layout.playlistautoplay

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.opcode
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

/**
 * R8 keeps enum constant names for Enum#name()/#toString(), so this enum's
 * static constructor is findable by its constant names as string literals.
 * A lower-level token enum shares the same constant names in its own
 * `<clinit>`, so the string filter alone is ambiguous; requiring an extra
 * constructor parameter (this enum wraps a token value, the other doesn't)
 * selects it uniquely.
 */
internal object NavigationIntentEnumFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.STATIC, AccessFlags.CONSTRUCTOR),
    filters = listOf(
        string("NEXT"),
        string("PREVIOUS"),
        string("AUTOPLAY"),
        string("AUTONAV"),
        string("JUMP"),
        opcode(Opcode.RETURN_VOID),
    ),
    custom = { _, classDef ->
        classDef.methods.any { it.name == "<init>" && it.parameterTypes.size > 2 }
    },
)
