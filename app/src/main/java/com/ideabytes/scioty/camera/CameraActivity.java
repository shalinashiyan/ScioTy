package com.ideabytes.scioty.camera;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.ideabytes.scioty.user.WelcomeActivity;
import com.ideabytes.scioty.utility.VarGlobal;

public class CameraActivity extends Activity {

	    private static final String TAG = "CameraActivity";
	    
	    private static final String PHOTO_PATH = Environment.getExternalStorageDirectory() 
				+ File.separator + "android-camera-photo";
	    
	    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	  
	    private static String picName;
	    private Uri fileUri;
	    
	    
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        //setContentView(R.layout.main);

	        // create Intent to take a picture and return control to the calling application
	        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	        
	        File imagesFolder = new File(PHOTO_PATH);
	        
	        if(!imagesFolder.exists()){
	        	imagesFolder.mkdirs();
	        }
	        
	        // Create a media file name
	        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
	                .format(new Date());
	        picName = imagesFolder.getPath() + File.separator + "IMG_" + timeStamp + ".jpg"; 
	        File mediaFile = new File(picName);
	        

	        fileUri = Uri.fromFile(mediaFile); // create a file to save the image
	        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

	        // start the image capture Intent
	        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
	    }
	    
	    @Override
	    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
				Log.d(TAG, "photo saved: " + picName);

	            if (resultCode == RESULT_OK) {
	                // Image captured and saved to fileUri specified in the Intent
	            	Toast.makeText(this, "Image saved", Toast.LENGTH_LONG).show();
	            	
	            	try{
	                    Thread.sleep(1000);
	                }catch(InterruptedException e) {
	                    Log.e(TAG, "thread sleep: " + e.getMessage());
	                }
	    	        
	    	        exitCamera();
	    	        
	                
	            } else if (resultCode == RESULT_CANCELED) {

					Log.d(TAG, "user cancelled, photo deleted: " + picName);
	            /*	if(picName != null){
	            		File photo = new File (picName);
	            		
	            		if( photo.exists() ){
	            			photo.delete();
							Log.d(TAG, "photo deleted: " + picName);
	            		}
	            	}
	            */
	            	 // User cancelled the image capture
	            	Toast.makeText(this, "User cancelled, photo deleted", Toast.LENGTH_LONG).show();
	            	
	            } else {
	                // Image capture failed, advise user
	            	Toast.makeText(this, "Capture failed", Toast.LENGTH_LONG).show();
	            }
	        }
	        
	        
	        
	    }
	    
	    public void exitCamera(){
			VarGlobal.isCameraInUse = 0;
			VarGlobal.request_user_ID=" ";

	        Intent myIntent = new Intent(CameraActivity.this, WelcomeActivity.class);
	        startActivity(myIntent);
	        finish();

	        //moveTaskToBack(true);
	    }
	    
	    @Override
	    public void onBackPressed() {
			VarGlobal.isCameraInUse = 0;
			VarGlobal.request_user_ID=" ";
	      
	        Intent myIntent = new Intent(CameraActivity.this, WelcomeActivity.class);
	        startActivity(myIntent);
	        finish();

	        super.onBackPressed();

	    }

}