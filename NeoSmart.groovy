/*
 * Neo Smart Controller
 *
 * Calls URIs with HTTP GET for shade open/close/stop/favourite using the Neo Smart Controller
 * 
 * Assumption: The blinds (or room of blinds) you're controlling all take the 
 * same amount of time to open/close (some may be a little faster/slower than
 * the others, but they should all generally take the same amount of time to
 * otherwise, create another virtual driver for ones that are slower/faster). 
 * 
 * To use: 
 * 1) Run a stopwatch to see how long it takes for the blinds to close (in
 * seconds)
 * 2) Open the blinds completely and time how long it takes to get to your
 * favorite setting) 
 * 
 * Input these values in the configuration, rounding down. 
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
        input "timeToClose", "number", title: "Time in seconds it takes to close the blinds completely", required: true
        input "timeToFav", "number", title: "Time in seconds it takes to reach your favorite setting when closing the blinds", required: true
        input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: true
    }
}

def date() {
	def date = new Date().getTime().toString().drop(6)
    //if (logEnable) log.debug "Using ${date}"
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
	if (!controllerID || !controllerIP || !blindCode || !timeToClose || !timeToFav) {
		log.error "Please make sure controller ID, IP, blind/room code, time to close and time to favorite are configured." 
	}
    log.warn "debug logging is: ${logEnable == true}"
    if (logEnable) runIn(1800, logsOff)
}

def updated() {
    log.info "updated..."
	if (!controllerID || !controllerIP || !blindCode || !timeToClose || !timeToFav) {
		log.error "Please make sure controller ID, IP, blind/room code, time to close and time to favorite are configured." 
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
	state.level=timeToClose
}

def open() {
    url = "http://" + controllerIP + ":8838/neo/v1/transmit?command=" + blindCode + "-up&id=" + controllerID + "&hash=" + date()
    if (logEnable) log.debug "Sending open GET request to ${url}"
	get(url,"open")
	state.level=0
}

def stop() {
    url = "http://" + controllerIP + ":8838/neo/v1/transmit?command=" + blindCode + "-sp&id=" + controllerID + "&hash=" + date()
    if (logEnable) log.debug "Sending stop GET request to ${url}"
	get(url,"partially open")
}

def favorite() {
    url = "http://" + controllerIP + ":8838/neo/v1/transmit?command=" + blindCode + "-gp&id=" + controllerID + "&hash=" + date()
    if (logEnable) log.debug "Sending favorite GET request to ${url}"
    state.level=timeToFav
	get(url,"partially open")
}

def setPosition(position) { // timeToClose= closed/down, 0=open/up
	if (position >= timeToClose) {
		position = timeToClose
	}
    if (position != state.level) {
            if (position < state.level) { // requested location is more open than current. 
                if (position == 0) {
                    open()
                    state.level=0
                } else {
                    def pos = state.level - position
                    open()
                    pauseExecution(pos.toInteger()*1000)
                    stop()
                    state.level=position
                }
            } else {  // location is more closed than current
                if (position == timeToClose) {
                    close()
                    state.level=timeToClose
                } else {
                    pos = position - state.level
                    close()
                    pauseExecution(pos.toInteger()*1000)
                    stop()
                    state.level=position
                }
            }
      }
}

def startLevelChange(direction) {
	// https://github.com/hubitat/HubitatPublic/blob/master/examples/drivers/genericComponentDimmer.groovy 
    if (direction == "up") {
        url = "http://" + controllerIP + ":8838/neo/v1/transmit?command=" + blindCode + "-mu&id=" + controllerID + "&hash=" + date()
    } else {
        url = "http://" + controllerIP + ":8838/neo/v1/transmit?command=" + blindCode + "-md&id=" + controllerID + "&hash=" + date()
    }
    if (logEnable) log.debug "Sending startLevel Change ${direction} GET request to ${url}"
    get(url,"partially open")
}

def setLevel(level) {
    setPosition(level)
}
