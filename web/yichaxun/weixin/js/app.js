/**
 * Created by xuxk on 2017/6/8.
 */

var u = navigator.userAgent, app = navigator.appVersion;
var isAndroid = u.indexOf('Android') > -1 || u.indexOf('Linux') > -1; //android终端或者uc浏览器
var isIOS = !!u.match(/\(i[^;]+;( U;)? CPU.+Mac OS X/); //ios终端
alert('isIOS'+isIOS)
alert('isAndroid'+isAndroid)
var openApp = function(){
    var a = document.getElementById('link');
    alert(a)
    if(isAndroid){
        //判断是否是android
        $('#link').attr('href','privateboard://splash/instrumentsearch');
        a.click();
        var loadDateTime = Date.now();
        setTimeout(function () {
            var timeOutDateTime = Date.now();
            if (timeOutDateTime - loadDateTime < 1000) {
                window.location.href = "http://61.155.220.192/imtt.dd.qq.com/16891/9E2E3F059CCBDFE9A4258BC23749B5D8.apk?mkey=58b917130095fb87&f=ae08&c=0&fsname=com.huitong.privateboard_2.0.7_17.apk&csr=1bbd&p=.apk";
            }
        }, 2000);
    }else if(isIOS){
        //判断是否是ios
        $('#link').addClass('ios');
        $('#link').attr('href','medicallshare://medicalHtml/');
        a.click();
        var loadDateTime = Date.now();
        setTimeout(function () {
            var timeOutDateTime = Date.now();
            if (timeOutDateTime - loadDateTime < 1000) {
                window.location.href = "http://www.baidu.com";
            }
        }, 500);
    }
};

openApp();