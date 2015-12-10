package com.example.bot.map;
//main
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.location.Location;
import com.google.android.gms.maps.model.CameraPosition;

import android.view.View;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;

import com.google.android.gms.maps.UiSettings;

import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.lang.String;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,LocationListener,TextToSpeech.OnInitListener {

    int MyplaceChecker = 0;
    int change_picture_count = 0;//航空写真用
    int btn_delete =0;//動的ボタン削除用

    //動的ボタン生成で使用
    TextView tv;//ルート経路関連
    int info_id;//ルート検索時のボタンのID
    FrameLayout routes_fl;//ルート検索時レイアウト保存
    Button info_btn;//ルート情報ボタン生成
    int speech_id;//音声案内時
    Button speech_btn;//音声案内時
    final private Float SPEECH_SLOW = 0.5f;
    final private Float SPEECH_NORMAL = 1.0f;
    final private Float SPEECH_FAST = 1.5f;
    final private Float PITCH_LOW = 0.5f;
    final private Float PITCH_NORMAL = 1.0f;
    final private Float PITCH_HIGH = 1.5f;

    //動的ボタン生成で使用
    int set_id;//サーバ通信開始ボタンID
    RadioButton set_btn;//サーバ通信開始ボタン生成
    FrameLayout server_fl;//サーバ通信ボタンレイアウト
    int com_id;//サーバ通信終了ボタンID
    Button com_btn;//サーバ通信終了ボタン生成

    int servercount=0;//サーバ通信で使用

    int get_list_position=0;//生成したListのタッチした番号を保存

    //糞汚いけど勘弁
    //カテゴリで検索した緯度経度
    static List<HashMap<String,Double>> list_location = new ArrayList<HashMap<String,Double>>();
    static int backcounter = 0;

    //ArrayListの初期化,リストビューに表示するためのデータを設定
    static List<String> dataList = java.util.Arrays.asList("病院","コンビニ","郵便局","銀行","スーパーマーケット",
            "図書館","公園");
    static List<String> typeList = java.util.Arrays.asList("hospital","convenience_store","post_office",
            "bank","grocery_or_supermarke","library","park");
    //dataListからListView1に変換するのに使用
    static ArrayAdapter<String> adapter;
    //listを生成するときのレイアウト保存
    LinearLayout list_layout;
    //listを生成するときに使用
    ListView listView1;
    //list生成の確認に使用
    int generationlist=0;

    String type_url = "";//listで選択したタイプ

    private InputMethodManager inputMethodManager;
    private FrameLayout mainLayout;

    //server通信で送る再使用
    public int serverpoint_count=0;

    Location testLocation;

    public static LatLng markersplace=null;//ルート検索での目的地

    private List<Marker> mMarkerList = new ArrayList<Marker>();
    private List<Marker> mMarkerList2 = new ArrayList<Marker>();//setUpMap3で使用
    private List<Marker> mMarkerList3 = new ArrayList<Marker>();//setUpMap4で使用

    private List<LatLng> serverPointsList = new ArrayList<LatLng>();

    public static String posinfo = "";//ルート情報のすべてが入ってる
    public static String info_A = "";//ルートの出発位置情報
    public static String info_B = "";//ルートの到着位置情報

    //場所を説明するラベル
    public final Activity context = this;

    static SQLiteDatabase mydb;//サーバ通信の際使用
    private SimpleCursorAdapter myadapter;//サーバ通信の際使用

    private FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;

    public static GoogleMap mMap = null; // Might be null if Google Play services APK is not available.

    private GoogleApiClient mLocationClient = null;
    private static final LocationRequest REQUEST = LocationRequest.create()
            .setInterval(5000)//=5s ミリ秒単位で位置情報更新設定
            .setFastestInterval(16)//=60fps　ミリ秒単位で位置情報更新の正確な間隔設定
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);//位置情報取得要求の優先順位設定,できるかぎり正確な位置

    //public static MarkerOptions options;
    public static ProgressDialog progressDialog;
    public static String travelMode = "walking";//徒歩モード

    double search_lat; //検索時に使用する緯度
    double search_lng;//検索時に使用する経度

    TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //起動時にキーボード出さない
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();


        try{
            // TextToSpeechオブジェクトの生成
            tts = new TextToSpeech(this, (TextToSpeech.OnInitListener) this);
        }catch (Exception e){
            Log.e("tts実行エラー",e.toString());
        }

        // ボタンを設定追加
        Button button = (Button) findViewById(R.id.genzaiti);
        //button.setOnClickListener(this);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //do something
                CameraPosition cameraPos = new CameraPosition.Builder()
                        .target(new LatLng(testLocation.getLatitude(), testLocation.getLongitude())).zoom(17.0f)
                        .bearing(0).build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPos));
                // MyLocationレイヤーを有効に
                mMap.setMyLocationEnabled(true);
                // MyLocationButtonを有効に
                UiSettings settings = mMap.getUiSettings();
                settings.setMyLocationButtonEnabled(true);
            }
        });

        // ボタンを設定追加
        Button button3 = (Button) findViewById(R.id.syasin);
        //button.setOnClickListener(this);
        button3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //do something
                if (change_picture_count == 0) {
                    // 航空写真に変更
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    change_picture_count = 1;
                } else if (change_picture_count == 1) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    change_picture_count = 0;
                }
            }
        });
        //ボタンを押したらルート案内
        Button button2 = (Button) findViewById(R.id.annai);
        //button.setOnClickListener(this);
        button2.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                //do something
                if(btn_delete==1) {
                    outitButton();//動的ボタンの削除
                }
                if(mMarkerList3.size()==0){
                    if(servercount==1) {
                        outitButton2();//動的ボタンの削除
                    }
                }
                if (mMarkerList.size() >= 1) {
                    initButton();
                    //routeSearch();
                    progressDialog.show();
                    LatLng ori = new LatLng(testLocation.getLatitude(), testLocation.getLongitude());//現在地
                    LatLng des = new LatLng(markersplace.latitude, markersplace.longitude);//マーカー(タップ)位置
                    List<LatLng> vr_pos=new ArrayList<LatLng>();
                    vr_pos.add(ori);
                    vr_pos.add(des);
                    VolleyRoutes vr = new VolleyRoutes();
                    vr.GET(vr_pos, getApplicationContext());
                }
                if (mMarkerList2.size() >= 1) {
                    if(markersplace!=null) {
                        //routeSearch();
                        initButton();
                        progressDialog.show();
                        LatLng ori = new LatLng(testLocation.getLatitude(), testLocation.getLongitude());//現在地
                        LatLng des = new LatLng(markersplace.latitude, markersplace.longitude);//マーカー(タップ)位置
                        List<LatLng> vr_pos=new ArrayList<LatLng>();
                        vr_pos.add(ori);
                        vr_pos.add(des);
                        VolleyRoutes vr = new VolleyRoutes();
                        vr.GET(vr_pos, getApplicationContext());
                    }
                }
            }
        });

        //付け足し
        mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        if (mMap != null) {
            mMap.setMyLocationEnabled(true);
        }
        mLocationClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        if (mLocationClient != null) {
            mLocationClient.connect();
        }

        mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        if (mMap != null) {
            // タップ時のイベントハンドラ登録
            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng point) {
                    // TODO Auto-generated method stub
                    //キーボードを消す
                    inputMethodManager.hideSoftInputFromWindow(mainLayout.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    Toast.makeText(getApplicationContext(), "タップ位置\n緯度：" + point.latitude + "\n経度:" + point.longitude, Toast.LENGTH_LONG).show();
                    //マーカーがあればタップで消す
                    if (mMarkerList.size() >= 1) {
//                        mMarkerList.get(0).remove();//Mapのマーカー削除
                        mMarkerList.remove(0);//mMarkerList[0]の要素を削除
                        mMap.clear();//ルート？ていうより全て削除
                    }
                    if(mMarkerList2.size()>=1) {
                        mMarkerList2.clear();//mMarkerList2の要素を一括削除
                        mMap.clear();
                    }
                    if(mMarkerList3.size()>=1){
                        mMarkerList3.clear();
                        serverpoint_count=0;
                        mMap.clear();
                    }
                    if(btn_delete==1) {
                        outitButton();//動的ボタンの削除
                    }
                    if(servercount==1) {
                        outitButton2();//動的ボタンの削除
                    }
                    if(generationlist==1) {
                        deleteList();
                    }
                }
            });

            // 長押し時のイベントハンドラ登録
            mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng point) {
                    // TODO Auto-generated method stub
                    //キーボードを消す
                    inputMethodManager.hideSoftInputFromWindow(mainLayout.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    if (mMarkerList.size() >= 1) {
                        mMarkerList.remove(0);//mMarkerList[0]の要素を削除
                        mMap.clear();
                    }
                    if(mMarkerList2.size()>=1) {
                        mMarkerList2.clear();//mMarkerList2の要素を一括削除
                        mMap.clear();
                    }
                    if(btn_delete==1) {
                        outitButton();
                    }
                    if(generationlist==1) {
                        deleteList();
                    }
                    Toast.makeText(getApplicationContext(), "長押し位置\n緯度：" + point.latitude + "\n経度:" + point.longitude, Toast.LENGTH_LONG).show();
                    if(serverpoint_count==0) {
                        setUpMap2(point.latitude, point.longitude);
                    }
                    if(serverpoint_count>0) {
                        setUpMap4(point.latitude,point.longitude);
                    }
                    if(mMarkerList3.size()==0){
                        if(servercount==1) {
                            outitButton2();//動的ボタンの削除
                        }
                    }
                    //markesplace ルート検索で使用するためpointぶちこむ
                    markersplace = point;
                }
            });
        }

        //キーボードを閉じたいEditTextオブジェクト
        final EditText editText1=(EditText)findViewById(R.id.editText1);
        //画面全体のレイアウト
        mainLayout = (FrameLayout)findViewById(R.id.map_LinearLayout);
        //キーボード表示を制御するためのオブジェクト
        inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        // ボタンを設定追加
        Button button5 = (Button) findViewById(R.id.server);
        editText1.setClickable(true);
        editText1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    setAdapters();
                }catch (Exception e){
                    Log.e("送るのがダメ!!!!!!!!!!!!!!",e.toString());
                }
                editText1.requestFocus();
            }
        });
        button5.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //do something
                if(mMarkerList3.size()==0){
                    if(servercount==1) {
                        outitButton2();//動的ボタンの削除
                    }
                }
                initButton2();
            }
        });

        Button button4=(Button)findViewById(R.id.search);
            button4.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if(editText1.getText().toString().length()>0) {
                        try {
                            if (mMarkerList.size() >= 1) {
                                mMarkerList.remove(0);//mMarkerList[0]の要素を削除
                                mMap.clear();
                            }
                            if(btn_delete==1) {
                                outitButton();
                            }
                            if(mMarkerList3.size()==0){
                                if(servercount==1) {
                                    outitButton2();//動的ボタンの削除
                                }
                            }
                            if(generationlist==1) {
                                deleteList();
                            }
                            if(mMarkerList2.size()>=1) {
                                mMap.clear();
                                for (int i = 0; i < mMarkerList2.size(); i++) {
                                    mMarkerList2.get(i).remove();//Mapのマーカーを削除
                                }
                                mMarkerList2.clear();//mMarkerList2の要素を一括削除
                            }

                            String place_type = editText1.getText().toString();
                            Place_search(place_type);//住所取得のメソッドに飛ばす
                        } catch (Exception e) {
                            Toast.makeText(MapsActivity.this, "検索エラー", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });

        //ルート案内
        //プログレス
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("検索中........");
        progressDialog.hide();
    }
    //ルート検索したらgetDirectionsUrlから返ってきたものをdownloadTaskに飛ばす
    public void routeSearch() {
        progressDialog.show();

        LatLng origin = new LatLng(testLocation.getLatitude(), testLocation.getLongitude());//現在地
        LatLng dest = new LatLng(markersplace.latitude, markersplace.longitude);//マーカー(タップ)位置

        String url = getDirectionsUrl(origin, dest);

        RouteDownloadTask downloadTask = new RouteDownloadTask();

        downloadTask.execute(url);
    }
    //httpのurl + 現在地と目的地の緯度経度返す
    public static String getDirectionsUrl(LatLng origin, LatLng dest) {
        String url=null;
        try {
            String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

            String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

            String sensor = "sensor=false";

            //パラメータ
            String parameters = str_origin + "&" + str_dest + "&" + sensor + "&language=ja" + "&mode=" + travelMode;

            //JSON指定
            String output = "json";

            url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        } catch (Exception e) {
            Log.d("error1", "!!!!!!!!!!!!!!!!!!!");
        }
        return url;
    }

    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    //動的ボタン生成　ルート詮索時
    private void initButton() {
        FrameLayout flayout=(FrameLayout)findViewById(R.id.map_LinearLayout);
        // ボタンを設定する
        info_btn = new Button(this);
        info_btn.setTag("underbutton");
        info_id = info_btn.getId();
        info_btn.setId(info_id);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM + Gravity.LEFT;
        info_btn.setBackgroundColor(Color.rgb(137,189,222));//SkyBlue
        info_btn.setLayoutParams(params);
        info_btn.setText("ルート情報");

        speech_btn = new Button(this);
        speech_btn.setTag("underbutton");
        speech_id = speech_btn.getId();
        speech_btn.setId(speech_id);
        FrameLayout.LayoutParams params2 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.WRAP_CONTENT);
        params2.gravity = Gravity.BOTTOM + Gravity.RIGHT;
        speech_btn.setBackgroundColor(Color.rgb(137,189,222));//SkyBlue
        speech_btn.setLayoutParams(params2);
        speech_btn.setText("音声案内");

        info_btn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                //do something
                //Toast.makeText(MapsActivity.this,points.toString(),Toast.LENGTH_LONG).show();//ルートの緯度経度
                tv = new TextView(v.getContext());//TextViewのインスタンス
                tv.setMovementMethod(LinkMovementMethod.getInstance());
                tv.setText(Html.fromHtml(posinfo));//HTMLのフォーマットに変える
                new AlertDialog.Builder(MapsActivity.this)//ダイアログにHTMLを表示
                        .setTitle("経路詳細")
                        .setView(tv)
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
        speech_btn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                //do something
                tv = new TextView(v.getContext());//TextViewのインスタンス
                tv.setMovementMethod(LinkMovementMethod.getInstance());
                tv.setText(Html.fromHtml(posinfo));//HTMLのフォーマットに変える
                //再生ピッチの設定
                tts.setPitch(PITCH_NORMAL);
                // 再生速度の設定
                tts.setSpeechRate(SPEECH_NORMAL);
                tts.setLanguage(Locale.JAPAN);
                String yomiage = tv.getText().toString().replaceAll("/","　");
                for(int i=0;i<yomiage.length();i++){//正規表現

                }
                speechText(yomiage);
            }
        });
        flayout.addView(info_btn);
        flayout.addView(speech_btn);
        routes_fl=flayout;
        btn_delete=1;
    }

    private void outitButton(){//動的ボタンの削除　ルート検索時の
        routes_fl.removeView(info_btn);
        routes_fl.removeView(speech_btn);
        btn_delete=0;
    }

    //動的ボタン生成　サーバ通信時
    private void initButton2() {
        FrameLayout flayout=(FrameLayout)findViewById(R.id.map_LinearLayout);
        // ボタンを設定する
        set_btn = new RadioButton(this);
        set_btn.setTag("under_set_button");
        set_id = set_btn.getId();
        set_btn.setId(set_id);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM + Gravity.LEFT;
        set_btn.setBackgroundColor(Color.rgb(137,189,222));//SkyBlue
        set_btn.setLayoutParams(params);
        set_btn.setText("ルート選択");

        com_btn = new Button(this);
        com_btn.setTag("under_com_button");
        com_id = com_btn.getId();
        com_btn.setId(com_id);
        FrameLayout.LayoutParams params2 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.WRAP_CONTENT);
        params2.gravity = Gravity.BOTTOM + Gravity.RIGHT;
        com_btn.setBackgroundColor(Color.rgb(137,189,222));//SkyBlue
        com_btn.setLayoutParams(params2);
        com_btn.setText("送信");

        set_btn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                //do something
                try {
                    serverpoint_count++;
                } catch (Exception e) {
                    Log.e("通信エラー/"+e,toString());
                }
            }
        });
        com_btn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                //do something
                Toast.makeText(MapsActivity.this,"Server通信開始します",Toast.LENGTH_SHORT).show();
                //非同期タスクの生成
