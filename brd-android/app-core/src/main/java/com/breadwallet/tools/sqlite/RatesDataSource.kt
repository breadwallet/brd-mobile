/**
 * BreadWallet
 *
 * Created by Mihail Gutan <mihail@breadwallet.com> on 9/25/15.
 * Copyright (c) 2021 Breadwinner AG
 *
 * SPDX-License-Identifier: BUSL-1.1
 */
package com.breadwallet.tools.sqlite

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.breadwallet.breadbox.BreadBox
import com.breadwallet.legacy.presenter.entities.CurrencyEntity
import com.breadwallet.tools.manager.BRReportsManager
import com.breadwallet.tools.util.BRConstants
import com.breadwallet.tools.util.Utils
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.instance
import java.util.ArrayList
import java.util.Locale

class RatesDataSource private constructor(context: Context) : BRDataSourceInterface, DIAware {

    // Database fields
    private var database: SQLiteDatabase? = null
    private val dbHelper = BRSQLiteHelper.getInstance(context)
    private val allColumns = arrayOf(
        BRSQLiteHelper.CURRENCY_CODE,
        BRSQLiteHelper.CURRENCY_NAME,
        BRSQLiteHelper.CURRENCY_RATE,
        BRSQLiteHelper.CURRENCY_ISO
    )

    override val di: DI by closestDI { context }
    private val breadBox by instance<BreadBox>()

    fun putCurrencies(currencyEntities: Collection<CurrencyEntity>): Boolean {
        if (currencyEntities.isEmpty()) {
            Log.e(TAG, "putCurrencies: failed: $currencyEntities")
            return false
        }
        return try {
            database = openDatabase()
            database!!.beginTransaction()
            var failed = 0
            for (c in currencyEntities) {
                val code = c.code.uppercase()
                val iso = c.iso.uppercase()
                val values = ContentValues()
                if (Utils.isNullOrEmpty(code) || c.rate <= 0) {
                    failed++
                    continue
                }
                values.put(BRSQLiteHelper.CURRENCY_CODE, code)
                values.put(BRSQLiteHelper.CURRENCY_NAME, c.name)
                values.put(BRSQLiteHelper.CURRENCY_RATE, c.rate)
                values.put(BRSQLiteHelper.CURRENCY_ISO, iso)
                database!!.insertWithOnConflict(
                    BRSQLiteHelper.CURRENCY_TABLE_NAME,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_REPLACE
                )
            }
            if (failed != 0) {
                Log.e(TAG, "putCurrencies: failed:$failed")
            }
            database!!.setTransactionSuccessful()
            true
        } catch (ex: Exception) {
            Log.e(TAG, "putCurrencies: failed: ", ex)
            BRReportsManager.reportBug(ex)

            //Error in between database transaction
            false
        } finally {
            database!!.endTransaction()
            closeDatabase()
        }
    }

    fun deleteAllCurrencies(app: Context?, iso: String) {
        try {
            database = openDatabase()
            database!!.delete(
                BRSQLiteHelper.CURRENCY_TABLE_NAME,
                BRSQLiteHelper.CURRENCY_ISO + " = ?",
                arrayOf(iso.uppercase())
            )
        } finally {
            closeDatabase()
        }
    }

    fun getAllCurrencies(iso: String): List<CurrencyEntity> {
        val currencies: MutableList<CurrencyEntity> = ArrayList()
        var cursor: Cursor? = null
        try {
            database = openDatabase()
            cursor = database!!.query(
                BRSQLiteHelper.CURRENCY_TABLE_NAME,
                allColumns,
                BRSQLiteHelper.CURRENCY_ISO + " = ? COLLATE NOCASE",
                arrayOf(iso.uppercase()),
                null,
                null,
                "\'" + BRSQLiteHelper.CURRENCY_CODE + "\'"
            )
            cursor.moveToFirst()
            val system = breadBox.getSystemUnsafe()
            while (!cursor.isAfterLast) {
                val curEntity = cursorToCurrency(cursor)
                val isCrypto = system?.wallets?.none {
                    it.currency.code.equals(curEntity.code, true)
                } ?: true
                if (!isCrypto) {
                    currencies.add(curEntity)
                }
                cursor.moveToNext()
            }
            // make sure to close the cursor
        } finally {
            cursor?.close()
            closeDatabase()
        }
        Log.e(TAG, "getAllCurrencies: size:" + currencies.size)
        return currencies
    }

