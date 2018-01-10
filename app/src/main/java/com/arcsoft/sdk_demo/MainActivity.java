package com.arcsoft.sdk_demo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.os.EnvironmentCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.soft.sdk_demo.R;

import java.io.File;

public class MainActivity extends Activity implements OnClickListener, AlertDialog.OnClickListener {
	private final String TAG = this.getClass().toString();

	private static final int REQUEST_CODE_IMAGE_CAMERA = 1;
	private static final int REQUEST_CODE_IMAGE_OP = 2;
	private static final int REQUEST_CODE_OP = 3;

	private Uri mPath;
	String path = Environment.getExternalStorageDirectory().getPath();

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.main_test);
		View v = this.findViewById(R.id.button1);
		v.setOnClickListener(this);
		v = this.findViewById(R.id.button2);
		v.setOnClickListener(this);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	if (data!=null)
		mPath = data.getData();

		if (requestCode == REQUEST_CODE_IMAGE_OP && resultCode == RESULT_OK) {
			String file = getPath(mPath);
			Bitmap bmp = Application.decodeImage(file);
			if (bmp == null || bmp.getWidth() <= 0 || bmp.getHeight() <= 0 ) {
				Log.e(TAG, "error");
			} else {
				Log.i(TAG, "bmp [" + bmp.getWidth() + "," + bmp.getHeight());
			}
			startOilPainting(bmp, file);
		} else if (requestCode == REQUEST_CODE_OP) {
			Log.i(TAG, "RESULT =" + resultCode);
			if (data == null) {
				return;
			}
			Bundle bundle = data.getExtras();
			String path = bundle.getString("imagePath");
			Log.i(TAG, "path="+path);
		} else if (requestCode == REQUEST_CODE_IMAGE_CAMERA && resultCode == RESULT_OK) {
			String file = getPath(Uri.fromFile(new File(path+"/aaa.jpg")));
			Log.d(TAG,"filepath=="+file);
			Bitmap bmp = Application.decodeImage(file);
			startOilPainting(bmp, file);
		}
	}

	@Override
	public void onClick(View paramView) {
		// TODO Auto-generated method stub
		switch (paramView.getId()) {
			case R.id.button2:
				if( ((Application)getApplicationContext()).mFaceDB.mRegister.isEmpty() ) {
					Toast.makeText(this, "没有注册人脸，请先注册！", Toast.LENGTH_SHORT).show();
				} else {
					Intent it = new Intent(MainActivity.this, DetecterActivity.class);
					startActivityForResult(it, REQUEST_CODE_OP);
				}
				break;
			case R.id.button1:
				new AlertDialog.Builder(this)
						.setTitle("请选择注册方式")
						.setIcon(android.R.drawable.ic_dialog_info)
						.setItems(new String[]{"打开图片", "拍摄照片"}, this)
						.show();
				break;
		}
	}

	/**
	 * @param uri
	 * @return
	 */
	@SuppressLint("NewApi")
	private String getPath(Uri uri) {
		String filePath = null;
		if (DocumentsContract.isDocumentUri(this, uri)) {
			// 如果是document类型的 uri, 则通过document id来进行处理
			String documentId = DocumentsContract.getDocumentId(uri);
			if (isMediaDocument(uri)) { // MediaProvider
				// 使用':'分割
				String id = documentId.split(":")[1];

				String selection = MediaStore.Images.Media._ID + "=?";
				String[] selectionArgs = {id};
				filePath = getDataColumn(this, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection, selectionArgs);
			} else if (isDownloadsDocument(uri)) { // DownloadsProvider
				Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(documentId));
				filePath = getDataColumn(this, contentUri, null, null);
			}
		} else if ("content".equalsIgnoreCase(uri.getScheme())){
			// 如果是 content 类型的 Uri
			filePath = getDataColumn(this, uri, null, null);
		} else if ("file".equals(uri.getScheme())) {
			// 如果是 file 类型的 Uri,直接获取图片对应的路径
			filePath = uri.getPath();
		}
		return filePath;

	}
	private static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri.getAuthority());
	}
	private static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}


	/**
	 * Get the value of the data column for this Uri. This is useful for
	 * MediaStore Uris, and other file-based ContentProviders.
	 *
	 * @param context The context.
	 * @param uri The Uri to query.
	 * @param selection (Optional) Filter used in the query.
	 * @param selectionArgs (Optional) Selection arguments used in the query.
	 * @return The value of the _data column, which is typically a file path.
	 */
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
	 * @param mBitmap
	 */
	private void startOilPainting(Bitmap mBitmap, String file) {
		Intent it = new Intent(MainActivity.this, RegisterActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("imagePath", file);
		it.putExtras(bundle);
		startActivityForResult(it, REQUEST_CODE_OP);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which){
			case 1:
//				Intent getImageByCamera = new Intent(
//						"android.media.action.IMAGE_CAPTURE");
//				ContentValues values = new ContentValues(1);
//
//				values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
//				mPath = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
//				getImageByCamera.putExtra(MediaStore.EXTRA_OUTPUT, mPath);
//
//				startActivityForResult(getImageByCamera, REQUEST_CODE_IMAGE_CAMERA);
				Intent intent = new Intent();
				intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
//path为保存图片的路径，执行完拍照以后能保存到指定的路径下
				File file = new File(path,"aaa.jpg");
				Uri imageUri = Uri.fromFile(file);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
//				intent.setType("image/jpeg");
				startActivityForResult(intent, REQUEST_CODE_IMAGE_CAMERA);
				break;
			case 0:
				Intent getImageByalbum = new Intent(Intent.ACTION_GET_CONTENT);
				getImageByalbum.addCategory(Intent.CATEGORY_OPENABLE);
				getImageByalbum.setType("image/jpeg");
				startActivityForResult(getImageByalbum, REQUEST_CODE_IMAGE_OP);


				break;
		}
	}
}

