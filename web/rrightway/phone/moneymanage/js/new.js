(function (doc, win) {
    var docEl = doc.documentElement, vsize, resizeEvt = 'orientationchange' in window ? 'orientationchange' : 'resize',
    recalc = function () {
        var clientWidth = docEl.clientWidth;  
        if (!clientWidth) return;    
        if (clientWidth <= 640 && clientWidth >= 320) {
          
          vsize = Math.ceil(18 * (clientWidth / 320)) + 'px'; 
        } 
        else
          vsize = "36px";        
        docEl.style.fontSize = vsize;
        vsize
        //docEl.style.opacity = 1;
    };
  if (!doc.addEventListener) return; 
  win.addEventListener(resizeEvt, recalc, false);    
  doc.addEventListener('DOMContentLoaded', recalc, false);
})(document, window);

function goback(obj) {
    if (typeof (obj) != "undefined")
        history.back();
}
function accesstx(str,fun,obj){
    $("body").append('<div class="win_lay" id="access_pop"><div class="table"><div class="tr"><div class="td auto"><div class="plr40 h446"><div class="center h214 line40"><div class="txtcontent f28">'+str+'</div></div></div><p class="clearfix alert sp2"><a href="javascript:;" class="cancle closesbt c999">我知道了</a></p></div></div></div></div>');
     noscroll();
     $("#access_pop").show();  

    // $("#access_pop").click(function(){
    //     autoscroll();        
    //     $(this).remove();
    // })
    $("#access_pop .closesbt").click(function () {
        //console.log(fun);
        autoscroll();     
        $("#access_pop").remove();
        if (typeof (fun) != "undefined") {
           fun(obj) 
       }  
    })
}

function accessconfirm(str, fun, obj) {
    $("body").append('<div class="win_lay" id="access_pop"><div class="table"><div class="tr"><div class="td auto"><div class="plr40 h446"><div class="center pt60 pb60 line40"><div class="txtcontent f28">' + str + '</div></div></div><p class="clearfix alert"><a href="javascript:;" class="cancle closesbt c999">取消</a><a href="javascript:;" class="cancle submit c2f97f0">确认</a></p></div></div></div></div>');
     noscroll();
     $("#access_pop").show();  
    $("#access_pop .submit").click(function () {
        //console.log(fun);
        autoscroll();     
        $("#access_pop").remove();
        if (typeof (fun) != "undefined") {
           fun(obj) 
       }  
    })
    $("#access_pop .closesbt").click(function () {
        //console.log(fun);
        autoscroll();     
        $("#access_pop").remove();
    })
}



