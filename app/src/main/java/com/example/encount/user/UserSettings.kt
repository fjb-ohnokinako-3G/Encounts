package com.example.encount.user

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.encount.R
import com.example.encount.SQLiteHelper
import kotlinx.android.synthetic.main.activity_user_settings.*
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

/**
 * やってること
 * 設定変更
 *
 * 製作者：中村
 */

class UserSettings : AppCompatActivity() {

    private val _helper = SQLiteHelper(this@UserSettings)

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_settings)

        logoutbtn.setOnClickListener {

            val db = _helper.writableDatabase
            val sqlDelete = "delete from userInfo"
            var stmt = db.compileStatement(sqlDelete)
            stmt.executeUpdateDelete()

            val intent = Intent(this, UserLogin::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        //アカウント削除
        acdel.setOnClickListener {
            //削除処理
            AcountDel().execute()
            /*
            //ログアウト
            val db = _helper.writableDatabase
            val sqlDelete = "delete from userInfo"
            var stmt = db.compileStatement(sqlDelete)
            stmt.executeUpdateDelete()
            val intent = Intent(this, UserLogin::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            */
        }

    }

    override fun onDestroy(){

        //ヘルパーオブジェクトの開放
        _helper.close()
        super.onDestroy()
    }

    //アカウント削除処理（DB）
    private inner class AcountDel() : AsyncTask<String, String, String>() {
        override fun doInBackground(vararg params: String): String {

            var userId = ""
            val db = _helper.writableDatabase
            val sql = "select * from userInfo"
            val cursor = db.rawQuery(sql, null)
            while(cursor.moveToNext()){
                val idxId = cursor.getColumnIndex("user_id")
                userId = cursor.getString(idxId)
            }
            val client = OkHttpClient()

            //アクセスするURL
            val url = "https://encount.cf/encount/UserDataDel.php"

            //Formを作成
            val formBuilder = FormBody.Builder()

            //formに要素を追加
            formBuilder.add("user", userId)
            //リクエストの内容にformを追加
            val form = formBuilder.build()
            Log.d("debug", "quwa" + userId)
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
            Log.d("debug","実行しました")

        }

    }
}
