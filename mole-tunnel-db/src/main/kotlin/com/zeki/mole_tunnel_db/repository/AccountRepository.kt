package com.zeki.mole_tunnel_db.repository

import com.zeki.common.em.TradeMode
import com.zeki.mole_tunnel_db.entity.Account
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface AccountRepository : JpaRepository<Account, Long> {
    fun findByAccountTypeAndAccountName(accountType: TradeMode, accountName: String): Optional<Account>
}