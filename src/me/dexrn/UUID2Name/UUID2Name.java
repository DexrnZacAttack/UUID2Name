package me.dexrn.UUID2Name;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import javax.swing.JTextArea;
import org.apache.http.HttpException;
import org.apache.http.HttpStatus;
import java.io.*;
import java.net.URISyntaxException;
import java.util.*;



import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.http.client.HttpResponseException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.apache.commons.io.IOUtils;






public class UUID2Name extends JFrame implements ActionListener {
    private static final long serialVersionUID = 1L;
    private static final String UUID_URL = "https://sessionserver.mojang.com/session/minecraft/profile/%s";
    private final JFileChooser fileChooser;
    private final JTextField folderPathField;
    private final JLabel statusLabel;
    private final JButton chooseFolderButton;
    private final JButton convertButton;
    private final JTextArea console;

    public UUID2Name() {
        // create UI components
        setTitle("UUID to Name Converter");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        folderPathField = new JTextField(30);
        folderPathField.setEditable(true);

        statusLabel = new JLabel("Please choose a folder.");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

        chooseFolderButton = new JButton("Choose Folder");
        chooseFolderButton.addActionListener(this);

        convertButton = new JButton("Convert");
        convertButton.addActionListener(this);

        console = new JTextArea();
        console.setEditable(false);
        JScrollPane consoleScrollPane = new JScrollPane(console);
        consoleScrollPane.setPreferredSize(new Dimension(800, 300));

        // add components to main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        topPanel.add(folderPathField, BorderLayout.CENTER);
        topPanel.add(chooseFolderButton, BorderLayout.EAST);
        topPanel.add(statusLabel, BorderLayout.SOUTH);
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(convertButton, BorderLayout.CENTER);
        mainPanel.add(consoleScrollPane, BorderLayout.SOUTH);

        // redirect console output to text area
        System.setOut(new PrintStream(new JTextAreaOutputStream(console)));

        // show window
        setContentPane(mainPanel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    int counter = 0;

    private String getName(String uuid) {
        try {
            String url = String.format(UUID_URL, uuid);
            String json = IOUtils.toString(new URL(url), StandardCharsets.UTF_8);
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
            String name = jsonObject.get("name").getAsString();
            counter++;
            System.out.println("Loop " + counter + " completed.");
            return name;
        } catch (IOException e) {
            System.err.println("WARN: Error getting name for UUID " + uuid + ": " + e.getMessage());
            return null; // or return a default name or handle the error in some other way
        }
    }


	   





    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == chooseFolderButton) {
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFolder = fileChooser.getSelectedFile();
                folderPathField.setText(selectedFolder.getAbsolutePath());
            }
        } else if (event.getSource() == convertButton) {
            String folderPath = folderPathField.getText();
            if (folderPath.isEmpty()) {
                statusLabel.setText("Please choose a folder.");
                System.out.println("ERROR: Please choose a folder.");
                return;
            }

            // Start conversion process in a separate thread
            new Thread(() -> {
            	System.out.println("Started.");
                // get list of file names in folder
                statusLabel.setText("Getting all filenames in folder...");
                System.out.println("Getting all filenames in folder...");
                File folder = new File(folderPath);
                File[] files = folder.listFiles();
                if (files == null) {
                    statusLabel.setText("No files found in folder " + folderPath);
                    System.out.println("ERROR: No files found in folder " + folderPath);
                    return;
                }
                List<String> uuids = new ArrayList<>();
                for (File file : files) {
                    String name = file.getName();
                    if (name.endsWith(".dat")) {
                        name = name.substring(0, name.length() - 4);
                        uuids.add(name);
                    }
                }

                // convert UUIDs to names
                statusLabel.setText("Converting UUIDs to names...");
                System.out.println("Converting UUIDs to names...");
                List<String> names = new ArrayList<>();
                for (String uuid : uuids) {
                    try {
                        String name = getName(uuid);
                        if (name == null) {
                            continue;
                        }
                        names.add(name);
                    } catch (Exception e) {
                        System.err.println("WARN: Error getting name for UUID " + uuid + ": " + e.getMessage());
                    }
                }


                // save names to file
                statusLabel.setText("Saving names to file...");
                System.out.println("Saving names to file...");
                String outputPath = folderPath + "/names.txt";
                File outputFile = new File(outputPath);
                try {
                    FileWriter writer = new FileWriter(outputFile);
                    for (String name : names) {
                    	System.out.println(name + "\n");
                        writer.write(name + "\n");
                    }
                    writer.close();
                } catch (IOException e) {
                    statusLabel.setText("Error saving names to file: " + e.getMessage());
                    System.out.println("ERROR: Error saving names to file: " + e.getMessage());
                    return;
                }

                statusLabel.setText("Complete! Names saved to " + outputPath);
                System.out.println("Complete! Names saved to " + outputPath);
            }).start();
        }
    }
    


    public static void main(String[] args) {
        new UUID2Name();
    }
}