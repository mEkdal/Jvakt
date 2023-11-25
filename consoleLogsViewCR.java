package Jvakt;
/*
 * 2023-11-20 V.01 Michael Ekdal		New pgm to list the logs in the DB
 */


import javax.swing.*;
import java.awt.*;
import javax.swing.table.*;

class consoleLogsViewCR extends DefaultTableCellRenderer
{
	//	private String columnName;
	static final long serialVersionUID = 51L;

	public consoleLogsViewCR()
	{
		super.setOpaque(true); 
	}

	public Component getTableCellRendererComponent(JTable table, Object obj, boolean isSelected, boolean hasFocus, int row, int column)
	{
		
		Component cell = super.getTableCellRendererComponent(
				   table, obj, isSelected, hasFocus, row, column);

		setFont(new javax.swing.plaf.FontUIResource("Dialog", Font.BOLD, table.getRowHeight()-3));

		if(isSelected) {
			setBackground(table.getSelectionBackground());
//			setForeground(table.getSelectionForeground());
		}
		else {
			super.setBackground(table.getBackground());
//			super.setForeground(table.getForeground());
//			super.setBackground(Color.lightGray);
//			super.setBackground(Color.yellow);
		}

		return cell;
	}
}