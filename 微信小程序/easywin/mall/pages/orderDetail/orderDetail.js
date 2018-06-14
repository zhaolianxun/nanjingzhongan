// share.js
Page({

  /**
   * 页面的初始数据
   */
  data: {
    order_id: "",
    goodList: [],
    amount: '',
    orderId: '',
    orderTime: '',
    receiverAddress: '',
    receiverName: '',
    receiverPhone: '',
    status: '',
    mall_id: '',
    token:'',
    userId:'',
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {
    var that = this
    var userId = wx.getStorageSync('userId')
    that.setData({
      userId: userId,
    })
    var mall_id = wx.getStorageSync('mall_id')
    var token = wx.getStorageSync('token')
    that.setData({
      mall_id: mall_id,
      token: token,
    })
   
    var order_id = options.id
    wx.request({
      url: 'https://passion.njshangka.com/easywin/m/e/allorder/orderinfopage',
      method: "POST",
      data: {
        "mall_id": that.data.mall_id,
        order_id: order_id,
        token: token
      },
      header: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      success: function (res) {
        if (res.data.code == 0) {


          that.setData({
            goodList: res.data.data.details,
            amount: res.data.data.amount,
            orderId: res.data.data.orderId,
            orderTime: that.dateChange(res.data.data.orderTime),
            receiverAddress: res.data.data.receiverAddress,
            receiverName: res.data.data.receiverName,
            receiverPhone: res.data.data.receiverPhone,
            status: res.data.data.status,
          });


          // console.log(that.data.imgUrls)

        } else if (res.data.code == 20 || res.data.code == 26) {
          wx.hideToast()
          // wx.navigateTo({
          //   url: '../login/login',
          // })
        }
      },
    })
  },
  // 取消订单
  cancelorder:function(e){
    var that = this
    var orderId = e.currentTarget.dataset.id

    console.log(orderId)
    wx.request({
      url: 'https://passion.njshangka.com/easywin/m/e/allorder/cancelorder',
      method: "POST",
      data: {
        "mall_id": that.data.mall_id,
        order_id: orderId,
        token: that.data.token
      },
      header: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      success: function (res) {
        if (res.data.code == 0) {
          wx.showToast({
            title: '签收成功',
            // icon:'loading'
          })
        } else {
          wx.showModal({
            title: res.data.codeMsg,
            // icon:'loading'
          })
        }
      },
    })
  },
  // 删除订单
  delOrder: function (e) {
    var that = this
    var orderId = e.currentTarget.dataset.id

    console.log(orderId)
    wx.request({
      url: 'https://passion.njshangka.com/easywin/m/e/allorder/delorder',
      method: "POST",
      data: {
        "mall_id": that.data.mall_id,
        order_id: orderId,
        token: that.data.token
      },
      header: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      success: function (res) {
        if (res.data.code == 0) {
          wx.showToast({
            title: '签收成功',
            // icon:'loading'
          })
        } else {
          wx.showModal({
            title: res.data.codeMsg,
            // icon:'loading'
          })
        }
      },
    })
  },
  // 确认收货
  confirm: function (e) {
    var that = this
    var orderId = e.currentTarget.dataset.id

    console.log(orderId)
    wx.request({
      url: 'https://passion.njshangka.com/easywin/m/e/allorder/sign',
      method: "POST",
      data: {
        "mall_id": that.data.mall_id,
        order_id: orderId,
        token: that.data.token
      },
      header: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      success: function (res) {
        if (res.data.code == 0) {
          wx.showToast({
            title: '签收成功',
            // icon:'loading'
          })
        } else {
          wx.showModal({
            title: res.data.codeMsg,
            // icon:'loading'
          })
        }
      },
    })
  },

  // 立即付款
  callWxPay: function (e) {
    var that = this
    var orderId = e.currentTarget.dataset.id
    wx.request({
      url: 'https://passion.njshangka.com/easywin/m/e/buy/pay/wxminiapp',
      method: "POST",
      data: {
        "mall_id": that.data.mall_id,
        order_id: orderId,
        token: that.data.token
      },
      header: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      success: function (res) {
        if (res.data.code == 0) {
          var timeStamp = res.data.data.timeStamp;
          var nonceStr = res.data.data.nonceStr;
          var packages = res.data.data.pack;
          var paySign = res.data.data.paySign;
          wx.requestPayment({
            'timeStamp': timeStamp,
            'nonceStr': nonceStr,
            'package': packages,
            'signType': 'MD5',
            'paySign': paySign,
            'success': function (res) {
              console.log('123')
              wx.navigateTo({
                url: '../order/order',
              })
            },
            'fail': function (res) {
              console.log('456')
            }
          })
          // })
        }
      },
    })
  },
  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady: function () {

  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow: function () {

  },

  /**
   * 生命周期函数--监听页面隐藏
   */
  onHide: function () {

  },

  /**
   * 生命周期函数--监听页面卸载
   */
  onUnload: function () {

  },

  /**
   * 页面相关事件处理函数--监听用户下拉动作
   */
  onPullDownRefresh: function () {

  },

  /**
   * 页面上拉触底事件的处理函数
   */
  onReachBottom: function () {

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
  dateChange: function (data) {
    var date = new Date(data)
    var Y = date.getFullYear() + '/';
    var M = (date.getMonth() + 1 < 10 ? '0' + (date.getMonth() + 1) : date.getMonth() + 1) + '/';
    var D = date.getDate() < 10 ? '0' + date.getDate() : date.getDate();
    return (Y + M + D)
  },
})