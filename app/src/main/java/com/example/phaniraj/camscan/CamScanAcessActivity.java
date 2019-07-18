package com.example.phaniraj.camscan;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

import com.intsig.csopen.sdk.CSOpenAPI;
import com.intsig.csopen.sdk.CSOpenAPIParam;
import com.intsig.csopen.sdk.CSOpenApiFactory;
import com.intsig.csopen.sdk.CSOpenApiHandler;
import com.intsig.csopen.sdk.ReturnCode;

public class CamScanAcessActivity  {

        private static final String Tag="CamScanAcessActivity";

        private final int REQ_CODE_PICK_IMAGE = 1;
        private final int REQ_CODE_CALL_CAMSCANNER = 2;
        private static final String APP_KEY = "KLPt0gTtYUyP0fTXV8aH44e7";

        static final String DIR_IMAGE = Environment.getExternalStorageDirectory().getAbsolutePath();

        private String mSourceImagePath;
        private String mOutputImagePath;
        private String mOutputPdfPath;
        private String mOutputOrgPath;

        private ImageView mImageView;
        private Bitmap mBitmap;
        private CSOpenAPI mApi;
        private Activity activity;

     public CamScanAcessActivity(Activity activity,CSOpenAPI api){
         this.activity = activity;
         this.mApi = api;
     }


    public void handleActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(Tag, "requestCode:"+requestCode+" resultCode:"+resultCode);
        if(requestCode == REQ_CODE_CALL_CAMSCANNER){
            mApi.handleResult(requestCode, resultCode, data, new CSOpenApiHandler() {

                @Override
                public void onSuccess() {
                    new AlertDialog.Builder(activity)
                            .setTitle(R.string.a_title_success)
                            .setMessage(R.string.a_msg_api_success)
                            .setPositiveButton(android.R.string.ok, null)
                            .create().show();
                    mBitmap = Utils.loadBitmap(mOutputImagePath);
                }

                @Override
                public void onError(int errorCode) {
                    String msg = handleResponse(errorCode);
                    new AlertDialog.Builder(activity)
                            .setTitle(R.string.a_title_reject)
                            .setMessage(msg)
                            .setPositiveButton(android.R.string.ok, null)
                            .create().show();
                }

                @Override
                public void onCancel() {
                    new AlertDialog.Builder(activity)
                            .setMessage(R.string.a_msg_cancel)
                            .setPositiveButton(android.R.string.ok, null)
                            .create().show();
                }
            });
        } else if (requestCode == REQ_CODE_PICK_IMAGE && resultCode == activity.RESULT_OK) {
            if (data != null) {
                Uri u = data.getData();
                Cursor c = activity.getContentResolver().query(u, new String[] { "_data" }, null, null, null);
                if (c == null || c.moveToFirst() == false) {
                    return;
                }
                mSourceImagePath = c.getString(0);
                c.close();
                goToCamScanner();
            }
        }
    }

    public void goToGallery() {

        try {
            if (mApi.isCamScannerInstalled()){
                Intent i = new Intent(Intent.ACTION_PICK,
                        Images.Media.EXTERNAL_CONTENT_URI);
                i.setType("image/*");
                activity.startActivityForResult(i, REQ_CODE_PICK_IMAGE);
            }
            else {
                new AlertDialog.Builder(activity)
                        .setMessage("Install CamScanner to Continue")
                        .setPositiveButton(android.R.string.ok, null)
                        .create().show();

            }
        }catch (ActivityNotFoundException e) {
            e.printStackTrace();
    }}

    private void goToCamScanner() {
        mOutputImagePath = DIR_IMAGE + "/scanned.jpg";
        mOutputPdfPath = DIR_IMAGE + "/scanned.pdf";
        mOutputOrgPath = DIR_IMAGE + "/org.jpg";
        try {
            FileOutputStream fos = new FileOutputStream(mOutputOrgPath);
            fos.write(3);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        CSOpenAPIParam param = new CSOpenAPIParam(mSourceImagePath,
                mOutputImagePath, mOutputPdfPath, mOutputOrgPath, 1.0f);
        boolean res = mApi.scanImage(activity, REQ_CODE_CALL_CAMSCANNER, param);
        android.util.Log.d(Tag, "send to CamScanner result: " + res);
    }

    private String handleResponse(int code){
        switch(code){
            case ReturnCode.OK:
                return activity.getString(R.string.a_msg_api_success);
            case  ReturnCode.INVALID_APP:
                return activity.getString(R.string.a_msg_invalid_app);
            case ReturnCode.INVALID_SOURCE:
                return activity.getString(R.string.a_msg_invalid_source);
            case ReturnCode.AUTH_EXPIRED:
                return activity.getString(R.string.a_msg_auth_expired);
            case ReturnCode.MODE_UNAVAILABLE:
                return activity.getString(R.string.a_msg_mode_unavailable);
            case ReturnCode.NUM_LIMITED:
                return activity.getString(R.string.a_msg_num_limit);
            case ReturnCode.STORE_JPG_ERROR:
                return activity.getString(R.string.a_msg_store_jpg_error);
            case ReturnCode.STORE_PDF_ERROR:
                return activity.getString(R.string.a_msg_store_pdf_error);
            case ReturnCode.STORE_ORG_ERROR:
                return activity.getString(R.string.a_msg_store_org_error);
            case ReturnCode.APP_UNREGISTERED:
                return activity.getString(R.string.a_msg_app_unregistered);
            case ReturnCode.API_VERSION_ILLEGAL:
                return activity.getString(R.string.a_msg_api_version_illegal);
            case ReturnCode.DEVICE_LIMITED:
                return activity.getString(R.string.a_msg_device_limited);
            case ReturnCode.NOT_LOGIN:
                return activity.getString(R.string.a_msg_not_login);
            default:
                return "Return code = " + code;
        }
    }
    }

