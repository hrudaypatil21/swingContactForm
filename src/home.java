import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;

public class Home extends JFrame {
    private JButton viewContactsButton;
    private Connection connection;

    public Home() {
        initDB();
        initComponents();
    }

    private void initDB() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/contacts_manager", "root", "Typewriter@2128");
            System.out.println("Database connected successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to connect to the database.");
        }
    }

    private boolean isValidPhoneNumber(String phone) {
        if (phone.matches(".*[a-zA-Z]+.*")) {
            JOptionPane.showMessageDialog(this, "Phone number cannot contain letters!",
                    "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (phone.length() != 10) {
            JOptionPane.showMessageDialog(this, "Phone number must contain exactly 10 digits!",
                    "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private boolean checkEmail(String email) {
        if (!email.endsWith("@gmail.com") && !email.endsWith("@yahoo.com")) {
            JOptionPane.showMessageDialog(this, "Email not in proper format.",
                    "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }


    private void initComponents() {
        setTitle("Contact Manager");
        setSize(500, 300);
        setLayout(null);

        JLabel label1 = new JLabel("Add Contact");
        label1.setBounds(200, 10, 100, 25);
        add(label1);

        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setBounds(50, 50, 100, 25);
        add(nameLabel);
        JTextField nameField = new JTextField();
        nameField.setBounds(150, 50, 200, 25);
        add(nameField);

        JLabel phoneLabel = new JLabel("Phone:");
        phoneLabel.setBounds(50, 90, 100, 25);
        add(phoneLabel);
        JTextField phoneField = new JTextField();
        phoneField.setBounds(150, 90, 200, 25);
        add(phoneField);

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setBounds(50, 130, 100, 25);
        add(emailLabel);
        JTextField emailField = new JTextField();
        emailField.setBounds(150, 130, 200, 25);
        add(emailField);

        JLabel addressLabel = new JLabel("Address:");
        addressLabel.setBounds(50, 170, 100, 25);
        add(addressLabel);
        JTextField addressField = new JTextField();
        addressField.setBounds(150, 170, 200, 25);
        add(addressField);

        JButton submitButton = new JButton("Submit");
        submitButton.setBounds(150, 210, 100, 30);
        add(submitButton);

        viewContactsButton = new JButton("View Contacts");
        viewContactsButton.setBounds(270, 210, 120, 30);
        add(viewContactsButton);

        submitButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();
            String address = addressField.getText().trim();

            if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || address.isEmpty()) {
                JOptionPane.showMessageDialog(Home.this, "All fields are required!");
                return;
            }

            if (!isValidPhoneNumber(phone) || !checkEmail(email)) {
                return;
            }

            saveContactToDB(name, phone, email, address);
            JOptionPane.showMessageDialog(Home.this, "Contact saved successfully!");

            // Clear fields after successful save
            nameField.setText("");
            phoneField.setText("");
            emailField.setText("");
            addressField.setText("");
        });


        viewContactsButton.addActionListener(e -> new ContactListFrame(connection));

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }




    private void saveContactToDB(String name, String phone, String email, String address) {
        try {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO contacts (name, phone, email, address) VALUES (?, ?, ?, ?)");
            ps.setString(1, name);
            ps.setString(2, phone);
            ps.setString(3, email);
            ps.setString(4, address);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Home();
    }
}

class ContactListFrame extends JFrame {
    private JTable contactsTable;
    private DefaultTableModel tableModel;
    Connection connection;

    public ContactListFrame(Connection connection) {
        this.connection = connection;
        setTitle("Contacts List");
        setSize(800, 400);
        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Phone", "Email", "Address"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        contactsTable = new JTable(tableModel);
        updateTable();

        JScrollPane scrollPane = new JScrollPane(contactsTable);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton editButton = new JButton("Edit");
        JButton deleteButton = new JButton("Delete");
        JButton exportButton = new JButton("Export to CSV");
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(exportButton);
        add(buttonPanel, BorderLayout.SOUTH);

        editButton.addActionListener(e -> {
            int selectedRow = contactsTable.getSelectedRow();
            if (selectedRow != -1) {
                int id = (int) tableModel.getValueAt(selectedRow, 0);
                String name = (String) tableModel.getValueAt(selectedRow, 1);
                String phone = (String) tableModel.getValueAt(selectedRow, 2);
                String email = (String) tableModel.getValueAt(selectedRow, 3);
                String address = (String) tableModel.getValueAt(selectedRow, 4);

                new EditContactDialog(this, id, name, phone, email, address);
                updateTable();
            } else {
                JOptionPane.showMessageDialog(this, "Please select a contact to edit.");
            }
        });

        contactsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(   MouseEvent e) {
                int row = contactsTable.rowAtPoint(e.getPoint());
                int column = contactsTable.columnAtPoint(e.getPoint());

                // Email column index (Assuming it's the 4th column, index 3)
                if (column == 3) {
                    String email = (String) tableModel.getValueAt(row, column);
                    openGmail(email);
                }
            }
        });

        contactsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(   MouseEvent e) {
                int row = contactsTable.rowAtPoint(e.getPoint());
                int column = contactsTable.columnAtPoint(e.getPoint());

                if (column == 4) {
                    String address = (String) tableModel.getValueAt(row, column);
                    openMaps(address);
                }
            }
        });

        deleteButton.addActionListener(e -> {
            int selectedRow = contactsTable.getSelectedRow();
            if (selectedRow != -1) {
                int id = (int) tableModel.getValueAt(selectedRow, 0);
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to delete this contact?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    deleteContact(id);
                    updateTable();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a contact to delete.");
            }
        }
        );
        exportButton.addActionListener(e -> exportToCSV());


        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void exportToCSV() {
        String filepath = "C:/Hruday/college codes/contacts.csv";
        try (FileWriter writer = new FileWriter(filepath)) {
            for (int i = 0; i < tableModel.getColumnCount(); i++) {
                writer.write(tableModel.getColumnName(i) + ",");
            }
            writer.write("\n");

            for (int row = 0; row < tableModel.getRowCount(); row++) {
                for (int col = 0; col < tableModel.getColumnCount(); col++) {
                    writer.write(tableModel.getValueAt(row, col).toString() + ",");
                }
                writer.write("\n");
            }
            JOptionPane.showMessageDialog(this, "Contacts exported to contacts.csv!");
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error exporting contacts.");
        }
    }

    private void openGmail(String email) {
        try {
            String mailtoLink = "https://mail.google.com/mail/?view=cm&fs=1&to=" + email;
            Desktop.getDesktop().browse(new URI(mailtoLink));
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to open Gmail.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openMaps(String address) {
        try {
            String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8.name());
            String mapsLink = "https://www.google.com/maps/search/" + encodedAddress;
            Desktop.getDesktop().browse(new URI(mapsLink));
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to open Maps.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void deleteContact(int id) {
        try {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM contacts WHERE id = ?");
            ps.setInt(1, id);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Contact deleted successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting contact.");
        }
    }

    private void updateTable() {
        tableModel.setRowCount(0);
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM contacts");
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("address")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

class EditContactDialog extends JDialog {
    private Connection connection;
    private int contactId;

    public EditContactDialog(ContactListFrame parent, int id, String name, String phone, String email, String address) {
        super(parent, "Edit Contact", true);
        this.connection = parent.connection;
        this.contactId = id;

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField(name, 20);
        JTextField phoneField = new JTextField(phone, 20);
        JTextField emailField = new JTextField(email, 20);
        JTextField addressField = new JTextField(address, 20);

        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        add(phoneField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        add(new JLabel("Address:"), gbc);
        gbc.gridx = 1;
        add(addressField, gbc);

        JButton saveButton = new JButton("Save");
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        add(saveButton, gbc);

        saveButton.addActionListener(e -> {
            if (nameField.getText().trim().isEmpty() || phoneField.getText().trim().isEmpty() ||
                    emailField.getText().trim().isEmpty() || addressField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required!");
                return;
            }

            String phoneNumber = phoneField.getText().trim();
            if (!isValidPhoneNumber(phoneNumber)) {
                return; 
            }

            try {
                PreparedStatement ps = connection.prepareStatement(
                        "UPDATE contacts SET name=?, phone=?, email=?, address=? WHERE id=?");
                ps.setString(1, nameField.getText());
                ps.setString(2, phoneField.getText());
                ps.setString(3, emailField.getText());
                ps.setString(4, addressField.getText());
                ps.setInt(5, contactId);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Contact updated successfully!");
                dispose();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error updating contact.");
            }
        });



        pack();
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    private boolean isValidPhoneNumber(String phone) {
        if (phone.matches(".*[a-zA-Z]+.*")) {
            JOptionPane.showMessageDialog(this, "Phone number cannot contain letters!",
                    "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        String cleanPhone = phone.replaceAll("[^0-9]", "");

        if (cleanPhone.length() != 10) {
            JOptionPane.showMessageDialog(this, "Phone number must be exactly 10 digits!",
                    "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        return true;
    }


}