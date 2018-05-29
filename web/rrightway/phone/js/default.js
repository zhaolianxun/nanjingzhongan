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

rxw.moneyInputLimit = function(ele){
    $(ele).keydown(function (){
        if(event.keyCode == 110||event.keyCode == 190)
            if(event.target.value.indexOf('.')!= -1)
                return false
            else
                return true
        return true;
    }).bind('input propertychange', function() {
        this.value =this.value.replace(/[^\d\.]/g,'');
    });
}


rxw.phoneInputLimit = function(ele){
    $(ele).bind('input propertychange', function() {
        this.value =this.value.replace(/[^\d\.]/g,'');
    });
}

rxw.checkPhone = function (phone) {
    var myreg=/^[1][0-9]{10}$/;
    if (!myreg.test(phone)) {
        return false;
    } else {
        return true;
    }
}


rxw.checkIdcardnoStrict = function(idcardno){
    // 1 "验证通过!", 0 //校验不通过
    var format = /^(([1][1-5])|([2][1-3])|([3][1-7])|([4][1-6])|([5][0-4])|([6][1-5])|([7][1])|([8][1-2]))\d{4}(([1][9]\d{2})|([2]\d{3}))(([0][1-9])|([1][0-2]))(([0][1-9])|([1-2][0-9])|([3][0-1]))\d{3}[0-9xX]$/;
    //号码规则校验
    if(!format.test(idcardno)){
       return false;
    }
    //区位码校验
    //出生年月日校验   前正则限制起始年份为1900;
    var year = idcardno.substr(6,4),//身份证年
        month = idcardno.substr(10,2),//身份证月
        date = idcardno.substr(12,2),//身份证日
        time = Date.parse(month+'-'+date+'-'+year),//身份证日期时间戳date
        now_time = Date.parse(new Date()),//当前时间戳
        dates = (new Date(year,month,0)).getDate();//身份证当月天数
    if(time>now_time||date>dates){
       return false
    }
    //校验码判断
    var c = new Array(7,9,10,5,8,4,2,1,6,3,7,9,10,5,8,4,2);   //系数
    var b = new Array('1','0','X','9','8','7','6','5','4','3','2');  //校验码对照表
    var id_array = idcardno.split("");
    var sum = 0;
    for(var k=0;k<17;k++){
        sum+=parseInt(id_array[k])*parseInt(c[k]);
    }
    if(id_array[17].toUpperCase() != b[sum%11].toUpperCase()){
       return false;
    }
    return true;
}

rxw.checkIdcardnoLoose = function(idcardno){
    // 身份证号码为15位或者18位，15位时全为数字，18位前17位为数字，最后一位是校验位，可能为数字或字符X
    var reg = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/;
    return reg.test(idcardno)

}

rxw.checkUsername =function(username){
    var reg = /^[a-zA-Z0-9_-]{4,16}$/;
    return reg.test(username)
}