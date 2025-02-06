import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

public class home extends JFrame {
    private List<Contact> contactList = new ArrayList<>();
    private JButton viewContactsButton;

    public home() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Contact Manager");
        setSize(500, 300);
        setLayout(null);

        JLabel label1 = new JLabel("Add Contact");
        label1.setBounds(200, 10, 100, 25);
        add(label1);

        JLabel label2 = new JLabel("Name:");
        label2.setBounds(50, 50, 100, 25);
        add(label2);
        JTextField textField1 = new JTextField();
        textField1.setBounds(150, 50, 200, 25);
        add(textField1);

        JLabel label3 = new JLabel("Phone:");
        label3.setBounds(50, 90, 100, 25);
        add(label3);
        JTextField textField2 = new JTextField();
        textField2.setBounds(150, 90, 200, 25);
        add(textField2);

        JLabel label4 = new JLabel("Email:");
        label4.setBounds(50, 130, 100, 25);
        add(label4);
        JTextField textField3 = new JTextField();
        textField3.setBounds(150, 130, 200, 25);
        add(textField3);

        JLabel label5 = new JLabel("Address:");
        label5.setBounds(50, 170, 100, 25);
        add(label5);
        JTextField textField4 = new JTextField();
        textField4.setBounds(150, 170, 200, 25);
        add(textField4);

        JButton button1 = new JButton("Submit");
        button1.setBounds(150, 210, 100, 30);
        add(button1);

        viewContactsButton = new JButton("View Contacts");
        viewContactsButton.setBounds(270, 210, 120, 30);
        add(viewContactsButton);

        button1.addActionListener(e -> {
            if(textField1.getText().trim().isEmpty() || textField2.getText().trim().isEmpty() ||
                    textField3.getText().trim().isEmpty() || textField4.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(home.this, "All fields required");
            } else {
                String name = textField1.getText();
                String phone = textField2.getText();
                String email = textField3.getText();
                String address = textField4.getText();

                Contact contact = new Contact(name, phone, email, address);
                contactList.add(contact);

                JOptionPane.showMessageDialog(this, "Contact saved successfully!");
                textField1.setText("");
                textField2.setText("");
                textField3.setText("");
                textField4.setText("");
            }
        });

        viewContactsButton.addActionListener(e -> new ContactListFrame(contactList));

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }
}

class ContactListFrame extends JFrame {
    private JTable contactsTable;
    private DefaultTableModel tableModel;
    private List<Contact> contactList;

    public ContactListFrame(List<Contact> contactList) {
        this.contactList = contactList;
        setTitle("Contacts List");
        setSize(500, 300);
        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(new String[]{"Name", "Phone", "Email", "Address"}, 0);
        contactsTable = new JTable(tableModel);
        updateTable();
        add(new JScrollPane(contactsTable), BorderLayout.CENTER);

        JButton deleteButton = new JButton("Delete Contact");
        deleteButton.addActionListener(e -> deleteSelectedContact());
        add(deleteButton, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void updateTable() {
        tableModel.setRowCount(0);
        for (Contact contact : contactList) {
            tableModel.addRow(new Object[]{contact.name, contact.phone, contact.email, contact.address});
        }
    }

    private void deleteSelectedContact() {
        int selectedRow = contactsTable.getSelectedRow();
        if (selectedRow != -1) {
            contactList.remove(selectedRow);
            updateTable();
            JOptionPane.showMessageDialog(this, "Contact deleted successfully!");
        } else {
            JOptionPane.showMessageDialog(this, "Please select a contact to delete.");
        }
    }
}


