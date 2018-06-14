// share.js
Page({

  /**
   * 页面的初始数据
   */
  data: {
    from:'',
    token: '',
    addressesList: [],
    type: '1',
    left: '',
    startX: 0, //开始坐标
    startY: 0,
    receiverName: '',
    mobile: '',
    province_name: '',
    city_name: '',
    district_name: '',
    addressDetail: '',
    color: '0',
    addressId: '',
    // receiverName: '',
    phone: '',
    provinceName: '',
    cityName: '',
    districtName: '',
    fullAddress: '',
    remove: '0',
    // defaultIs: '',
    mall_id:'',
    userId:'',
  },
  // 選擇地址
  tapSelAddress: function (e) {
    var that=this
    var addressIdSel = e.currentTarget.dataset.id
    
    var receiverName = e.currentTarget.dataset.receivername
    var phone = e.currentTarget.dataset.phone
    var provinceName = e.currentTarget.dataset.provincename
    var cityName = e.currentTarget.dataset.cityname
    var districtName = e.currentTarget.dataset.districtname
    var fullAddress = e.currentTarget.dataset.fulladdress
    console.log(addressIdSel + "==" + receiverName + "==" + provinceName + "==" + districtName + "==" + fullAddress + "==" + cityName + "==" + phone)
    wx.setStorageSync('addressIdSel', addressIdSel)   
    wx.setStorageSync('receiverNameSel', receiverName)
    wx.setStorageSync('phoneSel', phone)
    wx.setStorageSync('cityNameSel', cityName)
    wx.setStorageSync('districtNameSel', districtName)
    wx.setStorageSync('provinceNameSel', provinceName)
    wx.setStorageSync('fullAddressSel', fullAddress)
    console.log(addressIdSel)
    if(that.data.from==1){
      wx.navigateTo({
        url: '../comfireOrder/comfireOrder',
      })
    }
  },
  // 添加地址
  addAddress: function () {
    var that = this
    var token = that.data.token
    that.setData({
      token: token,
      type: '2',
    })

  },
  receiverName: function (e) {
    this.setData({
      receiverName: e.detail.value
    })
  },
  mobile: function (e) {
    this.setData({
      mobile: e.detail.value
    })
  },
  province_name: function (e) {
    this.setData({
      province_name: e.detail.value
    })
  },
  city_name: function (e) {
    this.setData({
      city_name: e.detail.value
    })
  },
  district_name: function (e) {
    this.setData({
      district_name: e.detail.value
    })
  },
  addressDetail: function (e) {
    this.setData({
      addressDetail: e.detail.value
    })
  },
  addressAct: function (e) {
    console.log(e.currentTarget.dataset.color)
    if (e.currentTarget.dataset.color == 1) {
      this.setData({
        color: 0
      })
    } else {
      this.setData({
        color: 1
      })
    }
  },
  // 完成添加
  writeOver: function () {
    var that = this;
    if (that.data.remove == 0) {
      wx.request({
        url: 'https://passion.njshangka.com/easywin/m/e/addrmanage/addaddress',
        method: "POST",
        data: {
          "mall_id":  that.data.mall_id,

          receiver_name: that.data.receiverName,
          phone: that.data.mobile,
          full_address: that.data.addressDetail,
          default_is: that.data.color,
          province_name: that.data.province_name,
          city_name: that.data.city_name,
          district_name: that.data.district_name,
          token: that.data.token
        },
        header: {
          "Content-Type": "application/x-www-form-urlencoded",
        },
        success: function (res) {
          if (res.data.code == 0) {
            that.reload();
            that.setData({
              type: '1',
              receiver_name: '',
              phone: '',
              full_address: '',
              default_is: '',
              province_name: '',
              city_name: '',
              district_name: '',
            })
          }else{
            wx.showModal({
              title: res.data.codeMsg,
              content: '',
            })
          }
        },
      })
    } else {
      wx.request({
        url: 'https://passion.njshangka.com/easywin/m/e/addrmanage/alteraddress',
        method: "POST",
        data: {
          "mall_id":  that.data.mall_id,
          address_id: that.data.addressId,
          receiver_name: that.data.receiverName,
          phone: that.data.mobile,
          full_address: that.data.addressDetail,
          default_is: that.data.color,
          province_name: that.data.provinceName,
          city_name: that.data.cityName,
          district_name: that.data.districtName,
          token: that.data.token
        },
        header: {
          "Content-Type": "application/x-www-form-urlencoded",
        },
        success: function (res) {
          if (res.data.code == 0) {
            that.reload();
            that.setData({
              type: '1',

            })
          }
        },
      })
    }

  },

  // 修改地址
  editAddress: function (e) {
    var that = this
    that.setData({
      type: '2',
    })

    that.setData({

      remove: '1',
      addressId: e.currentTarget.dataset.addressid,
      receiverName: e.currentTarget.dataset.receivername,
      mobile: e.currentTarget.dataset.phone,
      provinceName: e.currentTarget.dataset.provincename,
      cityName: e.currentTarget.dataset.cityname,
      districtName: e.currentTarget.dataset.districtname,
      addressDetail: e.currentTarget.dataset.fulladdress,
      color: e.currentTarget.dataset.defaultis,
    })

  },
  //手指触摸动作开始 记录起点X坐标
  touchstart: function (e) {
    //开始触摸时 重置所有删除
    this.data.addressesList.forEach(function (v, i) {
      if (v.isTouchMove)//只操作为true的
        v.isTouchMove = false;
    })
    this.setData({
      startX: e.changedTouches[0].clientX,
      startY: e.changedTouches[0].clientY,
      addressesList: this.data.addressesList
    })
  },
  //滑动事件处理
  touchmove: function (e) {
    var that = this,
      index = e.currentTarget.dataset.index,//当前索引
      startX = that.data.startX,//开始X坐标
      startY = that.data.startY,//开始Y坐标
      touchMoveX = e.changedTouches[0].clientX,//滑动变化坐标
      touchMoveY = e.changedTouches[0].clientY,//滑动变化坐标
      //获取滑动角度
      angle = that.angle({ X: startX, Y: startY }, { X: touchMoveX, Y: touchMoveY });
    that.data.addressesList.forEach(function (v, i) {
      v.isTouchMove = false
      //滑动超过30度角 return
      if (Math.abs(angle) > 30) return;
      if (i == index) {
        if (touchMoveX > startX) //右滑
          v.isTouchMove = false
        else //左滑
          v.isTouchMove = true
      }
    })
    //更新数据
    that.setData({
      addressesList: that.data.addressesList
    })
  },
  /**
   * 计算滑动角度
   * @param {Object} start 起点坐标
   * @param {Object} end 终点坐标
   */
  angle: function (start, end) {
    var _X = end.X - start.X,
      _Y = end.Y - start.Y
    //返回角度 /Math.atan()返回数字的反正切值
    return 360 * Math.atan(_Y / _X) / (2 * Math.PI);
  },
  //删除事件
  del: function (e) {
    var that = this
    // that.data.addressesList.splice(e.currentTarget.dataset.index, 1)
    // that.setData({
    //   addressesList: that.data.addressesList
    // })
    var address_id = e.currentTarget.dataset.addressid
    console.log(address_id)
    wx.request({
      url: 'https://passion.njshangka.com/easywin/m/e/addrmanage/removeaddress',
      method: "POST",
      data: {
        "mall_id":  that.data.mall_id,
        address_id: address_id,
        token: that.data.token
      },
      header: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      success: function (res) {
        if (res.data.code == 0) {
          wx.request({
            url: 'https://passion.njshangka.com/easywin/m/e/addrmanage',
            method: "POST",
            data: {
              "mall_id":  that.data.mall_id,
              page_no: 1,
              page_size: 100,
              token: that.data.token
            },
            header: {
              "Content-Type": "application/x-www-form-urlencoded",
            },
            success: function (res) {
              if (res.data.code == 0) {
                that.setData({
                  addressesList: res.data.data.addresses,
                })
              }
            },
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
    var from = options.from
    var userId = wx.getStorageSync('userId')
    that.setData({
      userId: userId,
    })
    var mall_id = wx.getStorageSync('mall_id')
    that.setData({
      from: from,
      mall_id: mall_id,
    })
    console.log(from)
    that.reload();
  },


  reload: function () {
    var that = this
    var token = wx.getStorageSync("token")
    that.setData({
      token: token,
      type: '1',
    })
    wx.request({
      url: 'https://passion.njshangka.com/easywin/m/e/addrmanage',
      method: "POST",
      data: {
        "mall_id":  that.data.mall_id,
        page_no: 1,
        page_size: 100,
        token: token
      },
      header: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      success: function (res) {
        if (res.data.code == 0) {
          that.setData({
            addressesList: res.data.data.addresses,
          })
        }
      },
    })

  },
  // 返回
  back: function () {
    this.setData({
      type: '1',
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
})