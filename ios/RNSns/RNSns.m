//
//  RNSns.m
//  RNSns
//
//  Created by dwwang on 2017/8/29.
//  Copyright © 2017年 dwwang. All rights reserved.
//

#import "RNSns.h"
#import <React/RCTLog.h>
#import <UMShare/UMShare.h>
#import <UShareUI/UShareUI.h>

@implementation RNSns

RCT_EXPORT_MODULE();

+ (dispatch_queue_t)sharedMethodQueue
{
    static dispatch_queue_t methodQueue;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        methodQueue = dispatch_queue_create("react-native-sns", DISPATCH_QUEUE_SERIAL);
    });
    return methodQueue;
}

- (dispatch_queue_t)methodQueue
{
    return [RNSns sharedMethodQueue];
}

+ (BOOL)requiresMainQueueSetup
{
    return YES;
}

+ (BOOL)application:(UIApplication *)application handleOpenURL:(NSURL *)url
{
    BOOL result = [[UMSocialManager defaultManager] handleOpenURL:url];
    if (!result) {
        // 其他如支付等SDK的回调
    }
    return result;
}

RCT_EXPORT_METHOD(addEvent:(NSString *)name location:(NSString *)location)
{
    RCTLogInfo(@"Pretending to create an event %@ at %@", name, location);
}

RCT_EXPORT_METHOD(openLog:(BOOL)isOpen)
{
    [[UMSocialManager defaultManager] openLog:isOpen];
}

RCT_EXPORT_METHOD(setUmSocialAppkey:(NSString*)appKey)
{
    //[[UMSocialManager defaultManager] setUmSocialAppkey:appKey];
}

RCT_EXPORT_METHOD(setPlaform:(NSString *)type
                  appKey:(NSString *)appKey
                  appSecret:(NSString *)appSecret
                  redirectUrl:(NSString *)redirectUrl
                  fileProvider:(NSString *)fileProvider)
{
    [[UMSocialManager defaultManager] setPlaform:[self getUMSocialPlatformType:type]
                                          appKey:appKey
                                       appSecret:appSecret
                                     redirectURL:redirectUrl];
}

RCT_REMAP_METHOD(getPlatformInfo,
                 platform:(NSString*)type
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject)
{
    dispatch_async(dispatch_get_main_queue(), ^{
        __block BOOL isCompleted = NO;
        [[UMSocialManager defaultManager] getUserInfoWithPlatform:[self getUMSocialPlatformType:type]
                                            currentViewController:nil
                                                       completion:^(id result, NSError *error)
         {
             if (isCompleted) {
                 return;
             } else {
                 isCompleted = true;
             }
             
             if (!error && result != nil) {
                 UMSocialUserInfoResponse *resp = result;
                 
#ifdef DEBUG
                 // 第三方登录数据(为空表示平台未提供)
                 // 授权数据
                 NSLog(@" uid: %@", resp.uid);
                 NSLog(@" openid: %@", resp.openid);
                 NSLog(@" accessToken: %@", resp.accessToken);
                 NSLog(@" refreshToken: %@", resp.refreshToken);
                 NSLog(@" expiration: %@", resp.expiration);
                 
                 // 用户数据
                 NSLog(@" name: %@", resp.name);
                 NSLog(@" iconurl: %@", resp.iconurl);
                 NSLog(@" gender: %@", resp.gender);
                 
                 // 第三方平台SDK原始数据
                 NSLog(@" originalResponse: %@", resp.originalResponse);
#endif
                 
                 // 微博返回的openid为空
                 NSString *openid = resp.openid ? resp.openid : resp.uid;
                 NSDictionary *dic = [[NSDictionary alloc] initWithObjectsAndKeys:
                                      resp.uid, @"uid",
                                      openid, @"openId",
                                      resp.name, @"username",
                                      resp.iconurl, @"iconUrl",
                                      resp.gender, @"gender",
                                      nil];
                 
                 resolve(dic);
             } else {
                 //NSLog(@" getUserInfo error = %@", error);
                 reject(@"getUserInfo", @"getUserInfo error", error);
             }
         }];
    });
}

