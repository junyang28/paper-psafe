#include <iostream>
#include <fstream>
#include <cmath>
#include <vector>
#include <numeric>
#include <cstdlib>
#include <ctime>
#include <map>
#include <algorithm>
using namespace std;

//#define SEND_POWER 0.318    //mJ
//#define RECEIVE_POWER 0.338	//mJ
#define IDEL_POWER 0.0192	//mJ
#define SEND_POWER 0.34452
#define RECEIVE_POWER 0.37224
//#define SEND_POWER 0.2222 //mJ
//#define RECEIVE_POWER 0.2515 //mJ
//#define WAIT_POWER 0.1302 //mJ -> No packet received just wait and close radio
//#define IDEL_POWER 0.0192

#define DATA_PERIOD 200
#define RUNTIMES 20000

#define MAXNODE 200
#define ACCESS_POINT1 121
#define ACCESS_POINT2 124

ifstream ScheduleReader;
ifstream TopologyReader;

ofstream EnergyRecord;
ofstream LatencyRecord;
ofstream LatencyRecord_B;
ofstream PdrRecord;

ofstream CoutRecord;


struct node_attribute {
	int receive;
	int send;
	int receive_wait;
}; 
struct node_attribute node[MAXNODE];
map<int, map<int, map<int, int> > > mmmPRR;  //key1 node1; key2 node2; key3 channel; value PRR
map<int, map<int, map<int, int> > >::iterator itmmmPRR;
		 map<int, map<int, int> >::iterator itmmPRR;
		 		  map<int, int>::iterator itmPRR;
vector<double> energy_v;

int generate_prr(){
	return rand()%100+1;
}

int generate_change(){
	int change;
	if(generate_prr()>50){
		change = rand()%5;
	}
	else {
		change = -rand()%5;
	}
	return change;
}

int globalASN=0;



class flow {
public:
	int schedule_flow[60][6];	//record all schedule
	int bound;					//bound for all schedule

	int record_main_flow0[60][6];	//record main schedule
	int main_bound0;				//bound for main schedule -> uplink

	int record_main_flow1[60][6];	//record main schedule2
	int main_bound1;				//bound for main schedule2 -> downlink

	int miss;						//total miss
	int transmission_attempts;		//it can be 1,2,3,...
	int transmission_attempts_backup;

	bool whether_in_back_route;		//packet goes into backup route
	bool whether_failed;			//failed or not
	bool main_flag;					

	vector<double> latency;
	int flow_number;
	vector<int> channellist;
	vector<int> nodelist;


	int sensor;
	int actuator;


	flow(){
		bound = 0;
		main_bound0 = 0;
		main_bound1 = 0;
		miss = 0;
		transmission_attempts = 1;
		transmission_attempts_backup = 0;
		whether_in_back_route = false;
		whether_failed = false;
		main_flag = false;
	}

	void record(int fid){
		flow_number = fid;
		int timeslot, channel, flowid, sender, receiver, routeid, hopcount, updown;
		char ii;
		int i;
		ScheduleReader.open("/home/young/Music/WirelessHARTsim/data/FlowSchedule.txt");
		if(ScheduleReader.is_open()){
			while(1){
				ScheduleReader>>timeslot>>ii>>channel>>ii>>flowid>>ii>>sender>>ii>>receiver>>ii>>routeid>>ii>>hopcount>>ii>>updown;
				if(ScheduleReader.eof())break;

				if (std::find(channellist.begin(), channellist.end(),channel)!=channellist.end()){

				}
				else {
					channellist.push_back(channel);
				}


				if(flowid == fid){
					schedule_flow[bound][0]=timeslot;   //The most important part is a flow's timeslot sender receiver and routeID
					schedule_flow[bound][1]=sender;
					schedule_flow[bound][2]=receiver;
					schedule_flow[bound][3]=routeid;   //Whether in the main route or backup route
					schedule_flow[bound][4]=channel;
					schedule_flow[bound][5]=updown;
					bound++;
				}
			}

				
			for(i=0;i<bound;i++){
				if(schedule_flow[i][3]==0 && schedule_flow[i][5]==0){
					record_main_flow0[main_bound0][0]=schedule_flow[i][0];
					record_main_flow0[main_bound0][1]=schedule_flow[i][1];
					record_main_flow0[main_bound0][2]=schedule_flow[i][2];
					record_main_flow0[main_bound0][3]=schedule_flow[i][3];
					record_main_flow0[main_bound0][4]=schedule_flow[i][4];
					record_main_flow0[main_bound0][5]=schedule_flow[i][5];
					main_bound0++;
				}

				if(schedule_flow[i][3]==0 && schedule_flow[i][5]==1){
					record_main_flow1[main_bound1][0]=schedule_flow[i][0];
					record_main_flow1[main_bound1][1]=schedule_flow[i][1];
					record_main_flow1[main_bound1][2]=schedule_flow[i][2];
					record_main_flow1[main_bound1][3]=schedule_flow[i][3];
					record_main_flow1[main_bound1][4]=schedule_flow[i][4];
					record_main_flow1[main_bound1][5]=schedule_flow[i][5];
					main_bound1++;
				}
			}

			for(i=0;i<main_bound0;i++){
				if(record_main_flow0[i][1]==record_main_flow0[i+1][1] && record_main_flow0[i][2]==record_main_flow0[i+1][2]){
					transmission_attempts++;
				}
				else{
					break;
				}
			}

			for(i=0;i<bound;i++){
				if(schedule_flow[i][3]!=0){
					transmission_attempts_backup++;
					break;
				}
			}
		}

		sensor = record_main_flow0[0][1];
		actuator = record_main_flow1[main_bound1-1][2];

		ScheduleReader.close();
	}


