BUILD_EXTRA_DEPS = Main.class

Main.class: TraceMsg.java ScheduleMsg.java
	javac *.java

TraceMsg.java: ../tdmaMsg.h
	nescc-mig java -java-classname=TraceMsg ../tdmaMsg.h trace_msg -o $@

ScheduleMsg.java: ../tdmaMsg.h
	nescc-mig java -java-classname=ScheduleMsg ../tdmaMsg.h schedule_msg -o $@

clean:
	rm -f ScheduleMsg.java TraceMsg.java *.class

