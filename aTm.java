package ccp2;
import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.category.*;

public class aTm {
    static Scanner sc = new Scanner(System.in);

    static class User {
        String name, accountNumber;
        int pin;
        double balance, dailyWithdrawn = 0;
        ArrayList<String> history = new ArrayList<>();

        User(String name, String acc, int pin, double balance) {
            this.name = name;
            this.accountNumber = acc;
            this.pin = pin;
            this.balance = balance;
        }
    }

    static HashMap<String, User> users = new HashMap<>();
    static final double dailyLimit = 5000;

    static User currentUser = null;
    static LocalDateTime loginTime;
    static LocalDate today = LocalDate.now();

    public static void main(String[] args) {
        createAccounts();
        welcome();
        login();
        dashboard();
    }

    // Create 3 users with 5-digit account number & PIN
    static void createAccounts() {
        users.put("12345", new User("Ali", "12345", 11111, 10000));
        users.put("87654", new User("Fatima", "87654", 22222, 15000));
        users.put("54321", new User("Ahmed", "54321", 33333, 12000));
    }

    static void welcome() {
        System.out.println("SMART ATM SIMULATION");
    }

    // Login updated to check 5-digit PIN & acc number length
    static void login() {
        System.out.print("Enter your name: ");
        String name = sc.nextLine();
        System.out.print("Enter your 5-digit account number: ");
        String acc = sc.nextLine();

        if (acc.length() != 5 || !users.containsKey(acc)) {
            System.out.println("❌ Account not found or invalid account number.");
            System.exit(0);
        }

        User user = users.get(acc);

        if (!user.name.equalsIgnoreCase(name.trim())) {
            System.out.println("❌ Name does not match with this account.");
            System.exit(0);
        }

        int attempts = 0;
        while (true) {
            System.out.print("Enter your 5-digit PIN: ");
            String pinStr = sc.nextLine();
            if (pinStr.length() != 5) {
                System.out.println("❌ PIN must be 5 digits.");
                attempts++;
            } else {
                try {
                    int input = Integer.parseInt(pinStr);
                    if (input == user.pin) {
                        currentUser = user;
                        loginTime = LocalDateTime.now();
                        System.out.println("✅ Welcome, " + user.name + "!\n");
                        break;
                    } else {
                        System.out.println("❌ Incorrect PIN.");
                        attempts++;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("❌ PIN must be numeric.");
                    attempts++;
                }
            }
            if (attempts == 3) {
                System.out.println("❌ Too many attempts. Exiting...");
                System.exit(0);
            }
        }
    }

    // Main dashboard menu
    static void dashboard() {
        int choice;
        do {
            System.out.println("\n ATM MENU ");
            System.out.println("User: " + currentUser.name + " | Account: " + currentUser.accountNumber);
            System.out.println("1. Balance Inquiry");
            System.out.println("2. Deposit");
            System.out.println("3. Withdraw");
            System.out.println("4. Transaction History");
            System.out.println("5. Change PIN");
            System.out.println("6. Show Graph");
            System.out.println("7. Exit");
            
            System.out.print("Choose option: ");
            choice = sc.nextInt();
            sc.nextLine(); // clear buffer

            switch (choice) {
                case 1 -> checkBalance();
                case 2 -> deposit();
                case 3 -> withdrawMenu();  // updated withdraw menu
                case 4 -> showHistory();
                case 5 -> changePIN();
                case 6 -> showGraph();
                case 7 -> exitSession();
                default -> System.out.println("❌ Invalid option.");
            }
        } while (choice != 7);
    }

    static void checkBalance() {
        System.out.println("💰 Current Balance: Rs. " + currentUser.balance);
    }

    static void deposit() {
        System.out.println("Enter deposit amount in cents (e.g., 125 for Rs. 1.25) or 0 to cancel:");
        int cents = sc.nextInt();
        sc.nextLine(); // clear buffer
        if (cents == 0) {
            System.out.println("❌ Deposit cancelled.");
            return;
        } else if (cents < 0) {
            System.out.println("❌ Invalid amount.");
            return;
        }

        double amount = cents / 100.0;
        System.out.println("📩 Please insert deposit envelope now...");
        System.out.print("Press 'Y' once envelope is inserted (within 2 mins simulation): ");
        String confirm = sc.nextLine();

        if (confirm.equalsIgnoreCase("Y")) {
            currentUser.balance += amount;
            currentUser.history.add("Deposited: Rs. " + amount);
            System.out.println("✅ Rs. " + amount + " deposited successfully.");
        } else {
            System.out.println("❌ Deposit not confirmed. Transaction cancelled.");
        }
    }

    // New Withdraw Menu as per problem statement
    static void withdrawMenu() {
        int option;
        while (true) {
            System.out.println("\n Withdrawal Menu ");
            System.out.println("1. Rs. 20");
            System.out.println("2. Rs. 40");
            System.out.println("3. Rs. 60");
            System.out.println("4. Rs. 100");
            System.out.println("5. Rs. 200");
            System.out.println("6. Cancel Transaction");
         
            System.out.print("Choose withdrawal option (1-6): ");

            option = sc.nextInt();

            double withdrawAmount = 0;
            switch (option) {
                case 1 -> withdrawAmount = 20;
                case 2 -> withdrawAmount = 40;
                case 3 -> withdrawAmount = 60;
                case 4 -> withdrawAmount = 100;
                case 5 -> withdrawAmount = 200;
                case 6 -> {
                    System.out.println("Withdrawal cancelled. Returning to main menu...");
                    return; // cancel transaction → back to main dashboard
                }
                default -> {
                    System.out.println("❌ Invalid option. Please choose 1-6.");
                    continue; // ask again
                }
            }

            // Check if user has sufficient balance
            if (withdrawAmount > currentUser.balance) {
                System.out.println("❌ Insufficient balance. Please choose a smaller amount.");
                continue; // re-show withdraw menu
            }

            // Check daily withdrawal limit
            if ((currentUser.dailyWithdrawn + withdrawAmount) > dailyLimit) {
                System.out.println("❌ Daily withdrawal limit exceeded.");
                return; // back to main menu
            }

            // Process withdrawal
            currentUser.balance -= withdrawAmount;
            currentUser.dailyWithdrawn += withdrawAmount;
            currentUser.history.add("Withdrew: Rs. " + withdrawAmount);
            System.out.println("✅ Rs. " + withdrawAmount + " withdrawn successfully.");
            return; // after successful withdraw back to main menu
        }
    }

    static void showHistory() {
        System.out.println("📜 Transaction History:");
        if (currentUser.history.isEmpty()) {
            System.out.println("No transactions yet.");
        } else {
            for (String h : currentUser.history) {
                System.out.println("• " + h);
            }
        }
    }

    static void changePIN() {
        System.out.print("Enter current PIN: ");
        int current = sc.nextInt();
        sc.nextLine();
        if (current == currentUser.pin) {
            System.out.print("Enter new 5-digit PIN: ");
            String newPinStr = sc.nextLine();
            if (newPinStr.length() != 5) {
                System.out.println("❌ PIN must be 5 digits. PIN change aborted.");
                return;
            }
            try {
                int newPin = Integer.parseInt(newPinStr);
                currentUser.pin = newPin;
                System.out.println("✅ PIN changed successfully.");
            } catch (NumberFormatException e) {
                System.out.println("❌ PIN must be numeric. PIN change aborted.");
            }
        } else {
            System.out.println("❌ Incorrect current PIN.");
        }
    }

    static void showGraph() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        double totalDeposits = currentUser.history.stream()
                .filter(s -> s.startsWith("Deposited"))
                .mapToDouble(s -> Double.parseDouble(s.split("Rs. ")[1])).sum();
        double totalWithdraws = currentUser.history.stream()
                .filter(s -> s.startsWith("Withdrew"))
                .mapToDouble(s -> Double.parseDouble(s.split("Rs. ")[1])).sum();

        dataset.setValue(currentUser.balance, "Amount", "Balance");
        dataset.setValue(totalDeposits, "Amount", "Deposits");
        dataset.setValue(totalWithdraws, "Amount", "Withdrawals");

        JFreeChart chart = ChartFactory.createBarChart(
                "ATM Summary - " + currentUser.name,
                "Type",
                "Rs",
                dataset,
                PlotOrientation.VERTICAL,
                false, true, false);

        ChartFrame frame = new ChartFrame("ATM Dashboard - " + currentUser.name, chart);
        frame.setSize(600, 400);
        frame.setVisible(true);
    }

    static void exitSession() {
        LocalDateTime logoutTime = LocalDateTime.now();
        Duration session = Duration.between(loginTime, logoutTime);
        System.out.println("👋 Thank you, " + currentUser.name + "!");
        System.out.println("👤 Account: " + currentUser.accountNumber);
        System.out.println("🕒 Session Duration: " + session.toMinutes() + " minutes.");
        System.out.println("📅 Date: " + today.format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        System.exit(0);
    }
}