	void print_schedule(){
		//cout<<"uplink main route:"<<endl;
		cout<<"flow:\t"<<flow_number<<endl;
		cout<<"transmission_attempts:\t"<<transmission_attempts<<endl;
		cout<<"transmission_attempts_backup:\t"<<transmission_attempts_backup<<endl;
		cout<<"timeslot\tsender\treceiver\trouteid\tchannel\tupdown\n";
		for(int i=0;i<main_bound0;i++){
			cout<<record_main_flow0[i][0]<<"\t"<<record_main_flow0[i][1]<<"\t"<<record_main_flow0[i][2]<<"\t"<<record_main_flow0[i][3]<<"\t"
			<<record_main_flow0[i][4]<<"\t"<<record_main_flow0[5][0]<<"\n";
		}
		for(int i=0;i<main_bound1;i++){
			cout<<record_main_flow1[i][0]<<"\t"<<record_main_flow1[i][1]<<"\t"<<record_main_flow1[i][2]<<"\t"<<record_main_flow1[i][3]<<"\t"
			<<record_main_flow1[i][4]<<"\t"<<record_main_flow1[5][0]<<"\n";
		}
	}

	void print_node(){
		for(int i=0;i<bound;i++){
			if (std::find(nodelist.begin(), nodelist.end(),schedule_flow[i][1])==nodelist.end()){
				nodelist.push_back(schedule_flow[i][1]);
			}
			if (std::find(nodelist.begin(), nodelist.end(),schedule_flow[i][2])==nodelist.end()){
				nodelist.push_back(schedule_flow[i][2]);
			}
		}

		for(int i=0;i<nodelist.size();i++){
			cout<<nodelist[i]<<"\t";
		}
		cout<<endl;
	}


	
		
	



	//-------------------------------------------------------------------Re 2------------------------------------------------//

	int start_simulation_re2(){
		int i=0;
		int random_prr;

		for(i=0;i<bound;i++){
    		node[schedule_flow[i][2]].receive++;
    	}

		for(i=0;i<main_bound0;i=i+2){
			random_prr=generate_prr();
			node[record_main_flow0[i][1]].send++;


			int c = channellist[(record_main_flow0[i][0]+globalASN+flow_number-2)%channellist.size()];
			//timestamp --- channel ---sender --- receiver---flowid
			CoutRecord<<(record_main_flow0[i][0]+globalASN)<<"\t"<<c<<"\t"<<record_main_flow0[i][1]<<"\t"<<record_main_flow0[i][2]<<"\t"<<flow_number<<endl;


			if(random_prr>mmmPRR[record_main_flow0[i][1]][record_main_flow0[i][2]][c]){
				if(i==(main_bound0-2)){
					main_flag=1;
				}
				random_prr=generate_prr();
				node[record_main_flow0[i+1][1]].send++;

				c = channellist[(record_main_flow0[i+1][0]+globalASN+flow_number-2)%channellist.size()];
				CoutRecord<<(record_main_flow0[i+1][0]+globalASN)<<"\t"<<c<<"\t"<<record_main_flow0[i+1][1]<<"\t"<<record_main_flow0[i+1][2]<<"\t"<<flow_number<<endl;



				if(random_prr>mmmPRR[record_main_flow0[i+1][1]][record_main_flow0[i+1][2]][c]){
					backup_simulation(i/2+1);
					break;
				}
			}
		}

	
		if(whether_failed==1){
			whether_failed=0;
			whether_in_back_route=0;
			main_flag=0;
			return 0;
		}

		if(whether_failed==0){
			//if(main_flag==1){
				//latency1.push_back(((record_main_flow_1[main_bound_1-1][0]-record_main_flow_1[0][0])+1)*10);
			//	latency.push_back(((record_main_flow0[main_bound0-1][0]-1)+1)*10);
			//}
			//else{
				//cout<<schedule_flow1[main_bound_1-2][0]<<endl;
				//latency1.push_back(((record_main_flow_1[main_bound_1-2][0]-record_main_flow_1[0][0])+1)*10);
			//	latency.push_back(((record_main_flow0[main_bound0-2][0]-1)+1)*10);
			//}
			whether_failed=0;
			whether_in_back_route=0;
			main_flag=0;
			start_simulation2();
		}
		whether_failed=0;
		whether_in_back_route=0;
		main_flag=0;
		return 0;
	}


