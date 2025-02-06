public class Contact {

    final String name;
    final String phone;
    final String email;
    final String address;

    public Contact(String name, String phone, String email, String address) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
    }

    @Override
    public String toString() {
        return name + "\n" + phone + "\n" + email + "\n" + address;
    }
}


