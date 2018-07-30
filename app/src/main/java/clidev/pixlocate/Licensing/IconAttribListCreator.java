package clidev.pixlocate.Licensing;

import java.util.ArrayList;
import java.util.List;

import clidev.pixlocate.R;

public final class IconAttribListCreator {

    public static List<IconObject> CreateIconAttribList() {

        List<IconObject> iconAttribList = new ArrayList<>();

        // Camera trigger
        IconObject camTrigger = new IconObject(R.drawable.take_photo,
                "Bogdan Rosu",
                "https://www.flaticon.com/authors/bogdan-rosu");

        iconAttribList.add(camTrigger);

        // Camera flash
        IconObject camFlash = new IconObject(R.drawable.flash_on,
        "Freepik",
        "http://www.freepik.com");

        iconAttribList.add(camFlash);

        // Camera no flash
        IconObject camNoFlash = new IconObject(R.drawable.flash_off,
                "Plainicon",
                "https://www.flaticon.com/authors/plainicon");

        iconAttribList.add(camNoFlash);

        // Camera rotate
        IconObject camRotate = new IconObject(R.drawable.camera_rotate,
        "Those Icons",
        "https://www.flaticon.com/authors/those-icons");

        iconAttribList.add(camRotate);

        // gallery clicked
        IconObject galleryClicked = new IconObject(R.drawable.gallery_clicked,
                "Lucy G",
                "https://www.flaticon.com/authors/lucy-g");

        iconAttribList.add(galleryClicked);

        // gallery unclicked
        IconObject galleryUnclicked = new IconObject(R.drawable.gallery_unclicked,
                "Lucy G",
                "https://www.flaticon.com/authors/lucy-g");

        iconAttribList.add(galleryUnclicked);

        // Personal clicked
        IconObject personalClicked = new IconObject(R.drawable.personal_clicked,
                "Lucy G",
                "https://www.flaticon.com/authors/lucy-g");

        iconAttribList.add(personalClicked);

        // Personal unclicked
        IconObject personalUnclicked = new IconObject(R.drawable.personal_unclicked,
                "Lucy G",
                "https://www.flaticon.com/authors/lucy-g");

        iconAttribList.add(personalUnclicked);

        // Explore clicked
        IconObject exploreClicked = new IconObject(R.drawable.explore_clicked,
        "Smashicons",
        "https://www.flaticon.com/authors/smashicons");

        iconAttribList.add(exploreClicked);

        // Explore unclicked
        IconObject exploreUnclicked = new IconObject(R.drawable.explore_unclicked,
                "Smashicons",
                "https://www.flaticon.com/authors/smashicons");

        iconAttribList.add(exploreUnclicked);

        // setting clicked
        IconObject settingClicked = new IconObject(R.drawable.menu_clicked,
                "Lucy G",
                "https://www.flaticon.com/authors/lucy-g");

        iconAttribList.add(settingClicked);

        // setting unclicked
        IconObject settingUnclicked = new IconObject(R.drawable.menu_unclicked,
                "Lucy G",
                "https://www.flaticon.com/authors/lucy-g");

        iconAttribList.add(settingUnclicked);

        // cancel upload
        IconObject cancel = new IconObject(R.drawable.cancel,
                "Hadrien",
                "https://www.flaticon.com/authors/hadrien");

        iconAttribList.add(cancel);

        // upload
        IconObject upload = new IconObject(R.drawable.upload,
                "Gregor Cresnar",
                "https://www.flaticon.com/authors/gregor-cresnar");

        iconAttribList.add(upload);

        // ninja
        IconObject ninja = new IconObject(R.drawable.ninja,
                "Iconnice",
                "https://www.flaticon.com/authors/iconnice");

        iconAttribList.add(ninja);

        // image lading
        IconObject imageLoad = new IconObject(R.drawable.image_loading,
                "Roundicons",
                "https://www.flaticon.com/authors/roundicons");

        iconAttribList.add(imageLoad);

        // rotate left
        IconObject rotateLeft = new IconObject(R.drawable.rotate_left,
                "Vaadin",
                "https://www.flaticon.com/authors/vaadin");

        iconAttribList.add(rotateLeft);


        // rotate right
        IconObject rotateRight = new IconObject(R.drawable.rotate_right,
                "Vaadin",
                "https://www.flaticon.com/authors/vaadin");

        iconAttribList.add(rotateRight);

        // back arrow
        IconObject backArrow = new IconObject(R.drawable.left_arrow,
                "Roundicons",
                "https://www.flaticon.com/authors/roundicons");

        iconAttribList.add(backArrow);

        // next arrow
        IconObject nextArrow = new IconObject(R.drawable.right_arrow,
                "Roundicons",
                "https://www.flaticon.com/authors/roundicons");

        iconAttribList.add(nextArrow);

        IconObject travel = new IconObject(R.drawable.travel,
                "Icon Pond",
                "https://www.flaticon.com/authors/popcorns-arts");

        iconAttribList.add(travel);

        IconObject cameraColor = new IconObject(R.drawable.camera_color,
                "Freepik",
                "http://www.freepik.com");

        iconAttribList.add(cameraColor);

        IconObject welcome = new IconObject(R.drawable.welcome_small,
                "Johan Arthursson",
                "http://www.unsplash.com/@johanarthur");

        iconAttribList.add(welcome);

        return iconAttribList;
    }
}
