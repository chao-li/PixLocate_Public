package clidev.pixlocate.Licensing;

public class LicenseObject {
    private String mLibrary;
    private String mLicense;

    // constructor
    public LicenseObject(String library, String license) {
        mLibrary = library;
        mLicense = license;
    }


    // getter and setters
    public String getLibrary() {
        return mLibrary;
    }

    public void setLibrary(String library) {
        mLibrary = library;
    }

    public String getLicense() {
        return mLicense;
    }

    public void setLicense(String license) {
        mLicense = license;
    }
}
