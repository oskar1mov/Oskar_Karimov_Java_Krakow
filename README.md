# Oskar_Karimov_Java_Krakow

## Description
A Java application designed to optimize payment methods for a list of customer orders based on available promotions, discounts, and balance limits. It selects the most beneficial payment strategy to maximize total savings while respecting given constraints.

## Payment Logic Notes
- The payment methods parsing mechanism assumes a coupon is being assigned to every order.
- The logic assumes that discounts from PUNKTY is always higher than or equal 10%. The reason of mentioning it is that if the input will include discount for PUNKTY that is less than 10% then the partial payment ( points + a card ) might yield a better result due to the fact of applying 10% if points cover 10% or more of the order value. This case is not covered by the optimized payment plan search because it assumes the PUNKTY discount is always 10% or higher
- The JSON file parsing implementation assumes the structure of the file will remain the same without new fields. Otherwise, usage of @JsonIgnoreProperties on data objects is needed. It was not added right now to ensure the lightweight package without extra dependencies.

## Running the app

Run the application like this:

```bash
java -jar target/Oskar_Karimov_Java_Krakow-1.0-SNAPSHOT-jar-with-dependencies.jar orders.json paymentmethods.json
```

## Building the JAR yourself

If you prefer to build the JAR manually:
```bash
git clone <repo-url>
cd Oskar_Karimov_Java_Krakow
mvn clean package
```
Resulting JAR will be in:

```bash
target/Oskar_Karimov_Java_Krakow-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## Tests

To run unit tests (You need to have Maven installed):
```bash
mvn test
```

