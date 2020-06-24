#include <limits.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "contiki.h"
#include "coap-engine.h"
#include "coap-blocking-api.h"
#include "random.h"

//Log configuration
#include "sys/log.h"
#define LOG_MODULE "Air sensor"
#define LOG_LEVEL LOG_LEVEL_DBG

extern double carbon_dioxide_level; //percentage of carbon dioxide present in the air
bool purification_mode = false; //TRUE: actuator active, FALSE: actuator idle

static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_post_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

RESOURCE(res_purifier,
	 "title=\"Air purifier\";methods=\"GET/POST/PUT\"mode=on|off\";rt=\"float\";obs\n",
	 res_get_handler,
	 res_post_put_handler,
	 res_post_put_handler,
	 NULL);

static void res_post_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset){
	if(request!= NULL){
		LOG_DBG("received POST/PUT");
	}
	int success = 1;
	size_t len_mode;
	const char *mode;
	
	if((len_mode = coap_get_post_variable(request, "mode", &mode))){
		LOG_INFO("mode %s\n", mode);
			
		if(strncmp(mode, "on", len_mode) == 0){ //activating actuator
			purification_mode = true;
			LOG_INFO("OK");
		}else if(strncmp(mode, "off", len_mode) == 0){ //deactivating actuator
			purification_mode = false;
			LOG_INFO("OK");
		}else{
			success = 0;
		}
	}else{
		success = 0;
	}

	if(!success){
		coap_set_status_code(response, BAD_REQUEST_4_00);

	}else{
		coap_set_status_code(response, CHANGED_2_04);
	}

}


static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset){
	unsigned int accept = APPLICATION_JSON;
	coap_get_header_accept(request, &accept);
	/*
	coap_set_header_content_format(response, TEXT_PLAIN);
  	coap_set_payload(response, buffer, snprintf((char *)buffer, preferred_size, "EVENT %lu, carbon_dioxide_level: %lf", (unsigned long) counter, carbon_dioxide_level));
	*/
	
	if(accept == TEXT_PLAIN){
		coap_set_header_content_format(response, TEXT_PLAIN);
  		coap_set_payload(response, buffer, snprintf((char *)buffer, preferred_size, "carbon_dioxide_level: %lf, purification mode: %s\n", carbon_dioxide_level,(purification_mode == true ? "ON":"OFF")));
	}else if(accept == APPLICATION_XML) {
		coap_set_header_content_format(response, APPLICATION_XML);
		snprintf((char *)buffer, COAP_MAX_CHUNK_SIZE, "<purification_mode=\"%d\"/>", purification_mode?1:0);
		coap_set_payload(response, buffer, strlen((char *)buffer));
  	}else if(accept == APPLICATION_JSON) {
		coap_set_header_content_format(response, APPLICATION_JSON);
		snprintf((char *)buffer, COAP_MAX_CHUNK_SIZE, "{\"purification_mode\":%d}", purification_mode?1:0);
		coap_set_payload(response, buffer, strlen((char *)buffer));
    	}
}


