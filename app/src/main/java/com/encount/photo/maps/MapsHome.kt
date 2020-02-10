package com.encount.photo.maps

import androidx.core.app.ActivityCompat
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Address
import android.location.Geocoder
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.encount.photo.MapPostData
import com.encount.photo.MapsDataClassList
import com.encount.photo.PostList
import com.encount.photo.R
import com.encount.photo.post.PostDetails
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator
import kotlinx.android.synthetic.main.fragment_maps_home.*
import kotlinx.android.synthetic.main.info_window_segment.*
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.lang.Exception

class MapsHome : Fragment(), OnMapReadyCallback {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var mMap: GoogleMap? = null
    private val requestingLocationUpdates = true //フラグ
    private val locationRequest: LocationRequest = LocationRequest.create()
    private var postList = mutableListOf<MapPostData>()
    //取得した写真の件数を格納する
    private var cnt = 0
    //マップ上に打つピンを管理するための変数
    private var mmm: Marker? = null
    //下のfor文内で使うカウント変数
    var ccnt = 0

    var pass = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_maps_home, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val mapFragment: SupportMapFragment =
            getChildFragmentManager().findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        ivSpotPost.setOnClickListener {

            //スポット詳細画面へ遷移
            val intent = Intent(context, SpotMainActivity::class.java)
            startActivity(intent)
        }

