import {initializeApp} from 'firebase/app';
import { getStorage } from 'firebase/storage';

const firebaseConfig = {
    apiKey: "AIzaSyBHWV30fR2WepACJEMFnmQUXFVhiS6XlL8",
    authDomain: "imagenes-deluna.firebaseapp.com",
    projectId: "imagenes-deluna",
    storageBucket: "imagenes-deluna.appspot.com",
    messagingSenderId: "958073331164",
    appId: "1:958073331164:web:f21292f5442bc4c3af4319"
}

const firebaseApp = initializeApp(firebaseConfig);

export const storage = getStorage(firebaseApp);