package ss19915.steganography;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Environment;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.net.ContentHandler;
import java.security.acl.LastOwnerException;
import java.util.Calendar;

/**
 * Created by ss19915 on 23/12/17.
 */
//-----------------------------------------------------------------------------------------------------------------------------------------------
//-------------X------------Conceal message into Image------------------X----------------
//The new image is modified to store message.The modified image is called Stego image

public class Stego {//this class contains all methods and variable to conceal message file into image & to retrieve message from stego image

    Cover cover;//declare variables
    String name;
    long size;
    public int height,width,capacity;
    public Bitmap img;
    private String sdcard = Environment.getExternalStorageDirectory().getPath()+"/Steganography/";


    Stego(Cover cover){
        this.cover=cover;
        height=cover.height;
        width=cover.width;
        capacity=width*(height-1);
        img=Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
    }
    public boolean conceal (Msg file, ProgressBar p) {
        p.setMax(width-1);
        if (capacity>file.size+file.name.length()) {
            concealinfo(file);
            conceilFile(file, p);

        }
        else {
            MainActivity.show("File too large! please choose another message file.");
            return false;
        }
        return true;
    }


    private void concealinfo(Msg file){//conceals message size and name into new stego image
        int pixel[]=new int[3],k,i,data,sizeLength=0;
        String name;
        long size =file.size;//Log.e("Size: ",""+Long.toBinaryString(size));
        while(size!=0){//get no of pixel needed to store size into image
            size=size>>9;
            sizeLength++;
        }
        //store sizelength into new image
        pixel=cover.getPixel(0,0);//get original pixel of provided image
        concealDataintoPixel(sizeLength,pixel);//store size length into the pixel
        img.setPixel(0,0, Color.rgb(pixel[0],pixel[1],pixel[2]));//save the modified pixel into new image

        //concealing size into stego image
        size=file.size;
        for (i=1;i<=sizeLength;i++){
            pixel=cover.getPixel(i,0);//get original pixel of provided image
            data=(int)size & 511;//break size into 9 byte of data
            size=size>>9;
            concealDataintoPixel(data,pixel);//store data into pixel
            img.setPixel(i,0,Color.rgb(pixel[0],pixel[1],pixel[2]));//save the modified pixel into new image
            //Log.e("Pixels "+i,": "+Integer.toBinaryString(pixel[0])+","+Integer.toBinaryString(pixel[1])+","+Integer.toBinaryString(pixel[2]));
        }

        //store length of name into image

        sizeLength=file.name.length();
        pixel=cover.getPixel(i,0);
        concealDataintoPixel(sizeLength,pixel);
        img.setPixel(i,0, Color.rgb(pixel[0],pixel[1],pixel[2]));
        i++;

        //store file name into image
        for(k=0;k<sizeLength;k++){
            data=file.name.charAt(k);
            pixel=cover.getPixel(i+k,0);
            concealDataintoPixel(data,pixel);
            img.setPixel(i+k,0, Color.rgb(pixel[0],pixel[1],pixel[2]));
        }
        for (;k+i<width;k++){
            pixel=cover.getPixel(i+k,0);
            img.setPixel(i+k,0,Color.rgb(pixel[0],pixel[1],pixel[2]));
        }
    }

    private void concealDataintoPixel(int data,int pixel[]){//conceals 9 bit data into R G B channel of pixel
        //reset 3 LSB of pixels
        pixel[0]=pixel[0]&248;
        pixel[1]=pixel[1]&248;
        pixel[2]=pixel[2]&248;

        //store data into pixels
        pixel[0]=pixel[0]|(data&7);
        data=data>>3;
        pixel[1]=pixel[1]|(data&7);
        data=data>>3;
        pixel[2]=pixel[2]|(data&7);
    }




    private void conceilFile(Msg file, ProgressBar p){
        int i,j,data,pixel[]=new int[3];
        for(i=0;i<cover.width;i++){
            p.setProgress(i);
            for (j=1;j<cover.height;j++){
                pixel=cover.getPixel(i,j);
                if (file.bRead>0){
                    data=file.getBite()[0];
                    concealDataintoPixel(data,pixel);
                }
                img.setPixel(i,j,Color.rgb(pixel[0],pixel[1],pixel[2]));
            }
        }
    }












