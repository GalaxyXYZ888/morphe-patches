/*
 * Copyright 2026 Morphe.
 * https://github.com/MorpheApp/morphe-patches
 *
 * See the included NOTICE file for GPLv3 Section 7 terms that apply to this code.
 */

package app.morphe.extension.youtube.patches;

import app.morphe.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class DisableVideoFrameRateMatchingPatch {

    /**
     * Injection point.
     * <p>
     * A frame rate of zero tells the system the video surface has no preferred frame rate.
     */
    public static float getVideoFrameRate(float original) {
        if (Settings.DISABLE_VIDEO_FRAME_RATE_MATCHING.get()) {
            return 0f;
        }
        return original;
    }
}
