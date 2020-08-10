#include<iostream>
#include<fstream>
using namespace std;

int ConvertSchedule();
int CalculateNode();
int ConvertSchedule_Specify();
int CalculateNode_1();

int main(){

ConvertSchedule();
CalculateNode();
ConvertSchedule_Specify();
CalculateNode_1();

return 0;
}

/*-----------------------------------------------*/
//This is used to convert schedule into TDMA input
int ConvertSchedule(){
ifstream ScheduleReader;
ofstream ScheduleConvert;

ScheduleReader.open("input.txt", ios::in);
ScheduleConvert.open("output.txt",ios::out);

if (ScheduleReader.is_open()){
//	cout << " I am reading"<<endl;
	while(!ScheduleReader.eof())
	{
		int slot, channel, flow, sender, receiver;
		int routeID, hopcount, updown;
	 	char i;
		ScheduleReader>>slot>>i>>channel>>i>>flow>>i>>sender>>i>>receiver>>i>>routeID>>i>>hopcount>>i>>updown;
		slot= slot * 1;						//This can be changed
		if(slot <= 200){					//This can be changed
			ScheduleConvert<<slot<<", "<<channel<<", "<<flow<<", "<<sender<<", "<<receiver<<endl;
		}
	}

}

ScheduleReader.close();
ScheduleConvert.close();

return 0;
}
/*-----------------------------------------------*/


/*-----------------------------------------------*/
//In this function we only convert the specific data flow we want to use -> similar to ConvertSchedule
#define dataflow flow==1||flow==2||flow==3
#define dataflow_1 flow==1
int ConvertSchedule_Specify(){
	ifstream ScheduleReader;
	ofstream ScheduleConvert;

ScheduleReader.open("input.txt", ios::in);
ScheduleConvert.open("output_specific_dataflow.txt",ios::out);

if (ScheduleReader.is_open()){

	while(!ScheduleReader.eof())
	{
		int slot, channel, flow, sender, receiver;
		int routeID, hopcount, updown;
	 	char i;
		ScheduleReader>>slot>>i>>channel>>i>>flow>>i>>sender>>i>>receiver>>i>>routeID>>i>>hopcount>>i>>updown;
		slot= slot * 2;						//This can be changed
		if(dataflow_1){					//This can be changed
			ScheduleConvert<<slot<<", "<<channel<<", "<<flow<<", "<<sender<<", "<<receiver<<endl;
		}
	}

}

ScheduleReader.close();
ScheduleConvert.close();

return 0;
}
/*-----------------------------------------------*/



/*-----------------------------------------------*/
//This is used to find involved node in the network
//Find involved node ID
int CalculateNode(){
ifstream ScheduleReader;
ofstream ValidNode;
ScheduleReader.open("input.txt", ios::in);
ValidNode.open("validnode.txt", ios::out);

int valid_node[100];
int j=0;
int bound=0;
int flag_sender=0;
int flag_receiver=0;

if (ScheduleReader.is_open()){
	while(!ScheduleReader.eof())
	{
		int slot, channel, flow, sender, receiver;
		int routeID, hopcount, updown;
	 	char i;
		ScheduleReader>>slot>>i>>channel>>i>>flow>>i>>sender>>i>>receiver>>i>>routeID>>i>>hopcount>>i>>updown;
		
		for(j=0;j<=bound;j++){
			if(valid_node[j]==sender){
				flag_sender=1;
			}
			if(valid_node[j]==receiver){
				flag_receiver=1;
			}
		}
		if(flag_sender==0){
			valid_node[bound] = sender;
			bound++;
		}
		if(flag_receiver==0){
			valid_node[bound] = receiver;
			bound++;
		}

		flag_sender=0;
		flag_receiver=0;

	}

}

for(j=0;j<=bound;j++){
	ValidNode<<valid_node[j]<<endl;
	cout<<valid_node[j]<<" ";
}
cout<<endl;
ScheduleReader.close();
ValidNode.close();
return 0;
}

int CalculateNode_1(){
ifstream ScheduleReader;
//ofstream ValidNode;
ScheduleReader.open("output_specific_dataflow.txt", ios::in);
//ValidNode.open("validnode.txt", ios::out);

int valid_node[100];
int j=0;
int bound=0;
int flag_sender=0;
int flag_receiver=0;

if (ScheduleReader.is_open()){
	while(!ScheduleReader.eof())
	{
		int slot, channel, flow, sender, receiver;
		int routeID, hopcount, updown;
	 	char i;
		ScheduleReader>>slot>>i>>channel>>i>>flow>>i>>sender>>i>>receiver;
		
		for(j=0;j<=bound;j++){
			if(valid_node[j]==sender){
				flag_sender=1;
			}
			if(valid_node[j]==receiver){
				flag_receiver=1;
			}
		}
		if(flag_sender==0){
			valid_node[bound] = sender;
			bound++;
		}
		if(flag_receiver==0){
			valid_node[bound] = receiver;
			bound++;
		}

		flag_sender=0;
		flag_receiver=0;

	}

}

for(j=0;j<=bound;j++){
//	ValidNode<<valid_node[j]<<endl;
	cout<<valid_node[j]<<" ";
}
cout<<endl;
ScheduleReader.close();
//ValidNode.close();
return 0;
}