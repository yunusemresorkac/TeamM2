package com.app.gamenews.controller

import android.app.Activity
import android.content.Context
import com.app.gamenews.util.Constants
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback


class AdsController(private val context : Context) {


    private  var mInterstitialAd: InterstitialAd? = null
    private  var mRewardedAd: RewardedAd? = null


    fun loadBanner(bannerView: AdView){
        MobileAds.initialize(context) { }
        val adRequest = AdRequest.Builder().build()
        bannerView.loadAd(adRequest)
    }

    fun loadRewarded() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, Constants.REWARDED_ID,
            adRequest, object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    super.onAdFailedToLoad(loadAdError)
                    println(loadAdError.message + " hata")
                }

                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    super.onAdLoaded(rewardedAd)
                    mRewardedAd = rewardedAd
                }
            })
    }

    fun showRewarded(onFinish : () -> Unit){
        if (mRewardedAd!=null){
            mRewardedAd!!.show(context as Activity){ item ->
                onFinish()
            }

        }

    }


    fun loadInters( onAdsClosed : () -> Unit) {
        MobileAds.initialize(
            context
        ) { }
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context,
            Constants.INTERS_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    mInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                    mInterstitialAd!!.fullScreenContentCallback = object :
                        FullScreenContentCallback() {
                        override fun onAdClicked() {
                            super.onAdClicked()
                            //Toast.makeText(getContext(), "tıklandı", Toast.LENGTH_SHORT).show();
                        }

                        override fun onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent()
                            onAdsClosed()

                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            super.onAdFailedToShowFullScreenContent(adError)

                        }

                        override fun onAdImpression() {
                            super.onAdImpression()
                        }

                        override fun onAdShowedFullScreenContent() {
                            super.onAdShowedFullScreenContent()
                        }
                    }
                }
            })
    }

    fun showInters() {
        if (mInterstitialAd != null) {
            mInterstitialAd!!.show(context as Activity)
        }
    }


}