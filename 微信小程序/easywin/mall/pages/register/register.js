// share.js
Page({

  /**
   * 页面的初始数据
   */
  data: {
    phone: '',
    code: '',
    sendMsgDisabled: false,
    flag: false,
    codeDis: false,
    time: '',
    mall_id: '',
    // time: "获取验证码",
    userId:'',
  },
  onLoad:function(options){
    var that = this
    var userId = wx.getStorageSync('userId')
    that.setData({
      userId: userId,
    })
    var mall_id = wx.getStorageSync('mall_id')
    that.setData({
      mall_id: mall_id,
    })
  },
  inputNum: function (e) {
    console.log(e.detail.value)
    this.setData({
      phone: e.detail.value
    })
  },
  codeInput: function (e) {
    this.setData({
      code: e.detail.value
    })
  },
  sendCode: function () {
    var that = this
    var phone = that.data.phone
    console.log(phone)
    if (phone.length != 11 || isNaN(phone)) {
      wx.showToast({
        title: '请输入有效的手机号码',
        icon: "loading"
      })
      setTimeout(function () {
        wx.hideToast()
      }, 2000)
      return
    };


    // 发送短信验证码
    wx.request({
      url: 'https://passion.njshangka.com/sms/verification/send',
      data: {
        // clien: 'yichaxun',
        phone: that.data.phone,
      },
      success: function (res) {
        var data = res.data

        if (data.code != 0) {
          that.setData({
            codeDis: false
          })
          wx.showToast({
            title: data.codeMsg,
            icon: "loading"
          })
          setTimeout(function () {
            wx.hideToast()
          }, 2000)
        } else {
          that.setData({
            time: 60,
            sendMsgDisabled: true,
          })
          var timer = setInterval(function () {
            var time = that.data.time
            time--;
            that.setData({
              time: time
            })
            if (time == 0) {
              clearInterval(time)
              that.setData({
                sendMsgDisabled: false,
              })
            }
          }, 1000);
        }
      }
    })
  },

  finish: function () {
    var that = this
    var token = wx.getStorageSync('token')
    wx.request({
      url: 'https://passion.njshangka.com/easywin/m/e/i/bindphone',
      data: {
        "mall_id": that.data.mall_id,
        phone: that.data.phone,
        verification_code: that.data.code,
        token: token
      },
      success: function (res) {
        if (res.data.code == 0) {
          wx.navigateBack({
            url: '../info/info',
          })
        } else {
          wx.showToast({
            title: data.codeMsg,
            icon: "loading"
          })
        }
      }
    })
  },

  /**
 * 用户点击右上角分享
 */
  onShareAppMessage: function () {
    var that = this
    return {
      title: '三分钟小程序',
      path: 'pages/indexPage/index?shareUserId=' + that.data.userId,
      success: function (res) {
        // 转发成功
      },
      fail: function (res) {
        // 转发失败
      }
    }
  },
})