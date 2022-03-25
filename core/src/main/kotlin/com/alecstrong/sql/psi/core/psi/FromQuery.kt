package com.alecstrong.sql.psi.core.psi

interface FromQuery {

  fun fromQuery(): Collection<QueryElement.QueryResult>
}
