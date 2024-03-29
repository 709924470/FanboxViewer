package cn.settile.fanboxviewer.Fragments.Main;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.List;
import java.util.concurrent.Executors;

import cn.settile.fanboxviewer.Adapters.RecyclerView.Main.SubscribedPostsRecyclerViewAdapter;
import cn.settile.fanboxviewer.Network.Bean.CardItem;
import cn.settile.fanboxviewer.Network.RESTfulClient.FanboxUserParser;
import cn.settile.fanboxviewer.R;


public class SubscPostFragment extends Fragment {

    public Context c;
    private int lastVisibleItem;
    private View v;
    private RecyclerView recyclerView;
    private SubscribedPostsRecyclerViewAdapter adapter;
    private SwipeRefreshLayout srl;

    public SubscPostFragment() {
    }

    public static SubscPostFragment newInstance() {
        SubscPostFragment fragment = new SubscPostFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        android.view.View inflate = inflater.inflate(R.layout.fragment_main_post_list, container, false);

        v = inflate;
        c = inflate.getContext();

        recyclerView = v.findViewById(R.id.frag_post_list);
        LinearLayoutManager llm = new LinearLayoutManager(c);
        recyclerView.setLayoutManager(llm);

        adapter = new SubscribedPostsRecyclerViewAdapter();
        recyclerView.setAdapter(adapter);

        srl = v.findViewById(R.id.frag_post_refresh);

        adapter.setOnBottomReachedListener(pos -> {
            if (srl.isRefreshing()) {
                return;
            }
            srl.setRefreshing(true);
            refreshPosts(false,false);
        });

        srl.setOnRefreshListener(() -> refreshPosts(true, true));
        srl.setRefreshing(true);
        refreshPosts(true,true);
        return inflate;
    }


    public void refreshPosts(boolean refresh, boolean refreshAll) {
        Executors.newSingleThreadExecutor().submit(() -> {
            List<CardItem> lci = FanboxUserParser.getSupportingPosts(refresh, c);
            getActivity().runOnUiThread(() -> srl.setRefreshing(false));
            if (lci != null) {
                updateList(lci, refreshAll);
            }
            return null;
        });
    }

    public void updateList(List<CardItem> lci, boolean refreshAll) {
        if (v == null || c == null) {
            return;
        }
        if (recyclerView == null) {
            recyclerView = v.findViewById(R.id.frag_msg_list);
            recyclerView.setLayoutManager(new LinearLayoutManager(c));
            adapter = new SubscribedPostsRecyclerViewAdapter();
            adapter.updateItems(lci, true);
            recyclerView.setAdapter(adapter);
        } else {
            getActivity().runOnUiThread(() -> {
                adapter.updateItems(lci, refreshAll);
            });
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
