'use strict';

var RNSns = require('react-native').NativeModules.RNSns;
module.exports = {
  isWXInstall: RNSns.isWXInstall,
  isQQInstall: RNSns.isQQInstall,
  isSinaInstall: RNSns.isSinaInstall,

  setUmSocialAppkey: function(appKey) {
    return RNSns.setUmSocialAppkey(appKey);
  },

  openLog: function(isOpen) {
    return RNSns.openLog(isOpen);
  },

  setPlaform: function(type, appKey, appSecret, redirectUrl) {
    return RNSns.setPlaform.apply(null, arguments);
  },

  getPlatformInfo: function(type) {
    return RNSns.getPlatformInfo(type);
  },

  showShareMenuView: function(url, title, description) {
    return RNSns.showShareMenuView(url, title, description);
  },

  hasAnyMarketInstalled: function() {
    return RNSns.hasAnyMarketInstalled();
  },

  appraiseInMarket: function() {
    return RNSns.appraiseInMarket();
  }
};
