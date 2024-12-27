package com.zeki.elphago

import org.springframework.data.jpa.repository.JpaRepository

interface ElphagoRepository : JpaRepository<Elphago, Long> {
    fun findByCodeIn(stockCodeList: List<String>): List<Elphago>
}