import React from 'react';
import { useParams } from 'react-router-dom';

function EditUser() {
    const { id } = useParams();

    return (
        <div>
            <h2>Edit User</h2>
            <p>User ID: {id}</p>
            {/* Implement logic to fetch and display user details based on the ID */}
        </div>
    );
}

export default EditUser;
