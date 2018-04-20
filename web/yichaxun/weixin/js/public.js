/**
 * Created by xuxk on 2017/5/24.
 */
//转换时间
function getLocalTime(nS) {
    return new Date(parseInt(nS)).toLocaleString().replace(/:\d{1,2}$/, ' ');
}
//获取回答时间
function getDateDiff(dateTimeStamp){
    var minute = 1000 * 60;
    var hour = minute * 60;
    var day = hour * 24;
    var now = new Date().getTime();


    var diffValue = dateTimeStamp-now;

    if(diffValue < 0){
        result=''+ 0;
        return result;}else{
        var dayC = diffValue/day;
        if(dayC >= 1){
            result=""+ parseInt(dayC) ;
        }else if(dayC<1){
            result=''+ parseInt(1);
        }
    }
    return result;
}
//将0123456789转换成一二三四五六七八九
function atoc(numberValue){
    var numberValue=new String(Math.round(numberValue*100)); // 数字金额
    var chineseValue=""; // 转换后的汉字金额
    var String1 = "零一二三四五六七八九十"; // 汉字数字
    var len=numberValue.length; // numberValue 的字符串长度
    var Ch1; // 数字的汉语读法
    var Ch2; // 数字位的汉字读法
    var nZero=0; // 用来计算连续的零值的个数
    var String3; // 指定位置的数值
    if(len>15){
        return "";
    }
    if (numberValue==0){
        chineseValue = "零";
        return chineseValue;
    }
    for(var i=0; i<len; i++){
        String3 = parseInt(numberValue.substr(i, 1),10); // 取出需转换的某一位的值
        if ( i != (len - 3) && i != (len - 7) && i != (len - 11) && i !=(len - 15) ){
            if ( String3 == 0 ){
                Ch1 = "";
                Ch2 = "";
                nZero = nZero + 1;
            }
            else if ( String3 != 0 && nZero != 0 ){
                Ch1 = "零" + String1.substr(String3, 1);
                nZero = 0;
            }
            else{
                Ch1 = String1.substr(String3, 1);
                nZero = 0;
            }
        }

        else{ // 该位是万亿，亿，万，元位等关键位
            if( String3 != 0 && nZero != 0 ){
                Ch1 = "零" + String1.substr(String3, 1);
                nZero = 0;
            }
            else if ( String3 != 0 && nZero == 0 ){
                Ch1 = String1.substr(String3, 1);
                nZero = 0;
            }
            else if( String3 == 0 && nZero >= 3 ){
                Ch1 = "";
                Ch2 = "";
                nZero = nZero + 1;
            }
            else{
                Ch1 = "";
                nZero = nZero + 1;
            }
            if( i == (len - 11) || i == (len - 3)){ // 如果该位是亿位或元位，则必须写上
            }
        }
        chineseValue = chineseValue + Ch1 ;
    }
    if ( String3 == 0 ){ // 最后一位（分）为0时，加上“整”
        chineseValue = chineseValue ;
    }
    console.log(chineseValue)
    return chineseValue;
}

//时间转换
function getLocalTime(nS) {
    var datas= new Date(parseInt(nS)).toLocaleString().replace(/:\d{1,2}$/, ' ');
    var m="上"
    var n="下"
       if( datas.split(m)[0].length<12){
           return datas.split(m)[0]
       }else{
           return datas.split(n)[0]
       }

}
function _getLocalTime(nS) {
    //alert( new Date(parseInt(nS)).toLocaleString());
    var datas= new Date(parseInt(nS)).toLocaleString().replace(/:\d{1,2}$/, ' ');
    //alert(datas)
    var data_=datas.replace('/','-').replace('/','-');
    var m="上"
    var n="下"
    if( data_.split(m)[0].length<12){
        return data_.split(m)[0]
    }else{
        return data_.split(n)[0]
    }

}

//(function($) {
//    $.extend({
//        myTime: {
//            /**
//             * 当前时间戳
//             * @return <int>    unix时间戳(秒)
//             */
//            CurTime: function(){
//                return Date.parse(new Date())/1000;
//            },
//            /**
//             * 日期 转换为 Unix时间戳
//             * @param <string> 2014-01-01 20:20:20 日期格式
//             * @return <int>    unix时间戳(秒)
//             */
//            DateToUnix: function(string) {
//                var f = string.split(' ', 2);
//                var d = (f[0] ? f[0] : '').split('-', 3);
//                var t = (f[1] ? f[1] : '').split(':', 3);
//                return (new Date(
//                        parseInt(d[0], 10) || null,
//                        (parseInt(d[1], 10) || 1) - 1,
//                        parseInt(d[2], 10) || null,
//                        parseInt(t[0], 10) || null,
//                        parseInt(t[1], 10) || null,
//                        parseInt(t[2], 10) || null
//                    )).getTime() / 1000;
//            },
//            /**
//             * 时间戳转换日期
//             * @param <int> unixTime  待时间戳(秒)
//             * @param <bool> isFull  返回完整时间(Y-m-d 或者 Y-m-d H:i:s)
//             * @param <int> timeZone  时区
//             */
//            UnixToDate: function(unixTime, isFull, timeZone) {
//                if (typeof (timeZone) == 'number')
//                {
//                    unixTime = parseInt(unixTime) + parseInt(timeZone) * 60 * 60;
//                }
//                var time = new Date(unixTime * 1000);
//                var ymdhis = "";
//                ymdhis += time.getUTCFullYear() + "-";
//                ymdhis += (time.getUTCMonth()+1) + "-";
//                ymdhis += time.getUTCDate();
//                if (isFull === true)
//                {
//                    ymdhis += " " + time.getUTCHours() + ":";
//                    ymdhis += time.getUTCMinutes() + ":";
//                    ymdhis += time.getUTCSeconds();
//                }
//                //alert(ymdhis)
//                return ymdhis;
//            }
//        }
//    });
//})(jQuery);


