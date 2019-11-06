package cn.settile.fanboxviewer.Adapters.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.settile.fanboxviewer.Adapters.Bean.CardItem;
import cn.settile.fanboxviewer.Network.Common;
import cn.settile.fanboxviewer.R;
import cn.settile.fanboxviewer.TabFragments.AllPostFragment;
import cn.settile.fanboxviewer.TabFragments.AllPostFragment.OnListFragmentInteractionListener;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AllPostsRecyclerViewAdapter extends RecyclerView.Adapter<AllPostsRecyclerViewAdapter.viewHolder> {

    private AllPostFragment allPostFragment;
    private List<CardItem> cardItems;
    private OnListFragmentInteractionListener mListener;

    OnBottomReachedListener onBottomReachedListener;
    OnLastShownListener onLastShownListener;

    public AllPostsRecyclerViewAdapter(AllPostFragment apf, List<CardItem> cardItems, AllPostFragment.OnListFragmentInteractionListener mListener){
        allPostFragment = apf;
        this.cardItems = cardItems;
        this.mListener = mListener;
    }

    public void setOnBottomReachedListener(OnBottomReachedListener onBottomReachedListener){
        this.onBottomReachedListener = onBottomReachedListener;
    }

    public void setOnLastShownListener(OnLastShownListener onLastShownListener){
        this.onLastShownListener = onLastShownListener;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.component_item_card, viewGroup, false);
        return new viewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int i) {
//        if(i == cardItems.size() - 1){
//            onBottomReachedListener.onBottomReached(i);
//            if(holder.view.isShown()){
//                onLastShownListener.onLastShown(i);
//            }
//        }

        holder.item = cardItems.get(i);

        holder.userName.setText(cardItems.get(i).getCreator());
        holder.time.setText(cardItems.get(i).getCreateTime());
        holder.plan.setText(cardItems.get(i).getPlan());
        holder.title.setText(cardItems.get(i).getTitle());

        String desc = cardItems.get(i).getDesc();
        if(Objects.equals(desc, ""))
            holder.desc.setVisibility(View.GONE);
        else
            holder.desc.setText(desc);

        Picasso.get()
                .load(cardItems.get(i).getIconUrl())
                .placeholder(R.drawable.load_24dp)
                .into(holder.userIcon);

        String header = cardItems.get(i).getHeaderUrl();
        if (header.equals("null")){
            holder.header.setVisibility(View.GONE);
        }else {
            new Picasso.Builder(allPostFragment.c)
                    .downloader(new OkHttp3Downloader(Common.client))
                    .build()
                    .load(header)
                    .placeholder(R.drawable.load_24dp)
                    .fit()
                    .centerCrop()
                    .into(holder.header);
        }

        holder.view.setOnClickListener(v -> {
            if (null != mListener) {
                mListener.onListFragmentInteraction(holder.item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cardItems == null ? 0 : cardItems.size();
    }

    public void updateItems(List<CardItem> cardItems, boolean refreshAll) {
        if(refreshAll) {
            if(cardItems.equals(this.cardItems))
                return;
            this.cardItems = cardItems;
        } else {
            if(cardItems != null && this.cardItems != null)
                this.cardItems.addAll(cardItems);
        }
        notifyDataSetChanged();
    }

    public class viewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_header_image) ImageView header;
        @BindView(R.id.item_card_avatar) ImageView userIcon;
        @BindView(R.id.item_card_user_name) TextView userName;
        @BindView(R.id.item_card_create_time) TextView time;
        @BindView(R.id.item_card_title) TextView title;
        @BindView(R.id.item_card_desc) TextView desc;
        @BindView(R.id.item_card_plan) TextView plan;
        public CardItem item;
        public final View view;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            view = itemView;
        }
    }

    public interface OnBottomReachedListener{
        void onBottomReached(int pos);
    }

    public interface OnLastShownListener{
        void onLastShown(int pos);
    }
}
