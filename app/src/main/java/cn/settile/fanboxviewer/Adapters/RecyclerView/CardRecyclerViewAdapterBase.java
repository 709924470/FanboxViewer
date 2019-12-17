package cn.settile.fanboxviewer.Adapters.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.settile.fanboxviewer.Adapters.Bean.CardItem;
import cn.settile.fanboxviewer.Network.Common;
import cn.settile.fanboxviewer.R;

public class CardRecyclerViewAdapterBase extends RecyclerView.Adapter<CardRecyclerViewAdapterBase.PostViewHolder> {

    private List<CardItem> lci = new ArrayList<>();

    OnBottomReachedListener onBottomReachedListener;
    private AdapterView.OnItemClickListener onClickListener;

    public interface OnBottomReachedListener{
        void onBottomReached(int pos);
    }

    public void setOnItemClickListener(@NonNull AdapterView.OnItemClickListener listener){
        this.onClickListener = listener;
    }

    public void setOnBottomReachedListener(OnBottomReachedListener onBottomReachedListener){
        this.onBottomReachedListener = onBottomReachedListener;
    }

    public void updateItems(List<CardItem> cardItems, boolean refreshAll) {
        if(refreshAll) {
            if(cardItems.equals(this.lci))
                return;
            this.lci = cardItems;
        } else {
            if(cardItems != null && this.lci != null)
                this.lci.addAll(cardItems);
        }
        notifyDataSetChanged();
    }

    public CardRecyclerViewAdapterBase(){
        super();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.component_item_card, parent, false);
        return new PostViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        if(position == lci.size() - 1){
            onBottomReachedListener.onBottomReached(position);
        }

        holder.item = lci.get(position);

        holder.userName.setText(lci.get(position).getCreator());
        holder.time.setText(lci.get(position).getCreateTime());
        holder.plan.setText(lci.get(position).getPlan());
        holder.title.setText(lci.get(position).getTitle());

        String desc = lci.get(position).getDesc();
        if(Objects.equals(desc, "")) {
            holder.desc.setVisibility(View.GONE);
            int padding_in_dp = 8;
            final float scale = holder.view.getResources().getDisplayMetrics().density;
            int padding_in_px = (int) (padding_in_dp * scale + 0.5f);
            holder.title.setPadding(0, 0, 0, padding_in_px);
        }else
            holder.desc.setText(desc);

        Picasso.get()
                .load(lci.get(position).getIconUrl())
                .placeholder(R.drawable.load_24dp)
                .into(holder.userIcon);

        String header = lci.get(position).getHeaderUrl();
        if (header.equals("null")){
            holder.header.setVisibility(View.GONE);
        }else {
            new Picasso.Builder(holder.itemView.getContext())
                    .downloader(new OkHttp3Downloader(Common.client))
                    .build()
                    .load(header)
                    .placeholder(R.drawable.load_24dp)
                    .fit()
                    .centerCrop()
                    .into(holder.header);
        }
    }

    @Override
    public int getItemCount() {
        return lci.size();
    }

    public class PostViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_header_image)
        ImageView header;
        @BindView(R.id.item_card_avatar) ImageView userIcon;
        @BindView(R.id.item_card_user_name)
        TextView userName;
        @BindView(R.id.item_card_create_time) TextView time;
        @BindView(R.id.item_card_title) TextView title;
        @BindView(R.id.item_card_desc) TextView desc;
        @BindView(R.id.item_card_plan) TextView plan;
        @BindView(R.id.item_card_plan_div) View div;
        @BindView(R.id.item_card_plan_detail) TextView detail;
        public CardItem item;
        public final View view;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            view = itemView;
            div.setVisibility(View.GONE);
            detail.setVisibility(View.GONE);
        }
    }
}
