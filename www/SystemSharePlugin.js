var exec = require('cordova/exec');

function AsArray (param) {
    if (param == null) {
      param = [];
    } else if (typeof param === 'string') {
      param = new Array(param);
    }
    return param;
};

exports.share_txt = function (title,txt,package_name,ui_page, success, error) { 
        exec(success, error, 'SystemSharePlugin', 'share_txt', [title,txt,package_name,ui_page]);    
};


exports.share_image = function (title,path,img_name,package_name,ui_page, success, error) { 
    exec(success, error, 'SystemSharePlugin', 'share_image', [title,path,img_name,package_name,ui_page]);    
};

 

exports.share_link = function (title, webpageUrl,package_name,ui_page, success, error) { 
    exec(success, error, 'SystemSharePlugin', 'share_link', [title, webpageUrl, package_name,ui_page]);    
};


exports.share_image_ios = function ( path , success, error) { 
    exec(success, error, 'SystemSharePlugin', 'share', [null, null, AsArray(path), null, ""]);    
};

