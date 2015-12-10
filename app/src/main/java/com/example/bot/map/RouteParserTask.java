package com.example.bot.map;
//ルートに経路表示
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/*parse the Google Places in JSON format */
//Jsonをリスト化して、別クラスに飛ばす
public class RouteParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> > {
    MapsActivity ma;
    ArrayList<LatLng> points = null;

    @Override
    protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

        JSONObject jObject;
        List<List<HashMap<String, String>>> routes = null;

        try{
            jObject = new JSONObject(jsonData[0]);
            parseJsonpOfDirectionAPI parser = new parseJsonpOfDirectionAPI();

            routes = parser.parse(jObject);
            //Log.e("routes",posinfo);
        }catch(Exception e){

            Log.d("error2", "??????????????????????????????????");
            e.printStackTrace();
        }
        return routes;
    }

    //ルート検索で得た座標を使って経路表示
    @Override
    protected void onPostExecute(List<List<HashMap<String, String>>> result) {
        PolylineOptions lineOptions = null;
        MarkerOptions markerOptions = new MarkerOptions();

        if(result.size() != 0){
            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = result.get(i);

                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);//これにルート判定の中間点が全て入ってると思う
                }
                //ポリライン
                lineOptions.addAll(points);
                lineOptions.width(12);
                lineOptions.color(0x550000ff);
            }

            //描画
            ma.mMap.addPolyline(lineOptions);
            ma.markersplace = null;
            //Toast.makeText(this,points.toString(),Toast.LENGTH_LONG).show();//ルートの緯度経度
        }else{
            ma.mMap.clear();
            Toast.makeText(ma.context,"ルート情報を取得できませんでした", Toast.LENGTH_LONG).show();
        }
        ma.progressDialog.hide();
    }
}