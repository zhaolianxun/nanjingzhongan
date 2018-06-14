// share.js
Page({

  /**
   * 页面的初始数据
   */

  data: {
    list: [],
    left: '',
    startX: 0, //开始坐标
    startY: 0,
    totalPrice: 0,
    colors: '',
    goods: [],
    mall_id:'',
    userId:'',
    // color:''
  },
  // 添加购物车
  addOrder: function () {
    var that = this
    // console.log(that.data.list[0].amount)
    wx.request({
      url: 'https://passion.njshangka.com/easywin/m/e/gooddetail',
      method: "POST",
      data: {
        "mall_id": that.data.mall_id,
        good_id: 552446375867,
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
      url: 'https://passion.njshangka.com/easywin/m/e/gooddetail/good2cart',
      method: "POST",
      data: {
        "mall_id": that.data.mall_id,
        address_id: 14,
        good_id: 552446375867,
        name: '1',
        count: 11,
        price: 1,
        detail_pic: "https://img.alicdn.com/imgextra/i2/1058052659/TB2vFuii4HI8KJjy1zbXXaxdpXa_!!1058052659-0-beehive-scenes.jpg_300x300.jpg",
        sku_id: 1,
        attr_names: "规格",
        value_names: '红XXL',
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
  },
  // 选择商品
  selectTap: function (e) {
    var that = this
    var goodId = e.currentTarget.dataset.id
    var count = e.currentTarget.dataset.count
    var price = e.currentTarget.dataset.price
    var valueNames = e.currentTarget.dataset.valuenames
    var attrNames = e.currentTarget.dataset.attrnames
    var skuId = e.currentTarget.dataset.skuid
    var totalPrice = that.data.totalPrice

    console.log(goodId)

    for (var i = 0; i < that.data.list.length; i++) {
      var c = "percent_list[" + i + "].color"
      if (that.data.list[i].skuId == skuId && that.data.list[i].color != 'red') {
        that.data.list[i].color = 'red'
        var goods = that.data.goods
        var goodsAttr = [{ id: goodId, cnt: count, skuId: skuId, attrNames: attrNames, valueNames: valueNames }]
        console.log(goodsAttr)
        that.setData({
          goods: goods.concat(goodsAttr),
        })
        console.log(that.data.goods)
        totalPrice = totalPrice + count * price
      } else if (that.data.list[i].skuId == skuId && that.data.list[i].color == 'red') {
        that.data.list[i].color = 'rgb(153,153,153)'
        totalPrice = totalPrice - count * price

        for(var r=0;r<that.data.goods.length;r++){
          if (that.data.goods[r].id == goodId){
            var goods = that.data.goods
            goods.splice(r, 1)
          }
        }

        
        that.setData({
          colors: '',
          goods: goods
        })

        console.log(that.data.goods)
      }

    }
    var list = that.data.list
    console.log(that.data.list)
    that.setData({
      list: list,
      totalPrice: totalPrice,
    })
  },
  // 全选商品
  selectAll: function (e) {
    var that = this
    var count, price, goodId, valueNames, attrNames, skuId,goodEve
    // var goodId = e.currentTarget.dataset.id

    var totalPrice = 0
    var colors = that.data.colors
    var goodsAll=[]
    // console.log(goodId)
    if (colors != 'red') {

      for (var i = 0; i < that.data.list.length; i++) {
        count = that.data.list[i].count
        price = that.data.list[i].price
        that.data.list[i].color = 'red'
        totalPrice = totalPrice + count * price
        goodId = that.data.list[i].goodId
        valueNames = that.data.list[i].valueNames
        attrNames = that.data.list[i].attrNames
        skuId = that.data.list[i].skuId
        goodEve = [{ id: goodId, cnt: count, skuId: skuId, attrNames: attrNames, valueNames: valueNames }]
        goodsAll = goodsAll.concat(goodEve)
      }
      var list = that.data.list
      that.setData({
        colors: 'red',
        list: list,
        totalPrice: totalPrice,
        goods: goodsAll,
      })
      console.log(that.data.goods)
    } else {
      for (var i = 0; i < that.data.list.length; i++) {
        that.data.list[i].color = ''
      }
      var list = that.data.list
      that.setData({
        colors: '',
        list: list,
        totalPrice: 0,
        goods:[],
      })
    }


  },
  // 减少商品数量
  jianBtnTap: function (e) {
    var that = this
    var goodId = e.currentTarget.dataset.id
    var count = e.currentTarget.dataset.count
    var price = e.currentTarget.dataset.price
    var totalPrice = that.data.totalPrice
    var skuid = e.currentTarget.dataset.skuid

    wx.request({
      url: 'https://passion.njshangka.com/easywin/m/e/cart/subcount',
      method: "POST",
      data: {
        "mall_id": that.data.mall_id,
        good_id: goodId,
        count: 1,
        token: that.data.token
      },
      header: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      success: function (res) {
        if (res.data.code == 0) {
          for (var i = 0; i < that.data.list.length; i++) {
            if (that.data.list[i].skuId == skuid && that.data.list[i].count > 1 && that.data.list[i].color == 'red') {
              that.data.list[i].count--
              totalPrice = totalPrice - price * 1
              console.log(price, i, totalPrice)
            }
            else if (that.data.list[i].skuId == skuid && that.data.list[i].count > 1 && that.data.list[i].color != 'red') {
              that.data.list[i].count--
            }

          }
          console.log(totalPrice)
          var list = that.data.list
          console.log(that.data.list)
          that.setData({
            list: list,
            totalPrice: totalPrice,
          })

        }
      },
    })


  },
  // 增加商品数量
  jiaBtnTap: function (e) {
    var that = this
    var goodId = e.currentTarget.dataset.id
    var count = e.currentTarget.dataset.count
    var price = e.currentTarget.dataset.price
    var skuInventory = e.currentTarget.dataset.skuinventory
    var totalPrice = that.data.totalPrice
    var skuid = e.currentTarget.dataset.skuid
    wx.request({
      url: 'https://passion.njshangka.com/easywin/m/e/cart/addcount',
      method: "POST",
      data: {
        "mall_id": that.data.mall_id,
        good_id: goodId,
        count: 1,
        token: that.data.token
      },
      header: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      success: function (res) {
        if (res.data.code == 0) {

          for (var i = 0; i < that.data.list.length; i++) {
            if (that.data.list[i].skuId == skuid && that.data.list[i].count < skuInventory && that.data.list[i].color == 'red') {
              that.data.list[i].count++
              totalPrice = totalPrice + price * 1
              console.log(price, i, totalPrice)
            }
            else if (that.data.list[i].skuId == skuid && that.data.list[i].count < skuInventory && that.data.list[i].color != 'red') {
              console.log(123)
              that.data.list[i].count++
            }

          }
          console.log(totalPrice)
          var list = that.data.list
          console.log(that.data.list)
          that.setData({
            list: list,
            totalPrice: totalPrice,
          })
        }
      },
    })


  },
  // 分页
  lastPage: function (toPageNo) {

    var that = this;
    var pageSize = 15


    var toPageNo = parseInt(toPageNo) + 1
    wx.request({
      url: 'https://passion.njshangka.com/easywin/m/e/cart',
      method: "POST",
      data: {
        "mall_id": that.data.mall_id,
        // status: status,
        page_no: toPageNo,
        page_size: pageSize,
        token: that.data.token
      },
      header: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      success: function (res) {
        if (res.data.code == 0) {
          var ordersListArr = that.data.list

          var newSchemeListArr = ordersListArr.concat(res.data.data)
          if (res.data.data.length == 0) {
            that.setData({
              list: ordersListArr,
              toPageNo: String(toPageNo)
            });
            console.log(that.data.list)
            wx.showToast({
              title: '数据已全部加载',
              // icon: 'loading',
            })
            console.log(res.data.data.length)
          } else {
            console.log(res.data.data.length)
            that.setData({
              list: newSchemeListArr,
              toPageNo: String(toPageNo)
            });
            console.log(that.data.list)
          }




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
  /**
   * 生命周期函数--监听页面加载
   */
  // onLoad: function (options) {
    // var that = this
    // var token = wx.getStorageSync('token')
    // var mall_id = wx.getStorageSync('mall_id')
    // that.setData({
    //   list: [],
    //   token: token,
    //   mall_id: mall_id,
    // })
    // that.lastPage(0)

  // },
  // 删除
  del: function (e) {
    var that = this
    var goodId = e.currentTarget.dataset.id
    var skuId = e.currentTarget.dataset.skuid
    console.log(goodId)

    wx.request({
      url: 'https://passion.njshangka.com/easywin/m/e/cart/remove',
      method: "POST",
      data: {
        "mall_id": that.data.mall_id,
        good_id: goodId,
        token: that.data.token
      },
      header: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      success: function (res) {
        if (res.data.code == 0) {
          for (var i = 0; i < that.data.list.length; i++) {
            if (that.data.list[i].goodId == goodId) {
              var list = that.data.list
              console.log(i)
              list.splice(i, 1)
              that.setData({
                list: list
              })
            }
          }
        }
      },
    })


    console.log(that.data.list)

  },
  // 去结算
  toPayOrder:function(){
    var that = this
    wx.setStorageSync('goods', that.data.goods)
    wx.setStorageSync('from_cart', 1)
    wx.navigateTo({
      url: '../comfireOrder/comfireOrder',
    })
  },
  // 去看看
  goIndex:function(){
    wx.switchTab({
      url: '../index/index',
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
  onShow: function (options) {
   
    var that = this
    var userId = wx.getStorageSync('userId')
    that.setData({
      userId: userId,
    })
    var token = wx.getStorageSync('token')
    var mall_id = wx.getStorageSync('mall_id')
    that.setData({
      list:[],
      token: token,
      mall_id: mall_id,
    })
    that.lastPage(0)
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
  //手指触摸动作开始 记录起点X坐标
  touchstart: function (e) {
    //开始触摸时 重置所有删除
    this.data.list.forEach(function (v, i) {
      if (v.isTouchMove)//只操作为true的
        v.isTouchMove = false;
    })
    this.setData({
      startX: e.changedTouches[0].clientX,
      startY: e.changedTouches[0].clientY,
      list: this.data.list
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
    that.data.list.forEach(function (v, i) {
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
      list: that.data.list
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
})