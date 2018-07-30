package clidev.pixlocate.FirebaseDataObjects;

public class FirebaseUserDetail {

    private String userUniqueId;
    private String username;
    private String userRank;
    private String userTitle;
    private String userEmail;

    public FirebaseUserDetail() {};

    public FirebaseUserDetail(String userUniqueId, String username, String userRank, String userTitle, String userEmail) {
        this.userUniqueId = userUniqueId;
        this.username = username;
        this.userRank = userRank;
        this.userTitle = userTitle;
        this.userEmail = userEmail;
    }

    // getter and setters
    public String getUserUniqueId() {
        return userUniqueId;
    }

    public void setUserUniqueId(String userUniqueId) {
        this.userUniqueId = userUniqueId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserRank() {
        return userRank;
    }

    public void setUserRank(String userRank) {
        this.userRank = userRank;
    }

    public String getUserTitle() {
        return userTitle;
    }

    public void setUserTitle(String userTitle) {
        this.userTitle = userTitle;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}