    private String getSaveFileName() {
        Calendar c = Calendar.getInstance();
        String date = c.get(Calendar.YEAR)+""+c.get(Calendar.MONTH)+""+c.get(Calendar.DATE);
        String time = c.get(Calendar.HOUR)+""+c.get(Calendar.MINUTE)+""+c.get(Calendar.SECOND)+""+c.get(Calendar.MILLISECOND);
        return "Stego_"+date+time+".png";
    }

    //save stego image into memory
    public void save() throws Exception {
        String sdcardE=sdcard+"Encrypted/";
        (new File(sdcardE)).mkdirs();

        try{
            String saveName=getSaveFileName();
            FileOutputStream savedFile = new FileOutputStream(new File(sdcardE,saveName));
            img.compress(Bitmap.CompressFormat.PNG,100,savedFile);
            savedFile.flush();
            savedFile.close();
            Log.e("Stego","Image Saved");
            MainActivity.show("File saved as: "+saveName);

        }
        catch(Exception e){
            Log.e("Stego","File I/O error"+e);
            MainActivity.show("File I/O error");
        }



    }




    //---------------------------------------------------------------------------------------------------------------------------------
    //==============X============Retrieve message from Stego Image===============X==================
    Stego(Bitmap b){
        cover=new Cover(b);
        height=cover.height;
        width=cover.width;
        capacity=(height-1)*width;
    }
    public void getMessage(ProgressBar p){
        retrieveinfo();
        if (size>0&&size<=capacity) {
            p.setMax((int)size/height);
            String sdcardD = sdcard + "Decrypted";//create folder if not exixt
            (new File(sdcardD)).mkdirs();
            FileOutputStream f;
            try {
                f = new FileOutputStream(sdcardD + "/" + name);
                retrieveFile(f, p);
                MainActivity.show("Message received: " + name);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            MainActivity.show("Invalid Image");
            p.setProgress(p.getMax());
            MainActivity.act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MainActivity.addFile.setEnabled(true);
                    MainActivity.addFile.setTextColor(Color.rgb(155,50,50));
                }
            });
        }

    }
    private void retrieveinfo(){//get name and size of hidden file from image
        int i,pixel[]=new int[3],sizeLength;
        pixel=cover.getPixel(0,0);
        sizeLength=retrieveDataFromPixel(pixel);// get no of pixel used to store size of message file
        if(sizeLength>10){//check for invalid size length
            size=-1;
            return;
        }
        //get size of message file
        size=0;
        for (i=sizeLength;i>0;i--){
            pixel=cover.getPixel(i,0);
            //Log.e("Pixels "+i,": "+Integer.toBinaryString(pixel[0])+","+Integer.toBinaryString(pixel[1])+","+Integer.toBinaryString(pixel[2]));
            size=size<<9;
            size=size|retrieveDataFromPixel(pixel);
        }//Log.e("Stego","size: "+size);
        //get no of pixels used to store name of the message file
        //Log.e("Size",": "+ Long.toBinaryString(size));
        i=sizeLength+1;
        pixel=cover.getPixel(i,0);
        i++;
        sizeLength=retrieveDataFromPixel(pixel);
        name="";
        for(int k=0;k<sizeLength;k++){
            pixel=cover.getPixel(k+i,0);
            name+=(char)retrieveDataFromPixel(pixel);
        }//Log.e("Stego","Received name: "+name);
    }
    private int retrieveDataFromPixel(int pixel[]){
        int data;
        data=pixel[2]&7;
        data=data<<3;
        data=data|(pixel[1]&7);
        data=data<<3;
        data=data|(pixel[0]&7);
        return data;
    }

    private void retrieveFile(FileOutputStream f,ProgressBar p){
        int i,j,pixel[]=new int [3];
        byte data;
        long flag=0;//Log.e("Size",": "+size);
        for (i=0;i<width;i++){
            p.setProgress(i);
            for (j=1;j<height;j++){
                if (flag<size) {
                    pixel=cover.getPixel(i,j);
                    data=(byte) retrieveDataFromPixel(pixel);
                    try {
                        f.write(data);
                    }catch (Exception e){e.printStackTrace();break;}
                    flag++;
                }
                else
                    break;
            }
        }

    }

}
