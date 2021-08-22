# Huawei Auth Service Solar2d Plugin

This plugin was created based on Huawei Auth Service. Please [Auth Service](https://developer.huawei.com/consumer/en/agconnect/auth-service/) for detailed information. 

In order to use this plugin, you must first create an account from developer.huawei.com. And after logging in with your account, create a project in the huawei console in order to use HMS kits.

## Project Setup

To use the plugin please add following to `build.settings`

```lua
{
    plugins = {
        ["plugin.huaweiAuthService"] = {
            publisherId = "com.solar2d",
        },
    },
}
```

And then you have to create keystore for your app. And you must generate sha-256 bit fingerprint from this keystore using the command here. 
```cmd
keytool -v -list -keystore name.keystore
```
And then You have to define this fingerprint to your project on the huawei console.

And you must add the keystore you created while building your project.
Also you need to pass the package-name of the project you created on Huawei Console to your project.
And also you need to put `agconnect-services.json` file into `main.lua` directory.

After all the configuration processes, you must define the plugin in main.lua.

```lua
local huaweiAuthService = require "plugin.huaweiAuthService"

local function listener(event)
    print(event)
end

huaweiAuthService.init(listener) -- sets listener 
```

## Methods in the Plugin
# Auth Service
### signIn
Signs in a user to AppGallery Connect through third-party authentication.

```lua
-- w/ MobileNumber
huaweiAuthService.signIn("MobileNumber", {countryCode="", phoneNumber="", password="", verifyCode (optional)=""}, function(event) 
    print(json.prettify( event ))
end)

-- w/ EmailAdress
huaweiAuthService.signIn("EmailAdress", {email="", password="", verifyCode (optional) = ""}, function(event) 
    print(json.prettify( event ))
end)

-- w/ HuaweiID
huaweiAuthService.signIn("HuaweiID", {token="", autoCreateUser (optional)=""}, function(event) 
    print(json.prettify( event ))
end)

-- w/ WeChat
huaweiAuthService.signIn("WeChat", {token="", openId="", autoCreateUser (optional)=""}, function(event) 
    print(json.prettify( event ))
end)

-- w/ Facebook
huaweiAuthService.signIn("Facebook", {token="", autoCreateUser (optional)=""}, function(event) 
    print(json.prettify( event ))
end)

-- w/ Twitter
huaweiAuthService.signIn("Twitter", {token="", secret="", autoCreateUser (optional)=""}, function(event) 
    print(json.prettify( event ))
end)

-- w/ Weibo
huaweiAuthService.signIn("Weibo", {token="", openId="", autoCreateUser (optional)=""}, function(event) 
    print(json.prettify( event ))
end)

-- w/ QQ
huaweiAuthService.signIn("QQ", {token="", openId="", autoCreateUser (optional)=""}, function(event) 
    print(json.prettify( event ))
end)

-- w/ Google
huaweiAuthService.signIn("Google", {openId="", autoCreateUser (optional)=""}, function(event) 
    print(json.prettify( event ))
end)

-- w/ GooglePlay
huaweiAuthService.signIn("GooglePlay", {serverAuthCode="", autoCreateUser (optional)=""}, function(event) 
    print(json.prettify( event ))
end)

-- w/ SelfBuild
huaweiAuthService.signIn("SelfBuild", {token="", autoCreateUser (optional)=""}, function(event) 
    print(json.prettify( event ))
end)

    
--Result 
--[[Table {
      isError = true|false
      message = text
      type = signIn (text)
      provider = AuthService (text)
      data = AGConnectUser(json)
} 
]]--
```

### getSupportedAuthList
Obtains the list of supported authentication modes. This API needs to be used with the third-party SDK for unified sign-in or packaging in aggregation services.

```lua
    val = huaweiAuthService.getSupportedAuthList()
    -- Result (Integer Array (json))
```

### signInAnonymously
Signs in a user anonymously.

```lua 
huaweiAuthService.signInAnonymously(function(event) 
    print(json.prettify( event ))
end)

--Result 
--[[Table {
      isError = true|false
      message = text
      type = signInAnonymously (text)
      provider = AuthService (text)
      data = AGConnectUser(json)
} 
]]--
```

### deleteUser
Deletes the current user information and cache information from the AppGallery Connect server.

```lua 
huaweiAuthService.deleteUser()
```
### signOut
Signs out a user and deletes the user's cached data.

```lua 
huaweiAuthService.signOut()
```

### getCurrentUser
Obtains information about the current signed-in user. If the user has not signed in, a null value is returned.

```lua 
val = huaweiAuthService.getCurrentUser()
    
--Result 
--[[Table {
      isError = true|false
      message = text
      type = getCurrentUser (text)
      provider = AuthService (text)
      data = AGConnectUser(json)
    } 
]]--
```
### addTokenListener
Adds a token change listener. Multiple listening objects can be added at the same time. The listening callback function is called by the UI thread.

```lua 
val = huaweiAuthService.addTokenListener()
    
--Result (Communication is established through the listening method you specified in the init method.) 
--[[Table {
      isError = true|false
      message = token
      type = addTokenListener (text)
      provider = AuthService (text)
    } 
]]--
```

### removeTokenListener
Removes the token change listener.

```lua 
val = huaweiAuthService.removeTokenListener()
```
### createUser
Creates an account using an email address or mobile number.

```lua
huaweiAuthService.createUser("MobileNumber", {countryCode="", phoneNumber="", password(optional)="", verifyCode =""}, function(event) 
    print(json.prettify( event ))
end)


huaweiAuthService.createUser("EmailAdress", {email="", password=(optional)"", verifyCode=""}, function(event) 
    print(json.prettify( event ))
end)

--Result 
--[[Table {
      isError = true|false
      message = text
      type = createUser (text)
      provider = AuthService (text)
      data = AGConnectUser(json)
} 
]]--
```
### requestVerifyCode
Applies for a verification code using an email address or mobile number.

```lua
huaweiAuthService.requestVerifyCode("MobileNumber", ACTION_REGISTER_LOGIN, 60, {countryCode="", phoneNumber=""}, function(event) 
    print(json.prettify( event ))
end)


huaweiAuthService.requestVerifyCode("EmailAdress", ACTION_RESET_PASSWORD, 60, {email=""}, function(event) 
    print(json.prettify( event ))
end)

--Result 
--[[Table {
      isError = true|false
      message = text
      type = requestVerifyCode (text)
      provider = AuthService (text)
} 
]]--

```
### updatePhone
Updates the mobile number of the current user.

```lua
huaweiAuthService.updatePhone({countryCode="", phoneNumber="", verifyCode=""}) 

--Result 
--[[Table {
      isError = true|false
      message = text
      type = updatePhone (text)
      provider = AuthService (text)
} 
]]--
```
### resetPassword
Resets a user's password using the email address or mobile number.

```lua
huaweiAuthService.resetPassword("MobileNumber", {newPassword="", countryCode="", phoneNumber="", verifyCode=""}, function(event) 
    print(json.prettify( event ))
end)

huaweiAuthService.resetPassword("EmailAdress", {newPassword="", email="", verifyCode=""}, function(event) 
    print(json.prettify( event ))
end)

--Result 
--[[Table {
      isError = true|false
      message = text
      type = resetPassword (text)
      provider = AuthService (text)
} 
]]--
```
### link
Links a new authentication mode for the current user.

```lua
huaweiAuthService.link("MobileNumber", {countryCode="", phoneNumber="", password="", verifyCode(optional)=""}, function(event) 
    print(json.prettify( event ))
end)


huaweiAuthService.link("EmailAdress", {email="", password="", verifyCode(optional)=""}, function(event) 
    print(json.prettify( event ))
end)


huaweiAuthService.link("HuaweiID", {token="", autoCreateUser (optional)=""}, function(event) 
    print(json.prettify( event ))
end)


huaweiAuthService.link("WeChat", {token="", openId="", autoCreateUser (optional)=""}, function(event) 
    print(json.prettify( event ))
end)


huaweiAuthService.link("Facebook", {token="", autoCreateUser (optional)=""}, function(event) 
    print(json.prettify( event ))
end)


huaweiAuthService.link("Twitter", {token="", secret="", autoCreateUser (optional)=""}, function(event) 
    print(json.prettify( event ))
end)


huaweiAuthService.link("Weibo", {token="", openId="", autoCreateUser (optional)=""}, function(event) 
    print(json.prettify( event ))
end)


huaweiAuthService.link("QQ", {token="", openId="", autoCreateUser (optional)=""}, function(event) 
    print(json.prettify( event ))
end)

huaweiAuthService.link("Google", {openId="", autoCreateUser (optional)=""}, function(event) 
    print(json.prettify( event ))
end)

huaweiAuthService.link("GooglePlay", {serverAuthCode="", autoCreateUser (optional)=""}, function(event) 
    print(json.prettify( event ))
end)

huaweiAuthService.link("SelfBuild", {token="", autoCreateUser (optional)=""}, function(event) 
    print(json.prettify( event ))
end)


--Result 
--[[Table {
      isError = true|false
      message = text
      type = link (text)
      provider = AuthService (text)
      data = AGConnectUser(json)
} 
]]--
```
### unlink
Unlinks the current user from the linked authentication mode.

```lua
huaweiAuthService.unlink("MobileNumber", function(event) 
    print(json.prettify( event ))
end)

huaweiAuthService.unlink("EmailAdress", function(event) 
    print(json.prettify( event ))
end)

huaweiAuthService.unlink("HuaweiID", function(event) 
    print(json.prettify( event ))
end)

huaweiAuthService.unlink("WeChat", function(event) 
    print(json.prettify( event ))
end)

huaweiAuthService.unlink("Facebook", function(event) 
    print(json.prettify( event ))
end)

huaweiAuthService.unlink("Twitter", function(event) 
    print(json.prettify( event ))
end)

huaweiAuthService.unlink("Weibo", function(event) 
    print(json.prettify( event ))
end)

huaweiAuthService.unlink("QQ", function(event) 
    print(json.prettify( event ))
end)

huaweiAuthService.unlink("Google", function(event) 
    print(json.prettify( event ))
end)

huaweiAuthService.unlink("GooglePlay", function(event) 
    print(json.prettify( event ))
end)

huaweiAuthService.unlink("SelfBuild", function(event) 
    print(json.prettify( event ))
end)

huaweiAuthService.unlink("Anonymous", function(event) 
    print(json.prettify( event ))
end)

--Result 
--[[Table {
      isError = true|false
      message = text
      type = unlink (text)
      provider = AuthService (text)
} 
]]--
```
### reauthenticate
Uses AGConnectAuthCredential to reauthenticate user accounts.

```lua 
huaweiAuthService.reauthenticate("MobileNumber", {countryCode="", phoneNumber="", password="", verifyCode(optional)=""}, function(event) 
    print(json.prettify( event ))
end)


huaweiAuthService.reauthenticate("EmailAdress", {email="", password="", verifyCode(optional)=""}, function(event) 
    print(json.prettify( event ))
end)


huaweiAuthService.reauthenticate("HuaweiID", {token="", autoCreateUser (optional)=""}, function(event) 
    print(json.prettify( event ))
end)


huaweiAuthService.reauthenticate("WeChat", {token="", openId="", autoCreateUser (optional)=""}, function(event) 
    print(json.prettify( event ))
end)


huaweiAuthService.reauthenticate("Facebook", {token="", autoCreateUser (optional)=""}, function(event) 
    print(json.prettify( event ))
end)


huaweiAuthService.reauthenticate("Twitter", {token="", secret="", autoCreateUser (optional)=""}, function(event) 
    print(json.prettify( event ))
end)


huaweiAuthService.reauthenticate("Weibo", {token="", openId="", autoCreateUser (optional)=""}, function(event) 
    print(json.prettify( event ))
end)


huaweiAuthService.reauthenticate("QQ", {token="", openId="", autoCreateUser (optional)=""}, function(event) 
    print(json.prettify( event ))
end)

huaweiAuthService.reauthenticate("Google", {openId="", autoCreateUser (optional)=""}, function(event) 
    print(json.prettify( event ))
end)

huaweiAuthService.reauthenticate("GooglePlay", {serverAuthCode="", autoCreateUser (optional)=""}, function(event) 
    print(json.prettify( event ))
end)

huaweiAuthService.reauthenticate("SelfBuild", {token="", autoCreateUser (optional)=""}, function(event) 
    print(json.prettify( event ))
end)

--Result 
--[[Table {
      isError = true|false
      message = text
      type = reauthenticate (text)
      provider = AuthService (text)
      data = AGConnectUser(json)
} 
]]--
```


## References
HMS Auth Service [Check](https://developer.huawei.com/consumer/en/agconnect/auth-service/)

## License
MIT