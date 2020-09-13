package com.alecstrong.sql.psi.core.psi

internal interface FromQuery {

  fun fromQuery(): Collection<QueryElement.QueryResult>
}
