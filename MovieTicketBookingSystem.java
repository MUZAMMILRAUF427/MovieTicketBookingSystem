package movieticketbookingsystem;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class MovieTicketBookingSystem extends JFrame {
    private static final String URL = "jdbc:mysql://localhost:3306/sys";
    private static final String USER = "root";
    private static final String PASSWORD = "Muz@1234";

    private Connection connection;

    public MovieTicketBookingSystem() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database connection failed!", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        setTitle("VIP Movie Ticket Booking System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel headerLabel = new JLabel("VIP Movie Ticket Booking System", JLabel.CENTER);
        headerLabel.setFont(new Font("Serif", Font.BOLD, 28));
        headerLabel.setForeground(new Color(0, 102, 204));
        add(headerLabel, BorderLayout.NORTH);
      
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Book Tickets", createBookingPanel());
        tabbedPane.addTab("View Bookings", createViewPanel());
        tabbedPane.addTab("Cancel Booking", createCancelPanel());
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private JPanel createBookingPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 248, 255));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel movieLabel = new JLabel("Movie Name:");
        JTextField movieField = new JTextField(15);

        JLabel seatLabel = new JLabel("Seat Type:");
        JComboBox<String> seatBox = new JComboBox<>(new String[]{"Regular", "VIP", "Balcony"});

        JLabel quantityLabel = new JLabel("Quantity:");
        JTextField quantityField = new JTextField(5);

        JLabel priceLabel = new JLabel("Total Price:");
        JLabel priceValue = new JLabel("0");

        JButton calculateButton = new JButton("Calculate Price");
        calculateButton.addActionListener(e -> {
            try {
                String seatType = (String) seatBox.getSelectedItem();
                int quantity = Integer.parseInt(quantityField.getText());
                int pricePerSeat = switch (seatType) {
                    case "VIP" -> 500;
                    case "Balcony" -> 300;
                    default -> 200;
                };
                priceValue.setText(String.valueOf(pricePerSeat * quantity));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input. Please enter a valid quantity.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton bookButton = new JButton("Book Ticket");
        bookButton.addActionListener(e -> {
            try {
                String movieName = movieField.getText();
                String seatType = (String) seatBox.getSelectedItem();
                int quantity = Integer.parseInt(quantityField.getText());
                double totalPrice = Double.parseDouble(priceValue.getText());

                String query = "INSERT INTO movie_tickets (movie_name, seat_type, quantity, total_price) VALUES (?, ?, ?, ?)";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, movieName);
                preparedStatement.setString(2, seatType);
                preparedStatement.setInt(3, quantity);
                preparedStatement.setDouble(4, totalPrice);

                preparedStatement.executeUpdate();
                JOptionPane.showMessageDialog(this, "Ticket booked successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error booking ticket: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

    
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(movieLabel, gbc);

        gbc.gridx = 1;
        panel.add(movieField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(seatLabel, gbc);

        gbc.gridx = 1;
        panel.add(seatBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(quantityLabel, gbc);

        gbc.gridx = 1;
        panel.add(quantityField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(priceLabel, gbc);

        gbc.gridx = 1;
        panel.add(priceValue, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(calculateButton, gbc);

        gbc.gridx = 1;
        panel.add(bookButton, gbc);

        return panel;
    }

   
    private JPanel createViewPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        DefaultTableModel tableModel = new DefaultTableModel(new String[]{"Ticket ID", "Movie Name", "Seat Type", "Quantity", "Total Price"}, 0);
        JTable table = new JTable(tableModel);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> {
            try {
                tableModel.setRowCount(0); 
                String query = "SELECT * FROM movie_tickets";
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);
                while (resultSet.next()) {
                    tableModel.addRow(new Object[]{
                            resultSet.getInt("ticket_id"),
                            resultSet.getString("movie_name"),
                            resultSet.getString("seat_type"),
                            resultSet.getInt("quantity"),
                            resultSet.getDouble("total_price")
                    });
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error loading bookings: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(refreshButton, BorderLayout.SOUTH);

        return panel;
    }

   
    private JPanel createCancelPanel() {
        JPanel panel = new JPanel(new FlowLayout());

        JLabel ticketIdLabel = new JLabel("Ticket ID:");
        JTextField ticketIdField = new JTextField(10);
        JButton cancelButton = new JButton("Cancel Booking");

        cancelButton.addActionListener(e -> {
            try {
                int ticketId = Integer.parseInt(ticketIdField.getText());
                String query = "DELETE FROM movie_tickets WHERE ticket_id = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setInt(1, ticketId);

                int rowsDeleted = preparedStatement.executeUpdate();
                if (rowsDeleted > 0) {
                    JOptionPane.showMessageDialog(this, "Booking cancelled successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "No booking found with the provided Ticket ID.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error cancelling booking: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(ticketIdLabel);
        panel.add(ticketIdField);
        panel.add(cancelButton);

        return panel;
    }
    
    public static void main(String[] args) {
     SwingUtilities.invokeLater(() -> new MovieTicketBookingSystem().setVisible(true));
    }
    
}