//                    final AsyncJob asynctask = new AsyncJob(MapsActivity.this);
//                    //実行
//                    asynctask.execute("A","B","C");//引数は適当

                VolleyCommunication vo = new VolleyCommunication();
                vo.POST(serverPointsList, getApplication());
                serverpoint_count=0;
                serverPointsList.clear();
                outitButton2();
                mMarkerList3.clear();
                mMap.clear();
            }
        });
        flayout.addView(set_btn);
        flayout.addView(com_btn);
        server_fl=flayout;
        servercount=1;
    }

    private void outitButton2(){//動的ボタンの削除　サーバ通信時の
        server_fl.removeView(set_btn);
        server_fl.removeView(com_btn);
        servercount=0;
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap2(double x,double y)} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();//マーカーが生成されれば地図はちゃんと生成されているという証明
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    //最初に呼び出される
    private void setUpMap() {
        Toast.makeText(MapsActivity.this, "MAP描画完了...現在地に移動します", Toast.LENGTH_LONG).show();
        }
    //マーカーを設置
    private void setUpMap2(double x,double y) {
        // 場所名を文字列で取得する
        String str_address = null;
        try{// 住所を取得
            str_address = GeocodeManager.point2address(x,y,context);
        }
        catch(IOException e){
            str_address = "座標情報から住所へのデコードに失敗";
        }
        //ここまで住所

        Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(x,y)).title(str_address));
        mMarkerList.add(marker);
        //2個以上設置しようとすると新しいのを設置し最初のマーカーを消す
        if (mMarkerList.size() >= 2) {
            mMarkerList.get(0).remove();
            mMarkerList.remove(0);
        }
    }

    //複数マーカーを設置
    private void setUpMap3(double x,double y) {
        // 場所名を文字列で取得する
        String str_address2 = null;
        try{// 住所を取得
            str_address2 = GeocodeManager.point2address(x,y,context);
        }
        catch(IOException e){
            str_address2 = "座標情報から住所へのデコードに失敗";
        }
        //ここまで住所

        Marker marker2 = mMap.addMarker(new MarkerOptions().position(new LatLng(x, y))//緯度経度
                .title(str_address2)//住所
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));//色b
        mMarkerList2.add(marker2);//マーカー作成
    }

    //マーカーを設置(サーバに送るとき)
    private void setUpMap4(double x,double y) {
        // 場所名を文字列で取得する
        String str_address = null;
        try{// 住所を取得
            str_address = GeocodeManager.point2address(x,y,context);
        }
        catch(IOException e){
            str_address = "座標情報から住所へのデコードに失敗";
        }
        Marker marker3 = mMap.addMarker(new MarkerOptions().position(new LatLng(x,y))
                .title(str_address)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMarkerList3.add(marker3);
        serverPointsList.add(new LatLng(x,y));
        //3個以上設置しようとすると新しいのを設置し最初のマーカーを消す
        if (mMarkerList3.size() >= 3) {
            mMarkerList3.get(0).remove();
            mMarkerList3.remove(0);
        }
    }

    //付け足し
    @Override
    public void onLocationChanged(Location location) {      //現在地に戻る動作
        if(MyplaceChecker == 0) {
            testLocation = location;//上で使えるように他で使える引数に入れる
            CameraPosition cameraPos = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude()/*Location型の緯度*/, location.getLongitude()/*経度*/)).zoom(17.0f)
                    .bearing(0).build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPos));

            // MyLocationレイヤーを有効に
            mMap.setMyLocationEnabled(true);
            // MyLocationButtonを有効に
            UiSettings settings = mMap.getUiSettings();
            settings.setMyLocationButtonEnabled(true);
        }
        MyplaceChecker = 1;
    }

    @Override
    public void onConnectionFailed(ConnectionResult result){}

    @Override
    public void onConnected(Bundle connectionHint){
        fusedLocationProviderApi.requestLocationUpdates(mLocationClient, REQUEST, this);
    }

    @Override
    public void onConnectionSuspended(int cause){}

    //検索ワードの住所取得のメソッド
    public void Place_search(String type){
        try {
            Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
            //検索範囲指定
            List<Address> addressList = geocoder.getFromLocationName(type, 30,//検索件数
                    testLocation.getLatitude() - 0.009,//左下の緯度
                    testLocation.getLongitude() - 0.009,//左下の経度
                    testLocation.getLatitude() + 0.009,//右上の緯度
                    testLocation.getLongitude() + 0.009);//右上の経度

            if (addressList.size() >= 2) {
                for (int i = 0; i < addressList.size(); i++) {
                    Address address = addressList.get(i);
                    search_lat = address.getLatitude(); //緯度
                    search_lng = address.getLongitude();//経度
                    setUpMap3(search_lat, search_lng);
                }

                //ズームアウト
                CameraPosition cameraPos = new CameraPosition.Builder()
                        .target(new LatLng(testLocation.getLatitude(), testLocation.getLongitude()))
                        .zoom(13.0f)
                        .bearing(0).build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPos));
                // タップ時のイベントハンドラ登録
                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    public boolean onMarkerClick(Marker marker) {
                        // TODO Auto-generated method stub
                        if (btn_delete == 1) {
                            outitButton();
                        }
                        if(mMarkerList3.size()==0){
                            if(servercount==1) {
                                outitButton2();//動的ボタンの削除
                            }
                        }
                        //markersplace ルート検索で使用するため緯度、経度ぶちこむ
                        markersplace = marker.getPosition();//タップしたマーカーの緯度経度取得
                        return false;
                    }
                });
            }
            else if (addressList.size() == 1) {
                Address address = addressList.get(0);
                search_lat = address.getLatitude(); //緯度
                search_lng = address.getLongitude();//経度
                setUpMap3(search_lat, search_lng);
                //カメラ移動
                CameraPosition cameraPos = new CameraPosition.Builder()
                        .target(new LatLng(address.getLatitude(), address.getLongitude()))
                        .zoom(13.0f)
                        .bearing(0).build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPos));
                //markersplace ルート検索で使用するため緯度、経度ぶちこむ
                markersplace = new LatLng(search_lat, search_lng);
            }
