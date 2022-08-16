package  com.plugin.huayu.noahSystemSharePlugin;


import android.Manifest;
import android.annotation.SuppressLint;
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
import android.util.Base64;
import android.util.Log;


import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.apache.cordova.camera.FileProvider;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class SystemSharePlugin extends CordovaPlugin {

  final SystemSharePlugin plugin = this;
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
      if(initPermissionCheck() == false){
        return false;
      }

      String title = args.getString(0);
      String path = args.getString(1);
      String img_name = args.getString(2);
      String package_name = args.getString(3);
      String ui_page = args.getString(4);

      String fileName = getFileName();
      File file = savePic(path,this.cordova.getContext(),1001,fileName);

      Uri fileUri = getUriForFile(this.cordova.getContext(), file);

      Intent imageIntent = new Intent(Intent.ACTION_SEND);

      imageIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
      imageIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
      imageIntent.putExtra(Intent.EXTRA_SUBJECT, fileName);


      imageIntent.setType("image/*");
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
            //安卓版本是否大于7.0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
              cordova.getContext().startActivity(Intent.createChooser(imageIntent, title));
            } else {
              imageIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(getFileUrl(cordova.getContext()), title)));
              cordova.getContext().startActivity(imageIntent);
            }
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

  /**
   * 保存图片
   *
   * @param context
   * @param dir
   * @param bitmap
   * @param fileName
   */
  @SuppressLint("SdCardPath")
  public static void saveBitmap(Context context, File dir, Bitmap bitmap, String fileName) {
    if (!dir.exists()) {
      dir.mkdir();
    }
    File file = new File(dir, fileName + ".png");
    FileOutputStream out;
    try {
      out = new FileOutputStream(file);
      if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
        out.flush();//空方法体，此输出流并强制写出所有缓冲的输出字节
        out.close();//关闭流
        bitmap.recycle();
      }
    } catch (Exception e) {
      e.printStackTrace();

    }
    //发送广播更新，扫描某个文件(文件绝对路径，必须是以 Environment.getExternalStorageDirectory() 方法的返回值开头)
    context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + file.getAbsolutePath())));
  }


  /**
   * 当前时间戳作为分享的文件名
   */
  private static String getFileName() {
    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
  }

  /**
   * 返回uri
   */
  private static Uri getUriForFile(Context context, File file) {
    //应用包名.provider
    String authority = context.getPackageName().concat(".provider");
    Uri fileUri = FileProvider.getUriForFile(context, authority, file);
    return fileUri;
  }

  /**
   * 返回文件夹
   */
  private static File getFileUrl(Context context) {
    File root = context.getFilesDir();
    File dir = new File(root, "Download/");
    if (!dir.exists()) {
      //创建失败
      if (!dir.mkdir()) {
       // Log.e(TAG, "createBitmapPdf: 创建失败");
      }
    }
    return dir;
  }


  public static File savePic(final String imgurl, final Context context, final int savePathType, final String fileName) {

    File    file = url2bitmap(imgurl, context, savePathType, fileName);

    return file ;
  }

  /**
   * url转bitmap对象
   * @param
   * @param context
   * @param savePathType
   * @param fileName
   */
  public static File url2bitmap(String imgPath, Context context, int savePathType, String fileName) {
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

//          //系统相册目录
//          String galleryPath = Environment.getExternalStorageDirectory()
//            + File.separator + Environment.DIRECTORY_DCIM
//            + File.separator + "Camera" + File.separator;
//
//          // 声明文件对象
//          File file = null;
//          // 声明输出流
//          FileOutputStream outStream = null;
//
//          // 如果有目标文件，直接获得文件对象，否则创建一个以filename为名称的文件
//          file = new File(galleryPath, fileName + ".png");
//
//          // 获得文件相对路径
//          fileName = file.toString();
//          // 获得输出流，如果文件中有内容，追加内容
//          outStream = new FileOutputStream(fileName);
//          if (null != outStream) {
//            b.compress(Bitmap.CompressFormat.PNG, 90, outStream);
//          }
//          outStream.close();
//
//          path = fileName;


          File path2 = getFileUrl(context);
          saveBitmap(context, path2,  b, fileName);
          File file2 = new File(path2 + "/" + fileName + ".png");

          return  file2;
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

    return  null;

  }



  private boolean  initPermissionCheck()
  {
    if (!permissionCheck()) {
      if (Build.VERSION.SDK_INT >= 23) {
         ActivityCompat.requestPermissions(this.cordova.getActivity(), permissionManifest, PERMISSION_REQUEST_CODE);
      } else {
        mCallbackContext.error("0|请授权");
        return false;
      }
    }
    return  true;
  }

  private int mNoPermissionIndex = 0;
  private final int PERMISSION_REQUEST_CODE = 1;
  private final String[] permissionManifest = {
    Manifest.permission.WRITE_EXTERNAL_STORAGE,
    Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.INTERNET,
  };



  private boolean permissionCheck() {
    int permissionCheck = PackageManager.PERMISSION_GRANTED;
    String permission;
    for (int i = 0; i < permissionManifest.length; i++) {
      permission = permissionManifest[i];
      mNoPermissionIndex = i;
      if (PermissionChecker.checkSelfPermission(this.cordova.getContext(), permission)
        != PermissionChecker.PERMISSION_DENIED) {
        permissionCheck = PackageManager.PERMISSION_DENIED;
      }
    }
    if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
      return false;
    } else {
      return true;
    }
  }




}
