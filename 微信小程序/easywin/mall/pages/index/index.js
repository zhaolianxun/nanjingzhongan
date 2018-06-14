// pages/indexPage/indexPage.js
//获取应用实例
var app = getApp()

Page({

  /**
   * 页面的初始数据
   */
  data: {
    imgUrls: [
      'http://img02.tooopen.com/images/20150928/tooopen_sy_143912755726.jpg',
      'http://img06.tooopen.com/images/20160818/tooopen_sy_175866434296.jpg',
      'http://img06.tooopen.com/images/20160818/tooopen_sy_175833047715.jpg'
    ],
    notices: [
      '我的你的大家',
      '你的大家的他们我的们的',
      '十元优惠券你信不信的十元优惠券你信不信的十元优惠券你信不信的十元优惠券你信不信的'
    ],
    couponList: ['1', '2', '3', '4', '5'],
    shopGridList: ['http://img02.tooopen.com/images/20150928/tooopen_sy_143912755726.jpg',
      'http://img06.tooopen.com/images/20160818/tooopen_sy_175866434296.jpg',
      'http://img06.tooopen.com/images/20160818/tooopen_sy_175833047715.jpg'],
    schemeList: [],
    departmentSchemeList: [],
    indicatorDots: true,
    autoplay: true,
    interval: 3000,
    duration: 1000,
    code: [],
    // miniappId: [],
    // mall_id: '680314497113',
    doctorOneSchemeList: [],
    toPageNo: '',
    token: '',
    goodsList: [],
    mainrotaions: [],
    notices: [],
    coupons: [],
    mall_id: '',
    nickName: '',
    avatarUrl:'',
    userId:'',
    shareUserId:'',
  },

  // 记录足迹
  recordtrack: function (e) {
    var that = this
    var recoreId = e.currentTarget.dataset.id;
    var mall_id;
    console.log(recoreId)
    wx.request({
      url: 'https://passion.njshangka.com/easywin/m/useraction/recordtrack',
      data: {
        'mall_id': that.data.mall_id,
        'good_id': recoreId,
        token: that.data.token,
      },
      header: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      method: 'post',
      success: function (res) {

      }
    })
  },


  // 轮播图跳转
  clickImg: function (e) {
    var link = e.currentTarget.dataset.link;
    console.log(link.substring(0, 1))
    if (link.substring(0, 1) == 'h') {
      wx.navigateTo({
        url: '../out/out?link=' + link,
      })
    } else {
      wx.redirectTo({
        url: '../goodsDetail/goodsDetail?id=' + link,
      })
    }

  },
  /**
   * 生命周期函数--监听页面加载
   */
  onShow: function (options) {
  },
  onLoad: function (options) {
    var that = this;
    var shareUserId = wx.getStorageSync('shareUserId')
    that.setData({
      goodsList: [],
      shareUserId: shareUserId||''
    });
    console.log('shareUserId===='+that.data.shareUserId)

    // wx.removeStorageSync('token')
    wx.login({
      success: function (res) {
        console.log('code=='+res.code)
        if (res.code) {         
          wx.getUserInfo({
            success: function (res) {
              var userInfo = res.userInfo
              var nickName = userInfo.nickName
              var avatarUrl = userInfo.avatarUrl
              wx.setStorageSync('nickName', nickName)
              console.log('nickName=' + nickName)
              that.setData({
                nickName: nickName,
                avatarUrl: avatarUrl,
              })

              if (wx.getExtConfig) {
                // console.log(456)
                wx.getExtConfig({

                  success: function (res) {
                    console.log('extConfig='+res.extConfig)
                    // console.log(res)
                    // that.setData({
                    //   mall_id: res.extConfig.mall_id,
                    // })
                    console.log("59====" + res)
                    console.log("59====" + res.extConfig)
                    var mall_id = res.extConfig.mallId
                    // wx.showModal({
                    //   title: mall_id,
                    //   content: '',
                    // })
                    that.setData({
                      mall_id: mall_id,
                    })
                    wx.setStorageSync('mall_id', mall_id)
                    console.log('mall_id=' + mall_id)
                    wx.request({
                      url: 'https://passion.njshangka.com/easywin/m/useraction/login',
                      method: "POST",
                      data: {
                        "mall_id": mall_id,
                        jscode: jscode,
                        headimg: avatarUrl,
                        nickname: nickName,
                        from: that.data.shareUserId,
                      },
                      header: {
                        "Content-Type": "application/x-www-form-urlencoded",
                      },
                      success: function (res) {
                        if (res.data.code == 0) {
                          console.log(res.data.data.token)
                          wx.setStorageSync('token', res.data.data.token)
                          wx.setStorageSync('userId', res.data.data.userId)
                          that.setData({
                            userId: res.data.data.userId,
                            token: res.data.data.token
                          })
                          if (res.data.data.phone) {
                            wx.setStorageSync('bindphone', res.data.data.phone)
                          }
                          wx.request({
                            url: 'https://passion.njshangka.com/easywin/m/e/home',
                            method: "POST",
                            data: {
                              "mall_id": that.data.mall_id,
                              page_no: 1,
                              page_size: 15,
                              // token: that.data.token
                            },
                            header: {
                              "Content-Type": "application/x-www-form-urlencoded",
                            },
                            success: function (res) {
                              if (res.data.code == 0) {
                                that.setData({
                                  mainrotaions: res.data.data.mainrotaions,
                                  notices: res.data.data.notices,
                                  coupons: res.data.data.coupons,
                                })
                              } else {
                                wx.showModal({
                                  title: res.data.codeMsg,
                                  content: '',
                                })
                              }

                              var type1Starttime, type1Endtime
                              for (var i = 0; i < that.data.coupons.length; i++) {
                                type1Starttime = that.data.coupons[i].type1Starttime
                                type1Endtime = that.data.coupons[i].type1Endtime
                                that.data.coupons[i].type1Starttime = that.dateChange(type1Starttime)
                                that.data.coupons[i].type1Endtime = that.dateChange(type1Endtime)
                              }
                              var coupons = that.data.coupons
                              console.log(coupons)
                              that.setData({
                                coupons: coupons
                              })
                            },
                          })
                        } else {
                          wx.showModal({
                            title: res.data.codeMsg,
                            content: '',
                          })
                        }
                      }
                    })




                    that.lastPage(0)

                  },
                  fail: function (res) {
                    console.log(res)
                  },
                })
              }

            },
            fail: function (res) {
              console.log("res=" + res + "mmm=" + res.errMsg)
              wx.showModal({
                title: "res"+res,
                content: '',
              })
            }
          })
          that.setData({
            code: res.code,
          })
          var jscode = res.code
          // console.log("49===="+jscode)
          //发起网络请求

        } else {
          console.log('获取用户登录态失败！' + res.errMsg)
        }
      },
      fail: function (res) {
        console.log(res)
      }
    });

  },

  /*分页*/
  lastPage: function (toPageNo) {

    var that = this;
    var pageSize = 15


    var toPageNo = parseInt(toPageNo) + 1
    wx.request({
      url: 'https://passion.njshangka.com/easywin/m/e/home',
      method: "POST",
      data: {
        "mall_id": that.data.mall_id,
        page_no: toPageNo,
        page_size: pageSize,
        // token: that.data.token
      },
      header: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      success: function (res) {
        if (res.data.code == 0) {

          var goodsListArr = that.data.goodsList
          console.log("114=====" + goodsListArr)
          var newSchemeListArr = goodsListArr.concat(res.data.data.goods)
          if (res.data.data.goods.length == 0) {
            that.setData({
              W: goodsListArr,
              toPageNo: String(toPageNo)
            });
            wx.showToast({
              title: '数据已全部加载',
              // icon: 'loading',
            })
            console.log(res.data.data.goods.length)
          } else {
            console.log(res.data.data.goods.length)
            that.setData({
              goodsList: newSchemeListArr,
              toPageNo: String(toPageNo)
            });
          }

          // console.log(that.data.imgUrls)

        } else if (res.data.code == 20 || res.data.code == 26) {
          wx.hideToast()
          // wx.navigateTo({
          //   url: '../login/login',
          // })
        } else {
          wx.showModal({
            title: res.data.codeMsg,
            content: '',
          })
        }
      },
    })

  },

  getNow: function (e) {
    var that = this
    var couponId = e.currentTarget.dataset.couponid
    console.log(couponId)

    wx.request({
      url: 'https://passion.njshangka.com/easywin/m/e/home/getcoupon',
      method: "POST",
      data: {
        "mall_id": that.data.mall_id,
        coupon_id: couponId,
        token: that.data.token
      },
      header: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      success: function (res) {
        if (res.data.code == 0) {
          wx.showToast({
            title: '领取成功',
            // icon:'loading'
          })
          that.setData({
            mainrotaions: res.data.data.mainrotaions,
            notices: res.data.data.notices,
            coupons: res.data.data.coupons,
          })

          var type1Starttime, type1Endtime
          for (var i = 0; i < that.data.coupons.length; i++) {
            type1Starttime = that.data.coupons[i].type1Starttime
            type1Endtime = that.data.coupons[i].type1Endtime
            that.data.coupons[i].type1Starttime = that.dateChange(type1Starttime)
            that.data.coupons[i].type1Endtime = that.dateChange(type1Endtime)
          }
          var coupons = that.data.coupons
          console.log(coupons)
          that.setData({
            coupons: coupons
          })
        } else {
          wx.showToast({
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
    // console.log(123)
    var that = this
    that.setData({
      goodsList: [],
    })
    that.lastPage(0)
    wx.stopPullDownRefresh()
  },

  /**
   * 页面上拉触底事件的处理函数
   */
  onReachBottom: function () {
    var that = this
    var toPageNo = that.data.toPageNo
    that.lastPage(toPageNo)
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
  // 时间转换
  dateChange: function (data) {
    var date = new Date(data)
    var Y = date.getFullYear() + '/';
    var M = (date.getMonth() + 1 < 10 ? '0' + (date.getMonth() + 1) : date.getMonth() + 1) + '/';
    var D = date.getDate() < 10 ? '0' + date.getDate() : date.getDate();
    return (M + D)
  },

})