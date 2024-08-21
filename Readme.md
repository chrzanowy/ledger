# Ledger
@Author: Jakub Chrzanowski
### Requirements

* Java 21

### Assumptions

* ledger and transactions are partitioned by lease id. It is possible to add user id that is assigned to multiple leases, code should be easy modifiable.
* communication is asynchronous is most cases, but app is not scalable due to synchronization lock made in ledger calculation. To run it in scalable way, we
  need to implement some kind of event sourcing or use some kind of distributed lock.
* ledger is recalculated on each event, it is not optimal, but it is simple and easy to understand.
* I had to made few compromises in db query implementation, because I had to use h2 db, which does not support some of the features that I wanted to use.
* only USD is supported, to support more currencies we need to add currency conversion service or lock lease to one currency.

### Documentation
* swagger documentation is available at http://localhost:8080/v3/api-docs
* You can generate api documentation using `./gradlew generateOpenApiDocs` and open `build/openapi.json`

### Example requests

* submit new event

```shell
POST /api/v1/events
{
    "eventType": "PAYMENT",
    "eventUuid": "{{$guid}}",
    "eventTime": "2024-04-01T10:06:02.048Z",
    "paymentUuid": "{{$guid}}",
    "paymentProcessor": "Stripe",
    "amount": 221817, // cents
    "currency": "USD",
    "leaseUuid": "922c3eba-2775-4360-b5c0-9874b11d5787",
    "status": "SUCCESS",
    "fee": 500, // cents
    "createdAt": "2024-04-01T10:06:02.048Z" // when payment occured
}
{
    "eventType": "CHARGE",
    "eventUuid": "{{$guid}}",
    "eventTime": "2024-04-01T10:00:00Z",
    "amount": 6574, // cents
    "currency": "USD",
    "dueDate": "2024-04-02",
    "feeGroup": "Rent & Fees",
    "feeType": "Base rent",
    "serviceDateStart": "2024-04-01",
    "serviceDateEnd": "2024-04-30",
    "description": "April rent",
    "leaseUuid": "922c3eba-2775-4360-b5c0-9874b11d5787"
}
{
    "eventType": "CREDIT",
    "eventUuid": "{{$guid}}",
    "eventTime": "2024-04-01T10:00:00Z",
    "amount": 1054,
    "currency": "USD",
    "effectiveOnDate": "2024-04-02",
    "feeGroup": "MARKETING",
    "feeType": "RENT_DISCOUNTS",
    "description": "April rent marketing correction",
    "leaseUuid": "922c3eba-2775-4360-b5c0-9874b11d5787"
}
```