    fun getAllCurrencyCodes(app: Context?, iso: String): List<String> {
        val ISOs: MutableList<String> = ArrayList()
        var cursor: Cursor? = null
        try {
            database = openDatabase()
            cursor = database!!.query(
                BRSQLiteHelper.CURRENCY_TABLE_NAME,
                allColumns, BRSQLiteHelper.CURRENCY_ISO + " = ? COLLATE NOCASE",
                arrayOf(iso.uppercase()),
                null, null, null
            )
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                val curEntity = cursorToCurrency(cursor)
                ISOs.add(curEntity.code)
                cursor.moveToNext()
            }
            // make sure to close the cursor
        } finally {
            cursor?.close()
            closeDatabase()
        }
        return ISOs
    }

    @Synchronized
    fun getCurrencyByCode(iso: String, code: String): CurrencyEntity? {
        var cursor: Cursor? = null
        var result: CurrencyEntity? = null
        return try {
            database = openDatabase()
            cursor = database!!.query(
                BRSQLiteHelper.CURRENCY_TABLE_NAME,
                allColumns,
                BRSQLiteHelper.CURRENCY_CODE + " = ? AND " + BRSQLiteHelper.CURRENCY_ISO + " = ? COLLATE NOCASE",
                arrayOf(code.uppercase(), iso.uppercase()),
                null,
                null,
                null
            )
            cursor.moveToNext()
            if (!cursor.isAfterLast) {
                result = cursorToCurrency(cursor)
            }
            result
        } finally {
            cursor?.close()
            closeDatabase()
        }
    }

    private fun printTest() {
        var cursor: Cursor? = null
        try {
            database = openDatabase()
            val builder = StringBuilder()
            cursor = database!!.query(
                BRSQLiteHelper.CURRENCY_TABLE_NAME,
                allColumns, null, null, null, null, null
            )
            builder.append("Total: ${cursor.count}")
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                val ent = cursorToCurrency(cursor)
                builder.append("Name: ${ent.name}, code: ${ent.code}, rate: ${ent.rate}, iso: ${ent.iso}")
                cursor.moveToNext()
            }
            Log.e(TAG, "printTest: $builder")
        } finally {
            cursor?.close()
            closeDatabase()
        }
    }

    private fun cursorToCurrency(cursor: Cursor?): CurrencyEntity {
        return CurrencyEntity(cursor!!.getString(0), cursor.getString(1), cursor.getFloat(2), cursor.getString(3))
    }

    override fun openDatabase(): SQLiteDatabase {
//        if (mOpenCounter.incrementAndGet() == 1) {
        // Opening new database
        if (database == null || !database!!.isOpen) database = dbHelper.writableDatabase
        dbHelper.setWriteAheadLoggingEnabled(BRConstants.WRITE_AHEAD_LOGGING)
        //        }
//        Log.d("Database open counter: ",  String.valueOf(mOpenCounter.get()));
        return database!!
    }

    override fun closeDatabase() {
//        if (mOpenCounter.decrementAndGet() == 0) {
//            // Closing database
//        database.close();
//onChanged
//        }
//        Log.d("Database open counter: " , String.valueOf(mOpenCounter.get()));
    }

    companion object {
        private val TAG = RatesDataSource::class.java.name
        private var instance: RatesDataSource? = null

        @JvmStatic
        @Synchronized
        fun getInstance(context: Context): RatesDataSource {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = RatesDataSource(context)
                    }
                }
            }
            return instance!!
        }
    }
}
