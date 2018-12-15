'use strict';

var RNSns = require('react-native').NativeModules.RNSns;
module.exports = {
  setUmSocialAppkey: function(appKey) {
    return RNSns.setUmSocialAppkey(appKey);
  },

  openLog: function(isOpen) {
    return RNSns.openLog(isOpen);
  },

  setPlaform: function(type, appKey, appSecret, redirectUrl) {
    return RNSns.setPlaform.apply(null, arguments);
  },

  isInstall: function(type) {
    return RNSns.isInstall(type);
  },

  getPlatformInfo: function(type) {
    return RNSns.getPlatformInfo(type);
  },

  showShareMenuView: function(url, title, description) {
    return RNSns.showShareMenuView(url, title, description);
  },

  showShareMenuViewWithImage: function(imageBase64) {
    return RNSns.showShareMenuViewWithImage(imageBase64);
  },

  hasAnyMarketInstalled: function() {
    return RNSns.hasAnyMarketInstalled();
  },

  appraiseInMarket: function() {
    return RNSns.appraiseInMarket();
  }
};
