package com.nu.photouploader.activities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.UiLifecycleHelper;
import com.facebook.SessionState;
import com.kbeanie.imagechooser.api.ChooserType;
import com.kbeanie.imagechooser.api.ChosenImage;
import com.kbeanie.imagechooser.api.ImageChooserListener;
import com.kbeanie.imagechooser.api.ImageChooserManager;
import com.nu.adapters.GridViewAdapter;
import com.nu.photouploader.R;

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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

public class ImageUploaderActivity extends Activity implements ImageChooserListener{
	
	private float ENABLED_ALPHA = 1.0f; // Alpha value of the button when enabled
	private float DISABLED_ALPHA = 0.6f; // Alpha value of the button when disabled
	
	// UI elements
	private ImageButton coverImageButton;
	private ImageButton imageButton1;
	private ImageButton imageButton2;
	private ImageButton imageButton3;
	private ImageButton imageButton4;
	private ImageButton selectedButton;
	private Button uploadToFacebook;
	RelativeLayout progress_overlay;
	
	// Utility class variables
	private ImageChooserManager imageChooserManager;
	private UiLifecycleHelper uiHelper;
	private Lock uploadLock = new ReentrantLock();
	
	// Others
	private ArrayList<String> filePaths = new ArrayList<String>();
	private String filePath;
	private int chooserType;
	private int fileCount = 0;
	
	// Set a callback on Facebook Session so that we can track session change
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
		