RCT_EXPORT_METHOD(showShareMenuView:(NSString *)url
                  title:(NSString *)title
                  imageUrl:(NSString *)imageUrl
                  description:(NSString *)description)
{
    dispatch_async(dispatch_get_main_queue(), ^{
        //显示分享面板
        [UMSocialUIManager setPreDefinePlatforms:@[@(UMSocialPlatformType_Sina),@(UMSocialPlatformType_QQ),@(UMSocialPlatformType_WechatSession), @(UMSocialPlatformType_WechatTimeLine)]];
        
        [UMSocialUIManager showShareMenuViewInWindowWithPlatformSelectionBlock:^(UMSocialPlatformType platformType, NSDictionary *userInfo) {
            // 根据获取的platformType确定所选平台进行下一步操作
            
            //创建分享消息对象
            UMSocialMessageObject *messageObject = [UMSocialMessageObject messageObject];
            
            //创建网页内容对象
            UMShareWebpageObject *shareObject = [UMShareWebpageObject shareObjectWithTitle:title
                                                                                     descr:description
                                                                                 thumImage:imageUrl];
            //设置网页地址
            shareObject.webpageUrl = url;
            
            //分享消息对象设置分享内容对象
            messageObject.shareObject = shareObject;
            
            //调用分享接口
            [[UMSocialManager defaultManager] shareToPlatform:platformType
                                                messageObject:messageObject
                                        currentViewController:nil
                                                   completion:^(id data, NSError *error) {
#ifdef DEBUG
                if (error) {
                   UMSocialLogInfo(@"************Share fail with error %@*********",error);
                }else{
                   if ([data isKindOfClass:[UMSocialShareResponse class]]) {
                       UMSocialShareResponse *resp = data;
                       //分享结果消息
                       UMSocialLogInfo(@"response message is %@",resp.message);
                       //第三方原始返回的数据
                       UMSocialLogInfo(@"response originalResponse data is %@",resp.originalResponse);
                       
                   }else{
                       UMSocialLogInfo(@"response data is %@",data);
                   }
                }
#endif
           }];
        }];
    });
}

RCT_REMAP_METHOD(showShareMenuViewWithImage,
                 imageBase64:(NSString *)imageBase64
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject)
{
    dispatch_async(dispatch_get_main_queue(), ^{
        //显示分享面板
        [UMSocialUIManager setPreDefinePlatforms:@[@(UMSocialPlatformType_Sina),@(UMSocialPlatformType_QQ),@(UMSocialPlatformType_WechatSession), @(UMSocialPlatformType_WechatTimeLine)]];
        
        [UMSocialUIManager showShareMenuViewInWindowWithPlatformSelectionBlock:^(UMSocialPlatformType platformType, NSDictionary *userInfo) {
            // 根据获取的platformType确定所选平台进行下一步操作
            
            //创建分享消息对象
            UMSocialMessageObject *messageObject = [UMSocialMessageObject messageObject];
            
            //创建图片对象
            UMShareImageObject *shareObject = [[UMShareImageObject alloc] init];
            //如果有缩略图，则设置缩略图
            shareObject.thumbImage = [UIImage imageNamed:@"icon-50"];
            //base64转图片
            NSData *data = [[NSData alloc] initWithBase64EncodedString:imageBase64
                                                               options:NSDataBase64DecodingIgnoreUnknownCharacters];
            [shareObject setShareImage:[UIImage imageWithData:data]];
            //分享消息对象设置分享内容对象
            messageObject.shareObject = shareObject;
            
            //调用分享接口
            [[UMSocialManager defaultManager] shareToPlatform:platformType
                                                messageObject:messageObject
                                        currentViewController:nil
                                                   completion:^(id data, NSError *error)
            {
                if (error)
                {
                    reject(@"shareImage", @"shareImage error", error);
                }
                else
                {
                    resolve(data);
                }
                                                       
#ifdef DEBUG
               if (error) {
                   UMSocialLogInfo(@"************Share fail with error %@*********",error);
               }else{
                   if ([data isKindOfClass:[UMSocialShareResponse class]]) {
                       UMSocialShareResponse *resp = data;
                       //分享结果消息
                       UMSocialLogInfo(@"response message is %@",resp.message);
                       //第三方原始返回的数据
                       UMSocialLogInfo(@"response originalResponse data is %@",resp.originalResponse);
                       
                   }else{
                       UMSocialLogInfo(@"response data is %@",data);
                   }
               }
#endif
            }];
        }];
    });
}

RCT_REMAP_METHOD(hasAnyMarketInstalled,
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject)
{
    resolve([NSNumber numberWithBool:YES]);
}

RCT_EXPORT_METHOD(appraiseInMarket)
{
    
}

- (UMSocialPlatformType)getUMSocialPlatformType:(NSString*)type
{
    UMSocialPlatformType platformType = UMSocialPlatformType_UnKnown;
    if (!type) return platformType;
    
    if ([type isEqualToString:@"weixin"])
    {
        platformType = UMSocialPlatformType_WechatSession;
    }
    else if ([type isEqualToString:@"weibo"])
    {
        platformType = UMSocialPlatformType_Sina;
    }
    else if ([type isEqualToString:@"qq"])
    {
        platformType = UMSocialPlatformType_QQ;
    }
    
    return platformType;
}

- (NSDictionary *)constantsToExport
{
    UMSocialManager *manager = [UMSocialManager defaultManager];
    return @{
             @"isWXInstall": [NSNumber numberWithBool:[manager isInstall:UMSocialPlatformType_WechatSession]],
             @"isQQInstall": [NSNumber numberWithBool:[manager isInstall:UMSocialPlatformType_QQ]],
             @"isSinaInstall": [NSNumber numberWithBool:[manager isInstall:UMSocialPlatformType_Sina]],
             };
}

@end
