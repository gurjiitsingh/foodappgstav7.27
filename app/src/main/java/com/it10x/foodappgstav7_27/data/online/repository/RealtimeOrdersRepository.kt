package com.it10x.foodappgstav7_27.data.online.repository

import android.util.Log
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.it10x.foodappgstav7_27.data.online.models.OrderMasterData

class RealtimeOrdersRepository {

    private val db = FirebaseFirestore.getInstance()
    private var listener: ListenerRegistration? = null

    fun startListening(
        onNewOrder: (OrderMasterData) -> Unit
    ) {

        Log.e(
            "REALTIME_ORDER",
            "🔥 STARTING FIRESTORE LISTENER"
        )

        listener = db.collection("orderMaster")
            .whereIn("source", listOf("WEB", "APP"))
            .orderBy(
                "createdAt",
                Query.Direction.DESCENDING
            )
            .limit(15)
            .addSnapshotListener { snapshots, error ->

                if (error != null) {

                    Log.e(
                        "REALTIME_ORDER",
                        "❌ FIRESTORE LISTENER ERROR",
                        error
                    )

                    return@addSnapshotListener
                }

                if (snapshots == null) {

                    Log.e(
                        "REALTIME_ORDER",
                        "❌ SNAPSHOT IS NULL"
                    )

                    return@addSnapshotListener
                }

                Log.e(
                    "REALTIME_ORDER",
                    "📡 SNAPSHOT RECEIVED: " +
                            "documents=${snapshots.size()}, " +
                            "changes=${snapshots.documentChanges.size}"
                )

                for (change in snapshots.documentChanges) {

                    Log.d(
                        "REALTIME_ORDER",
                        "CHANGE: " +
                                "type=${change.type}, " +
                                "id=${change.document.id}"
                    )

                    if (change.type != DocumentChange.Type.ADDED) {
                        continue
                    }

                    try {

                        val order = change.document
                            .toObject(OrderMasterData::class.java)
                            .copy(
                                id = change.document.id
                            )

                        Log.e(
                            "REALTIME_ORDER",
                            "🚨 NEW ORDER DETECTED: " +
                                    "id=${order.id}, " +
                                    "source=${order.source}, " +
                                    "srno=${order.srno}, " +
                                    "createdAt=${order.createdAt}"
                        )

                        onNewOrder(order)

                    } catch (e: Exception) {

                        Log.e(
                            "REALTIME_ORDER",
                            "❌ Failed to convert order: ${change.document.id}",
                            e
                        )
                    }
                }
            }
    }

    fun stopListening() {

        Log.e(
            "REALTIME_ORDER",
            "🛑 STOPPING FIRESTORE LISTENER"
        )

        listener?.remove()
        listener = null
    }
}