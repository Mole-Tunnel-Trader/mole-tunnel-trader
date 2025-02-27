package com.zeki.mole_tunnel_db.repository

import com.zeki.mole_tunnel_db.entity.Algorithm
import org.springframework.data.jpa.repository.JpaRepository

interface AlgorithmRepository : JpaRepository<Algorithm, Long> {
}