function formatDateTime(inputTime) {
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
var scrollttt = 0;
function noscroll() {
    scrollttt = $(window).scrollTop();
    $(window).scrollTop(0);
    $("body,html").addClass("noscroll");
    //console.log(scrollttt);
}
function autoscroll() {
    $("body,html").removeClass("noscroll");
    $(window).scrollTop(scrollttt);
}

function sethtml(id, value) {
    $("#" + id).html(value);
}

//刷新到url页面
function freshpage(url) {
    setTimeout(function () {
        window.location.href = url;
    }, 500);
}

function geturl(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
     var r = window.location.search.substr(1).match(reg);
    if (r != null) return unescape(r[2]); return null;
}

function getDiffDate(m) {
    m -= (D = parseInt(m / 86400000)) * 86400000;
    m -= (H = parseInt(m / 3600000)) * 3600000;
    S = parseInt((m -= (M = parseInt(m / 60000)) * 60000) / 1000);
    if (M < 10 & M > 0) { M = '0' + M; }
    if (H < 10 & H > 0) { H = '0' + H; }
    if (S < 10) { S = '0' + S; }
    if (D != 0) { D += ':'; } else { D = "" }
    if (m > 0) return D + H + ':' + M + ':' + S;
    else { return "已经下架！"; }
}

$(function () {
    if ($(".clearinput").length > 0) {
        $(".clearinput").parent().append('<i class="ico_inputclose"></i>')
        $('.clearinput').keyup(function () {
            //console.log($(this).val());
            if ($(this).val().replace(/\s+/g, "") != "") {
                $(this).parent().find(".ico_inputclose").eq(0).show();
            } else {
                $(this).parent().find(".ico_inputclose").eq(0).hide();
            }
        });

        $('.clearinput').focus(function () {
            //console.log($(this).val());
            if ($(this).val().replace(/\s+/g, "") != "") {
                $(this).parent().find(".ico_inputclose").eq(0).show();
            } else {
                $(this).parent().find(".ico_inputclose").eq(0).hide();
            }
        });

        $(".ico_inputclose").click(function () {
            $(this).parent().find(".clearinput").eq(0).val("");
            $(this).parent().find(".clearinput").eq(0).focus();
            $(this).hide();
        })

    }

    //关闭底部弹出窗口
    if ($(".contact_fixed").length > 0) {
        $(".contact_fixed").each(function () {
            var obj = $(this);
            obj.find(".bg,.closesbt").click(function () {
                $(this).parents(".contact_fixed").removeClass("active");
                autoscroll();
            });
        })
      
    }

    /**加载中特效**/
    var appstr = '<div id="loadings"><i></i>加载中</div><a id="backtop" href="javascript:" data="body"><img src="images/blankbg.gif" /></a>';
    $("body").append(appstr);

    //返回顶部
   $(window).scroll(function () {
        if ($(window).scrollTop() > 100) {
            $("#backtop").addClass("up");
        }
        else {
            $("#backtop").removeClass("up");
        }
    });

    //数字统计
    if ($(".textarea_list").length > 0) {
        $(".textarea_list").each(function () {
            var obj = $(this);
            var maxcount = parseInt($(obj).find(".num").eq(0).attr("data"));
          var limitNum = 0; 
            var pattern = limitNum + '/' + maxcount;
          $(obj).find(".num").eq(0).html(pattern); 
            $(obj).find('.txt').eq(0).keyup(function () {
            var remain = $(this).val().length; 
                if (remain > maxcount) {
                    $(this).val($(this).val().substring(0, maxcount));
                } else {
              var result = limitNum + remain; 
                    pattern = result + '/' + maxcount;
            } 
            $(obj).find(".num").eq(0).html(pattern); 
          });
        })
    }

    if ($(".viewport").length > 0 && $("#home").length == 0)
    $(".viewport").prepend("<div class='h90'></div>");

    if ($(".com_footer").length > 0) {
        if (!$(".com_footer").hasClass("dis"))
            $("body").append("<div class='h90 foot mt40'></div>");
   }

    if ($(".win_lay").length > 0) {
        $(".win_lay .closesbt,.win_lay .winbg").click(function () { autoscroll(); $(this).parents(".win_lay").hide(); })
   }

   //顶部右键菜单
    if ($(".popmenu").length > 0) {
        $("body").append('<div class="pop_content"><div class="pop_menu_list alltrans"><b class="rotate45"></b><a href="/index.html" class="first"><i><img src="images/right_nav1.png" /></i>首页</a><a href="/type/index.aspx"><i><img src="images/right_nav2.png" /></i>分类</a><a href="/invite/index.aspx"><i><img src="images/right_nav3.png" /></i>推广</a><a href="/search/index.aspx"><i><img src="images/right_nav4.png" /></i>搜索</a><a href="/user/index.aspx"><i><img src="images/right_nav5.png" /></i>我的</a></div></div>');     
        $(".popmenu").click(function () {
            noscroll();
            $(".pop_content").addClass('open');
            $(".pop_menu_list").addClass("show");
            //$("html").toggleClass("noscroll");            
        })

        $(".pop_content").click(function () {
            autoscroll();
            $(".pop_menu_list").removeClass("show");
            setTimeout(function () { 
                $(".pop_content").removeClass('open');
            }, 300);
            //$("html").removeClass("noscroll");
        });
    }

    //tab默认切换显示
    if ($(".tabarea").length > 0) {
        $(".tabarea").each(function () {
            var objarea = $(this);
            setTimeout(function () { $(objarea).find(".tabtxt").eq(0).show().siblings().hide(); }, 100);
            $(this).find(".tabmenu a").each(function () {
                $(this).click(function () {
                    if (!$(this).hasClass("crently") && !$(this).hasClass("dis")) {
                        $(this).addClass("crently").siblings().removeClass("crently");
                        objarea.find(".tabtxt").eq($(this).index()).show().siblings().hide();
                    }
                })
            })
        })    
    }

   //底部悬浮菜单
    if ($(".footer").length > 0) {
        $("body").addClass("pb110");
        var crentobj = $(".footer a.crently img");
        if ($(crentobj).length > 0)
            $(crentobj).eq(0).attr("src", "/images/" + $(crentobj).eq(0).attr("data") + ".png");
   }

   //滚动效果
    $("#backtop").click(function () {
        if ($($(this).attr("data")).length > 0)
            objh = $($(this).attr("data")).offset().top;
        $("html,body").animate({ scrollTop: objh }, 500);
    })
    

    $(window).load(function () {
        setTimeout(function () {
            $("#loadings").remove();
        }, 100);
    });


    //倒计时
    if ($(".lefttime").length > 0) {
        setcountdown();
    }

    //警告，维权提现
    //$.ajax({
    //    type: "get",
    //    url: "/ashx/getnotice.ashx",  //警告信息
    //    data: "rad=" + Math.random(),
    //    async: false,
    //    success: function (res) {
    //
    //
    //
    //        //corfirm5("您有订单存在资金风险，请及时核实处理！", '确定', "/user/msg/msg.aspx?stype=3");
    //
    //        if (res == "1") {
    //
    //            //  corfirm("您有订单存在资金风险，请及时核实处理！", nuls, $(this));
    //            corfirm5("您有订单存在资金风险，请及时核实处理！", '查看', "/user/msg/msg.aspx?stype=3");
    //
    //        }
    //
    //        else {
    //
    //            $.ajax({
    //                type: "get",
    //                url: "/ashx/wqtx.ashx",
    //                data: "rad=" + Math.random(),
    //                async: false,
    //                success: function (res) {
    //
    //
    //
    //
    //
    //                    if (res != "0") {
    //
    //                        if (res != "商家")
    //                            corfirm5("亲，您有新的维权需要处理！", '确定', res);
    //                        else
    //                            corfirm2("【商家请用电脑打开网站进行操作哦！www.zuozuojie.com", "", nuls, $(this));
    //                        // corfirm("亲，您有新的维权需要处理！", nuls, $(this));
    //                        //  corfirm2("亲，您有新的维权需要处理！", "<a href='#' class='cf60'>马上去处理</a>", nuls, $(this));
    //
    //                    }
    //
    //                }
    //            });
    //
    //        }
    //
    //    }
    //});
});

function setcountdown() {
    $(".lefttime").each(function (i) {
        if (!$(this).hasClass("ovd")) {
            var obj = $(this);
            var severtime = $(obj).attr("date").replace(/-/g, "/");
            var stdf = getDiffDate(new Date(severtime) - new Date());
            
            if (stdf != "已经下架！") {
                //alert(stdf);
                
                $(obj).html(stdf);// 参数为两个时间的毫秒差
                
            }
            else {
                $(obj).html('已结束');              
            }
            if ($(this).length > 0) {
                var obj = $(this);
                var activeInterval = setInterval(function () {
                    //alert(i);
                    var severtime = $(obj).attr("date").replace(/-/g, "/");
                    //alert(severtime);     
                    var stdf = getDiffDate(new Date(severtime) - new Date());
                    
                    if (stdf != "已经下架！") {
                        //alert(stdf);
                        
                        $(obj).html(stdf);// 参数为两个时间的毫秒差
                        
                    }
                    else {
                        $(obj).html('已结束');             
                    }
                }, 1000);
            }
            $(this).addClass("ovd");
        }
        }); 
    }

//confirm
function corfirm(txt, fun, obj) {
    if ($(".noscroll").length == 0) {
        noscroll();
    }
    $("body").append('<div class="confirm"><div class="alert"><div class="content"><table width="100%" height="100%" cellpadding="0" cellspacing="0" border="0"><tr><td>' + txt + '</td></tr></table></div><p class="clearfix link"><a href="javascript:;" class="cancle">取消<i></i></a><a href="javascript:;" class="submit">确认</a></p></div><div class="con_bg opa0"></div></div>');
    setTimeout(function () {
        $(".confirm .alert").addClass("anidown");
        $(".confirm .con_bg").addClass("opa50").removeClass("opa0");
    }, 300);
    $(".confirm .submit").click(function () {
        if (fun(obj)) {
            $(".confirm .alert").addClass("anidis").removeClass("anidown");             
            setTimeout(function () {
                $(".confirm .con_bg").addClass("opa0").removeClass("opa50"); 
                $(".confirm").remove();
            }, 300);
            if ($(".noscroll").length > 0) {
                autoscroll();
            }
        }
    });
    $(".confirm .cancle,.confirm .con_bg").click(function () {
        
        $(".confirm .alert").addClass("anidis").removeClass("anidown");         
        setTimeout(function () {
            $(".confirm .con_bg").addClass("opa0").removeClass("opa50"); 
            $(".confirm").remove();
        }, 300);
        if ($(".noscroll").length > 0) {
            autoscroll();
        }
    });
}

//confirm3
function corfirm3(txt, rtxt, stxt, url) {
    if ($(".noscroll").length == 0) {
        noscroll();
    }    
    $("body").append('<div class="confirm"><div class="alert"><div class="content"><table width="100%" height="100%" cellpadding="0" cellspacing="0" border="0"><tr><td>' + txt + '</td></tr></table></div><p class="link clearfix"><a href="javascript:;" class="cancle">' + rtxt + '<i></i></a><a href="javascript:;" class="submit">' + stxt + '</a></p></div><div class="con_bg opa0"></div></div>');
    //$(".confirm").show();    
    setTimeout(function () {
        $(".confirm .alert").addClass("anidown");
        $(".confirm .con_bg").addClass("opa50").removeClass("opa0");
    }, 300);
    $(".confirm .submit").click(function () {
        window.location.href = url;
        if ($(".noscroll").length > 0) {
            autoscroll();
        }
    });
    $(".confirm .cancle,.confirm .con_bg").click(function () {
        $(".confirm .alert").addClass("anidis").removeClass("anidown");
        setTimeout(function () {
            $(".confirm .con_bg").addClass("opa0").removeClass("opa50");
            $(".confirm").remove();
        }, 300);
        if ($(".noscroll").length > 0) {
            autoscroll();
        }
    });
}

//获取验证码
function getcode(msgtype) { 
    $(".gcode").hide();
    $(".vcode").show();
    var crenttimes = parseInt($(".vcode i").eq(0).attr("sdata"));
    $(".vcode i").eq(0).text(crenttimes);
    if (typeof (msgtype) != "undefined")  //点击发送验证码
    {

        $.ajax({
            url: "/ashx/sendMsg.ashx?msgtype=" + escape(msgtype) + "&rad=" + Math.random(),
            type: "get",
            datatype: "json",
            async: false,
            success: function (data) {
                var dataJson = JSON.parse(data);
                if (dataJson.success == "Y") {



                } else {

                    error(dataJson.msg);
                    $(".gcode").show();
                    $(".vcode").hide();
                    clearInterval(ints);


                }
            }
        })

    }
    
    
        var ints = setInterval(function () {


            if (crenttimes <= 1) {

                $(".gcode").show();
                $(".vcode").hide();
                clearInterval(ints);

            }
            else {
                    crenttimes = crenttimes - 1;
                
            }
            $(".vcode i").eq(0).text(crenttimes);
        }, 1000);
    
}


function error(str) {
    if ($("#errormsg").length <= 0) {
        $("body").append('<div class="hidden" id="errormsg">' + str + '</div>');
    }
    else {
        $("#errormsg").html(str);
    }
    $("#errormsg").fadeIn("slow");
    setTimeout(function () {
        $("#errormsg").remove();
    }, 2000);
}

//倒计时
function countdown() {
    var crenttimes = $(".countdown").attr("count");
    var ints = setInterval(function () {
        
        if (crenttimes <= 1) {
            clearInterval(ints);
            window.location.href = $(".countdown").attr("url");
        }
        else {
            crenttimes = crenttimes - 1;
        }
        $(".countdown").text(crenttimes);
    }, 1000);
}

function clearallcokie() {    
    var colist = document.cookie.split(";");
    var cname = "";
    for (var i = 0; i < colist.length; i++) {
        if (colist[i] != "") {
            cname = colist[i].split("=")[0].replace(" ", "");
            console.log(cname);
            $.cookie(cname, null);
        }
    }
}

function nuls(obj) {
    $(".confirm2,.confirm").remove();
}

function getDiffDate(m) {
    m -= (D = parseInt(m / 86400000)) * 86400000;
    m -= (H = parseInt(m / 3600000)) * 3600000;
    S = parseInt((m -= (M = parseInt(m / 60000)) * 60000) / 1000);
    if (M < 10 & M > 0) { M = '0' + M; }
    if (H < 10 & H > 0) { H = H; }
    if (S < 10) { S = '0' + S; }
    if (D < 10) { D = '0' + D; }
    D != 0 ? D = '<i>' + D + '</i>天' : D = '<i>00</i>天';
    H != 0 ? H = '<i>' + H + '</i>时' : H = '<i>00</i>时';
    M != 0 ? M = '<i>' + M + '</i>分' : M = '<i>00</i>分';
    S != 0 ? S = '<i>' + S + '</i>秒' : S = '<i>00</i>秒';
    

    if (m > 0) return D + H + M + S;
    else { return "已经下架！"; }
}


//confirm
function corfirm(txt, fun, obj) {
    if ($(".noscroll").length == 0) {
        noscroll();
    }
    $("body").append('<div class="confirm"><div class="alert"><div class="content"><table width="100%" height="100%" cellpadding="0" cellspacing="0" border="0"><tr><td>' + txt + '</td></tr></table></div><p class="clearfix link"><a href="javascript:;" class="cancle">取消<i></i></a><a href="javascript:;" class="submit">确认</a></p></div><div class="con_bg opa0"></div></div>');
    setTimeout(function () {
        $(".confirm .alert").addClass("anidown");
        $(".confirm .con_bg").addClass("opa50").removeClass("opa0");
    }, 300);
    $(".confirm .submit").click(function () {
        if (fun(obj)) {
            $(".confirm .alert").addClass("anidis").removeClass("anidown");             
            setTimeout(function () {
                $(".confirm .con_bg").addClass("opa0").removeClass("opa50"); 
                $(".confirm").remove();
            }, 300);
            if ($(".noscroll").length > 0) {
                autoscroll();
            }
        }
    });
    $(".confirm .cancle,.confirm .con_bg").click(function () {
        
        $(".confirm .alert").addClass("anidis").removeClass("anidown");         
        setTimeout(function () {
            $(".confirm .con_bg").addClass("opa0").removeClass("opa50"); 
            $(".confirm").remove();
        }, 300);
        if ($(".noscroll").length > 0) {
            autoscroll();
        }
    });
}


function corfirm_order(txt, fun, id, tbname, usecoupon) {
    if ($(".noscroll").length == 0) {
        noscroll();
    }
    $("body").append('<div class="confirm"><div class="alert"><div class="content"><table width="100%" height="100%" cellpadding="0" cellspacing="0" border="0"><tr><td>' + txt + '</td></tr></table></div><p class="clearfix link"><a href="javascript:;" class="cancle">取消<i></i></a><a href="javascript:;" class="submit">确认</a></p></div><div class="con_bg opa0"></div></div>');
    setTimeout(function () {
        $(".confirm .alert").addClass("anidown");
        $(".confirm .con_bg").addClass("opa50").removeClass("opa0");
    }, 300);
    $(".confirm .submit").click(function () {
        if (fun( id, tbname, usecoupon)) {
            $(".confirm .alert").addClass("anidis").removeClass("anidown");
            setTimeout(function () {
                $(".confirm .con_bg").addClass("opa0").removeClass("opa50");
                $(".confirm").remove();
            }, 300);
            if ($(".noscroll").length > 0) {
                autoscroll();
            }
        }
    });
    $(".confirm .cancle,.confirm .con_bg").click(function () {

        $(".confirm .alert").addClass("anidis").removeClass("anidown");
        setTimeout(function () {
            $(".confirm .con_bg").addClass("opa0").removeClass("opa50");
            $(".confirm").remove();
        }, 300);
        if ($(".noscroll").length > 0) {
            autoscroll();
        }
    });
}


//confirm5
function corfirm5(txt, stxt, url) {
    if ($(".noscroll").length == 0) {
        noscroll();
    }
    $("body").append('<div class="confirm"><div class="alert"><div class="content"><table width="100%" height="100%" cellpadding="0" cellspacing="0" border="0"><tr><td>' + txt + '</td></tr></table></div><p class="link clearfix"><a href="javascript:;" class="submit all">' + stxt + '</a></p></div><div class="con_bg opa0"></div></div>');
    //$(".confirm").show();    
    setTimeout(function () {
        $(".confirm .alert").addClass("anidown");
        $(".confirm .con_bg").addClass("opa50").removeClass("opa0");
    }, 300);
    $(".confirm .submit").click(function () {
        window.location.href = url;
        if ($(".noscroll").length > 0) {
            autoscroll();
        }
    });
    $(".confirm .cancle,.confirm .con_bg").click(function () {
        $(".confirm .alert").addClass("anidis").removeClass("anidown");
        setTimeout(function () {
            $(".confirm .con_bg").addClass("opa0").removeClass("opa50");
            $(".confirm").remove();
        }, 300);
        if ($(".noscroll").length > 0) {
            autoscroll();
        }
    });
}

//confirm3
function corfirm3(txt, rtxt, stxt, url) {
    if ($(".noscroll").length == 0) {
        noscroll();
    }    
    $("body").append('<div class="confirm"><div class="alert"><div class="content"><table width="100%" height="100%" cellpadding="0" cellspacing="0" border="0"><tr><td>' + txt + '</td></tr></table></div><p class="link clearfix"><a href="javascript:;" class="cancle">' + rtxt + '<i></i></a><a href="javascript:;" class="submit">' + stxt + '</a></p></div><div class="con_bg opa0"></div></div>');
    //$(".confirm").show();    
    setTimeout(function () {
        $(".confirm .alert").addClass("anidown");
        $(".confirm .con_bg").addClass("opa50").removeClass("opa0");
    }, 300);
    $(".confirm .submit").click(function () {
        window.location.href = url;
        if ($(".noscroll").length > 0) {
            autoscroll();
        }
    });
    $(".confirm .cancle,.confirm .con_bg").click(function () {
        $(".confirm .alert").addClass("anidis").removeClass("anidown");
        setTimeout(function () {
            $(".confirm .con_bg").addClass("opa0").removeClass("opa50");
            $(".confirm").remove();
        }, 300);
        if ($(".noscroll").length > 0) {
            autoscroll();
        }
    });
}

//获取验证码
function getcode(msgtype) { 
    $(".gcode").hide();
    $(".vcode").show();
    var crenttimes = parseInt($(".vcode i").eq(0).attr("sdata"));
    $(".vcode i").eq(0).text(crenttimes);
    if (typeof (msgtype) != "undefined")  //点击发送验证码
    {

        $.ajax({
            url: "/ashx/sendMsg.ashx?msgtype=" + escape(msgtype) + "&rad=" + Math.random(),
            type: "get",
            datatype: "json",
            async: false,
            success: function (data) {
                var dataJson = JSON.parse(data);
                if (dataJson.success == "Y") {



                } else {

                    error(dataJson.msg);
                    $(".gcode").show();
                    $(".vcode").hide();
                    clearInterval(ints);


                }
            }
        })

    }
    
    
    var ints = setInterval(function () {


        if (crenttimes <= 1) {

            $(".gcode").show();
            $(".vcode").hide();
            clearInterval(ints);

        }
        else {
            crenttimes = crenttimes - 1;
                
        }
        $(".vcode i").eq(0).text(crenttimes);
    }, 1000);
    
}

//JS版  
//将传入数据转换为字符串,并清除字符串中非数字与.的字符  
//按数字格式补全字符串  
var getFloatStr = function (num) {
    num += '';
    num = num.replace(/[^0-9|\.]/g, ''); //清除字符串中的非数字非.字符  

    if (/^0+/) //清除字符串开头的0  
        num = num.replace(/^0+/, '');
    if (!/\./.test(num)) //为整数字符串在末尾添加.00  
        num += '.00';
    if (/^\./.test(num)) //字符以.开头时,在开头添加0  
        num = '0' + num;
    num += '00';        //在字符串末尾补零  
    num = num.match(/\d+\.\d{2}/)[0];
    return num
};
///剪切时间  var time='2018-04-03 10:15:16';    var timse = '2018-04-03 10:15';
function formatDatebox(value) {
    var times = new String(value);
    if (times.length == 19) {
        times = times.substring(0, times.lastIndexOf(":"));
    }
    return times;
}

function error(str) {
    if ($("#errormsg").length <= 0) {
        $("body").append('<div class="hidden" id="errormsg">' + str + '</div>');
    }
    else {
        $("#errormsg").html(str);
    }
    $("#errormsg").fadeIn("slow");
    setTimeout(function () {
        $("#errormsg").remove();
    }, 2000);
}

//倒计时
function countdown() {
    var crenttimes = $(".countdown").attr("count");
    var ints = setInterval(function () {
        
        if (crenttimes <= 1) {
            clearInterval(ints);
            window.location.href = $(".countdown").attr("url");
        }
        else {
            crenttimes = crenttimes - 1;
        }
        $(".countdown").text(crenttimes);
    }, 1000);
}

function clearallcokie() {    
    var colist = document.cookie.split(";");
    var cname = "";
    for (var i = 0; i < colist.length; i++) {
        if (colist[i] != "") {
            cname = colist[i].split("=")[0].replace(" ", "");
            console.log(cname);
            $.cookie(cname, null);
        }
    }
}


//cookies
jQuery.cookie = function (name, value, options) {
    if (typeof value != 'undefined') {
        //options = options || {};
        options = options || { expires: 7, path: "/" };
        if (value === null) {
            value = '';
            options = $.extend({}, options);
            options.expires = -1;
        }
        var expires = '';
        if (options.expires && (typeof options.expires == 'number' || options.expires.toUTCString)) {
            var date;
            if (typeof options.expires == 'number') {
                date = new Date();
                date.setTime(date.getTime() + (options.expires * 24 * 60 * 60 * 1000));
            } else {
                date = options.expires;
            }
            expires = '; expires=' + date.toUTCString();
        }
        var path = options.path ? '; path=' + (options.path) : '';
        var domain = options.domain ? '; domain=' + (options.domain) : '';
        var secure = options.secure ? '; secure' : '';
        document.cookie = [name, '=', encodeURIComponent(value), expires, path, domain, secure].join('');
    } else {
        var cookieValue = null;
        if (document.cookie && document.cookie != '') {
            var cookies = document.cookie.split(';');
            for (var i = 0; i < cookies.length; i++) {
                var cookie = jQuery.trim(cookies[i]);
                if (cookie.substring(0, name.length + 1) == (name + '=')) {
                    cookieValue = decodeURIComponent(cookie.substring(name.length + 1));
                    break;
                }
            }
        }
        return cookieValue;
    }
};

function mobT() {
    var sUserAgent = navigator.userAgent.toLowerCase();
    var tourl = "";
    var url = document.location.href;
    var pcurl = "http://www.zuozuojie.com";
    var moblieurl = "http://m.zuozuojie.com";
    if (sUserAgent.match(/(ipod|iphone os|midp|ucweb|android|windows ce|windows mobile)/i)) {
        //pc端转手机端
        //if(url.indexOf(moblieurl)==-1)
        //    window.location.href = moblieurl;
    }
    else {
        //手机端转pc端
        if (url.indexOf(pcurl) == -1)
            window.location.href = pcurl;
    }
}


Date.prototype.format = function (fmt) {
    var o = {
        "M+": this.getMonth() + 1,                 //月份 
        "d+": this.getDate(),                    //日 
        "h+": this.getHours(),                   //小时 
        "m+": this.getMinutes(),                 //分 
        "s+": this.getSeconds(),                 //秒 
        "q+": Math.floor((this.getMonth() + 3) / 3), //季度 
        "S": this.getMilliseconds()             //毫秒 
    };
    if (/(y+)/.test(fmt)) {
        fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
    }
    for (var k in o) {
        if (new RegExp("(" + k + ")").test(fmt)) {
            fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
        }
    }
    return fmt;
}