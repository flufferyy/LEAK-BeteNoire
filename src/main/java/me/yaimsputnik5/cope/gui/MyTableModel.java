package me.yaimsputnik5.cope.gui;

import javax.swing.table.DefaultTableModel;

class MyTableModel extends DefaultTableModel 
{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
    public boolean isCellEditable(int row, int column) 
	{
        return false;
    }
	
    public MyTableModel()
    {
        addColumn("Server #");
        addColumn("IP Address");
        addColumn("Port");
        addColumn("Player(s)");
        addColumn("Version");
        addColumn("Message Of The Day (MOTD)");

    }
}