        /**
         * 権限を求める処理を、起動画面にまとめて追加する。
         * 　＋以前に許可されていても、その後に拒否される可能性も考える必要がある。
         */
        // Android 6, API 23以上でパーミッションの確認
        if (Build.VERSION.SDK_INT >= 23) {
            val permissions = arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            checkPermission(permissions, REQUEST_CODE)
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context!!)
        locationRequest.setInterval(10000)   //最遅の更新間隔
        locationRequest.setFastestInterval(5000)   //最速の更新間隔
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)           //バッテリー消費を抑えたい場合、精度は100m程度
        onResume()
    }

    //Update Result
    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {

            locationResult ?: return
            for (location in locationResult.locations) {

                if (location != null) {

                    latitude = location.latitude
                    longitude = location.longitude

                    ivSpotPost.visibility = View.VISIBLE

                    //グローバル変数に位置情報を代入

                    mMap!!.setMyLocationEnabled(true)

                    //mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(Segment.Kita.coordinate, 12.0f))

                    mMap!!.moveCamera(CameraUpdateFactory.newLatLng(LatLng(latitude, longitude)))

                    val camPos = CameraPosition.Builder()
                        .target(LatLng(latitude, longitude)) // Sets the new camera position
                        .zoom(19f) // Sets the zoom
                        .bearing(0f) // Rotate the camera
                        .tilt(40f) // Set the camera tilt
                        .build()
                    mMap!!.animateCamera(CameraUpdateFactory.newCameraPosition(camPos))

                    //座標から住所変換のテスト
                    val geocoder = Geocoder(context)
                    //val addressList: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
                    //val addressList: List<Address>? =
                        //geocoder.getFromLocation(latitude, longitude, 1)
                    //val adminArea = addressList?.first()!!.adminArea
                    //println(adminArea)

                    //MapPostGet(this,lat,lng).execute()で緯度経度を引数にして渡す
                    //MapPostGet(this@MapsHome).execute()
                    //サーバと通信する処理（インナークラス）を呼び出して実行する
                    SpotPhotoGet(this@MapsHome).execute()

                    Log.d("debug", "現在地の緯度" + location.latitude)
                    Log.d("debug", "現在地の経度" + location.longitude)

                    //写真が１件以上あれば、マップのピンを立てる処理を行う
                    if (cnt >= 1) {

                        ccnt = 0

                        Log.d("debug", "取得した写真の件数 : " + cnt)

                        //取得した写真の件数分ピンを打つ処理
                        //for(i in postList)にすると、初回の写真取得で数値がおかしくなるので、仕方なく変数を用意している。

                        for (i in 0..cnt - 1) {

                            //onMapReady(mMap!!)

                            /*
                            pass = postList[i].imagePath

                            ClusterManager<SegmentClusterItem>(activity, mMap!!).let {
                                it.renderer = MoreSegmentClusterRenderer(context!!, mMap!!, it)
                                mMap!!.setOnCameraIdleListener(it)
                                mMap!!.setOnMarkerClickListener(it)

                                /*Segment.values().forEach { segment ->
                                    it.addItem(SegmentClusterItem(segment))
                                }*/

                                postList.forEach {
                                    postList ->
                                    it.addItem(SegmentClusterItem(postList))
                                }

                                /*for (j in 0..cnt - 1) {
                                    it.addItem(SegmentClusterItem(postList[j]))
                                }*/
                            }

                            mMap!!.setInfoWindowAdapter(SegmentInfoWindowAdapter(context!!))*/


                            //前回マップ上に打ったピンを全て削除
                            if (mmm != null) {
                                mmm!!.remove()
                            }

                            val spot = LatLng(

                                postList[ccnt].imageLat.toDouble(),
                                postList[ccnt].imageLng.toDouble()
                            )

                            //val iconGenerator = IconGenerator(activity)
                            //val window = activity!!.layoutInflater.inflate(R.layout.info_window_segment, null)
                            //iconGenerator.setContentView(window)

                            Glide.with(activity)
                                .asBitmap()
                                .load(postList[ccnt].imagePath)
                                .into(object : SimpleTarget<Bitmap>(100, 100) {

                                    //正常に写真取得できればピンを打つ
                                    override fun onResourceReady(
                                        resource: Bitmap?,
                                        transition: Transition<in Bitmap>?
                                    ) {
                                        
                                        //imageView.setImageBitmap(resource)
                                        val iconGenerator = IconGenerator(activity)
                                        val imageView = ImageView(activity)
                                        imageView.setImageBitmap(resource)

                                        /*val window = activity!!.layoutInflater.inflate(R.layout.info_window_segment, null)
                                        iconGenerator.setContentView(window)*/
                                        iconGenerator.setContentView(imageView)

                                        mmm = mMap!!.addMarker(
                                            MarkerOptions()
                                                .position(spot)
                                                .title(postList[i].postId)
                                                .snippet(postList[i].userId)
                                                //.icon(BitmapDescriptorFactory.fromBitmap(resource))
                                                .icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon()))
                                        )
                                    }
                                })
                            ccnt++
                        }
                    }
                }
            }
        }
    }

    fun setPostList(postList: MutableList<MapPostData>) {
        this.postList = postList
    }

    override fun onResume() {
        super.onResume()
        if (requestingLocationUpdates) startLocationUpdates()
    }

    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback, null /* Looper */
        )
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    //default location
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        //マップのスタイルも変えられるようにしたい
        //mMap!!.setMapStyle(GoogleMap.MAP_TYPE_TERRAIN)

        //移動
        googleMap.uiSettings.isScrollGesturesEnabled = true
        //ズーム
        googleMap.uiSettings.isZoomGesturesEnabled = true
        //回転
        googleMap.uiSettings.isCompassEnabled = true
        //ティルト 2本指スワイプで視点を傾けることができる
        googleMap.uiSettings.isTiltGesturesEnabled = true
        //
        googleMap.uiSettings.isRotateGesturesEnabled = true

        /*mMap = googleMap.apply {
            moveCamera(CameraUpdateFactory.newLatLngZoom(Segment.Kita.coordinate, 12.0f))

            uiSettings.run {
                isRotateGesturesEnabled = false
                isTiltGesturesEnabled = false
            }
        }*/

        mMap!!.setOnMarkerClickListener { marker ->
            val intent = Intent(context, PostDetails::class.java)
            intent.putExtra("Post_Id",marker.title)
            intent.putExtra("imageLat",postList[ccnt-1].imageLat)
            intent.putExtra("imageLng",postList[ccnt-1].imageLng)
            intent.putExtra("Pre_Act", postList[ccnt-1].preAct)
            startActivity(intent)
            true
        }
    }

    //許可されていないパーミッションリクエスト
    fun checkPermission(permissions: Array<String>, request_code: Int) {
        ActivityCompat.requestPermissions(activity!!, permissions, request_code)
    }

    //結果
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PERMISSION) {
            for (i in permissions.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("Permission", "Added Permission: " + permissions[i])
                    //mMap!!.setMyLocationEnabled(true)
                } else {
                    // パーミッションが拒否された
                    Log.d("Permission", "Rejected Permission: " + permissions[i])
                }
            }
        }
    }

    companion object {

        private val REQUEST_CODE = 1000
        private val REQUEST_PERMISSION = 1000
    }

    /**
     * ここから下はサーバに現在地を表示し、現在地周辺の写真を取得する処理
     */

    private inner class SpotPhotoGet(val activity: MapsHome) : AsyncTask<String, String, String>() {

        override fun doInBackground(vararg params: String?): String {

            val client = OkHttpClient()

            //アクセスするURL
            val url = "https://encount.cf/encount/SpotInfoSendMap.php"

            //Formを作成
            val formBuilder = FormBody.Builder()

            println("経度" + latitude.toString())
            println("緯度" + longitude.toString())

            //Formに要素を追加
            formBuilder.add("latitude", latitude.toString())
            formBuilder.add("longitude", longitude.toString())

            //リクエスト内容にformを追加
            val form = formBuilder.build()

            //リクエストを生成
            val request = Request.Builder().url(url).post(form).build()

            try {
                val response = client.newCall(request).execute()
                println(url)
                return response.body()!!.string()
            } catch (e: IOException) {
                e.printStackTrace()
                return "Error"
            }
        }

        override fun onPostExecute(result: String) {
            try {
                var postList = mutableListOf<MapPostData>()
                val listType = object : TypeToken<List<MapPostData>>() {}.type
                val postData = Gson().fromJson<List<MapPostData>>(result, listType)
                var postCount = 0

                for (i in postData) {

                    postCount++

                    postList.add(
                        MapPostData(
                            i.imageId,
                            i.userId,
                            i.imagePath,
                            i.imageLat,
                            i.imageLng,
                            i.postId,
                            i.likeFlag,
                            "map"
                        )
                    )
                }

                cnt = postCount
                activity.setPostList(postList)

            } catch (e: Exception) {

            }
        }
    }

    /**
     * アイコンをカスタマイズするための処理
     */

