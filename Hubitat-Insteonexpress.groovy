/*
    HTTP Insteon Switch/Dimmer
      Original code base comes from HTTP Momentary Switch by @ogiewon (Dan Ogorchock)

      Modified by @cwwilson08 (Chris Wilson) to work with the Insteonlocal driver for Homebridge by Scott Kuester

    *  Last Update 09/17/2018
    *
    *  V1.3.1 - Removed ability to set refresh in driver    @cwwilson 10/06/18
    *  V1.3.0 - User definable refresh and smooth dimming    @cwwilson 09/17/2018
    *  V1.2.0 - Added dim level capability    @cwwilson08 09/01/2018 
    *  V1.1.0 - Changed method to enforce GET    @cwwilson08 08/28/2018
    *  V1.0.0 - First build    @cwwilson08 08/27/2018
    */
    metadata {
    	definition (name: "HTTP Insteon Switch/Dimmer", namespace: "cw", author: "cwwilson08") {
        capability "Switch"
        capability "Refresh"
        capability "Switch Level"
    	}
        
     
        
        preferences {
    		input(name: "deviceIP", type: "string", title:"Express Device IP Address", description: "Enter IP Address of your Express server", required: true, displayDuringSetup: true)
    		input(name: "devicePort", type: "string", title:"Express Device Port", description: "Enter Port of your Express server (defaults to 80)", defaultValue: "80", required: false, displayDuringSetup: true)
    		input(name: "hubIP", type: "string", title:"HUB/PLM IP Address", description: "Enter IP Address of your HUB/PLM", required: true, displayDuringSetup: true)
    		input(name: "hubPort", type: "string", title:"HUB/PLM Device Port", description: "Enter Port of your HUB/PLM (defaults to 80)", defaultValue: "80", required: false, displayDuringSetup: true)
            input(name: "deviceID", type: "string", title:"Device ID", description: "Device ID here", displayDuringSetup: true)
            input("username", "text", title: "Username", description: "The hub username (found in app)")
            input("password", "text", title: "Password", description: "The hub password (found in app)")
           
    	}
    }
    def installed() {
    	updated()
    }

    def updated() {
    }


   
    def parse(String description) {
    def resp = parseLanMessage(description)
    	if (resp.json == null){
            log.debug "resp.json is null"
            log.debug "resp = ${resp}"
    		}else{
    	def cmdResponse = resp.json.level
    	if (cmdResponse == 0){
         sendEvent(name: "switch", value: "off", isStateChange: true)
         sendEvent(name: "level", value: "${resp.json.level}", isStateChange: true)
    	}else{
         sendEvent(name: "switch", value: "on", isStateChange: true)
         sendEvent(name: "level", value: "${resp.json.level}", isStateChange: true)
    		}
    	}
    }

    def refresh(){
    	devicePath = "/light/" + "${deviceID}" + "/status"
        log.debug "deviceid at refresh is {deviceID}"
    runCmd(devicePath, deviceMethod)
    }

    def on() {
        
    	sendEvent(name: "switch", value: "on", isStateChange: true)
       		devicePath = "/light/" + "${deviceID}" + "/on"
            runCmd(devicePath, deviceMethod)
        
    }

    def off() {
    	sendEvent(name: "switch", value: "off", isStateChange: true)	
    		devicePath = "/light/" + "${deviceID}" + "/off"
            runCmd(devicePath, deviceMethod)
      
    }

    def setLevel(value) {

        // log.debug "setLevel >> value: $value"
        
        // Max is 255
        def percent = value / 100
        def realval = percent * 255
        def valueaux = realval as Integer
        def level = Math.max(Math.min(valueaux, 255), 0)
        if (level > 0) {
            sendEvent(name: "switch", value: "on")
        } else {
            sendEvent(name: "switch", value: "off")
        }
        // log.debug "dimming value is $valueaux"
        log.debug "dimming to $level"
        dim(level,value)
    }

    def setLevel(value,rate) {

        // log.debug "setLevel >> value: $value"
        
        // Max is 255
        def percent = value / 100
        def realval = percent * 255
        def valueaux = realval as Integer
        def level = Math.max(Math.min(valueaux, 255), 0)
        if (level > 0) {
            sendEvent(name: "switch", value: "on")
        } else {
            sendEvent(name: "switch", value: "off")
        }
        // log.debug "dimming value is $valueaux"
        log.debug "dimming to $level"
        dim(level,value)
    }

    def dim(level, real) {
        String hexlevel = level.toString().format( '%02x', level.toInteger() )
        // log.debug "Dimming to hex $hexlevel"
        sendCmd("11",hexlevel)
        sendEvent(name: "level", value: real, unit: "%")
    }



    def runCmd(String varCommand, String method) {
    	def localDevicePort = (devicePort==null) ? "3000" : devicePort
    	def path = varCommand 
    	def body = "" 
    	def headers = [:] 
        headers.put("HOST", "${deviceIP}:${localDevicePort}")
    	headers.put("Content-Type", "application/x-www-form-urlencoded")

    	try {
    		def hubAction = new hubitat.device.HubAction(
    			method: "GET",
    			path: path,
    			body: body,
    			headers: headers
    			)
    		log.debug hubAction
    		return hubAction
    	}
    	catch (Exception e) {
        log.debug "runCmd hit exception ${e} on ${hubAction}"
    	}  
    }

    def sendCmd(num, level)
    {
      

        // Will re-test this later
        // sendHubCommand(new physicalgraph.device.HubAction("""GET /3?0262${settings.deviceid}0F${num}${level}=I=3 HTTP/1.1\r\nHOST: IP:PORT\r\nAuthorization: Basic B64STRING\r\n\r\n""", physicalgraph.device.Protocol.LAN, "${deviceNetworkId}"))
        httpGet("http://${username}:${password}@${hubIP}:${hubPort}//3?0262${deviceID}0F${num}${level}=I=3") {response -> 
            def content = response
            
            // log.debug content
        }
        log.debug "Command Completed"
    }
