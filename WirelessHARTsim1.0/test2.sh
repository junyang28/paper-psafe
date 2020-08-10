#!/bin/sh
cd /home/young/Desktop/ReliableScheduler_TESTBED
make

for i in 3 2 1
do
	for j in 1 2 3 4 5 6 7 8
	do
		for k in 0.7 0.71 0.72 0.73 0.74 0.75 0.76 0.77 0.78 0.79 0.8 0.81 0.82 0.83 0.84 0.85 0.86 0.87 0.88 0.89 0.9
		do
			cd /home/young/Desktop/ReliableScheduler_TESTBED
			./a.out $i $j $k
			cd /home/young/Music/WirelessHARTsim
			./a.out $i $j $k
		done
	done
done

