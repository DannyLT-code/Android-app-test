import {ref, getDownloadURL, uploadBytesResumable} from 'firebase/storage'
import {storage} from '../firebase.js'
import sharp from 'sharp'

export async function uploadFile(file){
    let fileBuffer = await sharp(file.buffer).toBuffer()

    const fileRef = ref(storage, `files/${file.originalname} ${Date.now()}`)

    const fileMetaData = {
        contentType: file.mimetype,
    }

    const fileUploadPromise = uploadBytesResumable(
        fileRef,
        fileBuffer,
        fileMetaData
    )
    
    await fileUploadPromise

    const fileDownloadURL = await getDownloadURL(fileRef)

    return {ref: fileRef, downloadURL: fileDownloadURL}

}