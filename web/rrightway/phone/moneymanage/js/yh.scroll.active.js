(function (window, document) {
    var d = {
        container: '#container',
        url: '',
        params: {},
        page: 1,
        count: 20,
        cookieName: '',
        loadoverText: '加载完成',
        loadingText: '加载中……',
        waitTime: 200     //加载时的等待时间
    };

    var c = {};
    var isf = false;
    var isl = false;

    window.ys = function (d) {
        d = d || {};
        return c.config(d), c.beforeLoad(), c.loadHis(), ys;
    }
    window.onscroll = function () {
        var currentHeight = document.body.scrollHeight - document.body.scrollTop-$(".loadmore").height();
        var windowHeight = window.innerHeight;
        if (currentHeight <= windowHeight && !isl && !isf) {
            $.cookie(c.options.cookieName + 'cp', document.body.scrollTop);
            isl = true;
            setTimeout(function () {
                c.loadMore();
            }, c.options.waitTime);
        }
    }
    window.onunload = function () {
    }
    ys.v = '1.0.0';
    ys.reload = function (d) {
        d = d || {};
        $(c.options.container).empty();
        $('.loadover').remove();
        return c.config(d), c.options.page = 1,c.options.total = 0, c.beforeLoad(), c.onbeforeReload(), c.loadMore(), ys;
    }
    ys.savehis = function () {
        $.cookie(c.options.cookieName + 'page', c.options.page - 1);
        $.cookie(c.options.cookieName + 'position', document.body.scrollTop);
        $.cookie(c.options.cookieName + 'params', JSON.stringify(c.options.params));
        console.log('保存的位置：' + document.body.scrollTop);
    }
    ys.total = function () {
        return c.options.total;
    }
    ys.lastPage = function () {
        return c.options.page;
    }
    ys.lastPage = function () {
        return c.options.page;
    }
    ys.getReqParams = function () {
        return c.options.params;
    }
    c.config = function (a) {
        c.options = c.options || d,
        c.options.total = 0,
        c.options.container = a.container || c.options.container,
        c.options.url = a.url || c.options.url,
        c.options.params = a.params || c.options.params,
        c.options.count = a.count || c.options.count,
        c.options.waitTime = a.waitTime || c.options.waitTime,
        c.options.cookieName = a.cookieName || c.options.cookieName || c.getPageName(),
        c.options.loadoverText = a.loadoverText || c.options.loadoverText,
        c.options.loadingText = a.loadingText || c.options.loadingText,
        c.onloadItem = a.onloadItem || c.onloadItem || function (row) { return ""; }
        c.onloadSuccess = a.onloadSuccess || c.onloadSuccess || function () { },
        c.onloadError = a.onloadError || c.onloadError || function (msg) { },
        c.onNodata = a.onNodata || c.onNodata || function () { },
        c.onbeforeLoad = a.onbeforeLoad || c.onbeforeLoad || function () { },
        c.onbeforeReload = a.onbeforeReload || c.onbeforeReload || function () { };
    }
    c.loadMore = function () {
        c.options.params.page = c.options.page;
        c.options.params.count = c.options.count;
        $.post(c.options.url, c.options.params, function (text) {
            var res = eval('(' + text + ')');
            if (res.success == 'Y') {
                c.options.total += res.content.length;
                if (res.content.length > 0) {
                    for (var i = 0; i < res.content.length; i++) {
                        res.content.page = c.options.page;
                        $(c.options.container).append(c.onloadItem(res.content[i]));
                    }
                    c.onloadSuccess();
                    if (res.content.length < c.options.count) {
                        c.loadOver();
                    }
                } else if (c.options.page == 1 && c.options.total == 0) {
                    c.onNodata();
                    $(".loadmore").remove();
                    $(".loadover").remove();
                } else {
                    //c.onloadSuccess();
                    c.loadOver();
                }
                c.options.page += 1;
            } else {
                c.loadOver();
                c.onloadError(res.msg);
                $(".loadover").remove();
            }

            isl = false;
        });
    }
    c.loadOver = function () {
        isf = true;
        $(".loadmore").remove();
        if ($(".loadover").length == 0 && c.options.total > 0) {
            var txt = '<div class="loadover"><span>' + c.options.loadoverText + '</span></div>';
            $(c.options.container).after(txt);
        }
    }
    c.beforeLoad = function () {
        isf = false;
        if ($(".loadmore").length == 0) {
            var txt = '<div class="loadmore"><span class="loading"></span>' + c.options.loadingText + '</div>';
            $(c.options.container).after(txt);
        }
        c.onbeforeLoad();
    }
    c.loadHis = function () {
        var cpage = $.cookie(c.options.cookieName + 'page') == null ? 1 : $.cookie(c.options.cookieName + 'page');
        var position = $.cookie(c.options.cookieName + 'position') == null ? 0 : $.cookie(c.options.cookieName + 'position');
        c.options.params = $.cookie(c.options.cookieName + 'params') == null ? c.options.params : JSON.parse($.cookie(c.options.cookieName + 'params'));
        console.log('page:' + cpage + ' position:' + position+' params:'+c.options.params.key);

        c.loadMore();
        if (c.options.page < cpage) {
            setTimeout(function () {
                c.loadHis();
            }, c.options.waitTime);
        } else {
            setTimeout(function () { 
                window.scrollTo(0, position);
                console.log('滚动的位置：' + document.body.scrollTop);
                $.cookie(c.options.cookieName + 'page', null);
                $.cookie(c.options.cookieName + 'position', null);
                $.cookie(c.options.cookieName + 'params', null);
            }, 200);
        }

        
    }
    c.getPageName = function () {
        var a = location.href;
        var b = a.split("/");
        var c = b.slice(b.length - 1, b.length).toString(String).split(".");
        return c.slice(0, 1);
    }
})(window, document);


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