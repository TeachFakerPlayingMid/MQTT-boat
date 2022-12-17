package com.example.mqtt_boat;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.baidu.location.LocationClient;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.example.mqtt_boat.choosepoints.SetpointActivity;
import com.example.mqtt_boat.joystick.CircleViewByImage;
import com.example.mqtt_boat.listview.ListViewActivity;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    MapView mapView;
    BaiduMap BD_View;
    boolean isFirstLocate =true;
    LocationClient mLocationClient;
    private int showmap_flag = 0;
    private double g_lon;
    private double g_lat;
    private float g_dir;

    private String host = "tcp://a16CdcvOkvF.iot-as-mqtt.cn-shanghai.aliyuncs.com:1883"; //阿里云服务器地址
    private String userName = "APP1.0&a16CdcvOkvF";   //物联网产品名
    private String passWord = "1FFEA531B7FB5BE49DB06641649A3776C2090B1D";  //物联网产品密码
    private String mqtt_id = "Boat|securemode=3,signmethod=hmacsha1|";   //物联网设备id

    private final String mqtt_sub_lon = "/a16CdcvOkvF/APP1.0/user/Longitude"; //订阅经度话题
    private final String mqtt_sub_lat = "/a16CdcvOkvF/APP1.0/user/Latitude"; //订阅纬度话题
    private final String mqtt_sub_head = "/a16CdcvOkvF/APP1.0/user/DirectionAngle"; //订阅航向角话题
    private final String mqtt_sub_speed = "/a16CdcvOkvF/APP1.0/user/USVSpeed"; //订阅航速话题
    private final String mqtt_sub_temp = "/a16CdcvOkvF/APP1.0/user/Salinity"; //订阅温度（/盐度）话题

    private final String mqtt_pub_switchpump = "/a16CdcvOkvF/APP1.0/user/pump";//发布水泵开关话题
    private final String mqtt_pub_chooseboat = "/a16CdcvOkvF/APP1.0/user/Chooseboat";//发布用艇选择话题
    private final String mqtt_pub_position = "/a16CdcvOkvF/APP1.0/user/Position";//发布采样位置话题
    private final String mqtt_pub_mode = "/a16CdcvOkvF/APP1.0/user/governor";//发布模式切换话题
    private final String mqtt_pub_wsad = "/a16CdcvOkvF/APP1.0/user/control";//发布手动方向指令话题
    private final String mqtt_pub_rpm = "/a16CdcvOkvF/APP1.0/user/setrpm";//发布转速调节话题
    private final String mqtt_pub_setPoint = "/a16CdcvOkvF/APP1.0/user/setPoint";//发布转速调节话题

    //以下是实例化MQTT通讯相关变量
    private ScheduledExecutorService scheduler;
    private MqttClient client;
    private MqttConnectOptions options;
    private Handler handler;

    private TextClock mTextClock;
    private RadioButton selectedPoint;

    private int pump_flag = 0;
    private int mode_flag = 1;
    private ImageView show_map;
    private ImageView pump_off;
    private ImageView pump_on;
    private ImageView operation;
    private ImageView self_motion;
    private TextView text_lon;
    private TextView text_lat;
    private TextView text_head;
    private TextView text_speed;
    private TextView text_nacl;
    private TextView txt_onoff;
    private LinearLayout ly_test;
    private FrameLayout layout_whool;
    private LinearLayout data_bases;
    private LinearLayout HKVision;
    private LinearLayout Pump_Switch;
    private LinearLayout Mode_Change;
    private TextView txt_mode;
    private LinearLayout txt_GB;
    private Switch mSwitch;

    private SeekBar sb_rpm;
    private TextView txt_cur;

    private CircleViewByImage mmview;
    private TextView mmaction;
    private String newLine = "\n";
    private String tab = "\t";
    private String text = "";

    private RadioGroup USV_Rg;
    private RadioGroup Sample_Rg;
    private RadioButton homeward;
    private RadioButton planPath;
    List<LatLng> USV_points = new ArrayList<LatLng>();

    WaterDataBase waterDataBase;
    SQLiteDatabase database;
    SharedPreferences sp;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
