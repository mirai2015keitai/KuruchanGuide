package com.example.bot.map;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VolleyCommunication{
    //Volleyによるサーバへの送信
    public void POST(List<LatLng> serverpoint, final Context context){
        final LatLng start = serverpoint.get(0);
        final LatLng end = serverpoint.get(1);
        //Volleyの宣言
        RequestQueue mQueue = Volley.newRequestQueue(context);

        //行き先URL
        final String url = "http://mirai2015kuru.ddns.net/json_in.php";

        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("レスポンス", response);//ここのString型のresponseにサーバからのレスポンスが入る
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("エラーレスポンス", "error");
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //サーバに送る値を決める
                Map<String, String> params = new HashMap<String, String>();
                //(値の種類,値)
                params.put("st_lat", String.valueOf(start.latitude));      //始点緯度
                params.put("st_lng", String.valueOf(start.longitude));     //始点経度
                params.put("en_lat", String.valueOf(end.latitude));        //終点緯度
                params.put("en_lng", String.valueOf(end.longitude));       //終点経度
                params.put("no_dump","0");
                params.put("high_dump","999");
                return params;
            }
        };
        mQueue.add(request);

        //確認のためのトースト
        Toast.makeText(context,"通行不可な場所送信完了", Toast.LENGTH_LONG).show();
    }
}
