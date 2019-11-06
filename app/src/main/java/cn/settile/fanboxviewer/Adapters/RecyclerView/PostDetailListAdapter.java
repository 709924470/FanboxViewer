package cn.settile.fanboxviewer.Adapters.RecyclerView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.Arrays;
import java.util.List;

import cn.settile.fanboxviewer.Network.Common;
import cn.settile.fanboxviewer.R;

public class PostDetailListAdapter extends RecyclerView.Adapter<PostDetailListAdapter.ViewHolder> {
    List<String> urls;
    Context ctx;

    public PostDetailListAdapter(Context ctx){
        this.ctx = ctx;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        ImageView v = (ImageView) LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.component_item_post_image, viewGroup, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Picasso p = new Picasso.Builder(viewHolder.post.getContext())
                .downloader(new OkHttp3Downloader(Common.client))
                .build();
        Picasso.get()
            .load(urls.get(i))
            .into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            });
    }

    @Override
    public int getItemCount() {
        return urls.size();
    }

    public PostDetailListAdapter(String[] urls){
        this.urls = Arrays.asList(urls);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView post;
        LinearLayout placeholder;
        ProgressBar pb;
        TextView info;

        public ViewHolder(View itemView) {
            super(itemView);
            post = itemView.findViewById(R.id.detail_image);
            placeholder = itemView.findViewById(R.id.placeHolder_item);
            pb = itemView.findViewById(R.id.img_progressBar);
            info = itemView.findViewById(R.id.load_info);
        }
    }
}
