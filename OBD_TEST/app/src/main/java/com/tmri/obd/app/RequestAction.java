package com.tmri.obd.app;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tmri.obd.app.util.APPUtil;
import com.tmri.obd.app.util.PermissionsUtils;

import java.util.ArrayList;
import java.util.List;

public class RequestAction extends AppCompatActivity {
    String cylsh = "";
    String clsbdh = "";
    Intent intent = new Intent();

    private static final int REQUEST_ID = 1001;
    private String mPackageName = "com.cnlaunch.x431.eVin";
    private String mActivityName = "com.cnlaunch.x431pro.activity.diagnose.DiagnoseActivity";
    private boolean isAllSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PermissionsUtils.requestPermissionSDCard(this);//权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            appRequestPermission();
        }
        cylsh = getIntent().getStringExtra("cylsh");//流水号
        clsbdh = getIntent().getStringExtra("clsbdh");
        //缩小为一个像素
        initview();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                goYuanZhengApk(false);
            }
        } else {
            goYuanZhengApk(false);
        }
    }

    private void goYuanZhengApk(boolean flag) {
        isAllSearch = flag;
        if (!isWxInstall2()) {//先判断是否安装对应软件
            Toast.makeText(this, "不存在该应用！", Toast.LENGTH_SHORT).show();
        } else {
            ComponentName cn = new ComponentName(mPackageName, mActivityName);
            try {
                Intent intent = new Intent();
                intent.putExtra("diagnose_flag", "traditional");
                // true为全车扫描，false为快速扫描
                intent.putExtra("isScanAll", flag);
                intent.setComponent(cn);
                startActivityForResult(intent, REQUEST_ID);
            } catch (Exception e) {
                Log.e("lylog", "该应用err:" + e.toString());
                e.printStackTrace();
            }
        }
    }

    public boolean isWxInstall2() {
        PackageManager packageManager = getPackageManager();
        boolean hasInstallWx = false;
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(mPackageName, PackageManager.GET_GIDS);
            hasInstallWx = packageInfo != null;
        } catch (PackageManager.NameNotFoundException e) {
            hasInstallWx = false;
            e.printStackTrace();
        }
        return hasInstallWx;
    }

    private void initview() {
        Window window = getWindow();
        window.setGravity(Gravity.LEFT | Gravity.TOP);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.x = 0;
        layoutParams.y = 0;
        layoutParams.height = 1;
        layoutParams.width = 1;
        window.setAttributes(layoutParams);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PermissionsUtils.REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("lylog", "going!!!!!!");
                    goYuanZhengApk(false);
                } else {
                    finish();
                }
                break;
            default:

                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void appRequestPermission() {
        List<String> list = new ArrayList<>();
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            list.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            list.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (list.size() > 0) {
            ActivityCompat.requestPermissions(this, list.toArray(new String[list.size()]), 1);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        String vin = "NULL";
        if (resultCode != Activity.RESULT_CANCELED) {
            switch (requestCode) {
                case REQUEST_ID:
                    if (data != null && data.hasExtra("result")) {
                        String result = data.getStringExtra("result");
                        JsonParser parser = new JsonParser();
                        JsonObject jsons = (JsonObject) parser.parse(result);
                        String code = jsons.get("code").getAsString();

                        if (!isAllSearch && code != null && !code.equals("-1")) { //-1才是成功
                            goYuanZhengApk(true);
                            return;
                        }

                        if (isAllSearch && code != null && !code.equals("-1")) { //-1才是成功
                            showToast("code != -1,vin全车扫描获取失败 !");
                            APPUtil.write(cylsh + " , " + vin + "; --> vin全车扫描获取失败");
                            finish();
                            return;
                        }

                        JsonObject njson = (JsonObject) parser.parse(result);
                        if (null == njson.get("carCheckVinList")){
                            if(!isAllSearch){
                                goYuanZhengApk(true);
                                return;
                            }else {
                                showToast("result 结果里没有carCheckVinList字段");
                                APPUtil.write(cylsh + " , " + vin + "; --> result 结果里没有carCheckVinList字段");
                                finish();
                                return;
                            }
                        }
                        JsonArray vinList =  (JsonArray) njson.get("carCheckVinList").getAsJsonArray();

                        if(vinList.size()<= 0 ){
                            if(!isAllSearch){
                                goYuanZhengApk(true);
                                return;
                            }else {
                                showToast("carCheckVinList数组为空");
                                APPUtil.write(cylsh + " , " + vin + ";  --> carCheckVinList数组为空");
                                finish();
                                return;
                            }
                        }
                        JsonObject jb = (JsonObject) njson.get("carCheckVinList").getAsJsonArray().get(0);
                        if(null !=jb.get("currVin")){
                            vin = jb.get("currVin").getAsString();
                        }else{
                            vin = "";
                        }

                        intent.putExtra("cylsh", getIntent().getStringExtra("cylsh"));
                        intent.putExtra("clsbdh", getIntent().getStringExtra("clsbdh"));

                        if (vin != null && !vin.equals("")) vin = vin.trim();
                        if (vin == null || vin.equals("")) {
                            showToast("读取VIN数据为空");
                            intent.putExtra("code", "1");
                            intent.putExtra("message", "读取VIN数据为空！");
                        } else if (vin.equals(clsbdh)) {
                            showToast("合格");
                            intent.putExtra("code", "1");
                            intent.putExtra("message", "合格");
                        } else {
                            showToast("不合格");
                            intent.putExtra("code", "1");
                            intent.putExtra("message", "不合格");
                        }
                        if (getIntent().getStringExtra("keystr") != null) {
                            intent.putExtra("keystr", getIntent().getStringExtra("keystr"));
                        } else intent.putExtra("keystr", "");
                        Toast.makeText(this, "vin = " + vin, Toast.LENGTH_SHORT).show();
                        intent.putExtra("obd_clsbdh", vin);
                        setResult(Activity.RESULT_OK, intent);
                        APPUtil.write(cylsh + " , " + vin + ";");
                        finish();
                    } else {
                        if (!isAllSearch) {
                            showToast("获取result为空，进入全车扫描");
                            goYuanZhengApk(true);
                            return;
                        } else {
                            Toast.makeText(this, "全车扫描获取result为空 ", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                    break;
            }
        } else if (data != null && resultCode == Activity.RESULT_CANCELED && requestCode == REQUEST_ID) {//解决退出后，屏幕卡死问题
            Log.d("lylog", " data = " + data.getStringExtra("result"));
            intent.putExtra("code", "0");
            intent.putExtra("cylsh", getIntent().getStringExtra("cylsh"));
            intent.putExtra("clsbdh", getIntent().getStringExtra("clsbdh"));
            intent.putExtra("message", "读取VIN数据为空！");
            if (isAllSearch) {
                Toast.makeText(this, "RESULT_CANCELED，全车扫描失败result = " + data.getStringExtra("result"), Toast.LENGTH_SHORT).show();
                setResult(Activity.RESULT_CANCELED, intent);
                finish();
            } else {
                Toast.makeText(this, "RESULT_CANCELED，进入全车扫描  result = " + data.getStringExtra("result"), Toast.LENGTH_SHORT).show();
                goYuanZhengApk(true);
                return;
            }
        } else {
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }
    public void showToast(String s){
        Toast.makeText(this,s,Toast.LENGTH_SHORT).show();
    }
}
