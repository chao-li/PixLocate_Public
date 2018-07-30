package clidev.pixlocate.FirebaseUtilities.Download;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import timber.log.Timber;

public class FirebaseDownloadLocationDateUtilities {

    private int searchCount;

    public FirebaseDownloadLocationDateUtilities() {
    }

    private void queryThisLocationForDataCount(final LatLng latLng, final Double radius) {
        // First check the current year
        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy");

        String yearString = formatter.format(currentTime).trim();
        int currentYear = Integer.parseInt(yearString);

        if (currentYear < 2018) {
            Timber.d("Invalid year");
            currentYear = 2018;
        }





    }



    private static Double[] radiusArray = new Double[]{
            0.05,
            0.1,
            0.2,
            0.5,
            1d,
            2d,
            3d,
            4d,
            5d,
            10d,
            15d,
            20d,
            50d,
            75d,
            100d,
            200d,
            300d,
            500d,
            1000d,
            1500d,
            2000d,
            5000d};

    private static int[] yearArray = new int[]{

    };

}
