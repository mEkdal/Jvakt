package Jvakt;


import javax.swing.*;
import java.awt.*;
import javax.swing.table.*;

class consoleCR extends JLabel implements TableCellRenderer
    {
     private String columnName;
//     public consoleCR(String column)
     public consoleCR()
         {
//         this.columnName = column;
         setOpaque(true);
     }
     public Component getTableCellRendererComponent(JTable table, Object value,boolean isSelected, boolean hasFocus, int row, int column)
         {
         Object ValuePrio   = table.getValueAt(row,table.getColumnModel().getColumnIndex("Prio"));
         Object ValueType   = table.getValueAt(row,table.getColumnModel().getColumnIndex("Type"));
         Object ValueStatus = table.getValueAt(row,table.getColumnModel().getColumnIndex("Status"));
//         System.out.println("ValueType " + ValueType);
         int vPrio;
         if (ValuePrio == null) vPrio = 3;
         else vPrio = (int)ValuePrio;
//         System.out.println("vPrio " + vPrio);
         String vType = (String)ValueType;
         String vStatus = (String)ValueStatus;
//         if (vPrio == null) vPrio = 0; 
         if (vType == null) vType = " "; 
         if (vStatus == null) vStatus = " "; 
//         System.out.println("vR " + ValueType);
         
//         if (value != null) setText(value.toString()+ " " + columnName+ " " + columnValue2);
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
             
//             if (columnValue.equals("1")) setBackground(java.awt.Color.pink);
//             if (columnValue.equals("2")) setBackground(java.awt.Color.green);
//               if (vType.startsWith("I") && vStatus.startsWith("INFO") ) setBackground(Color.lightGray); 
               if (vStatus.startsWith("Timed") ) setBackground(java.awt.Color.orange); 
               if (vPrio >= 3 && !vStatus.startsWith("INFO") && !vStatus.startsWith("Timed")  && !vStatus.startsWith(" ") ) setBackground(java.awt.Color.pink); 
//               if (vPrio >= 3  ) setBackground(java.awt.Color.pink); 
               if (vPrio < 3  && !vStatus.startsWith("Timed") ) setBackground(java.awt.Color.magenta); 
//             if (columnValue.equals("4")) setBackground(java.awt.Color.LIGHT_GRAY);
            
         }
         return this;
     }
}

