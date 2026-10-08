/*
 * Copyright 2026 Morphe.
 * https://github.com/MorpheApp/morphe-patches
 *
 * See the included NOTICE file for GPLv3 Section 7 terms that apply to this code.
 */

package app.morphe.patches.youtube.video.framerate

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.shared.misc.settings.preference.SwitchPreference
import app.morphe.patches.youtube.misc.extension.sharedExtensionPatch
import app.morphe.patches.youtube.misc.settings.PreferenceScreen
import app.morphe.patches.youtube.misc.settings.settingsPatch
import app.morphe.patches.youtube.shared.Constants.COMPATIBILITY_YOUTUBE

private const val EXTENSION_CLASS =
    "Lapp/morphe/extension/youtube/patches/DisableVideoFrameRateMatchingPatch;"

@Suppress("unused")
val disableVideoFrameRateMatchingPatch = bytecodePatch(
    name = "Disable video frame rate matching",
    description = "Adds an option to stop the video player from requesting a display " +
            "refresh rate that matches the video frame rate."
) {
    compatibleWith(COMPATIBILITY_YOUTUBE)

    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
    )

    execute {
        PreferenceScreen.MISC.addPreferences(
            SwitchPreference("morphe_disable_video_frame_rate_matching", summary = true)
        )

        // Static method: p0 is the surface, p1 is the frame rate.
        SurfaceSetFrameRateFingerprint.matchAll().forEach {
            it.method.addInstructions(
                0,
                """
                    invoke-static { p1 }, $EXTENSION_CLASS->getVideoFrameRate(F)F
                    move-result p1
                """
            )
        }
    }
}
