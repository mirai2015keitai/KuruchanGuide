package com.example.bot.map;
//ルート案内時にサーバから中間点をもらい経路表示のラスに飛ばす
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VolleyRoutes {
    List<LatLng> waypoints = new ArrayList<>();
//    public static int waycount=0;
    int counter=0;//responceのlatの数

    //Volleyによるサーバへの送信
    public void GET(List<LatLng> serverpoint, final Context context){
        final LatLng start = serverpoint.get(0);
        final LatLng end = serverpoint.get(1);
        Log.e("始点",start.toString());
        Log.e("終点",end.toString());
        //Volleyの宣言
        RequestQueue mQueue = Volley.newRequestQueue(context);

        waypoints.add(start);

        //行き先URL
        final String url = "http://mirai2015kuru.ddns.net/json_out.php?"
                +"st_lat="+String.valueOf(start.latitude)
                +"&st_lng="+String.valueOf(start.longitude)
                +"&en_lat="+String.valueOf(end.latitude)
                +"&en_lng="+String.valueOf(end.longitude);

        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("レスポンス", response);//ここのString型のresponseにサーバからのレスポンスが入る
                        Log.e("レスポンスの長さ",String.valueOf(response.length()));
                        counter = countStringInString(response,"lat");
                        Log.e("latの数",String.valueOf(counter));
                        if(counter!=0) {
                            for (int i = 0; i < counter; i++) {
                                //この正規表現であってるか検証の余地あり
                                //緯度
                                String p_lat = response.substring(response.indexOf("lat")
                                        + 6, response.indexOf("lng") - 3);
                                //経度
                                String p_lng = response.substring(response.indexOf("lng")
                                        + 6, response.indexOf("}") - 1);
                                Log.e("p_lat:", p_lat);
                                Log.e("p_lng:", p_lng);
                                LatLng tmp = new LatLng(Double.valueOf(p_lat), Double.valueOf(p_lng));
                                waypoints.add(tmp);
                            }
                        }

                        waypoints.add(end);
                        Log.e("waypointsの長さ",String.valueOf(waypoints.size()));

                        if(waypoints.size()>2){
                            for(int j=0;j<waypoints.size()-1;j++){
                                Log.e("出発地点(中間点あり)",String.valueOf(waypoints.get(j)));
                                Log.e("目的地(中間点あり)",String.valueOf(waypoints.get(j+1)));
                                String routes_url = MapsActivity.getDirectionsUrl(waypoints.get(j), waypoints.get(j+1));
                                RouteDownloadTask downloadTask = new RouteDownloadTask();

                                downloadTask.execute(routes_url);
                            }
                        }
                        else if(waypoints.size()==2){
                            Log.e("出発地点(中間点なし)",String.valueOf(waypoints.get(0)));
                            Log.e("目的地(中間点なし)",String.valueOf(waypoints.get(1)));
                            String routes_url = MapsActivity.getDirectionsUrl(waypoints.get(0), waypoints.get(1));
                            RouteDownloadTask downloadTask = new RouteDownloadTask();

                            downloadTask.execute(routes_url);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("エラーレスポンス", "error");
                    }
                });
        mQueue.add(request);
//        waypoints.add(end);
//        Log.e("waypointsの長さ：",String.valueOf(waypoints.size()));
//
//        if(waypoints.size()>2){
//            for(int j=0;j<waypoints.size();j++){
//                Log.e("出発地点：",String.valueOf(waypoints.get(j)));
//                Log.e("目的地：",String.valueOf(waypoints.get(j+1)));
//                String routes_url = MapsActivity.getDirectionsUrl(waypoints.get(j), waypoints.get(j+1));
//                RouteDownloadTask downloadTask = new RouteDownloadTask();
//
//                downloadTask.execute(routes_url);
//            }
//        }
//        else if(waypoints.size()==2){
//          　Log.e("出発地点(中間点なし)",String.valueOf(waypoints.get(0)));
//        　　Log.e("目的地(中間点なし)",String.valueOf(waypoints.get(1)));
//         　 String routes_url = MapsActivity.getDirectionsUrl(waypoints.get(0), waypoints.get(1));
//         　 RouteDownloadTask downloadTask = new RouteDownloadTask();
//
//        　　downloadTask.execute(routes_url);
//        }

        //確認のためのトースト
        Toast.makeText(context, "中間点を用いたルート", Toast.LENGTH_LONG).show();
    }

    // 文字の出現回数をカウント
    public int countStringInString(String target, String searchWord) {
        return (target.length() - target.replaceAll(searchWord, "").length()) / searchWord.length();
    }
}
