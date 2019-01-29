package com.example.ch225253.baidumap;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

public class IpSetActivity extends AppCompatActivity {
    private LinearLayout linearLayout;
    private EditText editText3, editText4;
    private Button button_ip;
    public String ip = "127.0.0.1";
    public int port = 8989;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
        editText3 = (EditText) findViewById(R.id.editText3);
        editText4 = (EditText) findViewById(R.id.editText4);
        button_ip = (Button) findViewById(R.id.button_ip);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        Intent intent = getIntent();
        ip = intent.getStringExtra("item_ip");
        port = intent.getIntExtra("port",8989);
        editText3.setHint(ip);
//        editText4.setHint(port);

        button_ip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ip = editText3.getText().toString();
                if ("".equals(ip)) {
                    ip = "127.0.0.1";
                }
                editText3.setText(ip);
                String s = editText4.getText().toString();
                if ("".equals(s)) {
                    s = "8989";
                }
                port = Integer.parseInt(s);
                editText4.setText(s);
                button_ip.setText("已保存");
                button_ip.setTextColor(Color.parseColor("#80ffffff"));
                button_ip.setEnabled(false);

            }
        });

    }
    //标题栏返回
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent();
                intent.putExtra("item_ip",ip);
                intent.putExtra("port", port);
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    //系统返回
    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("item_ip", ip);
        intent.putExtra("port", port);
        setResult(RESULT_OK, intent);
        finish();
    }
}


