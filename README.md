# Stock Market Simulator

Stock Market Simulator is an application that simulates financial markets, allowing users to trade stocks and other financial instruments in a virtual environment.

## Table of Contents

- Requirements
- Installation
- Database Setup
- Running the Application
- Features
- Usage
- License

## Requirements

Before running the Stock Market Simulator, ensure you have the following installed:

- Java 17 or higher
- Maven 3.6+
- SQLite (or the SQLite JDBC driver)
- Git (optional, for cloning the repository)

## Installation

1. Clone the repository:

    ```
    git clone https://github.com/yourusername/stock-market-simulator.git
    cd stock-market-simulator
    ```

2. Build the project using Maven:

    ```
    mvn clean install
    ```

3. Ensure all dependencies are installed by Maven during the build process.

## Database Setup

The application uses SQLite as the database to store user data, portfolio, and simulation states.

1. The database will be automatically created during the first run. However, if you wish to create the database manually or modify its schema, you can use the following SQL schema:

    ```
    CREATE TABLE IF NOT EXISTS users (
        user_id TEXT PRIMARY KEY,
        username TEXT NOT NULL UNIQUE,
        password TEXT NOT NULL,
        email TEXT NOT NULL
    );

    CREATE TABLE IF NOT EXISTS simulation_state (
        user_id TEXT NOT NULL,
        instrument_symbol TEXT NOT NULL,
        elapsed_time INTEGER,
        price REAL,
        quantity REAL,
        budget REAL,
        PRIMARY KEY (user_id, instrument_symbol)
    );
    ```

2. You can modify the database using an SQLite tool like DB Browser for SQLite or using the SQLite command-line interface.

## Running the Application

To run the application, use the following command:

```
java -jar target/stock-market-simulator-1.0-SNAPSHOT.jar
```

This will launch the Stock Market Simulator with a graphical user interface (GUI) for login, registration, and portfolio management.

## Features

- **User Authentication**: Users can register and log in to manage their stock portfolios.
- **Portfolio Management**: Track and manage multiple financial instruments (stocks, bonds, etc.) with real-time price updates.
- **Simulation State Saving**: Automatically saves user portfolio, including stock prices and quantities.
- **Stock Price Simulation**: Simulates stock price movements over time with graphical updates.
- **Budget Tracking**: Keep track of your virtual budget while trading.
- **SQLite Database**: User data and simulation state are stored in a lightweight, embedded SQLite database.

## Usage

1. **Login or Register**: Start by logging in or registering a new account.
2. **Portfolio Management**: After logging in, view and manage your stock portfolio. You can simulate buying and selling stocks.
3. **Simulation**: The stock price simulation will run automatically, and you can observe real-time price updates.

## Troubleshooting

If you encounter issues such as "Table not found" errors, ensure that the SQLite database has been correctly initialized. You can modify the schema in the `DatabaseManager.java` class or manually execute the SQL commands provided in the Database Setup section.

## License

This project is licensed under the MIT License. See the LICENSE file for details.

---
