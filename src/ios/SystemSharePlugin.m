/********* SystemSharePlugin.m Cordova Plugin Implementation *******/

#import <Cordova/CDV.h>


 

@interface SystemSharePlugin : CDVPlugin {
 
     
     CDVPluginResult* pluginResult;
}
 

- (void)share_txt:(CDVInvokedUrlCommand*)command;
- (void)share_image:(CDVInvokedUrlCommand*)command;
- (void)share_link:(CDVInvokedUrlCommand*)command;
 
@end


@implementation SystemSharePlugin


static NSString* myAsyncCallBackId = nil;
 

- (void)pluginInitialize {
 
}

- (void)share_txt:(CDVInvokedUrlCommand*)command
{
    myAsyncCallBackId = command.callbackId;

    NSString* txt =  [command.arguments objectAtIndex:0];
   
    //返回结果
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString: @"success"];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    
}

- (void)share_image:(CDVInvokedUrlCommand*)command
{
    myAsyncCallBackId = command.callbackId;

    NSString* path =  [command.arguments objectAtIndex:0];
    NSString* filename =  [command.arguments objectAtIndex:1];
    NSString* filetype =  [command.arguments objectAtIndex:2];

    
 
    //返回结果
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString: @"success"];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    
}
 

- (void)share_link:(CDVInvokedUrlCommand*)command
{
    myAsyncCallBackId = command.callbackId;

    NSString* thumbUrl =  [command.arguments objectAtIndex:0];
    NSString* webpageUrl =  [command.arguments objectAtIndex:1];
    NSString* title =  [command.arguments objectAtIndex:2];
    NSString* description =  [command.arguments objectAtIndex:3];
    NSString* thumb_img_type =  [command.arguments objectAtIndex:4];

   
 
    //返回结果
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString: @"success"];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    
}
 


-  (void)  sendCmd : (NSString *)msg
{
    if(myAsyncCallBackId != nil)
    {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString: msg ];
        //将 CDVPluginResult.keepCallback 设置为 true ,则不会销毁callback
        [pluginResult  setKeepCallbackAsBool:YES];
        [selfplugin.commandDelegate sendPluginResult:pluginResult callbackId: myAsyncCallBackId];

    }
}


@end
