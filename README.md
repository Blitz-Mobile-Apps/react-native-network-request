
# react-native-network-request

## Getting started

`$ npm install react-native-network-request --save`

### Mostly automatic installation

`$ react-native link react-native-network-request`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-network-request` and add `RNNetworkRequest.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNNetworkRequest.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactlibrary.RNNetworkRequestPackage;` to the imports at the top of the file
  - Add `new RNNetworkRequestPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-network-request'
  	project(':react-native-network-request').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-network-request/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-network-request')
  	```

#### Windows
[Read it! :D](https://github.com/ReactWindows/react-native)

1. In Visual Studio add the `RNNetworkRequest.sln` in `node_modules/react-native-network-request/windows/RNNetworkRequest.sln` folder to their solution, reference from their app.
2. Open up your `MainPage.cs` app
  - Add `using Network.Request.RNNetworkRequest;` to the usings at the top of the file
  - Add `new RNNetworkRequestPackage()` to the `List<IReactPackage>` returned by the `Packages` method


## Usage
```javascript
import RNNetworkRequest from 'react-native-network-request';

// TODO: What to do with the module?
RNNetworkRequest;
```
  