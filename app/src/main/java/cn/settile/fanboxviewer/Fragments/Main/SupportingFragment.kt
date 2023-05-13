package cn.settile.fanboxviewer.Fragments.Main

import android.net.wifi.p2p.WifiP2pManager.ServiceResponseListener
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.settile.fanboxviewer.Adapters.RecyclerView.Supporting.SupportingRecycleViewAdapter
import cn.settile.fanboxviewer.R
import cn.settile.fanboxviewer.ViewModels.SupportingFViewModel

class SupportingFragment : Fragment() {

    companion object {
        fun newInstance() = SupportingFragment()
    }

    private lateinit var viewModel: SupportingFViewModel

    lateinit var recycleView : androidx.recyclerview.widget.RecyclerView

    lateinit var adapter: SupportingRecycleViewAdapter

    lateinit var srl: androidx.swiperefreshlayout.widget.SwipeRefreshLayout
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProvider(this).get(SupportingFViewModel::class.java)
        val v=inflater.inflate(R.layout.fragment_main_supporting, container, false)


        recycleView = v.findViewById(R.id.frag_supporting_recycler_view)

        srl=v.findViewById(R.id.frag_supporting_swipe_refresh_layout)

        srl.setOnRefreshListener { viewModel.refresh() }

        viewModel.supportingList.observe(viewLifecycleOwner) {
            adapter = SupportingRecycleViewAdapter(it)
            recycleView.adapter = adapter
            recycleView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) {
            srl.isRefreshing = it
        }


        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


    }

}