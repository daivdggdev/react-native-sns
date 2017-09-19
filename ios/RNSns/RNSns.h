//
//  RNSns.h
//  RNSns
//
//  Created by dwwang on 2017/8/29.
//  Copyright © 2017年 dwwang. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>

@interface RNSns : NSObject <RCTBridgeModule>

+ (BOOL)application:(UIApplication *)application
            openURL:(NSURL *)url
  sourceApplication:(NSString *)sourceApplication
         annotation:(id)annotation;

@end
