#include <iostream>
#include <fstream>
#include <vector>
#include <algorithm>
#include <map>

using namespace std;

ifstream CollectReader;
ofstream TraceGenerator;

map<int, map<int, int> >  PRRTopology;
map<int, map<int, int> >::iterator mmit;
		map<int, int>::iterator mit;

int generate_prr(){
	return rand()%100+1;
}

int generate_change(){
	int change;
	if(generate_prr()>50){
		change = rand()%30;
	}
	else {
		change = -rand()%4;
	}
	return change;
}

void countnode(){
	vector<int> countnode;

	int node1, node2, seq, power, rssi, lqi, chop, timestamp;
	char date[20];
	char time[20];
	int timestamp2;
	double longdata;
	int number;

	CollectReader.open("new.txt");

	if(CollectReader.is_open()){
		while(!CollectReader.eof()){
			CollectReader >> node1 >> node2 >> seq >> power >> rssi >> lqi >> chop >> timestamp2 >> date >> time >> timestamp2 >> longdata >> number;
			if(CollectReader.eof()) break;
			if(find(countnode.begin(),countnode.end(),node1)==countnode.end()){
				countnode.push_back(node1);
			}
			if(find(countnode.begin(),countnode.end(),node2)==countnode.end()){
				countnode.push_back(node2);
			}

		}
	}

	sort(countnode.begin(),countnode.end());
	
	for(vector<int>::iterator i=countnode.begin();i!=countnode.end();i++){
		cout<<*i<<"\t";
	}
	cout<<endl;

	CollectReader.close();
}

void Generate_Topology(){
	int node1, node2, seq, power, rssi, lqi, chop, timestamp;
	char date[20];
	char time[20];
	int timestamp2;
	double longdata;
	int number;

	bool find=false;

	CollectReader.open("new.txt");

	if(CollectReader.is_open()){
		while(!CollectReader.eof()){
			CollectReader >> node1 >> node2 >> seq >> power >> rssi >> lqi >> chop >> timestamp2 >> date >> time >> timestamp2 >> longdata >> number;
			if(CollectReader.eof()) break;
			for(mmit=PRRTopology.begin();mmit!=PRRTopology.end();mmit++){
				for(mit=mmit->second.begin();mit!=mmit->second.end();mit++){
					if(mmit->first == node1 && mit->first == node2){
						find = true;
					}
				}
			}
			if(find==true){
				PRRTopology[node1][node2]++;
			}
			else{
				PRRTopology[node1][node2] = 1;
			}
			find = false;
		}
	}
	

	CollectReader.close();

}

void Print_Topology(){
	TraceGenerator.open("trace.txt");

	for(mmit=PRRTopology.begin();mmit!=PRRTopology.end();mmit++){
		for(mit=mmit->second.begin();mit!=mmit->second.end();mit++){
			if(PRRTopology[mit->first][mmit->first]==0){
				PRRTopology[mit->first][mmit->first] = mit->second;
			}	
		}
	}
	//double the PRR


	for(mmit=PRRTopology.begin();mmit!=PRRTopology.end();mmit++){
		for(mit=mmit->second.begin();mit!=mmit->second.end();mit++){
			//cout<<mmit->first<<"\t"<<mit->first<<"\t"<<mit->second<<endl;
			TraceGenerator<<mmit->first<<"\t"<<mit->first<<"\t"<<mit->second<<"\t26"<<endl;
		}
	}

	
}

void Generate_Other_Channel(){
	for(int i=11;i<26;i++){
		for(mmit=PRRTopology.begin();mmit!=PRRTopology.end();mmit++){
			for(mit=mmit->second.begin();mit!=mmit->second.end();mit++){
				//cout<<mmit->first<<"\t"<<mit->first<<"\t"<<mit->second<<endl;
				mit->second = mit->second + generate_change();
				if(mit->second<0){
					mit->second = 0;
				}
				if(mit->second>100){
					mit->second = 100;
				}

				TraceGenerator<<mmit->first<<"\t"<<mit->first<<"\t"<<mit->second<<"\t"<<i<<endl;
			}
		}

	}
	TraceGenerator.close();
}

int main(int argc, char const *argv[])
{
	srand(time(NULL));


	countnode();
	Generate_Topology();
	Print_Topology();
	Generate_Other_Channel();


	return 0;
}