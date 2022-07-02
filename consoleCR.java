package Jvakt;
/*
 * 2022-07-02 V.54 Michael Ekdal		Added getVersion() to get at consistent version throughout all classes.
 */


import javax.swing.*;
import java.awt.*;
import javax.swing.table.*;

class consoleCR extends JLabel implements TableCellRenderer
{
//	private String columnName;
	static final long serialVersionUID = 50L;
	public consoleCR()
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
		else vPrio = (Integer)ValuePrio;
//		else vPrio = (int)ValuePrio;
		String vType = (String)ValueType;
		String vStatus = (String)ValueStatus;
		if (vType == null) vType = " "; 
		if (vStatus == null) vStatus = " "; 
		if (value != null) setText(value.toString());
		else 				setText(" ");
		
//		System.out.println("getFont :" + getFont());		 new javax.swing.plaf.FontUIResource("Serif"
//		setFont(new Font(Font.SERIF, Font.BOLD, table.getRowHeight()-4));
		setFont(new javax.swing.plaf.FontUIResource("Dialog", Font.BOLD, table.getRowHeight()-3));
//		System.out.println("rowHeight :" + table.getRowHeight());		 

		
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

			if (vStatus.startsWith("T") ) setBackground(java.awt.Color.orange); 
			if (vPrio >= 30 && !vStatus.startsWith("INFO") && !vStatus.startsWith("T")  && !vStatus.startsWith(" ") ) setBackground(java.awt.Color.pink); 
//			if (vPrio < 30  && !vStatus.startsWith("T") ) setBackground(java.awt.Color.magenta); 
			if (vPrio < 30 ) setBackground(java.awt.Color.magenta); 

			if (vStatus.startsWith(" ") ) setBackground(java.awt.Color.lightGray); 
			if (vStatus.startsWith("OK") ) setBackground(java.awt.Color.green); 

		}
		return this;
	}
}

