package com.nu.photouploader.activities;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.UiLifecycleHelper;
import com.facebook.Session.StatusCallback;
import com.facebook.SessionState;
import com.facebook.widget.FacebookDialog;
import com.kbeanie.imagechooser.api.ChooserType;
import com.kbeanie.imagechooser.api.ChosenImage;
import com.kbeanie.imagechooser.api.ImageChooserListener;
import com.kbeanie.imagechooser.api.ImageChooserManager;
import com.nu.photouploader.R;

import android.net.Uri;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;

public class ImageUploaderActivity extends Activity implements ImageChooserListener{
	
	private float ENABLED_ALPHA = 1.0f;
	private float DISABLED_ALPHA = 0.6f;
	
	private ImageChooserManager imageChooserManager;
	private Button imageButton1;
	private Button imageButton2;
	private Button imageButton3;
	private Button imageButton4;
	private ProgressBar pbar;
	private ArrayList<String> filePaths = new ArrayList<String>();
	private String filePath;
	private Button uploadToFacebook;
	private UiLifecycleHelper uiHelper;
	private Button selectedButton;
	private int chooserType;
	private int fileCount = 0;
	private Lock downloadLock = new ReentrantLock();
	
	private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    uiHelper = new UiLifecycleHelper(this, callback);
	    uiHelper.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_uploader);
		
		imageButton1 = (Button) findViewById(R.id.imageButton1);
		imageButton2 = (Button) findViewById(R.id.imageButton2);
		imageButton3 = (Button) findViewById(R.id.imageButton3);
		imageButton4 = (Button) findViewById(R.id.imageButton4);
		
		enableButton(imageButton2, false, DISABLED_ALPHA);
		enableButton(imageButton3, false, DISABLED_ALPHA);
		enableButton(imageButton4, false, DISABLED_ALPHA);
		
		imageButton1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showPhotoSelectionDialog(v);
			}
		});
		
		imageButton2.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showPhotoSelectionDialog(v);
			}
		});
		
		imageButton3.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						showPhotoSelectionDialog(v);
					}
				});
		
		imageButton4.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showPhotoSelectionDialog(v);
			}
		});
		
		uploadToFacebook = (Button) findViewById(R.id.uploadToFacebookButton);
		enableButton(uploadToFacebook, false, DISABLED_ALPHA);
		
		uploadToFacebook.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if(!hasPublishPermission()){
					Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(ImageUploaderActivity.this, Arrays.asList("publish_actions"));
					Session session = Session.getActiveSession();
					session.requestNewPublishPermissions(newPermissionsRequest);
				}
				else {
					postPhoto();
				}
			}
		});
		
		pbar = (ProgressBar) findViewById(R.id.progressBar);
		pbar.setVisibility(View.GONE);
	}
	
	@SuppressLint("NewApi")
	private void enableButton(Button button, boolean enable, float alpha){
		button.setEnabled(enable);
		button.setAlpha(alpha);
	}
	
	
	private void takePicture() {
		chooserType = ChooserType.REQUEST_CAPTURE_PICTURE;
		imageChooserManager = new ImageChooserManager(this,
				ChooserType.REQUEST_CAPTURE_PICTURE, "myfolder", true);
		imageChooserManager.setImageChooserListener(this);
		try {
			pbar.setVisibility(View.VISIBLE);
			imageChooserManager.choose();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void showPhotoSelectionDialog(View v){
		selectedButton = (Button) v;
		
		CharSequence colors[] = new CharSequence[] {"Take Photo", "Choose Photo"};

		AlertDialog.Builder builder = new AlertDialog.Builder(ImageUploaderActivity.this);
		builder.setTitle("Pick an option");
		
		builder.setItems(colors, new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		    	if(which == 0){
		    		takePicture();
		    	}
		    	else if(which == 1){
		        	chooseImage();
		        }
		    }
		});
		builder.show();
	}
	
	private void chooseImage() {
		chooserType = ChooserType.REQUEST_PICK_PICTURE;
		imageChooserManager = new ImageChooserManager(this,
				ChooserType.REQUEST_PICK_PICTURE, "myfolder", true);
		imageChooserManager.setImageChooserListener(this);
		try {
			pbar.setVisibility(View.VISIBLE);
			imageChooserManager.choose();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.image_uploader, menu);
		return true;
	}

	@Override
	public void onImageChosen(final ChosenImage image) {
		runOnUiThread(new Runnable() {

			@SuppressLint("NewApi")
			@Override
			public void run() {
				pbar.setVisibility(View.GONE);
				if (image != null) {
					selectedButton.setBackground(Drawable.createFromPath(image.getFilePathOriginal()));
					handleImageSelection(selectedButton.getId(), image.getFilePathOriginal());
					//filePaths.add(image.getFilePathOriginal());
				}
			}
		});
	}
	
	private void handleImageSelection(int selectedButtonId, String path){
		switch(selectedButtonId){
			case R.id.imageButton1:
				filePaths.add(0, path);
				enableButton(imageButton2, true, ENABLED_ALPHA);
				enableButton(uploadToFacebook, true, ENABLED_ALPHA);
				break;
			case R.id.imageButton2:
				filePaths.add(1, path);
				enableButton(imageButton3, true, ENABLED_ALPHA);
				break;
			case R.id.imageButton3:
				filePaths.add(2, path);
				enableButton(imageButton4, true, ENABLED_ALPHA);
				break;
			case R.id.imageButton4:
				filePaths.add(3, path);
				break;
			default:
				filePaths.add(0, path);
				enableButton(imageButton1, true, ENABLED_ALPHA);
				break;
		}
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		 uiHelper.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK
				&& (requestCode == ChooserType.REQUEST_PICK_PICTURE || requestCode == ChooserType.REQUEST_CAPTURE_PICTURE)) {
			if (imageChooserManager == null) {
				reinitializeImageChooser();
			}
			imageChooserManager.submit(requestCode, data);
		} else {
			pbar.setVisibility(View.GONE);
		}
		
	}
	
	@Override
	public void onResume() {
	    super.onResume();
	    
	    // For scenarios where the main activity is launched and user
	    // session is not null, the session state change notification
	    // may not be triggered. Trigger it if it's open/closed.
	    Session session = Session.getActiveSession();
	    if (session != null &&
	           (session.isOpened() || session.isClosed()) ) {
	        onSessionStateChange(session, session.getState(), null);
	    }
	    
	    uiHelper.onResume();
	}

	@Override
	public void onPause() {
	    super.onPause();
	    uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
	    super.onDestroy();
	    uiHelper.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    uiHelper.onSaveInstanceState(outState);
	}
	
	// Should be called if for some reason the ImageChooserManager is null (Due
		// to destroying of activity for low memory situations)
		private void reinitializeImageChooser() {
			imageChooserManager = new ImageChooserManager(this, chooserType,
					"myfolder", true);
			imageChooserManager.setImageChooserListener(this);
			imageChooserManager.reinitialize(filePath);
		}
		
		private void postPhoto() {
			enableButton(imageButton1, false, DISABLED_ALPHA);
			enableButton(imageButton2, false, DISABLED_ALPHA);
			enableButton(imageButton3, false, DISABLED_ALPHA);
			enableButton(imageButton4, false, DISABLED_ALPHA);
			
			enableButton(uploadToFacebook, false, DISABLED_ALPHA);
			
	        pbar.setVisibility(View.VISIBLE);
	        fileCount = filePaths.size();
	        
	        for (String file : filePaths) {
	        	Bitmap image = BitmapFactory.decodeFile(file);

		        if(hasPublishPermission()){
			        Request request = Request.newUploadPhotoRequest(Session.getActiveSession(), image, new Request.Callback() {
		                @Override
		                public void onCompleted(Response response) {
		                	downloadLock.lock();
		                	try {
		                	fileCount--;
		                	
		                	if(fileCount == 0){
			                	pbar.setVisibility(View.GONE);
			                	Intent intent = new Intent(ImageUploaderActivity.this, ConfirmationActivity.class);
			    				startActivity(intent);
			    				finish();
		                	}
		                	} finally {
		                	downloadLock.unlock();
		                	}
		                	
		                }
		            });
			        
		            request.executeAsync();
		        }
			}
	    }
		
		private boolean hasPublishPermission() {
	        Session session = Session.getActiveSession();
	        return session != null && session.getPermissions().contains("publish_actions");
	    }
		 
		 private void onSessionStateChange(Session session, SessionState state, Exception exception) {
		        if (state == SessionState.OPENED_TOKEN_UPDATED) {
		            postPhoto();
		        }
		    }

	@Override
	public void onError(String reason) {
		// TODO Auto-generated method stub
		
	}

}
