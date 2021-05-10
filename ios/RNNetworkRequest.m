#import "RNNetworkRequest.h"
#import "AFNetworking.h"
#import <sys/socket.h>
#import <netinet/in.h>
//#import <SystemConfiguration/SystemConfiguration.h>
#import "Reachability.h"
@import MobileCoreServices;    // only needed in iOS
@implementation RNNetworkRequest
RCT_EXPORT_MODULE(RNNetworkRequest);
RCT_EXPORT_METHOD(fetch:(NSString *)urlString jsRequest:(NSDictionary *)jsRequest resolve:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{ // fetch function called from js side
  dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
    
    //Run your loop here
    if([self isNetworkAvailable] == NO){
      // network not available aborting process, wont go for the hit if there's no internet
      reject(@"error",@"Network not reachable, make sure you have an active internet connection.",nil);
    }else{
      
      NSString *post = @"POST";
      NSString *get = @"GET";
      NSString *put = @"PUT";
      NSString *delete = @"DELETE";
      
      NSString *method = [jsRequest valueForKey:@"method"];
      
      NSString *escapedString = [urlString stringByAddingPercentEncodingWithAllowedCharacters:[NSCharacterSet URLQueryAllowedCharacterSet]];
      NSURL *url = [NSURL URLWithString:escapedString];
      
      NSMutableDictionary *headers = jsRequest[@"headers"];
      if(headers == nil){
        headers = [NSMutableDictionary dictionaryWithDictionary:@{ // default headers, should be set from js side, TODO later
          @"Accept":@"application/json",
          @"Content-Type":@"application/json",
          @"X-Requested-With":@"XMLHttpRequest"
        }];
      }
      if(headers[@"Authorization"]){
        if([headers valueForKey:@"Authorization"] == nil){
          dispatch_async(dispatch_get_main_queue(), ^(void) {
            //stop your HUD here
            //This is run on the main thread
            return reject(@"error",@"value for header can not be null",nil);
          });
          
        }
      }
      
      if(method == nil){ // set default method as get
        method = get;
      }
      
      if([method isEqualToString:get]){
        [self processGet:url headers:headers resolve:resolve rejecter:reject];
      }else if([method isEqualToString:post]){
        [self processPost:url jsRequest:jsRequest headers:headers resolve:resolve rejecter:reject];
      }else if([method isEqualToString:put]){
        [self processPut:url jsRequest:jsRequest headers:headers resolve:resolve rejecter:reject];
      }else if([method isEqualToString:delete]){
        [self processDelete:url jsRequest:jsRequest headers:headers resolve:resolve rejecter:reject];
      }else{
        dispatch_async(dispatch_get_main_queue(), ^(void) {
          //stop your HUD here
          //This is run on the main thread
          return reject(@"error",@"Invalid method provided",nil);
        });
        
      }
    }
  });
}
-(void)processPost:(NSURL *)url jsRequest:(NSDictionary *)jsRequest headers:(NSDictionary *)headers resolve:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject{
  dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
    if([[headers valueForKey:@"Content-Type"] isEqualToString:@"multipart/form-data"]){ // hit is formdata
      [self processFormDataPost:url jsRequest:jsRequest headers:headers resolve:resolve rejecter:reject];
    }else{ // hit is regular json
      if([jsRequest[@"body"] isKindOfClass:[NSString class]]){ // data is stringified from js
        [self processJSONRequest:url jsRequest:jsRequest body:jsRequest[@"body"] headers:headers resolve:resolve rejecter:reject method:@"POST"];
      }else{ // string data not received
        dispatch_async(dispatch_get_main_queue(), ^(void) {
          //stop your HUD here
          //This is run on the main thread
          return reject(@"message",@"Expected json string for post request",nil);
        });
      }
    }
  });
}


