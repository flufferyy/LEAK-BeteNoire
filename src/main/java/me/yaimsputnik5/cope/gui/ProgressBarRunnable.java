package me.yaimsputnik5.cope.gui;


import me.yaimsputnik5.utils.Log;

import javax.swing.*;

class ProgressBarRunnable implements Runnable {

    private final JProgressBar progressBar;
    private final me.yaimsputnik5.cope.BeteNoireInstance beteNoireInstance;
    public ProgressBarRunnable(JProgressBar progressBar, me.yaimsputnik5.cope.BeteNoireInstance beteNoireInstance){
        this.progressBar = progressBar;
        this.beteNoireInstance = beteNoireInstance;
    }

    @Override
    public void run() {
        int percentage = (int) beteNoireInstance.getPercentage();
        progressBar.setString(percentage + "%");
        progressBar.setValue(percentage);
        if(progressBar.getValue() == 100) return;
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Log.log_to_file(e.toString(),"log.txt");
        }
    }

}
