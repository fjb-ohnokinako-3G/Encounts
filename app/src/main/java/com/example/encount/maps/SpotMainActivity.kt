package com.example.encount.maps

import android.location.Geocoder
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.encount.*
import com.example.encount.user.TabAdapter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_spot_home.*
import kotlinx.android.synthetic.main.activity_spot_home.tabLayout
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.lang.Exception

var latitude = 35.704292
var longitude = 139.984092

class SpotMainActivity : AppCompatActivity() {

    var postId = ""

    private val _helper = SQLiteHelper(this@SpotMainActivity)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spot_home)

        Log.d("a", latitude.toString())

        SpotDataList.adapter = SpotAdapter(supportFragmentManager, this)
        //TabLayoutにViewPagerを設定
        tabLayout.setupWithViewPager(SpotDataList)

        SpotCode.setText(getAddress(latitude, longitude))
    }

    //位置情報を住所に変換する関数
    private fun getAddress(lat: Double, lng: Double): String {
        val geocoder = Geocoder(this)
        val list = geocoder.getFromLocation(lat, lng, 1)
        //println("0" + list[0].getAddressLine(0))
        var result = list[0].getAdminArea() + list[0].getLocality() + list[0].getThoroughfare() +list[0].getSubThoroughfare()
        return result
    }

    override fun onDestroy(){

        //ヘルパーオブジェクトの開放
        _helper.close()
        super.onDestroy()
    }
}