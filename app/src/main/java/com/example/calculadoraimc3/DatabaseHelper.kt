package com.example.calculadoraimc3

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class BMIRecord(
    val id: Long = 0,
    val userName: String,
    val height: Double,
    val weight: Double,
    val bmi: Double,
    val dateTime: String
)

data class User(
    val id: Long = 0,
    val username: String,
    val email: String
)

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "bmi_database.db"
        private const val DATABASE_VERSION = 2

        // Tabla de registros BMI
        private const val TABLE_BMI_RECORDS = "bmi_records"
        private const val COLUMN_ID = "id"
        private const val COLUMN_USER_NAME = "user_name"
        private const val COLUMN_HEIGHT = "height"
        private const val COLUMN_WEIGHT = "weight"
        private const val COLUMN_BMI = "bmi"
        private const val COLUMN_DATE_TIME = "date_time"

        // Tabla de usuarios
        private const val TABLE_USERS = "users"
        private const val COLUMN_USER_ID = "user_id"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_EMAIL = "email"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createBMITable = """
            CREATE TABLE $TABLE_BMI_RECORDS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_NAME TEXT NOT NULL,
                $COLUMN_HEIGHT REAL NOT NULL,
                $COLUMN_WEIGHT REAL NOT NULL,
                $COLUMN_BMI REAL NOT NULL,
                $COLUMN_DATE_TIME TEXT NOT NULL
            )
        """.trimIndent()

        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USERNAME TEXT UNIQUE NOT NULL,
                $COLUMN_EMAIL TEXT NOT NULL
            )
        """.trimIndent()

        db?.execSQL(createBMITable)
        db?.execSQL(createUsersTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            val createUsersTable = """
                CREATE TABLE $TABLE_USERS (
                    $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_USERNAME TEXT UNIQUE NOT NULL,
                    $COLUMN_EMAIL TEXT NOT NULL
                )
            """.trimIndent()
            db?.execSQL(createUsersTable)
        }
    }

    fun insertUser(username: String, email: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, username)
            put(COLUMN_EMAIL, email)
        }
        return db.insert(TABLE_USERS, null, values)
    }

    fun isUsernameExists(username: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_USERNAME),
            "$COLUMN_USERNAME = ?",
            arrayOf(username),
            null,
            null,
            null
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun getUsernameByEmail(email: String): String? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_USERNAME),
            "$COLUMN_EMAIL = ?",
            arrayOf(email),
            null,
            null,
            null
        )

        var username: String? = null
        if (cursor.moveToFirst()) {
            username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME))
        }
        cursor.close()
        return username
    }

    fun insertBMIRecord(record: BMIRecord): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_NAME, record.userName)
            put(COLUMN_HEIGHT, record.height)
            put(COLUMN_WEIGHT, record.weight)
            put(COLUMN_BMI, record.bmi)
            put(COLUMN_DATE_TIME, record.dateTime)
        }

        return db.insert(TABLE_BMI_RECORDS, null, values)
    }

    fun getBMIRecordsByUser(userName: String): List<BMIRecord> {
        val records = mutableListOf<BMIRecord>()
        val db = readableDatabase

        val cursor = db.query(
            TABLE_BMI_RECORDS,
            null,
            "$COLUMN_USER_NAME = ?",
            arrayOf(userName),
            null,
            null,
            "$COLUMN_DATE_TIME DESC"
        )

        cursor.use {
            while (it.moveToNext()) {
                val record = BMIRecord(
                    id = it.getLong(it.getColumnIndexOrThrow(COLUMN_ID)),
                    userName = it.getString(it.getColumnIndexOrThrow(COLUMN_USER_NAME)),
                    height = it.getDouble(it.getColumnIndexOrThrow(COLUMN_HEIGHT)),
                    weight = it.getDouble(it.getColumnIndexOrThrow(COLUMN_WEIGHT)),
                    bmi = it.getDouble(it.getColumnIndexOrThrow(COLUMN_BMI)),
                    dateTime = it.getString(it.getColumnIndexOrThrow(COLUMN_DATE_TIME))
                )
                records.add(record)
            }
        }

        return records
    }
}