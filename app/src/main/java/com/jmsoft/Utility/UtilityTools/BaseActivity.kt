package com.jmsoft.basic.UtilityTools

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
      /*  if (Const.Development == Constants.Live) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }*/
        super.onCreate(savedInstanceState)
    }

/*

    open fun showProgressDialog(activity: Activity?, binding: ProgressDialogBinding) {
        Utils.changeStatusBarColor(activity!!, R.color.background)
        binding.lvMarker.progress = 0F
        binding.lvMarker.playAnimation()
        binding.root.visibility = View.VISIBLE
    }

    open fun stopProgressDialog(activity: Activity?, binding: ProgressDialogBinding) {
        binding.lvMarker.cancelAnimation()
        binding.root.visibility = View.GONE
    }

    open fun setError(context: Context, binding: NoInternetConnectionBinding,view: View) {
        binding.ivError.setImageResource(R.drawable.error)
        binding.tvError.text = getString(R.string.something_went_wrong)
        binding.tvDescription.text = getString(R.string.we_are_facing_some_issues_fetching_your_data_please_try_again_in_a_bit)
        Utils.applyAnimation(binding.root,context,R.anim.bottom_to_top)
        view.visibility = View.GONE
        binding.llGoback.setOnClickListener {
            finish()
        }
    }

    open fun setNoInternet(context: Context, binding: NoInternetConnectionBinding,view: View) {
        binding.ivError.setImageResource(R.drawable.no_internet)
        binding.tvError.text = getString(R.string.no_internet_connection)
        binding.tvDescription.text = getString(R.string.could_not_connect_to_internet_please_check_your_wifi_or_mobile_data_and_try_again)
        Utils.applyAnimation(binding.root,context,R.anim.bottom_to_top)
        view.visibility = View.GONE
        binding.llGoback.setOnClickListener {
            finish()
        }
    }
*/


}