//时间转换,适应度比较好
function   formatDate(now)
{
    var   now= new Date(now*1000);
    var   year=now.getFullYear();
    var   month=now.getMonth()+1;
    var   date=now.getDate();
    var   hour=now.getHours();
    var   minute=now.getMinutes();
    var   second=now.getSeconds();
    return   year+"年"+fixZero(month,2)+"月"+fixZero(date,2)+"日   "+fixZero(hour,2)+":"+fixZero(minute,2)+":"+fixZero(second,2);
}
function   _formatDates(now)
{
    var   now= new Date(now*1000);
    var   year=now.getFullYear();
    var   month=now.getMonth()+1;
    var   date=now.getDate();
    var   hour=now.getHours();
    var   minute=now.getMinutes();
    var   second=now.getSeconds();
    return   year+"-"+fixZero(month,2)+"-"+fixZero(date,2)+"   "+fixZero(hour,2)+":"+fixZero(minute,2)+":"+fixZero(second,2);
}
function   _formatDate(now)
{      var   now= new Date(now);
    var   year=now.getFullYear();
    var   month=now.getMonth()+1;
    var   date=now.getDate();
    var   hour=now.getHours();
    var   minute=now.getMinutes();
    var   second=now.getSeconds();
    return   year+"/"+fixZero(month,2)+"/"+fixZero(date,2)
}
function   formatDates(now)
{      var   now= new Date(now);
    var   year=now.getFullYear();
    var   month=now.getMonth()+1;
    var   date=now.getDate();
    var   hour=now.getHours();
    var   minute=now.getMinutes();
    var   second=now.getSeconds();
    return   year+"-"+fixZero(month,2)+"-"+fixZero(date,2)
}
function   _formatDatesNoS(now)
{
    var   now= new Date(now*1000);
    var   year=now.getFullYear();
    var   month=now.getMonth()+1;
    var   date=now.getDate();
    var   hour=now.getHours();
    var   minute=now.getMinutes();
    var   second=now.getSeconds();
    return   year+"-"+fixZero(month,2)+"-"+fixZero(date,2)+"   "+fixZero(hour,2)+":"+fixZero(minute,2);
}
//时间如果为单位数补0
function fixZero(num,length){
    var str=""+num;
    var len=str.length;
    var s="";
    for(var i=length;i-->len;){         s+="0";     }
    return s+str; }


//日期转换时间戳
 function dataString(string) {
                var f = string.split(' ', 2);
                var d = (f[0] ? f[0] : '').split('-', 3);
                var t = (f[1] ? f[1] : '').split(':', 3);
                return (new Date(
                        parseInt(d[0], 10) || null,
                        (parseInt(d[1], 10) || 1) - 1,
                        parseInt(d[2], 10) || null,
                        parseInt(t[0], 10) || null,
                        parseInt(t[1], 10) || null,
                        parseInt(t[2], 10) || null
                    )).getTime() / 1000;
            }

//rem自适应
//var dpr, rem, scale;
//var docEl = document.documentElement;
//var fontEl = document.createElement('style');
//var metaEl = document.querySelector('meta[name="viewport"]');
//dpr = window.devicePixelRatio || 1;
//
//rem = docEl.clientWidth * dpr / 10;//跟换宽度设置offsetWidth 代替clientWidth
//console.log("rem="+ docEl.clientWidth)
//scale = 1 / dpr;
//// 设置viewport，进行缩放，达到高清效果
//metaEl.setAttribute('content', 'width=' + dpr * docEl.clientWidth + ',initial-scale=' + scale + ',maximum-scale=' + scale + ', minimum-scale=' + scale + ',user-scalable=no');
//// 设置data-dpr属性，留作的css hack之用
//docEl.setAttribute('data-dpr', dpr);
//// 动态写入样式
//docEl.firstElementChild.appendChild(fontEl);
//fontEl.innerHTML = 'html{font-size:' + rem + 'px!important;}';
//// 给js调用的，某一dpr下rem和px之间的转换函数
//window.rem2px = function(v) {
//    v = parseFloat(v);
//    return v * rem;
//};
//window.px2rem = function(v) {
//    v = parseFloat(v);
//    return v / rem;
//};
//window.dpr = dpr;
//window.rem = rem;