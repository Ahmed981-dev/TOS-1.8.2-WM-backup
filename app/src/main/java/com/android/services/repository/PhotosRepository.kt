package com.android.services.repository

import com.android.services.db.dao.PhotosDao
import com.android.services.db.entities.Photos
import javax.inject.Inject

class PhotosRepository @Inject constructor(private val photosDao: PhotosDao) {

    fun insertPhotos(photos: List<Photos>) = photosDao.insert(photos)
    fun insertPhoto(Photos: Photos) = photosDao.insert(Photos)
    fun getPhotos(): List<Photos> = photosDao.selectAllImages(0)
    fun checkImageNotAlreadyExists(id: String): Boolean = photosDao.checkIfAlreadyExist(id) == null
    fun updatePhoto(id: String) = photosDao.update(1, id)

}