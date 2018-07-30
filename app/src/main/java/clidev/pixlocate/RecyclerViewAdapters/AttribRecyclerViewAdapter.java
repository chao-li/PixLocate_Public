package clidev.pixlocate.RecyclerViewAdapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.PointerIcon;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import clidev.pixlocate.Licensing.IconAttribListCreator;
import clidev.pixlocate.Licensing.IconObject;
import clidev.pixlocate.R;

public class AttribRecyclerViewAdapter extends RecyclerView.Adapter<AttribRecyclerViewAdapter.AttribViewHolder>{

    private List<IconObject> mIconList;
    private Context mContext;

    public AttribRecyclerViewAdapter (Context context) {
        mIconList = IconAttribListCreator.CreateIconAttribList();
        mContext = context;
    }

    @NonNull
    @Override
    public AttribViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_icon_list, parent, false);

        AttribViewHolder viewHolder = new AttribViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull AttribViewHolder holder, int position) {

        // loading image into the recycler view item
        if (position != RecyclerView.NO_POSITION) {

            RequestOptions options = new RequestOptions();
            options.centerCrop();
            //options.placeholder(R.drawable.image_loading);
            options.override(150, 150);


            Glide.with(mContext)
                    .load(mIconList.get(position).getImageId())
                    .apply(options)
                    .into(holder.mAttribImage);

        } else {
            Glide.with(mContext).clear(holder.mAttribImage);
            holder.mAttribImage.setImageDrawable(null);
        }

        holder.mAttribText.setText("Icon created by " + mIconList.get(position).getAuthor() +
        " from " + mIconList.get(position).getWebsite() + ".");


    }

    @Override
    public int getItemCount() {
        return mIconList.size();
    }

    public class AttribViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.attrib_image)
        ImageView mAttribImage;

        @BindView(R.id.attrib_text)
        TextView mAttribText;


        public AttribViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}
