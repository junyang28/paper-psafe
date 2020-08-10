#include<iostream>
#include<fstream>
using namespace std;

int main(){
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
		ScheduleConvert<<slot<<", "<<channel<<", "<<flow<<", "<<sender<<", "<<receiver<<endl;
	}

}

return 0;

}
