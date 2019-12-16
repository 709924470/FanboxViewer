package cn.settile.fanboxviewer.TabFragments.MainTab;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import cn.settile.fanboxviewer.Adapters.Bean.CardItem;
import cn.settile.fanboxviewer.Adapters.RecyclerView.Main.SubscPostsRecyclerViewAdapter;
import cn.settile.fanboxviewer.Network.FanboxParser;
import cn.settile.fanboxviewer.R;


public class SubscPostFragment extends Fragment {

    private OnListFragmentInteractionListener mListener;

    private int lastVisibleItem;

    private View v;
    public Context c;
    private RecyclerView recyclerView;
    private SubscPostsRecyclerViewAdapter adapter;
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

        adapter = new SubscPostsRecyclerViewAdapter(this, new ArrayList<>(), mListener);
        recyclerView.setAdapter(adapter);

        srl = v.findViewById(R.id.frag_post_refresh);

        adapter.setOnBottomReachedListener(pos -> {
            if (srl.isRefreshing()) {
                return;
            }
            srl.setRefreshing(true);
            Executors.newSingleThreadExecutor().submit(() -> {
                List<CardItem> lci = FanboxParser.getSupportingPosts(false, c);
                getActivity().runOnUiThread(() -> srl.setRefreshing(false));
                if (lci != null) {
                    updateList(lci, false);
                }
                return null;
            });
        });

        srl.setOnRefreshListener(() -> Executors.newSingleThreadExecutor().submit(() -> {
            List<CardItem> lci = FanboxParser.getSupportingPosts(true, c);
            getActivity().runOnUiThread(() -> srl.setRefreshing(false));
            if (lci != null) {
                updateList(lci, true);
            }
            return null;
        }));

        return inflate;
    }

    public void updateList(List<CardItem> lci, boolean refreshAll) {
        if (v == null || c == null) {
            return;
        }
        if (recyclerView == null) {
            recyclerView = v.findViewById(R.id.frag_msg_list);
            recyclerView.setLayoutManager(new LinearLayoutManager(c));
            adapter = new SubscPostsRecyclerViewAdapter(this, lci, mListener);
            recyclerView.setAdapter(adapter);
        } else {
            getActivity().runOnUiThread(() -> {
                adapter.updateItems(lci, refreshAll);
            });
        }
    }

    // TODO: Rename method, updateList argument and hook method into UI event
    public void onButtonPressed(CardItem cardItem) {
        if (mListener != null) {
            mListener.onListFragmentInteraction(cardItem);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(CardItem cardItem);
    }
}
