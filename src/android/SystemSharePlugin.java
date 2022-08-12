package  com.plugin.huayu.noahSystemSharePlugin;


import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;

import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.util.Base64;
import android.util.Log; 


import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class SystemSharePlugin extends CordovaPlugin {

  final LivePlugin plugin = this;
  private static CallbackContext mCallbackContext;
  int stringId;


  @RequiresApi(api = Build.VERSION_CODES.M)
  @Override
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);
    stringId = this.cordova.getContext().getApplicationInfo().labelRes;
    Context context = this.cordova.getActivity().getApplicationContext();
    ApplicationInfo applicationInfo = null;
    try {
      applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(),
        PackageManager.GET_META_DATA);
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    } 
  }


  @RequiresApi(api = Build.VERSION_CODES.M)
  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    mCallbackContext = callbackContext;    //拿到回调对象并保存
    //package_name = com.tencent.wework 企业微信
    //ui_page = com.tencent.wework.launch.QyDiskShareLaunchActivity 分享到文件磁盘
    //ui_page = com.tencent.wework.launch.AppSchemeLaunchActivity 分享到联系人列表
    if (action.equals("share_txt")) {

      String title = args.getString(0);
      String msg = args.getString(1);
      String package_name = args.getString(2);
      String ui_page = args.getString(3);
   
      Intent textIntent = new Intent(Intent.ACTION_SEND);
      textIntent.putExtra(Intent.EXTRA_TEXT, msg);
      textIntent.setType("text/plain");
      if(package_name.length()>0) {
        textIntent.setPackage(package_name);
        if(ui_page.length()>0) {
          ComponentName comp = new ComponentName(package_name,ui_page);
          textIntent.setComponent(comp);
        }
      }

      cordova.getActivity().runOnUiThread(new Runnable() {
        public void run() {
          try {
            cordova.startActivityForResult(plugin, Intent.createChooser(textIntent, title), 0);
            callJS("成功");
          } catch (Exception e) {
            callbackContext.error(e.getMessage());
          }
        }
      });
 
      return true;
    }
    else if(action.equals("share_image")) {
      String title = args.getString(0);
      String path = args.getString(1);
      String img_name = args.getString(2);
      String package_name = args.getString(3);
      String ui_page = args.getString(4);

      path = savePic(path,this.cordova.getContext(),1002,img_name);
      
      Intent imageIntent = new Intent(Intent.ACTION_SEND);
      imageIntent.putExtra(Intent.EXTRA_STREAM, path);
      imageIntent.setType("image/jpeg");
      if(package_name.length()>0) {
        imageIntent.setPackage(package_name);
        if(ui_page.length()>0) {
          ComponentName comp = new ComponentName(package_name,ui_page);
          imageIntent.setComponent(comp);
        }
      }

      cordova.getActivity().runOnUiThread(new Runnable() {
        public void run() {
          try {
            cordova.startActivityForResult(plugin, Intent.createChooser(imageIntent, title), 0);
            callJS("成功");
          } catch (Exception e) {
            callbackContext.error(e.getMessage());
          }
        }
      });
  
      return true;
    }   
    else if(action.equals("share_link")) {   
     
      String title =  args.getString(0);     
      String webpageUrl = args.getString(1);
      String package_name = args.getString(2);
      String ui_page = args.getString(3);

      Intent textIntent = new Intent(Intent.ACTION_SEND);
      textIntent.putExtra(Intent.EXTRA_TEXT, title+'\n'+webpageUrl);
      textIntent.setType("text/plain");
      if(package_name.length()>0) {
        textIntent.setPackage(package_name);
        if(ui_page.length()>0) {
          ComponentName comp = new ComponentName(package_name,ui_page);
          textIntent.setComponent(comp);
        }
      }

      cordova.getActivity().runOnUiThread(new Runnable() {
        public void run() {
          try {
            cordova.startActivityForResult(plugin, Intent.createChooser(textIntent, title), 0);
            callJS("成功");
          } catch (Exception e) {
            callbackContext.error(e.getMessage());
          }
        }
      });

      return true;
    }
 
      return false;
  }



  public static void callJS(String message) {
    if (mCallbackContext != null) {
      PluginResult dataResult = new PluginResult(PluginResult.Status.OK, message);
      dataResult.setKeepCallback(true);// 非常重要
      mCallbackContext.sendPluginResult(dataResult);
    }
  }
 


  private static final String TAG = "PicSaveUtil";
  final static int SAVE_PATH_TYPE_DCIM = 1001;
  final static int SAVE_PATH_TYPE_DATA = 1002;
  public static String savePic(final String imgurl, final Context context, final int savePathType, final String fileName) {

    String path = "";
//    new Thread(new Runnable() {
//      @Override
//      public void run() {
        path = url2bitmap(imgurl, context, savePathType, fileName);
//      }
//    }).start();
    return path ;
  }

  /**
   * url转bitmap对象
   * @param
   * @param context
   * @param savePathType
   * @param fileName
   */
  public static String url2bitmap(String imgPath, Context context, int savePathType, String fileName) {
   String path = "";
    HttpURLConnection conn=null;
    InputStream is=null;
    try {
      URL url=new URL(imgPath);
      //开启连接
      conn=(HttpURLConnection) url.openConnection();
      //设置连接超时
      conn.setConnectTimeout(5000);
      //设置请求方式
      conn.setRequestMethod("GET");
      //conn.connect();
      if(conn.getResponseCode()==200){
        is=conn.getInputStream();
        Bitmap b= BitmapFactory.decodeStream(is);
        if (b != null) {
          //saveImageToGallery(context, b);
          if(savePathType == SAVE_PATH_TYPE_DCIM) {
            path =  addBitmapToAlbum(context, b, fileName, "jpg", Bitmap.CompressFormat.JPEG);
          } else if (savePathType == SAVE_PATH_TYPE_DATA) {
            path = saveImageToGallery(context, b, fileName);
          }

        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }finally{
      try {
        //用完记得关闭
        is.close();
        conn.disconnect();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    return path;

  }


  public static String  addBitmapToAlbum(Context context, Bitmap bitmap, String displayName, String mimeType, Bitmap.CompressFormat compressFormat) {
    ContentValues values = new ContentValues();
    values.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName);
    values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM);
    } else {
      values.put(MediaStore.MediaColumns.DATA, Environment.getExternalStorageDirectory().getPath() + "/"
              +  Environment.DIRECTORY_DCIM + "/" + displayName);
    }
    ContentResolver resolver = context.getContentResolver();
    Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    if (uri != null) {
      OutputStream outputStream = null;
      try {
        outputStream = resolver.openOutputStream(uri);
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
      if (outputStream != null) {
        bitmap.compress(compressFormat, 100, outputStream);
        try {
          outputStream.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return  uri.getPath();
  }

  public static String saveImageToGallery(Context context, Bitmap bmp, String fileName) {
    Log.d(TAG, "saveImageToGallery: 保存了图片");
    // 首先保存图片
    File appDir = new File(context.getExternalFilesDir("").getAbsoluteFile() + "/pics");
    if (!appDir.exists()) {
      appDir.mkdir();
    }
    File file = new File(appDir, fileName);
    try {
      FileOutputStream fos = new FileOutputStream(file);
      bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
      fos.flush();
      fos.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return  appDir+"/"+fileName;
  }


}
