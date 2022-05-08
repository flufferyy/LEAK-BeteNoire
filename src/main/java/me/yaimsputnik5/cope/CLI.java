package me.yaimsputnik5.cope;

import me.yaimsputnik5.utils.FileUtils;
import me.yaimsputnik5.utils.KeyboardThread;
import me.yaimsputnik5.utils.Log;
import me.yaimsputnik5.versionChecker.VersionChecker;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CLI {

	private static BeteNoireInstance beteNoireInstance;

	public static BeteNoireInstance getBeteNoireInstance()
	{
		return beteNoireInstance;
	}

	static void init(String[] a) 
	{
		printLogo();
		if(!isUTF8Mode()){
			System.out.println("The scanner isn't running in UTF-8 mode!");
			System.out.println("Put \"-Dfile.encoding=UTF-8\" in JVM args in order to run the program correctly!");
			System.exit(-1);
		}
		VersionChecker.checkNewVersion();
		FileUtils.createFolder("outputs");
		ExecutorService inputService = Executors.newSingleThreadExecutor();
		inputService.execute(new KeyboardThread());
		if (Arrays.equals(new String[] { "-txt" }, a))
			txtRun();
		else
			standardRun(a);
		Log.logln("Scan terminated - " + Info.serverFound + " (" + Info.serverNotFilteredFound + " in total)");
		System.exit(0);
	}

	private static void printLogo()
	{
		System.out.println("S3XY L0G) C0M1NG S00N!");
	}

	private static void standardRun(String[] a)
	{
		InputData i;
		try 
		{
			i = new InputData(a);
		} 
		catch (Exception e) 
		{
			System.err.println(e.getMessage());
			return;
		}
		Info.debugMode = i.isDebugMode();
		beteNoireInstance = new BeteNoireInstance(i);
		try{
			beteNoireInstance.run();
		}catch (NumberFormatException e){
			beteNoireInstance.inputData.help();
		}
	}

	private static void txtRun() 
	{
		try 
		{
			BufferedReader reader = new BufferedReader(new FileReader("ranges.txt"));
			String s;
			while ((s = reader.readLine()) != null) 
			{
				if (s.isEmpty())
				{					
					continue;
				}
				
				InputData i;
				try 
				{
					i = new InputData(s.split(" "));
				}
				catch (Exception e) 
				{
					System.err.println(e.getCause().getMessage());
					reader.close();
					return;
				}

				beteNoireInstance = new BeteNoireInstance(i);
				Log.logln("Now running: " + beteNoireInstance.getFilename());
				beteNoireInstance.run();
			}
			reader.close();
		} 
		catch (IOException e) 
		{
			System.err.println("File \"ranges.txt\" not found, create a new one and restart the scanner");
			System.exit(-1);
		}
	}

	private static boolean isUTF8Mode()
	{
		List<String> arguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
		return arguments.contains("-Dfile.encoding=UTF-8");
	}

}