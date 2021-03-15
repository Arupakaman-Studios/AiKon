package com.arupakaman.aikon.uiModules.home

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter


class AdapterFragmentState(mActivity: AppCompatActivity) :
        FragmentStateAdapter(mActivity) {

    private val mFragmentList = ArrayList<Fragment>()
    private val mFragmentTitleList = ArrayList<String>()
    //private val pageIds = mFragmentList.map { it.hashCode().toLong() }

    override fun getItemCount(): Int = mFragmentList.size

    override fun createFragment(position: Int): Fragment {
        return mFragmentList[position]
    }

    fun getItemTitle(position: Int) = mFragmentTitleList[position]

    fun removeFragment(position: Int) {
        mFragmentList.removeAt(position)
        mFragmentTitleList.removeAt(position)
        // notifyItemRangeChanged(position, mFragmentList.size)
        notifyDataSetChanged()
    }

    fun addFragment(fragment: Fragment, title: String) {
        mFragmentList.add(fragment)
        mFragmentTitleList.add(title)
        notifyDataSetChanged()
    }

   /* override fun getItemId(position: Int): Long {
        return mFragmentList[position].hashCode().toLong() // make sure notifyDataSetChanged() works
    }

    override fun containsItem(itemId: Long): Boolean {
        return pageIds.contains(itemId)
    }*/

}