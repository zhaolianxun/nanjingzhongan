var ossApi = new Object();///shijian/oss/var result;ossApi.fileUpload = function(path,formId) {    var fd = new FormData(document.getElementById(formId));    $.ajax({        url: 'http://passion.njshangka.com/oss/file/upload?path='+path,        type: 'POST',        data: fd,        async: false,        cache: false,        contentType: false,        processData: false,        success: function(data) {            if(data.code == 0){                result =  data.data.url ;            }else                alert(data.msg)        },        error: function(xhr,msg,reasonString){            alert('请求错误，请稍后再试...');            console.log('error回调函数...响应完成但存在问题');            console.log(arguments);        }    });return result;};