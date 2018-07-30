package clidev.pixlocate.Utilities;

public final class InfoUtilities {

    public static String galleryInfo() {
        String info = "- Tap image to select the image and move the map to its location. \n\n" +
                "- Tap next arrow after selecting an image to see the image in detail. \n\n" +
                "- Tap refresh to search for image within your vicinity. \n\n" +
                "- Long click on the map to search that location \n\n" +
                "- Search radius will expand until at least 12 images are found. \n\n" +
                "- Images displayed in reverse chronological order. \n\n" +
                "- Tap camera or add photo to upload images. \n\n";

        return info;
    }

    public static String personalInfo() {
        String info = "- Tap image to select the image and move the map to its location. \n\n" +
                "- Tap navigate next after selecting an image to see the image in detail. \n\n" +
                "- Long press on an image to delete the image. \n\n" +
                "- Tap refresh to reload all your images. \n\n" +
                "- The incognito icon indicates photo that are private. \n\n" +
                "- Images displayed in reverse chronological order. \n\n" +
                "- Tap camera or add photo to upload images. \n\n";

        return info;
    }

    public static String exploreInfo() {
        String info = "- Select location via: search, long click map, current location. \n\n" +
                "- Confirm location to begin searching for photo. \n\n" +
                "- Tap markers to show photo preview. \n\n" +
                "- Tap cluster bubble to show preview of all photos within that bubble. \n\n" +
                "- Tap the image preview or the full image button to see detailed image. \n\n" +
                "- Zooming in will split cluster bubble into its individual markers. \n\n" +
                "- Sometimes, when the camera is zoomed out too far, the markers don't render. So try moving/zooming camera around. \n\n";

        return info;
    }

    public static String cameraInfo() {
        String info = "- Before taking photo, app may request permission to enable GPS and Camera. \n\n" +
                "- Photo will not be taken if GPS is not activated. \n\n" +
                "- During photo preview, choose whether to upload as public or private. \n\n" +
                "- Due to security requirement, once photo is uploaded, its privacy mode cannot be changed. \n\n" +
                "- To change privacy setting later, you must delete and re-upload your photo. \n\n" +
                "- Internet access is required while upload occurs. \n\n" +
                "- Tap rotate left/right to correct photo orientation. Sometime photo don't orient correctly by default (depending on the camera you have). \n\n";



        return info;
    }

    public static String addPhotoInfo() {
        String info = "- App may request permission from you to access your storage. \n\n" +
                "- First select the photo you want to upload. \n\n" +
                "- Re-orient your photo using rotate left or right. \n\n" +
                "- During photo preview, choose whether to upload as public or private. \n\n" +
                "- Press next to continue upload and input your location details \n\n" +
                "- Choose photo location via: search, long click map, current location \n\n" +
                "- Due to security requirement, once photo is uploaded, its privacy mode cannot be changed. \n\n" +
                "- To change privacy setting later, you must delete and re-upload your photo. \n\n" +
                "- Internet access is required while upload occurs. \n\n";

        return info;
    }

    public static String settingInfo() {
        String info = "Hint: \n" +
                "- All license and attributions are displayed here. \n" +
                "- Click sign out to sign out of your account. \n" +
                "- Click the discord link if you like to join our discord channel. \n" +
                "\n(Note: click this window to close)";

        return info;

    }


}
