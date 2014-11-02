package com.nu.photouploader.activities;

import java.io.File;
import java.util.Arrays;

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
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ImageUploaderActivity extends Activity implements ImageChooserListener{
	
	private ImageView imageViewThumbnail;

	private ImageView imageViewThumbSmall;

	private TextView textViewFile;

	private ImageChooserManager imageChooserManager;

	private ProgressBar pbar;

	//private AdView adView;

	private String filePath;

	private int chooserType;
	
	private Button uploadToFacebook;
	private UiLifecycleHelper uiHelper;
	
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
		
		Button buttonChooseImage = (Button) findViewById(R.id.buttonChooseImage);
		buttonChooseImage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
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
		});
		
		imageViewThumbSmall = (ImageView) findViewById(R.id.imageViewThumbSmall);
		uploadToFacebook = (Button) findViewById(R.id.uploadToFacebookButton);
		
		pbar = (ProgressBar) findViewById(R.id.progressBar);
		pbar.setVisibility(View.GONE);
		
		uploadToFacebook.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if(!hasPublishPermission()){
					Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(ImageUploaderActivity.this, Arrays.asList("publish_actions"));
//					newPermissionsRequest.setCallback(new StatusCallback() {
//						
//						@Override
//						public void call(Session session, SessionState state, Exception exception) {
//							postPhoto();
//						}
//					});
					Session session = Session.getActiveSession();
					session.requestNewPublishPermissions(newPermissionsRequest);
				}
				else {
					postPhoto();
				}
			}
		});
	}
	
	
	private void takePicture() {
		chooserType = ChooserType.REQUEST_CAPTURE_PICTURE;
		imageChooserManager = new ImageChooserManager(this,
				ChooserType.REQUEST_CAPTURE_PICTURE, "myfolder", true);
		imageChooserManager.setImageChooserListener(this);
		try {
			pbar.setVisibility(View.VISIBLE);
			filePath = imageChooserManager.choose();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
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

			@Override
			public void run() {
				pbar.setVisibility(View.GONE);
				if (image != null) {
					//textViewFile.setText(image.getFilePathOriginal());
//					imageViewThumbnail.setImageURI(Uri.parse(new File(image
//							.getFileThumbnail()).toString()));
					imageViewThumbSmall.setImageURI(Uri.parse(new File(image
							.getFileThumbnailSmall()).toString()));
					
					filePath = image.getFilePathOriginal();
				}
			}
		});
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
	        Bitmap image = BitmapFactory.decodeFile(filePath);
	        pbar.setVisibility(View.VISIBLE);
	        
	        if(hasPublishPermission()){
		        Request request = Request.newUploadPhotoRequest(Session.getActiveSession(), image, new Request.Callback() {
	                @Override
	                public void onCompleted(Response response) {
	                	pbar.setVisibility(View.GONE);
	                	Intent intent = new Intent(ImageUploaderActivity.this, ImageUploaderActivity.class);
	    				startActivity(intent);
	                }
	            });
		        
	            request.executeAsync();
	        }
	    }
		
		private boolean hasPublishPermission() {
	        Session session = Session.getActiveSession();
	        return session != null && session.getPermissions().contains("publish_actions");
	    }
		
//		 private void performPublish() {
//		        Session session = Session.getActiveSession();
//		        if (session != null) {
//		            if (hasPublishPermission()) {
//		                postPhoto();
//		                return;
//		            } else if (session.isOpened()) {
//		                // We need to get new permissions, then complete the action when we get called back.
//		                session.requestNewPublishPermissions(new Session.NewPermissionsRequest(this, "publish_actions"));
//		                return;
//		            }
//		        }
//
//		    }
//		 
//		 
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
