#include "tdmaMsg.h"
configuration TdmaAppC
{
}
implementation
{
	components MainC, TimeSyncMicroC;
    
  	MainC.SoftwareInit -> TimeSyncMicroC;
  	TimeSyncMicroC.Boot -> MainC;
    
  	components TdmaP;
  	TdmaP.Boot -> MainC;
    
	TdmaP.GlobalTime -> TimeSyncMicroC;
  	TdmaP.TimeSyncInfo -> TimeSyncMicroC;
  	TdmaP.TimeSyncMode -> TimeSyncMicroC;
	
	components CC2420XActiveMessageC as TdmaActiveMessageC ;

	TdmaP.PacketField-> TdmaActiveMessageC.PacketRSSI;
  	TdmaP.RadioControl -> TdmaActiveMessageC;
	TdmaP.AMSend ->TdmaActiveMessageC.AMSend[AM_TDMA_MSG];
  	TdmaP.Receive ->TdmaActiveMessageC.Receive[AM_TDMA_MSG];
  	TdmaP.Packet -> TdmaActiveMessageC;
  	TdmaP.PacketTimeStamp -> TdmaActiveMessageC.PacketTimeStampRadio;
	TdmaP.Channel -> TdmaActiveMessageC.RadioChannel;
	TdmaP.PacketAcknowledgements -> TdmaActiveMessageC;
	
	//TdmaP.PacketField-> TdmaActiveMessageC.PacketTransmitPower;
	//App.CCAConfig -> TdmaActiveMessageC;
    
	components SerialActiveMessageC as TdmaSerialC;
  	TdmaP.SerialControl -> TdmaSerialC;
  	TdmaP.SerialAMSend -> TdmaSerialC.AMSend[AM_TRACE_MSG];
	TdmaP.SerialReceive -> TdmaSerialC.Receive[AM_SCHEDULE_MSG];
  	TdmaP.SerialPacket -> TdmaSerialC;
    
	components new TimerMilliC() as StartTimer;
	components new TimerMilliC() as GlobalTimer;
	components new TimerMilliC() as SyncTimer;
	components new TimerMilliC() as SendTimer;
	components new Alarm32khz32C() as SlotAlarm;
	components new AlarmMicro16C() as MicroAlarm;
	
	
	TdmaP.StartTimer -> StartTimer;	
  	TdmaP.GlobalTimer -> GlobalTimer;
	TdmaP.SyncTimer -> SyncTimer;
	TdmaP.SendTimer -> SendTimer;
	TdmaP.SlotAlarm -> SlotAlarm;
	TdmaP.MicroAlarm -> MicroAlarm;
  }
