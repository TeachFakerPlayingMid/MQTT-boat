package com.example.mqtt_boat.choosepoints;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.Projection;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.example.mqtt_boat.GpsCorrect;
import com.example.mqtt_boat.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SetpointActivity extends AppCompatActivity
        implements BaiduMap.OnMapStatusChangeListener, PoiItemAdapter.MyOnItemClickListener
        , OnGetGeoCoderResultListener {

    public LatLng mLatLon;
    public List<LatLng> points = new ArrayList<LatLng>();
    public Overlay mPolyline;
    SQLiteDatabase pathBase;
    // 地图View实例
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private LatLng mCenter;
    private Handler mHandler;
    private TextView point_lon;
    private TextView point_lat;
    private Switch showSwitch;

    private TextView showPoint;
    private Button btn_toList;
    private Button btn_showPath;
    private RadioGroup setSample_Rg;

    private RecyclerView mRecyclerView;

    private PoiItemAdapter mPoiItemAdapter;

    private GeoCoder mGeoCoder = null;

    SimpleDataBase simpleDataBase;
    SQLiteDatabase simpleBase;
    SharedPreferences sp;

    private boolean mStatusChangeByItemClick = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setpoint);

        simpleDataBase = new SimpleDataBase(this);//实例化水质数据库
        simpleBase = simpleDataBase.getWritableDatabase(); //声明数据库单元

        sp = getSharedPreferences("setSimple",MODE_PRIVATE);

        init();
        setTitle("采样规划");
        //切换显示/隐藏路径
        showSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(points.isEmpty()){
                    showSwitch.setChecked(true);
                    Toast.makeText(SetpointActivity.this, "请添加路径点", Toast.LENGTH_SHORT).show();//弹窗功能
                    return;
                }
                if (isChecked){
                    mPolyline.setVisible(false);
                }else {
                    if(points.size()<2){
                        return;
                    }
                    showPathline(points);
                }
            }
        });

        //表记采样点
        setSample_Rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                String polon = point_lon.getText().toString();
                String polat = point_lat.getText().toString();
                String poid = "1";
                if (R.id.rb_simple1 == checkedId) {
                    poid = "1";
                } else if (R.id.rb_simple2 == checkedId) {
                    poid = "2";
                } else if (R.id.rb_simple3 == checkedId) {
                    poid = "3";
                }else if (R.id.rb_simple4 == checkedId) {
                    poid = "4";
                }else if (R.id.rb_simple5 == checkedId) {
                    poid = "5";
                }else if (R.id.rb_simple6 == checkedId) {
                    poid = "6";
                }
                savePointDate(poid,polon,polat);
                setMarka(mLatLon);
                Toast.makeText(SetpointActivity.this, poid+"号采样点记录完成！", Toast.LENGTH_SHORT).show();//弹窗功能

                //代替传值，选点后直接写入底层数据库
                String sql = "insert into user(编号,经度,纬度)values(?,?,?)";
                simpleBase.execSQL(sql,
                        new Object[]{poid,
                                point_lon.getText().toString(),
                                point_lat.getText().toString()});
            }
        });

        //跳转采样点数据记录界面
        btn_toList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sql = "select * from user";                                                      //数据库操作语句，按时间排序，检索数据库 ??
                Cursor cursor = simpleBase.rawQuery(sql, null);                             //操作数据库的光标
                ArrayList<Map<String, String>> listData = cursorCursorToList(cursor);                   //界面传值动态数组
                Bundle bundle = new Bundle();                                                           //实例化传值数据类型
                bundle.putSerializable("simpledata", listData);                                         //通过bundle对数据进行封装
                Intent intent = new Intent(SetpointActivity.this, PathListActivity.class);  //初始化历史数据界面
                intent.putExtras(bundle);                                                               //传递的数据是bundle类型
                startActivity(intent);                                                                  //启动界面
            }
        });
    }

    public void savePointDate(String ID, String LON, String LAT){
        SharedPreferences.Editor editor = sp.edit();
        String setLon_id = "LON"+ID;
        String setLat_id = "LAT"+ID;
        editor.putString(setLon_id,LON);
        editor.putString(setLat_id,LAT);
        editor.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null != mMapView) {
            mMapView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (null != mMapView) {
            mMapView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (simpleBase != null) {
            simpleBase.close();
        }
        if (simpleDataBase != null) {
            simpleDataBase.close();
        }

        if (pathBase != null) {
            pathBase.close();
        }
        if (null != mHandler) {
            mHandler.removeCallbacksAndMessages(null);
        }

        if (null != mGeoCoder) {
            mGeoCoder.destroy();
        }

        if (null != mMapView) {
            mMapView.onDestroy();
        }
    }

    private void init() {
        initRecyclerView();
        mHandler = new Handler(this.getMainLooper());
        initMap();
    }
    private void initMap() {
        mMapView = findViewById(R.id.mapview);
        point_lon = findViewById(R.id.txt_point_lon);
        point_lat = findViewById(R.id.txt_point_lat);
        setSample_Rg = findViewById(R.id.rg_simple);

        showPoint = findViewById(R.id.txt_ManyPoint);
        btn_toList = findViewById(R.id.btn_toList);
        btn_showPath = findViewById(R.id.btn_showpath);
        showSwitch = findViewById(R.id.show_switch);

        if (null == mMapView) {
            return;
        }
        mBaiduMap = mMapView.getMap();
        if (null == mBaiduMap) {
            return;
        }

        // 设置初始中心点为喻家湖
        mCenter = new LatLng(30.52530678,114.4362567);
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLngZoom(mCenter, 58f);
        mBaiduMap.setMapStatus(mapStatusUpdate);
        mBaiduMap.setOnMapStatusChangeListener(this);
        mBaiduMap.setOnMapLoadedCallback(new BaiduMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                createCenterMarker();
                reverseRequest(mCenter);
            }
        });
    }

    private void setMarka(LatLng latlon){
        //定义Maker坐标点
        LatLng point = new LatLng(latlon.latitude, latlon.longitude);
        //构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_marka);
        //构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions()
                .position(point)
                .icon(bitmap);
        //在地图上添加Marker，并显示
        mBaiduMap.addOverlay(option);
        points.add(point);
    }

    /**
     * 创建地图中心点marker
     */
    private void createCenterMarker() {
        Projection projection = mBaiduMap.getProjection();
        if (null == projection) {
            return;
        }

        Point point = projection.toScreenLocation(mCenter);
        BitmapDescriptor bitmapDescriptor =
                BitmapDescriptorFactory.fromResource(R.drawable.icon_add_ani);
        if (null == bitmapDescriptor) {
            return;
        }

        MarkerOptions markerOptions = new MarkerOptions()
                .position(mCenter)
                .icon(bitmapDescriptor)
                .flat(false)
                .fixedScreenPosition(point);
        mBaiduMap.addOverlay(markerOptions);
        bitmapDescriptor.recycle();
    }

    /**
     * 初始化recyclerView
     */
    private void initRecyclerView() {
        mRecyclerView = findViewById(R.id.poi_list);
        if (null == mRecyclerView) {
            return;
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
    }

    /**
     * 逆地理编码请求,GPS纠偏问题！
     *
     * @param latLng
     */
    private void reverseRequest(LatLng latLng) {
        if (null == latLng) {
            return;
        }
        mLatLon = latLng;
//        ReverseGeoCodeOption reverseGeoCodeOption = new ReverseGeoCodeOption().location(latLng)
//                .newVersion(1) // 建议请求新版数据
//                .radius(sDefaultRGCRadius);
        GpsCorrect ptWGS = new GpsCorrect();
        LatLng P = ptWGS.WGS2GCJ(latLng.longitude,latLng.latitude);     //GPS纠偏，存在误差。需要BD至WGS?????

//        //方法二还是有误差！！
//        CoordinateConverter converter  = new CoordinateConverter()
//                .from(CoordinateConverter.CoordType.GPS)
//                .coord(latLng);
//        LatLng P = converter.convert();
////        x = 2*latLng.longitude-P.longitude;
////        y = 2*latLng.latitude-P.latitude;

//        Toast.makeText(SetpointActivity.this, String.valueOf(P.latitude), Toast.LENGTH_SHORT).show();//弹窗功能
        point_lon.setText(String.valueOf(P.longitude));               //写入对应文本显示控件
        point_lat.setText(String.valueOf(P.latitude));               //写入对应文本显示控件

        if (null == mGeoCoder) {
            mGeoCoder = GeoCoder.newInstance();
        }

        mGeoCoder.setOnGetGeoCodeResultListener(this);
    }

    //
    private ArrayList<Map<String, String>> cursorCursorToList(Cursor cursor) {
        ArrayList<Map<String, String>> result = new ArrayList<>();
        while (cursor.moveToNext()) {
            Map<String, String> map = new HashMap<>();
            map.put("P_id", cursor.getString(0));
            map.put("P_lon", cursor.getString(1));
            map.put("P_lat", cursor.getString(2));
            result.add(map);
        }
        return result;
    }


    public void hidePath(View source) {
        mPolyline.setVisible(false);
    }


    private void showPathline(List<LatLng> points){
        OverlayOptions mOverlayOptions = new PolylineOptions()
                .width(10)
                .color(0xAAFF0000)
                .points(points)
                .dottedLine(true); //设置折线显示为虚线
        //在地图上绘制折线
        //mPloyline 折线对象
        mPolyline = mBaiduMap.addOverlay(mOverlayOptions);
    }


    //标记路径信息写入数据库+显示路径连线
    public void setPoint(View source) {
        btn_showPath.setClickable(false);
        showPathline(points);
    }

    private void showPathData(List<OnePoint> ManyPoint) {
        StringBuilder stringBuilder = new StringBuilder();
        for (OnePoint po : ManyPoint) {
            stringBuilder.append("经度:");
            stringBuilder.append(po.getP_lon());
            stringBuilder.append(" 纬度:");
            stringBuilder.append(po.getP_lat() + "\n");
        }
        showPoint.setText(stringBuilder.toString());
    }

    //清空路径节点
    public void clc_Path(View source) {
        String sql = "delete from user";

        simpleBase.execSQL(sql, new String[]{});
        mBaiduMap.clear();
        createCenterMarker();
        points.clear();
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

    }
    @Override
    public void onGetReverseGeoCodeResult(final ReverseGeoCodeResult reverseGeoCodeResult) {
        if (null == reverseGeoCodeResult) {
            return;
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                updateUI(reverseGeoCodeResult);
            }
        });
    }
    /**
     * 更新UI
     *
     * @param reverseGeoCodeResult
     */
    private void updateUI(ReverseGeoCodeResult reverseGeoCodeResult) {
        List<PoiInfo> poiInfos = reverseGeoCodeResult.getPoiList();
        PoiInfo curAddressPoiInfo = new PoiInfo();
        curAddressPoiInfo.address = reverseGeoCodeResult.getAddress();
        curAddressPoiInfo.location = reverseGeoCodeResult.getLocation();

        if (null == poiInfos) {
            poiInfos = new ArrayList<>(2);
        }

        poiInfos.add(0, curAddressPoiInfo);

        if (null == mPoiItemAdapter) {

            mPoiItemAdapter = new PoiItemAdapter(poiInfos);
            mRecyclerView.setAdapter(mPoiItemAdapter);
            mPoiItemAdapter.setOnItemClickListener(this);
        } else {
            mPoiItemAdapter.updateData(poiInfos);
        }
    }

    @Override
    public void onMapStatusChangeStart(MapStatus mapStatus) {
    }

    @Override
    public void onMapStatusChangeStart(MapStatus mapStatus, int i) {
    }

    @Override
    public void onMapStatusChange(MapStatus mapStatus) {

    }

    @Override
    public void onMapStatusChangeFinish(MapStatus mapStatus) {
        LatLng newCenter = mapStatus.target;

        // 如果是点击poi item导致的地图状态更新，则不用做后面的逆地理请求，
        if (mStatusChangeByItemClick) {
            if (!Utils.isLatlngEqual(mCenter, newCenter)) {
                mCenter = newCenter;
            }
            mStatusChangeByItemClick = false;
            return;
        }

        if (!Utils.isLatlngEqual(mCenter, newCenter)) {
            mCenter = newCenter;
            reverseRequest(mCenter);
        }
    }

    @Override
    public void onItemClick(int position, PoiInfo poiInfo) {
        if (null == poiInfo || null == poiInfo.getLocation()) {
            return;
        }

        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLng(poiInfo.getLocation());
        mBaiduMap.setMapStatus(mapStatusUpdate);
    }

    /*
     * 设置顶部工具条的标题
     */
    protected void setTitle(String title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }

}