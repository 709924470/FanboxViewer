package cn.settile.fanboxviewer.Fragments.UserDetail;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import cn.settile.fanboxviewer.Adapters.RecyclerView.UserDetail.UserDetailRecyclerViewAdapter;
import cn.settile.fanboxviewer.R;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserDetailFragment extends Fragment {

    private View v;
    private Context c;

    RecyclerView rv = null;
    public UserDetailRecyclerViewAdapter uda = null;


    public UserDetailFragment(){
    }

    public int getFragID(){
        return 0xbe;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        c = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.component_user_detail, container, false);

        v = view;

        rv = v.findViewById(R.id.com_detail_list);
        LinearLayoutManager llm = new LinearLayoutManager(c);
        rv.setLayoutManager(llm);

        uda = new UserDetailRecyclerViewAdapter();
        rv.setAdapter(uda);

        return view;
    }

    public static UserDetailFragment newInstance(){
        return new UserDetailFragment();
    }
}
