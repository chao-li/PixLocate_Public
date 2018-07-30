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

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;


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

public class PreviewRecyclerViewAdapter extends RecyclerView.Adapter<PreviewRecyclerViewAdapter.PreviewViewHolder> {


    private Context mContext;
    private List<FirebaseImageWithLocation> mImageDataList;


    private int mClickedPosition = -1;

    private ItemSelectionHandler mItemSelectionHandler;

    public interface ItemSelectionHandler {
        void OnPhotoHighlighted(Double latitude, Double longitude);

    }


    // Constructor
    public PreviewRecyclerViewAdapter (Context context){

        mContext = context;
        mImageDataList = new ArrayList<>();
    }

    // constructor for gallery map fragment
    public PreviewRecyclerViewAdapter (Context context, ItemSelectionHandler itemSelectionHandler){

        mContext = context;
        mItemSelectionHandler = itemSelectionHandler;
        mImageDataList = new ArrayList<>();
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
        // Only pass in this data if this data does not exist.
        if (mImageDataList.contains(firebaseImageWithLocation) == false) {

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



    @NonNull
    @Override
    public PreviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_gallery_list, parent, false);

        PreviewViewHolder viewHolder = new PreviewViewHolder(view);


        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PreviewViewHolder holder, int position) {

        // highlighting clicked position
        if (mClickedPosition == position) {
            holder.mLayout.setBackgroundColor(Color.parseColor("#FFFF4081"));
        } else {
            holder.mLayout.setBackgroundColor(Color.parseColor("#FFFFFFFF"));
        }


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



    }

    @Override
    public int getItemCount() {
        return mImageDataList.size();
    }





    // View holder class
    public class PreviewViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private SquareRelativeLayout mLayout;
        private ImageView mPhoto;

        public PreviewViewHolder(View itemView) {
            super(itemView);

            mLayout = itemView.findViewById(R.id.recyclerview_background_layout);
            mPhoto = itemView.findViewById(R.id.gallery_rv_image_view);

            itemView.setOnClickListener(this);
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

                //pass the selected image to detail view
                FirebaseImageWithLocation imageData = mImageDataList.get(position);
                Intent intent = new Intent(mContext, DetailedImageActivity.class);
                intent.putExtra(PutExtraKeys.IMAGE_OBJECT, imageData);

                mContext.startActivity(intent);
            }

        }

    }

}
