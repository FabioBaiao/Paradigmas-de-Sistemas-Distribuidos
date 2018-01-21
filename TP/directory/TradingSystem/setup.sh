curl -X PUT 0.0.0.0:8080/companies/cenas1
curl -X PUT 0.0.0.0:8080/companies/cenas2
curl -X PUT 0.0.0.0:8080/companies/cenas3

curl -X PUT 0.0.0.0:8080/companies/cenas1/exchange/qwe
curl -X PUT 0.0.0.0:8080/companies/cenas2/exchange/rty
curl -X PUT 0.0.0.0:8080/companies/cenas3/exchange/iop

curl -H 'Content-Type: application/json' -X PUT -d '{"openingPrice":1,"closingPrice":2,"minPrice":0,"maxPrice":4}' 0.0.0.0:8080/companies/cenas1/current
