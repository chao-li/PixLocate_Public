package clidev.pixlocate.FirebaseDataObjects;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

public class FirebaseImageWithLocation implements Parcelable{

    private String userId;
    private String username;
    private Boolean isPrivatePhoto;
    private String imageUrl;
    private String smallImageUrl;
    private String imageName;
    private String smallImageName;
    private Double latitude;
    private Double longitude;
    private String imageKey;
    private long timeStamp;


    public static final Creator<FirebaseImageWithLocation> CREATOR = new Creator<FirebaseImageWithLocation>() {
        @Override
        public FirebaseImageWithLocation createFromParcel(Parcel in) {
            return new FirebaseImageWithLocation(in);
        }

        @Override
        public FirebaseImageWithLocation[] newArray(int size) {
            return new FirebaseImageWithLocation[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(userId);
        parcel.writeString(username);
        parcel.writeByte((byte) (isPrivatePhoto ? 1 : 0));
        parcel.writeString(imageUrl);
        parcel.writeString(smallImageUrl);
        parcel.writeString(imageName);
        parcel.writeString(smallImageName);
        parcel.writeDouble(latitude);
        parcel.writeDouble(longitude);
        parcel.writeString(imageKey);
        parcel.writeLong(timeStamp);
    }

    public FirebaseImageWithLocation(Parcel in) {
        this.userId = in.readString();
        this.username = in.readString();
        this.isPrivatePhoto = in.readByte() != 0;
        this.imageUrl = in.readString();
        this.smallImageUrl = in.readString();
        this.imageName = in.readString();
        this.smallImageName = in.readString();
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        this.imageKey = in.readString();
        this.timeStamp = in.readLong();

    }


    public FirebaseImageWithLocation () {}


    public FirebaseImageWithLocation(String userId,
                         String username,
                         Boolean isPrivatePhoto,
                         String imageUrl,
                         String smallImageUrl,
                         String imageName,
                         String smallImageName,
                         Double latitude,
                         Double longitude,
                         String imageKey,
                         long timeStamp) {

        this.userId = userId;
        this.username = username;
        this.isPrivatePhoto = isPrivatePhoto;
        this.imageUrl = imageUrl;
        this.smallImageUrl = smallImageUrl;
        this.imageName = imageName;
        this.smallImageName = smallImageName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imageKey = imageKey;
        this.timeStamp = timeStamp;

    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Boolean getPrivatePhoto() {
        return isPrivatePhoto;
    }

    public void setPrivatePhoto(Boolean privatePhoto) {
        isPrivatePhoto = privatePhoto;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSmallImageUrl() {
        return smallImageUrl;
    }

    public void setSmallImageUrl(String smallImageUrl) {
        this.smallImageUrl = smallImageUrl;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getSmallImageName() {
        return smallImageName;
    }

    public void setSmallImageName(String smallImageName) {
        this.smallImageName = smallImageName;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getImageKey() {
        return imageKey;
    }

    public void setImageKey(String imageKey) {
        this.imageKey = imageKey;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
