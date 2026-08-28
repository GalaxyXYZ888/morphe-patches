/*
 * Copyright 2026 Morphe.
 * https://github.com/MorpheApp/morphe-patches/pull/2628
 *
 * See the included NOTICE file for GPLv3 Section 7 terms that apply to this code.
 */

package app.morphe.patches.youtube.layout.playlistautoplay

import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.all.misc.resources.resourceMappingPatch
import app.morphe.patches.shared.misc.settings.preference.SwitchPreference
import app.morphe.patches.youtube.misc.extension.sharedExtensionPatch
import app.morphe.patches.youtube.misc.settings.PreferenceScreen
import app.morphe.patches.youtube.misc.settings.settingsPatch
import app.morphe.patches.youtube.shared.Constants.COMPATIBILITY_YOUTUBE
import app.morphe.util.findFreeRegister
import app.morphe.util.getMutableMethod
import com.android.tools.smali.dexlib2.iface.Method

private const val EXTENSION_CLASS =
    "Lapp/morphe/extension/youtube/patches/DisablePlaylistAutoplayPatch;"

/**
 * Video-ended navigation shares one dispatch object carrying an enum: an
 * AUTOPLAY value means "continue the active playlist/queue" and fires
 * unconditionally, while AUTONAV ("play a suggested video") is the one
 * already gated by the existing Settings toggle. Every class exposing a
 * matching navigate/has-next method pair is patched, since which one handles
 * the main watch flow isn't stable across builds.
 */
@Suppress("unused")
val disablePlaylistAutoplayPatch = bytecodePatch(
    name = "Disable playlist autoplay",
    description = "Adds an option to stop a playlist from automatically advancing to the next video.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        resourceMappingPatch,
    )

    compatibleWith(COMPATIBILITY_YOUTUBE)

    execute {
        PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("morphe_disable_playlist_autoplay", summary = true)
        )

        val enumType = NavigationIntentEnumFingerprint.originalClassDef.type

        // Resolve the dispatch object's own class from its 3-arg constructor.
        var wrapperType: String? = null
        classDefForEach { classDef ->
            if (wrapperType != null) return@classDefForEach
            for (method in classDef.methods) {
                val params = method.parameterTypes
                if (method.name == "<init>" && params.size == 3 && params.firstOrNull() == enumType) {
                    wrapperType = classDef.type
                    return@classDefForEach
                }
            }
        }
        val resolvedWrapperType = wrapperType ?: return@execute

        // Patch every class with a matching navigate/has-next method pair
        // rather than the one exact concrete class - harmless if unused.
        val navigateMethods = mutableListOf<Method>()
        classDefForEach { classDef ->
            var vMethod: Method? = null
            var iMethod: Method? = null
            for (method in classDef.methods) {
                if (method.implementation == null) continue
                val params = method.parameterTypes
                if (params.size == 1 && params.firstOrNull() == resolvedWrapperType) {
                    if (method.returnType == "V") vMethod = method
                    if (method.returnType == "I") iMethod = method
                }
            }
            if (vMethod != null && iMethod != null) {
                navigateMethods += vMethod
            }
        }

        navigateMethods.forEach { method ->
            val mutableMethod = method.getMutableMethod()
            val freeRegister = mutableMethod.findFreeRegister(0)

            mutableMethod.addInstructionsWithLabels(
                0,
                """
                    invoke-static { p1 }, $EXTENSION_CLASS->shouldSkipPlaylistAutoplay(Ljava/lang/Object;)Z
                    move-result v$freeRegister
                    if-eqz v$freeRegister, :continue
                    return-void
                    :continue
                    nop
                """
            )
        }
    }
}
