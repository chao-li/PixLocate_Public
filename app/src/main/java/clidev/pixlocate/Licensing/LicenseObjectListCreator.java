package clidev.pixlocate.Licensing;

import android.app.Application;

import java.util.ArrayList;
import java.util.List;

public final class LicenseObjectListCreator {

    private static final String APACHE_LICENSE = "Apache License, Version 2.0";

    public static List<LicenseObject> compileLicenseObjectList() {
        List<LicenseObject> licenseObjectList = new ArrayList<>();

        // glide
        LicenseObject glide = new LicenseObject("Glide", APACHE_LICENSE);

        // timber
        LicenseObject timber = new LicenseObject("Timber", APACHE_LICENSE);

        // camerakit
        LicenseObject cameraKit = new LicenseObject("Camera Kit", APACHE_LICENSE);

        // styleable toast
        LicenseObject styleabletoast = new LicenseObject("Styleable Toast", APACHE_LICENSE);

        // leak canary
        LicenseObject leakCanary = new LicenseObject("Leak Canary", APACHE_LICENSE);


        licenseObjectList.add(glide);
        licenseObjectList.add(timber);
        licenseObjectList.add(cameraKit);
        licenseObjectList.add(styleabletoast);
        licenseObjectList.add(leakCanary);


        return licenseObjectList;
    }
}
