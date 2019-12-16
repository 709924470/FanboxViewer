package cn.settile.fanboxviewer.Adapters.RecyclerView.UserDetail;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import cn.settile.fanboxviewer.Adapters.Bean.MessageItem;

public class UserDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int TYPE_IMAGE = 0;
    private final int TYPE_TEXT = 1;

    private List<MessageItem> lmi;

    @Override
    public int getItemViewType(int position) {
        return getItemType(position);
    }

    private int getItemType(int pos){
        return lmi.get(pos).getTitle().equals("IMG") ? TYPE_IMAGE : TYPE_TEXT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (TYPE_IMAGE == viewType){
            return new ImageVH(parent.getRootView());
        }else if (TYPE_TEXT == viewType){
            return new TextVH(parent.getRootView());
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (TYPE_IMAGE == getItemType(position)){
            ImageVH ivh = (ImageVH) holder;
            MessageItem mi = lmi.get(position);
            Picasso.get()
                    .load(mi.getUrl())
                    .into(ivh.iv);
        }else if (TYPE_TEXT == getItemType(position)){
        }
    }

    @Override
    public int getItemCount() {
        return lmi.size();
    }

    public class ImageVH extends RecyclerView.ViewHolder{
        ImageView iv;

        public ImageVH(@NonNull View itemView) {
            super(itemView);
            iv = new ImageView(itemView.getContext());

        }
    }

    public class TextVH extends RecyclerView.ViewHolder{
        TextView tv;

        public TextVH(@NonNull View itemView) {
            super(itemView);
            tv = new TextView(itemView.getContext());
        }
    }
}