		// Initialize all UI elements
		initializeUI();
	}
	
	
	private void initializeUI() {
		// Get all the UI elements
		coverImageButton = (ImageButton) findViewById(R.id.coverImageButton);
		imageButton1 = (ImageButton) findViewById(R.id.imageButton1);
		imageButton2 = (ImageButton) findViewById(R.id.imageButton2);
		imageButton3 = (ImageButton) findViewById(R.id.imageButton3);
		imageButton4 = (ImageButton) findViewById(R.id.imageButton4);
		uploadToFacebook = (Button) findViewById(R.id.uploadToFacebookButton);
		progress_overlay = (RelativeLayout) findViewById(R.id.progress_layout);
		
		// Disable all the buttons initially except first image button(imageButton1)
		enableButton(coverImageButton, false, DISABLED_ALPHA);
		enableButton(imageButton2, false, DISABLED_ALPHA);
		enableButton(imageButton3, false, DISABLED_ALPHA);
		enableButton(imageButton4, false, DISABLED_ALPHA);
		uploadToFacebook.setEnabled(false);
		progress_overlay.setVisibility(View.GONE);
		
		/*Set click listeners on all the buttons*/
		
		coverImageButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// When coverImageButton is clicked show a dialog with image options
				showOptionsDialog();
			}
		});
		
		
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
		
		
		uploadToFacebook.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// If session doesn't have publish permission then make a request
				if(!hasPublishPermission()){
					Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(ImageUploaderActivity.this, Arrays.asList(getResources().getString(R.string.publish_actions)));
					Session session = Session.getActiveSession();
					session.requestNewPublishPermissions(newPermissionsRequest);
				}
				// Else post added photos
				else {
					postPhoto();
				}
			}
		});
	}
	
	
	@SuppressLint("NewApi")
	private void enableButton(ImageButton button, boolean enable, float alpha){
		button.setEnabled(enable);
		button.setAlpha(alpha);
	}
	
	
	private void takePicture() {
		chooserType = ChooserType.REQUEST_CAPTURE_PICTURE;
		imageChooserManager = new ImageChooserManager(this, ChooserType.REQUEST_CAPTURE_PICTURE, getResources().getString(R.string.photo_uploader), true);
		imageChooserManager.setImageChooserListener(this);
		try {
			progress_overlay.setVisibility(View.VISIBLE);
			imageChooserManager.choose();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void showPhotoSelectionDialog(View v) {
		// set selectedButton to use it later for showing selected image as this button's background
		selectedButton = (ImageButton) v;
		
		CharSequence options[] = getResources().getStringArray(R.array.photo_selection_options);

		AlertDialog.Builder builder = new AlertDialog.Builder(ImageUploaderActivity.this);
		builder.setTitle(getResources().getText(R.string.pick_an_option));
		
		// Based on user selection take the appropriate action
		builder.setItems(options, new DialogInterface.OnClickListener() {
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
		imageChooserManager = new ImageChooserManager(this, ChooserType.REQUEST_PICK_PICTURE, getResources().getString(R.string.photo_uploader), true);
		imageChooserManager.setImageChooserListener(this);
		try {
			// While user is choosing the photo show progress bar
		progress_overlay.setVisibility(View.VISIBLE);
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
				// Once we get the image selected by user hide the progress bar
				// and if image is not 
				progress_overlay.setVisibility(View.GONE);
				if (image != null) {
					selectedButton.setImageDrawable(Drawable.createFromPath(image.getFilePathOriginal()));
					handleImageSelection(selectedButton.getId(), image.getFilePathOriginal());
				}
				else{
					new AlertDialog.Builder(ImageUploaderActivity.this).setTitle(R.string.error_title).setMessage(R.string.photo_selection_error_message).setPositiveButton(R.string.ok, null).show();
				}
			}
		});
	}
	
	@SuppressLint("NewApi")
	private void handleImageSelection(int selectedButtonId, String path){
		// When user selects a photo add the path to list and enable next button
		switch(selectedButtonId){
			case R.id.imageButton1:
				// Add file path to the list
				filePaths.add(0, path);
				
				// On first photo selection enable coverImageButton and enable it
				coverImageButton.setImageDrawable(Drawable.createFromPath(path));
				enableButton(coverImageButton, true, ENABLED_ALPHA);
				
				// Enable next button to add photo
				enableButton(imageButton2, true, ENABLED_ALPHA);
				
				// Enable uploadToFacebook button
				uploadToFacebook.setEnabled(true);
				break;
			case R.id.imageButton2:
				// Add file path to the list and enable next add button
				filePaths.add(1, path);
				enableButton(imageButton3, true, ENABLED_ALPHA);
				break;
			case R.id.imageButton3:
				// Add file path to the list and enable next add button
				filePaths.add(2, path);
				enableButton(imageButton4, true, ENABLED_ALPHA);
				break;
			case R.id.imageButton4:
				// Add file path to the list and enable next add button
				filePaths.add(3, path);
				break;
			default:
				// Add file path to the list and enable next add button
				filePaths.add(0, path);
				enableButton(imageButton1, true, ENABLED_ALPHA);
				break;
		}
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		 uiHelper.onActivityResult(requestCode, resultCode, data);
		 
		if (resultCode == RESULT_OK && (requestCode == ChooserType.REQUEST_PICK_PICTURE || requestCode == ChooserType.REQUEST_CAPTURE_PICTURE)) {
			if (imageChooserManager == null) {
				reinitializeImageChooser();
			}
			
			// Submit the data to imageChooserManager
			imageChooserManager.submit(requestCode, data);
		} else {
			progress_overlay.setVisibility(View.GONE);
		}
		
	}
	
	@Override
	public void onResume() {
	    super.onResume();
	    
	    // For scenarios where the main activity is launched and user
	    // session is not null, the session state change notification
	    // may not be triggered. Trigger it if it's open/closed.
	    Session session = Session.getActiveSession();
	    if (session != null && (session.isOpened() || session.isClosed()) ) {
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
		imageChooserManager = new ImageChooserManager(this, chooserType, getResources().getString(R.string.photo_uploader), true);
		imageChooserManager.setImageChooserListener(this);
		imageChooserManager.reinitialize(filePath);
	}
		
	private void postPhoto() {
		// Show the progress_overlay while photo upload is in progress
		progress_overlay.setVisibility(View.VISIBLE);
        
        // Get the count of all the photos that needs to be uploaded
        fileCount = filePaths.size();
        
        // Iterate over the list of photos and make the upload request
        for (String file : filePaths) {
        	// Create bitmap from the image file path
        	Bitmap image = BitmapFactory.decodeFile(file);
        	
        	// Check if session has permission to publish
	        if(hasPublishPermission()){
	        	
	        	// Make the upload photo request and pass it the bitmap to be uploaded
		        Request request = Request.newUploadPhotoRequest(Session.getActiveSession(), image, new Request.Callback() {
	                @Override
	                public void onCompleted(Response response) {
	                	
	                	// When a photo upload is completed start the lock so that callback for each
	                	// photo upload request can be handled without any race condition
	                	uploadLock.lock();
	                	try {
	                		
		                	// Make sure there was no error in uploading photos
		                	if(response != null && response.getError() == null){
			                		// Decrement the file count to keep track how many responses have been received
				                	fileCount--;
				                	
				                	// Check if all the responses have been received
				                	if(fileCount == 0){
					                	progress_overlay.setVisibility(View.GONE);
					                	
					                	// Navigate user to the upload confirmation activity
					                	Intent intent = new Intent(ImageUploaderActivity.this, ConfirmationActivity.class);
					    				startActivity(intent);
					    				ImageUploaderActivity.this.overridePendingTransition(android.R.anim.slide_in_left,
					    		                android.R.anim.slide_out_right);
					    				finish();
				                	}
			                	
			                	
			                }else{
			                	new AlertDialog.Builder(ImageUploaderActivity.this)
			                				   .setTitle(R.string.error_title)
			                				   .setMessage(response.getError().getErrorMessage() + getResources().getString(R.string.error_message))
			                				   .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
												
													public void onClick(DialogInterface dialog, int which) {
														// Let the user try again
														progress_overlay.setVisibility(View.GONE);
													}
												})
			                				   .show();
			                }
	                	} finally {
	                		uploadLock.unlock();
	                	}
		             }
	            });
		        
	            request.executeAsync();
	        }
		}
    }
	
		
	private boolean hasPublishPermission() {
		// Check if session has the publish permission
        Session session = Session.getActiveSession();
        return session != null && session.getPermissions().contains("publish_actions");
    }
	
	
	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
		// Handle session state change
        if (state == SessionState.OPENED_TOKEN_UPDATED) {
            postPhoto();
        }
	}

	@Override
	public void onError(String reason) {
		new AlertDialog.Builder(ImageUploaderActivity.this).setTitle(R.string.error_title).setMessage(R.string.photo_selection_error_message).setPositiveButton(R.string.ok, null).show();
	}
	
	private void showOptionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog optionDialog = builder.create();
        
        // Create a new grid view to display in the dialog
        GridView gridView = new GridView(this);
        
        // Set an adapter that will populate the images in the grid view
        gridView.setAdapter(new GridViewAdapter(ImageUploaderActivity.this, filePaths));
        
        // Set the number of columns in grid view
        gridView.setNumColumns(2);
        
        // Set listener for grid view item click event
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("NewApi")
			@Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	// Hide the dialog
            	optionDialog.dismiss();
            	
            	// Show the selected image on cover image button
            	coverImageButton.setImageDrawable(Drawable.createFromPath(filePaths.get(position)));
            }
        });
        optionDialog.setView(gridView);
        optionDialog.setTitle(getResources().getString(R.string.select_cover_photo));
        optionDialog.show();
    }
}
