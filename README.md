# Transaction Starter Project

This is the starter project for the Customer Transactions exercise.

## Before you start

The first thing you should do after cloning the repository is:

### Linux / macOS

```bash
./mvnw clean test
```

### Windows

```bat
mvnw.cmd clean test
```

The sample test should pass before you begin implementing the exercise.

## What is already provided

- Java 17
- Spring Boot
- Maven wrapper
- Spring Web
- Spring Data JPA
- H2 embedded database
- JUnit / Spring Boot Test
- A sample REST endpoint: `GET /api/sample`
- A sample test that loads the Spring context

## Exercise

Implement these four operations:

1. Create transaction
2. Get transaction
3. Update transaction status
4. Get all transactions for a customer


You may change the surrounding design if you believe your solution is better.

## Transaction fields

Every transaction contains:

- Transaction ID
- Customer ID
- Amount
- Currency
- Transaction Type
- Transaction Status

### Validation rules

Define what makes a transaction valid. At minimum, consider:

- Transaction ID
- Customer ID
- Amount
- Currency
- Transaction type
- Initial status

Also explain any business validation you add beyond the annotations already supplied.

1. Understanding of the Problem

-->The goal of this project was to build a backend service to handle customer transactions. Since the starter project was already set up with Spring Boot, Maven, and an H2 database, my main job was to write the actual business logic inside it. The application takes in transaction data in JSON format, checks if the data is valid, saves it to the embedded H2 database, and makes sure transaction statuses are updated correctly. I organized my code into Controller, Service, and Repository layers. This way, the web stuff, the business rules, and the database logic are all kept neatly separated.

2. Assumptions Made

-->Database: Since the starter code came with an H2 in-memory database, I assumed it was perfectly fine that the data gets wiped every time I stop the server. I didn't try to connect a real external database to keep the setup simple.
Security: I didn't add any login requirements, passwords, or security tokens. I assumed this API is just meant to be tested locally and that security wasn't the main focus of this assignment.
Currencies: For the currency field, I assumed users will always send standard 3-letter codes (like "USD" or "EUR"), so I just set up my validation to check that the string is exactly 3 characters long.

3. Validation Rules Enforced

-->I set up several layers of validation to make sure bad data never messes up the database. 

First, I used Jakarta annotations (like `@NotBlank` and `@NotNull`) on my DTOs to catch basic mistakes right at the door. The transaction ID and customer ID cannot be blank, and the amount has to be at least 0.01 (to prevent negative or zero transactions). If a user sends bad JSON, the system automatically rejects it with a 400 Bad Request. 

Second, I handle database checks in my Service layer. Before creating a new transaction, my code checks if that ID already exists. If it does, it returns a `409 Conflict` so we don't overwrite anything. Also, if someone tries to fetch or update a transaction that isn't in the database at all, they get a standard 404 Not Found.

The most important business rules I added were for the transaction statuses. When a new transaction is created, my code forces it to be PENDING users can't cheat and pass in a "completed" status from the start. Later on, it can be updated to COMPLETED, FAILED, or REJECTED. But once it hits one of those final three states, it is locked forever. If someone tries to update a locked transaction, my service layer catches it and returns a 400 Bad Request with an "Invalid state transition" message.

4. API Endpoints Built
-->POST /api/transactions-Creates a new transaction.
GET /api/transactions/{id}- Gets one specific transaction. It returns a `404 Not Found` if the ID doesn't exist.
PATCH /api/transactions/{id}/status- Updates the status of an existing transaction safely.
GET /api/transactions/customer/{customerId}- Gets all transactions for a specific customer. It returns an empty list if they don't have any.

5. How I Approached Testing

-->I used JUnit and MockMvc to write 6 automated tests, which covers the minimum 4 required scenarios and adds two extra checks. MockMvc was really helpful because it let me test the actual web endpoints exactly like a user would, but without having to start a real server. To make sure my tests don't mess each other up, I added a @BeforeEach method. This method automatically deletes everything in the database before every single test runs, giving me a perfectly clean slate every time. I made sure to test the edge cases, like what happens when someone sends a negative amount or a duplicate transaction ID. Finally, I verified that everything runs smoothly from the command line using ./mvnw clean test.

6. Known Limitations

-->Since this is a starter project, there are a couple of things I would do differently for a real-world application. First, because I used the in-memory H2 database, all the transaction data is lost whenever the server restarts. If I had more time, I would connect this to a permanent database like PostgreSQL. 
Second, the API is completely open right now. If this were a real product, I would add Spring Security with JWT tokens so users actually have to log in, and to make sure people can only see their own transactions.

7. Future Improvements

-->If I were to take this project further, 
the first thing I would do is connect a real database like PostgreSQL so the data saves permanently. 
I would also want to add a login system using Spring Security so that the API isn't just open to everyone. 
Finally, right now the "get customer transactions" endpoint fetches everything at once. I would like to learn how to add pagination to it, 
so if a customer has thousands of transactions, it only loads a few at a time and doesn't slow down the system. Adding Swagger for a nice web-based API documentation page.





