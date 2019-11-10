/*
 * Neo Smart Controller
 *
 * Calls URIs with HTTP GET for shade open/close/stop/favourite using the Neo Smart Controller
 * 
 * Based on the Hubitat community driver httpGetSwitch
 */
metadata {
    definition(name: "Neo Smart Controller", namespace: "bigrizzo", author: "bigrizz", importUrl: "https://github.com/bigrizzo/hubitatDrivers/blob/master/NeoSmart.groovy") {
        capability "WindowShade"
		
		command "stop"
		command "favorite"
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

def logsOff() {
    log.warn "debug logging disabled..."
    device.updateSetting("logEnable", [value: "false", type: "bool"])
}

def updated() {
    log.info "updated..."
    log.warn "debug logging is: ${logEnable == true}"
    if (logEnable) runIn(1800, logsOff)
}

def parse(String description) {
    if (logEnable) log.debug(description)
}

def close() {
    def dateTime = new Date()     
    def currentTimeEpoch = dateTime.getTime()
    url = "http://" + controllerIP + "/neo/v1/transmit?command=" + blindCode + "-dn&id=" + controllerID + "&hash=" + currentTimeEpoch
    if (logEnable) log.debug "Sending close GET request to ${url}"

    try {
        httpGet(url) { resp ->
            if (resp.success) {
                sendEvent(name: "windowShade", value: "close", isStateChange: true)
            }
            if (logEnable)
                if (resp.data) log.debug "${resp.data}"
        }
    } catch (Exception e) {
        log.warn "Call to close failed: ${e.message}"
    }
}

def open() {
    def dateTime = new Date()
    def currentTimeEpoch = dateTime.getTime()
    url = "http://" + controllerIP + "/neo/v1/transmit?command=" + blindCode + "-up&id=" + controllerID + "&hash=" + currentTimeEpoch
    if (logEnable) log.debug "Sending open GET request to ${url}"

    try {
        httpGet(url) { resp ->
            if (resp.success) {
                sendEvent(name: "windowShade", value: "open", isStateChange: true)
            }
            if (logEnable)
                if (resp.data) log.debug "${resp.data}"
        }
    } catch (Exception e) {
        log.warn "Call to open failed: ${e.message}"
    }
}

def stop() {
    def dateTime = new Date()
    def currentTimeEpoch = dateTime.getTime()
    url = "http://" + controllerIP + "/neo/v1/transmit?command=" + blindCode + "-sp&id=" + controllerID + "&hash=" + currentTimeEpoch
    if (logEnable) log.debug "Sending stop GET request to ${url}"

    try {
        httpGet(url) { resp ->
            if (resp.success) {
                sendEvent(name: "windowShade", value: "partially open", isStateChange: true)
            }
            if (logEnable)
                if (resp.data) log.debug "${resp.data}"
        }
    } catch (Exception e) {
        log.warn "Call to stop failed: ${e.message}"
    }
}

def favorite() {
    def dateTime = new Date()
    def currentTimeEpoch = dateTime.getTime()
    url = "http://" + controllerIP + "/neo/v1/transmit?command=" + blindCode + "-gp&id=" + controllerID + "&hash=" + currentTimeEpoch
    if (logEnable) log.debug "Sending favorite GET request to ${url}"

    try {
        httpGet(url) { resp ->
            if (resp.success) {
                sendEvent(name: "windowShade", value: "partially open", isStateChange: true)
            }
            if (logEnable)
                if (resp.data) log.debug "${resp.data}"
        }
    } catch (Exception e) {
        log.warn "Call to favourite failed: ${e.message}"
    }
}

def setPosition(position) {
    def dateTime = new Date()
    def currentTimeEpoch = dateTime.getTime()
    url = "http://" + controllerIP + "/neo/v1/transmit?command=" + blindCode + "-" + position + "&id=" + controllerID + "&hash=" + currentTimeEpoch
    if (logEnable) log.debug "Sending position ${position} GET request to ${url}"

    try {
        httpGet(url) { resp ->
            if (resp.success) {
                sendEvent(name: "windowShade", value: "partially open", isStateChange: true)
            }
            if (logEnable)
                if (resp.data) log.debug "${resp.data}"
        }
    } catch (Exception e) {
        log.warn "Call to favourite failed: ${e.message}"
    }
}
