#ifndef TDMA_H
#define TDMA_H


typedef nx_struct schedule_msg
{
	nx_uint16_t 	source;
	nx_uint16_t 	destination;
	nx_uint16_t 	slot;
	nx_uint16_t		channel;
	
} schedule_msg_t;

typedef nx_struct tdma_msg
{
	nx_uint16_t   	round;
	nx_uint8_t    	source;
	nx_uint16_t		src_slot;
	nx_uint16_t		src_channel;
	
} tdma_msg_t;

typedef nx_struct trace_msg
{
	nx_uint16_t 	round;
	nx_uint8_t		source;
	nx_uint8_t		destination;
	nx_uint8_t		seqNum;
	nx_uint8_t		numReceive;
	nx_uint16_t		slotNum;
	nx_uint32_t		gtime;
	nx_int8_t		rssi;
	
}trace_msg_t;
enum
{
	AM_TDMA_MSG = 102,
	AM_SCHEDULE_MSG = 103,
	AM_TRACE_MSG = 104
};

#endif