## API skeleton

### Create

`POST /api/transactions`

Example Request Body:

```json
{
  "transactionId": "txn-001",
  "customerId": "cust-100",
  "amount": 150.75,
  "currency": "USD",
  "type": "DEPOSIT"
}
```

Example Response (`201 Created`):

```json
{
  "transactionId": "txn-001",
  "customerId": "cust-100",
  "amount": 150.75,
  "currency": "USD",
  "type": "DEPOSIT",
  "status": "PENDING",
  "createdAt": "2026-09-01T03:46:00",
  "updatedAt": "2026-09-01T03:46:00"
}
```

### Get

`GET /api/transactions/{id}`

Example Response (`200 OK`):

```json
{
  "transactionId": "txn-001",
  "customerId": "cust-100",
  "amount": 150.75,
  "currency": "USD",
  "type": "DEPOSIT",
  "status": "PENDING",
  "createdAt": "2026-09-01T03:46:00",
  "updatedAt": "2026-09-01T03:46:00"
}
```

### Update status

`PATCH /api/transactions/{id}/status`

Example Request Body:

```json
{
  "status": "COMPLETED"
}
```

Example Response (`200 OK`):

```json
{
  "transactionId": "txn-001",
  "customerId": "cust-100",
  "amount": 150.75,
  "currency": "USD",
  "type": "DEPOSIT",
  "status": "COMPLETED",
  "createdAt": "2026-09-01T03:46:00",
  "updatedAt": "2026-09-01T03:46:05"
}
```

### Get customer transactions

`GET /api/transactions/customer/{customerId}`

Example Response (`200 OK`):

```json
[
  {
    "transactionId": "txn-001",
    "customerId": "cust-100",
    "amount": 150.75,
    "currency": "USD",
    "type": "DEPOSIT",
    "status": "COMPLETED",
    "createdAt": "2026-09-01T03:46:00",
    "updatedAt": "2026-09-01T03:46:05"
  }
]
```


## Testing expectations

8. Test Run Output

```text
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.example.transactionstarter.controller.TransactionControllerIntegrationTest
2026-09-01T03:36:09.988+05:30  INFO 33696 --- [main] c.e.t.controller.TransactionController   : POST /api/transactions - transactionId=txn-001
2026-09-01T03:36:09.989+05:30  INFO 33696 --- [main] c.e.t.service.TransactionServiceImpl     : Creating transaction with id: txn-001
2026-09-01T03:36:10.661+05:30  INFO 33696 --- [main] c.e.t.service.TransactionServiceImpl     : Transaction created successfully: txn-001
2026-09-01T03:36:10.885+05:30  INFO 33696 --- [main] c.e.t.controller.TransactionController   : PATCH /api/transactions/txn-term/status - newStatus=COMPLETED
2026-09-01T03:36:10.886+05:30  INFO 33696 --- [main] c.e.t.service.TransactionServiceImpl     : Updating status of transaction txn-term to COMPLETED
2026-09-01T03:36:10.909+05:30  INFO 33696 --- [main] c.e.t.service.TransactionServiceImpl     : Transaction txn-term status updated: PENDING -> COMPLETED
2026-09-01T03:36:10.915+05:30  INFO 33696 --- [main] c.e.t.controller.TransactionController   : PATCH /api/transactions/txn-term/status - newStatus=FAILED
2026-09-01T03:36:10.915+05:30  INFO 33696 --- [main] c.e.t.service.TransactionServiceImpl     : Updating status of transaction txn-term to FAILED
2026-09-01T03:36:10.936+05:30  INFO 33696 --- [main] c.e.t.controller.TransactionController   : POST /api/transactions - transactionId=txn-dup
2026-09-01T03:36:10.937+05:30  INFO 33696 --- [main] c.e.t.service.TransactionServiceImpl     : Creating transaction with id: txn-dup
2026-09-01T03:36:10.942+05:30  INFO 33696 --- [main] c.e.t.service.TransactionServiceImpl     : Transaction created successfully: txn-dup
2026-09-01T03:36:10.947+05:30  INFO 33696 --- [main] c.e.t.controller.TransactionController   : POST /api/transactions - transactionId=txn-dup
2026-09-01T03:36:10.948+05:30  INFO 33696 --- [main] c.e.t.service.TransactionServiceImpl     : Creating transaction with id: txn-dup
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 10.40 s -- in com.example.transactionstarter.controller.TransactionControllerIntegrationTest
[INFO] Running com.example.transactionstarter.TransactionStarterApplicationTests
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.154 s -- in com.example.transactionstarter.TransactionStarterApplicationTests
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  16.452 s
[INFO] Finished at: 2026-09-01T03:36:12+05:30
[INFO] ------------------------------------------------------------------------
```
