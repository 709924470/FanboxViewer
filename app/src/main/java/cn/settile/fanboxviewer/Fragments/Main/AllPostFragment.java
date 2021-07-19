package cn.settile.fanboxviewer.Fragments.Main;

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

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import cn.settile.fanboxviewer.Network.Bean.CardItem;
import cn.settile.fanboxviewer.Network.Bean.MessageItem;
import cn.settile.fanboxviewer.Adapters.RecyclerView.Main.AllPostsRecyclerViewAdapter;
import cn.settile.fanboxviewer.Network.RESTfulClient.FanboxParser;
import cn.settile.fanboxviewer.R;


//@Slf4j
public class AllPostFragment extends Fragment {

    private View v;
    public Activity ctx;
    private RecyclerView recyclerView;
    public AllPostsRecyclerViewAdapter adapter;
    private SwipeRefreshLayout srl;

    public AllPostFragment() {
    }

    public static AllPostFragment newInstance() {
        return new AllPostFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_main_post_list, container, false);

        v = inflate;

        recyclerView = v.findViewById(R.id.frag_post_list);

        LinearLayoutManager llm = new LinearLayoutManager(ctx);
        recyclerView.setLayoutManager(llm);

        adapter = new AllPostsRecyclerViewAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(v.getContext(), R.anim.layout_default));
        recyclerView.scheduleLayoutAnimation();

        srl = v.findViewById(R.id.frag_post_refresh);

        adapter.setOnBottomReachedListener(pos -> {
            if (srl.isRefreshing()) {
                return;
            }
            srl.setRefreshing(true);
            Executors.newSingleThreadExecutor().submit(() -> {
                List<CardItem> lci = FanboxParser.getAllPosts(false, ctx);
                List<MessageItem> lmi = FanboxParser.getPlans();
                getActivity().runOnUiThread(() -> srl.setRefreshing(false));
                if (lci != null) {
                    updateList(lci, lmi, false);
                }
                return null;
            });
        });

        srl.setOnRefreshListener(() -> Executors.newSingleThreadExecutor().submit(() -> {
            List<CardItem> lci = FanboxParser.getAllPosts(true, ctx);
            List<MessageItem> lmi = FanboxParser.getPlans();
            getActivity().runOnUiThread(() -> srl.setRefreshing(false));
            if (lci != null) {
                updateList(lci, lmi, true);
            }
            return null;
        }));

        return inflate;
    }

    public void updateList(List<CardItem> lci, List<MessageItem> lmi, boolean refreshAll) {
        if (v == null || ctx == null) {
            return;
        }
        if (recyclerView == null) {
            recyclerView = v.findViewById(R.id.frag_msg_list);
            recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
            adapter = new AllPostsRecyclerViewAdapter(this, lci);
            recyclerView.setAdapter(adapter);
        } else {
            requireActivity().runOnUiThread(() -> {
                adapter.refreshPlanView(lmi);
                adapter.updateItems(lci, refreshAll);
            });
        }
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
