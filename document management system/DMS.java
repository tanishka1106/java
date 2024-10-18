package com.groot.controlstmtex;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.HashMap;

public class DMS {

    private static HashMap<String, String> documentMap = new HashMap<>();
    private static final String USERNAME = "root";
    private static final String PASSWORD = "1111";
    private static Connection con = null;

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/crt", USERNAME, PASSWORD);
        } catch (Exception e) {
            e.printStackTrace(); // Print the stack trace for debugging
            return; // Exit if connection fails
        }

        JFrame frame = new JFrame("Document Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        JTabbedPane tabbedPane = new JTabbedPane();

        // Document Upload Section
        JPanel uploadPanel = new JPanel();
        uploadPanel.setLayout(new BorderLayout());
        String[] documentTypes = {"PDF", "Word Document", "Excel Spreadsheet", "PowerPoint Presentation"};
        JComboBox<String> uploadComboBox = new JComboBox<>(documentTypes);
        JTextField documentNameField = new JTextField(20);
        JButton uploadButton = new JButton("Upload Document");
        JTextArea uploadTextArea = new JTextArea();
        uploadTextArea.setEditable(false);

        uploadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String type = (String) uploadComboBox.getSelectedItem();
                String name = documentNameField.getText().trim();

                if (name.isEmpty()) {
                    uploadTextArea.setText("Please provide a document name.");
                    return;
                }

                JFileChooser fileChooser = new JFileChooser();
                int returnValue = fileChooser.showOpenDialog(null);

                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    String file_path = selectedFile.getAbsolutePath();

                    // Store the document name and file path in the HashMap
                    documentMap.put(name, file_path);
                    uploadTextArea.setText("Uploaded: " + name + "\nFile Path: " + file_path + "\nDocument Type: " + type);

                    try {
                        String insertSQL = "INSERT INTO documents (name, file_path, type) VALUES (?, ?, ?)";
                        PreparedStatement ps = con.prepareStatement(insertSQL);
                        ps.setString(1, name);
                        ps.setString(2, file_path);
                        ps.setString(3, type);
                        ps.executeUpdate();
                        ps.close(); // Close PreparedStatement
                    } catch (Exception e2) {
                        uploadTextArea.setText("Error saving to database: " + e2.getMessage());
                        e2.printStackTrace(); // Print stack trace for debugging
                    }
                } else {
                    uploadTextArea.setText("No document selected.");
                }
            }
        });

        JPanel uploadInputPanel = new JPanel();
        uploadInputPanel.add(new JLabel("Document Name:"));
        uploadInputPanel.add(documentNameField);
        uploadInputPanel.add(uploadComboBox);
        uploadPanel.add(uploadInputPanel, BorderLayout.NORTH);
        uploadPanel.add(uploadButton, BorderLayout.CENTER);
        uploadPanel.add(new JScrollPane(uploadTextArea), BorderLayout.SOUTH);
        tabbedPane.addTab("Upload Document", uploadPanel);

        // Other panels (View, List, Search) remain unchanged...
        
        // Document View Section
        JPanel viewPanel = new JPanel();
        viewPanel.setLayout(new BorderLayout());
        JComboBox<String> viewComboBox = new JComboBox<>();
        JButton viewButton = new JButton("View Document");
        JTextArea viewTextArea = new JTextArea();
        viewTextArea.setEditable(false);

        viewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedDocument = (String) viewComboBox.getSelectedItem();
                if (selectedDocument != null && documentMap.containsKey(selectedDocument)) {
                    String filePath = documentMap.get(selectedDocument);
                    viewTextArea.setText("Document Name: " + selectedDocument + "\nFile Path: " + filePath);
                } else {
                    viewTextArea.setText("No document selected.");
                }
            }
        });

        viewPanel.add(viewComboBox, BorderLayout.NORTH);
        viewPanel.add(viewButton, BorderLayout.CENTER);
        viewPanel.add(new JScrollPane(viewTextArea), BorderLayout.SOUTH);
        tabbedPane.addTab("View Document", viewPanel);
        
        
        
        // Document List Section
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BorderLayout());
        JTextArea listTextArea = new JTextArea("List of uploaded documents will appear here.");
        listTextArea.setEditable(false);
        listPanel.add(new JScrollPane(listTextArea), BorderLayout.CENTER);
        tabbedPane.addTab("Document List", listPanel);

        // Update the document list when a new document is uploaded
        uploadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                StringBuilder documentList = new StringBuilder("List of uploaded documents:\n");
                viewComboBox.removeAllItems(); // Clear existing items in the view combo box

                for (String docName : documentMap.keySet()) {
                    documentList.append("- ").append(docName).append("\n");
                    viewComboBox.addItem(docName); // Add document names to the view combo box
                }

                listTextArea.setText(documentList.toString());
            }
        });

        // Search Section
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BorderLayout());
        JPanel searchInputPanel = new JPanel(); // To hold search field and button

        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        JTextArea searchResultArea = new JTextArea();
        searchResultArea.setEditable(false);

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String searchTerm = searchField.getText().trim();
                if (documentMap.containsKey(searchTerm)) {
                    String filePath = documentMap.get(searchTerm);
                    searchResultArea.setText("Document found: " + searchTerm + "\nFile Path: " + filePath);
                } else {
                    searchResultArea.setText("No document found with the name: " + searchTerm);
                }
            }
        });

        searchInputPanel.add(searchField);
        searchInputPanel.add(searchButton);
        searchPanel.add(searchInputPanel, BorderLayout.NORTH);
        searchPanel.add(new JScrollPane(searchResultArea), BorderLayout.CENTER);
        tabbedPane.addTab("Search Document", searchPanel);

        frame.getContentPane().add(tabbedPane);
        frame.setVisible(true);
    }
}

