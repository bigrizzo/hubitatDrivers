/*
 * Neo Smart Controller
 *
 * Calls URIs with HTTP GET for shade open/close/stop/favourite using the Neo Smart Controller
 * 
 * Based on the Hubitat community driver httpGetSwitch
 */
metadata {
    definition(name: "Neo Smart Controller", namespace: "bigrizzo", author: "bigrizz", importUrl: "https://github.com/bigrizzo/hubitatDrivers/upload/master/NeoSmart.groovy") {
        capability "Actuator"
        capability "doorControl"
        capability "Sensor"
		
		command "stop"
		command "favourite"
    }
}

preferences {
    section("URIs") {
        input "closeURI", "text", title: "Close URI", required: false
        input "openURI", "text", title: "Open URI", required: false
		input "stopURI", "text", title: "Stop URI", required: false
		input "faveURI", "text", title: "Favourite URI", required: false
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
    if (logEnable) log.debug "Sending close GET request to [${settings.closeURI}]"

    try {
        httpGet(settings.closeURI) { resp ->
            if (resp.success) {
                sendEvent(name: "doorControl", value: "close", isStateChange: true)
            }
            if (logEnable)
                if (resp.data) log.debug "${resp.data}"
        }
    } catch (Exception e) {
        log.warn "Call to close failed: ${e.message}"
    }
}

def open() {
    if (logEnable) log.debug "Sending open GET request to [${settings.openURI}]"

    try {
        httpGet(settings.openURI) { resp ->
            if (resp.success) {
                sendEvent(name: "doorControl", value: "open", isStateChange: true)
            }
            if (logEnable)
                if (resp.data) log.debug "${resp.data}"
        }
    } catch (Exception e) {
        log.warn "Call to open failed: ${e.message}"
    }
}

def stop() {
    if (logEnable) log.debug "Sending stop GET request to [${settings.stopURI}]"

    try {
        httpGet(settings.stopURI) { resp ->
            if (resp.success) {
                sendEvent(name: "shade", value: "stop", isStateChange: true)
            }
            if (logEnable)
                if (resp.data) log.debug "${resp.data}"
        }
    } catch (Exception e) {
        log.warn "Call to stop failed: ${e.message}"
    }
}

def favourite() {
    if (logEnable) log.debug "Sending favourite GET request to [${settings.faveURI}]"

    try {
        httpGet(settings.faveURI) { resp ->
            if (resp.success) {
                sendEvent(name: "shade", value: "favourite", isStateChange: true)
            }
            if (logEnable)
                if (resp.data) log.debug "${resp.data}"
        }
    } catch (Exception e) {
        log.warn "Call to favourite failed: ${e.message}"
    }
}