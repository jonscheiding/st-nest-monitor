/**
 *  Nest Status Monitor
 *
 *  Copyright 2016 Jon Scheiding
 *
 */
definition(
    name: "Nest Status Monitor",
    namespace: "jonscheiding",
    author: "Jon Scheiding",
    description: "Nest Status Monitor",
    category: "",
    iconUrl: "http://cdn.device-icons.smartthings.com/Home/home1-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Home/home1-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Home/home1-icn@2x.png")


preferences {
	section("Title") {
		input "thermostat", "capability.thermostatOperatingState", title: "Thermostat", required: true
	}
}

def onTemperatureChange(evt) {
    log.info "Current temperature is ${thermostat.currentTemperature}."
    
    def warn = false
    switch(thermostat.currentThermostatOperatingState) {
    	case "heating":
        	warn = (thermostat.currentTemperature < state.lastTemperature)
        	break
    	case "cooling":
        	warn = (thermostat.currentTemperature > state.lastTemperature)
            break
    }
    
    if(warn) {
    	def message = 
        	"Thermostat is ${thermostat.currentThermostatOperatingState}, " +
            "but the temperature has changed " + 
            "from ${state.lastTemperature} " +
            "to ${thermostat.currentTemperature}."
            
        log.warn message
        sendPush message
    }
    
    state.lastTemperature = thermostat.currentTemperature

}

def onOperatingStateChange(evt) {
    log.info "Operating state changed to ${thermostat.currentThermostatOperatingState}."
    state.operatingState = thermostat.currentThermostatOperatingState
    state.operatingStateSince = now()
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(thermostat, 'temperature', onTemperatureChange)
    subscribe(thermostat, 'thermostatOperatingState', onOperatingStateChange)

    state.lastTemperature = thermostat.currentTemperature
    state.operatingState = thermostat.currentThermostatOperatingState
    state.operatingStateSince = now()
    
    log.debug state
}
