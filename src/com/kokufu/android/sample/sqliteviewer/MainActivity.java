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

    private static final String CATEGORY = "com.kokufu.intent.category.APP_DB_VIEWER";
    private static final String EXTRA_DB_TABLE = "com.kokufu.intent.extra.DB_TABLE";
    private static final String EXTRA_DB_COLUMNS = "com.kokufu.intent.extra.DB_COLUMNS";
    private static final String EXTRA_DB_SELECTION = "com.kokufu.intent.extra.DB_SELECTION";
    private static final String EXTRA_DB_SELECTION_ARGS = "com.kokufu.intent.extra.DB_SELECTION_ARGS";
    private static final String EXTRA_DB_SORT_ORDER = "com.kokufu.intent.extra.DB_SORT_ORDER";


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        findViewById(R.id.button_my_uri).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startSqliteViewer(
                        Uri.parse("content://com.kokufu.android.provider.sqliteviewer"),
                        null);
            }
        });

        findViewById(R.id.button_my_file).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                File destFile = copyDatabase();
                if (destFile != null) {
                    // Send the database path to SQLiteViewer
                    startSqliteViewer(Uri.fromFile(destFile), null);
                }
            }
        });

        findViewById(R.id.button_my_file_with_args).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                File destFile = copyDatabase();
                if (destFile != null) {
                    // These args can be used on SQLiteViewer Free ver. 0.4.2. and above.
                    Bundle extras = new Bundle();
                    extras.putString(EXTRA_DB_TABLE, "dummy_table");
                    extras.putStringArray(EXTRA_DB_COLUMNS, new String[] {"_id", "data"});
                    extras.putString(EXTRA_DB_SELECTION, "_id <= ?");
                    extras.putStringArray(EXTRA_DB_SELECTION_ARGS, new String[] {"2"});
                    extras.putString(EXTRA_DB_SORT_ORDER, "_id DESC");

                    // Send the database path to SQLiteViewer
                    startSqliteViewer(Uri.fromFile(destFile), extras);
                }

            }
        });

        findViewById(R.id.button_image).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startSqliteViewer(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null);
            }
        });

        findViewById(R.id.button_image_with_args).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // These args can be used on SQLiteViewer Free ver. 0.4.2. and above.
                Bundle extras = new Bundle();
                extras.putStringArray(EXTRA_DB_COLUMNS, new String[] {
                        MediaStore.Images.ImageColumns._ID,
                        MediaStore.Images.ImageColumns.DATA,
                        MediaStore.Images.ImageColumns.DISPLAY_NAME,
                        MediaStore.Images.ImageColumns.MIME_TYPE,
                        MediaStore.Images.ImageColumns.DATE_TAKEN});
                extras.putString(EXTRA_DB_SELECTION,
                        MediaStore.Images.ImageColumns.MIME_TYPE + " = ?");
                extras.putStringArray(EXTRA_DB_SELECTION_ARGS, new String[] {"image/jpeg"});
                extras.putString(EXTRA_DB_SORT_ORDER,
                        MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");

                startSqliteViewer(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, extras);
            }
        });

        findViewById(R.id.button_audio).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startSqliteViewer(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null);
            }
        });

        findViewById(R.id.button_video).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startSqliteViewer(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null);
            }
        });
    }

    /**
     * Send an intent to SqliteViewer.
     *
     * @param uri data on intent
     */
    private void startSqliteViewer(Uri uri, Bundle extras) {
        Intent intent = new Intent(
                Intent.ACTION_VIEW,
                uri);

        // This category must be added
        intent.addCategory(CATEGORY);

        if (extras != null) {
            intent.putExtras(extras);
        }

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

    /**
     * Copy the database file to accessible space.
     *
     * @return the accessible file
     */
    private File copyDatabase() {
        File srcFile = new File(Environment.getDataDirectory(), "data/" + getPackageName() + "/databases/" + DB_NAME);
        File destFile = new File(getFilesDir() + "/" + DB_NAME);

        FileOutputStream outputStream = null;
        try {
            outputStream = openFileOutput(DB_NAME, Context.MODE_WORLD_READABLE);
        } catch (FileNotFoundException e) {
            String message = getString(R.string.error_file_not_found, destFile.getPath());
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
            return null;
        }

        if (copyFromFileToStream(srcFile, outputStream)) {
            return destFile;
        } else {
            String message = getString(R.string.error_file_not_found, srcFile.getPath());
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
            return null;
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
