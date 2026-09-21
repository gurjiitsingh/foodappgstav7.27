package com.it10x.foodappgstav7_27.fiscal

import com.it10x.foodappgstav7_27.fiskaly.GermanyFiscalService
import com.it10x.foodappgstav7_27.fiskaly.IndiaFiscalService
import com.it10x.foodappgstav7_27.fiskaly.SpainFiscalService
import com.it10x.foodappgstav7_27.fiskaly.FiskalyRepository

fun getFiscalService(
    countryCode: String,
    fiskalyRepository: FiskalyRepository? = null
): FiscalService {

    return when (countryCode) {
        "DE" -> GermanyFiscalService(fiskalyRepository!!)
        "IN" -> IndiaFiscalService()
        "ES" -> SpainFiscalService()
        else -> IndiaFiscalService()
    }
}