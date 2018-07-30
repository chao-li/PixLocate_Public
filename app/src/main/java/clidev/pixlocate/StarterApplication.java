package clidev.pixlocate;

import android.app.Application;
import android.support.multidex.MultiDex;


import com.squareup.leakcanary.LeakCanary;

import timber.log.Timber;

public class StarterApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();



        if (BuildConfig.DEBUG) {
            if (LeakCanary.isInAnalyzerProcess(this)) {
                // This process is dedicated to LeakCanary for heap analysis.
                // You should not init your app in this process.
                return;
            }
            LeakCanary.install(this);
        }



        // Planting timber

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }




    }


}
