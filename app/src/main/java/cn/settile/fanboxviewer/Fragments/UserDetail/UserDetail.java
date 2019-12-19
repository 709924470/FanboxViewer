package cn.settile.fanboxviewer.TabFragments.UserDetail;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import cn.settile.fanboxviewer.R;

public class UserDetail extends Fragment {

    private View v;
    private Context c;

    public UserDetail(){
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.component_user_detail, container, false);

        v = view;
        c = view.getContext();

        RecyclerView rv = v.findViewById(R.id.user_tab_detail);

        return view;
    }

    public static UserDetail newInstance(){
        return new UserDetail();
    }
}