//        fullScreenConfig();  // 全屏显示
        setContentView(R.layout.activity_main);
        sp = getSharedPreferences("setSimple",MODE_PRIVATE);                                  //采样点信息，通过规划存储在应用内存

        waterDataBase = new WaterDataBase(this);                                            //实例化水质数据库
        database = waterDataBase.getWritableDatabase();                                             //声明数据库单元

        ui_init();                                                                                  //变量初始化，规范化写法
        bindViews();                                                                                //转速调节杆的调用函数
        initJoystick();                                                                             //初始化手动控制摇杆

        BD_View = mapView.getMap();
//        BD_View.setMapLanguage(MapLanguage.ENGLISH);//英文版电子地图！
        BD_View.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        BD_View.setMyLocationEnabled(true);

        //水泵开关按钮的点击事件
        Pump_Switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pump_flag == 0)                                                                 //led_flag为水泵状态的标志位
                {
                    publishmessageplus(mqtt_pub_switchpump, "{\"pump\":11}");
                    pump_flag = 1;                                                                  //状态标志位变更,打开1，关闭0
                    txt_onoff.setText("关闭水泵");                                                    //文本提示变更
                    pump_on.clearAnimation();                                                       //切换水泵图片，使界面直观显示水泵状态，清空图片状态
                    pump_on.setVisibility(View.VISIBLE);                                            //隐藏图片
                    pump_off.clearAnimation();                                                      //清空图片状态
                    pump_off.setVisibility(View.INVISIBLE);                                         // 使图片可见
                } else {
                    publishmessageplus(mqtt_pub_switchpump, "{\"pump\":10}");
                    pump_flag = 0;
                    txt_onoff.setText("打开水泵");
                    pump_on.clearAnimation();
                    pump_on.setVisibility(View.INVISIBLE);
                    pump_off.clearAnimation();
                    pump_off.setVisibility(View.VISIBLE);
                }
            }
        });

        //模式切换按钮的点击事件【手动0，自动1】
        Mode_Change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mode_flag == 1)     //
                {
                    publishmessageplus(mqtt_pub_mode, "{\"Mode\":0}");
                    Sample_Rg.setVisibility(View.INVISIBLE);                                        //使采样点单选组不可见
                    layout_whool.setVisibility(View.VISIBLE);                                       //使手动摇杆可见
                    //改变界面图标
                    mode_flag = 0;
                    txt_mode.setText("定点追踪");
//                    txt_mode.setText("AUTO");
                    self_motion.clearAnimation();                                                   //清空图片状态
                    self_motion.setVisibility(View.VISIBLE);                                        //隐藏图片
                    operation.clearAnimation();
                    operation.setVisibility(View.INVISIBLE);                                        // 使图片可见
                } else {
                    publishmessageplus(mqtt_pub_mode, "{\"Mode\":1}");
                    mode_flag = 1;
                    Sample_Rg.setVisibility(View.VISIBLE);
                    layout_whool.setVisibility(View.INVISIBLE);
                    txt_mode.setText("手动模式");
                    homeward.setChecked(true);
                    operation.clearAnimation();
                    operation.setVisibility(View.VISIBLE);
                    self_motion.clearAnimation();
                    self_motion.setVisibility(View.INVISIBLE);
                }
            }
        });

        //水质检测按钮的点击事件
        ly_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);      //实例化等待进度对话框
                progressDialog.setTitle("提示");                                                     //对话框标题
                progressDialog.setMessage("水质检测中……");                                            //对话框提示内容
                //涉及进度侦听事件，此处为演示用代码【随机生成盐度数据】
                progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        int max = 100, min = 1;
                        int ran2 = (int) (Math.random() * (max - min) + min);
                        String nacl_1 = String.valueOf(ran2);
                        text_nacl.setText("盐度：" + nacl_1 + "‰");                                  //为展示APP数据效果，暂时在此将数据“写死”后期在此接入传感器数据
                        Toast.makeText(MainActivity.this, "检测完成", Toast.LENGTH_SHORT).show();//弹窗功能
                    }
                });
                progressDialog.show();    //显示等待进度对话框
            }
        });

        //跳转数据记录界面【涉及界面跳转、传值、数据库操作】
        data_bases.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sql = "select * from user order by 时间 desc";                                //数据库操作语句，按时间排序，检索数据库 ||select 查找*所有 跟user表明 通过时间排序
                Cursor cursor = database.rawQuery(sql, null);                           //操作数据库的光标 用光标的形式生成并选中sql语句输出的值
                ArrayList<Map<String, String>> listData = cursorCursorToList(cursor);               //界面传值动态数组 见下文
                Bundle bundle = new Bundle();                                                       //实例化传值数据类型
                bundle.putSerializable("data", listData);                                           //通过bundle对数据进行封装 用于界面传值 从mainActivity到ListViewActivity
                Intent intent = new Intent(MainActivity.this, ListViewActivity.class);//初始化水质历史数据界面 界面跳转.class 到另一个class里
                intent.putExtras(bundle);                                                           //传递的数据是bundle类型 input bundle from one class into another by using bundle
                startActivity(intent);                                                              //启动界面
            }
        });

        //长按跳转本地试航视频播放界面、单击跳转摄像头实况界面
        HKVision.setOnLongClickListener(new MyVideoListenr());
        HKVision.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VideoActivity.class);   //初始化摄像头界面
                startActivity(intent);                                                              //启动界面
            }
        });

        //长按跳转路径规划界面、单机打开电子地图
        show_map.setOnLongClickListener(new MyOnLongClickListenr());
        show_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //通过标识判断隐藏or显示电子地图
                if(showmap_flag==0){
                    mapView.setVisibility(View.VISIBLE);
                    showmap_flag=1;
                }else {
                    mapView.setVisibility(View.GONE);
                    showmap_flag=0;
                }
            }
        });

        //跳转水质指标本地网页界面 转跳至WebViewActivity
        txt_GB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, WebViewActivity.class); //初始化本地指标网页界面
                startActivity(intent);                                                              //启动界面
            }
        });

        //切换规划路径节点顺序标定or实时目标点的标定
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    publishmessageplus(mqtt_pub_setPoint, "{\"setPoint\":10}");
                    planPath.setVisibility(View.VISIBLE);
