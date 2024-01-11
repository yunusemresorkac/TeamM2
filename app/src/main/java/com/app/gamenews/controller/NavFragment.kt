package com.app.gamenews.controller

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

class NavFragment {

    companion object{
        fun openNewFragment(fragment: Fragment, activity: FragmentActivity, view : Int) {
            val fragmentTransaction = activity.supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(view, fragment)
            fragmentTransaction.commit()
        }
    }




}