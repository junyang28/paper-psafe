module TdmaP
{
    uses
    {
        interface Boot;
        // Ftsp
        interface GlobalTime<TMicro>;
        interface TimeSyncMode;
        interface TimeSyncInfo;
        // ActiveMessageC
        interface SplitControl as RadioControl;
        interface AMSend;
        interface Receive;
        interface Packet;
        interface PacketTimeStamp<TMicro,uint32_t>;
		interface RadioChannel as Channel;
		interface PacketAcknowledgements;
		interface AMPacket;
		interface PacketField<uint8_t>;


		//interface PacketField<uint8_t> as PacketTransmitPower;
        //interface CCAConfig;
        // SerialActiveMessageC
        interface SplitControl as SerialControl;
        interface Packet as SerialPacket;
        interface AMSend as SerialAMSend;
		interface Receive as SerialReceive;
        // Timer+Alarm
        interface Timer<TMilli> as GlobalTimer;
        interface Timer<TMilli> as StartTimer;
     	interface Timer<TMilli> as SyncTimer;
		interface Timer<TMilli> as SendTimer;
		interface Alarm<T32khz, uint32_t> as SlotAlarm;
		interface Alarm<TMicro, uint16_t> as MicroAlarm;
    }  
}
implementation
{
	task void sendTestMsg();
	task void sendToSerial();


	norace uint32_t globalTime;		// current global time
	norace uint32_t nextFired; 		// start time of the next TDMA window
	norace uint16_t currentSlot;	// keep track of the current slot number
	norace uint32_t round;			// keep track of current communication round (superframe)

	norace uint8_t countSyncMsg;	// count no. of sync message each node needs to send out during SYNC window
	norace bool mode; 				// current operational mode=1->TDMA, mode=0->Sync
	norace bool isSync;
	norace bool start, startFTSP;	


	// message buffer
	message_t recmsg, senmsg, serialmsg;
	message_t* recmsgPtr;


	// store communication schedule (at most 100 schedule entries: <node, slot_num, channel>)
	norace uint8_t destArray[50];	// destArray[i]=0 (if a node is a receiver), destArray[i]>0 (a node is a sender where destArray[i] is a receiver)
	norace uint8_t sourceArray[50];
	norace uint16_t slotArray[50]; 
	norace uint8_t chArray[50];	
	norace uint16_t countElem;		// keep track of total no. of schedule entries
	norace uint16_t currentIndex;	// current index of the schedule array


	//norace uint8_t countRec[50];		// count no. of packets a node received
	norace uint32_t sendTime;		// current global time
	norace uint8_t flag;
	//norace int8_t rssi;
	//norace uint8_t currentSource;
	
	
	enum
	{
		DEFAULT_CHANNEL = 12,		// channel for synchronization (every node switch to this channel during SYNC period)

		START_POLLING = 120000,		// 120 sec in ms (let nodes sync for a while before polling global time)
		GLOBAL_START = 200000000,	// 200 sec in us (start the first TDMA window)
		TDMA_WINDOW = 10000000, 	// 10 sec in us (length of TDMA window - 1000 slots)
		SYNC_WINDOW = 3000000,		// 3 sec in us + 100 ms (length of SYNC window)
		ALARM_OVERHEAD = 405000,	// in us
		SYNC_MSG_INTERVAL = 500,	// 500 ms
		SYNC_WAIT = 200,
		SLOT_LENGTH = 320,			// 10 ms in 32kHz 
		SLOT_WINDOW = 1000,			// 1000 slots (=10s)

		// how often a node check the its gloabl clock
		CHECK_GLOBAL_MS = 1,		// 1 ms 
		CHECK_GLOBAL_US = 300,		// 300 us 
		PRE_START = 30000,			// 30 ms
		
	};
	event void Boot.booted()
   	{
   		//SerialTimer turn on radio and serial communication
		call RadioControl.start();
		call SerialControl.start();
		
		// initialization
		start = FALSE;
		startFTSP = FALSE;

		flag = 0;
		round = 0;
		currentSlot = 0; 
		countElem = 0;	
		nextFired = GLOBAL_START; // the first TDMA window will start at this time
    }
    event message_t* SerialReceive.receive(message_t* msgPtr, void* payload, uint8_t len) // receive packet from the serial port
    	{
		schedule_msg_t* in = (schedule_msg_t*)call Packet.getPayload(msgPtr, sizeof(schedule_msg_t));
		if(in->source==0) // receive command to start ftsp
		{
			call StartTimer.startOneShot(15); // wait for all nodes to finish receiving start cmd
		}
		else	// receive communication schedule (related to the node) and stored them.
		{ 
			flag = 1;
			if(in->source==TOS_NODE_ID)	// node is a sender
			{
				destArray[countElem] = in->destination;
				sourceArray[countElem] = in->source;
			}
			else // node is a receiver
			{				
				destArray[countElem] = 0;
				sourceArray[countElem] = in->source;
			}
			//countRec[countElem] = 0;
			slotArray[countElem] = in->slot;
			chArray[countElem] = in->channel;
			countElem++;
		}
		return msgPtr;
	}


	event void StartTimer.fired()
    {
		if(!startFTSP) // start FTSP (flooding mode)
		{
			startFTSP = TRUE;
			call TimeSyncMode.setMode(0); 						// start time synchronization (flooding mode)
			call StartTimer.startOneShot(START_POLLING);		
		}
		else // FTSP is already working, let a node start polling the global time (every 1 ms)
		{
			call GlobalTimer.startPeriodic(CHECK_GLOBAL_MS);	
		}
    }

	event void GlobalTimer.fired()
    {
    	uint8_t i;
        isSync = call GlobalTime.getGlobalTime(&globalTime); // get current global time
		if(globalTime>= nextFired-PRE_START)	// a little bit before the start of the next TDMA window, polling the global time more frequent
		{
			call GlobalTimer.stop();
			if(!start)
			{
				start=TRUE;					  
				call TimeSyncMode.setMode(1); // stop time synchronization
			}
			/*for(i=0;i<5;i++) // initialize countRec every round
			{
				countRec[i] = 0;
			}*/
			call MicroAlarm.start(CHECK_GLOBAL_US); // poll global time more frequent (every 300 us)
		}
    }

	async event void MicroAlarm.fired()
	{
		call MicroAlarm.start(CHECK_GLOBAL_US); 
		isSync = call GlobalTime.getGlobalTime(&globalTime); // get current global time

		/////// start TDMA Window ///////
		if(globalTime>=nextFired)
		{
			call MicroAlarm.stop();
			mode = 1; 						  											// switch to TDMA mode	
			atomic nextFired = nextFired + TDMA_WINDOW + ALARM_OVERHEAD + SYNC_WINDOW;	// calculate the time of the TDMA window
			atomic round++;																// increase round by 1
			atomic currentSlot = 0;														// set slot to 0
			atomic countSyncMsg = 0;
			signal SlotAlarm.fired(); 													// signal the first slot of this round
		}
			
	}
	async event void SlotAlarm.fired()
	{
		uint16_t i;
		atomic
		{
			call SlotAlarm.start(SLOT_LENGTH); // start timer for the next slot
			currentSlot++;
			if(currentSlot<=SLOT_WINDOW)
			{
				
				for(i=0;i<countElem;i++) // check the schedule
				{
					if(currentSlot%10==slotArray[i]) // a node is scheduled to either send or receive
					{
						currentIndex=i;
						call Channel.setChannel(chArray[i]);	// switch channel, whether a node is a sender or a receiver
						if(destArray[i]!=0) 	// sender, prepare to send
						{
							call SendTimer.startOneShot(3); // wait for 3 ms (for channel switching process) before sending out packet
						}
						break;
					}
					if(currentSlot%10==slotArray[i]+1) // switch to diferent channel
					{
						call Channel.setChannel(DEFAULT_CHANNEL);
					}
				}
				// ...also possible to schedule a node to send data to serial during TDMA window (for data collection).. //
			}
			else
			{
				/////// start sync window///////
				call SlotAlarm.stop();

				mode = 0;															// set SYNC mode
				call Channel.setChannel(DEFAULT_CHANNEL);							// every node switch to the deafult channel
				call SyncTimer.startPeriodic(SYNC_WAIT + TOS_NODE_ID%100);			// a node with lowest id (root) will start sending out time sync info first
				call StartTimer.startOneShot(SYNC_MSG_INTERVAL * 4);				// 500 ms * 4 (no. of SYNC msg each node send out)
			}
		}
	}
	event void SendTimer.fired()
	{
		post sendTestMsg();
	}
	task void sendTestMsg() // send out the test message
    {
		atomic
		{
			tdma_msg_t* out = (tdma_msg_t*)call Packet.getPayload(&senmsg, sizeof(tdma_msg_t));
			out->round = round;
			out->source = TOS_NODE_ID;
			out->src_slot = currentSlot;
			out->src_channel = call Channel.getChannel();
			//call PacketAcknowledgements.requestAck(&senmsg); // ack is required or not
			call AMSend.send(destArray[currentIndex], &senmsg, sizeof(tdma_msg_t));
			//call AMSend.send(AM_BROADCAST_ADDR, &senmsg, sizeof(tdma_msg_t));
		}
    }

	event void SyncTimer.fired()
	{
		countSyncMsg++;
        call TimeSyncMode.send(); // send out sync msg (FTSP unicast)
        if(countSyncMsg>=3) 	  // sent out 3rd sync msg -> stop 
        {
            call SyncTimer.stop();
        }
	}
	
	event message_t* Receive.receive(message_t* msgPtr, void* payload, uint8_t len)
    {
    	recmsgPtr = msgPtr;
		post sendToSerial();
		return msgPtr;
	}
	
	task void sendToSerial()
	{
		atomic
		{
			trace_msg_t* out = (trace_msg_t*)call SerialPacket.getPayload(&serialmsg, sizeof(trace_msg_t));
			tdma_msg_t* in = (tdma_msg_t*)call SerialPacket.getPayload(recmsgPtr, sizeof(tdma_msg_t));

			uint8_t raw_rssi = call PacketField.get(recmsgPtr);
			int8_t rssi = (int8_t)(((raw_rssi-0x7F)&0xFF)-45);

			call GlobalTime.getGlobalTime(&sendTime);
			out->round = round;
			out->destination = TOS_NODE_ID;
			out->slotNum = currentSlot;
			out->gtime = sendTime;
			out->source = in->source;
			out->rssi = rssi;
			out->numReceive = raw_rssi;

			call SerialAMSend.send(AM_BROADCAST_ADDR, &serialmsg, sizeof(trace_msg_t));
		}
	}
	
	event void AMSend.sendDone(message_t* ptr, error_t success){}
    event void SerialAMSend.sendDone(message_t* ptr, error_t success) {}
	event void Channel.setChannelDone(){}
	event void RadioControl.startDone(error_t err) {}
    event void RadioControl.stopDone(error_t error){}
    event void SerialControl.startDone(error_t err) {}
    event void SerialControl.stopDone(error_t error){}
}
