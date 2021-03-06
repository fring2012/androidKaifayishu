package com.study.czq.androidKaiFaYiShu;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.study.czq.androidKaiFaYiShu.ano.QueryPermission;
import com.study.czq.androidKaiFaYiShu.base.PermissionActivity;
import com.study.czq.androidKaiFaYiShu.entity.Book;
import com.study.czq.androidKaiFaYiShu.service.MyService;
import com.study.czq.androidKaiFaYiShu.utils.Trace;

import java.util.List;


@QueryPermission(permissions = {Manifest.permission.WRITE_CALENDAR})
public class MainActivity extends PermissionActivity {
    private final static String TAG = "MainActivity";
    private static final int MESSAGE_NEW_BOOK_ARRIVED = 1;
    private static IBookManager mBinder;
    private IOnNewBookArrivedListener mOnNewBookArrivedListener = new IOnNewBookArrivedListener.Stub(){

        @Override
        public void onNewBookArrived(Book newBook) throws RemoteException {
            mHandler.obtainMessage(MESSAGE_NEW_BOOK_ARRIVED,newBook)
                    .sendToTarget();
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);

    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent service = new Intent(this,MyService.class);
        bindService(service,mConnection,Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection mConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mBinder = IBookManager.Stub.asInterface(iBinder);

            try {

                List<Book> list = mBinder.getBookList();
                Trace.i(TAG,"query book list,list type:"
                        + list.getClass().getCanonicalName());
                Trace.i(TAG,"query book list:" + list.toString());
                Book newBook = new Book(3,"Android进阶","A230");
                mBinder.addBook(newBook);
                Trace.i(TAG,"add book:" + newBook);
                List<Book> newList = mBinder.getBookList();
                Trace.i(TAG,"query book list:" + newList.toString());
                mBinder.registerListener(mOnNewBookArrivedListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_NEW_BOOK_ARRIVED:
                    Trace.d(TAG,"receive new book :" + msg.obj);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getRLayoutId() {
        return R.layout.activity_main;
    }

    public void onClick(View view){

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }



    @Override
    protected void onDestroy() {
        if (mBinder != null
                && mBinder.asBinder().isBinderAlive()) {
            try {
                Trace.i(TAG,"unregister listener:");
                mBinder.unregisterListener(mOnNewBookArrivedListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        unbindService(mConnection);
        super.onDestroy();
    }

    private void getUriColumns(Uri uri){
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String[] columns = cursor.getColumnNames();
        for (String string : columns) {
            System.out.println(cursor.getColumnIndex(string)+" = "+string);
        }
    }
}
