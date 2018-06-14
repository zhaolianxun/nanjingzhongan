// share.js
Page({
  /**
   * 页面的初始数据
   */
  data: {
    tabList: ['全部订单', '待支付', '待发货', '待收货', '已完成'],
    token: '',
    toPageNo: '',
    status: '',
    color: 'color',
    color0: '',
    color1: '',
    color2: '',
    color3: '',
    orderList: [],
    goodList:[],
    mall_id: '',
    userId:'',
  },
  swichNav: function (e) {
    var that = this
    that.setData({
      orderList: [],
      color: 'color',
      color0: '',
      color1: '',
      color2: '',
      color3: '',
      status: ''
    })
    that.lastPage(0, '')
  },
  swichNav0: function (e) {
    var that = this
    that.setData({
      orderList: [],
      color: '',
      color0: 'color',
      color1: '',
      color2: '',
      color3: '',
      status: 0
    })
    that.lastPage(0, 0)
  },
  swichNav1: function (e) {
    var that = this
    that.setData({
      orderList: [],
      color: '',
      color0: '',
      color1: 'color',
      color2: '',
      color3: '',
      status: 1
    })
    that.lastPage(0, 1)
  },
  swichNav2: function (e) {
    var that = this
    that.setData({
      orderList: [],
      color: '',
      color0: '',
      color1: '',
      color2: 'color',
      color3: '',
      status: 2
    })
    that.lastPage(0, 2)
  },
  swichNav3: function (e) {
    var that = this
    that.setData({
      orderList: [],
      color: '',
      color0: '',
      color1: '',
      color2: '',
      color3: 'color',
      status: 3
    })
    that.lastPage(0, 3)
  },


  // // 下单
  // // /m/e/ buy / order
  // // mall_id
  // // address_id
  // // orders	JSONARRAY
  // // buyer_note 500
  // // -----------------
  // //   orderId  /m/e/gooddetail
  // // good_id
  addOrder: function () {
    var that = this
    // console.log(that.data.orderList[0].amount)
    wx.request({
      url: 'https://passion.njshangka.com/easywin/m/e/gooddetail',
      method: "POST",
      data: {
        "mall_id": that.data.mall_id,
        good_id: 1,
        token: that.data.token
      },
      header: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      success: function (res) {
        if (res.data.code == 0) {

        }
      },
    })

    wx.request({
      url: 'https://passion.njshangka.com/easywin/m/e/buy/order',
      method: "POST",
      data: {
        "mall_id": that.data.mall_id,
        address_id: 14,
        goods: JSON.stringify([{ id: "1", cnt: 4, skuId: 1, attrNames: "颜色,尺码", valueNames: "红,XXL" }, { id: "552446375867", cnt: 1, skuId: 5, attrNames: "颜色,尺码", valueNames: "红,XXL" }]),
        buyer_note: "备注",
        token: that.data.token
      },
      header: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      success: function (res) {
        if (res.data.code == 0) {
          wx.showToast({
            title: '取消订单成功',
            // icon:'loading'
          })
          that.onLoad()
        }
      },
    })
  },

  // 取消订单
  refOrder: function (e) {
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
            title: '取消订单成功',
            // icon:'loading'
          })
          that.onLoad()
        }else{
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
          that.onLoad()
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
            title: '删除成功',
            // icon:'loading'
          })
          that.onLoad()
        } else {
          wx.showModal({
            title: res.data.codeMsg,
            // icon:'loading'
          })
        }
      },
    })

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
    var token = wx.getStorageSync('token')
    var mall_id = wx.getStorageSync('mall_id')
    that.setData({
      mall_id: mall_id,
    })
    that.setData({
      token: token,
    })
    that.lastPage(0, '')


    console.log(that.data.orderList)
  },
  // 分页
  lastPage: function (toPageNo, status) {

    var that = this;
    var pageSize = 5


    var toPageNo = parseInt(toPageNo) + 1
    wx.request({
      url: 'https://passion.njshangka.com/easywin/m/e/allorder/orderspage',
      method: "POST",
      data: {
        "mall_id": that.data.mall_id,
        status: status,
        page_no: toPageNo,
        page_size: pageSize,
        token: that.data.token
      },

      header: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      success: function (res) {
        if (res.data.code == 0) {
          for (var i = 0; i < res.data.data.orders.length; i++) {
            var details = res.data.data.orders[i].details;
            // console.log(details)
            // that.setData({
            //   goodList: details
            // })
            console.log(that.data.goodList)
            for (var r = 0; r < details.length ;r++){
              console.log('r====' + details[r].cover)
              that.setData({
                goodList: details
              })
            }
          }
          var ordersListArr = that.data.orderList

          var newSchemeListArr = ordersListArr.concat(res.data.data.orders)
          if (res.data.data.orders.length == 0) {
            that.setData({
              orderList: ordersListArr,
              toPageNo: String(toPageNo)
            });
            console.log(that.data.orderList)
            wx.showToast({
              title: '数据已全部加载',
              // icon: 'loading',
            })
            console.log(res.data.data.orders.length)
          } else {
            console.log(res.data.data.orders.length)
            that.setData({
              orderList: newSchemeListArr,
              toPageNo: String(toPageNo)
            });
            console.log(that.data.orderList)
          }




          // console.log(that.data.imgUrls)

        } else if (res.data.code == 20 || res.data.code == 26) {
          wx.hideToast()
          // wx.navigateTo({
          //   url: '../login/login',
          // })
        }
        var orderTime
        for (var i = 0; i < that.data.orderList.length; i++) {
          orderTime = that.data.orderList[i].orderTime
          that.data.orderList[i].orderTime = that.dateChange(orderTime)
        }
        console.log(that.data.orderList)
        that.setData({
          orderList: that.data.orderList,
        })
      },
    })

  },

  // 立即付款
  payMoney:function(e){
    var that=this
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
                that.onLoad()
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
   * 页面相关事件处理函数--监听用户下拉动作
   */
  onPullDownRefresh: function () {
    console.log(123)
    this.setData({ flag: false })
    wx.stopPullDownRefresh()
  },

  /**
   * 页面上拉触底事件的处理函数
   */
  onReachBottom: function () {
    var that = this
    var toPageNo = that.data.toPageNo
    that.lastPage(toPageNo, that.data.status)
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