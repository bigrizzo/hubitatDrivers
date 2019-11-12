/*
 * Neo Smart Controller
 *
 * Calls URIs with HTTP GET for shade open/close/stop/favourite using the Neo Smart Controller
 * 
 * Based on the Hubitat community driver httpGetSwitch
 */
metadata {
    definition(name: "Neo Smart Controller", namespace: "bigrizzo", author: "bigrizz", importUrl: "https://raw.githubusercontent.com/bdwilson/hubitatDrivers/master/NeoSmart.groovy") {
        capability "WindowShade"
		capability "Switch"
		capability "Actuator"
	 	capability "ChangeLevel"   
		capability "Switch Level"
		
		command "stop"
		command "favorite"
		command "up"
		command "down"
    }
}

preferences {
    section("URIs") {
		input "blindCode", "text", title: "Blind or Room Code (from Neo App)", required: true
		input "controllerID", "text", title: "Controller ID (from Neo App)", required: true
		input "controllerIP", "text", title: "Controller IP Address (from Neo App)", required: true
        input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: true
    }
}

def date() {
	def date = new Date().getTime().toString().drop(6)
    if (logEnable) log.debug "Using ${date}"
	return date
}

def get(url,state) {
   try {
        httpGet(url) { resp ->
            if (resp.success) {
                sendEvent(name: "windowShade", value: "${state}", isStateChange: true)
            }
            if (logEnable)
                if (resp.data) log.debug "${resp.data}"
        }
    } catch (Exception e) {
        log.warn "Call to ${url} failed: ${e.message}"
    }
}

def logsOff() {
    log.warn "debug logging disabled..."
    device.updateSetting("logEnable", [value: "false", type: "bool"])
}

def installed() {
    log.info "installed..."
	if (!controllerID || !controllerIP || !blindCode) {
		log.error "Please make sure controller ID, IP and blind/room codes are configured." 
	}
    log.warn "debug logging is: ${logEnable == true}"
    if (logEnable) runIn(1800, logsOff)
}

def updated() {
    log.info "updated..."
	if (!controllerID || !controllerIP || !blindCode) {
		log.error "Please make sure controller ID, IP and blind/room codes are configured." 
	}
    log.warn "debug logging is: ${logEnable == true}"
    if (logEnable) runIn(1800, logsOff)
}

def parse(String description) {
    if (logEnable) log.debug(description)
}

def up() {
	startLevelChange("up")
}

def down() {
	startLevelChange("down")
}
	
def on() {
	open() 
}

def off() {
	close()
}

def close() {
    url = "http://" + controllerIP + ":8838/neo/v1/transmit?command=" + blindCode + "-dn&id=" + controllerID + "&hash=" + date()
    if (logEnable) log.debug "Sending close GET request to ${url}"
	get(url,"closed")
	state.level=0
}

def open() {
    url = "http://" + controllerIP + ":8838/neo/v1/transmit?command=" + blindCode + "-up&id=" + controllerID + "&hash=" + date()
    if (logEnable) log.debug "Sending open GET request to ${url}"
	get(url,"open")
	state.level=100
}

def stop() {
    url = "http://" + controllerIP + ":8838/neo/v1/transmit?command=" + blindCode + "-sp&id=" + controllerID + "&hash=" + date()
    if (logEnable) log.debug "Sending stop GET request to ${url}"
	get(url,"partially open")
}

def favorite() {
    url = "http://" + controllerIP + ":8838/neo/v1/transmit?command=" + blindCode + "-gp&id=" + controllerID + "&hash=" + date()
    if (logEnable) log.debug "Sending favorite GET request to ${url}"
	get(url,"partially open")
}

def setPosition(position) {
	/* what would be ideal is if we knew the position of blind at any time, and
    could then use setPosition to do the micro-step up/down multiple times for
    blinds that don't support going to specific positions */
	if (position >= 100) {
		position = 99
	}
	if (position < 10) {
    	url = "http://" + controllerIP + ":8838/neo/v1/transmit?command=" + blindCode + "-0" + position + "&id=" + controllerID + "&hash=" + date()
	} else {
    	url = "http://" + controllerIP + ":8838/neo/v1/transmit?command=" + blindCode + "-" + position + "&id=" + controllerID + "&hash=" + date()
	}
    if (logEnable) log.debug "Sending position ${position} GET request to ${url}"
	get(url,"partialy open")
}

def startLevelChange(direction) {
	/* https://github.com/hubitat/HubitatPublic/blob/master/examples/drivers/genericComponentDimmer.groovy */
    if (direction == "up") {
        url = "http://" + controllerIP + ":8838/neo/v1/transmit?command=" + blindCode + "-mu" + position + "&id=" + controllerID + "&hash=" + date()
    } else {
        url = "http://" + controllerIP + ":8838/neo/v1/transmit?command=" + blindCode + "-md" + position + "&id=" + controllerID + "&hash=" + date()
    }
    if (logEnable) log.debug "Sending startLevel Change ${direction} GET request to ${url}"
    get(url,"partially open")
}

def setLevel(level) {
	if ((level <= 100) && (level >= 0)) {
		if (level < state.level) {
			for (def i=state.level; i>=level; i--) {
				url = "http://" + controllerIP + ":8838/neo/v1/transmit?command=" + blindCode + "-mu" + position + "&id=" + controllerID + "&hash=" + date()
				get(url,"partially open")
				if (logEnable) log.debug "Sending micro up burst ${i} >= ${level}"
				pauseExecution(500)
			}
		} else {
			for (def і=state.level; i<=level; i++) {
				url = "http://" + controllerIP + ":8838/neo/v1/transmit?command=" + blindCode + "-md" + position + "&id=" + controllerID + "&hash=" + date()
				get(url,"partially open")
				if (logEnable) log.debug "Sending micro down burst ${i} <= ${level}"
				pauseExecution(500)
			}
		}
		state.level=level
	}
}

