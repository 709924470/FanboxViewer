package cn.settile.fanboxviewer.PostDetail;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import cn.settile.fanboxviewer.Bean.CardItem;
import cn.settile.fanboxviewer.R;

/**
 * A fragment representing a single Post detail screen.
 * This fragment is either contained in a {@link PostListActivity}
 * in two-pane mode (on tablets) or a {@link PostDetailActivity}
 * on handsets.
 */
public class PostDetailFragment extends Fragment {
    public static final String ARG_1 = "CardItem";
    private CardItem bean;

    public static PostDetailFragment newInstance(CardItem pc) {
        PostDetailFragment fragment = new PostDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_1, pc);
        fragment.setArguments(args);
        return fragment;
    }

    public PostDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() == null)
            return;
        if (getArguments().containsKey(ARG_1)) {
            bean = getArguments().getParcelable(ARG_1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.component_item_card, container, false);
        ImageView iv = rootView.findViewById(R.id.headerImage);
        Picasso.get()
                .load(bean.header)
                .into(iv);
        ImageView aiv = rootView.findViewById(R.id.creatorAvatar);
        Picasso.get()
                .load(bean.avatar)
                .into(aiv);
        TextView mName = rootView.findViewById(R.id.id_text);
        mName.setText(bean.creator);
        TextView mDesc = rootView.findViewById(R.id.content);
        mDesc.setText(bean.url);
        return rootView;
    }
}
