var dgHeadImg;
function uploadImg(formId){
    var img = new FormData(document.getElementById(formId));
    $.ajax( {
        type: 'POST',
        data: img,
        async: false,
        cache: false,
        contentType: false,
        processData: false,
        url: 'http://121.40.168.181/fss/file/upload?path=yichaxun/oss/',
        success: function(data){
            if(data.rstCde == 0){
               alert('upload success!');
               /* var returnUrl = data.data.urls[0];
                $.ajax( {
                    type: 'POST',
                    data: {urls:returnUrl},
                    url: '/object/in/i/cb',
                    success: function(data){
                        if(data.rstCde == 0){
                            dgHeadImg = returnUrl;
                        }
                    },
                    error: function(){}
                } )*/
            }
        },
        error: function(){}
    } )
}
