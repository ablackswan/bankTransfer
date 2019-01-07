# Transfer with event sourcing AKKA

simple example of account transfer with event sourcing

### Prerequisites
maven
java8


## Running the tests
see TransferTest.class

## Running server
main class in QuickStartServer.class

## Example tests
get account information
get http://localhost:8080/transfer/accounts/IBAN1  (IBAN1 is an example of account number)

receive a transfer from an external account
post http://localhost:8080/transfer/receive
"{\"accountId\": \"IBAN1\", \"amount\": 200}"

send a transfer from an account
post http://localhost:8080/transfer/send
"{\"accountId\": \"IBAN1\", \"amount\": 200}"