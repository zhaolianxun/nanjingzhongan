// share.js
var WxParse = require('../../wxParse/wxParse.js');
var app = getApp();
Page({

  /**
   * 页面的初始数据
   */
  data: {
    winHeight: '750rpx',
    detailPics: [],
    dataDetails: [],
    attrs: [],
    skus: [],
    canOrder: '',
    hidden: false,
    orderNum: '1',
    color: '#fff',
    background: 'red',
    imageSrc: '../../images/likeno.png',
    isFavorite: false,
    inventorys: '',
    inventorysBf: '1',
    prices: '',
    value_names: '',
    attr_names: '',
    attr_goodIds: '',
    hiddens: '',
    mall_id: '',
    userId:'',
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {
    var good_id = options.id
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

    wx.request({
      url: 'https://passion.njshangka.com/easywin/m/e/gooddetail',
      method: "POST",
      data: {
        "good_id": good_id,
        token: that.data.token,
      },
      header: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      success: function (res) {
        if (res.data.code == 0) {


          // 是否收藏
          if (res.data.data.favorIs == 0) {
            that.setData({
              isFavorite: false,
              imageSrc: '../../images/likeno.png'
            })
          } else {
            that.setData({
              isFavorite: true,
              imageSrc: '../../images/likeyes.png'
            })
          }
          // 富文本
          var info = res.data.data.detail;
          WxParse.wxParse('info', 'html', info, that, 5);
          // 是否有库存或者在售，判断按钮
          if (res.data.data.inventory && res.data.data.onsale == 1) {
            that.setData({
              canOrder: true
            })
          } else {
            that.setData({
              canOrder: false
            })
          }
          var detailPics = res.data.data.detailPics;
          var coverPic = detailPics.split(',')
          that.setData({
            color: '#000',
            background: '#ccc',
            detailPics: coverPic,
            dataDetails: res.data.data,
            attrs: res.data.data.attrs,
            skus: res.data.data.skus,
            inventorys: res.data.data.inventory,
            inventorysBf: res.data.data.inventory,
            prices: res.data.data.price,
          })
        } else if (res.data.code == 20 || res.data.code == 26) {
          wx.hideToast()
          // wx.navigateTo({
          //   url: '../login/login',
          // })
        }
      },
    })
  },
  // 加入购物车
  takeCart: function () {
    var that = this
    that.setData({
      hidden: true,
      hiddens: 1,
    })
  },
  // 立即购买
  takeOrder: function () {
    var that = this
    that.setData({
      hidden: true,
      hiddens: '',
    })
  },
  // 减少商品数量
  jianBtnTap: function (e) {
    var that = this
    var goodId = e.currentTarget.dataset.id
    var count = e.currentTarget.dataset.count
    var price = e.currentTarget.dataset.price
    var orderNum = e.currentTarget.dataset.ordernum
    if (orderNum > 1) {
      orderNum--;
    }
    that.setData({
      orderNum: orderNum
    })
  },
  // 增加商品数量
  jiaBtnTap: function (e) {
    var that = this
    var orderNum = e.currentTarget.dataset.ordernum
    console.log(orderNum)
    if (orderNum < that.data.inventorys) {
      orderNum++;
    }
    console.log(orderNum)
    that.setData({
      orderNum: orderNum
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

  // 关闭页面
  closeModel: function () {
    this.setData({
      hidden: false
    })
  },

  // 收藏
  takeFavorite: function (e) {
    var that = this
    var isFavorite = that.data.isFavorite
    var favorIs = e.currentTarget.dataset.favoris
    console.log(favorIs)
    if (isFavorite == false) {
      wx.request({
        url: 'https://passion.njshangka.com/easywin/m/e/gooddetail/favor',
        method: "POST",
        data: {
          "mall_id": that.data.mall_id,
          good_id: that.data.dataDetails.goodId,
          name: that.data.dataDetails.name,
          cover: that.data.detailPics[0],
          token: that.data.token,
        },
        header: {
          "Content-Type": "application/x-www-form-urlencoded",
        },
        success: function (res) {
          if (res.data.code == 0) {
            that.setData({
              imageSrc: '../../images/likeyes.png',
              isFavorite: true
            })
          }
        },
      })
    } else {
      wx.request({
        url: 'https://passion.njshangka.com/easywin/m/e/myfavorite/remove',
        method: "POST",
        data: {
          "mall_id": that.data.mall_id,
          good_id: that.data.dataDetails.goodId,
          token: that.data.token
        },
        header: {
          "Content-Type": "application/x-www-form-urlencoded",
        },
        success: function (res) {
          if (res.data.code == 0) {
            that.setData({
              imageSrc: '../../images/likeno.png',
              isFavorite: false
            })
          }
        },
      })
    }
  },

  // 选择规格
  selAttr: function (e) {
    var that = this
    var color = e.currentTarget.dataset.color
    var goodIds = e.currentTarget.dataset.id
    var value_names = e.currentTarget.dataset.name
    var attr_names = e.currentTarget.dataset.attrnames
    if (color == '#fff') {

      for (var i = 0; i < that.data.attrs[0].values.length; i++) {
        if (that.data.attrs[0].values[i].id == goodIds) {
          var ids= that.data.attrs[0].values[i].id
          that.data.attrs[0].values[i].color = '#000'
          that.data.attrs[0].values[i].background = '#ccc'
          for (var i = 0; i <= that.data.skus.length; i++) {
            var id = that.data.skus[i].valueIds
            console.log(id)
            if (id == ids) {
              console.log(that.data.skus[i].price)
              that.setData({
                inventorys: that.data.skus[i].inventory,
                prices: that.data.skus[i].price,
                color: '#fff',
                background: 'red',
                value_names: value_names,
                attr_names: attr_names,
                attr_goodIds: id,
              })
            }
          }
        }
        // else{
        //   that.data.attrs[0].values[i].color = '#fff'
        //   that.data.attrs[0].values[i].background = 'red'
        // }
      }
      var attrs=that.data.attrs
      that.setData({
        attrs: attrs,
        inventorys: that.data.inventorysBf
      })
      
    } else {
      for (var r = 0; r < that.data.attrs[0].values.length; r++) {
        if (that.data.attrs[0].values[r].id == goodIds) {
          that.data.attrs[0].values[r].color = '#fff'
          that.data.attrs[0].values[r].background = 'red'
        } 
        else {
          that.data.attrs[0].values[r].color = '#000'
          that.data.attrs[0].values[r].background = '#ccc'
        }
      }
      var attrs = that.data.attrs
      that.setData({
        attrs: attrs,
        inventorys: that.data.inventorysBf
      })
      
      // console.log(that.data.skus.length)
      for (var i = 0; i <= that.data.skus.length; i++) {

        var id = that.data.skus[i].valueIds
        console.log(id)
        if (id == goodIds) {
          console.log(that.data.skus[i].price)
          that.setData({
            inventorys: that.data.skus[i].inventory,
            prices: that.data.skus[i].price,
            color: '#fff',
            background: 'red',
            value_names: value_names,
            attr_names: attr_names,
            attr_goodIds: that.data.skus[i].id,
          })
        }
      }
      // that.setData({

      // })
    }
  },

  // 加入购物车
  confirmTake: function (e) {
    var that = this

    if (that.data.attrs == 0) {
      wx.request({
        url: 'https://passion.njshangka.com/easywin/m/e/gooddetail/good2cart',
        method: "POST",
        data: {
          "mall_id": that.data.mall_id,
          good_id: that.data.dataDetails.goodId,
          name: that.data.dataDetails.name,
          count: that.data.orderNum,
          price: that.data.prices,
          detail_pic: that.data.detailPics[0],
          sku_id: that.data.dataDetails.goodId,
          attr_names: '',
          value_names: '',
          token: that.data.token
        },
        header: {
          "Content-Type": "application/x-www-form-urlencoded",
        },
        success: function (res) {
          if (res.data.code == 0) {
            that.setData({
              hidden: false,
              value_names: '',
              attr_names: '',
              attr_goodIds: '',
            })
          }
        },
      })
    } else {
      if (that.data.attr_goodIds == '') {
        wx.showToast({
          title: '请选择规格',
          icon: 'loading',
          duration: 1200
        })

      } else {
        wx.request({
          url: 'https://passion.njshangka.com/easywin/m/e/gooddetail/good2cart',
          method: "POST",
          data: {
            "mall_id": that.data.mall_id,
            good_id: that.data.dataDetails.goodId,
            name: that.data.dataDetails.name,
            count: that.data.orderNum,
            price: that.data.prices,
            detail_pic: that.data.detailPics[0],
            sku_id: that.data.attr_goodIds,
            attr_names: that.data.attr_names,
            value_names: that.data.value_names,
            token: that.data.token
          },
          header: {
            "Content-Type": "application/x-www-form-urlencoded",
          },
          success: function (res) {
            if (res.data.code == 0) {
              that.setData({
                hidden: false,
                value_names: '',
                attr_names: '',
                attr_goodIds: '',
                color: '#000',
                background: '#ccc',
                inventorys: that.data.inventorysBf
              })
            }
          },
        })
      }
    }



  },
  // 立即购买
  buyNow: function (e) {
    var that = this

    wx.setStorageSync('from_cart', 0)
    if (that.data.attr_goodIds == '') {
      wx.showToast({
        title: '请选择规格',
        icon: 'loading',
        duration: 1200
      })

    } else {
      var goods = [{ id: that.data.dataDetails.goodId, cnt: that.data.orderNum, skuId: that.data.attr_goodIds, attrNames: that.data.attr_names, valueNames: that.data.value_names }]
      console.log(goods)
      wx.setStorageSync('goods', goods)
      wx.navigateTo({
        url: '../comfireOrder/comfireOrder',
      })

      // wx.request({
      //   url: 'https://passion.njshangka.com/easywin/m/e/buy/orderconfirmpage',
      //   method: "POST",
      //   data: {
      //     "mall_id": that.data.mall_id,
      //     good_id: that.data.dataDetails.goodId,
      //     name: that.data.dataDetails.name,
      //     count: that.data.orderNum,
      //     price: that.data.prices,
      //     detail_pic: that.data.detailPics[0],
      //     sku_id: that.data.attr_goodIds,
      //     attr_names: that.data.attr_names,
      //     value_names: that.data.value_names,
      //     token: that.data.token
      //   },
      //   header: {
      //     "Content-Type": "application/x-www-form-urlencoded",
      //   },
      //   success: function (res) {
      //     if (res.data.code == 0) {
      //       that.setData({
      //         hidden: false,
      //         value_names: '',
      //         attr_names: '',
      //         attr_goodIds: '',
      //         color: '#000',
      //         background: '#ccc',
      //         inventorys: that.data.inventorysBf
      //       })
      //     }
      //   },
      // })
    }
    // }

  },
})