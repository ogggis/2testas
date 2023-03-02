package lt.code.academy;

import java.util.Scanner;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

public class MoneyTransfer {
    private static final String DATABASE_NAME = "money_transfer";
    private static final String USER_COLLECTION_NAME = "users";
    private static final String TRANSFER_COLLECTION_NAME = "transfers";

    private static MongoClient mongoClient;
    private static MongoDatabase database;
    private static MongoCollection<Document> userCollection;
    private static MongoCollection<Document> transferCollection;

    public static void main(String[] args) {
        mongoClient = MongoClients.create();
        database = mongoClient.getDatabase(DATABASE_NAME);
        userCollection = database.getCollection(USER_COLLECTION_NAME);
        transferCollection = database.getCollection(TRANSFER_COLLECTION_NAME);

        Scanner scanner = new Scanner(System.in);

        int choice = -1;
        while (choice != 0) {
            System.out.println("Choose an option:");
            System.out.println("1. Register user");
            System.out.println("2. Transfer money");
            System.out.println("0. Exit");

            choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    registerUser(scanner);
                    break;
                case 2:
                    transferMoney(scanner);
                    break;
                case 0:
                    System.out.println("Exiting...");
                    break;
                default:
                    System.out.println("Invalid choice");
                    break;
            }
        }

        mongoClient.close();
    }

    private static void registerUser(Scanner scanner) {
        System.out.println("Enter user name:");
        String name = scanner.nextLine();

        System.out.println("Enter user balance:");
        double balance = scanner.nextDouble();
        scanner.nextLine();

        Document newUser = new Document("name", name)
                .append("balance", balance);
        userCollection.insertOne(newUser);
        System.out.println("User registered");
    }

    private static void transferMoney(Scanner scanner) {
        System.out.println("Enter sender name:");
        String senderName = scanner.nextLine();

        System.out.println("Enter recipient name:");
        String recipientName = scanner.nextLine();

        System.out.println("Enter transfer amount:");
        double transferAmount = scanner.nextDouble();
        scanner.nextLine();

        Document sender = userCollection.find(new Document("name", senderName)).first();
        Document recipient = userCollection.find(new Document("name", recipientName)).first();

        if (sender == null || recipient == null) {
            System.out.println("Sender or recipient not found");
            return;
        }

        double senderBalance = sender.getDouble("balance");
        if (senderBalance < transferAmount) {
            System.out.println("Insufficient funds");
            return;
        }

        double recipientBalance = recipient.getDouble("balance");

        userCollection.updateOne(sender, new Document("$set", new Document("balance", senderBalance - transferAmount)));
        userCollection.updateOne(recipient, new Document("$set", new Document("balance", recipientBalance + transferAmount)));

        Document transfer = new Document("sender", senderName)
                .append("recipient", recipientName)
                .append("amount", transferAmount);
        transferCollection.insertOne(transfer);

        System.out.println("Transfer successful. Recipient information:");
        System.out.println("Name: " + recipientName);
        System.out.println("Balance: " + (recipientBalance + transferAmount));
    }
}
