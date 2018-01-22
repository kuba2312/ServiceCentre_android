package com.example.kuba.servicecentre

import android.hardware.camera2.CaptureFailure
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_login.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class LoginActivity : AppCompatActivity() {
    val client = OkHttpClient()
    val FORM = MediaType.parse("application/x-www-form-urlencoded")

    fun httpPost(url: String, body: RequestBody, success: (response: Response)-> Unit, failure: () -> Unit){
        val request = Request.Builder()
                .url(url)
                .post(body)
                .addHeader ("Accept", "application/json")
                .build()
        client.newCall(request).enqueue(object : Callback{
            override fun onFailure(call: Call?, e: IOException?) {
                failure()
            }

            override fun onResponse(call: Call, response: Response) {
                success(response)
            }

        })
    }

    fun login(login: String, password: String) {
        Toast.makeText(this, "Logowanie. (" + login +  ")", Toast.LENGTH_SHORT).show()
        val url = "http://192.168.0.2:3000/login"
        val body = RequestBody.create(FORM, "session[nickname]=" + login + "&session[password]=" + password)

        httpPost(url, body,
                fun (response: Response) {
                    Log.v("INFO", "Succeeded")
                    val response_string = response.body()?.string()
                    val json = JSONObject(response_string)
                    if (json.has("message")){
                        this.runOnUiThread {
                            Toast.makeText(this, json ["message"] as String, Toast.LENGTH_SHORT).show()
                        }
                    }
                    else if (json.has("token")){
                        this.runOnUiThread() {
                           // Toast.makeText(this, json["token"] as String, Toast.LENGTH_SHORT).show()
                            val intent = android.content.Intent(this, HomeActivity::class.java)
                            startActivity(intent)
                        }
                    }
                },
                fun (){
                    Log.v("INFO", "Failed")
                })

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if (login_field.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
        login_button.setOnClickListener {
            val login = login_field.text.toString()
            val password = password_field.text.toString()
            login(login, password)
        }
    }
}
