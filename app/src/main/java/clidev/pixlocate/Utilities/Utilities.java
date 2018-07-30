package clidev.pixlocate.Utilities;

import android.widget.Toast;

import com.muddzdev.styleabletoastlibrary.StyleableToast;

public final class Utilities {

    public static void cancelToast(Toast toast) {
        if (toast != null) {
            toast.cancel();
        }

    }

    public static void cancelStyledToast(StyleableToast toast) {
        if (toast != null) {
            toast.cancel();
        }
    }

}
