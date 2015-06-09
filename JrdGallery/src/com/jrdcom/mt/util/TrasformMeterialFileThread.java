package com.jrdcom.mt.util;


import com.jrdcom.android.gallery3d.R;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import  java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import android.os.Environment;
 import com.jrdcom.mt.util.*;


public class TrasformMeterialFileThread extends Thread {

    private final String TAG = "TrasformMeterialFileThread";
    private Context context;
    private  String DST_PATH = "";
    private final String DIR_NAME = "resource";
    private final String FILE_NAME = "resource.zip";
    private Handler msgHandler;
    
    private InputStream iStream;
    private OutputStream oStream;
    
    
    public TrasformMeterialFileThread(Context context,Handler handler)
    {
        DST_PATH = FileUtils.getAbsolutePathOnExternalStorage(context,"");
        this.context = context;
        msgHandler = handler;
        start();
    }
    
    @Override
    public void run() {
        
           try {
               copyZipFileToSdcard();
           } catch (Exception e) {
               Log.e(TAG, e.getMessage());
           }
    }

    //1\first : copy zip file to sdcard
    public void copyZipFileToSdcard()
    {
        if(iscreateFile())return;
        
       try {
            iStream = this.context.getAssets().open(FILE_NAME);
            if(null != iStream)
            {
                ZipInputStream zis = new ZipInputStream(new BufferedInputStream(iStream));
                FileOutputStream fosFileOutputStream ;
                String tempFileName = "";
                try {
                    ZipEntry ze;
                    while ((ze = zis.getNextEntry()) != null) {
                        tempFileName = ze.getName();
                        if(ze.isDirectory())
                        {
                            File parentFile = new File(DST_PATH+tempFileName);
                            boolean bool =  parentFile.mkdirs();
                        }
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int count;
                        while ((count = zis.read(buffer)) != -1) {
                            baos.write(buffer, 0, count);
                        }
                        String filename = ze.getName();
                        byte[] bytes = baos.toByteArray();
                        fosFileOutputStream = openFile(tempFileName);
                        if(null != fosFileOutputStream)
                        {
                            fosFileOutputStream.write(bytes);
                            fosFileOutputStream.close();
                        }
                    }
                } finally {
                    zis.close();
                    iStream.close();
                }
            }
           
       } catch (Exception e) {
           return;
       }
    }
    //judge MTSDKSrouce file 
    public boolean  iscreateFile()
    {
        File file = new File(DST_PATH+DIR_NAME);
        if (file.exists()) {
            return true;
        }
        return false;
    }
    //open file inputstream to operate 
    public FileOutputStream openFile(String filename)
    {
        
        File file = new File(DST_PATH+filename);
        FileOutputStream foStream = null;
       
        if(file != null)
        {
            createFile(file);
            try{
              foStream = new FileOutputStream(file);
            }catch(Exception e){
                e.printStackTrace();
            }
            return foStream;
        }
        return null;
    }
    //To create new file
    public void createFile(File file)
    {
        try {
            if(!file.exists())
            {
                boolean bool = file.createNewFile();
            }
        } catch (Exception e) {
        }
    }
}
