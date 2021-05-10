import { NativeModules } from "react-native";

const responseFunction = async (res) => {

  if (Platform.OS == "android") {
    try {

      res = JSON.parse(res)
      // console.log(res);
      if (res.data) {
        res.data = JSON.parse(res.data)
      }

    } catch (error) {
      console.log("JSON parse error", error);
    }
  }
  // Promise.resolve(res)
  res = {
    ...res,
    json: () => {
      return Promise.resolve(res.data)
    }
  }
  return res
}

export const fetch = async (url, config) => {
  const { RNNetworkRequest } = NativeModules;
  var configToSend = {};
  if (!url) {
    return Promise.reject("Expected a url as first argument");
  }
  if (!config) {
    configToSend["method"] = "GET";
  }
  if (config["Authorization"]) {
    configToSend["Authorization"] = config.Authorization;
  }
  if (config.body) {
    configToSend["body"] = config.body;
  }
  if (config.headers) {
    configToSend["headers"] = config.headers;
  }
  if (config.method) {
    configToSend["method"] = config.method;
  }
  try {
    var res = await RNNetworkRequest.fetch(url, configToSend);

    return Promise.resolve(responseFunction(res));
    // return Promise.resolve(res);
  } catch (e) {
    return Promise.reject(e);
  }
};

export const NativeRequest = {
  fetch: fetch,
};
export default NativeRequest;