-(void)processPut:(NSURL *)url jsRequest:(NSDictionary *)jsRequest headers:(NSDictionary *)headers resolve:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject{
  dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
    if([jsRequest[@"body"] isKindOfClass:[NSString class]]){ // data is stringified from js
      [self processJSONRequest:url jsRequest:jsRequest body:jsRequest[@"body"] headers:headers resolve:resolve rejecter:reject method:@"PUT"];
    }else{ // string data not received
      dispatch_async(dispatch_get_main_queue(), ^(void) {
        //stop your HUD here
        //This is run on the main thread
        return reject(@"message",@"Expected json string for put request",nil);
      });
    }
  });
}
-(void)processDelete:(NSURL *)url jsRequest:(NSDictionary *)jsRequest headers:(NSDictionary *)headers resolve:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject{
  dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
    if([jsRequest[@"body"] isKindOfClass:[NSString class]]){ // data is stringified from js
      [self processJSONRequest:url jsRequest:jsRequest body:jsRequest[@"body"] headers:headers resolve:resolve rejecter:reject method:@"DELETE"];
    }else{ // string data not received
      dispatch_async(dispatch_get_main_queue(), ^(void) {
        //stop your HUD here
        //This is run on the main thread
        return reject(@"message",@"Expected json string for delete request",nil);
      });
    }
  });
}
-(void)processGet:(NSURL *)url headers:(NSDictionary *)headers resolve:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject{
  dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:url];
    [request setAllHTTPHeaderFields:headers];
    NSURLSession *session = [NSURLSession sessionWithConfiguration:[NSURLSessionConfiguration defaultSessionConfiguration]];
    NSURLSessionDataTask *task = [session dataTaskWithRequest:request completionHandler:^(NSData * _Nullable data, NSURLResponse * _Nullable response, NSError * _Nullable error) {
      [self handleRequestResolve:data withResponse:response withError:error resolve:resolve rejecter:reject url:url];
    }];
    [task resume];
  });
}

