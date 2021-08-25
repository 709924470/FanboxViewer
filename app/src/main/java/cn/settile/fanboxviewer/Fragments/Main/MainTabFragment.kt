package cn.settile.fanboxviewer.Fragments.Main

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import cn.settile.fanboxviewer.Adapters.Fragment.MainFragmentAdapter
import cn.settile.fanboxviewer.MainActivity
import cn.settile.fanboxviewer.Network.Common
import cn.settile.fanboxviewer.Network.RESTfulClient.FanboxParser
import cn.settile.fanboxviewer.Network.URLRequestor
import cn.settile.fanboxviewer.Network.URLRequestor.OnResponseListener
import cn.settile.fanboxviewer.R
import cn.settile.fanboxviewer.Util.Constants
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import okhttp3.Response
import org.json.JSONObject
import org.jsoup.Jsoup
import java.util.*

class MainTabFragment : Fragment(R.layout.fragment_main_tabs) {
    lateinit var allPostFragment: AllPostFragment
    private lateinit var tabPageAdapter: MainFragmentAdapter
    private lateinit var tl: TabLayout

    lateinit var subscPostFragment: SubscPostFragment
    lateinit var messageFragment: MessageFragment

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val mVp: ViewPager =
            requireActivity().findViewById(R.id.main_tab_pager) // inflating the main page

        mVp.isSaveEnabled = true
        mVp.offscreenPageLimit = 2


        //navigationView.getMenu().getItem(0).setChecked(true);

        //TODO: IMAGE Editing for club card.
        tl = requireActivity().findViewById<View>(R.id.main_page_tab) as TabLayout

        allPostFragment = AllPostFragment.newInstance()
        subscPostFragment = SubscPostFragment.newInstance()
        messageFragment = MessageFragment.newInstance()

        tabPageAdapter =
            MainFragmentAdapter(requireActivity().getSupportFragmentManager(), requireContext())

        tabPageAdapter.apply {
            addFragment(allPostFragment, resources.getString(R.string.tab_posts))
            addFragment(subscPostFragment, resources.getString(R.string.tab_subscribed))
            addFragment(messageFragment, resources.getString(R.string.tab_messages))
        }


        mVp.adapter = tabPageAdapter
        tl.setupWithViewPager(mVp)

        var mMainActivity = requireActivity() as MainActivity;
        if (!mMainActivity.getIntent().getBooleanExtra("NO_NETWORK", false)) {
            if (mMainActivity.getIntent()
                    .getBooleanExtra("IS_LOGGED_IN", false)
            ) {
                mMainActivity.viewModel.update_is_logged_in(true)
                fetchUserInfo()
                initTab()
            } else {
                mMainActivity.viewModel.update_is_logged_in(false)
            }
            mMainActivity.viewModel.update_is_online(true)
        } else {
            mMainActivity.viewModel.update_is_online(false)
        }

        tl.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                mVp.setCurrentItem(tab.position, true)
                if (tab.position == 2 && !MainActivity.flag) {
                    messageFragment.update(true)
                    MainActivity.flag = !MainActivity.flag
                    tab.removeBadge()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun getNotifications(mf: MessageFragment) {
        mf.update(true)
    }

    fun initTab() {
        getNotifications(messageFragment)
        allPostFragment.updateList(
            FanboxParser.getAllPosts(false, requireContext()),
            FanboxParser.getPlans(),
            true
        )
        subscPostFragment.updateList(
            FanboxParser.getSupportingPosts(false, requireContext()),
            true
        )

    }

    private fun fetchUserInfo() {
        URLRequestor(Constants.Domain, OnResponseListener<Boolean> { it: Response ->
            try {
                val document = Jsoup.parse(Objects.requireNonNull(it.body)!!.string())
                val metadata = document.getElementById("metadata")
                //TODO (fix this parser) : bug
                val jsonStr = metadata.attr("content")
                Common.userInfo = JSONObject(jsonStr)
                val user = Common.userInfo.getJSONObject("context").getJSONObject("user")
                val iconUrl = user.getString("iconUrl")
                val userName = user.getString("name")
                val userId = user.getString("userId")
                val unread = FanboxParser.getUnreadMessagesCount()
                requireActivity().runOnUiThread {
                    (requireActivity() as MainActivity).viewModel.update_user_info(
                        userName,
                        userId,
                        iconUrl
                    )
                    if (unread != 0) {
                        Objects.requireNonNull(tl.getTabAt(2))
                            ?.getOrCreateBadge()?.number = unread
                    }
                }
            } catch (ex: Exception) {
                requireActivity().runOnUiThread {
                    Toast.makeText(
                        (requireContext() as MainActivity).getBaseContext(), """
     Can't get user info.
     ${ex.message}
     """.trimIndent(), Toast.LENGTH_LONG
                    ).show()
                }
                Log.e("MainActivity", "fetchUserInfo: ", ex)
            }
            null
        }, null)

    }
}