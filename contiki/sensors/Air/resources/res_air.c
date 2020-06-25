#include <limits.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include "contiki.h"
#include "coap-engine.h"
#include "coap-blocking-api.h"
#include "random.h"

//Log configuration
#include "sys/log.h"
#define LOG_MODULE "Air sensor"
#define LOG_LEVEL LOG_LEVEL_DBG

static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_event_handler(void);

double carbon_dioxide_level = 0.03; //percentage of carbon dioxide present in the air
static int counter = 0; //number of time observing function is triggered
enum levels {GREEN, YELLOW, RED};
unsigned long timest;
extern bool purification_mode;
enum levels alert_level = GREEN;


EVENT_RESOURCE(res_air,
		       "title=\"Air sensor\";methods=\"GET\";rt=\"float\";obs\n",
		        res_get_handler,
		        NULL,
		        NULL,
			NULL,
		        res_event_handler);

static void res_event_handler(void){
	double increment = ((double)rand()/RAND_MAX)*0.004+0.001; //random increment between 0.001 and 0.005
	counter++;	
	if(purification_mode){
		if(alert_level == GREEN){
			carbon_dioxide_level = 0.03;
			LOG_INFO("PURIFICATION ON GREEN");
		}else if(alert_level == YELLOW){
			carbon_dioxide_level += increment-0.003;
			LOG_INFO("PURIFICATION ON YELLOW");
		}else if(alert_level == RED){
			carbon_dioxide_level += increment-0.005;
			LOG_INFO("PURIFICATION ON RED");
		}
	}else{
		carbon_dioxide_level += increment;
	}	
	if(carbon_dioxide_level < 0.03){
		carbon_dioxide_level = 0.03;
	}
	if(carbon_dioxide_level >= 0.03 && carbon_dioxide_level <= 0.08){

		alert_level = GREEN;
	}else if (carbon_dioxide_level > 0.08 && carbon_dioxide_level <= 0.20){

		alert_level = YELLOW;
	}else if (carbon_dioxide_level > 0.20){

		alert_level = RED;
	}
	
	//notify all observer
	coap_notify_observers(&res_air);
	LOG_DBG("observing_sender");

}

static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset){
	unsigned int accept = APPLICATION_JSON;
	coap_get_header_accept(request, &accept);
	
	timest = (unsigned long)time(NULL);
		
	if(accept == APPLICATION_JSON) {
		coap_set_header_content_format(response, APPLICATION_JSON);
		snprintf((char *)buffer, COAP_MAX_CHUNK_SIZE, "{\"carbon_dioxide\":%lf,\"timestamp\":%lu}", carbon_dioxide_level, timest);
		coap_set_payload(response, buffer, strlen((char *)buffer));
    	}else{
		coap_set_status_code(response, NOT_ACCEPTABLE_4_06);
		const char *msg = "Supporting content-types application/json";
		coap_set_payload(response, msg, strlen(msg));
	}
    
}



