//                    Toast.makeText(MainActivity.this, "请选择采样点", Toast.LENGTH_SHORT).show();//弹窗功能
                }else {
                    publishmessageplus(mqtt_pub_setPoint, "{\"setPoint\":11}");
                    planPath.setVisibility(View.INVISIBLE);
//                    Toast.makeText(MainActivity.this, "请标定路径点", Toast.LENGTH_SHORT).show();//弹窗功能
                }
            }
        });

        //选择用艇指令事件【单选】
        USV_Rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (R.id.rb_no1 == checkedId) {
                    publishmessageplus(mqtt_pub_chooseboat, "{\"Choose_boat\":0}");
                } else if (R.id.rb_no2 == checkedId) {
                    publishmessageplus(mqtt_pub_chooseboat, "{\"Choose_boat\":1}");
                } else if (R.id.rb_no3 == checkedId) {
                    publishmessageplus(mqtt_pub_chooseboat, "{\"Choose_boat\":2}");
                }
            }
        });

        //采样点选择事件【单选】
        Sample_Rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (R.id.rb_Planning == checkedId) {
                    publishmessageplus(mqtt_pub_mode, "{\"Mode\":2}");
                } else {
                    publishmessageplus(mqtt_pub_mode, "{\"Mode\":1}");
                    if (R.id.rb_aim1 == checkedId) {
                        String po = sp.getString("LON1","").substring(0, 11);//这里注意，LabVIEW下位机的数据解析精度！是否改成11or12？
                        String pa = sp.getString("LAT1","").substring(0, 11);
                        Toast.makeText(MainActivity.this,"采样点①"+"\r\n"+"经度："+po+"\r\n"+"纬度："+pa,Toast.LENGTH_SHORT).show();
                        publishmessageplus(mqtt_pub_position, "{\"Position\":{\"Lon\":"+po+",\"Lat\":"+pa+"}}");
                    } else if (R.id.rb_aim2 == checkedId) {
                        String po = sp.getString("LON2","").substring(0, 11);
                        String pa = sp.getString("LAT2","").substring(0, 11);
                        Toast.makeText(MainActivity.this,"采样点②"+"\r\n"+"经度："+po+"\r\n"+"纬度："+pa,Toast.LENGTH_SHORT).show();
                        publishmessageplus(mqtt_pub_position, "{\"Position\":{\"Lon\":"+po+",\"Lat\":"+pa+"}}");
                    } else if (R.id.rb_aim3 == checkedId) {
                        String po = sp.getString("LON3","").substring(0, 11);
                        String pa = sp.getString("LAT3","").substring(0, 11);
                        Toast.makeText(MainActivity.this,"采样点③"+"\r\n"+"经度："+po+"\r\n"+"纬度："+pa,Toast.LENGTH_SHORT).show();
                        publishmessageplus(mqtt_pub_position, "{\"Position\":{\"Lon\":"+po+",\"Lat\":"+pa+"}}");
                    } else if (R.id.rb_aim4 == checkedId) {
                        String po = sp.getString("LON4","").substring(0, 11);
                        String pa = sp.getString("LAT4","").substring(0, 11);
                        Toast.makeText(MainActivity.this,"采样点④"+"\r\n"+"经度："+po+"\r\n"+"纬度："+pa,Toast.LENGTH_SHORT).show();
                        publishmessageplus(mqtt_pub_position, "{\"Position\":{\"Lon\":"+po+",\"Lat\":"+pa+"}}");
                    } else if (R.id.rb_aim5 == checkedId) {
                        String po = sp.getString("LON5","").substring(0, 11);
                        String pa = sp.getString("LAT5","").substring(0, 11);
                        Toast.makeText(MainActivity.this,"采样点⑤"+"\r\n"+"经度："+po+"\r\n"+"纬度："+pa,Toast.LENGTH_SHORT).show();
                        publishmessageplus(mqtt_pub_position, "{\"Position\":{\"Lon\":"+po+",\"Lat\":"+pa+"}}");
                    } else if (R.id.rb_aim6 == checkedId) {
                        String po = sp.getString("LON6","").substring(0, 11);
                        String pa = sp.getString("LAT6","").substring(0, 11);
                        Toast.makeText(MainActivity.this,"采样点⑥"+"\r\n"+"经度："+po+"\r\n"+"纬度："+pa,Toast.LENGTH_SHORT).show();
                        publishmessageplus(mqtt_pub_position, "{\"Position\":{\"Lon\":"+po+",\"Lat\":"+pa+"}}");
                    }
                    else if (R.id.rb_homeward == checkedId) {
                        publishmessageplus(mqtt_pub_position, "{\"Position\":{\"Lon\":114.4241956,\"Lat\":30.52195261}}");//{"Position":{"Lon":114.42419561,"Lat":30.52195261}}
                    }
                }
                selectedPoint = group.findViewById(checkedId);                                      //单选组选中选项名称，便于标定水质记录
            }
        });

        Mqtt_init();                                                                                //调用MQTT通讯初始化函数
        startReconnect();                                                                           //调用MQTT通讯重连函数
        //设计Handler是用来结合线程的消息队列来发送、处理Message对象，关联线程和该线程的消息队列。作为线程通信的桥梁，再通过主线程更新UI
        handler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 1: //开机校验更新回传
                        break;
                    case 2:  // 反馈回传
                        break;
                    case 3:  //MQTT 收到消息回传   UTF8Buffer msg=new UTF8Buffer(object.toString());
                        //处理message 传过来的 obj字段（里面包了数据）截取主题
                        String T = msg.obj.toString().substring(msg.obj.toString().indexOf("user\"/") + 26, msg.obj.toString().indexOf("---"));//user"/
                        //定义局部变量，方便按照接收到的主题，进行数据处理。
                        String S = "Longitude";
                        String S1 = "Latitude";
                        String S2 = "DirectionAngle";
                        String S3 = "USVSpeed";
                        String S4 = "Salinity";
                        if (T.equals(S)) {
                            //截取数据
                            String T_val = msg.obj.toString().substring(msg.obj.toString().indexOf("Longitude\":") + 11, msg.obj.toString().indexOf("}}"));
                            double num = Double.parseDouble(T_val);
                            g_lon = num;
                            //四舍五入规范数据格式，截取数据
                            String str = String.format("%.7f", num);                                //改变数据类型，装换成字符串的形式
                            String text_val = "Longitude:" + str + "°";                             //字符串拼接，名称：数据 单位
                            //String text_val = "经度：" + str + "°";
                            text_lon.setText(text_val);                                             //写入对应文本显示控件，在主进程 handler 里面更新UI  既保证了稳定性  又不影响网络传输
                        }
                        if (T.equals(S1)) {
                            String T_val1 = msg.obj.toString().substring(msg.obj.toString().indexOf("Latitude\":") + 10, msg.obj.toString().indexOf("}}"));
                            double num = Double.parseDouble(T_val1);
                            g_lat = num;
                            String str1 = String.format("%.7f", num);
                            String text_val1 = "Latitude:  " + str1 + "°";
                            //String text_val1 = "纬度：" + str + "°";
                            text_lat.setText(text_val1);
                        }
                        if (T.equals(S2)) {
                            String T_val2 = msg.obj.toString().substring(msg.obj.toString().indexOf("DirectionAngle\":") + 16, msg.obj.toString().indexOf("}}"));
                            float num = Float.parseFloat(T_val2);
                            g_dir = num;
                            String str2 = String.format("%.1f", num);
                            String text_val2 = "Heading:  " + str2 + "°";
                            //String text_val2 = "航向：" + str + "°";
                            text_head.setText(text_val2);
                        }
                        if (T.equals(S3)) {
                            String T_val3 = msg.obj.toString().substring(msg.obj.toString().indexOf("USVSpeed\":") + 10, msg.obj.toString().indexOf("}}"));
                            float num = Float.parseFloat(T_val3);
                            String str3 = String.format("%.1f", num);
                            String text_val3 = "Speed:      " + str3 + " km/h";
                            //String text_val3 = "航速：" + str3 + " km/h";
                            text_speed.setText(text_val3);
                        }
                        if (T.equals(S4)) {
                            //注意：这里变量命名以及UI上的盐度（yandu\nacl）对应云数据为CurrentTemperature“当前温度”。。。后期检测指标待确定。
                            String T_val4 = msg.obj.toString().substring(msg.obj.toString().indexOf("Salinity\":") + 10, msg.obj.toString().indexOf("}}")); //精度格式待定~
                            float num = Float.parseFloat(T_val4);
                            String str3 = String.format("%.1f", num);
                            String text_val4 = "盐度：" + str3 + " ‰";
                            text_nacl.setText(text_val4);
                        }
                        break;
                    case 30:  //连接失败
                        Toast.makeText(MainActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
                        break;
                    case 31:   //连接成功
//                        Toast.makeText(MainActivity.this,"连接成功" ,Toast.LENGTH_SHORT).show();
                        //以下是获取不同主题下的云端信息。
                        try {
                            client.subscribe(mqtt_sub_lon, 1);
                        } catch (MqttException e) {
                            e.printStackTrace();    //调用MQTT通讯类中的函数
                        }
                        try {
                            client.subscribe(mqtt_sub_lat, 1);
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                        try {
                            client.subscribe(mqtt_sub_head, 1);
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                        try {
                            client.subscribe(mqtt_sub_speed, 1);
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                        try {
                            client.subscribe(mqtt_sub_temp, 1);
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        break;
                }

                navigateTo(g_lon,g_lat,g_dir);                                                      //接收同步显示无人艇位姿
            }
        };
    }

    //长按跳转路径规划界面
    private class MyOnLongClickListenr implements View.OnLongClickListener {
        @Override
        public boolean onLongClick(View v) {
            Intent intent = new Intent(MainActivity.this, SetpointActivity.class);    //初始化指标网页界面
            startActivity(intent);                                                                  //启动界面
            return true;
        }
    }

    //长按本地试航视频展示界面
    private class MyVideoListenr implements View.OnLongClickListener {
        @Override
        public boolean onLongClick(View v) {
            Intent intent = new Intent(MainActivity.this, LocalVideoActivity.class);  //初始化指标网页界面
            startActivity(intent);                                                                  //启动界面
            return true;
        }
    }

    //调速杆相关函数
    private void bindViews() {
        sb_rpm = (SeekBar) findViewById(R.id.sb_speed);
        txt_cur = (TextView) findViewById(R.id.txt_rspeed);
        sb_rpm.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txt_cur.setText("转速：" + progress);
//                txt_cur.setText("setSpeed: " + progress);
                publishmessageplus(mqtt_pub_rpm, "{\"setrpm\":" + progress + "}");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
//                Toast.makeText(MainActivity.this, "触碰SeekBar", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
//                Toast.makeText(MainActivity.this, "放开SeekBar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //手动控制摇杆相关函数
    //初始化
    private void initJoystick() {
        try {
            mmview.setCallback(callback);
            mmaction = (TextView) findViewById(R.id.mmaction);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //手动指定方向动作指令发布
    private CircleViewByImage.ActionCallback callback = new CircleViewByImage.ActionCallback() {
        @Override
        public void forwardMove() {
            showAction("Up");
            publishmessageplus(mqtt_pub_wsad, "{\"WSAD\":10001}");
        }

        @Override
        public void backMove() {
            showAction("Down");
            publishmessageplus(mqtt_pub_wsad, "{\"WSAD\":11000}");
        }

        @Override
        public void leftMove() {
            showAction("Left");
            publishmessageplus(mqtt_pub_wsad, "{\"WSAD\":10010}");
        }

        @Override
        public void rightMove() {
            showAction("Right");
            publishmessageplus(mqtt_pub_wsad, "{\"WSAD\":10100}");
        }

        @Override
        public void centerMove() {
//                    showAction("Move a little in center area");
        }

        @Override
        public void centerClick() {
//                    showAction("Click on center area");
//                    publishmessageplus(mqtt_pub_mode,"{\"Mode\":0}");
        }

        @Override
        public void actionUp() {
            showAction("Action up");
            publishmessageplus(mqtt_pub_wsad, "{\"WSAD\":10000}");
        }
    };
    //测试手柄用到的信息提示，可实时反应客户端的手动指令
    private String getCurTime() {
        return new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(System.currentTimeMillis());
    }
    private void showAction(String string) {
        mmaction.setText(text.length() > 2000 ? text = "" : (text = (getCurTime() + tab + string + newLine + text)));
    }

    //【UI右上两按钮单独写出点击函数】
    //记录水质数据函数
    public void insert(View source) {
        String sql = "insert into user(采样点,时间,水质)values(?,?,?)";
        database.execSQL(sql,
                new Object[]{selectedPoint.getText().toString(),
                        mTextClock.getText().toString(),
                        text_nacl.getText().toString()});
        Toast.makeText(MainActivity.this, "数据记录完成", Toast.LENGTH_SHORT).show();
    }
    //清空数据库函数
    public void delete(View source) {
        String sql = "delete from user";
        database.execSQL(sql, new String[]{});
    }
    //水质数据的界面传值 将cursor表里的信息处理变成一个3xN的ArrayList 3列
    private ArrayList<Map<String, String>> cursorCursorToList(Cursor cursor) {
        ArrayList<Map<String, String>> result = new ArrayList<>();
        while (cursor.moveToNext()) {
            Map<String, String> map = new HashMap<>();
            map.put("point", cursor.getString(0));
            map.put("time", cursor.getString(1));
            map.put("nacl", cursor.getString(2));
            result.add(map);
        }
        return result;
    }

    //无人艇实时动态连线【待优化……】
    private void showPathline(List<LatLng> points){
        OverlayOptions mOverlayOptions = new PolylineOptions()
                .width(10)
                .color(0xAAFF0000)
                .points(points);
        //在地图上绘制折线
        //mPloyline 折线对象
        Overlay mPolyline = BD_View.addOverlay(mOverlayOptions);
    }

    //地图定位与导航函数【待优化……GPS纠偏】
    private void navigateTo(double lon, double lat, float dir) {
        if(isFirstLocate){
            LatLng P = new LatLng(30.52530678,114.4362567);                                  //无人艇初始化位姿
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(P);
            BD_View.animateMapStatus(update);
            update = MapStatusUpdateFactory.zoomTo(18f);
            BD_View.animateMapStatus(update);
            isFirstLocate=false;
        }

        CoordinateConverter converter  = new CoordinateConverter()
                .from(CoordinateConverter.CoordType.GPS)
                .coord(new LatLng(lat,lon));

        LatLng P = converter.convert();
//        GpsCorrect ptWGS = new GpsCorrect();
//        LatLng P = ptWGS.WGS2BD(lon,lat);     //GPS纠偏，地球坐标系转百度坐标系
        MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
        locationBuilder.longitude(P.longitude);
        locationBuilder.latitude(P.latitude);
        locationBuilder.direction(dir);
        MyLocationData locationData = locationBuilder.build();
        BD_View.setMyLocationData(locationData);
        BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory.fromResource(R.drawable.boat);
        MyLocationConfiguration config = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, mCurrentMarker);
        BD_View.setMyLocationConfigeration(config);                  //地图显示定位图标
        //轨迹显示【可用！】
//        if(P.latitude!=0&&P.longitude!=0){
//            USV_points.add(new LatLng(P.latitude,P.longitude));
//        }
//        if(USV_points.size()>3){
//            showPathline(USV_points);
//        }
    }

    // 全屏显示【已注释】
    private void fullScreenConfig() {
        // 去除ActionBar
        // 如果该类 extends Activity，使用下面这句
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 如果该类 extends AppCompatActivity，使用下面这句
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        //去除状态栏，如 电量、Wifi信号等
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
    //水质数据库释放资源接口 + 百度地图的生命周期
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null) {
            database.close();
        }
        if (waterDataBase != null) {
            waterDataBase.close();
        }
        if(null!= mapView){
            mapView.onDestroy();
        }
        BD_View.setMyLocationEnabled(false);
        mLocationClient.stop();
    }
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    //主界面控件的声明
    private void ui_init() {
        show_map =findViewById(R.id.image_map);
        mapView = findViewById(R.id.BD_map);
        pump_off = findViewById(R.id.image_closepump);
        pump_on = findViewById(R.id.image_openpump);
        self_motion = findViewById(R.id.image_hangxiang);
        operation = findViewById(R.id.image_handle);
        text_lon = findViewById(R.id.text_jingdu);
        text_lat = findViewById(R.id.text_weidu);
        txt_onoff = findViewById(R.id.txt_onoff);
        text_head = findViewById(R.id.text_hangxiang);
        text_speed = findViewById(R.id.text_hangsu);
        text_nacl = findViewById(R.id.text_yandu);
        Pump_Switch = findViewById(R.id.Pump_Switch);
        ly_test = findViewById(R.id.water_quality);
        USV_Rg = findViewById(R.id.rg_boat);
        Sample_Rg = findViewById(R.id.rg_aim);
        homeward = findViewById(R.id.rb_homeward);
        HKVision = findViewById(R.id.HK);
        mTextClock = findViewById(R.id.textClock);
        planPath = findViewById(R.id.rb_Planning);

        layout_whool = findViewById(R.id.layout_whool);
        mmview = findViewById(R.id.joystick);
        data_bases = findViewById(R.id.databases);
        Mode_Change = findViewById(R.id.Mode_Change);
        txt_mode = findViewById(R.id.txt_mode);
        txt_GB = findViewById(R.id.web_GB);
        mSwitch = findViewById(R.id.main_switch);

        //通讯相关函数
        Mqtt_init();
        Mqtt_connect();
        startReconnect();
    }

    //一下均是通讯相关函数的定义
    private void Mqtt_init() {
        try {
            //host为主机名，test为clientid即连接MQTT的客户端ID，一般以客户端唯一标识符表示，MemoryPersistence设置clientid的保存形式，默认为以内存保存
            client = new MqttClient(host, mqtt_id,
                    new MemoryPersistence());
            //MQTT的连接设置
            options = new MqttConnectOptions();
            //设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
            options.setCleanSession(false);
            //设置连接的用户名
            options.setUserName(userName);
            //设置连接的密码
            options.setPassword(passWord.toCharArray());
            // 设置超时时间 单位为秒
            options.setConnectionTimeout(70);
            // 设置会话心跳时间 单位为秒 服务器会每隔1.5*100秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
            options.setKeepAliveInterval(100);
            //设置回调
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    //连接丢失后，一般在这里面进行重连
                    System.out.println("connectionLost----------");
                    //startReconnect();
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    //publish后会执行到这里
                    System.out.println("deliveryComplete---------"
                            + token.isComplete());
                }

                @Override
                public void messageArrived(String topicName, MqttMessage message)
                        throws Exception {
                    //subscribe后得到的消息会执行到这里面
                    System.out.println("messageArrived----------");
                    Message msg = new Message();
                    msg.what = 3;   //收到消息标志位
                    msg.obj = topicName + "---" + message.toString();
                    handler.sendMessage(msg);    // hander 回传
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void Mqtt_connect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!(client.isConnected()))  //如果还未连接
                    {
                        client.connect(options);
                        Message msg = new Message();
                        msg.what = 31;
                        handler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Message msg = new Message();    //连接成功
                    msg.what = 30;
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }

    private void startReconnect() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (!client.isConnected()) {
                    Mqtt_connect();
                }
            }
        }, 0 * 1000, 10 * 1000, TimeUnit.MILLISECONDS);
    }

    private void publishmessageplus(String topic, String message2) {
        if (client == null || !client.isConnected()) {
            return;
        }
        MqttMessage message = new MqttMessage();
        message.setPayload(message2.getBytes());
        try {
            client.publish(topic, message);
        } catch (MqttException e) {

            e.printStackTrace();
        }

    }

}