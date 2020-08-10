import java.io.*;
import net.tinyos.message.*;
import net.tinyos.util.*;
public class Logger implements MessageListener
{
	// neeed to update the NODES and NUM_NODES
	
	private static final int NUM_NODES = 70;
	public String filename = "sched.txt";
	PrintStream outputFile = null;
	MoteIF mote;
	public Logger() throws IOException
	{
		start();
		sendSchedule();
		String name=""+System.currentTimeMillis();
		try
                {
                        outputFile = new PrintStream(new FileOutputStream(name+".txt"));
                }
                catch (Exception e){}
		sendStartFtsp();
	}
	
	public void start()
	{
		try 
		{
            mote = new MoteIF();
            mote.registerListener(new TraceMsg(), this);
            System.out.println("Connect Successfully!");
        }
        catch(Exception e) {}
	}
	public void sendSchedule()
	{
		String line = null;
		String delim = "[,]";
		String[] tokens;
		int src, dest, slot, lineCount=0;
		short ch;
		try
		{
			FileInputStream fstream = new FileInputStream(filename);
			DataInputStream input = new DataInputStream(fstream);
			BufferedReader buffer = new BufferedReader(new InputStreamReader(input));
			
			//Read Schedule file line by line//				
			while ((line = buffer.readLine()) != null) 
			{
					line = line.replaceAll("\\s+", "");
					tokens = line.split(delim);
					ScheduleMsg scheduleCmd = new ScheduleMsg();
					slot = Integer.parseInt(tokens[0]);
					ch = Short.parseShort(tokens[1]);
					src = Integer.parseInt(tokens[3]);
					dest = Integer.parseInt(tokens[4]);
					scheduleCmd.set_source(src);
					scheduleCmd.set_destination(dest);
					scheduleCmd.set_slot(slot);
					scheduleCmd.set_channel(ch);
					mote.send(src, scheduleCmd); // send schedule to source*/
					try 
					{
	    					Thread.sleep(50);
					
					}
					catch (Exception e) 
					{
	   					 e.printStackTrace();
	 				}
					mote.send(dest, scheduleCmd); // send schedule to destination
					try 
					{
	    					Thread.sleep(50);
					
					}
					catch (Exception e) 
					{
	   					 e.printStackTrace();
	 				}
	  				System.out.println("source:" +src +" slot:" +slot +" channel:" +ch +" destination:" +dest);
  			}
  			input.close();
    	}
		catch (Exception e)
		{
  			System.err.println("Error: " + e.getMessage());
  		}
	}
	public void sendStartFtsp()
	{
		ScheduleMsg startCmd = new ScheduleMsg();
		startCmd.set_source(0);
		for(int k=1;k<=NUM_NODES;k++)
		{
			int node_id = 100+k;
			try
			{
				mote.send(node_id, startCmd);
			}
			catch (Exception e){}
			try
			{
				Thread.sleep(30);
			}
			catch(InterruptedException e){}
			//System.out.println("send start cmd to " +NODES[k]);
		}
		
	}
	public void messageReceived(int dest, Message m)
     {
		TraceMsg msg = (TraceMsg)m;
		int src = msg.get_source();
		int rssi = msg.get_rssi();
		int rssi_raw = msg.get_numReceive();
		int round = msg.get_round();
		int des = msg.get_destination();
		int slot = msg.get_slotNum();
		long gtime = msg.get_gtime();
		String output = "";
		
		output = output +round +" " +src +" " +des +" " +slot +" " +rssi +" " +rssi_raw +" " +gtime; 
			
        outputFile.println(output);
        System.out.println(output);
        outputFile.flush();
	}
	public static void main(String args[]) throws IOException
  	{
		new Logger();
	}
}

