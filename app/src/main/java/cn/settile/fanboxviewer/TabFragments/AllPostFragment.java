package cn.settile.fanboxviewer.TabFragments;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import cn.settile.fanboxviewer.Adapters.Bean.CardItem;
import cn.settile.fanboxviewer.Adapters.Bean.MessageItem;
import cn.settile.fanboxviewer.Adapters.RecyclerView.AllPostsRecyclerViewAdapter;
import cn.settile.fanboxviewer.Network.FanboxParser;
import cn.settile.fanboxviewer.R;
import lombok.extern.java.Log;


@Log
public class AllPostFragment extends Fragment {

    private OnListFragmentInteractionListener mListener;

    private int lastVisibleItem;

    private View v;
    public Context c;
    private RecyclerView recyclerView;
    private AllPostsRecyclerViewAdapter adapter;
    private SwipeRefreshLayout srl;
    private LinearLayout planView;
    private ViewGroup.LayoutParams ivParam;

    public AllPostFragment() {
    }

    public static AllPostFragment newInstance() {
        AllPostFragment fragment = new AllPostFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        android.view.View inflate = inflater.inflate(R.layout.fragment_post_list, container, false);

        v = inflate;
        c = inflate.getContext();

        planView = v.findViewById(R.id.frag_post_plans);
        ivParam = v.findViewById(R.id.frag_post_plan_icon).getLayoutParams();

        recyclerView = v.findViewById(R.id.frag_post_list);
        LinearLayoutManager llm = new LinearLayoutManager(c);
        recyclerView.setLayoutManager(llm);
        recyclerView.setNestedScrollingEnabled(false);

        adapter = new AllPostsRecyclerViewAdapter(this, new ArrayList<>(), mListener);
        recyclerView.setAdapter(adapter);

        srl = v.findViewById(R.id.frag_post_refresh);

        NestedScrollView nsv = v.findViewById(R.id.frag_post_scroll);
        nsv.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if(v.getChildAt(v.getChildCount() - 1) != null) {
                if ((scrollY >= (v.getChildAt(v.getChildCount() - 1).getMeasuredHeight() - v.getMeasuredHeight())) &&
                        scrollY > oldScrollY) {
                    if (srl.isRefreshing()) {
                        return;
                    }
                    srl.setRefreshing(true);
                    Executors.newSingleThreadExecutor().submit(() -> {
                        List<CardItem> lci = FanboxParser.getAllPosts(false, c);
                        List<MessageItem> lmi = FanboxParser.getPlans(false);
                        srl.setRefreshing(false);
                        if (lci != null) {
                            updateList(lci, lmi, false);
                        }
                        return null;
                    });
                }
            }
        });

//        adapter.setOnBottomReachedListener(pos -> {
//            if (srl.isRefreshing()) {
//                return;
//            }
//            srl.setRefreshing(true);
//            Executors.newSingleThreadExecutor().submit(() -> {
//                List<CardItem> lci = FanboxParser.getAllPosts(false, c);
//                List<MessageItem> lmi = FanboxParser.getPlans(false);
//                srl.setRefreshing(false);
//                if (lci != null) {
//                    updateList(lci, lmi, false);
//                }
//                return null;
//            });
//        });

        srl.setOnRefreshListener(() -> Executors.newSingleThreadExecutor().submit(() -> {
            List<CardItem> lci = FanboxParser.getAllPosts(true, c);
            List<MessageItem> lmi = FanboxParser.getPlans(false);
            srl.setRefreshing(false);
            if (lci != null) {
                updateList(lci, lmi, true);
            }
            return null;
        }));

        return inflate;
    }

    public void updateList(List<CardItem> lci, List<MessageItem> lmi, boolean refreshAll) {
        if (v == null || c == null) {
            return;
        }
        if (recyclerView == null) {
            recyclerView = v.findViewById(R.id.frag_msg_list);
            recyclerView.setLayoutManager(new LinearLayoutManager(c));
            adapter = new AllPostsRecyclerViewAdapter(this, lci, mListener);
            recyclerView.setAdapter(adapter);
        } else {
            if (planView == null) {
                planView = v.findViewById(R.id.frag_post_plans);
            } else {
                planView.setVisibility(View.VISIBLE);
                getActivity().runOnUiThread(() -> {
                    if (lmi == null || lmi.size() == 0) {
                        planView.findViewById(R.id.frag_post_icon_nothing).setVisibility(View.VISIBLE);
                    } else {
                        planView.removeAllViewsInLayout();
                        for (MessageItem messageItem : lmi) {
                            RoundedImageView icon = new RoundedImageView(c);
                            icon.setLayoutParams(ivParam);
                            icon.setVisibility(View.VISIBLE);
                            icon.setCornerRadius(128.0f);
                            icon.setBorderColor(ColorStateList.valueOf(0xCCCCCC));
                            icon.setBorderWidth(2.0f);

                            planView.addView(icon);

                            Picasso.get()
                                    .load(messageItem.getIconUrl())
                                    .placeholder(R.drawable.load_24dp)
                                    .into(icon);
                        }
                        getActivity().findViewById(R.id.frag_post_plan_view).invalidate();
                    }
                });
            }
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
