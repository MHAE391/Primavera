package com.m391.primavera.utils.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Teachers")
data class LocalTeacherModel(
    @PrimaryKey val teacherId: String,
    @ColumnInfo(name = "first_name") val firstName: String,
    @ColumnInfo(name = "last_name") val lastName: String,
    @ColumnInfo(name = "phone") val phone: String,
    @ColumnInfo(name = "image") val image: ByteArray,
    @ColumnInfo(name = "image_uri") val imageUri: String,
    @ColumnInfo(name = "latitude") val latitude: Number,
    @ColumnInfo(name = "longitude") val longitude: Number,
    @ColumnInfo(name = "teacher_age") val age: String,
    @ColumnInfo(name = "academic_years") val academicYears: ArrayList<String>,
    @ColumnInfo(name = "subjects") val subjects: ArrayList<String>,
    @ColumnInfo(name = "rate") val rate: Number
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LocalTeacherModel

        if (teacherId != other.teacherId) return false
        if (firstName != other.firstName) return false
        if (lastName != other.lastName) return false
        if (phone != other.phone) return false
        if (!image.contentEquals(other.image)) return false
        if (imageUri != other.imageUri) return false
        if (latitude != other.latitude) return false
        if (longitude != other.longitude) return false
        if (age != other.age) return false
        if (academicYears != other.academicYears) return false
        if (subjects != other.subjects) return false
        if (rate != other.rate) return false

        return true
    }

    override fun hashCode(): Int {
        var result = teacherId.hashCode()
        result = 31 * result + firstName.hashCode()
        result = 31 * result + lastName.hashCode()
        result = 31 * result + phone.hashCode()
        result = 31 * result + image.contentHashCode()
        result = 31 * result + imageUri.hashCode()
        result = 31 * result + latitude.hashCode()
        result = 31 * result + longitude.hashCode()
        result = 31 * result + age.hashCode()
        result = 31 * result + academicYears.hashCode()
        result = 31 * result + subjects.hashCode()
        result = 31 * result + rate.hashCode()
        return result
    }
}
