package com.legendsayantan.xcrypt;

import static android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static android.hardware.biometrics.BiometricManager.Authenticators.DEVICE_CREDENTIAL;
import static android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.borutsky.neumorphism.NeumorphicFrameLayout;
import com.google.gson.Gson;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;

import dev.shreyaspatil.MaterialDialog.BottomSheetMaterialDialog;

public class CryptActivity extends AppCompatActivity {
    EditText key;
    TextView info;
    CheckBox encrypt,decrypt,delete;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    NeumorphicFrameLayout xcr;
    LinearLayout button;
    ScrollView sview;
    boolean cryptrun;
    ArrayList<String> files, results ;
    public AsyncTask<Void, Void, Void> taskA;
    int success,fail,skip;
    long time,resetTime;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crypt);
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.theme));
        sharedPreferences= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor=sharedPreferences.edit();
        key=findViewById(R.id.editTextTextPassword);
        encrypt=findViewById(R.id.encrypt);
        decrypt=findViewById(R.id.decrypt);
        delete=findViewById(R.id.delete);
        xcr=findViewById(R.id.startbutton);
        info=findViewById(R.id.info);
        sview=findViewById(R.id.status);
        button=findViewById(R.id.button);
        encrypt.setChecked(sharedPreferences.getBoolean("encrypt",false));
        encrypt.setOnCheckedChangeListener((buttonView, isChecked) -> editor.putBoolean("encrypt",isChecked).apply());
        decrypt.setChecked(sharedPreferences.getBoolean("decrypt",false));
        decrypt.setOnCheckedChangeListener((buttonView, isChecked) -> editor.putBoolean("decrypt",isChecked).apply());
        delete.setChecked(sharedPreferences.getBoolean("delete",false));
        delete.setOnCheckedChangeListener((buttonView, isChecked) -> editor.putBoolean("delete",isChecked).apply());
        key.setText(sharedPreferences.getString("key",""));
        key.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                editor.putString("key",s.toString()).apply();
            }
        });
        xcr.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(cryptrun)return false;
                if(motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                    xcr.setState(NeumorphicFrameLayout.State.PRESSED);
                }
                if(motionEvent.getAction()==MotionEvent.ACTION_UP){
                    xcr.setState(NeumorphicFrameLayout.State.FLAT);
                    try {
                        startTasks();
                    }catch (Exception e){
                        reportException(e);
                    }
                }
                return true;
            }
        });
        try {
            if(!getIntent().getStringArrayListExtra("files").toString().equals("")){
                files=getIntent().getStringArrayListExtra("files");
            }
            permission();
            success=fail=skip=0;
        }catch (Exception e){
            reportException(e);
        }

    }
    public void Xcrypt(){
        if(key.getText().toString().length()!=16){
            key.post(new Runnable() {
                @Override
                public void run() {
                    key.setError("please set 16 digit crypt key");
                }
            });
            return;
        }
        if(!(encrypt.isChecked()||decrypt.isChecked())){
            encrypt.post(new Runnable() {
                @Override
                public void run() {
                    encrypt.setError("Please select at least one operation");
                    decrypt.setError("Please select at least one operation");
                }
            });
            return;
        }
        results=files;
        String fileText = files.size()>1?"files":"file";
        printInfo("Starting Xcrypt for "+files.size()+" "+fileText+"...",true);
        time=System.currentTimeMillis();
        resetTime=System.currentTimeMillis();
        for(String str : files){
            String[] parts = str.split("\\.");
            String ext = parts[parts.length-1];
            if(ext.contains("xcrypt_")){
                if(decrypt.isChecked()) {
                    printInfo("Decrypting " + str);
                    try {
                        CryptoUtils.decrypt(key.getText().toString(), new File(str), new File(str.replace("xcrypt_", "")));
                        if (new File(str.replace("xcrypt_", "")).exists()) {
                            printInfo("Decrypted " + str +", took "+getResetTime());
                            success++;
                            if (delete.isChecked()) {
                                if (new File(str).delete()) {
                                    results.set(results.indexOf(str), str.replace("xcrypt_", ""));
                                    printInfo("Deleted file " + str);
                                }
                            }
                        }

                    } catch (CryptoException e) {
                        fail++;
                        printInfo("Error decrypting file " + str + "\n" + e.getMessage() + "-" + e.getCause());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                FancyToast.makeText(getApplicationContext(),e.getMessage(),FancyToast.LENGTH_SHORT,FancyToast.ERROR,false).show();
                            }
                        });
                    }
                }else {
                    printInfo("Skipped decryption "+str);
                    skip++;
                }
            }else {
                if (encrypt.isChecked()) {
                    printInfo("Encrypting " + str);
                    try {
                        CryptoUtils.encrypt(key.getText().toString(), new File(str), new File(str.replace(ext, "xcrypt_" + ext)));
                        if (new File(str.replace(ext, "xcrypt_" + ext)).exists()) {
                            printInfo("Encrypted " + str +", took "+getResetTime());
                            success++;
                            if (delete.isChecked()) {
                                if (new File(str).delete()) {
                                    results.set(results.indexOf(str), str.replace(ext, "xcrypt_" + ext));
                                    printInfo("Deleted file " + str);
                                }
                            }
                        }
                    } catch (CryptoException e) {
                        fail++;
                        printInfo("Error encrypting file " + str + "\n" + e.getMessage() + "-" + e.getCause());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                FancyToast.makeText(getApplicationContext(),e.getMessage(),FancyToast.LENGTH_SHORT,FancyToast.ERROR,false).show();
                            }
                        });
                    }
                }else {
                    printInfo("Skipped encryption "+str);
                    skip++;
                }
            }
        }
        printInfo("Finished Xcrypt in "+formatTime(System.currentTimeMillis()-time)+"\n("+success+" successful, "+fail+" failed, "+skip+" skipped).");
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sview.fullScroll(View.FOCUS_DOWN);
            }
        },2000);

        saveArrayList(results,"filepaths");
    }
    public void permission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()){
                Toast.makeText(getApplicationContext(), "Grant the permission for xcrypt",Toast.LENGTH_LONG).show();
                startActivity(new Intent(ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
            else {
            }
        }else{
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
            }
            else {
            }
        }
    }
    @SuppressLint("StaticFieldLeak")
    public void createTaskA() {
        taskA = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                Xcrypt();
                return null;
            }
        };
    }
    public void startTasks() {
        createTaskA();
        taskA.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    public void printInfo(String information){
        sview.post(() -> {
            cryptrun=true;
            sview.setVisibility(View.VISIBLE);
            button.setVisibility(View.GONE);
            info.setText(info.getText()+"\n\n"+information);
            sview.fullScroll(View.FOCUS_DOWN);
        });
    }
    public void printInfo(String information,boolean ignored){
        sview.post(() -> {
            cryptrun=true;
            sview.setVisibility(View.VISIBLE);
            button.setVisibility(View.GONE);
            info.setText(info.getText()+"\n"+information);
            sview.fullScroll(View.FOCUS_DOWN);
        });
    }
    @Override
    public void onLowMemory() {
        FancyToast.makeText(getApplicationContext(),"Low memory available, cannot process",FancyToast.LENGTH_LONG,FancyToast.WARNING,false).show();
        super.onLowMemory();
    }
    public void reportException(Exception e){
        startActivity(new Intent(getApplicationContext(),ExceptionActivity.class).putExtra("exception",getClass().getName()+"\n"+e.getMessage()+"\n"+e.getLocalizedMessage()+"\n"+e.getCause()));
    }
    public String formatTime(long ms){
        if(ms>1000){
            ms=ms/1000;
            if(ms>60){
                String s= String.valueOf(ms);
                ms=ms/60;
                if(ms>60){
                    String m= String.valueOf(ms);
                    return ms+" h "+m+" m "+s+" s";
                }else return ms+" m "+s+" s";
            }else return ms+" s";
        }else return ms+" ms";
    }
    public String getResetTime(){
        String ret = formatTime(System.currentTimeMillis()-resetTime);
        resetTime=System.currentTimeMillis();
        return ret;
    }
    public void saveArrayList(ArrayList<String> list, String key){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(key, json);
        editor.apply();

    }
}