package me.yaimsputnik5.cope;

import org.apache.commons.cli.*;

import inet.ipaddr.IPAddressSeqRange;
import inet.ipaddr.IPAddressString;
import me.yaimsputnik5.utils.FileUtils;
import me.yaimsputnik5.utils.InvalidRangeException;
import me.yaimsputnik5.utils.IpList;
import me.yaimsputnik5.utils.PortList;

public class InputData{

    private IpList ipList;
    private PortList portrange;


    private CommandLine cmd;

    private boolean ping;
    private final String filename;

    private final Options options;

    private Options buildOptions()
    {
        Option iprange = new Option("range","iprange",true,"The IP/CIDR Range That Bête Noire Will Scan");
        iprange.setRequired(true);

        Option portrange = new Option("ports","portrange",true,"The Port(s) Bête Noire Will Scan For");
        portrange.setRequired(true);

        Option threads = new Option("th","threads",true,"Maximum Number Of Running Async Threads");
        threads.setRequired(true);

        Option timeout = new Option("ti","timeout",true,"Server Ping Timeout");
        timeout.setRequired(true);

        Option count = new Option("c","pingcount",true,"How Many Times Bête Noire Pings A Server");
        count.setRequired(false);

        Option noping = new Option("noping",false,"Prevent Bête Noire From Pinging IPs Before Initializing Bête Noire");
        noping.setRequired(false);

        Option nooutput = new Option("nooutput",false,"Prevents Bête Noire From Outputting To File");
        nooutput.setRequired(false);

        Option all = new Option("all",false,"Forces Bête Noire To Scan Broadcast IP(s) And Common Port(s)");
        all.setRequired(false);

        Option fulloutput = new Option("fulloutput",false,"Creates A More Readable, But Bigger Output File");
        fulloutput.setRequired(false);

        Option filterVersion = new Option("ver","filterversion",true,"Only Show Servers Per Selected Version");
        filterVersion.setRequired(false);

        Option filterMotd = new Option("motd","filtermotd",true,"Only Show Servers Per Entered MOTD");
        filterMotd.setRequired(false);

        Option filterOn = new Option("on","minonline",true,"Only Show Servers With <arg> Players Online");
        filterOn.setRequired(false);

        Option debug = new Option("d","debug",false,"Enables Debugger/Debugging Mode");
        debug.setRequired(false);

        Options options = new Options();
        options.addOption(iprange);
        options.addOption(portrange);
        options.addOption(threads);
        options.addOption(timeout);
        options.addOption(count);
        options.addOption(noping);
        options.addOption(nooutput);
        options.addOption(all);
        options.addOption(fulloutput);
        options.addOption(filterVersion);
        options.addOption(filterMotd);
        options.addOption(filterOn);
        options.addOption(debug);

        return options;
    }

    public void help(){
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("-range <arg> -ports <arg> -th <arg> -ti <arg>",options);
        System.exit(-1);
    }
    
    public InputData(String[] command) throws InvalidRangeException,NumberFormatException {
        options = buildOptions();
        CommandLineParser parser = new DefaultParser();
        String ipStart = "",ipEnd = "";
        try 
        {
            cmd = parser.parse(options,command);
            try
            {
            	//Check for begin-end range first, first split the string
            	String[] beginEnd = cmd.getOptionValue("range").split("-");
            	
            	//See if its length is 2 (begin-end)
            	if (beginEnd.length >= 2)
            	{            		
            		ipStart = cmd.getOptionValue("range").split("-")[0];
            		ipEnd = cmd.getOptionValue("range").split("-")[1];
            	}
            	
            	//Checks if the string split are both IPs. If not, IPAddressString parses them as a CIDR or shorthand range.
            	if (IpList.isNotIp(ipStart) || IpList.isNotIp(ipEnd))
            	{
            		IPAddressSeqRange range = new IPAddressString(cmd.getOptionValue("range")).getSequentialRange();
            		ipStart = range.getLower().toString();
            		ipEnd = range.getUpper().toString();            		
            	}
            }
            catch (NullPointerException | IndexOutOfBoundsException e) 
            {
            	if(Info.gui) throw new InvalidRangeException();
            	else help();
            }
            
            try
            {
                ipList = new IpList(ipStart,ipEnd);
            }        
            catch (IllegalArgumentException e){
                throw new IllegalArgumentException(e.getMessage());
            }
            try{
                portrange = new PortList(cmd.getOptionValue("ports"));
            }catch (NumberFormatException e){
                if(Info.gui) throw new NumberFormatException();
                help();
            }

            ping = !cmd.hasOption("noping");
        } catch (ParseException  e)
        {
            help(); //help contiene system.exit
        }

        if(isOutput())
        {
            filename = FileUtils.getCorrectFileName("outputs/" + ipStart + "-" + ipEnd);
            FileUtils.appendToFile("Bête Noire by Mr.Gibson#1337 | UserID: 889026633569275954 | IGN: YaimSputnik5 | ✞itsyoungdaddy✞#3788 | UserID: 253142674792644608 | Version " + Info.version + " " + Info.otherVersionInfo, filename);
        }
        else filename = null;
    }

    public boolean isPing() {
        return ping;
    }
    public boolean isOutput() {
        return !cmd.hasOption("nooutput");
    }
    public boolean isSkipCommonPorts() {
        return !cmd.hasOption("all");
    }
    public boolean isFulloutput() {
        return cmd.hasOption("fulloutput");
    }
    public void setPing(boolean ping){
        this.ping = ping;
    }

    public int getCount() {
        return Integer.parseInt(cmd.getOptionValue("c","1"));
    }
    public IpList getIpList() {
        return ipList;
    }

    public PortList getPortrange() {
        return portrange;
    }
    public int getThreads() throws NumberFormatException{
        return Integer.parseInt(cmd.getOptionValue("th"));
    }
    public int getTimeout() {
        return Integer.parseInt(cmd.getOptionValue("ti"));
    }
    public String getFilename() {
        return filename;
    }

    public String getMotd(){
        return cmd.getOptionValue("filtermotd","");
    }

    public String getVersion(){
        return cmd.getOptionValue("filterversion","");
    }
    public int getMinPlayer(){
        return Integer.parseInt(cmd.getOptionValue("on","-1"));
    }

    public boolean isDebugMode(){ return cmd.hasOption("debug"); }
}
