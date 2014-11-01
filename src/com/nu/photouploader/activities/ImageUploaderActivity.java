package com.nu.photouploader.activities;

import java.io.File;

import com.kbeanie.imagechooser.api.ChooserType;
import com.kbeanie.imagechooser.api.ChosenImage;
import com.kbeanie.imagechooser.api.ImageChooserListener;
import com.kbeanie.imagechooser.api.ImageChooserManager;
import com.nu.photouploader.R;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_uploader);
		
		Button buttonChooseImage = (Button) findViewById(R.id.buttonChooseImage);
		buttonChooseImage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				chooseImage();
			}
		});
		
		imageViewThumbSmall = (ImageView) findViewById(R.id.imageViewThumbSmall);
	}
	
	private void chooseImage() {
		chooserType = ChooserType.REQUEST_PICK_PICTURE;
		imageChooserManager = new ImageChooserManager(this,
				ChooserType.REQUEST_PICK_PICTURE, "myfolder", true);
		imageChooserManager.setImageChooserListener(this);
		try {
			//pbar.setVisibility(View.VISIBLE);
			filePath = imageChooserManager.choose();
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
				//pbar.setVisibility(View.GONE);
				if (image != null) {
					//textViewFile.setText(image.getFilePathOriginal());
//					imageViewThumbnail.setImageURI(Uri.parse(new File(image
//							.getFileThumbnail()).toString()));
					imageViewThumbSmall.setImageURI(Uri.parse(new File(image
							.getFileThumbnailSmall()).toString()));
				}
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
	
	// Should be called if for some reason the ImageChooserManager is null (Due
		// to destroying of activity for low memory situations)
		private void reinitializeImageChooser() {
			imageChooserManager = new ImageChooserManager(this, chooserType,
					"myfolder", true);
			imageChooserManager.setImageChooserListener(this);
			imageChooserManager.reinitialize(filePath);
		}

	@Override
	public void onError(String reason) {
		// TODO Auto-generated method stub
		
	}

}
