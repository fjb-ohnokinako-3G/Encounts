package com.encount.photo.user

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import cn.pedant.SweetAlert.SweetAlertDialog
import com.bumptech.glide.Glide
import com.encount.photo.R
import com.encount.photo.SQLiteHelper
import com.encount.photo.UserDataClassList
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_user_profile_change.*
import okhttp3.*
import java.io.IOException



/**
 * やってること
 * 新規登録項目をサーバに送信し、サーバから新規登録成功(true)が帰ってきたらホーム画面に飛ばす
 *
 * 製作者：中村
 */

class ProfileChange : AppCompatActivity() {

    private val _helper = SQLiteHelper(this@ProfileChange)
    var name = ""
    var bio = ""
    private val RESULT_PICK_IMAGEFILE = 1000

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile_change)



        ivUserIcon.setOnClickListener{

            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(intent, RESULT_PICK_IMAGEFILE)
        }

        ivSave.setOnClickListener {

            name = etName.text.toString()
            bio = etBio.text.toString()

            profileChange()
        }

        ivBack.setOnClickListener {

            startActivity(Intent(this, UserProfile::class.java))
        }
    }

    override fun onResume() {
        super.onResume()

        UserDataGet()
    }


    private inner class profileChange : AsyncTask<String, String, String>() {

        override fun doInBackground(vararg params: String): String {

            var id     = ""
            val db     = _helper.writableDatabase
            val sql    = "select * from userInfo"
            val cursor = db.rawQuery(sql, null)

            while(cursor.moveToNext()){

                val idxId = cursor.getColumnIndex("user_id")
                id = cursor.getString(idxId)
            }

            val client = OkHttpClient()

            //アクセスするURL
            val url = "https://encount.cf/encount/UserProfileChenge.php"

            //Formを作成
            val formBuilder = FormBody.Builder()

            //formに要素を追加
            formBuilder.add("id", id)
            formBuilder.add("name",name)
            formBuilder.add("mail",bio)
            //リクエストの内容にformを追加
            val form = formBuilder.build()

            //リクエストを生成
            val request = Request.Builder().url(url).post(form).build()

            try {
                val response = client.newCall(request).execute()
                return response.body()!!.string()
            }
            catch (e: IOException) {
                e.printStackTrace()
                return "Error"
            }
        }

        override fun onPostExecute(result: String) {

            SweetAlertDialog(this@ProfileChange, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("プロフィール変更")
                .setContentText("変更しました！")
                .setConfirmText("プロフィールへ")
                .setConfirmClickListener {
                        sDialog -> sDialog.dismissWithAnimation()
                    goProfile()
                }
                .show()
        }
    }

    private inner class UserDataGet : AsyncTask<String, String, String>() {

        override fun doInBackground(vararg params: String): String {

            var id     = ""
            val db     = _helper.writableDatabase
            val sql    = "select * from userInfo"
            val cursor = db.rawQuery(sql, null)

            while(cursor.moveToNext()){

                val idxId = cursor.getColumnIndex("user_id")
                id = cursor.getString(idxId)
            }

            val client = OkHttpClient()

            //アクセスするURL
            val url = "https://encount.cf/encount/UserDataGet.php"

            //Formを作成
            val formBuilder = FormBody.Builder()

            //formに要素を追加
            formBuilder.add("id", id)
            //リクエストの内容にformを追加
            val form = formBuilder.build()

            //リクエストを生成
            val request = Request.Builder().url(url).post(form).build()

            try {
                val response = client.newCall(request).execute()
                return response.body()!!.string()
            } catch (e: IOException) {
                e.printStackTrace()
                return "Error"
            }
        }

        override fun onPostExecute(result: String) {

            try{

                val userData = Gson().fromJson(result, UserDataClassList::class.java)

                Glide.with(this@ProfileChange).load(userData.userIcon).into(ivUserIcon)
                etName.setText(userData.userName)
                etBio.setText(userData.userBio)
            }
            catch(e : Exception){
            }
        }
    }

    override fun onDestroy() {

        _helper.close()
        super.onDestroy()
    }

    fun goProfile(){

        startActivity(Intent(this, UserProfile::class.java))
    }

}