'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = undefined;

var _wepy = require('./../../npm/wepy/lib/wepy.js');

var _wepy2 = _interopRequireDefault(_wepy);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var Counter = function (_wepy$component) {
  _inherits(Counter, _wepy$component);

  function Counter() {
    var _ref;

    var _temp, _this, _ret;

    _classCallCheck(this, Counter);

    for (var _len = arguments.length, args = Array(_len), _key = 0; _key < _len; _key++) {
      args[_key] = arguments[_key];
    }

    return _ret = (_temp = (_this = _possibleConstructorReturn(this, (_ref = Counter.__proto__ || Object.getPrototypeOf(Counter)).call.apply(_ref, [this].concat(args))), _this), _this.data = {
      startX: null,
      moveX: null
    }, _this.props = {
      swipeData: Object
    }, _this.methods = {
      ts: function ts(e) {
        if (e.touches.length === 1) {
          this.startX = e.touches[0].clientX;
          this.moveX = e.touches[0].clientX;
        }
      },
      tm: function tm(e) {
        this.swipeData.style = this.swipeData.style ? this.swipeData.style : 0;

        if (e.touches.length === 1) {
          // 手指起始点位置与移动期间的差值
          var distenceX = this.moveX - e.touches[0].clientX;
          this.moveX = e.touches[0].clientX;

          if (this.swipeData.style - distenceX < -140) {
            this.swipeData.style = -140;
          } else if (this.swipeData.style - distenceX > 0) {
            this.swipeData.style = 0;
          } else {
            this.swipeData.style = this.swipeData.style - distenceX;
          }

          this.$apply();
        }
      },
      te: function te(e) {
        this.swipeData.style = this.swipeData.style ? this.swipeData.style : 0;

        if (e.changedTouches.length === 1) {
          if (this.swipeData.style <= -70) {
            this.swipeData.style = -140;
          } else {
            this.swipeData.style = 0;
          }

          this.$apply();
        }
      },
      handleRightBtnTap: function handleRightBtnTap() {
        this.$emit('delItem', this.swipeData);
      }
    }, _temp), _possibleConstructorReturn(_this, _ret);
  }

  return Counter;
}(_wepy2.default.component);

