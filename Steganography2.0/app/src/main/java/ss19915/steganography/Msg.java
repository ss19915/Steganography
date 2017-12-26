package ss19915.steganography;

/**
 * Created by ss19915 on 23/12/17.
 */

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;

class Msg {//this class contains all methods and variables  applied to message file which has to be imbedded to the image file.

    public String name;
    public int bRead;
    public long size;
    private FileInputStream file;
    private byte[] bite;

    Msg(String s) {
        try {
            file = new FileInputStream(s);
            bite = new byte[1];
            name = (new File(s)).getName();
            bRead = 1;
            size = file.getChannel().size();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    Msg(FileInputStream f,String n) {
        try {
            file = f;
            bite = new byte[1];
            name = n;
            bRead = 1;
            size = file.getChannel().size();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public byte[] getBite() {
        try {
            bRead = file.read(bite);
        } catch (Exception e) {
            MainActivity.show("Msg:read error");
            Log.e("Stego","Msg:read error");
        }
        return bite;
    }
    public void close(){
        try{
            file.close();
        }
        catch(Exception e){
            System.out.println(e);
        }
    }
}