-(void)handleRequestResolve:(NSData *) data withResponse:(NSURLResponse *) response withError:(NSError *) error resolve:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject url:(NSURL *)url{
  NSString *dataString = [[NSString alloc] initWithData:data encoding:NSISOLatin1StringEncoding];

  NSData *dataUTF8 = [dataString dataUsingEncoding:NSUTF8StringEncoding];

  NSMutableDictionary *dict = (NSMutableDictionary *) [NSJSONSerialization JSONObjectWithData:dataUTF8 options:0 error:&error];

  dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
    if(error == nil){
      NSHTTPURLResponse *httpResponse = (NSHTTPURLResponse *) response;
      NSNumber *statusCode = [NSNumber numberWithLong:[httpResponse statusCode]];
      NSMutableDictionary *dataToSend;
      if(dict != nil){
        dataToSend = [NSMutableDictionary dictionaryWithDictionary:@{@"data":dict}];
      }else{
        dataToSend = [NSMutableDictionary dictionaryWithDictionary:@{@"data":error.userInfo}];
      }
      [dataToSend setValue:statusCode forKey:@"status"];
      [dataToSend setValue:url forKey:@"url"];
      if (dataToSend != nil) {
        dispatch_async(dispatch_get_main_queue(), ^(void) {
          resolve(dataToSend);
        });
      }
    }else{
      dispatch_async(dispatch_get_main_queue(), ^(void) {
        reject(@"error",@"error",nil);
      });
    }
  });
}
-(void)processJSONRequest:(NSURL *)url jsRequest:(NSDictionary *)jsRequest body:(NSString *)body headers:(NSDictionary *)headers resolve:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject method:(NSString *)method{
  dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
  NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:url];
  [request setAllHTTPHeaderFields:headers];
  [request setHTTPMethod:method];
  [request setHTTPBody:[body dataUsingEncoding:NSUTF8StringEncoding]]; //body
  NSURLSession *session = [NSURLSession sessionWithConfiguration:[NSURLSessionConfiguration defaultSessionConfiguration]];
  NSURLSessionDataTask *task = [session dataTaskWithRequest:request completionHandler:^(NSData * _Nullable data, NSURLResponse * _Nullable response, NSError * _Nullable error) {
    [self handleRequestResolve:data withResponse:response withError:error resolve:resolve rejecter:reject url:url];
  }];
  [task resume];
  });
}
-(void)processFormDataPost:(NSURL *)url jsRequest:(NSDictionary *)jsRequest headers:(NSDictionary *)headers resolve:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject{
  dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
  NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:url];
  [request setHTTPMethod:@"POST"];
  NSString *boundary = [self generateBoundaryString];
  NSString *contentType = [NSString stringWithFormat:@"multipart/form-data; boundary=%@",boundary];
  NSObject *jsObject = [jsRequest valueForKey:@"body"];
  NSMutableDictionary *mutableHeaders = [NSMutableDictionary dictionaryWithDictionary:headers];
  [mutableHeaders setValue:contentType forKey:@"Content-Type"];
  [request setAllHTTPHeaderFields:mutableHeaders];
  [request setValue:contentType forHTTPHeaderField: @"Content-Type"];
  if([jsObject isKindOfClass:[NSDictionary class]] || [jsObject isKindOfClass:[NSMutableDictionary class]]){
    NSDictionary *jsBody =(NSDictionary *) jsObject;
    if(jsBody[@"_parts"]){
      NSArray *parts = (NSArray *) jsBody[@"_parts"];
      NSMutableData *httpBody = [NSMutableData data];
      for(NSArray *part in parts){
        NSString *key = part[0];
        if([part[1] isKindOfClass:[NSString class]]){
          [httpBody appendData:[[NSString stringWithFormat:@"--%@\r\n", boundary] dataUsingEncoding:NSUTF8StringEncoding]];
          [httpBody appendData:[[NSString stringWithFormat:@"Content-Disposition: form-data; name=\"%@\"\r\n\r\n", key] dataUsingEncoding:NSUTF8StringEncoding]];
          [httpBody appendData:[[NSString stringWithFormat:@"%@\r\n", part[1]] dataUsingEncoding:NSUTF8StringEncoding]];
        }else{
          NSDictionary *value = part[1];
          NSURL *fileURI = [NSURL URLWithString:value[@"uri"]];
          
          NSString *filename  = [fileURI lastPathComponent];
          NSData   *data      = [NSData dataWithContentsOfFile:value[@"uri"]];
          NSString *mimetype  = [self mimeTypeForPath:value[@"uri"]];
          
          [httpBody appendData:[[NSString stringWithFormat:@"--%@\r\n", boundary] dataUsingEncoding:NSUTF8StringEncoding]];
          [httpBody appendData:[[NSString stringWithFormat:@"Content-Disposition: form-data; name=\"%@\"; filename=\"%@\"\r\n", key, filename] dataUsingEncoding:NSUTF8StringEncoding]];
          [httpBody appendData:[[NSString stringWithFormat:@"Content-Type: %@\r\n\r\n", mimetype] dataUsingEncoding:NSUTF8StringEncoding]];
          [httpBody appendData:data];
          [httpBody appendData:[@"\r\n" dataUsingEncoding:NSUTF8StringEncoding]];
        }
        
      }
      [httpBody appendData:[[NSString stringWithFormat:@"--%@--\r\n", boundary] dataUsingEncoding:NSUTF8StringEncoding]];
      [request setHTTPBody:httpBody];
      NSURLSession *session = [NSURLSession sessionWithConfiguration:[NSURLSessionConfiguration defaultSessionConfiguration]];
      NSURLSessionTask *task = [session uploadTaskWithRequest:request fromData:httpBody completionHandler:^(NSData * _Nullable data, NSURLResponse * _Nullable response, NSError * _Nullable error) {
        [self handleRequestResolve:data withResponse:response withError:error resolve:resolve rejecter:reject url:url];
      }];
      [task resume];
    }
    
  }else{
    dispatch_async(dispatch_get_main_queue(), ^(void) {
    reject(@"error",@"Form data expected as body",nil);
    });
  }
  });
}




// Utility functions for minor tasks
-(BOOL)isNetworkAvailable{ // to check the network available, can use other methods here.
  Reachability* reach = [Reachability reachabilityWithHostname:@"www.google.com"];
  return [reach isReachable];
}
- (NSString *)generateBoundaryString { // to generate boundry string for multipart headers, basically random strings concat with specific format string
  return [NSString stringWithFormat:@"Boundary-%@", [[NSUUID UUID] UUIDString]];
}

- (NSString *)mimeTypeForPath:(NSString *)path { // to get the mime type of the provided url
  // get a mime type for an extension using MobileCoreServices.framework
  CFStringRef extension = (__bridge CFStringRef)[path pathExtension];
  CFStringRef UTI = UTTypeCreatePreferredIdentifierForTag(kUTTagClassFilenameExtension, extension, NULL);
  assert(UTI != NULL);
  NSString *mimetype = CFBridgingRelease(UTTypeCopyPreferredTagWithClass(UTI, kUTTagClassMIMEType));
  assert(mimetype != NULL);
  CFRelease(UTI);
  return mimetype;
}
@end
