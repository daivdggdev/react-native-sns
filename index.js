'use strict';

var RNSns = require('react-native').NativeModules.RNSns;
module.exports = {
  isWXSupport: RNSns.isWXSupport,
  isQQSupport: RNSns.isQQSupport,
  isSinaSupport: RNSns.isSinaSupport,

  setPlaform: function(type, appKey, appSecret, redirectUrl) {
    return RNSns.setPlaform.apply(null, arguments);
  },

  getPlatformInfo: function(type) {
    return RNSns.getPlatformInfo(type);
  },

  showShareMenuView: function(url, title, description) {
    return RNSns.showShareMenuView(url, title, description);
  }
};
