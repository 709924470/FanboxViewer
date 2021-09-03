package cn.settile.fanboxviewer.Fragments.Main;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import cn.settile.fanboxviewer.R;

public class NetworkErrorFragment extends Fragment {

    private View v;
    public Context c;
    public NetworkErrorFragment() {
    }

    public static NetworkErrorFragment newInstance() {
        NetworkErrorFragment fragment = new NetworkErrorFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_networkerror, container, false);
        return view;
    }

}
