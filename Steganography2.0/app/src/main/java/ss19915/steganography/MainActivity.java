package ss19915.steganography;

import android.Manifest;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import static android.R.attr.delay;
import static android.R.attr.logo;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    //declare veriables
    public static Button addImageB,addFile,getFile,hideFile;
    private ImageButton addImage;
    public ProgressBar bar;
    public Intent imageIntent,fileIntent;
    private int writePermission=1,pictureFlag=2,fileFlag=3;
    public Bitmap image;
    public File msgFile;
    private TextView fileName;
    public static Activity act;
    public static Context cntx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize variables
        addImageB=(Button)findViewById(R.id.imgButton1);
        addImage=(ImageButton)findViewById(R.id.imgButton);
        fileName=(TextView)findViewById(R.id.fileName);
        addFile=(Button)findViewById(R.id.addFile);
        getFile=(Button)findViewById(R.id.getFile);
        hideFile=(Button)findViewById(R.id.hideFile);
        bar=(ProgressBar)findViewById(R.id.bar);
        bar.setMax(100);
        bar.setProgress(bar.getMax());
        act = this;
        cntx=getBaseContext();

        //initialize and set intent type
        imageIntent=new Intent();
        fileIntent=new Intent();
        imageIntent.setType("image/*");
        fileIntent.setType("*/*");
        imageIntent.setAction(Intent.ACTION_GET_CONTENT);
        fileIntent.setAction(Intent.ACTION_GET_CONTENT);

        //set action listener to buttons
        addFile.setOnClickListener(this);
        addImage.setOnClickListener(this);
        addImageB.setOnClickListener(this);
        getFile.setOnClickListener(this);
        hideFile.setOnClickListener(this);

        //disable buttons
        addFile.setEnabled(false);
        getFile.setEnabled(false);
        hideFile.setEnabled(false);


    }

    @Override
    public void onClick(View v) {//button click actions
            switch(v.getId()){
                case R.id.imgButton:
                case R.id.imgButton1:
                    if(bar.getProgress()==bar.getMax()){
                        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){//check write permission
                            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, writePermission);//ask write permission
                            Toast.makeText(MainActivity.this,"Image button",Toast.LENGTH_SHORT).show();
                        }
                        else{
                            bar.setVisibility(View.INVISIBLE);
                            startActivityForResult(Intent.createChooser(imageIntent,"Select Picture"),pictureFlag);//invoke an intent to choose image from device
                        }
                    }
                    else
                        Toast.makeText(MainActivity.this,"Please wait",Toast.LENGTH_SHORT).show();//show message to wait
                    break;
                case R.id.addFile:
                    startActivityForResult(Intent.createChooser(fileIntent,"Select File"),fileFlag);
                    break;
                case R.id.getFile:
                        getMessage(image);
                    break;
                case R.id.hideFile:
                    addFile.setEnabled(false);
                    hide(image,msgFile,bar);
                    break;

            }
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){//triggered when startActivityForResult intent is called
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==pictureFlag && resultCode==RESULT_OK && data != null && data.getData() != null){//For Image files
            Uri uri=data.getData();
            try{
                image= MediaStore.Images.Media.getBitmap(getContentResolver(),uri);//get the selected image
                addImage.setImageBitmap(image);
                getFile.setEnabled(true);
                addFile.setEnabled(true);
                addFile.setTextColor(android.graphics.Color.rgb(155,50,50));
                addImageB.setText("Change Image");
            }catch (Exception e){e.printStackTrace();}//print error if failed to open image
        }

        if(requestCode==fileFlag && resultCode==RESULT_OK && data != null && data.getData() != null){//For the selectedfile
            Uri uri=data.getData();
            try{
                msgFile=new File(uri.getPath());//get the file
            }catch(Exception e){}
            if(msgFile.length()==0){
                try{
                    msgFile=new File(getPathFromUri(MainActivity.this,uri));//get the file
                }catch(Exception e){
                    Log.e("Main","Unable to get file!");
                    e.printStackTrace();
                }
            }
            fileName.setText(msgFile.getName());

            addFile.setTextColor(android.graphics.Color.rgb(0,0,0));
            hideFile.setEnabled(true);
        }
    }
    private void disableButton(){//disables  buttons
        addFile.setEnabled(false);
        getFile.setEnabled(false);
        hideFile.setEnabled(false);
        addFile.setTextColor(Color.rgb(150,150,150));
    }

    public static String getPathFromUri(final Context context, final Uri uri) {//get path name from a uri object(source: web)

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }


    private void hide(final Bitmap img,final File f,final ProgressBar p){
        disableButton();
        p.setProgress(0);
        p.setVisibility(View.VISIBLE);
        Thread th=new Thread(){
            public void run(){
                super.run();
                Looper.prepare();

                Cover cover=new Cover(img);
                Msg msg=new Msg(f.getAbsolutePath());//Log.e("main","Embedded size: "+msg.size);
                Stego stego=new Stego(cover);
                if(stego.conceal(msg,p)) {
                    try {
                        stego.save();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else{//if file too large or invalid
                    act.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            addFile.setEnabled(true);
                            getFile.setEnabled(true);
                            fileName.setText("");
                            addFile.setTextColor(android.graphics.Color.rgb(155,50,50));
                        }
                    });
                }
                p.setProgress(p.getMax());
            }
        };
        th.start();


    }



    //-----------------------------------------------------------------------------------------------------------------------------------------------
    private void getMessage(final Bitmap image){
        disableButton();
        bar.setProgress(0);
        bar.setVisibility(View.VISIBLE);

        Thread th =new Thread() {
            public void run(){
                Looper.prepare();
                super.run();
                Stego stego = new Stego(image);
                stego.getMessage(bar);
            }
        };
        th.start();
    }
    public static void show(final String toast){
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.cntx,toast,Toast.LENGTH_LONG).show();
            }
        });
    }

}
