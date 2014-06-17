package com.example.photocrush;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.DropBoxManager.Entry;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Media;

public class MainActivity extends Activity {

	private ListView mPhotoWall;
	MyCustomListAdapter ListAdapter;
	String[][] imageThumbUrls2 = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_main);
		mPhotoWall = (ListView) findViewById(R.id.listView1);

		ListAdapter = new MyCustomListAdapter();
		imageThumbUrls2 = GetPhotoPathStringArrary();
		for (int i = 0; i < imageThumbUrls2.length; i++) {

			ListAdapter.addItem(imageThumbUrls2[i]);
		}

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.footbutton_layout, null);
		Button cleanButton = (Button) findViewById(R.id.checkBtn);

		// 按钮点击事件
		cleanButton.setOnClickListener(new View.OnClickListener() {
			@SuppressLint("WorldWriteableFiles")
			public void onClick(View v) {
				// Perform action on click
				// 增加自己的代码......
				byte[] buffer = new byte[1024 * 1024];
				for (int k = 0; k < buffer.length; k++) {
					buffer[k] = (byte) (k % 10 + 1);
				}

				for (int i = 0; i < ListAdapter.mStatus.size(); i++) {
					boolean[] bStatus = ListAdapter.mStatus.get(i);
					for (int j = 0; j < 4; j++) {
						if (bStatus[j]) {
							String[] elmentArray = imageThumbUrls2[i];
							String strPicMagic = "photocrush_pic=";
							if (elmentArray[j].startsWith(strPicMagic)) {
								File file = new File(elmentArray[j].substring(strPicMagic.length()));
								if (file.exists() && file.canWrite()) {
									CrushSpecifyFile(file, buffer);
								}
							}
						}
					}
				}
				ListAdapter = new MyCustomListAdapter();
				imageThumbUrls2 = GetPhotoPathStringArrary();
				for (int i = 0; i < imageThumbUrls2.length; i++) {

					ListAdapter.addItem(imageThumbUrls2[i]);
				}
				mPhotoWall.setAdapter(ListAdapter);
			}
		});
		// mPhotoWall.addFooterView(view);
		mPhotoWall.setAdapter(ListAdapter);
		mPhotoWall.setDivider(null);
	}

	/**
	 * 粉碎文件
	 */
	private boolean CrushSpecifyFile(File file, byte[] buffer) {
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(file, "rw");

			long nBlocks = (file.length()) / buffer.length;
			long nLeft = (file.length()) % buffer.length;

			// 工业标准循环写x次
			for (int nNumOfTimes = 0; nNumOfTimes < 8; nNumOfTimes++) {
				raf.seek(0); // 移动文件指针到文件头
				for (long n = 0; n < nBlocks; n++) {
					raf.write(buffer);
				}
				if (nLeft > 0) {
					// 转换成nLeft大小
					raf.write(buffer, 0, (int) nLeft);
				}
			}
			raf.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// 通知媒体管理删除
		file.delete();
		return true;
	}

	class viewHolder {
		ImageView image0;
		ImageView image1;
		ImageView image2;

		TextView monthText;
		TextView yearText;
	}

	public class MyCustomListAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private ArrayList<String[]> mData = new ArrayList<String[]>();
		private ArrayList<ArrayList<Bitmap>> bitmapArrary = new ArrayList<ArrayList<Bitmap>>();
		private ArrayList<boolean[]> mStatus = new ArrayList<boolean[]>();

		public MyCustomListAdapter() {
			super();
			mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return mData.size();
		}

		public long getItemId(int position) {
			return position;
		}

		public String[] getItem(int position) {
			return (String[]) mData.get(position);
		}

		public void addItem(final String[] item) {

			boolean[] bStatus = new boolean[] { false, false, false, false };

			mStatus.add(bStatus);

			mData.add(item);
		}

		int mBitmapWidth = 0;
		int mBitmapHeigh = 0;

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final String[] information = (String[]) getItem(position);
			View view = null;
			viewHolder myViewHolder;
			String strMonMagic = "photocrush_month=";
			String strNullMagic = "photocrush_null";
			String strPicMagic = "photocrush_pic=";
			if (mBitmapWidth == 0) {
				mBitmapWidth = parent.getWidth() / 4 - 4;
				mBitmapHeigh = mBitmapWidth * 3 / 4;
			}
			if (convertView == null) {

				view = mInflater.inflate(R.layout.item_layout, null);
				myViewHolder = new viewHolder();

				myViewHolder.monthText = (TextView) view.findViewById(R.id.monthTextView);
				myViewHolder.monthText = (TextView) view.findViewById(R.id.monthTextView);
				myViewHolder.yearText = (TextView) view.findViewById(R.id.yearTextView);
				myViewHolder.image0 = (ImageView) view.findViewById(R.id.imageView1);
				myViewHolder.image1 = (ImageView) view.findViewById(R.id.ImageView2);
				myViewHolder.image2 = (ImageView) view.findViewById(R.id.ImageView3);

				view.setTag(myViewHolder);
				view.setClickable(false);

			} else {
				view = convertView;
				myViewHolder = (viewHolder) view.getTag();

			}

			boolean[] bStatus = mStatus.get(position);

			ImageView image0CheckBtn = (ImageView) view.findViewById(R.id.picCheckBtn1);
			image0CheckBtn.setImageResource(R.drawable.photo_grid_checkbox_checked);
			image0CheckBtn.setVisibility(bStatus[1] ? View.VISIBLE : View.GONE);
			myViewHolder.image0.setTag(R.id.picCheckBtn1, image0CheckBtn);
			ImageView image1CheckBtn = (ImageView) view.findViewById(R.id.picCheckBtn2);
			myViewHolder.image1.setTag(R.id.picCheckBtn2, image1CheckBtn);
			image1CheckBtn.setImageResource(R.drawable.photo_grid_checkbox_checked);
			image1CheckBtn.setVisibility(bStatus[2] ? View.VISIBLE : View.GONE);

			ImageView image2CheckBtn = (ImageView) view.findViewById(R.id.picCheckBtn3);
			myViewHolder.image2.setTag(R.id.picCheckBtn3, image2CheckBtn);
			image2CheckBtn.setImageResource(R.drawable.photo_grid_checkbox_checked);
			image2CheckBtn.setVisibility(bStatus[3] ? View.VISIBLE : View.GONE);

			// 设置位置
			myViewHolder.image0.setTag(position);
			myViewHolder.image1.setTag(position);
			myViewHolder.image2.setTag(position);

			myViewHolder.image0.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					int position = (Integer) v.getTag();
					boolean[] bStatus = mStatus.get(position);
					bStatus[1] = !bStatus[1];
					ImageView checkBtn = (ImageView) v.getTag(R.id.picCheckBtn1);
					if (bStatus[1]) {
						checkBtn.setVisibility(View.VISIBLE);
					} else {
						checkBtn.setVisibility(View.GONE);
					}
				}

			});
			myViewHolder.image1.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					int position = (Integer) v.getTag();
					boolean[] bStatus = mStatus.get(position);
					bStatus[2] = !bStatus[2];

					ImageView checkBtn = (ImageView) v.getTag(R.id.picCheckBtn2);
					if (bStatus[2]) {
						checkBtn.setVisibility(View.VISIBLE);
					} else {
						checkBtn.setVisibility(View.GONE);
					}
				}

			});
			myViewHolder.image2.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					int position =  (Integer) v.getTag();
					boolean[] bStatus = mStatus.get(position);
					bStatus[3] = !bStatus[3];

					ImageView checkBtn = (ImageView) v.getTag(R.id.picCheckBtn3);
					if (bStatus[3]) {
						checkBtn.setImageResource(R.drawable.photo_grid_checkbox_checked);
						checkBtn.setVisibility(View.VISIBLE);
					} else {
						checkBtn.setVisibility(View.GONE);
					}
				}

			});

			if (information[0].startsWith(strNullMagic)) {
				myViewHolder.monthText.setText("");
				myViewHolder.yearText.setText("");
			} else if (information[0].startsWith(strMonMagic)) {
				String strTmp[], strYear, strMonth, strDay;
				strTmp = information[0].substring(strMonMagic.length()).split(":");
				strYear = strTmp[0];
				strMonth = strTmp[1];
				strDay = strTmp[2];

				myViewHolder.monthText.setText(strDay);
				myViewHolder.yearText.setText(strYear + "年" + strMonth + "月");
			}

			ArrayList<Bitmap> newBitmap = null;
			if (bitmapArrary.size() > position) {
				newBitmap = bitmapArrary.get(position);
			}
			if (newBitmap == null) {
				newBitmap = new ArrayList<Bitmap>();
				bitmapArrary.add(newBitmap);
				newBitmap.add(null);
				newBitmap.add(null);
				newBitmap.add(null);

			}

			myViewHolder.image0.setVisibility(View.INVISIBLE);
			if (information[1].startsWith(strNullMagic)) {
			} else if (information[1].startsWith(strPicMagic)) {

				Bitmap bitmapTmp = newBitmap.get(0);
				if (bitmapTmp == null) {
					bitmapTmp = getImageThumbnail(information[1].substring(strPicMagic.length()), mBitmapWidth,
							mBitmapHeigh);
					newBitmap.set(0, bitmapTmp);
				}
				myViewHolder.image0.setImageBitmap(bitmapTmp);
				myViewHolder.image0.setVisibility(View.VISIBLE);
			}
			myViewHolder.image1.setVisibility(View.INVISIBLE);
			if (information[2].startsWith(strNullMagic)) {

			} else if (information[2].startsWith(strPicMagic)) {
				Bitmap bitmapTmp = newBitmap.get(1);
				if (bitmapTmp == null) {
					bitmapTmp = getImageThumbnail(information[2].substring(strPicMagic.length()), mBitmapWidth,
							mBitmapHeigh);
					newBitmap.set(1, bitmapTmp);
				}
				myViewHolder.image1.setImageBitmap(bitmapTmp);
				myViewHolder.image1.setVisibility(View.VISIBLE);
			}
			myViewHolder.image2.setVisibility(View.INVISIBLE);
			if (information[3].startsWith(strNullMagic)) {

			} else if (information[3].startsWith(strPicMagic)) {
				Bitmap bitmapTmp = newBitmap.get(2);
				if (bitmapTmp == null) {
					bitmapTmp = getImageThumbnail(information[3].substring(strPicMagic.length()), mBitmapWidth,
							mBitmapHeigh);
					newBitmap.set(2, bitmapTmp);
				}

				myViewHolder.image2.setImageBitmap(bitmapTmp);
				myViewHolder.image2.setVisibility(View.VISIBLE);
			}

			return view;
		}

		private Bitmap getImageThumbnail(String imagePath, int width, int height) {
			Bitmap bitmap = null;
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			// 获取这个图片的宽和高，注意此处的bitmap为null
			bitmap = BitmapFactory.decodeFile(imagePath, options);
			options.inJustDecodeBounds = false; // 设为 false
			// 计算缩放比
			int h = options.outHeight;
			int w = options.outWidth;
			int beWidth = w / width;
			int beHeight = h / height;
			int be = 1;
			if (beWidth < beHeight) {
				be = beWidth;
			} else {
				be = beHeight;
			}
			if (be <= 0) {
				be = 1;
			}
			options.inSampleSize = be;
			// 重新读入图片，读取缩放后的bitmap，注意这次要把options.inJustDecodeBounds 设为 false
			bitmap = BitmapFactory.decodeFile(imagePath, options);
			// 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象
			bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
			return bitmap;
		}
	}

	private String GetDateString(Long lHashCode) {
		Integer nYear = (int) (lHashCode >> 32);

		Long temp1 = (lHashCode & 0xFFFFFF);
		Integer nMonth = (int) (temp1 >> 8);
		Integer nday = (int) (temp1 & 0x00000000000000FF);

		String str = nYear.toString() + ":";
		str += nMonth.toString() + ":";
		str += nday.toString();

		return str;
	}

	// 获取相册图片路径
	private String[][] GetPhotoPathStringArrary() {

		File externalDataDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
		String DICMPath = externalDataDir.getAbsolutePath();

		// 开始遍历文件夹,得到文件列表,..
		File dir = new File(DICMPath);
		File[] file = dir.listFiles();
		ArrayList<File> listDirectory = new ArrayList<File>();
		ArrayList<File> listFiles = new ArrayList<File>();
		for (int i = 0; i < file.length; i++) {
			if (file[i].isDirectory())
				listDirectory.add(file[i]);
			else {
				listFiles.add(file[i]);
			}
		}
		File tmp;
		/*
		 * while (!listDirectory.isEmpty()) { tmp = listDirectory.remove(0); if
		 * (tmp.isDirectory()) { file = tmp.listFiles(); if (file == null) {
		 * continue; } for (int i = 0; i < file.length; i++) { if
		 * (file[i].isDirectory()) listDirectory.add(file[i]); else
		 * listFiles.add(file[i]); } } else { listFiles.add(tmp); } }
		 */

		ArrayList<File> cameraListDirectory = new ArrayList<File>();
		while (!listDirectory.isEmpty()) {
			tmp = listDirectory.remove(0);
			if (tmp.isDirectory()) {
				if (tmp.getAbsolutePath().contains("Camera")) {
					cameraListDirectory.add(tmp);
					break;
				}

			}
		}

		while (!cameraListDirectory.isEmpty()) {
			tmp = cameraListDirectory.remove(0);
			if (tmp.isDirectory()) {
				file = tmp.listFiles();
				if (file == null) {
					continue;
				}
				for (int i = 0; i < file.length; i++) {
					if (file[i].isDirectory()) {
						cameraListDirectory.add(file[i]);
					} else {
						listFiles.add(file[i]);
					}

				}

			}
		}

		HashMap<Long, ArrayList<String>> dateFilePathMap = new HashMap<Long, ArrayList<String>>();

		for (int i = 0; i < listFiles.size(); i++) {
			// 根据文件的时间创建hash表
			File currentFile = listFiles.get(i);
			long fileTime = currentFile.lastModified();
			// java.util.Date date = new java.util.Date(fileTime);
			Date date = new Date(fileTime);

			java.util.Calendar c = java.util.Calendar.getInstance();
			c.setTime(date);

			int nYear = c.get(java.util.Calendar.YEAR);
			int nMonth = c.get(java.util.Calendar.MONTH) + 1;
			int nDay = c.get(java.util.Calendar.DAY_OF_MONTH);

			Long lHash = (long) 0;
			Long lMonth = (long) 0;
			Long lDay = (long) 0;

			lHash = (long) nYear;
			lMonth = (long) nMonth;
			lDay = (long) nDay;

			Long ltmp1 = lMonth << 8;
			Long ltmp2 = ltmp1 | lDay;

			lHash = lHash << 32 | ltmp2;

			String strFileAbsolutePath = currentFile.getAbsolutePath();
			ArrayList<String> pathList = dateFilePathMap.get(lHash);

			if (pathList == null) {
				pathList = new ArrayList<String>();
				if (pathList != null) {
					pathList.add(strFileAbsolutePath);
					dateFilePathMap.put(lHash, pathList);
				}
			} else {
				pathList.add(strFileAbsolutePath);
			}
		}

		// 产生字符串数组
		int nTotalRow = 0;
		Iterator<Long> it = dateFilePathMap.keySet().iterator();
		while (it.hasNext()) {
			Long key = (Long) it.next();
			ArrayList<String> filePathList = dateFilePathMap.get(key);
			nTotalRow += (filePathList.size() + 2) / 3; // 每行4列 第1列显示月份
		}

		// 二维数组
		String[][] pathArray = new String[nTotalRow][4];
		for (int l = 0; l < nTotalRow; l++) {
			for (int c = 0; c < 4; c++)
				pathArray[l][c] = "photocrush_null";
		}

		int nCurrentRow = 0;
		it = dateFilePathMap.keySet().iterator();
		while (it.hasNext()) {
			Long key = (Long) it.next();
			ArrayList<String> filePathList = dateFilePathMap.get(key);

			// 填充数组
			pathArray[nCurrentRow][0] = "photocrush_month=" + GetDateString(key); // 显示日期
			int nNumOfRow = (filePathList.size() + 2) / 3;
			int nCurrentIndex = 0;
			for (int k = 0; k < nNumOfRow; k++) {

				for (int j = 1; j < 4 && nCurrentIndex != filePathList.size(); j++, nCurrentIndex++) {
					String strMagic = "photocrush_pic=" + filePathList.get(nCurrentIndex);
					pathArray[nCurrentRow + k][j] = strMagic;
				}
			}
			// 更新行
			nCurrentRow += nNumOfRow;
		}

		return pathArray;
	}

}
