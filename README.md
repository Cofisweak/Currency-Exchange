# Currency Exchange

### About project

REST API for describing currencies and exchange rates. Allows you to view and edit lists of currencies and exchange rates, and calculate the conversion of arbitrary amounts from one currency to another.

### Used technologies

1. Database: SQLite, JDBC
2. Web-server: Tomcat 10

### Database

#### Table **Currencies**

<table>
    <tr>
        <th>Column</th>
        <th>Type</th>
        <th>Description</th>
    </tr>
    <tr>
        <td>ID</td>
        <td>Int</td>
        <td>Auto Increment, Primary Key</td>
    </tr>
    <tr>
        <td>Code</td>
        <td>Varchar</td>
        <td>Unique</td>
    </tr>
    <tr>
        <td>Fullname</td>
        <td>Varchar</td>
        <td></td>
    </tr>
    <tr>
        <td>Sign</td>
        <td>Varchar</td>
        <td></td>
    </tr>
</table>

Example
<table>
    <tr>
        <th>ID</th>
        <th>Code</th>
        <th>FullName</th>
        <th>Sign</th>
     </tr>
     <tr>
        <td>1</td>
        <td>AUD</td>
        <td>Australian dollar</td>
        <td>A$</td>
    </tr>
</table>

#### Table **ExchangeRates**
<table>
    <tr>
        <th>Column</th>
        <th>Type</th>
        <th>Description</th>
    </tr>
    <tr>
        <td>ID</td>
        <td>Int</td>
        <td>Auto Increment, Primary Key</td>
    </tr>
    <tr>
        <td>BaseCurrencyId</td>
        <td>Int</td>
        <td>References to Currencies.ID</td>
    </tr>
    <tr>
        <td>TargetCurrencyId</td>
        <td>Int</td>
        <td>References to Currencies.ID</td>
    </tr>
    <tr>
        <td>Rate</td>
        <td>Decimal(6)</td>
        <td></td>
    </tr>
</table>

#### Indexes
- Unique index (BaseCurrencyId, TargetCurrencyId)

### REST API
#### Currencies

##### GET `/currencies`
```
[
    {
        "id": 0,
        "name": "United States dollar",
        "code": "USD",
        "sign": "$"
    },   
    {
        "id": 0,
        "name": "Euro",
        "code": "EUR",
        "sign": "€"
    }
]
```
Status codes:
- Success - 200
- Error - 500

##### GET `/currency/EUR`
```
{
    "id": 0,
    "name": "Euro",
    "code": "EUR",
    "sign": "€"
}
```
Status codes:
- Success - 200
- Currency code not specified - 400
- Currency not found - 404
- Error - 500

##### POST `/currencies`
```
{
    "id": 0,
    "name": "Euro",
    "code": "EUR",
    "sign": "€"
}
```
Status codes:
- Success - 200
- Any required field not specified - 400
- Currency code already exists - 409
- Error - 500

#### Exchange Rates

##### GET `/exchangeRates`
```
[
    {
        "id": 0,
        "baseCurrency": {
            "id": 0,
            "name": "United States dollar",
            "code": "USD",
            "sign": "$"
        },
        "targetCurrency": {
            "id": 1,
            "name": "Euro",
            "code": "EUR",
            "sign": "€"
        },
        "rate": 0.99
    }
]
```
Status codes:
- Success - 200
- Error - 500

##### GET `/exchangeRates/USDRUB`
```
{
    "id": 0,
    "baseCurrency": {
        "id": 0,
        "name": "United States dollar",
        "code": "USD",
        "sign": "$"
    },
    "targetCurrency": {
        "id": 1,
        "name": "Euro",
        "code": "EUR",
        "sign": "€"
    },
    "rate": 0.99
}
```
Status codes:
- Success - 200
- Any currency code not specified - 400
- Exchange rate not found - 404
- Error - 500

##### POST `/exchangeRates`
```
{
    "id": 0,
    "baseCurrency": {
        "id": 0,
        "name": "United States dollar",
        "code": "USD",
        "sign": "$"
    },
    "targetCurrency": {
        "id": 1,
        "name": "Euro",
        "code": "EUR",
        "sign": "€"
    },
    "rate": 0.99
}
```
Status codes:
- Success - 200
- Any required field not specified - 400
- Exchange rate already exists - 409
- Error - 500

##### PATCH `/exchangeRates/USDRUB`
```
{
    "id": 0,
    "baseCurrency": {
        "id": 0,
        "name": "United States dollar",
        "code": "USD",
        "sign": "$"
    },
    "targetCurrency": {
        "id": 1,
        "name": "Euro",
        "code": "EUR",
        "sign": "€"
    },
    "rate": 0.99
}
```
Status codes:
- Success - 200
- Any required field not specified - 400
- Exchange rate not found - 404
- Error - 500

#### Currency exchange

##### GET `/exchange?from=BASE_CURRENCY_CODE&to=TARGET_CURRENCY_CODE&amount=$AMOUNT`
Example `/exchange?from=USD&to=AUD&amount=10`
```
{
    "baseCurrency": {
        "id": 0,
        "name": "United States dollar",
        "code": "USD",
        "sign": "$"
    },
    "targetCurrency": {
        "id": 1,
        "name": "Australian dollar",
        "code": "AUD",
        "sign": "A€"
    },
    "rate": 1.45,
    "amount": 10.00
    "convertedAmount": 14.50
}
```
Status codes:
- Success - 200
- Any required field not specified - 400
- Exchange rate not found - 404
- Error - 500
