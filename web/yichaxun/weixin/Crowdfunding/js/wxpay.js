/**
 * Created by xuxk on 2017/5/27.
 */
//微信支付JSSDK
function onBridgeReady(appId,timeStamp,nonceStr,pack,paySign){
    WeixinJSBridge.invoke(
        'getBrandWCPayRequest', {
            "appId" : appId,     //公众号名称，由商户传入
            "timeStamp":timeStamp,         //时间戳，自1970年以来的秒数
            "nonceStr" : nonceStr, //随机串
            "package" : pack,
            "signType" : "MD5",         //微信签名方式：
            "paySign" : paySign //微信签名
        },
        function(res){
            //alert(res.err_msg);
            if(res.err_msg == "get_brand_wcpay_request:ok" ) {
                //alert('123')
                window.location.reload();
            }     // 使用以上方式判断前端返回,微信团队郑重提示：res.err_msg将在用户支付成功后返回    ok，但并不保证它绝对可靠。
        }
    );
}
