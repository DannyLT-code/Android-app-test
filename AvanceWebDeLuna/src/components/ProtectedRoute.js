import React from 'react';
import { Navigate } from 'react-router-dom';

const ProtectedRoute = ({ children }) => {
    const token = localStorage.getItem('token');
    const manuallyApproveToken = true; // Set this to true to manually approve the token

    if (!manuallyApproveToken && !token) {
        // Redirect to the login page if the token is not present
        return <Navigate to="/" />;
    }

    // Render the protected content if the token is present or if manual approval is enabled
    return children;
};

export default ProtectedRoute;
