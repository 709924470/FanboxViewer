package cn.settile.fanboxviewer.PostDetail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import cn.settile.fanboxviewer.Adapters.RecyclerView.PostDetailListAdapter;
import cn.settile.fanboxviewer.R;

public class PostDetailList extends Fragment {
    public static final String ARG_1 = "Urls";
    private List<String> urls;

    public static PostDetailFragment newInstance(List<String> arg_urls) {
        PostDetailFragment fragment = new PostDetailFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_1, (ArrayList<String>) arg_urls);
        fragment.setArguments(args);
        return fragment;
    }

    public PostDetailList() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() == null)
            return;
        if (getArguments().containsKey(ARG_1)) {
            urls = getArguments().getParcelable(ARG_1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.list_card, container, false);
        RecyclerView rv = rootView.findViewById(R.id.card_list);

        rv.setAdapter(new PostDetailListAdapter((String[]) urls.toArray()));

        return rootView;
    }
}