	int backup_simulation(int breakpoint){
		//cout<<breakpoint<<endl;
		int random_prr;
		whether_in_back_route=1;
		for(int i=0;i<bound;i++){
			if(schedule_flow[i][3] == breakpoint && schedule_flow[i][5]==0){
				random_prr=generate_prr();
				node[schedule_flow[i][1]].send++;

				int c = channellist[(schedule_flow[i][0]+globalASN+flow_number-2)%channellist.size()];
				CoutRecord<<(schedule_flow[i][0]+globalASN)<<"\t"<<c<<"\t"<<schedule_flow[i][1]<<"\t"<<schedule_flow[i][2]<<"\t"<<flow_number<<endl;

				if(random_prr>mmmPRR[schedule_flow[i][1]][schedule_flow[i][2]][c]){
					miss++;
					whether_failed=1;
					break;
				}
			}
		}

		if(whether_failed==0 && whether_in_back_route==1){
			for(int b=bound-1;b>=0;b--){
				if(schedule_flow[b][3] == breakpoint){
					break;
				}
			}
		}
		return 0;
	}


	int start_simulation2(){
		int i=0;
		int random_prr;

		//for(i=0;i<bound;i++){
    	//	node[schedule_flow[i][2]%100].receive++;
    	//}

		for(i=0;i<main_bound1;i=i+2){
			random_prr=generate_prr();
			node[record_main_flow1[i][1]].send++;

			int c = channellist[(record_main_flow1[i][0]+globalASN+flow_number-2)%channellist.size()];
			CoutRecord<<(record_main_flow1[i][0]+globalASN)<<"\t"<<c<<"\t"<<record_main_flow1[i][1]<<"\t"<<record_main_flow1[i][2]<<"\t"<<flow_number<<endl;

			if(random_prr>mmmPRR[record_main_flow1[i][1]][record_main_flow1[i][2]][c]){
				if(i==(main_bound1-2)){
					main_flag=1;
				}
				random_prr=generate_prr();
				node[record_main_flow1[i+1][1]].send++;

				int c = channellist[(record_main_flow1[i+1][0]+globalASN+flow_number-2)%channellist.size()];
				CoutRecord<<(record_main_flow1[i+1][0]+globalASN)<<"\t"<<c<<"\t"<<record_main_flow1[i+1][1]<<"\t"<<record_main_flow1[i+1][2]<<"\t"<<flow_number<<endl;

			
				if(random_prr>mmmPRR[record_main_flow1[i+1][1]][record_main_flow1[i+1][2]][c]){
					
					backup_simulation2(i/2+1);
					break;
				}
			}
			
		}

		
		if(whether_failed==1){
			whether_failed=0;
			whether_in_back_route=0;
			main_flag=0;
			return 0;
		}

		if(whether_failed==0 && whether_in_back_route==0){
			if(main_flag==1){
				//latency1.push_back(((record_main_flow_1[main_bound_1-1][0]-record_main_flow_1[0][0])+1)*10);
				latency.push_back(((record_main_flow1[main_bound1-1][0]-1)+1)*10);
			}
			else{
				//cout<<schedule_flow1[main_bound_1-2][0]<<endl;
				//latency1.push_back(((record_main_flow_1[main_bound_1-2][0]-record_main_flow_1[0][0])+1)*10);
				latency.push_back(((record_main_flow1[main_bound1-2][0]-1)+1)*10);
			}
		}
		whether_failed=0;
		whether_in_back_route=0;
		main_flag=0;
		return 0;
	}


	int backup_simulation2(int breakpoint){
		//cout<<breakpoint<<endl;
		int random_prr;
		whether_in_back_route=1;
		for(int i=0;i<bound;i++){
			if(schedule_flow[i][3] == breakpoint && schedule_flow[i][5]==1){
				random_prr=generate_prr();
				node[schedule_flow[i][1]].send++;

				int c = channellist[(schedule_flow[i][0]+globalASN+flow_number-2)%channellist.size()];
				CoutRecord<<(schedule_flow[i][0]+globalASN)<<"\t"<<c<<"\t"<<schedule_flow[i][1]<<"\t"<<schedule_flow[i][2]<<"\t"<<flow_number<<endl;
	
				if(random_prr>mmmPRR[schedule_flow[i][1]][schedule_flow[i][2]][c]){
					miss++;
					whether_failed=1;
					break;
				}
			}
		}

		if(whether_failed==0 && whether_in_back_route==1){
			for(int b=bound-1;b>=0;b--){
				if(schedule_flow[b][3] == breakpoint && schedule_flow[b][5]==1){
				
					latency.push_back(((schedule_flow[b][0]-1)+1)*10);
					break;
				}
			}
		}
		return 0;
	}

   //-------------------------------------------------------------------Re 1------------------------------------------------//
	int start_simulation_re1(){
		int i=0;
		int random_prr;

		for(i=0;i<bound;i++){
    		node[schedule_flow[i][2]].receive++;
    	}

		for(i=0;i<main_bound0;i=i+1){
			random_prr=generate_prr();
			node[record_main_flow0[i][1]].send++;


			int c = channellist[(record_main_flow0[i][0]+globalASN+flow_number-2)%channellist.size()];
			CoutRecord<<(record_main_flow0[i][0]+globalASN)<<"\t"<<c<<"\t"<<record_main_flow0[i][1]<<"\t"<<record_main_flow0[i][2]<<"\t"<<flow_number<<endl;

			if(random_prr>mmmPRR[record_main_flow0[i][1]][record_main_flow0[i][2]][c]){
			
					backup_simulation_re1(i);
					break;
				
			}
		}

	
		if(whether_failed==1){
			whether_failed=0;
			whether_in_back_route=0;
			main_flag=0;
			return 0;
		}

		if(whether_failed==0){
			whether_failed=0;
			whether_in_back_route=0;
			main_flag=0;
			start_simulation2_re1();
		}
		whether_failed=0;
		whether_in_back_route=0;
		main_flag=0;
		return 0;
	}

