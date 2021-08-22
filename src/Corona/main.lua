local huaweiAuthService = require "plugin.huaweiAuthService"
local widget = require( "widget" )
local json = require("json")

local function listener(event)
    print(event)
end

huaweiAuthService.init(listener)

local signInAnonymously = widget.newButton(
    {
        left = 55,
        top = 30,
        id = "signInAnonymously",
        label = "signInAnonymously",
        onPress = function(event)
            huaweiAuthService.signInAnonymously(function(event) 
                print(json.prettify( event ))
            end)
        end,
        width = 210,
        height = 30
    }
)

local signOut = widget.newButton(
    {
        left = 55,
        top = 60,
        id = "signOut",
        label = "signOut",
        onPress = function(event)
            huaweiAuthService.signOut()
        end,
        width = 210,
        height = 30
    }
)

local deleteUser = widget.newButton(
    {
        left = 55,
        top = 90,
        id = "deleteUser",
        label = "deleteUser",
        onPress = function(event)
            huaweiAuthService.deleteUser()
        end,
        width = 210,
        height = 30
    }
)

local addTokenListener = widget.newButton(
    {
        left = 55,
        top = 120,
        id = "addTokenListener",
        label = "addTokenListener",
        onPress = function(event)
            huaweiAuthService.addTokenListener()
        end,
        width = 210,
        height = 30
    }
)


local removeTokenListener = widget.newButton(
    {
        left = 55,
        top = 150,
        id = "removeTokenListener",
        label = "removeTokenListener",
        onPress = function(event)
            huaweiAuthService.removeTokenListener()
        end,
        width = 210,
        height = 30
    }
)
