package com.legendsayantan.xcrypt;

import static android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION;
import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.borutsky.neumorphism.NeumorphicFrameLayout;
import com.developer.filepicker.model.DialogConfigs;
import com.developer.filepicker.model.DialogProperties;
import com.developer.filepicker.view.FilePickerDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.concurrent.Executor;

import dev.shreyaspatil.MaterialDialog.BottomSheetMaterialDialog;
import dev.shreyaspatil.MaterialDialog.MaterialDialog;

public class MainActivity extends AppCompatActivity {
    NeumorphicFrameLayout fileselect,start,done;
    ArrayList<String> filenames = new ArrayList<>();
    static ArrayList<String> filepaths = new ArrayList<>();
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    ListView listView ;
    TextView t1;
    private final int FILE_REQUEST_CODE=5804;
    public static SharedPreferences sharedPreferences;
    ConstraintLayout constraintLayout;
    ImageView single, about;
    EditText editText;
    static Context context ;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.theme));
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        fileselect=findViewById(R.id.logout);
        listView=findViewById(R.id.fileList);
        t1 = findViewById(R.id.textbox);
        start = findViewById(R.id.start);
        single =findViewById(R.id.singleOp);
        about =findViewById(R.id.about);
        constraintLayout=findViewById(R.id.oldLayout);
        editText=findViewById(R.id.keyText);
        done=findViewById(R.id.done);
        start.setOnTouchListener((view, motionEvent) -> {
            if(motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                start.setState(NeumorphicFrameLayout.State.PRESSED);
            }
            if(motionEvent.getAction()==MotionEvent.ACTION_UP){
                start.setState(NeumorphicFrameLayout.State.FLAT);
                try{
                    permission();
                    initVerify();
                }catch (Exception e){
                    reportException(e);
                }
            }
            return true;
        });
        fileselect.setOnTouchListener((view, motionEvent) -> {
            if(motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                fileselect.setState(NeumorphicFrameLayout.State.PRESSED);
            }
            if(motionEvent.getAction()==MotionEvent.ACTION_UP){
                fileselect.setState(NeumorphicFrameLayout.State.FLAT);
                try{
                    getFile();
                }catch (Exception e){
                    reportException(e);
                }
            }
            return true;
        });
        single.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 96);
            }else if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 96);
                new SingleFileDialog(MainActivity.this);
            }else new SingleFileDialog(MainActivity.this);
        });
        try{
            if(getArrayList("filepaths")!=null)
                filepaths=getArrayList("filepaths");
            refresh();
            permission();
        }catch (Exception e){
            reportException(e);
        }
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),AboutPage.class));
            }
        });
        context=getApplicationContext();
    }

    @Override
    protected void onResume() {
        refresh();
        super.onResume();
    }

    public void getFile(){
        if(!permission())return;
        new BottomSheetMaterialDialog.Builder(this)
                .setTitle("Choose from images or other files?")
                .setCancelable(true)
                .setPositiveButton("Other files", (dialogInterface, which) -> {
                    dialogInterface.dismiss();
                    DialogProperties properties = new DialogProperties();
                    properties.selection_mode = DialogConfigs.MULTI_MODE;
                    properties.selection_type = DialogConfigs.FILE_SELECT;
                    properties.root = new File("storage/emulated/0/");
                    properties.error_dir = new File("storage/emulated/0/");
                    properties.offset = new File("storage/emulated/0/");
                    properties.extensions = null;
                    properties.show_hidden_files = true;
                    FilePickerDialog dialog = new FilePickerDialog(MainActivity.this,properties);
                    dialog.setTitle("Select Files for Xcrypt");
                    dialog.setDialogSelectionListener(files -> {
                        for(String s : files)if(!filepaths.contains(s))filepaths.add(s);
                        saveArrayList(filepaths,"filepaths");
                        refresh();
                    });
                    dialog.show();
                })
                .setNegativeButton("Images", (dialogInterface, which) -> {
                    dialogInterface.dismiss();
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*"); //allows any file type. Change * to specific extension to limit it
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    startActivityForResult(Intent.createChooser(intent, "Select Files to Xcrypt"), FILE_REQUEST_CODE);
                }).build().show();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case FilePickerDialog.EXTERNAL_READ_PERMISSION_GRANT: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try{
                        getFile();
                    }catch (Exception e){
                        reportException(e);
                    }
                } else {
                    FancyToast.makeText(MainActivity.this, "Permission is Required for getting list of files", FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show();
                }
            }
        }
    }
    public void refresh(){
        filenames.clear();
        for(String s: filepaths){
            filenames.add(new File(s).getName());
        }
        if(filepaths.toString().equals("[]")){
            t1.setText("No Files Selected");
            start.setVisibility(View.GONE);
        }else {
            t1.setText(filepaths.size()+" Files Selected");
            start.setVisibility(View.VISIBLE);
        }
        listView.setAdapter(new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, filenames));

        listView.setOnItemClickListener((parent, view, position, id) -> new BottomSheetMaterialDialog.Builder(MainActivity.this)
                .setTitle("Do you want to remove the file "+filenames.get(position)+" from list?")
                .setCancelable(true)
                .setPositiveButton("Remove", (dialogInterface, which) -> {
                    dialogInterface.dismiss();
                    filepaths.remove(position);
                    refresh();
                })
                .setNegativeButton("Cancel", (dialogInterface, which) -> dialogInterface.dismiss()).build().show());
        saveArrayList(filepaths,"filepaths");
    }
    public void saveArrayList(ArrayList<String> list, String key){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(key, json);
        editor.apply();
    }

    public ArrayList<String> getArrayList(String key){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        return gson.fromJson(json, type);
    }
    public boolean permission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()){
                FancyToast.makeText(getApplicationContext(), "Grant the permission for xcrypt",FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show();
                startActivity(new Intent(ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                return false;
            }
            else {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 5);
                return true;
            }
        }else{
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
                return false;
            }
            else {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 5);
                return true;
            }
        }
    }
    public void initVerify(){
        if(!permission())return;
        if(sharedPreferences.getString("key","").equals("")){
            finish();
            startActivity(new Intent(getApplicationContext(),CryptActivity.class).putExtra("files",filepaths));
            return;
        }
        boolean bio =false;
        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate(BIOMETRIC_STRONG | DEVICE_CREDENTIAL)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                bio=true;
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                bio=false;
                oldAuth();
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                bio=false;
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                new BottomSheetMaterialDialog.Builder(this)
                        .setTitle("Your device does not have screen lock.")
                        .setMessage("Add screen lock to continue or continue by entering last digits of your crypt key?")
                        .setPositiveButton("Screen Lock", (dialogInterface, which) -> {
                            dialogInterface.dismiss();
                            Toast.makeText(getApplicationContext(), "Add device screen lock to continue", Toast.LENGTH_SHORT).show();
                            final Intent enrollIntent;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                enrollIntent = new Intent(Settings.ACTION_BIOMETRIC_ENROLL);
                                enrollIntent.putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                        BIOMETRIC_STRONG | DEVICE_CREDENTIAL);
                                startActivityForResult(enrollIntent, 02);
                            }else{
                                FancyToast.makeText(getApplicationContext(),"Go to about and single device lock to continue",FancyToast.LENGTH_LONG,FancyToast.WARNING,false).show();
                            }
                        }).setNegativeButton("Xcrypt key", (dialogInterface, which) -> {
                            dialogInterface.dismiss();
                            oldAuth();
                        }).build().show();
                return;
        }
        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                if(errString.toString().equals("Use Password")){
                    KeyguardManager km = (KeyguardManager)getSystemService(KEYGUARD_SERVICE);
                    Intent i = km.createConfirmDeviceCredentialIntent("Verify it's you","Verify using screen lock credentials");
                    startActivityForResult(i,120);
                }
                FancyToast.makeText(getApplicationContext(), "Verify yourself to continue",
                        FancyToast.LENGTH_LONG,FancyToast.ERROR,false)
                        .show();
            }
            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                finish();
                startActivity(new Intent(getApplicationContext(),CryptActivity.class).putExtra("files",filepaths));
            }
            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }
        });
        if(Build.VERSION.SDK_INT==28||Build.VERSION.SDK_INT==29){
            if(bio) {
                promptInfo = new BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Verify it's you")
                        .setSubtitle("Verify using screen lock credentials")
                        .setAllowedAuthenticators(BIOMETRIC_STRONG)
                        .setNegativeButtonText("Use Password")
                        .build();
                biometricPrompt.authenticate(promptInfo);
            } else {
                KeyguardManager km = (KeyguardManager)getSystemService(KEYGUARD_SERVICE);
                Intent i = km.createConfirmDeviceCredentialIntent("Verify it's you","Verify using screen lock credentials");
                startActivityForResult(i,120);
            }
        }else{
            promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Verify it's you")
                    .setSubtitle("Verify using screen lock credentials")
                    .setAllowedAuthenticators(BIOMETRIC_STRONG | DEVICE_CREDENTIAL)
                    .build();
            biometricPrompt.authenticate(promptInfo);
        }
    }
    public void reportException(Exception e){
        startActivity(new Intent(getApplicationContext(),ExceptionActivity.class).putExtra("exception",getClass().getName()+"\n"+e.getMessage()+"\n"+e.getLocalizedMessage()+"\n"+e.getCause()));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode==120) {
            if (resultCode == RESULT_OK) {
                finish();
                startActivity(new Intent(getApplicationContext(), CryptActivity.class).putExtra("files", filepaths));
            }
        }
        else if(requestCode == FILE_REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK) {
                permission();
                if(data.getClipData() != null) {
                    for(int i = 0; i<data.getClipData().getItemCount();i++){
                        Uri uri = data.getClipData().getItemAt(i).getUri();
                        String filePath =getPathFromURI(uri);
                        if(!filePath.isEmpty())
                        if(!filepaths.contains(filePath))filepaths.add(filePath);
                        refresh();
                    }
                }else if(data.getData() != null) {
                    Uri uri = data.getData();
                    String filePath =getPathFromURI(uri);
                    if(!filePath.isEmpty())
                    if(!filepaths.contains(filePath))filepaths.add(filePath);
                    refresh();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    public String getPathFromURI(Uri uri){
        String realPath="";
        try {
            String wholeID = DocumentsContract.getDocumentId(uri);
            // Split at colon, use second item in the array
            String id = wholeID.split(":")[1];
            String[] column = { MediaStore.Images.Media.DATA };
            // where id is equal to
            String sel = MediaStore.Images.Media._ID + "=?";
            Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, column, sel, new String[]{ id }, null);
            int columnIndex = 0;
            if (cursor != null) {
                columnIndex = cursor.getColumnIndex(column[0]);
                if (cursor.moveToFirst()) {
                    realPath = cursor.getString(columnIndex);
                }
                cursor.close();
            }
        }catch (Exception e){
            FancyToast.makeText(getApplicationContext(),"Please choose file only from default chooser",FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show();
        }
        return realPath;
    }
    public void oldAuth(){
        constraintLayout.setVisibility(View.VISIBLE);
        new nAnimator(done, NeumorphicFrameLayout.State.PRESSED, () -> {
            if(sharedPreferences.getString("key","").substring(sharedPreferences.getString("key","").length()-6).equals(editText.getText().toString())){
                finish();
                startActivity(new Intent(getApplicationContext(),CryptActivity.class).putExtra("files",filepaths));
            }else{
                editText.setError("Wrong Key");
            }
        });
    }

}