/*
    /*private inner class SegmentClusterItem(postList: MutableList<PostList>) : ClusterItem {
        override fun getSnippet(): String {
            return postList[ccnt].userId
        }
        override fun getPosition(): LatLng {
            return LatLng(postList[ccnt].imageLat.toDouble(),postList[ccnt].imageLng.toDouble())
        }
        override fun getTitle(): String {
            return postList[ccnt].postId
        }
    }*/
    //ClusterItem を実装したクラスを作成
    private inner class SegmentClusterItem(/*val segment: Segment*/postList: MapPostData) : ClusterItem {
        //override fun getSnippet(): String = segment.flowerName

        //override fun getTitle(): String = segment.title

       // override fun getPosition(): LatLng = segment.coordinate

        override fun getSnippet(): String {
            if(ccnt >= 7 ){
                ccnt = ccnt - 1
            }
            return postList[ccnt].userId
            //return postList[1].userId
        }
        override fun getPosition(): LatLng {
            if(ccnt >= 7 ){
                ccnt = ccnt - 1
            }
            return LatLng(postList[ccnt].imageLat.toDouble(),postList[ccnt].imageLng.toDouble())
            //return LatLng(postList[1].imageLat.toDouble(),postList[1].imageLng.toDouble())
            //return LatLng(35.7707407, 140.0022931)
        }
        override fun getTitle(): String {
            if(ccnt >= 7 ){
                ccnt = ccnt - 1
            }
            return postList[ccnt].postId
           // return postList[1].postId
        }
    }

    private inner class MoreSegmentClusterRenderer(context: Context, map: GoogleMap, manager: ClusterManager<SegmentClusterItem>) :
        DefaultClusterRenderer<SegmentClusterItem>(context, map, manager) {
        private var itemImageView: ImageView
        private val itemIconGenerator: IconGenerator = IconGenerator(context).apply {
            val iconView = LayoutInflater.from(context).inflate(R.layout.icon_segment, null, false).apply {
                itemImageView = findViewById(R.id.imageIcon)
            }
            setContentView(iconView)
        }

        private val clusterImageView: ImageView
        private val clusterTextView: TextView
        private val clusterIconGenerator: IconGenerator = IconGenerator(context).apply {
            val clusterView = LayoutInflater.from(context).inflate(R.layout.icon_segment_cluster, null, false).apply {
                clusterImageView = findViewById(R.id.imageIcon)
                clusterTextView = findViewById(R.id.textNumber)
            }
            setContentView(clusterView)
        }

        override fun onBeforeClusterItemRendered(item: SegmentClusterItem, markerOptions: MarkerOptions) {

            if(ccnt >= 7 ){
                ccnt = ccnt - 1
            }

            //for (i in 0..cnt - 1) {
                Glide.with(activity)
                    .asBitmap()
                    //.load("https://encount.cf/files/postImg/1086470832_5dea7e52cc0b4.jpg")
                    .load(/*pass*/postList[ccnt].imagePath)
                    .into(object : SimpleTarget<Bitmap>(130, 130) {

                        //正常に写真取得できればピンを打つ
                        override fun onResourceReady(
                            resource: Bitmap?,
                            transition: Transition<in Bitmap>?
                        ) {
                            itemImageView.setImageBitmap(resource)
                            val icon = itemIconGenerator.makeIcon()
                            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon))
                        }
                    })
            //}


            //itemImageView.setImageResource(item.segment.imageResId)
            //val icon = itemIconGenerator.makeIcon()
            //markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon))
        }

        // クラスタ化したやつの生成(でかいやつ)
        override fun onBeforeClusterRendered(cluster: Cluster<SegmentClusterItem>, markerOptions: MarkerOptions) {
            /*clusterImageView.setImageResource(R.drawable.app_logo)
            clusterTextView.text = cluster.size.toString()
            val icon = clusterIconGenerator.makeIcon()
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon))*/
        }

        override fun onClusterItemRendered(item: SegmentClusterItem, marker: Marker) {
            marker.tag = /*item.segment*/postList
            super.onClusterItemRendered(item, marker)
        }

        override fun shouldRenderAsCluster(cluster: Cluster<SegmentClusterItem>?): Boolean {
            // ClusterItemが一定距離内にいくつ集まったらクラスタ化するかをBooleanで返す
            return cluster?.size ?: 0 >= 5
        }
    }

    private inner class SegmentInfoWindowAdapter(private val context: Context) : GoogleMap.InfoWindowAdapter {
        override fun getInfoContents(marker: Marker): View? = null

        override fun getInfoWindow(marker: Marker): View =
            LayoutInflater.from(context).inflate(R.layout.info_window_segment, null, false).apply {
                //val segment = marker.tag as Segment
                //val segment = marker.tag as

                //var segment = marker.tag
                //findViewById<ImageView>(R.id.imageView).setImageResource(segment.imageResId)
                //findViewById<TextView>(R.id.textTitle).text = segment.title
                //findViewById<TextView>(R.id.textFlowerName).text = segment.flowerName
            }
    }*/

}