	int backup_simulation_re1(int breakpoint){
		//cout<<breakpoint<<endl;
		int random_prr;
		whether_in_back_route=1;
		for(int i=0;i<bound;i++){
			if(schedule_flow[i][3] == breakpoint && schedule_flow[i][5]==0){
				random_prr=generate_prr();
				node[schedule_flow[i][1]].send++;

				int c = channellist[(schedule_flow[i][0]+globalASN+flow_number-2)%channellist.size()];
				CoutRecord<<(schedule_flow[i][0]+globalASN)<<"\t"<<c<<"\t"<<schedule_flow[i][1]<<"\t"<<schedule_flow[i][2]<<"\t"<<flow_number<<endl;

				if(random_prr>mmmPRR[schedule_flow[i][1]][schedule_flow[i][2]][c]){
					miss++;
					whether_failed=1;
					break;
				}
			}
		}

		if(whether_failed==0 && whether_in_back_route==1){
			for(int b=bound-1;b>=0;b--){
				if(schedule_flow[b][3] == breakpoint){
					break;
				}
			}
		}
		return 0;
	}

	int start_simulation2_re1(){
		int i=0;
		int random_prr;

		//for(i=0;i<bound;i++){
    	//	node[schedule_flow[i][2]%100].receive++;
    	//}

		for(i=0;i<main_bound1;i=i+1){
			random_prr=generate_prr();
			node[record_main_flow1[i][1]].send++;

			int c = channellist[(record_main_flow1[i][0]+globalASN+flow_number-2)%channellist.size()];
			CoutRecord<<(record_main_flow1[i][0]+globalASN)<<"\t"<<c<<"\t"<<record_main_flow1[i][1]<<"\t"<<record_main_flow1[i][2]<<"\t"<<flow_number<<endl;

			if(random_prr>mmmPRR[record_main_flow1[i][1]][record_main_flow1[i][2]][c]){
					
					backup_simulation2_re1(i);
					break;
				
			}
			
		}

		
		if(whether_failed==1){
			whether_failed=0;
			whether_in_back_route=0;
			main_flag=0;
			return 0;
		}

		if(whether_failed==0 && whether_in_back_route==0){
				
				latency.push_back(((record_main_flow1[main_bound1-1][0]-1)+1)*10);
			
		
		}
		whether_failed=0;
		whether_in_back_route=0;
		main_flag=0;
		return 0;
	}


	int backup_simulation2_re1(int breakpoint){
		//cout<<breakpoint<<endl;
		int random_prr;
		whether_in_back_route=1;
		for(int i=0;i<bound;i++){
			if(schedule_flow[i][3] == breakpoint && schedule_flow[i][5]==1){
				random_prr=generate_prr();
				node[schedule_flow[i][1]].send++;

				int c = channellist[(schedule_flow[i][0]+globalASN+flow_number-2)%channellist.size()];
				CoutRecord<<(schedule_flow[i][0]+globalASN)<<"\t"<<c<<"\t"<<schedule_flow[i][1]<<"\t"<<schedule_flow[i][2]<<"\t"<<flow_number<<endl;

				if(random_prr>mmmPRR[schedule_flow[i][1]][schedule_flow[i][2]][c]){
					miss++;
					whether_failed=1;
					break;
				}
			}
		}

		if(whether_failed==0 && whether_in_back_route==1){
			for(int b=bound-1;b>=0;b--){
				if(schedule_flow[b][3] == breakpoint && schedule_flow[b][5]==1){
				
					latency.push_back(((schedule_flow[b][0]-1)+1)*10);
					break;
				}
			}
		}
		return 0;
	}


	//-------------------------------------------------------------------Re 0------------------------------------------------//
	int start_simulation_re0(){
		int i=0;
		int random_prr;

		for(i=0;i<bound;i++){
    		node[schedule_flow[i][2]].receive++;
    	}

		for(i=0;i<main_bound0;i=i+1){
			random_prr=generate_prr();
			node[record_main_flow0[i][1]].send++;


			int c = channellist[(record_main_flow0[i][0]+globalASN+flow_number-2)%channellist.size()];
			CoutRecord<<(record_main_flow0[i][0]+globalASN)<<"\t"<<c<<"\t"<<record_main_flow0[i][1]<<"\t"<<record_main_flow0[i][2]<<"\t"<<flow_number<<endl;

			if(random_prr>mmmPRR[record_main_flow0[i][1]][record_main_flow0[i][2]][c]){
					whether_failed = 1;
					miss++;
					break;	
			}
		}

	
		if(whether_failed==1){
			whether_failed=0;
			whether_in_back_route=0;
			main_flag=0;
			return 0;
		}

		if(whether_failed==0){
			whether_failed=0;
			whether_in_back_route=0;
			main_flag=0;
			start_simulation2_re0();
		}
		whether_failed=0;
		whether_in_back_route=0;
		main_flag=0;
		return 0;
	}

