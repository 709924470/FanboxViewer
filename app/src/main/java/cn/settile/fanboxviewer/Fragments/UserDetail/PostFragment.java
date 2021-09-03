package cn.settile.fanboxviewer.Fragments.UserDetail;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.List;
import java.util.concurrent.Executors;

import cn.settile.fanboxviewer.Adapters.RecyclerView.CardRecyclerViewAdapterBase;
import cn.settile.fanboxviewer.Network.Bean.CardItem;
import cn.settile.fanboxviewer.Network.RESTfulClient.FanboxUserParser;
import cn.settile.fanboxviewer.R;
import lombok.Setter;


//@Slf4j
public class PostFragment extends Fragment {

    public Activity c;
    public CardRecyclerViewAdapterBase adapter;
    @Setter
    public String userID;
    public String nextUrl = null;
    private View v;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout srl;
    List<CardItem> lci;
    public PostFragment() {
    }

    public static PostFragment newInstance() {
        PostFragment fragment = new PostFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        c = getActivity();
        try {
            lci = new FanboxUserParser(userID).getUserPosts();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_main_post_list, container, false);

        v = inflate;

        recyclerView = v.findViewById(R.id.frag_post_list);
        LinearLayoutManager llm = new LinearLayoutManager(c);
        recyclerView.setLayoutManager(llm);
        adapter = new CardRecyclerViewAdapterBase();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(v.getContext(), R.anim.layout_default));
        recyclerView.scheduleLayoutAnimation();

        srl = v.findViewById(R.id.frag_post_refresh);
        adapter.setOnBottomReachedListener(pos -> {
            if (srl.isRefreshing()) {
                return;
            }
            srl.setRefreshing(true);
            refreshPosts(false, false);
        });
        srl.setOnRefreshListener(() -> refreshPosts(false, true));
        srl.setRefreshing(true);
        refreshPosts(true, true);

        return inflate;
    }

    public void refreshPosts(boolean refresh, boolean refreshAll) {
        Executors.newSingleThreadExecutor().submit(() -> {

            getActivity().runOnUiThread(() -> srl.setRefreshing(false));
            if (lci != null) {
                updateList(lci, refreshAll);
            }
            return null;
        });
    }

    public void updateList(List<CardItem> lci, boolean refreshAll) {
        if (v == null || c == null || userID == null) {
            return;
        }
        if (recyclerView == null) {
            recyclerView = v.findViewById(R.id.frag_msg_list);
            recyclerView.setLayoutManager(new LinearLayoutManager(c));
            adapter = new CardRecyclerViewAdapterBase();
            recyclerView.setAdapter(adapter);
        } else {
            getActivity().runOnUiThread(() -> adapter.updateItems(lci, refreshAll));
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
