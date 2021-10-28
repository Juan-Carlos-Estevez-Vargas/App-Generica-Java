/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Vista;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author Juan Carlos Estevez Vargas
 */
public class Main {

    public static void main(String[] args) {
        Frame_App frame = new Frame_App();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}

class Frame_App extends JFrame {

    // Constructor de clase
    public Frame_App() {
        this.setBounds(300, 300, 700, 700);
        Panel_App panelApp = new Panel_App();
        this.add(panelApp);
        this.setLocationRelativeTo(null);
    }
}

final class Panel_App extends JPanel {

    private final JComboBox comboTablas;
    private final JTextArea areaInformación;
    private Connection conexion;
    private FileReader archivo_entrada;

    // Constructor de clase
    public Panel_App() {
        this.setLayout(new BorderLayout());
        comboTablas = new JComboBox();
        areaInformación = new JTextArea();
        conectarBD();
        obtenerTablas();

        comboTablas.addActionListener((ActionEvent e) -> {
            String nombreTabla = (String) comboTablas.getSelectedItem();
            mostrarInformacionTabla(nombreTabla);
        });

        this.add(comboTablas, BorderLayout.NORTH);
        this.add(areaInformación, BorderLayout.CENTER);
    }

    //Método para conectar con la bd
    public void conectarBD() {
        conexion = null;
        String datos[] = new String[3];
        try {
            // Ruta del archivo de configuración donde se lee la ruta de la base de datos
            archivo_entrada = new FileReader("D:\\Downloads\\Configuracion.txt");
        } catch (IOException ex) { // En caso de no encontrar el archivo de configuración
            JFileChooser chooser = new JFileChooser(); // Ventana para escoger un archivo
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Archivos de texto", "txt"); // Filtro de archivo
            chooser.setFileFilter(filter); // Seteando el filtro al chooser

            // Guardando la selección del usuario 
            int returnVal = chooser.showOpenDialog(this);

            // Si el usuario presiona "ok"
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                try {
                    // Obteniendo la ruta del archivo seleccionado
                    archivo_entrada = new FileReader(chooser.getSelectedFile().getAbsolutePath());
                } catch (FileNotFoundException ex1) {
                    JOptionPane.showMessageDialog(null, "No se encontró la ruta del archivo de configuración");
                }
            }
        }
        try ( BufferedReader flujo_datos = new BufferedReader(archivo_entrada)) {
            for (int i = 0; i < 3; i++) {
                datos[i] = flujo_datos.readLine();
            }

            Class.forName("com.mysql.jdbc.Driver");
            conexion = DriverManager.getConnection(datos[0], datos[1], datos[2]);
        } catch (IOException | ClassNotFoundException | SQLException ex) {
            Logger.getLogger(Panel_App.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Base de datos conectada");
    }

// Método para obetener las tablas de la base de datos
    public void obtenerTablas() {
        ResultSet rs = null;
        try {
            DatabaseMetaData datosBD = conexion.getMetaData();
            rs = datosBD.getTables(null, null, null, null);

            while (rs.next()) {
                comboTablas.addItem(rs.getString("TABLE_NAME"));
            }
        } catch (SQLException ex) {
            System.err.println("Error al obtener las tablas " + ex.getMessage());
        }
    }

    public void mostrarInformacionTabla(String tabla) {
        ArrayList<String> campos = new ArrayList();
        String consultaSQL = "SELECT * FROM " + tabla;

        try {
            areaInformación.setText("");
            Statement stmt = conexion.createStatement();
            ResultSet rs = stmt.executeQuery(consultaSQL);
            ResultSetMetaData rsMetaData = rs.getMetaData();

            for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
                campos.add(rsMetaData.getColumnLabel(i));
            }

            while (rs.next()) {
                for (String nombre_campo : campos) {
                    areaInformación.append(rs.getString(nombre_campo));
                }
                areaInformación.append("\n");
            }
        } catch (SQLException ex) {
            System.err.println("Error al mostrar la información de la tabla " + ex.getMessage());
        }

    }
}
