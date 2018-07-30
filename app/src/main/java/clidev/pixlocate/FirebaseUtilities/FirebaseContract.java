package clidev.pixlocate.FirebaseUtilities;

public final class FirebaseContract {

    public static final class ImageDatabase {
        public static final String PUBLIC_IMAGES = "public_images";
        public static final String ALL_USER_IMAGES = "all_user_images";
        public static final String IMAGE_UNIQUE_NAME = "image_unique_name";

        public static final String PUBLIC = "public";
        public static final String PRIVATE = "private";

    }

    public static final class ImageGeofireDatabase {
        public static final String IMAGE_LOCATION = "image_location";
        public static final String EACH_USER = "each_user";
        public static final String PUBLIC = "public";
        public static final String PRIVATE = "private";
    }

    public static final class ImageStorage {
        public static final String PUBLIC_IMAGE_FOLDER = "public_images";
        public static final String PRIVATE_IMAGE_FOLDER = "private_images";
        public static final String PUBLIC_SMALL_IMAGE_FOLDER = "public_small_images";
        public static final String PRIVATE_SMALL_IMAGE_FOLDER = "private_small_images";

    }

    public static final class User {
        public static final String USER_DETAILS = "user_details";
    }
}
