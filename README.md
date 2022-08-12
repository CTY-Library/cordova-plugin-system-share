# cordova-plugin-system-share
cordova system share  系统分享



### 1.安装命令

```
cordova plugin add https://github.com/lounai-chen/cordova-plugin-system-share   

``` 

### 2.使用方法


``` 

1.分享文字
SystemSharePlugin.share_txt('title','txt','package_name','ui_page',function(s){ },function(e){alert('error: '+e)}); 

2.分享图片
SystemSharePlugin.share_image('title','path','img_name','package_name','ui_page',function(s){ },function(e){alert('error: '+e)}); 
 
3.分享链接
SystemSharePlugin.share_link('title, 'webpageUrl','package_name','ui_page',function(s){ },function(e){alert('error: '+e)}); 

参数 package_name 如果传空,就是调用系统分享. 如果 package_name = 'com.tencent.wework' 就是调用企业微信分享 

当package_name不为空,ui_page参数可以指定打开特定的分享页面,例如: 

 ui_page = com.tencent.wework.launch.QyDiskShareLaunchActivity 分享到文件磁盘 

 ui_page = com.tencent.wework.launch.AppSchemeLaunchActivity 分享到联系人列表  
 

```
 

 
  

