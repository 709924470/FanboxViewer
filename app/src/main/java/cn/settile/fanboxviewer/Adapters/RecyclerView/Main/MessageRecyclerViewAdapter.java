package cn.settile.fanboxviewer.Adapters.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import cn.settile.fanboxviewer.Adapters.Bean.MessageItem;
import cn.settile.fanboxviewer.R;
import cn.settile.fanboxviewer.TabFragments.MainTab.MessageFragment;
import cn.settile.fanboxviewer.TabFragments.MainTab.MessageFragment.OnListFragmentInteractionListener;

public class MessageRecyclerViewAdapter extends RecyclerView.Adapter<MessageRecyclerViewAdapter.ViewHolder> {

    private List<MessageItem> messageItems;
    private final OnListFragmentInteractionListener mListener;
    private final MessageFragment parentView;

    public MessageRecyclerViewAdapter(MessageFragment mf, List<MessageItem> items, OnListFragmentInteractionListener listener) {
        messageItems = items;
        mListener = listener;
        this.parentView = mf;
    }

    public void updateItems(List<MessageItem> lmi, boolean refreshAll){
        if(refreshAll) {
            if(lmi.equals(this.messageItems))
                return;
            this.messageItems = lmi;
        } else {
            if(lmi != null && messageItems != null)
                this.messageItems.addAll(lmi);
        }
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.component_item_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = messageItems.get(position);
        holder.mIdView.setText(messageItems.get(position).getTitle() + parentView.c.getString(R.string.published));
        holder.mContentView.setText(messageItems.get(position).getMsg());

        Picasso.get()
                .load(messageItems.get(position).getIconUrl())
                .placeholder(R.drawable.load_24dp)
//                .resize(200, 200)
                .fit().centerCrop()
                .into(holder.mImgView);

        holder.mView.setOnClickListener(v -> {
            if (null != mListener) {
                // Notify the active callbacks interface (the activity, if the
                // fragment is attached to one) that an item has been selected.
                mListener.onListFragmentInteraction(holder.mItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return messageItems == null ? 0 : messageItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView mImgView;
        public final TextView mIdView;
        public final TextView mContentView;
        public MessageItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mImgView = view.findViewById(R.id.item_msg_avatar);
            mIdView = view.findViewById(R.id.item_msg_title);
            mContentView = view.findViewById(R.id.item_msg_content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
