using ReactNative.Bridge;
using System;
using System.Collections.Generic;
using Windows.ApplicationModel.Core;
using Windows.UI.Core;

namespace Network.Request.RNNetworkRequest
{
    /// <summary>
    /// A module that allows JS to share data.
    /// </summary>
    class RNNetworkRequestModule : NativeModuleBase
    {
        /// <summary>
        /// Instantiates the <see cref="RNNetworkRequestModule"/>.
        /// </summary>
        internal RNNetworkRequestModule()
        {

        }

        /// <summary>
        /// The name of the native module.
        /// </summary>
        public override string Name
        {
            get
            {
                return "RNNetworkRequest";
            }
        }
    }
}
