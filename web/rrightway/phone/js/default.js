var rxw ={};
rxw.parseQueryStr = function(queryStr){
    var str=decodeURIComponent(queryStr);
    var arr=str.split("&");
    var obj = {};
    for(var i=0;i < arr.length;i++){
        var arrsub=arr[i].split("=");
        obj[arrsub[0]]=arrsub[1];
    }
    return obj;
}


rxw.formatDateTime=function (inputTime) {
    var date = new Date(inputTime);
    var y = date.getFullYear();
    var m = date.getMonth() + 1;
    m = m < 10 ? ('0' + m) : m;
    var d = date.getDate();
    d = d < 10 ? ('0' + d) : d;
    var h = date.getHours();
    h = h < 10 ? ('0' + h) : h;
    var minute = date.getMinutes();
    var second = date.getSeconds();
    minute = minute < 10 ? ('0' + minute) : minute;
    second = second < 10 ? ('0' + second) : second;
    return y + '-' + m + '-' + d+' '+h+':'+minute+':'+second;
};


rxw.alert=function (txt,fun,funParam){


        $("body").append('<div class="confirm"><div class="alert"><div>' + txt + '</div><p><a href="javascript:;" class="submit" style="width:100%"><i></i>确认</a></p></div><div class="con_bg opa50"></div></div>');

        $(".confirm").show();

        $(".submit").click(function () {
            if(fun)
                fun(funParam);
            $(".confirm").remove();
        });



}