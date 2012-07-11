package com.kokufu.android.sample.sqliteviewer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String DB_NAME = "dummy.db";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        findViewById(R.id.button_my_uri).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startSqliteViewer(
                        Uri.parse("content://com.kokufu.android.provider.sqliteviewer"));
            }
        });

        findViewById(R.id.button_my_file).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Copy the database file to accessible space
                File srcFile = new File(Environment.getDataDirectory(), "data/" + getPackageName() + "/databases/" + DB_NAME);
                File destFile = new File(getFilesDir() + "/" + DB_NAME);

                FileOutputStream outputStream = null;
                try {
                    outputStream = openFileOutput(DB_NAME, Context.MODE_WORLD_READABLE);
                } catch (FileNotFoundException e) {
                    String message = getString(R.string.error_file_not_found, destFile.getPath());
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                    return;
                }

                if (copyFromFileToStream(srcFile, outputStream)) {
                    // Send the database path to SQLiteViewer
                    startSqliteViewer(Uri.fromFile(destFile));
                } else {
                    String message = getString(R.string.error_file_not_found, srcFile.getPath());
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                }
            }
        });

        findViewById(R.id.button_image).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startSqliteViewer(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            }
        });

        findViewById(R.id.button_audio).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startSqliteViewer(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
            }
        });

        findViewById(R.id.button_video).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startSqliteViewer(MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            }
        });
    }

    /**
     * Send an intent to SqliteViewer.
     *
     * @param uri data on intent
     */
    private void startSqliteViewer(Uri uri) {
        Intent intent = new Intent(
                Intent.ACTION_VIEW,
                uri);

        // This category must be added
        intent.addCategory("com.kokufu.intent.category.APP_DB_VIEWER");

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            AlertDialog.Builder ab = new AlertDialog.Builder(this)
            .setMessage(Html.fromHtml(getString(R.string.error_sqliteviewer_not_installed)))
            .setPositiveButton("Yes", new Dialog.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=com.kokufu.android.apps.sqliteviewer.free"));
                    startActivity(intent);
                }
            })
            .setNegativeButton("No", null);
            ab.show();
        }
    }

    private static boolean copyFromFileToStream(File srcFile, OutputStream openedOutputStream) {
        try {
            InputStream in = new FileInputStream(srcFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            try {
                while ((bytesRead = in.read(buffer)) >= 0) {
                    openedOutputStream.write(buffer, 0, bytesRead);
                }
            } finally {
                in.close();
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
