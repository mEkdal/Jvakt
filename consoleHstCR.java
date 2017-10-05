package Jvakt;


import javax.swing.*;
import java.awt.*;
import javax.swing.table.*;

class consoleHstCR extends JLabel implements TableCellRenderer
{
	private String columnName;
	public consoleHstCR()
	{
		setOpaque(true);
	}
	public Component getTableCellRendererComponent(JTable table, Object value,boolean isSelected, boolean hasFocus, int row, int column)
	{
		Object ValuePrio   = table.getValueAt(row,table.getColumnModel().getColumnIndex("Prio"));
		Object ValueType   = table.getValueAt(row,table.getColumnModel().getColumnIndex("Type"));
		Object ValueStatus = table.getValueAt(row,table.getColumnModel().getColumnIndex("Status"));
		int vPrio;
		if (ValuePrio == null) vPrio = 30;
		else vPrio = (int)ValuePrio;
		String vType = (String)ValueType;
		String vStatus = (String)ValueStatus;
		if (vType == null) vType = " "; 
		if (vStatus == null) vStatus = " "; 
		if (value != null) setText(value.toString());
		else 				setText(" ");
		if(isSelected)
		{
			setBackground(table.getSelectionBackground());
			setForeground(table.getSelectionForeground());
		}
		else
		{
			setBackground(table.getBackground());
			setForeground(table.getForeground());
			setBackground(Color.lightGray);

			if (vStatus.startsWith("INFO") ) setBackground(java.awt.Color.yellow); 
			
			if (vStatus.startsWith("Timed") ) setBackground(java.awt.Color.orange); 
			if (vPrio >= 30 && !vStatus.startsWith("INFO") && !vStatus.startsWith("Timed")  && !vStatus.startsWith(" ") ) setBackground(java.awt.Color.pink); 
			if (vPrio < 30  && !vStatus.startsWith("Timed") ) setBackground(java.awt.Color.magenta); 

			if (vStatus.startsWith(" ") ) setBackground(java.awt.Color.lightGray); 
			if (vStatus.startsWith("OK") ) setBackground(java.awt.Color.green); 

		}
		return this;
	}
}