	int start_simulation2_re0(){
		int i=0;
		int random_prr;

		//for(i=0;i<bound;i++){
    	//	node[schedule_flow[i][2]%100].receive++;
    	//}

		for(i=0;i<main_bound1;i=i+1){
			random_prr=generate_prr();
			node[record_main_flow1[i][1]].send++;

			int c = channellist[(record_main_flow1[i][0]+globalASN+flow_number-2)%channellist.size()];
			CoutRecord<<(record_main_flow1[i][0]+globalASN)<<"\t"<<c<<"\t"<<record_main_flow1[i][1]<<"\t"<<record_main_flow1[i][2]<<"\t"<<flow_number<<endl;

			if(random_prr>mmmPRR[record_main_flow1[i][1]][record_main_flow1[i][2]][c]){
				
					whether_failed = 1;
					miss++;
					break;
				
			}			
		}

		
		if(whether_failed==1){
			whether_failed=0;
			whether_in_back_route=0;
			main_flag=0;
			return 0;
		}

		if(whether_failed==0){
				
			latency.push_back(((record_main_flow1[main_bound1-1][0]-1)+1)*10);
			
		
		}
		whether_failed=0;
		whether_in_back_route=0;
		main_flag=0;
		return 0;
	}


	int start_simulation_re1_source(){
		int i=0;
		int random_prr;

		for(i=0;i<bound;i++){
    		node[schedule_flow[i][2]].receive++;
    	}

		for(i=0;i<main_bound0;i=i+2){
			random_prr=generate_prr();
			node[record_main_flow0[i][1]].send++;


			int c = channellist[(record_main_flow0[i][0]+globalASN+flow_number-2)%channellist.size()];
			CoutRecord<<(record_main_flow0[i][0]+globalASN)<<"\t"<<c<<"\t"<<record_main_flow0[i][1]<<"\t"<<record_main_flow0[i][2]<<"\t"<<flow_number<<endl;

			if(random_prr>mmmPRR[record_main_flow0[i][1]][record_main_flow0[i][2]][c]){
				if(i==(main_bound0-2)){
					main_flag=1;
				}
				random_prr=generate_prr();
				node[record_main_flow0[i+1][1]].send++;

				c = channellist[(record_main_flow0[i+1][0]+globalASN+flow_number-2)%channellist.size()];
				CoutRecord<<(record_main_flow0[i+1][0]+globalASN)<<"\t"<<c<<"\t"<<record_main_flow0[i+1][1]<<"\t"<<record_main_flow0[i+1][2]<<"\t"<<flow_number<<endl;
	
				if(random_prr>mmmPRR[record_main_flow0[i+1][1]][record_main_flow0[i+1][2]][c]){
					//backup_simulation(i/2+1);
					whether_failed = 1;
					miss++;
					break;
				}
			}
		}

	
		if(whether_failed==1){
			whether_failed=0;
			whether_in_back_route=0;
			main_flag=0;
			return 0;
		}

		if(whether_failed==0){
			//if(main_flag==1){
				//latency1.push_back(((record_main_flow_1[main_bound_1-1][0]-record_main_flow_1[0][0])+1)*10);
			//	latency.push_back(((record_main_flow0[main_bound0-1][0]-1)+1)*10);
			//}
			//else{
				//cout<<schedule_flow1[main_bound_1-2][0]<<endl;
				//latency1.push_back(((record_main_flow_1[main_bound_1-2][0]-record_main_flow_1[0][0])+1)*10);
			//	latency.push_back(((record_main_flow0[main_bound0-2][0]-1)+1)*10);
			//}
			whether_failed=0;
			whether_in_back_route=0;
			main_flag=0;
			start_simulation2_re1_source();
		}
		whether_failed=0;
		whether_in_back_route=0;
		main_flag=0;
		return 0;
	}



	int start_simulation2_re1_source(){
		int i=0;
		int random_prr;

		//for(i=0;i<bound;i++){
    	//	node[schedule_flow[i][2]%100].receive++;
    	//}

		for(i=0;i<main_bound1;i=i+2){
			random_prr=generate_prr();
			node[record_main_flow1[i][1]].send++;

			int c = channellist[(record_main_flow1[i][0]+globalASN+flow_number-2)%channellist.size()];
			CoutRecord<<(record_main_flow1[i][0]+globalASN)<<"\t"<<c<<"\t"<<record_main_flow1[i][1]<<"\t"<<record_main_flow1[i][2]<<"\t"<<flow_number<<endl;

			if(random_prr>mmmPRR[record_main_flow1[i][1]][record_main_flow1[i][2]][c]){
				if(i==(main_bound1-2)){
					main_flag=1;
				}
				random_prr=generate_prr();
				node[record_main_flow1[i+1][1]].send++;

				int c = channellist[(record_main_flow1[i+1][0]+globalASN+flow_number-2)%channellist.size()];
				CoutRecord<<(record_main_flow1[i+1][0]+globalASN)<<"\t"<<c<<"\t"<<record_main_flow1[i+1][1]<<"\t"<<record_main_flow1[i+1][2]<<"\t"<<flow_number<<endl;
			
				if(random_prr>mmmPRR[record_main_flow1[i+1][1]][record_main_flow1[i+1][2]][c]){
					
					//backup_simulation2(i/2+1);
					whether_failed = 1;
					miss++;
					break;
				}
			}
			
		}

		
		if(whether_failed==1){
			whether_failed=0;
			whether_in_back_route=0;
			main_flag=0;
			return 0;
		}

		if(whether_failed==0 && whether_in_back_route==0){
			if(main_flag==1){
				//latency1.push_back(((record_main_flow_1[main_bound_1-1][0]-record_main_flow_1[0][0])+1)*10);
				latency.push_back(((record_main_flow1[main_bound1-1][0]-1)+1)*10);
			}
			else{
				//cout<<schedule_flow1[main_bound_1-2][0]<<endl;
				//latency1.push_back(((record_main_flow_1[main_bound_1-2][0]-record_main_flow_1[0][0])+1)*10);
				latency.push_back(((record_main_flow1[main_bound1-2][0]-1)+1)*10);
			}
		}
		whether_failed=0;
		whether_in_back_route=0;
		main_flag=0;
		return 0;
	}





};



