package cn.settile.fanboxviewer.Fragments.MainTab;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.List;
import java.util.concurrent.Executors;

import cn.settile.fanboxviewer.Adapters.Bean.MessageItem;
import cn.settile.fanboxviewer.Adapters.RecyclerView.Main.MessageRecyclerViewAdapter;
import cn.settile.fanboxviewer.Network.FanboxParser;
import cn.settile.fanboxviewer.R;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the
 * interface.
 */
public class MessageFragment extends Fragment {

    private SwipeRefreshLayout srl;

    private int lastVisibleItem;
    private List<MessageItem> lmi;
    private View v;
    public Context c;
    private RecyclerView recyclerView = null;
    private MessageRecyclerViewAdapter msgAdapter;

    public MessageFragment() {
    }

    public static MessageFragment newInstance() {
        MessageFragment fragment = new MessageFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_msg_list, container, false);

        v = view;
        c = view.getContext();

        recyclerView = v.findViewById(R.id.frag_msg_list);
        LinearLayoutManager llm = new LinearLayoutManager(c);
        recyclerView.setLayoutManager(llm);
        msgAdapter = new MessageRecyclerViewAdapter(this, lmi);
        recyclerView.setAdapter(msgAdapter);

        srl = v.findViewById(R.id.frag_msg_refresh);

        //noinspection deprecation
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 == msgAdapter.getItemCount()) {
                    if (srl.isRefreshing()) {
                        return;
                    }
                    srl.setRefreshing(true);
                    Executors.newSingleThreadExecutor().submit(() -> {
                        List<MessageItem> lmi = FanboxParser.getMessages(false);
                        srl.setRefreshing(false);
                        if (lmi != null) {
                            update(lmi, false);
                        }
                        return null;
                    });
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastVisibleItem = llm.findLastVisibleItemPosition();
            }
        });

        srl.setOnRefreshListener(() -> Executors.newSingleThreadExecutor().submit(() -> {
            List<MessageItem> lmi = FanboxParser.getMessages(true);
            srl.setRefreshing(false);
            if (lmi != null) {
                update(lmi, true);
            }
            return null;
        }));

        return view;
    }

    public void update(List<MessageItem> lmi, boolean refreshAll) {
        if (v == null || c == null) {
            return;
        }
        if (recyclerView == null) {
            recyclerView = v.findViewById(R.id.frag_msg_list);
            recyclerView.setLayoutManager(new LinearLayoutManager(c));
            msgAdapter = new MessageRecyclerViewAdapter(this, lmi);
            recyclerView.setAdapter(msgAdapter);
            getActivity().runOnUiThread(() -> {
                msgAdapter.updateItems(lmi, refreshAll);
            });
        } else {
            getActivity().runOnUiThread(() -> {
                msgAdapter.updateItems(lmi, refreshAll);
            });
        }
        new Handler().postDelayed(() -> {
            srl.setRefreshing(false);
        }, 200);
    }
}
