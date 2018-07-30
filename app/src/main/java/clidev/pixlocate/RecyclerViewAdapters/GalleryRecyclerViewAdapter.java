package clidev.pixlocate.RecyclerViewAdapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.LongToIntFunction;

import clidev.pixlocate.Activities.DetailedImageActivity;
import clidev.pixlocate.CustomLayouts.SquareRelativeLayout;
import clidev.pixlocate.FirebaseDataObjects.FirebaseImageWithLocation;
import clidev.pixlocate.Keys.PutExtraKeys;
import clidev.pixlocate.R;
import timber.log.Timber;

public class GalleryRecyclerViewAdapter extends RecyclerView.Adapter<GalleryRecyclerViewAdapter.GalleryViewHolder> {


    private Context mContext;
    private List<FirebaseImageWithLocation> mImageDataList;
    private Boolean isPersonalGallery;
    private int mClickedPosition = -1;

    private ItemSelectionHandler mItemSelectionHandler;

    public interface ItemSelectionHandler {
        void OnPhotoHighlighted(Double latitude, Double longitude, FirebaseImageWithLocation selecteImage);
        void OnPhotoLongPressed(FirebaseImageWithLocation selectedImage);
    }



    // constructor for gallery map fragment
    public GalleryRecyclerViewAdapter (Context context, ItemSelectionHandler itemSelectionHandler, Boolean isPersonalGallery){

        mContext = context;
        mItemSelectionHandler = itemSelectionHandler;
        mImageDataList = new ArrayList<>();
        this.isPersonalGallery = isPersonalGallery;
    }



    // public methods to be accessed by Gallery Fragment
    public void deleteItem(FirebaseImageWithLocation firebaseImageWithLocation) {

        mImageDataList.remove(firebaseImageWithLocation);

        notifyDataSetChanged();

    }

    public void clear() {
        mClickedPosition = -1;
        if (mImageDataList != null) {
            mImageDataList.clear();
        }
        notifyDataSetChanged();
    }

    public FirebaseImageWithLocation getFirstImageData() {
        // make it seems like we clicked on the first item
        mClickedPosition = 0;
        notifyDataSetChanged();
        return mImageDataList.get(0);
    }

    public void setAllData(ArrayList<FirebaseImageWithLocation> allImageData) {
        if (allImageData != null) {
            mImageDataList = allImageData;

            // sort data in reverse chronological order
            Collections.sort(mImageDataList, new Comparator<FirebaseImageWithLocation>() {
                @Override
                public int compare(FirebaseImageWithLocation t1, FirebaseImageWithLocation t2) {
                    return String.valueOf(t2.getImageKey()).compareTo(t1.getImageKey());
                }
            });

            notifyDataSetChanged();
        }
    }

    public void setNewData(FirebaseImageWithLocation firebaseImageWithLocation) {

        // create a list of image keys from the mImageDataList.
        List<String> imageKeyList = new ArrayList<>();

        for (int i = 0; i < mImageDataList.size(); i++) {
            imageKeyList.add(mImageDataList.get(i).getImageKey());
        }

        // check if this new image object have an image key that is already contained within out database.
        Boolean isDuplicate = imageKeyList.contains(firebaseImageWithLocation.getImageKey());

        Timber.d("is duplicate: " + isDuplicate);

        if (isDuplicate == false) {
            Timber.d("Not a duplicate: " + firebaseImageWithLocation.getImageKey());

            // implement a method to update the recyclerview with data
            mImageDataList.add(firebaseImageWithLocation);

            // sort data in reverse chronological order
            Collections.sort(mImageDataList, new Comparator<FirebaseImageWithLocation>() {
                @Override
                public int compare(FirebaseImageWithLocation t1, FirebaseImageWithLocation t2) {
                    return String.valueOf(t2.getImageKey()).compareTo(t1.getImageKey());
                }
            });

            notifyDataSetChanged();
        } else {
            Timber.d("Is a duplicate: " + firebaseImageWithLocation.getImageKey());

        }
    }

