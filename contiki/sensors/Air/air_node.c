#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "contiki.h"
#include "coap-engine.h"
#include "coap-blocking-api.h"
#include "os/dev/leds.h"


// Log configuration
#include "sys/log.h"
#define LOG_MODULE "Air_node"
#define LOG_LEVEL LOG_LEVEL_INFO

// Resource definition
extern coap_resource_t res_air; //sensor
extern coap_resource_t res_purifier; //actuator
extern enum levels {GREEN, YELLOW, RED} alert_level;
//extern enum levels alert_level;

#define SERVER_EP ("coap://[fd00::1]:5683")
PROCESS(air_process, "Air_node");
AUTOSTART_PROCESSES(&air_process);

//static coap_message_type_t result = COAP_TYPE_RST;

void client_chunk_handler(coap_message_t *response){
  //const uint8_t *chunk;
  if(response == NULL) {
    return;
  }
}


PROCESS_THREAD(air_process, ev, data){
	static coap_endpoint_t server_ep;
    	static coap_message_t request[1];
   	static struct etimer timer;
	
	PROCESS_BEGIN();
	
	//acivate the resources
	coap_activate_resource(&res_air, "sensors/air");
	coap_activate_resource(&res_purifier, "actuators/purifier");
	
	//pupolate coap_endpoint_t data structure
	coap_endpoint_parse(SERVER_EP, strlen(SERVER_EP), &server_ep);

	//prepare the message
	coap_init_message(request, COAP_TYPE_CON, COAP_GET, 0);
	coap_set_header_uri_path(request, "registrant");
	LOG_INFO("registering\n");
	COAP_BLOCKING_REQUEST(&server_ep, request, client_chunk_handler);
	
	LOG_INFO("registered\n");
	etimer_set(&timer, CLOCK_SECOND * 10);
	while(1){
		
		PROCESS_WAIT_EVENT();
		if(ev == PROCESS_EVENT_TIMER && data == &timer){
			    res_air.trigger();
				LOG_INFO("Event triggered\n");
				if(alert_level == GREEN){
					leds_set(LEDS_NUM_TO_MASK(LEDS_GREEN));
				}else if(alert_level == YELLOW){
					leds_set(LEDS_NUM_TO_MASK(LEDS_YELLOW));
				}else if(alert_level == RED){
					leds_set(LEDS_NUM_TO_MASK(LEDS_RED));
				}
			
		etimer_set(&timer, CLOCK_SECOND * 10);
		}
		res_air.trigger();
		LOG_INFO("Event triggered\n");
		if(alert_level == GREEN){
			leds_set(LEDS_NUM_TO_MASK(LEDS_GREEN));
		}else if(alert_level == YELLOW){
			leds_set(LEDS_NUM_TO_MASK(LEDS_YELLOW));
		}else if(alert_level == RED){
			leds_set(LEDS_NUM_TO_MASK(LEDS_RED));
		}
			
		etimer_set(&timer, CLOCK_SECOND * 10);
	}
	
	PROCESS_END();
}
 





