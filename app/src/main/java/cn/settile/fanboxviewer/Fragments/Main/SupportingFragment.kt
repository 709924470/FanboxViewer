package cn.settile.fanboxviewer.Fragments.Main

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.settile.fanboxviewer.R
import cn.settile.fanboxviewer.ViewModels.SupportingFViewModel

class SupportingFragment : Fragment() {

    companion object {
        fun newInstance() = SupportingFragment()
    }

    private lateinit var viewModel: SupportingFViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main_supporting, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SupportingFViewModel::class.java)
        // TODO: Use the ViewModel
    }

}