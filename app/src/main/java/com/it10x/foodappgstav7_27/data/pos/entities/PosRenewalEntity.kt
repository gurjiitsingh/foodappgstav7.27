package com.it10x.foodappgstav7_27.data.pos.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pos_renewal")
data class PosRenewalEntity(

    @PrimaryKey
    val id: Int = 1,

    // Last successful Firestore renewal check
    val updatedAt: Long = System.currentTimeMillis()
)