//            else if(addressList.size() == 0){
//                Toast.makeText(getApplicationContext(),"0こ",Toast.LENGTH_LONG).show();
//            }
            // MyLocationレイヤーを有効に
            mMap.setMyLocationEnabled(true);
            // MyLocationButtonを有効に
            UiSettings settings = mMap.getUiSettings();
            settings.setMyLocationButtonEnabled(true);
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(MapsActivity.this, "NOT FOUND...", Toast.LENGTH_LONG).show();
        }
    }

    //EditTextを押した時にリストをだす
    protected void setAdapters() {
        if(generationlist==1) {
            deleteList();
        }
        if(mMarkerList.size()>=1) {
            mMarkerList.clear();//mMarkerListの要素を一括削除
            mMap.clear();
        }
        if(mMarkerList2.size()>=1) {
            mMarkerList2.clear();//mMarkerList2の要素を一括削除
            mMap.clear();
        }
        if (btn_delete == 1) {
            outitButton();
        }
        if(mMarkerList3.size()==0){
            if(servercount==1) {
                outitButton2();//動的ボタンの削除
            }
        }
        LinearLayout layout = (LinearLayout) findViewById(R.id.list_item);

        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,dataList);
        listView1=new ListView(this);
        listView1.setAdapter(adapter);
        layout.addView(listView1);
        list_layout=layout;
        generationlist=1;
        // アイテムクリック時ののイベントを追加
        listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //test
                list_location = new ArrayList<HashMap<String, Double>>();

                String list_position = "location=" + testLocation.getLatitude() + "," + testLocation.getLongitude();
                String list_radius = "radius=1000";

                //パラメータ
                String parameters = list_position + "&" + list_radius + "&types=" + typeList.get(position) + "&key=AIzaSyC0Tc_0WLZkyfvR4T_6OOUOvCrGwgTS2OQ";

                //JSON指定
                String output = "json";
                type_url = "https://maps.googleapis.com/maps/api/place/nearbysearch/" + output + "?" + parameters;
                // Log.e("url",type_url);

                PlaceDownloadTask placedownloadtask = new PlaceDownloadTask();
                placedownloadtask.execute(type_url);
                //何故か入ってない
                Log.e("list_location1:",list_location.toString());

                new AlertDialog.Builder(MapsActivity.this)//ダイアログにHTMLを表示
                        .setTitle("経路詳細")
                        .setMessage(dataList.get(position) + "は表示されました")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                // Positive Buttonが押された時の処理を記述{
                                //2買い目しないとlistにデータが入らない
                                PlaceDownloadTask placedownloadtask = new PlaceDownloadTask();
                                placedownloadtask.execute(type_url);
                                Log.e("list_location2:", list_location.toString());
                                for (int i = 0; i < list_location.size(); i++) {
                                    HashMap<String, Double> tmp_hs = list_location.get(i);
                                    setUpMap3(tmp_hs.get("p_lat"), tmp_hs.get("p_lng"));
                                    if (list_location.size() == 1) {
                                        markersplace = new LatLng(tmp_hs.get("p_lat"), tmp_hs.get("p_lng"));
                                    }
                                }
                            }
                        })
                        .show();
                //ズームアウト
                CameraPosition cameraPos = new CameraPosition.Builder()
                        .target(new LatLng(testLocation.getLatitude(), testLocation.getLongitude()))
                        .zoom(15.0f)
                        .bearing(0).build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPos));
                // タップ時のイベントハンドラ登録
                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    public boolean onMarkerClick(Marker marker) {
                        // TODO Auto-generated method stub
                        if (btn_delete == 1) {
                            outitButton();
                        }
                        if(mMarkerList3.size()==0){
                            if(servercount==1) {
                                outitButton2();//動的ボタンの削除
                            }
                        }
                        //markersplace ルート検索で使用するため緯度、経度ぶちこむ
                        markersplace = marker.getPosition();//タップしたマーカーの緯度経度取得
                        return false;
                    }
                });
                if(generationlist==1) {
                    deleteList();
                }
            }
        });
    }
    //生成したlistを削除
    public void deleteList(){
        list_layout.removeView(listView1);
        generationlist=0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != tts) {
            // TextToSpeechのリソースを解放する
            tts.shutdown();
        }
    }

    public void onInit(int status) {
        if (TextToSpeech.SUCCESS == status) {
            Locale locale = Locale.ENGLISH;
            if (tts.isLanguageAvailable(locale) >= TextToSpeech.LANG_AVAILABLE) {
                tts.setLanguage(locale);
            } else {
                Log.d("", "Error SetLocale");
            }
        } else {
            Log.d("", "Error Init");
        }
    }

    private void speechText(String tv) {
    if (0 < tv.length()) {
        if (tts.isSpeaking()) {
            // 読み上げ中なら止める
            tts.stop();
        }
        Log.e("tvの内容",tv);
        // 読み上げ開始
        tts.speak(tv, TextToSpeech.QUEUE_FLUSH, null);
        }
    }
}


