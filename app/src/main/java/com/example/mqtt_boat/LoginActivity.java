package com.example.mqtt_boat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private DBOpenHelper mDBOpenHelper;
    private TextView mBtLoginactivityRegister;
    private EditText mEtLoginactivityUsername;
    private EditText mEtLoginactivityPassword;
    private Button mBtLoginactivityLogin;
    private ImageView im_school_log;
    private int Administrator =0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initView();
        im_school_log.setOnLongClickListener(new MyOnLongClickListenr());
        mDBOpenHelper = new DBOpenHelper(this);
    }

    private void initView() {
        // 初始化控件
        mBtLoginactivityLogin = findViewById(R.id.btn_et);
        mBtLoginactivityRegister = findViewById(R.id.btn_enroll);
        mEtLoginactivityUsername = findViewById(R.id.userID);
        mEtLoginactivityPassword = findViewById(R.id.passkey);
        im_school_log = findViewById(R.id.school_log);


        // 设置点击事件监听器
        mBtLoginactivityLogin.setOnClickListener(this);
        mBtLoginactivityRegister.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 跳转到注册界面
            case R.id.btn_enroll:
                startActivity(new Intent(this, RegisterActivity.class));
                finish();
                break;
            /**
             * 登录验证：
             *
             * 从EditText的对象上获取文本编辑框输入的数据，并把左右两边的空格去掉
             *  String name = mEtLoginactivityUsername.getText().toString().trim();
             *  String password = mEtLoginactivityPassword.getText().toString().trim();
             *  进行匹配验证,先判断一下用户名密码是否为空，
             *  if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(password))
             *  再进而for循环判断是否与数据库中的数据相匹配
             *  if (name.equals(user.getName()) && password.equals(user.getPassword()))
             *  一旦匹配，立即将match = true；break；
             *  否则 一直匹配到结束 match = false；
             *
             *  登录成功之后，进行页面跳转：
             *
             *  Intent intent = new Intent(this, MainActivity.class);
             *  startActivity(intent);
             *  finish();//销毁此Activity
             */
            case R.id.btn_et:
                String name = mEtLoginactivityUsername.getText().toString().trim();
                String password = mEtLoginactivityPassword.getText().toString().trim();
                if ((!TextUtils.isEmpty(name) && !TextUtils.isEmpty(password)) || Administrator == 1) {
                    ArrayList<User> data = mDBOpenHelper.getAllData();
                    boolean match = false;
                    if(Administrator == 1) {
                        match = true;
                    }
                    else {
                        for (int i = 0; i < data.size(); i++) {
                            User user = data.get(i);
                            if (name.equals(user.getName()) && password.equals(user.getPassword())) {
                                match = true;
                                break;
                            } else {
                                match = false;
                            }
                        }
                    }
                    if (match) {
                        Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, MainActivity.class);
                        startActivity(intent);
                        finish();//销毁此Activity
                    } else {
                        Toast.makeText(this, "用户名或密码不正确，请重新输入", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "请输入你的用户名或密码", Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }

    //为方便开发调试，设置长按触发免账户登录
    private class MyOnLongClickListenr implements View.OnLongClickListener {
        @Override
        public boolean onLongClick(View v) {
            if (v.getId() == R.id.school_log) {
                Toast.makeText(LoginActivity.this, "已开启调试限权,可免账户登录", Toast.LENGTH_SHORT).show();
                Administrator = 1;
            }
            return true;
        }
    }

}