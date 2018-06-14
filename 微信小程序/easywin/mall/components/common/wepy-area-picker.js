'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = undefined;

var _slicedToArray = function () { function sliceIterator(arr, i) { var _arr = []; var _n = true; var _d = false; var _e = undefined; try { for (var _i = arr[Symbol.iterator](), _s; !(_n = (_s = _i.next()).done); _n = true) { _arr.push(_s.value); if (i && _arr.length === i) break; } } catch (err) { _d = true; _e = err; } finally { try { if (!_n && _i["return"]) _i["return"](); } finally { if (_d) throw _e; } } return _arr; } return function (arr, i) { if (Array.isArray(arr)) { return arr; } else if (Symbol.iterator in Object(arr)) { return sliceIterator(arr, i); } else { throw new TypeError("Invalid attempt to destructure non-iterable instance"); } }; }();

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _wepy = require('./../../npm/wepy/lib/wepy.js');

var _wepy2 = _interopRequireDefault(_wepy);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _asyncToGenerator(fn) { return function () { var gen = fn.apply(this, arguments); return new Promise(function (resolve, reject) { function step(key, arg) { try { var info = gen[key](arg); var value = info.value; } catch (error) { reject(error); return; } if (info.done) { resolve(value); } else { return Promise.resolve(value).then(function (value) { step("next", value); }, function (err) { step("throw", err); }); } } return step("next"); }); }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var regions = [];

var AreaPicker = function (_wepy$component) {
  _inherits(AreaPicker, _wepy$component);

  function AreaPicker() {
    var _ref;

    var _temp, _this, _ret;

    _classCallCheck(this, AreaPicker);

    for (var _len = arguments.length, args = Array(_len), _key = 0; _key < _len; _key++) {
      args[_key] = arguments[_key];
    }

    return _ret = (_temp = (_this = _possibleConstructorReturn(this, (_ref = AreaPicker.__proto__ || Object.getPrototypeOf(AreaPicker)).call.apply(_ref, [this].concat(args))), _this), _this.data = {
      provinces: [], //获取到的所有的省
      cities: [], //选择的该省的所有市
      areas: [], //选择的该市的所有区县
      defaultValue: [0, 0, 0],
      selectedRegion: [0, 0, 0],
      animationData: {},
      show: false,
      province: '',
      city: '',
      area: ''
    }, _this.methods = {
      //取消按钮
      cancelPicker: function cancelPicker() {
        //这里也是动画，然其高度变为0
        this.hidePicker();
      },

      //确认按钮
      onAddressPick: function onAddressPick() {
        //一样是动画，级联选择页消失，效果和取消一样
        this.hidePicker();

        var _selectedRegion = _slicedToArray(this.selectedRegion, 3),
            provinceIndex = _selectedRegion[0],
            cityIndex = _selectedRegion[1],
            areaIndex = _selectedRegion[2];

        var provinces = this.provinces,
            cities = this.cities,
            areas = this.areas;

        this.province = provinces[provinceIndex];
        this.city = cities[cityIndex];
        this.area = areas[areaIndex] || {};

        if (!this.area) {
          this.area.name = '';
          this.code.code = '';
        }

        this.$emit('areaArray', this.province, this.city, this.area);
        this.$apply();
      },

      //滚动选择的时候触发事件
      bindChange: function bindChange(e) {
        //这里是获取picker-view内的picker-view-column 当前选择的是第几项
        var val = e.detail.value;
        this.cities = regions[val[0]].cities;
        this.areas = regions[val[0]].cities[val[1]].areas;
        //省变化，市区分别选中第一个
        if (this.selectedRegion[0] !== val[0]) {
          this.selectedRegion = [val[0], 0, 0];
          //市变化，区选中第一个
        } else if (this.selectedRegion[1] !== val[1]) {
          this.selectedRegion = [val[0], val[1], 0];
          //区变化，省市不变
        } else {
          this.selectedRegion = val;
        }
        //

        this.defaultValue = this.selectedRegion;
        this.$apply();
      }
    }, _temp), _possibleConstructorReturn(_this, _ret);
  }

  _createClass(AreaPicker, [{
    key: 'setAddressPickerValue',
    value: function setAddressPickerValue(province, city, area) {
      this.province = province;
      this.city = city;
      this.area = area;
      this.$apply();
    }
  }, {
    key: 'showPicker',
    value: function showPicker() {
      var fadeAnim = _wepy2.default.createAnimation({
        duration: 500,
        timingFunction: 'ease'
      });
      this.fadeAnim = fadeAnim;

      var showAnim = _wepy2.default.createAnimation({
        duration: 500,
        timingFunction: 'ease'
      });

      this.showAnim = showAnim;
      fadeAnim.backgroundColor('#000').opacity(0.5).step();
      showAnim.bottom(0 + 'rpx').step();

      this.show = true;
      this.animationData = {
        fadeAnim: fadeAnim.export(),
        showAnim: showAnim.export()
      };

      this.$apply();
    }
  }, {
    key: 'hidePicker',
    value: function hidePicker() {
      this.fadeAnim.backgroundColor('#fff').opacity(0).step();
      this.showAnim.bottom(-600 + 'rpx').step();

      this.show = false;
      this.animationData = {
        fadeAnim: this.fadeAnim.export(),
        showAnim: this.showAnim.export()
      };

      this.$apply();
    }

    //点击事件，点击弹出选择页

  }, {
    key: 'openAddressPicker',
    value: function () {
      var _ref2 = _asyncToGenerator( /*#__PURE__*/regeneratorRuntime.mark(function _callee() {
        return regeneratorRuntime.wrap(function _callee$(_context) {
          while (1) {
            switch (_context.prev = _context.next) {
              case 0:
                _context.prev = 0;
                _context.next = 3;
                return this.$parent.$parent.$parent.getRegions();

              case 3:
                regions = _context.sent;

                this.initAddressPicker();
                this.showPicker();
                _context.next = 11;
                break;

              case 8:
                _context.prev = 8;
                _context.t0 = _context['catch'](0);

                console.log(_context.t0);

              case 11:
              case 'end':
                return _context.stop();
            }
          }
        }, _callee, this, [[0, 8]]);
      }));

      function openAddressPicker() {
        return _ref2.apply(this, arguments);
      }

      return openAddressPicker;
    }()
  }, {
    key: 'initAddressPicker',


    // 这里是判断省市名称的显示
    value: function initAddressPicker(selected) {
      var _this2 = this;

      var provinces = [];
      var cities = [];
      var areas = [];
      var defaultValue = selected || [0, 0, 0];

      // 遍历所有的省，将省的名字存到provinces这个数组中
      for (var i = 0; i < regions.length; i++) {
        provinces.push({ name: regions[i].name, code: regions[i].code, id: regions[i].id });
      }

      // 检查传入的省编码是否有，有的话，选中column第一个游标为province index
      provinces.some(function (item, index) {
        if (_this2.province && item.code === _this2.province.code) {
          defaultValue[0] = index;
          return true;
        }
      });

      var rCities = regions[defaultValue[0]].cities;

      if (rCities) {
        // 这里判断这个省级里面有没有市（如数据中的香港、澳门等就没有写市）
        // 填充cities数组
        for (var _i = 0; _i < rCities.length; _i++) {
          cities.push({ name: rCities[_i].name, code: rCities[_i].code, id: rCities[_i].id });
        }
        // 这里是判断这个选择的省里面，有没有相应的下标为cityCode的市，因为这里的下标是前一次选择后的下标，
        // 比如之前选择的一个省有10个市，我刚好滑到了第十个市，现在又重新选择了省，但是这个省最多只有5个市，
        // 但是这时候的cityCode为9，而这里的市根本没有那么多，所以会报错
        var hasCity = cities.some(function (item, index) {
          if (_this2.city && item.code === _this2.city.code) {
            defaultValue[1] = index;
            return true;
          }
        });

        var rAreas = rCities[defaultValue[1]].areas;

        if (rAreas) {
          // 这里是判断选择的这个市在数据里面有没有区县
          for (var _i2 = 0; _i2 < rAreas.length; _i2++) {
            areas.push({
              name: rAreas[_i2].name,
              code: rAreas[_i2].code,
              id: rAreas[_i2].id
            });
          }
          areas.some(function (item, index) {
            if (_this2.area && item.code === _this2.area.code) {
              defaultValue[2] = index;
              return true;
            }
          }); // 这里是判断选择的这个市里有没有下标为areaCode的区县，道理同上面市的选择
        } else {
          // 如果这个市里面没有区县，那么把这个市的名字就赋值给areas这个数组
          areas.push(cities[defaultValue[1]]);
        }
      } else {
        // 如果该省级没有市，那么就把省的名字作为市和区的名字
        cities.push(provinces[defaultValue[0]]);
        areas.push(provinces[defaultValue[0]]);
      }
      //选择成功后把相应的数组赋值给相应的变量
      this.provinces = provinces;
      this.cities = cities;
      this.areas = areas;
      this.defaultValue = defaultValue;
      this.selectedRegion = defaultValue;
      this.$apply();
    }
  }]);

  return AreaPicker;
}(_wepy2.default.component);

exports.default = AreaPicker;
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIndlcHktYXJlYS1waWNrZXIuanMiXSwibmFtZXMiOlsicmVnaW9ucyIsIkFyZWFQaWNrZXIiLCJkYXRhIiwicHJvdmluY2VzIiwiY2l0aWVzIiwiYXJlYXMiLCJkZWZhdWx0VmFsdWUiLCJzZWxlY3RlZFJlZ2lvbiIsImFuaW1hdGlvbkRhdGEiLCJzaG93IiwicHJvdmluY2UiLCJjaXR5IiwiYXJlYSIsIm1ldGhvZHMiLCJjYW5jZWxQaWNrZXIiLCJoaWRlUGlja2VyIiwib25BZGRyZXNzUGljayIsInByb3ZpbmNlSW5kZXgiLCJjaXR5SW5kZXgiLCJhcmVhSW5kZXgiLCJuYW1lIiwiY29kZSIsIiRlbWl0IiwiJGFwcGx5IiwiYmluZENoYW5nZSIsImUiLCJ2YWwiLCJkZXRhaWwiLCJ2YWx1ZSIsImZhZGVBbmltIiwiY3JlYXRlQW5pbWF0aW9uIiwiZHVyYXRpb24iLCJ0aW1pbmdGdW5jdGlvbiIsInNob3dBbmltIiwiYmFja2dyb3VuZENvbG9yIiwib3BhY2l0eSIsInN0ZXAiLCJib3R0b20iLCJleHBvcnQiLCIkcGFyZW50IiwiZ2V0UmVnaW9ucyIsImluaXRBZGRyZXNzUGlja2VyIiwic2hvd1BpY2tlciIsImNvbnNvbGUiLCJsb2ciLCJzZWxlY3RlZCIsImkiLCJsZW5ndGgiLCJwdXNoIiwiaWQiLCJzb21lIiwiaXRlbSIsImluZGV4IiwickNpdGllcyIsImhhc0NpdHkiLCJyQXJlYXMiLCJjb21wb25lbnQiXSwibWFwcGluZ3MiOiI7Ozs7Ozs7Ozs7O0FBQ0E7Ozs7Ozs7Ozs7Ozs7O0FBRUEsSUFBSUEsVUFBVSxFQUFkOztJQUVxQkMsVTs7Ozs7Ozs7Ozs7Ozs7OExBQ25CQyxJLEdBQU87QUFDTEMsaUJBQVcsRUFETixFQUNVO0FBQ2ZDLGNBQVEsRUFGSCxFQUVPO0FBQ1pDLGFBQU8sRUFIRixFQUdNO0FBQ1hDLG9CQUFjLENBQUMsQ0FBRCxFQUFJLENBQUosRUFBTyxDQUFQLENBSlQ7QUFLTEMsc0JBQWdCLENBQUMsQ0FBRCxFQUFJLENBQUosRUFBTyxDQUFQLENBTFg7QUFNTEMscUJBQWUsRUFOVjtBQU9MQyxZQUFNLEtBUEQ7QUFRTEMsZ0JBQVUsRUFSTDtBQVNMQyxZQUFNLEVBVEQ7QUFVTEMsWUFBTTtBQVZELEssUUFzRVBDLE8sR0FBVTtBQUNSO0FBQ0FDLGtCQUZRLDBCQUVRO0FBQ2Q7QUFDQSxhQUFLQyxVQUFMO0FBQ0QsT0FMTzs7QUFNUjtBQUNBQyxtQkFQUSwyQkFPUztBQUNmO0FBQ0EsYUFBS0QsVUFBTDs7QUFGZSw2Q0FHK0IsS0FBS1IsY0FIcEM7QUFBQSxZQUdSVSxhQUhRO0FBQUEsWUFHT0MsU0FIUDtBQUFBLFlBR2tCQyxTQUhsQjs7QUFBQSxZQUlQaEIsU0FKTyxHQUlzQixJQUp0QixDQUlQQSxTQUpPO0FBQUEsWUFJSUMsTUFKSixHQUlzQixJQUp0QixDQUlJQSxNQUpKO0FBQUEsWUFJWUMsS0FKWixHQUlzQixJQUp0QixDQUlZQSxLQUpaOztBQUtmLGFBQUtLLFFBQUwsR0FBZ0JQLFVBQVVjLGFBQVYsQ0FBaEI7QUFDQSxhQUFLTixJQUFMLEdBQVlQLE9BQU9jLFNBQVAsQ0FBWjtBQUNBLGFBQUtOLElBQUwsR0FBWVAsTUFBTWMsU0FBTixLQUFvQixFQUFoQzs7QUFFQSxZQUFJLENBQUMsS0FBS1AsSUFBVixFQUFnQjtBQUNkLGVBQUtBLElBQUwsQ0FBVVEsSUFBVixHQUFpQixFQUFqQjtBQUNBLGVBQUtDLElBQUwsQ0FBVUEsSUFBVixHQUFpQixFQUFqQjtBQUNEOztBQUVELGFBQUtDLEtBQUwsQ0FBVyxXQUFYLEVBQXdCLEtBQUtaLFFBQTdCLEVBQXVDLEtBQUtDLElBQTVDLEVBQWtELEtBQUtDLElBQXZEO0FBQ0EsYUFBS1csTUFBTDtBQUNELE9BdkJPOztBQXdCUjtBQUNBQyxnQkF6QlEsc0JBeUJJQyxDQXpCSixFQXlCTztBQUNiO0FBQ0EsWUFBTUMsTUFBTUQsRUFBRUUsTUFBRixDQUFTQyxLQUFyQjtBQUNBLGFBQUt4QixNQUFMLEdBQWNKLFFBQVEwQixJQUFJLENBQUosQ0FBUixFQUFnQnRCLE1BQTlCO0FBQ0EsYUFBS0MsS0FBTCxHQUFhTCxRQUFRMEIsSUFBSSxDQUFKLENBQVIsRUFBZ0J0QixNQUFoQixDQUF1QnNCLElBQUksQ0FBSixDQUF2QixFQUErQnJCLEtBQTVDO0FBQ0E7QUFDQSxZQUFJLEtBQUtFLGNBQUwsQ0FBb0IsQ0FBcEIsTUFBMkJtQixJQUFJLENBQUosQ0FBL0IsRUFBdUM7QUFDckMsZUFBS25CLGNBQUwsR0FBc0IsQ0FBQ21CLElBQUksQ0FBSixDQUFELEVBQVMsQ0FBVCxFQUFZLENBQVosQ0FBdEI7QUFDQTtBQUNELFNBSEQsTUFHTyxJQUFJLEtBQUtuQixjQUFMLENBQW9CLENBQXBCLE1BQTJCbUIsSUFBSSxDQUFKLENBQS9CLEVBQXVDO0FBQzVDLGVBQUtuQixjQUFMLEdBQXNCLENBQUNtQixJQUFJLENBQUosQ0FBRCxFQUFTQSxJQUFJLENBQUosQ0FBVCxFQUFpQixDQUFqQixDQUF0QjtBQUNBO0FBQ0QsU0FITSxNQUdBO0FBQ0wsZUFBS25CLGNBQUwsR0FBc0JtQixHQUF0QjtBQUNEO0FBQ0Q7O0FBRUEsYUFBS3BCLFlBQUwsR0FBb0IsS0FBS0MsY0FBekI7QUFDQSxhQUFLZ0IsTUFBTDtBQUNEO0FBNUNPLEs7Ozs7OzBDQXpEYWIsUSxFQUFVQyxJLEVBQU1DLEksRUFBTTtBQUMzQyxXQUFLRixRQUFMLEdBQWdCQSxRQUFoQjtBQUNBLFdBQUtDLElBQUwsR0FBWUEsSUFBWjtBQUNBLFdBQUtDLElBQUwsR0FBWUEsSUFBWjtBQUNBLFdBQUtXLE1BQUw7QUFDRDs7O2lDQUVhO0FBQ1osVUFBTU0sV0FBVyxlQUFLQyxlQUFMLENBQXFCO0FBQ3BDQyxrQkFBVSxHQUQwQjtBQUVwQ0Msd0JBQWdCO0FBRm9CLE9BQXJCLENBQWpCO0FBSUEsV0FBS0gsUUFBTCxHQUFnQkEsUUFBaEI7O0FBRUEsVUFBTUksV0FBVyxlQUFLSCxlQUFMLENBQXFCO0FBQ3BDQyxrQkFBVSxHQUQwQjtBQUVwQ0Msd0JBQWdCO0FBRm9CLE9BQXJCLENBQWpCOztBQUtBLFdBQUtDLFFBQUwsR0FBZ0JBLFFBQWhCO0FBQ0FKLGVBQVNLLGVBQVQsQ0FBeUIsTUFBekIsRUFBaUNDLE9BQWpDLENBQXlDLEdBQXpDLEVBQThDQyxJQUE5QztBQUNBSCxlQUFTSSxNQUFULENBQWdCLElBQUksS0FBcEIsRUFBMkJELElBQTNCOztBQUVBLFdBQUszQixJQUFMLEdBQVksSUFBWjtBQUNBLFdBQUtELGFBQUwsR0FBcUI7QUFDbkJxQixrQkFBVUEsU0FBU1MsTUFBVCxFQURTO0FBRW5CTCxrQkFBVUEsU0FBU0ssTUFBVDtBQUZTLE9BQXJCOztBQUtBLFdBQUtmLE1BQUw7QUFDRDs7O2lDQUVhO0FBQ1osV0FBS00sUUFBTCxDQUFjSyxlQUFkLENBQThCLE1BQTlCLEVBQXNDQyxPQUF0QyxDQUE4QyxDQUE5QyxFQUFpREMsSUFBakQ7QUFDQSxXQUFLSCxRQUFMLENBQWNJLE1BQWQsQ0FBcUIsQ0FBQyxHQUFELEdBQU8sS0FBNUIsRUFBbUNELElBQW5DOztBQUVBLFdBQUszQixJQUFMLEdBQVksS0FBWjtBQUNBLFdBQUtELGFBQUwsR0FBcUI7QUFDbkJxQixrQkFBVSxLQUFLQSxRQUFMLENBQWNTLE1BQWQsRUFEUztBQUVuQkwsa0JBQVUsS0FBS0EsUUFBTCxDQUFjSyxNQUFkO0FBRlMsT0FBckI7O0FBS0EsV0FBS2YsTUFBTDtBQUNEOztBQUVEOzs7Ozs7Ozs7Ozs7dUJBR29CLEtBQUtnQixPQUFMLENBQWFBLE9BQWIsQ0FBcUJBLE9BQXJCLENBQTZCQyxVQUE3QixFOzs7QUFBaEJ4Qyx1Qjs7QUFDQSxxQkFBS3lDLGlCQUFMO0FBQ0EscUJBQUtDLFVBQUw7Ozs7Ozs7O0FBRUFDLHdCQUFRQyxHQUFSOzs7Ozs7Ozs7Ozs7Ozs7Ozs7OztBQW9ESjtzQ0FDbUJDLFEsRUFBVTtBQUFBOztBQUMzQixVQUFJMUMsWUFBWSxFQUFoQjtBQUNBLFVBQUlDLFNBQVMsRUFBYjtBQUNBLFVBQUlDLFFBQVEsRUFBWjtBQUNBLFVBQUlDLGVBQWV1QyxZQUFZLENBQUMsQ0FBRCxFQUFJLENBQUosRUFBTyxDQUFQLENBQS9COztBQUVBO0FBQ0EsV0FBSyxJQUFJQyxJQUFJLENBQWIsRUFBZ0JBLElBQUk5QyxRQUFRK0MsTUFBNUIsRUFBb0NELEdBQXBDLEVBQXlDO0FBQ3ZDM0Msa0JBQVU2QyxJQUFWLENBQWUsRUFBRTVCLE1BQU1wQixRQUFROEMsQ0FBUixFQUFXMUIsSUFBbkIsRUFBeUJDLE1BQU1yQixRQUFROEMsQ0FBUixFQUFXekIsSUFBMUMsRUFBZ0Q0QixJQUFJakQsUUFBUThDLENBQVIsRUFBV0csRUFBL0QsRUFBZjtBQUNEOztBQUVEO0FBQ0E5QyxnQkFBVStDLElBQVYsQ0FBZSxVQUFDQyxJQUFELEVBQU9DLEtBQVAsRUFBaUI7QUFDOUIsWUFBSSxPQUFLMUMsUUFBTCxJQUFpQnlDLEtBQUs5QixJQUFMLEtBQWMsT0FBS1gsUUFBTCxDQUFjVyxJQUFqRCxFQUF1RDtBQUNyRGYsdUJBQWEsQ0FBYixJQUFrQjhDLEtBQWxCO0FBQ0EsaUJBQU8sSUFBUDtBQUNEO0FBQ0YsT0FMRDs7QUFPQSxVQUFNQyxVQUFVckQsUUFBUU0sYUFBYSxDQUFiLENBQVIsRUFBeUJGLE1BQXpDOztBQUVBLFVBQUlpRCxPQUFKLEVBQWE7QUFBRTtBQUNiO0FBQ0EsYUFBSyxJQUFJUCxLQUFJLENBQWIsRUFBZ0JBLEtBQUlPLFFBQVFOLE1BQTVCLEVBQW9DRCxJQUFwQyxFQUF5QztBQUN2QzFDLGlCQUFPNEMsSUFBUCxDQUFZLEVBQUU1QixNQUFNaUMsUUFBUVAsRUFBUixFQUFXMUIsSUFBbkIsRUFBeUJDLE1BQU1nQyxRQUFRUCxFQUFSLEVBQVd6QixJQUExQyxFQUFnRDRCLElBQUlJLFFBQVFQLEVBQVIsRUFBV0csRUFBL0QsRUFBWjtBQUNEO0FBQ0Q7QUFDQTtBQUNBO0FBQ0EsWUFBTUssVUFBVWxELE9BQU84QyxJQUFQLENBQVksVUFBQ0MsSUFBRCxFQUFPQyxLQUFQLEVBQWlCO0FBQzNDLGNBQUksT0FBS3pDLElBQUwsSUFBYXdDLEtBQUs5QixJQUFMLEtBQWMsT0FBS1YsSUFBTCxDQUFVVSxJQUF6QyxFQUErQztBQUM3Q2YseUJBQWEsQ0FBYixJQUFrQjhDLEtBQWxCO0FBQ0EsbUJBQU8sSUFBUDtBQUNEO0FBQ0YsU0FMZSxDQUFoQjs7QUFPQSxZQUFNRyxTQUFTRixRQUFRL0MsYUFBYSxDQUFiLENBQVIsRUFBeUJELEtBQXhDOztBQUVBLFlBQUlrRCxNQUFKLEVBQVk7QUFBRTtBQUNaLGVBQUssSUFBSVQsTUFBSSxDQUFiLEVBQWdCQSxNQUFJUyxPQUFPUixNQUEzQixFQUFtQ0QsS0FBbkMsRUFBd0M7QUFDdEN6QyxrQkFBTTJDLElBQU4sQ0FBVztBQUNUNUIsb0JBQU1tQyxPQUFPVCxHQUFQLEVBQVUxQixJQURQO0FBRVRDLG9CQUFNa0MsT0FBT1QsR0FBUCxFQUFVekIsSUFGUDtBQUdUNEIsa0JBQUlNLE9BQU9ULEdBQVAsRUFBVUc7QUFITCxhQUFYO0FBS0Q7QUFDRDVDLGdCQUFNNkMsSUFBTixDQUFXLFVBQUNDLElBQUQsRUFBT0MsS0FBUCxFQUFpQjtBQUMxQixnQkFBSSxPQUFLeEMsSUFBTCxJQUFhdUMsS0FBSzlCLElBQUwsS0FBYyxPQUFLVCxJQUFMLENBQVVTLElBQXpDLEVBQStDO0FBQzdDZiwyQkFBYSxDQUFiLElBQWtCOEMsS0FBbEI7QUFDQSxxQkFBTyxJQUFQO0FBQ0Q7QUFDRixXQUxELEVBUlUsQ0FhUDtBQUNKLFNBZEQsTUFjTztBQUNMO0FBQ0EvQyxnQkFBTTJDLElBQU4sQ0FBVzVDLE9BQU9FLGFBQWEsQ0FBYixDQUFQLENBQVg7QUFDRDtBQUNGLE9BbkNELE1BbUNPO0FBQ0w7QUFDQUYsZUFBTzRDLElBQVAsQ0FBWTdDLFVBQVVHLGFBQWEsQ0FBYixDQUFWLENBQVo7QUFDQUQsY0FBTTJDLElBQU4sQ0FBVzdDLFVBQVVHLGFBQWEsQ0FBYixDQUFWLENBQVg7QUFDRDtBQUNEO0FBQ0EsV0FBS0gsU0FBTCxHQUFpQkEsU0FBakI7QUFDQSxXQUFLQyxNQUFMLEdBQWNBLE1BQWQ7QUFDQSxXQUFLQyxLQUFMLEdBQWFBLEtBQWI7QUFDQSxXQUFLQyxZQUFMLEdBQW9CQSxZQUFwQjtBQUNBLFdBQUtDLGNBQUwsR0FBc0JELFlBQXRCO0FBQ0EsV0FBS2lCLE1BQUw7QUFDRDs7OztFQTNMcUMsZUFBS2lDLFM7O2tCQUF4QnZELFUiLCJmaWxlIjoid2VweS1hcmVhLXBpY2tlci5qcyIsInNvdXJjZXNDb250ZW50IjpbIlxuaW1wb3J0IHdlcHkgZnJvbSAnd2VweSdcblxubGV0IHJlZ2lvbnMgPSBbXVxuXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBBcmVhUGlja2VyIGV4dGVuZHMgd2VweS5jb21wb25lbnQge1xuICBkYXRhID0ge1xuICAgIHByb3ZpbmNlczogW10sIC8v6I635Y+W5Yiw55qE5omA5pyJ55qE55yBXG4gICAgY2l0aWVzOiBbXSwgLy/pgInmi6nnmoTor6XnnIHnmoTmiYDmnInluIJcbiAgICBhcmVhczogW10sIC8v6YCJ5oup55qE6K+l5biC55qE5omA5pyJ5Yy65Y6/XG4gICAgZGVmYXVsdFZhbHVlOiBbMCwgMCwgMF0sXG4gICAgc2VsZWN0ZWRSZWdpb246IFswLCAwLCAwXSxcbiAgICBhbmltYXRpb25EYXRhOiB7fSxcbiAgICBzaG93OiBmYWxzZSxcbiAgICBwcm92aW5jZTogJycsXG4gICAgY2l0eTogJycsXG4gICAgYXJlYTogJydcbiAgfVxuXG4gIHNldEFkZHJlc3NQaWNrZXJWYWx1ZSAocHJvdmluY2UsIGNpdHksIGFyZWEpIHtcbiAgICB0aGlzLnByb3ZpbmNlID0gcHJvdmluY2VcbiAgICB0aGlzLmNpdHkgPSBjaXR5XG4gICAgdGhpcy5hcmVhID0gYXJlYVxuICAgIHRoaXMuJGFwcGx5KClcbiAgfVxuXG4gIHNob3dQaWNrZXIgKCkge1xuICAgIGNvbnN0IGZhZGVBbmltID0gd2VweS5jcmVhdGVBbmltYXRpb24oe1xuICAgICAgZHVyYXRpb246IDUwMCxcbiAgICAgIHRpbWluZ0Z1bmN0aW9uOiAnZWFzZSdcbiAgICB9KVxuICAgIHRoaXMuZmFkZUFuaW0gPSBmYWRlQW5pbVxuXG4gICAgY29uc3Qgc2hvd0FuaW0gPSB3ZXB5LmNyZWF0ZUFuaW1hdGlvbih7XG4gICAgICBkdXJhdGlvbjogNTAwLFxuICAgICAgdGltaW5nRnVuY3Rpb246ICdlYXNlJ1xuICAgIH0pXG5cbiAgICB0aGlzLnNob3dBbmltID0gc2hvd0FuaW1cbiAgICBmYWRlQW5pbS5iYWNrZ3JvdW5kQ29sb3IoJyMwMDAnKS5vcGFjaXR5KDAuNSkuc3RlcCgpXG4gICAgc2hvd0FuaW0uYm90dG9tKDAgKyAncnB4Jykuc3RlcCgpXG5cbiAgICB0aGlzLnNob3cgPSB0cnVlXG4gICAgdGhpcy5hbmltYXRpb25EYXRhID0ge1xuICAgICAgZmFkZUFuaW06IGZhZGVBbmltLmV4cG9ydCgpLFxuICAgICAgc2hvd0FuaW06IHNob3dBbmltLmV4cG9ydCgpXG4gICAgfVxuXG4gICAgdGhpcy4kYXBwbHkoKVxuICB9XG5cbiAgaGlkZVBpY2tlciAoKSB7XG4gICAgdGhpcy5mYWRlQW5pbS5iYWNrZ3JvdW5kQ29sb3IoJyNmZmYnKS5vcGFjaXR5KDApLnN0ZXAoKVxuICAgIHRoaXMuc2hvd0FuaW0uYm90dG9tKC02MDAgKyAncnB4Jykuc3RlcCgpXG5cbiAgICB0aGlzLnNob3cgPSBmYWxzZVxuICAgIHRoaXMuYW5pbWF0aW9uRGF0YSA9IHtcbiAgICAgIGZhZGVBbmltOiB0aGlzLmZhZGVBbmltLmV4cG9ydCgpLFxuICAgICAgc2hvd0FuaW06IHRoaXMuc2hvd0FuaW0uZXhwb3J0KClcbiAgICB9XG5cbiAgICB0aGlzLiRhcHBseSgpXG4gIH1cblxuICAvL+eCueWHu+S6i+S7tu+8jOeCueWHu+W8ueWHuumAieaLqemhtVxuICBhc3luYyBvcGVuQWRkcmVzc1BpY2tlciAoKSB7XG4gICAgdHJ5IHtcbiAgICAgIHJlZ2lvbnMgPSBhd2FpdCB0aGlzLiRwYXJlbnQuJHBhcmVudC4kcGFyZW50LmdldFJlZ2lvbnMoKVxuICAgICAgdGhpcy5pbml0QWRkcmVzc1BpY2tlcigpXG4gICAgICB0aGlzLnNob3dQaWNrZXIoKVxuICAgIH0gY2F0Y2ggKGUpIHtcbiAgICAgIGNvbnNvbGUubG9nKGUpXG4gICAgfVxuICB9XG5cblxuICBtZXRob2RzID0ge1xuICAgIC8v5Y+W5raI5oyJ6ZKuXG4gICAgY2FuY2VsUGlja2VyICgpIHtcbiAgICAgIC8v6L+Z6YeM5Lmf5piv5Yqo55S777yM54S25YW26auY5bqm5Y+Y5Li6MFxuICAgICAgdGhpcy5oaWRlUGlja2VyKClcbiAgICB9LFxuICAgIC8v56Gu6K6k5oyJ6ZKuXG4gICAgb25BZGRyZXNzUGljayAoKSB7XG4gICAgICAvL+S4gOagt+aYr+WKqOeUu++8jOe6p+iBlOmAieaLqemhtea2iOWkse+8jOaViOaenOWSjOWPlua2iOS4gOagt1xuICAgICAgdGhpcy5oaWRlUGlja2VyKClcbiAgICAgIGNvbnN0IFtwcm92aW5jZUluZGV4LCBjaXR5SW5kZXgsIGFyZWFJbmRleF0gPSB0aGlzLnNlbGVjdGVkUmVnaW9uXG4gICAgICBjb25zdCB7IHByb3ZpbmNlcywgY2l0aWVzLCBhcmVhcyB9ID0gdGhpc1xuICAgICAgdGhpcy5wcm92aW5jZSA9IHByb3ZpbmNlc1twcm92aW5jZUluZGV4XVxuICAgICAgdGhpcy5jaXR5ID0gY2l0aWVzW2NpdHlJbmRleF1cbiAgICAgIHRoaXMuYXJlYSA9IGFyZWFzW2FyZWFJbmRleF0gfHwge31cblxuICAgICAgaWYgKCF0aGlzLmFyZWEpIHtcbiAgICAgICAgdGhpcy5hcmVhLm5hbWUgPSAnJ1xuICAgICAgICB0aGlzLmNvZGUuY29kZSA9ICcnXG4gICAgICB9XG5cbiAgICAgIHRoaXMuJGVtaXQoJ2FyZWFBcnJheScsIHRoaXMucHJvdmluY2UsIHRoaXMuY2l0eSwgdGhpcy5hcmVhKVxuICAgICAgdGhpcy4kYXBwbHkoKVxuICAgIH0sXG4gICAgLy/mu5rliqjpgInmi6nnmoTml7blgJnop6blj5Hkuovku7ZcbiAgICBiaW5kQ2hhbmdlIChlKSB7XG4gICAgICAvL+i/memHjOaYr+iOt+WPlnBpY2tlci12aWV35YaF55qEcGlja2VyLXZpZXctY29sdW1uIOW9k+WJjemAieaLqeeahOaYr+esrOWHoOmhuVxuICAgICAgY29uc3QgdmFsID0gZS5kZXRhaWwudmFsdWVcbiAgICAgIHRoaXMuY2l0aWVzID0gcmVnaW9uc1t2YWxbMF1dLmNpdGllc1xuICAgICAgdGhpcy5hcmVhcyA9IHJlZ2lvbnNbdmFsWzBdXS5jaXRpZXNbdmFsWzFdXS5hcmVhc1xuICAgICAgLy/nnIHlj5jljJbvvIzluILljLrliIbliKvpgInkuK3nrKzkuIDkuKpcbiAgICAgIGlmICh0aGlzLnNlbGVjdGVkUmVnaW9uWzBdICE9PSB2YWxbMF0pIHtcbiAgICAgICAgdGhpcy5zZWxlY3RlZFJlZ2lvbiA9IFt2YWxbMF0sIDAsIDBdXG4gICAgICAgIC8v5biC5Y+Y5YyW77yM5Yy66YCJ5Lit56ys5LiA5LiqXG4gICAgICB9IGVsc2UgaWYgKHRoaXMuc2VsZWN0ZWRSZWdpb25bMV0gIT09IHZhbFsxXSkge1xuICAgICAgICB0aGlzLnNlbGVjdGVkUmVnaW9uID0gW3ZhbFswXSwgdmFsWzFdLCAwXVxuICAgICAgICAvL+WMuuWPmOWMlu+8jOecgeW4guS4jeWPmFxuICAgICAgfSBlbHNlIHtcbiAgICAgICAgdGhpcy5zZWxlY3RlZFJlZ2lvbiA9IHZhbFxuICAgICAgfVxuICAgICAgLy9cblxuICAgICAgdGhpcy5kZWZhdWx0VmFsdWUgPSB0aGlzLnNlbGVjdGVkUmVnaW9uXG4gICAgICB0aGlzLiRhcHBseSgpXG4gICAgfVxuICB9XG5cbiAgLy8g6L+Z6YeM5piv5Yik5pat55yB5biC5ZCN56ew55qE5pi+56S6XG4gIGluaXRBZGRyZXNzUGlja2VyIChzZWxlY3RlZCkge1xuICAgIGxldCBwcm92aW5jZXMgPSBbXVxuICAgIGxldCBjaXRpZXMgPSBbXVxuICAgIGxldCBhcmVhcyA9IFtdXG4gICAgbGV0IGRlZmF1bHRWYWx1ZSA9IHNlbGVjdGVkIHx8IFswLCAwLCAwXVxuXG4gICAgLy8g6YGN5Y6G5omA5pyJ55qE55yB77yM5bCG55yB55qE5ZCN5a2X5a2Y5YiwcHJvdmluY2Vz6L+Z5Liq5pWw57uE5LitXG4gICAgZm9yIChsZXQgaSA9IDA7IGkgPCByZWdpb25zLmxlbmd0aDsgaSsrKSB7XG4gICAgICBwcm92aW5jZXMucHVzaCh7IG5hbWU6IHJlZ2lvbnNbaV0ubmFtZSwgY29kZTogcmVnaW9uc1tpXS5jb2RlLCBpZDogcmVnaW9uc1tpXS5pZCB9KVxuICAgIH1cblxuICAgIC8vIOajgOafpeS8oOWFpeeahOecgee8lueggeaYr+WQpuacie+8jOacieeahOivne+8jOmAieS4rWNvbHVtbuesrOS4gOS4qua4uOagh+S4unByb3ZpbmNlIGluZGV4XG4gICAgcHJvdmluY2VzLnNvbWUoKGl0ZW0sIGluZGV4KSA9PiB7XG4gICAgICBpZiAodGhpcy5wcm92aW5jZSAmJiBpdGVtLmNvZGUgPT09IHRoaXMucHJvdmluY2UuY29kZSkge1xuICAgICAgICBkZWZhdWx0VmFsdWVbMF0gPSBpbmRleFxuICAgICAgICByZXR1cm4gdHJ1ZVxuICAgICAgfVxuICAgIH0pXG5cbiAgICBjb25zdCByQ2l0aWVzID0gcmVnaW9uc1tkZWZhdWx0VmFsdWVbMF1dLmNpdGllc1xuXG4gICAgaWYgKHJDaXRpZXMpIHsgLy8g6L+Z6YeM5Yik5pat6L+Z5Liq55yB57qn6YeM6Z2i5pyJ5rKh5pyJ5biC77yI5aaC5pWw5o2u5Lit55qE6aaZ5riv44CB5r6z6Zeo562J5bCx5rKh5pyJ5YaZ5biC77yJXG4gICAgICAvLyDloavlhYVjaXRpZXPmlbDnu4RcbiAgICAgIGZvciAobGV0IGkgPSAwOyBpIDwgckNpdGllcy5sZW5ndGg7IGkrKykge1xuICAgICAgICBjaXRpZXMucHVzaCh7IG5hbWU6IHJDaXRpZXNbaV0ubmFtZSwgY29kZTogckNpdGllc1tpXS5jb2RlLCBpZDogckNpdGllc1tpXS5pZCB9KVxuICAgICAgfVxuICAgICAgLy8g6L+Z6YeM5piv5Yik5pat6L+Z5Liq6YCJ5oup55qE55yB6YeM6Z2i77yM5pyJ5rKh5pyJ55u45bqU55qE5LiL5qCH5Li6Y2l0eUNvZGXnmoTluILvvIzlm6DkuLrov5nph4znmoTkuIvmoIfmmK/liY3kuIDmrKHpgInmi6nlkI7nmoTkuIvmoIfvvIxcbiAgICAgIC8vIOavlOWmguS5i+WJjemAieaLqeeahOS4gOS4quecgeaciTEw5Liq5biC77yM5oiR5Yia5aW95ruR5Yiw5LqG56ys5Y2B5Liq5biC77yM546w5Zyo5Y+I6YeN5paw6YCJ5oup5LqG55yB77yM5L2G5piv6L+Z5Liq55yB5pyA5aSa5Y+q5pyJNeS4quW4gu+8jFxuICAgICAgLy8g5L2G5piv6L+Z5pe25YCZ55qEY2l0eUNvZGXkuLo577yM6ICM6L+Z6YeM55qE5biC5qC55pys5rKh5pyJ6YKj5LmI5aSa77yM5omA5Lul5Lya5oql6ZSZXG4gICAgICBjb25zdCBoYXNDaXR5ID0gY2l0aWVzLnNvbWUoKGl0ZW0sIGluZGV4KSA9PiB7XG4gICAgICAgIGlmICh0aGlzLmNpdHkgJiYgaXRlbS5jb2RlID09PSB0aGlzLmNpdHkuY29kZSkge1xuICAgICAgICAgIGRlZmF1bHRWYWx1ZVsxXSA9IGluZGV4XG4gICAgICAgICAgcmV0dXJuIHRydWVcbiAgICAgICAgfVxuICAgICAgfSlcblxuICAgICAgY29uc3QgckFyZWFzID0gckNpdGllc1tkZWZhdWx0VmFsdWVbMV1dLmFyZWFzXG5cbiAgICAgIGlmIChyQXJlYXMpIHsgLy8g6L+Z6YeM5piv5Yik5pat6YCJ5oup55qE6L+Z5Liq5biC5Zyo5pWw5o2u6YeM6Z2i5pyJ5rKh5pyJ5Yy65Y6/XG4gICAgICAgIGZvciAobGV0IGkgPSAwOyBpIDwgckFyZWFzLmxlbmd0aDsgaSsrKSB7XG4gICAgICAgICAgYXJlYXMucHVzaCh7XG4gICAgICAgICAgICBuYW1lOiByQXJlYXNbaV0ubmFtZSxcbiAgICAgICAgICAgIGNvZGU6IHJBcmVhc1tpXS5jb2RlLFxuICAgICAgICAgICAgaWQ6IHJBcmVhc1tpXS5pZFxuICAgICAgICAgIH0pXG4gICAgICAgIH1cbiAgICAgICAgYXJlYXMuc29tZSgoaXRlbSwgaW5kZXgpID0+IHtcbiAgICAgICAgICBpZiAodGhpcy5hcmVhICYmIGl0ZW0uY29kZSA9PT0gdGhpcy5hcmVhLmNvZGUpIHtcbiAgICAgICAgICAgIGRlZmF1bHRWYWx1ZVsyXSA9IGluZGV4XG4gICAgICAgICAgICByZXR1cm4gdHJ1ZVxuICAgICAgICAgIH1cbiAgICAgICAgfSkgLy8g6L+Z6YeM5piv5Yik5pat6YCJ5oup55qE6L+Z5Liq5biC6YeM5pyJ5rKh5pyJ5LiL5qCH5Li6YXJlYUNvZGXnmoTljLrljr/vvIzpgZPnkIblkIzkuIrpnaLluILnmoTpgInmi6lcbiAgICAgIH0gZWxzZSB7XG4gICAgICAgIC8vIOWmguaenOi/meS4quW4gumHjOmdouayoeacieWMuuWOv++8jOmCo+S5iOaKiui/meS4quW4gueahOWQjeWtl+Wwsei1i+WAvOe7mWFyZWFz6L+Z5Liq5pWw57uEXG4gICAgICAgIGFyZWFzLnB1c2goY2l0aWVzW2RlZmF1bHRWYWx1ZVsxXV0pXG4gICAgICB9XG4gICAgfSBlbHNlIHtcbiAgICAgIC8vIOWmguaenOivpeecgee6p+ayoeacieW4gu+8jOmCo+S5iOWwseaKiuecgeeahOWQjeWtl+S9nOS4uuW4guWSjOWMuueahOWQjeWtl1xuICAgICAgY2l0aWVzLnB1c2gocHJvdmluY2VzW2RlZmF1bHRWYWx1ZVswXV0pXG4gICAgICBhcmVhcy5wdXNoKHByb3ZpbmNlc1tkZWZhdWx0VmFsdWVbMF1dKVxuICAgIH1cbiAgICAvL+mAieaLqeaIkOWKn+WQjuaKiuebuOW6lOeahOaVsOe7hOi1i+WAvOe7meebuOW6lOeahOWPmOmHj1xuICAgIHRoaXMucHJvdmluY2VzID0gcHJvdmluY2VzXG4gICAgdGhpcy5jaXRpZXMgPSBjaXRpZXNcbiAgICB0aGlzLmFyZWFzID0gYXJlYXNcbiAgICB0aGlzLmRlZmF1bHRWYWx1ZSA9IGRlZmF1bHRWYWx1ZVxuICAgIHRoaXMuc2VsZWN0ZWRSZWdpb24gPSBkZWZhdWx0VmFsdWVcbiAgICB0aGlzLiRhcHBseSgpXG4gIH1cbn1cblxuIl19