exports.default = Counter;
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIndlcHktc3dpcGUtZGVsZXRlLmpzIl0sIm5hbWVzIjpbIkNvdW50ZXIiLCJkYXRhIiwic3RhcnRYIiwibW92ZVgiLCJwcm9wcyIsInN3aXBlRGF0YSIsIk9iamVjdCIsIm1ldGhvZHMiLCJ0cyIsImUiLCJ0b3VjaGVzIiwibGVuZ3RoIiwiY2xpZW50WCIsInRtIiwic3R5bGUiLCJkaXN0ZW5jZVgiLCIkYXBwbHkiLCJ0ZSIsImNoYW5nZWRUb3VjaGVzIiwiaGFuZGxlUmlnaHRCdG5UYXAiLCIkZW1pdCIsImNvbXBvbmVudCJdLCJtYXBwaW5ncyI6Ijs7Ozs7OztBQUNBOzs7Ozs7Ozs7Ozs7SUFFcUJBLE87Ozs7Ozs7Ozs7Ozs7O3dMQUNuQkMsSSxHQUFPO0FBQ0xDLGNBQVEsSUFESDtBQUVMQyxhQUFPO0FBRkYsSyxRQUtQQyxLLEdBQVE7QUFDTkMsaUJBQVdDO0FBREwsSyxRQUlSQyxPLEdBQVU7QUFDUkMsUUFEUSxjQUNKQyxDQURJLEVBQ0Q7QUFDTCxZQUFJQSxFQUFFQyxPQUFGLENBQVVDLE1BQVYsS0FBcUIsQ0FBekIsRUFBNEI7QUFDMUIsZUFBS1QsTUFBTCxHQUFjTyxFQUFFQyxPQUFGLENBQVUsQ0FBVixFQUFhRSxPQUEzQjtBQUNBLGVBQUtULEtBQUwsR0FBYU0sRUFBRUMsT0FBRixDQUFVLENBQVYsRUFBYUUsT0FBMUI7QUFDRDtBQUNGLE9BTk87QUFRUkMsUUFSUSxjQVFKSixDQVJJLEVBUUQ7QUFDTCxhQUFLSixTQUFMLENBQWVTLEtBQWYsR0FBdUIsS0FBS1QsU0FBTCxDQUFlUyxLQUFmLEdBQXVCLEtBQUtULFNBQUwsQ0FBZVMsS0FBdEMsR0FBOEMsQ0FBckU7O0FBRUEsWUFBSUwsRUFBRUMsT0FBRixDQUFVQyxNQUFWLEtBQXFCLENBQXpCLEVBQTRCO0FBQzFCO0FBQ0EsY0FBTUksWUFBWSxLQUFLWixLQUFMLEdBQWFNLEVBQUVDLE9BQUYsQ0FBVSxDQUFWLEVBQWFFLE9BQTVDO0FBQ0EsZUFBS1QsS0FBTCxHQUFhTSxFQUFFQyxPQUFGLENBQVUsQ0FBVixFQUFhRSxPQUExQjs7QUFFQSxjQUFLLEtBQUtQLFNBQUwsQ0FBZVMsS0FBZixHQUF1QkMsU0FBeEIsR0FBcUMsQ0FBQyxHQUExQyxFQUErQztBQUM3QyxpQkFBS1YsU0FBTCxDQUFlUyxLQUFmLEdBQXVCLENBQUMsR0FBeEI7QUFDRCxXQUZELE1BRU8sSUFBSyxLQUFLVCxTQUFMLENBQWVTLEtBQWYsR0FBdUJDLFNBQXhCLEdBQXFDLENBQXpDLEVBQTRDO0FBQ2pELGlCQUFLVixTQUFMLENBQWVTLEtBQWYsR0FBdUIsQ0FBdkI7QUFDRCxXQUZNLE1BRUE7QUFDTCxpQkFBS1QsU0FBTCxDQUFlUyxLQUFmLEdBQXVCLEtBQUtULFNBQUwsQ0FBZVMsS0FBZixHQUF1QkMsU0FBOUM7QUFDRDs7QUFFRCxlQUFLQyxNQUFMO0FBQ0Q7QUFDRixPQTFCTztBQTRCUkMsUUE1QlEsY0E0QkpSLENBNUJJLEVBNEJEO0FBQ0wsYUFBS0osU0FBTCxDQUFlUyxLQUFmLEdBQXVCLEtBQUtULFNBQUwsQ0FBZVMsS0FBZixHQUF1QixLQUFLVCxTQUFMLENBQWVTLEtBQXRDLEdBQThDLENBQXJFOztBQUVBLFlBQUlMLEVBQUVTLGNBQUYsQ0FBaUJQLE1BQWpCLEtBQTRCLENBQWhDLEVBQW1DO0FBQ2pDLGNBQUksS0FBS04sU0FBTCxDQUFlUyxLQUFmLElBQXdCLENBQUMsRUFBN0IsRUFBaUM7QUFDL0IsaUJBQUtULFNBQUwsQ0FBZVMsS0FBZixHQUF1QixDQUFDLEdBQXhCO0FBQ0QsV0FGRCxNQUVPO0FBQ0wsaUJBQUtULFNBQUwsQ0FBZVMsS0FBZixHQUF1QixDQUF2QjtBQUNEOztBQUVELGVBQUtFLE1BQUw7QUFDRDtBQUNGLE9BeENPO0FBMENSRyx1QkExQ1EsK0JBMENhO0FBQ25CLGFBQUtDLEtBQUwsQ0FBVyxTQUFYLEVBQXNCLEtBQUtmLFNBQTNCO0FBQ0Q7QUE1Q08sSzs7OztFQVZ5QixlQUFLZ0IsUzs7a0JBQXJCckIsTyIsImZpbGUiOiJ3ZXB5LXN3aXBlLWRlbGV0ZS5qcyIsInNvdXJjZXNDb250ZW50IjpbIlxuaW1wb3J0IHdlcHkgZnJvbSAnd2VweSdcblxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgQ291bnRlciBleHRlbmRzIHdlcHkuY29tcG9uZW50IHtcbiAgZGF0YSA9IHtcbiAgICBzdGFydFg6IG51bGwsXG4gICAgbW92ZVg6IG51bGwsXG4gIH1cblxuICBwcm9wcyA9IHtcbiAgICBzd2lwZURhdGE6IE9iamVjdFxuICB9XG5cbiAgbWV0aG9kcyA9IHtcbiAgICB0cyAoZSkge1xuICAgICAgaWYgKGUudG91Y2hlcy5sZW5ndGggPT09IDEpIHtcbiAgICAgICAgdGhpcy5zdGFydFggPSBlLnRvdWNoZXNbMF0uY2xpZW50WFxuICAgICAgICB0aGlzLm1vdmVYID0gZS50b3VjaGVzWzBdLmNsaWVudFhcbiAgICAgIH1cbiAgICB9LFxuXG4gICAgdG0gKGUpIHtcbiAgICAgIHRoaXMuc3dpcGVEYXRhLnN0eWxlID0gdGhpcy5zd2lwZURhdGEuc3R5bGUgPyB0aGlzLnN3aXBlRGF0YS5zdHlsZSA6IDBcblxuICAgICAgaWYgKGUudG91Y2hlcy5sZW5ndGggPT09IDEpIHtcbiAgICAgICAgLy8g5omL5oyH6LW35aeL54K55L2N572u5LiO56e75Yqo5pyf6Ze055qE5beu5YC8XG4gICAgICAgIGNvbnN0IGRpc3RlbmNlWCA9IHRoaXMubW92ZVggLSBlLnRvdWNoZXNbMF0uY2xpZW50WFxuICAgICAgICB0aGlzLm1vdmVYID0gZS50b3VjaGVzWzBdLmNsaWVudFhcblxuICAgICAgICBpZiAoKHRoaXMuc3dpcGVEYXRhLnN0eWxlIC0gZGlzdGVuY2VYKSA8IC0xNDApIHtcbiAgICAgICAgICB0aGlzLnN3aXBlRGF0YS5zdHlsZSA9IC0xNDBcbiAgICAgICAgfSBlbHNlIGlmICgodGhpcy5zd2lwZURhdGEuc3R5bGUgLSBkaXN0ZW5jZVgpID4gMCkge1xuICAgICAgICAgIHRoaXMuc3dpcGVEYXRhLnN0eWxlID0gMFxuICAgICAgICB9IGVsc2Uge1xuICAgICAgICAgIHRoaXMuc3dpcGVEYXRhLnN0eWxlID0gdGhpcy5zd2lwZURhdGEuc3R5bGUgLSBkaXN0ZW5jZVhcbiAgICAgICAgfVxuXG4gICAgICAgIHRoaXMuJGFwcGx5KClcbiAgICAgIH1cbiAgICB9LFxuXG4gICAgdGUgKGUpIHtcbiAgICAgIHRoaXMuc3dpcGVEYXRhLnN0eWxlID0gdGhpcy5zd2lwZURhdGEuc3R5bGUgPyB0aGlzLnN3aXBlRGF0YS5zdHlsZSA6IDBcblxuICAgICAgaWYgKGUuY2hhbmdlZFRvdWNoZXMubGVuZ3RoID09PSAxKSB7XG4gICAgICAgIGlmICh0aGlzLnN3aXBlRGF0YS5zdHlsZSA8PSAtNzApIHtcbiAgICAgICAgICB0aGlzLnN3aXBlRGF0YS5zdHlsZSA9IC0xNDBcbiAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICB0aGlzLnN3aXBlRGF0YS5zdHlsZSA9IDBcbiAgICAgICAgfVxuICAgICAgICBcbiAgICAgICAgdGhpcy4kYXBwbHkoKVxuICAgICAgfVxuICAgIH0sXG5cbiAgICBoYW5kbGVSaWdodEJ0blRhcCAoKSB7XG4gICAgICB0aGlzLiRlbWl0KCdkZWxJdGVtJywgdGhpcy5zd2lwZURhdGEpXG4gICAgfVxuICB9XG59XG4iXX0=