    public void removeDataByKey(String key) {
        // first create an array of those image keys
        List<String> keyList = new ArrayList<>();

        for (int i = 0; i < mImageDataList.size(); i++) {

            keyList.add(mImageDataList.get(i).getImageKey());
        }

        int removeIndex = keyList.indexOf(key);

        // now remove the corresponding data from this index
        mImageDataList.remove(removeIndex);

        notifyDataSetChanged();

    }

    public FirebaseImageWithLocation getLastItem() {
        return mImageDataList.get(mImageDataList.size() - 1);
    }



    @NonNull
    @Override
    public GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_gallery_list, parent, false);

        GalleryViewHolder viewHolder = new GalleryViewHolder(view);


        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryViewHolder holder, int position) {

        // highlighting clicked position
        if (mClickedPosition == position) {
            holder.mLayout.setBackgroundColor(Color.parseColor("#FFFF4081"));
        } else {
            holder.mLayout.setBackgroundColor(Color.parseColor("#FFFFFFFF"));
        }

        // loading image into the recycler view item
        if (position != RecyclerView.NO_POSITION) {

            RequestOptions options = new RequestOptions();
            options.centerCrop();
            options.placeholder(R.drawable.image_loading);
            options.override(150, 150);


            Glide.with(mContext)
                    .load(mImageDataList.get(position).getSmallImageUrl())
                    .apply(options)
                    .into(holder.mPhoto);

        } else {
            Glide.with(mContext).clear(holder.mPhoto);
            holder.mPhoto.setImageDrawable(null);
        }

        // attaching tags to identify views
        holder.itemView.setTag(mImageDataList.get(position));

        // Indicate if photo is private
        if (isPersonalGallery == true) {
            if (mImageDataList.get(position).getPrivatePhoto() == true) {
                holder.mPrivateImage.setVisibility(View.VISIBLE);
            } else {
                holder.mPrivateImage.setVisibility(View.INVISIBLE);
            }
        }



    }

    @Override
    public int getItemCount() {
        return mImageDataList.size();
    }

    // View holder class
    public class GalleryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnLongClickListener {

        private SquareRelativeLayout mLayout;
        private ImageView mPhoto;
        private ImageView mPrivateImage;

        public GalleryViewHolder(View itemView) {
            super(itemView);

            mLayout = itemView.findViewById(R.id.recyclerview_background_layout);
            mPhoto = itemView.findViewById(R.id.gallery_rv_image_view);
            mPrivateImage = itemView.findViewById(R.id.rv_private);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }


        @Override
        public void onClick(View view) {
            // identify and save the position where the item is clicked
            int position = getAdapterPosition();

            // check if index is out of bounds
            Boolean inBounds = (position >= 0) && (position < mImageDataList.size());

            Timber.d("index is inbound: " + inBounds);

            if (inBounds == true) {
                mClickedPosition = position;
                notifyDataSetChanged();

                // notify the map fragment to change map location accordingly.
                Double latitude = mImageDataList.get(position).getLatitude();
                Double longitude = mImageDataList.get(position).getLongitude();

                //notify the gallery fragment which picture was selected
                FirebaseImageWithLocation selectedImage = mImageDataList.get(position);

                mItemSelectionHandler.OnPhotoHighlighted(latitude, longitude, selectedImage);




            }
        }

        @Override
        public boolean onLongClick(View view) {


            int position = getAdapterPosition();

            // check if index is out of bounds
            Boolean inBounds = (position >= 0) && (position < mImageDataList.size());

            Timber.d("index is inbound: " + inBounds);

            if (inBounds == true) {
                FirebaseImageWithLocation selectedPhoto = mImageDataList.get(position);

                mItemSelectionHandler.OnPhotoLongPressed(selectedPhoto);

                return true;
            } else {
                return false;
            }
        }

    }

}
