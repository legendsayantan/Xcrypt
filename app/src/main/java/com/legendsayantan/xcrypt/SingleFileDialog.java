package com.legendsayantan.xcrypt;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;

import com.borutsky.neumorphism.NeumorphicFrameLayout;
import com.developer.filepicker.model.DialogConfigs;
import com.developer.filepicker.model.DialogProperties;
import com.developer.filepicker.view.FilePickerDialog;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class SingleFileDialog {
    String filepath = "";
    CheckBox delete;
    public static ArrayList<File> filesToDelete = new ArrayList<>();
    public SingleFileDialog(Activity context) {
        Dialog dialog = new Dialog(context);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View rootView = LayoutInflater.from(context).inflate(R.layout.dialog,null);
        dialog.setContentView(rootView);
        NeumorphicFrameLayout choose,close,start;
        EditText password = rootView.findViewById(R.id.edt);
        TextView textView = rootView.findViewById(R.id.textV);
        TextView starter = rootView.findViewById(R.id.startText);
        TextView title = rootView.findViewById(R.id.title);
        choose = rootView.findViewById(R.id.selection);
        close = rootView.findViewById(R.id.close);
        start = rootView.findViewById(R.id.open);
        delete = rootView.findViewById(R.id.delete);
        dialog.show();
        new nAnimator(choose, NeumorphicFrameLayout.State.PRESSED, new Runnable() {
            @Override
            public void run() {
                DialogProperties properties = new DialogProperties();
                properties.selection_mode = DialogConfigs.SINGLE_MODE;
                properties.selection_type = DialogConfigs.FILE_SELECT;
                properties.root = new File("storage/emulated/0/");
                properties.error_dir = new File("storage/emulated/0/");
                properties.offset = new File("storage/emulated/0/");
                properties.extensions = null;
                properties.show_hidden_files = true;
                FilePickerDialog dialog = new FilePickerDialog(context,properties);
                dialog.setTitle("Select a File for Xcrypt - Single File Operation");
                dialog.setDialogSelectionListener(files -> {
                    filepath=files[0];
                    textView.setText(new File(filepath).getName());
                    String[] parts = filepath.split("\\.");
                    String ext = parts[parts.length-1].trim();
                    if(ext.startsWith("xcrypt_")) {
                        starter.setText("Open File");
                        title.setText("Xcrypt - Decrypt and Open");
                    } else {
                        starter.setText("Share File");
                        title.setText("Xcrypt - Encrypt and Share");
                    }
                });
                dialog.show();
            }
        });
        new nAnimator(start, NeumorphicFrameLayout.State.PRESSED, () -> {
            if(filepath.equals("")){
                FancyToast.makeText(context,"Select a file first",FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show();
                return;
            }if(password.getText().toString().length()!=16){
                FancyToast.makeText(context,"Input 16 digit key",FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show();
                password.setError("Input 16 digit key");
                return;
            }
            processFile(filepath,password.getText().toString(),context);
        });
        new nAnimator(close, NeumorphicFrameLayout.State.PRESSED, () -> dialog.dismiss());
        dialog.setOnCancelListener(dialog1 -> {
            for(File f:filesToDelete)f.delete();
        });
        dialog.setOnDismissListener(dialog12 -> {
            for(File f:filesToDelete)f.delete();
        });
    }
    private void processFile(String str,String key,Activity activity){
        String[] parts = str.split("\\.");
        String ext = parts[parts.length-1].trim();
        String dl = "/storage/emulated/0/"+Environment.DIRECTORY_DOWNLOADS;
        File encOut = new File(dl,new File(str.replace("."+ext, ".xcrypt_" + ext)).getName());
        File decOut = new File(dl,new File(str.replace(".xcrypt_", ".")).getName());
        if(ext.startsWith("xcrypt_")){
            new Thread(() -> {
                activity.runOnUiThread(() -> FancyToast.makeText(activity,"Please wait , File is being decrypted",FancyToast.LENGTH_LONG,FancyToast.INFO,false).show());
                try {
                    if (!decOut.exists()) {
                        decOut.getParentFile().mkdirs();
                        decOut.createNewFile();
                    }
                } catch (IOException e) {
                    activity.runOnUiThread(() -> FancyToast.makeText(activity,"Failed to decrypt\n"+e.getLocalizedMessage(),FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show());
                }try{
                    CryptoUtils.decrypt(key,new File(str), decOut);
                    if(delete.isChecked())filesToDelete.add(decOut);
                    activity.runOnUiThread(() -> FancyToast.makeText(activity,"File saved in Downloads.",FancyToast.LENGTH_LONG,FancyToast.SUCCESS,false).show());
                    openFile(decOut,activity);
                } catch (CryptoException e) {
                    activity.runOnUiThread(() -> FancyToast.makeText(activity,"Failed to decrypt\n"+e.getLocalizedMessage(),FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show());
                }
            }).start();
        }else{
            new Thread(() -> {
                activity.runOnUiThread(() -> FancyToast.makeText(activity,"Please wait , File is being encrypted",FancyToast.LENGTH_LONG,FancyToast.INFO,false).show());
                try {
                    if (!encOut.exists()) {
                        encOut.getParentFile().mkdirs();
                        encOut.createNewFile();
                    }
                } catch (IOException e) {
                    activity.runOnUiThread(() -> FancyToast.makeText(activity,"Failed to decrypt\n"+e.getLocalizedMessage(),FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show());
                }
                try {
                    CryptoUtils.encrypt(key,new File(str),encOut);
                    if(delete.isChecked())filesToDelete.add(encOut);
                    activity.runOnUiThread(() -> FancyToast.makeText(activity,"File saved in Downloads.",FancyToast.LENGTH_LONG,FancyToast.SUCCESS,false).show());
                    shareFile(encOut,activity);
                } catch (CryptoException e) {
                    activity.runOnUiThread(() -> FancyToast.makeText(activity,"Failed to encrypt\n"+e.getLocalizedMessage(),FancyToast.LENGTH_LONG,FancyToast.ERROR,false).show());
                }
            }).start();
        }
    }
    private void shareFile(File file,Activity activity){
        ShareCompat.IntentBuilder.from(activity)
                .setStream(Uri.parse(file.getAbsolutePath()))
                .setType("*/*")
                .startChooser();
    }
    private void openFile(File file,Activity activity) {
        //Uri uri = Uri.fromFile(file);
        Uri uri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".fileProvider", file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, MimeTypeMap.getSingleton().getExtensionFromMimeType(file.getName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(Intent.createChooser(intent, "Open " + file.getName() + " with ..."));
    }
}
