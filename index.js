import { NativeModules } from "react-native";

const fetch = async (url, config) => {
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
    const res = await RNNativeRequest.fetch(url, configToSend);
    return Promise.resolve(res);
  } catch (e) {
    return Promise.reject(e);
  }
};
export const NativeRequest = {
  fetch: fetch,
};
export default NativeRequest;
