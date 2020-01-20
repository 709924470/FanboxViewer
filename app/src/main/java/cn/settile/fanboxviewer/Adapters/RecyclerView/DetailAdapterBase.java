package cn.settile.fanboxviewer.Adapters.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cn.settile.fanboxviewer.Adapters.Bean.DetailItem;
import cn.settile.fanboxviewer.R;

public class DetailAdapterBase extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int TYPE_IMAGE = 0;
    private final int TYPE_TEXT = 1;

    private List<DetailItem> detailItems;

    public DetailAdapterBase(List<DetailItem> detailItems) {
        this.detailItems = detailItems;
    }

    public DetailAdapterBase() {
        this.detailItems = new ArrayList<>();
    }

    public void updateItems(List<DetailItem> detailItems){
        if(Objects.equals(detailItems, null) || detailItems.isEmpty()){
            this.detailItems.clear();
            notifyDataSetChanged();
            return;
        }
        this.detailItems = detailItems;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return getItemType(position);
    }

    private int getItemType(int pos){
        return detailItems.get(pos).getType() == DetailItem.Type.IMAGE ? TYPE_IMAGE : TYPE_TEXT;
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
        }
        return new TextVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DetailItem detailItem = detailItems.get(position);
        if (TYPE_IMAGE == getItemType(position)){
            ImageVH ivh = (ImageVH) holder;
            Picasso.get()
                    .load(detailItem.content)
                    .placeholder(R.drawable.load_24dp)
                    .into(ivh.iv);
        }else if (TYPE_TEXT == getItemType(position)){
            TextVH tvh = (TextVH) holder;
            tvh.tv.setText(detailItem.getContent());
        }
    }

    @Override
    public int getItemCount() {
        return detailItems.size();
    }

    public class ImageVH extends RecyclerView.ViewHolder{
        ImageView iv;

        public ImageVH(@NonNull View itemView) {
            super(itemView);
            iv = itemView.findViewById(R.id.com_user_detail_img);
            iv.setVisibility(View.VISIBLE);
            itemView.findViewById(R.id.com_user_detail_text).setVisibility(View.GONE);

        }
    }

    public class TextVH extends RecyclerView.ViewHolder{
        TextView tv;

        public TextVH(@NonNull View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.com_user_detail_text);
            tv.setVisibility(View.VISIBLE);
            itemView.findViewById(R.id.com_user_detail_img).setVisibility(View.GONE);
        }
    }
}