int record_topology(){
	int sender, receiver, prr, channel;	
	if(TopologyReader.is_open()){
		while(1){
			if(TopologyReader.eof())break;
			TopologyReader>>sender>>receiver>>prr>>channel;
			mmmPRR[sender][receiver][channel]=prr;
		}
	}
	/*
	for(itmmmPRR=mmmPRR.begin(); itmmmPRR!=mmmPRR.end(); itmmmPRR++){
		for(itmmPRR=itmmmPRR->second.begin(); itmmPRR!=itmmmPRR->second.end(); itmmPRR++){
			for(itmPRR=itmmPRR->second.begin(); itmPRR!=itmmPRR->second.end(); itmPRR++){
				cout<<itmmmPRR->first<<" "<<itmmPRR->first<<" "<<itmPRR->first<<" "<<itmPRR->second<<endl;			
			}
		}
	}
	*/
	
	return 0;
}








//-----------------------------------------------------------main function--------------------------------//

int main(int argc, char const *argv[])
{

	srand(time(NULL));	
	TopologyReader.open("/home/young/Music/WirelessHARTsim/data/Topology.txt"); //Binghamton
	EnergyRecord.open("/home/young/Music/WHARTSim2.0/output/energy.txt", ios::out);
	LatencyRecord.open("/home/young/Music/WHARTSim2.0/output/latency.txt", ios::out);
	LatencyRecord_B.open("/home/young/Music/WHARTSim2.0/output/latency_B.txt", ios::app);
	PdrRecord.open("/home/young/Music/WHARTSim2.0/output/PDR.txt", ios::out);
	CoutRecord.open("/home/young/Music/WHARTSim2.0/output/print.txt", ios::out);
	CoutRecord<<"slot\tChannel\tTx\tRx\tflowid\n";

	int k;
	for(k=0;k<MAXNODE;k++){
		node[k].send = 0;
		node[k].receive = 0;
		//node[k].receive_wait = 0;
	}

	record_topology();
	//create all flows
	flow *f1 = new flow();
	flow *f2 = new flow();
	flow *f3 = new flow();
	flow *f4 = new flow();
	flow *f5 = new flow();
	flow *f6 = new flow();
	f1->record(1);
	//f1->print_schedule();
	f2->record(2);
	//f2->print_schedule();
	f3->record(3);
	//f3->print_schedule();
	f4->record(4);
	//f4->print_schedule();
	f5->record(5);
	//f5->print_schedule();
	f6->record(6);
	//f6->print_schedule();
	//------------------//
	f1->print_node();

	if(f1->transmission_attempts==2 && f1->transmission_attempts_backup==1){
		for(int i=0;i<RUNTIMES;i++){
			//globalASN = globalASN + DATA_PERIOD;
			f1->start_simulation_re2();
			f2->start_simulation_re2();
			f3->start_simulation_re2();
			f4->start_simulation_re2();
			f5->start_simulation_re2();
			f6->start_simulation_re2();
			globalASN = globalASN + DATA_PERIOD;
		}
	}
	else if(f1->transmission_attempts==1 && f1->transmission_attempts_backup==1){
		for(int i=0;i<RUNTIMES;i++){
			//globalASN = globalASN + DATA_PERIOD;
			f1->start_simulation_re1();
			f2->start_simulation_re1();
			f3->start_simulation_re1();
			f4->start_simulation_re1();
			f5->start_simulation_re1();
			f6->start_simulation_re1();
			globalASN = globalASN + DATA_PERIOD;
		}
	}
	else if(f1->transmission_attempts==1 && f1->transmission_attempts_backup==0){
		for(int i=0;i<RUNTIMES;i++){
			//globalASN = globalASN + DATA_PERIOD;
			f1->start_simulation_re0();
			f2->start_simulation_re0();
			f3->start_simulation_re0();
			f4->start_simulation_re0();
			f5->start_simulation_re0();
			f6->start_simulation_re0();
			globalASN = globalASN + DATA_PERIOD;
		}

	}
	else if(f1->transmission_attempts==2 && f1->transmission_attempts_backup==0){				//source re1
		for(int i=0;i<RUNTIMES;i++){
			//globalASN = globalASN + DATA_PERIOD;
			f1->start_simulation_re1_source();
			f2->start_simulation_re1_source();
			f3->start_simulation_re1_source();
			f4->start_simulation_re1_source();
			f5->start_simulation_re1_source();
			f6->start_simulation_re1_source();
			globalASN = globalASN + DATA_PERIOD;
		}

	}


	//cout<<f2->flow_number;
	/*
	for(int aaa=0;aaa<f1->channellist.size();aaa++){
		cout<<f1->channellist[aaa]<<"\t";
	}
	cout<<endl;
	cout<<endl;
	for(int aaa=0;aaa<f2->channellist.size();aaa++){
		cout<<f2->channellist[aaa]<<"\t";
	}
	cout<<endl;
	cout<<endl;
	for(int aaa=0;aaa<f3->channellist.size();aaa++){
		cout<<f3->channellist[aaa]<<"\t";
	}
	cout<<endl;
	cout<<endl;
	for(int aaa=0;aaa<f4->channellist.size();aaa++){
		cout<<f4->channellist[aaa]<<"\t";
	}
	cout<<endl;
	cout<<endl;
	for(int aaa=0;aaa<f5->channellist.size();aaa++){
		cout<<f5->channellist[aaa]<<"\t";
	}
	cout<<endl;
	cout<<endl;
	for(int aaa=0;aaa<f6->channellist.size();aaa++){
		cout<<f6->channellist[aaa]<<"\t";
	}
	cout<<endl;
	*/


	cout<<"node\tTx\tRx\tRx_Wait\t\n";
	for(k=0;k<MAXNODE;k++){
		cout<<k<<"\t";
		cout<<node[k].send<<"\t";
		cout<<node[k].receive<<"\t";
		//cout<<node[k].receive_wait<<"\t";
		cout<<endl;
	}


	int sleep_slots=0;
	double energy=0;  //mJ
	for(k=0;k<MAXNODE;k++){
		if(node[k].send!=0 || node[k].receive!=0){
			cout<<k<<": "<<node[k].send<<" "<<node[k].receive<<" ";
			sleep_slots = DATA_PERIOD*RUNTIMES - node[k].send - node[k].receive;
			energy = SEND_POWER*(double)node[k].send + RECEIVE_POWER*(double)node[k].receive + IDEL_POWER*(double)sleep_slots;
			cout<<"energy:"<<energy<<" "<<"power:"<<energy/(DATA_PERIOD*RUNTIMES*10)<<endl;
			EnergyRecord<<energy*1000/(DATA_PERIOD*RUNTIMES*10)<<"\n";
			if(ACCESS_POINT1 != k && ACCESS_POINT2 != k){
				energy_v.push_back(energy*1000/(DATA_PERIOD*RUNTIMES*10));
			}
		}
	}

	energy = accumulate( energy_v.begin(), energy_v.end(), 0.0)/energy_v.size(); 
	EnergyRecord<<endl<<"average"<<endl<<energy<<"\n";

	cout<<"flow1 missed:"<<f1->miss<<endl;
	cout<<"flow2 missed:"<<f2->miss<<endl;
	cout<<"flow3 missed:"<<f3->miss<<endl;
	cout<<"flow4 missed:"<<f4->miss<<endl;
	cout<<"flow5 missed:"<<f5->miss<<endl;
	cout<<"flow6 missed:"<<f6->miss<<endl;
	PdrRecord<<"Total miss count:\n"<<f1->miss+f2->miss+f3->miss+f4->miss+f5->miss+f6->miss<<endl;
	PdrRecord<<(double)(RUNTIMES-f1->miss)/(double)RUNTIMES<<endl;
	PdrRecord<<(double)(RUNTIMES-f2->miss)/(double)RUNTIMES<<endl;
	PdrRecord<<(double)(RUNTIMES-f3->miss)/(double)RUNTIMES<<endl;
	PdrRecord<<(double)(RUNTIMES-f4->miss)/(double)RUNTIMES<<endl;
	PdrRecord<<(double)(RUNTIMES-f5->miss)/(double)RUNTIMES<<endl;
	PdrRecord<<(double)(RUNTIMES-f6->miss)/(double)RUNTIMES<<endl;
	double pdr_average;
	pdr_average = (double)(6*RUNTIMES-(f1->miss+f2->miss+f3->miss+f4->miss+f5->miss+f6->miss))/(double)(RUNTIMES*6); 
	PdrRecord<<"avearge:"<<endl<<pdr_average<<endl;


	double average1,average2,average3,average4,average5,average6;
	average1 = accumulate( f1->latency.begin(), f1->latency.end(), 0.0)/f1->latency.size(); 
	cout <<"flow1 average latency:"<<average1<<endl;
	LatencyRecord <<"flow average latency:"<<endl<<average1<<endl;
	average2 = accumulate( f2->latency.begin(), f2->latency.end(), 0.0)/f2->latency.size(); 
	cout <<"flow2 average latency:"<<average2<<endl;
	LatencyRecord <<average2<<endl;
	average3 = accumulate( f3->latency.begin(), f3->latency.end(), 0.0)/f3->latency.size();
	cout <<"flow3 average latency:"<<average3<<endl;
	LatencyRecord <<average3<<endl;
	average4 = accumulate( f4->latency.begin(), f4->latency.end(), 0.0)/f4->latency.size();
	cout <<"flow4 average latency:"<<average4<<endl;
	LatencyRecord <<average4<<endl;
	average5 = accumulate( f5->latency.begin(), f5->latency.end(), 0.0)/f5->latency.size();
	cout <<"flow5 average latency:"<<average5<<endl;
	LatencyRecord <<average5<<endl;
	average6 = accumulate( f6->latency.begin(), f6->latency.end(), 0.0)/f6->latency.size();
	cout <<"flow6 average latency:"<<average6<<endl;
	LatencyRecord <<average6<<endl<<endl<<endl;
	LatencyRecord <<average1<<" "<<average2<<" "<<average3<<" "<<average4<<" "<<average5<<" "<<average6<<endl;
	LatencyRecord <<"Total average:"<<endl<<(average1+average2+average3+average4+average5+average6)/6<<endl<<endl<<endl;


	for(int abc=0;abc < f1->latency.size();abc++){
		//cout<<latency1[abc]<<" ";
		LatencyRecord<<f1->latency[abc]<<"\n";
	}
	//cout<<endl;
	LatencyRecord<<endl<<endl<<"aa";
	LatencyRecord<<endl<<endl;

	for(int abc=0;abc < f2->latency.size();abc++){
		//cout<<latency2[abc]<<" ";
		LatencyRecord<<f2->latency[abc]<<"\n";
	}
	//cout<<endl;
	LatencyRecord<<endl<<endl<<"aa";
	LatencyRecord<<endl<<endl;

	for(int abc=0;abc < f3->latency.size();abc++){
		//cout<<latency3[abc]<<" ";
		LatencyRecord<<f3->latency[abc]<<"\n";
	}
	//cout<<endl;
	LatencyRecord<<endl<<endl<<"aa";
	LatencyRecord<<endl<<endl;

	for(int abc=0;abc < f4->latency.size();abc++){
		//cout<<latency4[abc]<<" ";
		LatencyRecord<<f4->latency[abc]<<"\n";
	}
	//cout<<endl;
	LatencyRecord<<endl<<endl<<"aa";
	LatencyRecord<<endl<<endl;

	for(int abc=0;abc < f5->latency.size();abc++){
		//cout<<latency5[abc]<<" ";
		LatencyRecord<<f5->latency[abc]<<"\n";
	}
	//cout<<endl;
	LatencyRecord<<endl<<endl<<"aa";
	LatencyRecord<<endl<<endl;

	for(int abc=0;abc < f6->latency.size();abc++){
		//cout<<latency6[abc]<<" ";
		LatencyRecord<<f6->latency[abc]<<"\n";
	}
	//cout<<endl;
	LatencyRecord<<endl<<endl<<"aa";
	LatencyRecord<<endl<<endl;


 
	average1 = accumulate( f1->latency.begin(), f1->latency.end(), 0.0)/f1->latency.size(); 
	cout <<"flow1 average latency:"<<average1<<endl;
	//LatencyRecord_B <<"flow average latency:"<<endl<<average1<<endl;
	average2 = accumulate( f2->latency.begin(), f2->latency.end(), 0.0)/f2->latency.size(); 
	cout <<"flow2 average latency:"<<average2<<endl;
	//LatencyRecord_B <<average2<<endl;
	average3 = accumulate( f3->latency.begin(), f3->latency.end(), 0.0)/f3->latency.size();  
	cout <<"flow3 average latency:"<<average3<<endl;
	//LatencyRecord_B <<average3<<endl;
	average4 = accumulate( f4->latency.begin(), f4->latency.end(), 0.0)/f4->latency.size(); 
	cout <<"flow4 average latency:"<<average4<<endl;
	//LatencyRecord_B <<average4<<endl;
	average5 = accumulate( f5->latency.begin(), f5->latency.end(), 0.0)/f5->latency.size(); 
	cout <<"flow5 average latency:"<<average5<<endl;
	//LatencyRecord_B <<average5<<endl;
	average6 = accumulate( f6->latency.begin(), f6->latency.end(), 0.0)/f6->latency.size(); 
	cout <<"flow6 average latency:"<<average6<<endl;
	//LatencyRecord_B <<average6<<endl<<endl<<endl;
	//LatencyRecord_B <<average1<<" "<<average2<<" "<<average3<<" "<<average4<<" "<<average5<<" "<<average6<<endl;
	//LatencyRecord_B <<"Total average:"<<endl<<(average1+average2+average3+average4+average5+average6)/6<<"\t"<<energy<<"\t"<<pdr_average<<"\t"<<f1->miss+f2->miss+f3->miss+f4->miss+f5->miss+f6->miss<<endl<<endl<<endl;
	LatencyRecord_B <<(average1+average2+average3+average4+average5+average6)/6<<"\t"<<energy<<"\t"<<pdr_average<<"\t"<<f1->miss+f2->miss+f3->miss+f4->miss+f5->miss+f6->miss<<endl;





	TopologyReader.close();
	//ScheduleReader.close();
	//TopologyChange.close();
	EnergyRecord.close();
	LatencyRecord.close();
	LatencyRecord_B.close();
	PdrRecord.close();
	CoutRecord.close();
	return 0;
}






