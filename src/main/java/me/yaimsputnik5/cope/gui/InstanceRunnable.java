package me.yaimsputnik5.cope.gui;

import me.yaimsputnik5.utils.Log;

class InstanceRunnable implements Runnable {


    private final me.yaimsputnik5.cope.BeteNoireInstance beteNoireInstance;
    private final MainWindow window;

    public void stop(){
        this.beteNoireInstance.stop();
    }

    public InstanceRunnable(me.yaimsputnik5.cope.BeteNoireInstance beteNoireInstance, MainWindow window){ // Window is a "pointer" to the main window
        this.beteNoireInstance = beteNoireInstance;
        this.window = window;
    }

    @Override
    public void run() {
        try{
            beteNoireInstance.run();
        }
        catch (NumberFormatException e){
            if(Confirm.requestConfirm("Check threads or timeout fields and relaunch program, would you like to see an example configuration?"))
                window.exampleConf();
        }
        window.idle();
        Log.logln("Stopped");
    }
}
