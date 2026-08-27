package app.morphe.extension.youtube.patches;

import java.lang.reflect.Field;

import app.morphe.extension.shared.Logger;
import app.morphe.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class DisablePlaylistAutoplayPatch {

    /**
     * Injection point.
     */
    public static boolean shouldSkipPlaylistAutoplay(Object navigationIntent) {
        if (!Settings.DISABLE_PLAYLIST_AUTOPLAY.get()) {
            return false;
        }
        try {
            for (Field field : navigationIntent.getClass().getDeclaredFields()) {
                if (!Enum.class.isAssignableFrom(field.getType())) {
                    continue;
                }
                field.setAccessible(true);
                Object value = field.get(navigationIntent);
                if (value instanceof Enum<?> && "AUTOPLAY".equals(((Enum<?>) value).name())) {
                    return true;
                }
            }
        } catch (Exception ex) {
            Logger.printException(() -> "shouldSkipPlaylistAutoplay failure", ex);
        }
        return false;
    }
}
