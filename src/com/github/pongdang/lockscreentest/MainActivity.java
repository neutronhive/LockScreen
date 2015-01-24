package com.github.pongdang.lockscreentest;


import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class MainActivity extends Activity implements OnClickListener{
	
	KeyguardManager km = null;
	KeyguardManager.KeyguardLock keyLock = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		if(km == null)
			km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
		
		if(keyLock == null)
			keyLock = km.newKeyguardLock(Context.KEYGUARD_SERVICE);
		
		
		SQLiteDatabase db = new DBHelper(this).getWritableDatabase();
		
		if(getSharedPreferences("pref1", MODE_PRIVATE).getBoolean("isFirstExecuted", true))
			db.execSQL("INSERT INTO lock VALUES ("+0+" );"); //custom Off
		
		db.close();
		
		setActivityOrder();
		
		((Button) findViewById(R.id.btn_on)).setOnClickListener(this);
		((Button) findViewById(R.id.btn_off)).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent(this, ScreenService.class);
		
		SQLiteDatabase db = new DBHelper(this).getWritableDatabase();
		
		switch (v.getId()) {
		case R.id.btn_on:
			startService(intent);
			Toast.makeText(this, "On", Toast.LENGTH_SHORT).show();
			db.execSQL("UPDATE lock SET custom = "+1+";");
			break;

		case R.id.btn_off:
			stopService(intent);
			Toast.makeText(this, "Off", Toast.LENGTH_SHORT).show();
			db.execSQL("UPDATE lock SET custom = "+0+";");
			break;
		}
		
		db.close();
	}
	
	void setActivityOrder() {
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		if(!pm.isScreenOn()) {
			
			SQLiteDatabase db = new DBHelper(getApplicationContext()).getReadableDatabase();
			
			Cursor c = db.rawQuery("SELECT custom FROM lock", null);
			
			while(c.moveToNext()) {
				
				switch (c.getInt(0)) {
				case 0:
					getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
					Toast.makeText(this, "Custom : "+c.getInt(0), Toast.LENGTH_SHORT).show();
					break;

				case 1:
					getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
					Toast.makeText(this, "Custom : "+c.getInt(0), Toast.LENGTH_SHORT).show();
					break;
				}
			}
			
			db.close();
		}
	}
	
	@Override
	protected void onPause() {
		setActivityOrder();
		super.onPause();
	};
	
	//Home Button 누르면 종료
	@Override
	protected void onUserLeaveHint() {
		finish();
		super.onUserLeaveHint();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		SharedPreferences.Editor edit = getSharedPreferences("pref1", MODE_PRIVATE).edit();
		edit.putBoolean("isFirstExecuted", false);
		edit.commit();
	}
}
