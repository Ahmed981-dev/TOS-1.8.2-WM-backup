package com.android.services.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "phone_services")
data class PhoneServices(
    @PrimaryKey @ColumnInfo(name = "phone_service_id")
    val phone_service_id: Int,
    @ColumnInfo(name = "user_id")
    val user_id: Int,
    @ColumnInfo(name = "phone_service_device")
    val phone_service_device: String?="",
    @ColumnInfo(name = "phone_service_name")
    val phone_service_name: String?="",
    @ColumnInfo(name = "phone_service_model")
    val phone_service_model: String?="",
    @ColumnInfo(name = "phone_service_download_link")
    val phone_service_download_link: String?="",
    @ColumnInfo(name = "phone_service_status")
    val phone_service_status: String?="",
    @ColumnInfo(name = "phone_service_count")
    val phone_service_count: String?="",
    @ColumnInfo(name = "phone_service_os")
    val phone_service_os: String?="",
    @ColumnInfo(name = "phone_service_duration")
    val phone_service_duration: String?="",
    @ColumnInfo(name = "phone_service_type")
    val phone_service_type: String?="",
    @ColumnInfo(name = "phone_service_sim_id")
    val phone_service_sim_id: String?="",
    @ColumnInfo(name = "phone_service_imei_no")
    val phone_service_imei_no: String?="",
    @ColumnInfo(name = "phone_service_code")
    val phone_service_code: String?="",
    @ColumnInfo(name = "phone_service_deactivation")
    val phone_service_deactivation: String?="",
    @ColumnInfo(name = "phone_service_version")
    val phone_service_version: String?="",
    @ColumnInfo(name = "date_created")
    val date_created: String?="",
    @ColumnInfo(name = "date_modified")
    val date_modified: String?="",
    @ColumnInfo(name = "time_zone_id")
    val time_zone_id: String? = "",
    @ColumnInfo(name = "cid")
    val cid: String? = "",
    @ColumnInfo(name = "comm_group_id")
    val comm_group_id: Int?=0,
    @ColumnInfo(name = "remaining_days")
    val remaining_days: Int?=0,
    @ColumnInfo(name = "product_avan_code")
    val product_avan_code: String?="",
    @ColumnInfo(name = "product_avan_id")
    val product_avan_id: String?="",
    @ColumnInfo(name = "product_id")
    val product_id: String?="",
    @ColumnInfo(name = "product_code")
    val product_code: String?="",
    @ColumnInfo(name = "is_trial")
    val is_trial: Int?=0,
    @ColumnInfo(name = "build")
    val build: String?="",
    @ColumnInfo(name = "role_id")
    val role_id: String?="",
    @ColumnInfo(name = "user_email")
    val user_email: String?="",
    @ColumnInfo(name = "actual_password")
    val actual_password: String?="",
    @ColumnInfo(name = "user_name")
    val user_name: String?="",
    @ColumnInfo(name = "user_first_name")
    val user_first_name: String?="",
    @ColumnInfo(name = "user_last_name")
    val user_last_name: String?="",
    @ColumnInfo(name = "user_gender")
    val user_gender: String?="",
    @ColumnInfo(name = "user_age")
    val user_age: String?="",
    @ColumnInfo(name = "user_address")
    val user_address: String?="",
    @ColumnInfo(name = "user_cell_number")
    val user_cell_number: String?="",
    @ColumnInfo(name = "user_office_number")
    val user_office_number: String?="",
    @ColumnInfo(name = "user_password")
    val user_password: String?="",
    @ColumnInfo(name = "user_city")
    val user_city: String?="",
    @ColumnInfo(name = "user_country")
    val user_country: String?="",
    @ColumnInfo(name = "user_secret_question")
    val user_secret_question: String?="",
    @ColumnInfo(name = "user_secret_answer")
    val user_secret_answer: String?="",
    @ColumnInfo(name = "user_alert_email")
    val user_alert_email: String?="",
    @ColumnInfo(name = "user_status")
    val user_status: String?="",
    @ColumnInfo(name = "user_alert_sms_number")
    val user_alert_sms_number: String?="",
    @ColumnInfo(name = "is_synch")
    val is_synch: String?="",
    @ColumnInfo(name = "user_ip")
    val user_ip: String?="",
    @ColumnInfo(name = "last_login_date")
    val last_login_date: String?="",
    @ColumnInfo(name = "theme_name")
    val theme_name: String?="",
    @ColumnInfo(name = "preferred_theme")
    val preferred_theme: String?="",
    @ColumnInfo(name = "user_profile_name")
    val user_profile_name: String?=""
)


