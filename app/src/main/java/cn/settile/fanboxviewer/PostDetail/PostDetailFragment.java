package cn.settile.fanboxviewer.PostDetail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import cn.settile.fanboxviewer.Adapters.Bean.CardItem;
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

    public static PostDetailFragment newInstance() {
        PostDetailFragment fragment = new PostDetailFragment();
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
        return rootView;
    }
}
