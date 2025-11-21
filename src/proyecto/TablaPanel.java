import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class TablaPanel extends JPanel {
    private JTable tabla;
    private DefaultTableModel modelo;
    private JScrollPane scrollPane;
    
    public TablaPanel() {
        setLayout(new BorderLayout());
        
        modelo = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tabla = new JTable(modelo);
        tabla.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        tabla.setRowHeight(20);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        scrollPane = new JScrollPane(tabla);
        scrollPane.setPreferredSize(new Dimension(400, 300));
        add(scrollPane, BorderLayout.CENTER);
    }
    
    public void actualizarDatos(String[][] datos, String[] headers) {
        SwingUtilities.invokeLater(() -> {
            modelo.setRowCount(0);
            modelo.setColumnCount(0);
            
            if (headers != null && headers.length > 0) {
                modelo.setColumnIdentifiers(headers);
                
                if (datos != null && datos.length > 0) {
                    for (String[] fila : datos) {
                        if (fila.length == headers.length) {
                            modelo.addRow(fila);
                        } else {
                            Object[] filaAjustada = new Object[headers.length];
                            for (int i = 0; i < Math.min(fila.length, headers.length); i++) {
                                filaAjustada[i] = fila[i];
                            }
                            modelo.addRow(filaAjustada);
                        }
                    }
                }
            }
            
            ajustarAnchoColumnas();
        });
    }
    
    private void ajustarAnchoColumnas() {
        if (tabla.getColumnCount() == 0) return;
        
        for (int i = 0; i < tabla.getColumnCount(); i++) {
            int ancho = 80;
            String nombreCol = tabla.getColumnName(i);
            if (nombreCol != null) {
                ancho = Math.max(ancho, nombreCol.length() * 8);
            }
            
            for (int j = 0; j < tabla.getRowCount(); j++) {
                Object valor = tabla.getValueAt(j, i);
                if (valor != null) {
                    ancho = Math.max(ancho, valor.toString().length() * 7);
                }
            }
            
            tabla.getColumnModel().getColumn(i).setPreferredWidth(Math.min(ancho, 200));
        }
    }
    
    public JTable getTabla() {
        return tabla;
    }
}

