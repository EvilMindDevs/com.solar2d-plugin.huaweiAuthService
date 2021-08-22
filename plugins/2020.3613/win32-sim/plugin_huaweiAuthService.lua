local Library = require "CoronaLibrary"

local lib = Library:new{ name='plugin.huaweiAuthService', publisherId='com.solar2d' }

local placeholder = function()
	print( "WARNING: The '" .. lib.name .. "' library is not available on this platform." )
end

lib.init = placeholder
lib.signIn = placeholder
lib.getSupportedAuthList = placeholder
lib.signInAnonymously = placeholder
lib.deleteUser = placeholder
lib.signOut = placeholder
lib.getCurrentUser = placeholder
lib.addTokenListener = placeholder
lib.removeTokenListener = placeholder
lib.createUser = placeholder
lib.requestVerifyCode = placeholder
lib.updatePhone = placeholder
lib.resetPassword = placeholder
lib.link = placeholder
lib.unlink = placeholder
lib.reauthenticate = placeholder


-- Return an instance
return lib