package cn.settile.fanboxviewer.Adapters.RecyclerView.Main;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.settile.fanboxviewer.Network.Bean.CardItem;
import cn.settile.fanboxviewer.Network.Bean.MessageItem;
import cn.settile.fanboxviewer.Fragments.Main.AllPostFragment;
import cn.settile.fanboxviewer.Network.Common;
import cn.settile.fanboxviewer.PostDetailActivity;
import cn.settile.fanboxviewer.R;
import cn.settile.fanboxviewer.UserDetailActivity;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import static cn.settile.fanboxviewer.Network.FanboxParser.userToName;

@Slf4j
public class AllPostsRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private AllPostFragment allPostFragment;
    private List<CardItem> cardItems;

    @Setter
    public OnBottomReachedListener onBottomReachedListener;
    private List<MessageItem> lmi;


    public AllPostsRecyclerViewAdapter(AllPostFragment apf, List<CardItem> cardItems){
        allPostFragment = apf;
        this.cardItems = cardItems;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? 0 : 1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if(i == 0){
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.component_post_plan_view, viewGroup, false);
            return new planViewHolder(v);
        }
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.component_item_card, viewGroup, false);
        return new itemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int i) {
        if(i == cardItems.size() - 1){
            onBottomReachedListener.onBottomReached(i);
        }
        if(getItemViewType(i) == 0){
            planViewOnBind((planViewHolder) holder);
        }else{
            originalOnBind((itemViewHolder) holder, i - 1);
        }
    }

    private void planViewOnBind(@NonNull planViewHolder holder){
        holder.nothing.setVisibility(View.VISIBLE);
        holder.layout.removeAllViewsInLayout();
        if(lmi.size() == 0){
            holder.nothing.setVisibility(View.VISIBLE);
            return;
        }
        holder.nothing.setVisibility(View.GONE);
        holder.layout.setVisibility(View.VISIBLE);
        holder.layout.removeAllViewsInLayout();
        for(MessageItem mi: lmi){
            RoundedImageView icon = new RoundedImageView(holder.itemView.getContext());
            icon.setLayoutParams(holder.param);
            icon.setVisibility(View.VISIBLE);
            icon.setCornerRadius(128.0f);
            icon.setBorderColor(ColorStateList.valueOf(0xCCCCCC));
            icon.setBorderWidth(2.0f);

            holder.layout.addView(icon);

            Picasso.get()
                    .load(mi.getIconUrl())
                    .placeholder(R.drawable.load_24dp)
                    .into(icon);

            icon.setOnClickListener(v -> {
                Intent i1 = new Intent(v.getContext(), UserDetailActivity.class);
                i1.putExtra("NAME", userToName.get(mi.extra));
                i1.putExtra("ICON", mi.getIconUrl());
                i1.putExtra("URL", mi.getUrl());
                i1.putExtra("CID", mi.extra);
                v.getContext().startActivity(i1);
            });
        }
        holder.layout.invalidate();
    }

    private void originalOnBind(@NonNull itemViewHolder holder, int i) {

        holder.item = cardItems.get(i);

        holder.userName.setText(cardItems.get(i).getCreator());
        holder.time.setText(cardItems.get(i).getCreateTime());
        holder.plan.setText(cardItems.get(i).getPlan());
        holder.title.setText(cardItems.get(i).getTitle());

        String desc = cardItems.get(i).getDesc();
        if(Objects.equals(desc, "")) {
            holder.desc.setVisibility(View.GONE);
            int padding_in_dp = 8;
            final float scale = holder.view.getResources().getDisplayMetrics().density;
            int padding_in_px = (int) (padding_in_dp * scale + 0.5f);
            holder.title.setPadding(0, 0, 0, padding_in_px);
        }else
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
                    .downloader(new OkHttp3Downloader(Common.getClientInstance()))
                    .build()
                    .load(header)
                    .placeholder(R.drawable.load_24dp)
                    .fit()
                    .centerCrop()
                    .into(holder.header);
        }
        holder.view.setOnClickListener(v -> {
            Intent i1 = new Intent(v.getContext(), PostDetailActivity.class);
            i1.putExtra("NAME", cardItems.get(i).getCreator());
            i1.putExtra("ICON", cardItems.get(i).getIconUrl());
            i1.putExtra("URL", cardItems.get(i).getUrl());
            i1.putExtra("COVER", cardItems.get(i).getHeaderUrl());
            i1.putExtra("TITLE", cardItems.get(i).getTitle());
            i1.putExtra("TIME", cardItems.get(i).getCreateTime());
            i1.putExtra("FEE", cardItems.get(i).getPlan());
            i1.putExtra("CID", cardItems.get(i).getUserId());
            v.getContext().startActivity(i1);
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

    public void refreshPlanView(List<MessageItem> lmi) {
        this.lmi = lmi;
    }

    public class itemViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_header_image) ImageView header;
        @BindView(R.id.item_card_avatar) ImageView userIcon;
        @BindView(R.id.item_card_user_name) TextView userName;
        @BindView(R.id.item_card_create_time) TextView time;
        @BindView(R.id.item_card_title) TextView title;
        @BindView(R.id.item_card_desc) TextView desc;
        @BindView(R.id.item_card_plan) TextView plan;
        @BindView(R.id.item_card_plan_div) View div;
        @BindView(R.id.item_card_plan_detail) TextView detail;
        public CardItem item;
        public final View view;

        public itemViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            view = itemView;
            div.setVisibility(View.GONE);
            detail.setVisibility(View.GONE);
        }
    }

    public class planViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.frag_post_plans)
        LinearLayout layout;

        @BindView(R.id.frag_post_plan_icon)
        RoundedImageView riv;
        @BindView(R.id.frag_post_icon_nothing)
        TextView nothing;

        final Context ctx;
        ViewGroup.LayoutParams param;

        public planViewHolder(@NonNull View itemView) {
            super(itemView);
            ctx = itemView.getContext();
            ButterKnife.bind(this, itemView);
            param = riv.getLayoutParams();
        }
    }

    public interface OnBottomReachedListener{
        void onBottomReached(int pos);
    }
}
