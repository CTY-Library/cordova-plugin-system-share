var exec = require('cordova/exec');

 
exports.share_txt = function (title,txt,package_name, success, error) { 
        exec(success, error, 'SystemSharePlugin', 'share_txt', [title,txt,package_name]);    
};


exports.share_image = function (title,path,img_name,package_name, success, error) { 
    exec(success, error, 'SystemSharePlugin', 'share_image', [title,path,img_name,package_name]);    
};

 

exports.share_link = function (title, webpageUrl,package_name, success, error) { 
    exec(success, error, 'SystemSharePlugin', 'share_link', [title, webpageUrl, package_name]);    
};

 