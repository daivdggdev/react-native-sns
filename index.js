'use strict';

var RNSns = require('react-native').NativeModules.RNSns;
module.exports = {
  isWXSupport: RNSns.isWXSupport,
  isQQSupport: RNSns.isQQSupport,
  isSinaSupport: RNSns.isSinaSupport,

  setWeixin: function(id, secret) {
    return RNSns.setWeixin(id, secret);
  },

  setSinaWeibo: function(key, secret, redirectUrl) {
    return RNSns.setSinaWeibo(key, secret, redirectUrl);
  },

  setQQZone: function(id, key) {
    return RNSns.setQQZone(id, key);
  },

  getPlatformInfo: function(type) {
    return RNSns.getPlatformInfo(type);
  },

  showShareMenuView: function(url, title, description) {
    return RNSns.showShareMenuView(url, title, description);
  }
};
