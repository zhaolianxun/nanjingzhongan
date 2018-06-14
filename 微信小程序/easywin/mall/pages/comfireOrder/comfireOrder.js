// share.js
Page({
  /**
   * 页面的初始数据
   */
  data: {
    // address:[],
    goods:[],
    token: '',
    address: [],
    goodsAll: [],
    addressIdSel: '',
    bindKeyInput: '',
    totalPrice: '',
    subMoney:'',
    mall_id: '',
    usableCoupons:[],
    from_cart:'',
    id:'',
    userId:'',
    },
  bindKeyInput: function (e) {
    // var bindKeyInput=e.details.value
    this.setData({
      bindKeyInput: e.detail.value
    })
  },
  /**
   * 生命周期函数--监听页面加载
   */
  onShow: function (options) {
    console.log(111)
    var that = this
    var userId = wx.getStorageSync('userId')
    that.setData({
      userId: userId,
    })
    var from_cart =wx.getStorageSync('from_cart')
   
    console.log(from_cart)
    var id = wx.getStorageSync('coupon_ids')
    that.setData({
      from_cart: from_cart,
      id: id,
    })
    console.log(id)
    if (!id){
      id = ''
    }
   
    var goods = wx.getStorageSync('goods')
    var token = wx.getStorageSync('token')
    var mall_id = wx.getStorageSync('mall_id')
    that.setData({
      mall_id: mall_id,
    })
    that.setData({
      token: token,
      goods: goods
    })
    wx.request({
      url: 'https://passion.njshangka.com/easywin/m/e/buy/orderconfirmpage',
      method: "POST",
      data: {
        "mall_id": that.data.mall_id,
        // address_id: 14,
        goods: JSON.stringify(goods),
        token: token,
        from_cart:from_cart,
        coupon_id:id,
      },
      header: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      success: function (res) {
        if (res.data.code == 0) {
          wx.removeStorageSync('coupon_ids')
          var addressIdSel = wx.getStorageSync('addressIdSel')
          var receiverNameSel = wx.getStorageSync('receiverNameSel')
          var phoneSel = wx.getStorageSync('phoneSel')
          var cityNameSel = wx.getStorageSync('cityNameSel')
          var districtNameSel = wx.getStorageSync('districtNameSel')
          var provinceNameSel = wx.getStorageSync('provinceNameSel')
          var fullAddressSel = wx.getStorageSync('fullAddressSel')
          var provinceNameSel = wx.getStorageSync('provinceNameSel')
          if (receiverNameSel != '') {
            that.data.address = { addressId: addressIdSel, cityName: cityNameSel, districtName: districtNameSel, fullAddress: fullAddressSel, phone: phoneSel, provinceName: provinceNameSel, receiverName: receiverNameSel }
            // that.data.address.receiverName = receiverNameSel
            // that.data.address.phone = phoneSel
            // that.data.address.fullAddress = fullAddressSel
            // that.data.address.provinceName = provinceNameSel
            // that.data.address.cityName = cityNameSel
            // that.data.address.districtName = districtNameSel
            // that.data.address.addressId = addressIdSel
            var address = that.data.address
            console.log(address)

            that.setData({
              goodsAll: res.data.data.goods,
              addressIdSel: addressIdSel,
              address: address,
              subMoney: res.data.data.subMoney,
              totalPrice: res.data.data.totalPrice,
              usableCoupons: res.data.data.usableCoupons,
            })
            console.log(address)
          } else if (res.data.data.address) {
            // console.log(res.data.data.address)
            that.setData({
              subMoney: res.data.data.subMoney,
              usableCoupons: res.data.data.usableCoupons,
              goodsAll: res.data.data.goods,
              totalPrice: res.data.data.totalPrice,
              addressIdSel: res.data.data.address.addressId,
              address: res.data.data.address,
            })
            // console.log(that.data.address)
          } else {
            that.setData({
              goodsAll: res.data.data.goods,
              totalPrice: res.data.data.totalPrice,
              subMoney: res.data.data.subMoney,
            })
            wx.removeStorageSync('coupon_ids')
          }

        }else{
          wx.showModal({
            title: res.data.codeMsg,
            content: '',
          })
        }
      },
    })

    // console.log(addressIdSel + "==" + receiverNameSel + "==" + cityNameSel + "==" + districtNameSel + "==" + fullAddressSel + "==" + provinceNameSel+"==" + phoneSel)

    // console.log(goods)




    // console.log(that.data.address.addressId)

  },
  // 去选择优惠券
  goCoupon:function(){
    var that=this
    console.log(that.data.usableCoupons)
    // console.log(JSON.parse(that.data.usableCoupons))
    wx.setStorageSync('usableCoupons', that.data.usableCoupons)
      wx.navigateTo({
        url: '../couponSelect/couponSelect',
      })
  },
  //支付
  goPay: function () {
    var that = this
    wx.request({
      url: 'https://passion.njshangka.com/easywin/m/e/buy/order',
      method: "POST",
      data: {
        "mall_id": that.data.mall_id,
        address_id: that.data.addressIdSel,
        goods: JSON.stringify(that.data.goods),
        buyer_note: that.data.bindKeyInput,
        token: that.data.token,
        from_cart: that.data.from_cart,
        coupon_id: that.data.id,
      },
      header: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      success: function (res) {
        if (res.data.code == 0) {
          var orderId = res.data.data.orderId;
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
                // var appId = res.data.data.appId;
                var timeStamp = res.data.data.timeStamp;
                var nonceStr = res.data.data.nonceStr;
                var packages = res.data.data.package;
                var paySign = res.data.data.sign;
                var signType = res.data.data.signType
                console.log(timeStamp)
                wx.requestPayment({
                  // 'appId': appId,
                  'timeStamp': timeStamp,
                  'nonceStr': nonceStr,
                  'package': packages,
                  'signType': signType,
                  'paySign': paySign,
                  'success': function (res) {
                    console.log('123')
                    wx.navigateTo({
                      url: '../order/order',
                    })
                  },
                  'fail': function (res) {
                    wx.showModal({
                      title: res.data.codeMsg,
                      // icon:'loading'
                    })
                  }
                })
                // })
              }else{
                wx.showModal({
                  title: res.data.codeMsg,
                  // icon:'loading'
                })
              }
            },
          })
        }else{
          wx.showModal({
            title: res.data.codeMsg,
            // icon:'loading'
          })
        }
      },
    })
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
})