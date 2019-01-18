package Jvakt;


import javax.swing.*;
import java.awt.*;
import javax.swing.table.*;

class consoleStsCR extends JLabel implements TableCellRenderer
{
	private String columnName;
	public consoleStsCR()
	{
		setOpaque(true);
	}
	public Component getTableCellRendererComponent(JTable table, Object value,boolean isSelected, boolean hasFocus, int row, int column)
	{
		Object ValueState  = table.getValueAt(row,table.getColumnModel().getColumnIndex("state"));
		Object ValuePrio   = table.getValueAt(row,table.getColumnModel().getColumnIndex("prio"));
		Object ValueType   = table.getValueAt(row,table.getColumnModel().getColumnIndex("type"));
		Object ValueStatus = table.getValueAt(row,table.getColumnModel().getColumnIndex("status"));
		int vPrio;
		if (ValuePrio == null) vPrio = 30;
		else vPrio = (Integer)ValuePrio;
		String vState = (String)ValueState;
		String vType = (String)ValueType;
		String vStatus = (String)ValueStatus;
		if (vType == null) vType = " "; 
		if (vStatus == null) vStatus = " "; 
		if (value != null) setText(value.toString());
		else 				setText(" ");
		
		setFont(new javax.swing.plaf.FontUIResource("Dialog", Font.BOLD, table.getRowHeight()-3));
		
		if(isSelected)
		{
			setBackground(table.getSelectionBackground());
			setForeground(table.getSelectionForeground());
		}
		else
		{
			setBackground(table.getBackground());
			setForeground(table.getForeground());
			setBackground(Color.WHITE);

//			if (vStatus.startsWith("INFO") ) setBackground(java.awt.Color.yellow); 
			
//			if (vStatus.startsWith("T") ) setBackground(java.awt.Color.orange); 
//			if (vPrio >= 30 ) 				setBackground(java.awt.Color.yellow); 
			if (vPrio < 30 && vPrio > 10 ) 	setBackground(java.awt.Color.pink); 
			if (vPrio <= 10 ) 				setBackground(java.awt.Color.magenta); 

			try{if (!vState.startsWith("A") ) setBackground(java.awt.Color.lightGray); } catch(NullPointerException npe2) {}
			try{if (vType.startsWith("D") ) setBackground(java.awt.Color.yellow); } catch(NullPointerException npe2) {}
//			if (vStatus.startsWith("OK") ) setBackground(java.awt.Color.green); 

		}
		return this;
	}
}

