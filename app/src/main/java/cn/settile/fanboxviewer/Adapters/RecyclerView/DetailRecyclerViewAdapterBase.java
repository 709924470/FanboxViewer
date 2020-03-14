package cn.settile.fanboxviewer.Adapters.RecyclerView;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.potyvideo.library.AndExoPlayerView;
import com.potyvideo.library.globalEnums.EnumResizeMode;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import cn.settile.fanboxviewer.Adapters.Bean.DetailItem;
import cn.settile.fanboxviewer.ImageViewActivity;
import cn.settile.fanboxviewer.Network.WebViewCookieHandler;
import cn.settile.fanboxviewer.R;
import okhttp3.Cookie;
import okhttp3.HttpUrl;

public class DetailRecyclerViewAdapterBase extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int TYPE_IMAGE = 0;
    private final int TYPE_TEXT = 1;
    private final int TYPE_VIDEO = 2;
    private final int TYPE_LINK = 3;

    private String detail = "undefined";

    private List<DetailItem> detailItems;
    public List<String> images = new ArrayList<>();
    public List<String> thumbs = new ArrayList<>();

    public DetailRecyclerViewAdapterBase(String detail) {
        this.detail = detail;
        this.detailItems = new ArrayList<>();
    }

    public DetailRecyclerViewAdapterBase() {
        this.detailItems = new ArrayList<>();
    }

    public void updateItems(List<DetailItem> detailItems){
        if(Objects.equals(detailItems, null) || detailItems.isEmpty()){
            this.detailItems.clear();
            images.clear();
            thumbs.clear();
            notifyDataSetChanged();
            return;
        }
        this.detailItems = detailItems;
        for(DetailItem di: detailItems){
            if(di.getType() == DetailItem.Type.IMAGE){
                images.add((String) di.extra.get(0));
                thumbs.add(di.getContent());
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return getItemType(position);
    }

    private int getItemType(int pos){
        if (detailItems.get(pos).getType() == DetailItem.Type.IMAGE){
            return TYPE_IMAGE;
        }
        else if (detailItems.get(pos).getType() == DetailItem.Type.TEXT){
            return TYPE_TEXT;
        }
        else if (detailItems.get(pos).getType() == DetailItem.Type.VIDEO){
            return TYPE_VIDEO;
        }
        return TYPE_TEXT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.component_item_user_detail, parent, false);
        if (TYPE_IMAGE == viewType){
            return new ImageVH(v);
        }else if (TYPE_TEXT == viewType){
            return new TextVH(v);
        }else if (TYPE_VIDEO == viewType){
            return new VideoVH(v);
        }else if (TYPE_LINK == viewType){
            return new TextVH(v);
        }
        return new TextVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DetailItem detailItem = detailItems.get(position);
        if (TYPE_IMAGE == getItemType(position)){
            ImageVH ivh = (ImageVH) holder;
            if(detailItem.content.equals("false")){
                ivh.iv.setImageDrawable(holder.itemView.getResources().getDrawable(R.drawable.ic_lock_black_24dp));
                return;
            }
            Picasso.get()
                    .load(detailItem.content)
                    .placeholder(R.drawable.load_24dp)
                    .into(ivh.iv);
            ivh.iv.setOnClickListener(v -> {
                Intent i = new Intent(holder.itemView.getContext(), ImageViewActivity.class);
                i.putExtra("Position", position);
                i.putStringArrayListExtra("Images", (ArrayList<String>) images);
                i.putStringArrayListExtra("Thumbnails", (ArrayList<String>) thumbs);
                i.putExtra("Details", detail);
                holder.itemView.getContext().startActivity(i);
            });
        }else if (TYPE_TEXT == getItemType(position)){
            TextVH tvh = (TextVH) holder;
            tvh.tv.setText(detailItem.getContent());
        }else if (TYPE_VIDEO == getItemType(position)){
            VideoVH vvh = (VideoVH) holder;
            vvh.player.setResizeMode(EnumResizeMode.FIT);
            HashMap<String, String> cookies = new HashMap<>();
            cookies.put("Cookie", new WebViewCookieHandler().loadForRequest(detailItem.content));
            vvh.player.setShowController(true);
            vvh.player.setSource(detailItem.content, cookies);
            vvh.player.pausePlayer();
            vvh.player.setShowFullScreen(true);
            vvh.player.setPlayWhenReady(false);
        }
    }

    @Override
    public int getItemCount() {
        return detailItems.size();
    }

    class VideoVH extends RecyclerView.ViewHolder{
        AndExoPlayerView player;
        TextView text;
        Button download;

        public VideoVH(@NonNull View itemView) {
            super(itemView);
            player = itemView.findViewById(R.id.com_user_detail_video);
            player.setVisibility(View.VISIBLE);
            download = itemView.findViewById(R.id.com_user_detail_dl);
            download.setVisibility(View.VISIBLE);
            text = itemView.findViewById(R.id.com_user_detail_text);
            text.setVisibility(View.GONE);
            itemView.findViewById(R.id.com_user_detail_img).setVisibility(View.GONE);
        }
    }

    class ImageVH extends RecyclerView.ViewHolder{
        ImageView iv;

        public ImageVH(@NonNull View itemView) {
            super(itemView);
            iv = itemView.findViewById(R.id.com_user_detail_img);
            iv.setVisibility(View.VISIBLE);
            itemView.findViewById(R.id.com_user_detail_text).setVisibility(View.GONE);
            itemView.findViewById(R.id.com_user_detail_video).setVisibility(View.GONE);
            itemView.findViewById(R.id.com_user_detail_dl).setVisibility(View.GONE);

        }
    }

    class TextVH extends RecyclerView.ViewHolder{
        TextView tv;

        public TextVH(@NonNull View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.com_user_detail_text);
            tv.setVisibility(View.VISIBLE);
            itemView.findViewById(R.id.com_user_detail_img).setVisibility(View.GONE);
            itemView.findViewById(R.id.com_user_detail_video).setVisibility(View.GONE);
            itemView.findViewById(R.id.com_user_detail_dl).setVisibility(View.GONE);
        }
    }
}
