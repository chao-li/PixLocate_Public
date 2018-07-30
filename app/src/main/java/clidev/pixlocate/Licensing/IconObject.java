package clidev.pixlocate.Licensing;

public class IconObject {


    private int mImageId;
    private String mAuthor;
    private String mWebsite;

    public IconObject(int imageId, String author, String website) {
        mImageId = imageId;
        mAuthor = author;
        mWebsite = website;
    }

    public int getImageId() {
        return mImageId;
    }

    public void setImageId(int imageId) {
        mImageId = imageId;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public void setAuthor(String author) {
        mAuthor = author;
    }

    public String getWebsite() {
        return mWebsite;
    }

    public void setWebsite(String website) {
        mWebsite = website;
    }
}
