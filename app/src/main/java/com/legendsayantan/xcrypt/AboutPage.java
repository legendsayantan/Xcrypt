package com.legendsayantan.xcrypt;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.SpannedString;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.borutsky.neumorphism.NeumorphicFrameLayout;
import com.google.gson.Gson;

import java.util.ArrayList;

import dev.shreyaspatil.MaterialDialog.AbstractDialog;
import dev.shreyaspatil.MaterialDialog.BottomSheetMaterialDialog;
import dev.shreyaspatil.MaterialDialog.interfaces.DialogInterface;

public class AboutPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_page);
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.theme));
        TextView link = findViewById(R.id.textViewlink);
        TextView ver = findViewById(R.id.textVersion);
        NeumorphicFrameLayout delete = findViewById(R.id.delete);
        NeumorphicFrameLayout web = findViewById(R.id.web);
        NeumorphicFrameLayout code = findViewById(R.id.code);
        ver.setText("Version "+BuildConfig.VERSION_NAME);
        link.setClickable(true);
        link.setMovementMethod(LinkMovementMethod.getInstance());
        String text = "<a>From </a><a href='https://github.com/legendsayantan'>LegendSayantan.</a>";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            link.setText(Html.fromHtml(text,Html.FROM_HTML_MODE_COMPACT));
        }else{
            link.setText(Html.fromHtml(text));
        }
        new nAnimator(delete, NeumorphicFrameLayout.State.PRESSED, new Runnable() {
            @Override
            public void run() {
                new BottomSheetMaterialDialog.Builder(AboutPage.this)
                        .setTitle("Dangerous Operation")
                        .setMessage("Are you sure to Delete all saved Filepaths? This action cannot be undone.")
                        .setPositiveButton("Delete", (dialogInterface, which) -> {
                            MainActivity.filepaths = new ArrayList<>();
                            dialogInterface.cancel();
                        })
                        .setNegativeButton("Cancel", (dialogInterface, which) -> {
                            dialogInterface.cancel();
                        }).setCancelable(true).build().show();
            }
        });
        new nAnimator(web, NeumorphicFrameLayout.State.PRESSED, new Runnable() {
            @Override
            public void run() {
                Intent in = new Intent();
                in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                in.setData(Uri.parse("https://legendsayantan.github.io/xcrypt"));
                startActivity(in);
            }
        });
        new nAnimator(code, NeumorphicFrameLayout.State.PRESSED, new Runnable() {
            @Override
            public void run() {
                Intent in = new Intent();
                in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                in.setData(Uri.parse("https://github.com/legendsayantan/xcrypt"));
                startActivity(in);
            }
        });
    }
}